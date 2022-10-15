package de.flapdoodle.photosync

import de.flapdoodle.io.filetree.*
import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.diff.Scan
import de.flapdoodle.photosync.diff.ScanDiffAnalyzer
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.photosync.sync.Diff2CopySourceCommands
import de.flapdoodle.photosync.sync.Diff2SyncCommands
import de.flapdoodle.photosync.sync.SyncCommandGroup
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

class Scanner<T>(
        val srcPath: Path,
        val dstPath: Path,
        val filter: ((Path) -> Boolean)?,
        val map: (commands: List<SyncCommandGroup>, src: Tree.Directory, dst: Tree.Directory) -> T,
        val mode: Mode = Mode.Merge(),
        val hasher: Hasher<*> = QuickHash
) {
     init {
         val srcAsFile = srcPath.toFile()
         require(srcAsFile.exists()) {"$srcPath does not exist" }
         require(srcAsFile.isDirectory) {"$srcPath is not a directory" }

         val dstAsFile = dstPath.toFile()
         require(dstAsFile.exists()) {"$dstPath does not exist" }
         require(dstAsFile.isDirectory) {"$dstPath is not a directory" }

     }

    fun sync(
            reporter: Monitor.Reporter = Monitor.ConsoleReporter(),
            abort: () -> Boolean = { false },
            progress: (Int, Int) -> Unit = { _,_ -> }
    ): Result<T> {
        val start = LocalDateTime.now()

        return Monitor.execute(reporter) {
            val steps = 6
            progress(0, steps)

            val srcTree = Monitor.scope("scan files") {
                Monitor.message(srcPath.toString())
                tree(srcPath)
            }

            progress(1, steps)
            if (abort()) throw AbortedException()

            val src = Monitor.scope("scan") {
                scan(srcTree, filter = filter, hashStrategy = HashStrategy{ listOf(hasher) })
            }

            progress(2, steps)
            if (abort()) throw AbortedException()

            val dstTree = Monitor.scope("scan files") {
                Monitor.message(dstPath.toString())
                tree(dstPath)
            }

            progress(3, steps)
            if (abort()) throw AbortedException()

            val dst = Monitor.scope("scan") {
                Monitor.message(dstPath.toString())
                scan(dstTree, filter = filter, hashStrategy = HashStrategy{ listOf(hasher) })
            }

            progress(4, steps)
            if (abort()) throw AbortedException()

            val diff = Monitor.scope("diff") {
                Monitor.message("src: ${src.diskSpaceUsed()}, dst: ${dst.diskSpaceUsed()}")
                ScanDiffAnalyzer.scan(src, dst, hasher)
            }

            progress(5, steps)
            if (abort()) throw AbortedException()

            val syncCommands = when (mode) {
                is Mode.Merge -> {
                    Diff2SyncCommands(srcPath, dstPath,
                            sameContent = Diff2SyncCommands.sameContent(hasher)
                    ).generate(diff)
                }
                is Mode.CopySource -> {
                    Diff2CopySourceCommands(srcPath, dstPath,
                            sameContent = Diff2SyncCommands.sameContent(hasher)
                    ).generate(diff)
                }
            }

            progress(6, steps)
            val end = LocalDateTime.now()
            Result(
                    result = map(syncCommands, srcTree, dstTree),
                    srcDiskSpaceUsed = src.diskSpaceUsed(),
                    dstDiskSpaceUsed = dst.diskSpaceUsed(),
                    start = start,
                    end = end
            )
        }
    }

    private fun scan(
            tree: Tree.Directory,
            hashStrategy: HashStrategy,
            filter: ((Path) -> Boolean)? = null
    ): Scan {
        val filteredTree = if (filter != null)
            tree.filterChildren(filter)
        else
            tree

        val blobs = filteredTree.mapFiles { Blob(it.path, it.size, it.lastModifiedTime) }

        val groupMeta = GroupMetaData(blobs)
        val groupedByContent = GroupSameContent(
                blobs = groupMeta.baseBlobs(),
                hashStrategy = hashStrategy
        )

        return Scan.of(groupedByContent, groupMeta)
    }

    private fun tree(path: Path): Tree.Directory {
        val collector = TreeCollectorAdapter()
        Files.walkFileTree(path, FileTreeVisitorAdapter(collector.andThen(ProgressReportFileTreeCollector())))
        return collector.asTree()
    }

    data class Result<T>(
            val result: T,
            val srcDiskSpaceUsed: Long,
            val dstDiskSpaceUsed: Long,
            val start: LocalDateTime,
            val end: LocalDateTime
    )

    class AbortedException : Exception()
}