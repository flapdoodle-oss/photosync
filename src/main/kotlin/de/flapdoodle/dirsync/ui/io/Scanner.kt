package de.flapdoodle.dirsync.ui.io

import de.flapdoodle.io.filetree.*
import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

class Scanner(
        val srcPath: Path,
        val dstPath: Path,
        val filter: ((Path) -> Boolean) = { true }
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
    ): TreeDiff {
        val start = LocalDateTime.now()

        val hasher = QuickHash

        return Monitor.execute(reporter) {
            val steps = 3
            progress(0, steps)

            val srcTree = Monitor.scope("scan source") {
                Monitor.message(srcPath.toString())
                tree(srcPath, abort)
            }

            progress(1, steps)
            if (abort()) throw AbortedException()

//            val src = Monitor.scope("scan") {
//                scan(srcTree)
//            }

//            progress(2, steps)
//            if (abort()) throw AbortedException()

            val dstTree = Monitor.scope("scan destination") {
                Monitor.message(dstPath.toString())
                tree(dstPath, abort)
            }

            progress(2, steps)
            if (abort()) throw AbortedException()

//            val dst = Monitor.scope("scan") {
//                Monitor.message(dstPath.toString())
//                scan(dstTree)
//            }

            val diff = Monitor.scope("diff") {
                TreeDiff.diff(srcTree, dstTree, HashStrategy { listOf(hasher)});
            }

            progress(3, steps)
            if (abort()) throw AbortedException()

//            val diff = Monitor.scope("diff") {
//                Monitor.message("src: ${src.diskSpaceUsed()}, dst: ${dst.diskSpaceUsed()}")
//                ScanDiffAnalyzer.scan(src, dst, hasher)
//            }
//
//            progress(5, steps)
//            if (abort()) throw AbortedException()
//
//            progress(6, steps)
//            val end = LocalDateTime.now()
//            Result(
//                    result = diff,
//                    srcDiskSpaceUsed = src.diskSpaceUsed(),
//                    dstDiskSpaceUsed = dst.diskSpaceUsed(),
//                    start = start,
//                    end = end
//            )
            diff
        }
    }

    private fun scan(
            tree: Tree.Directory,
            hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash) },
            filter: ((Path) -> Boolean)? = null
    ): GroupSameContent {
        val filteredTree = if (filter != null)
            tree.filterChildren(filter)
        else
            tree

        val blobs = filteredTree.mapFiles { Blob(it.path, it.size, it.lastModifiedTime) }

        val groupedByContent = GroupSameContent(
                blobs = blobs,
                hashStrategy = hashStrategy
        )

        return groupedByContent
    }

    private fun tree(path: Path,abort: () -> Boolean): Tree.Directory {
        val collector = TreeCollectorAdapter()
        Files.walkFileTree(path, FileTreeVisitorAdapter(collector.withFilter(filter)
                .withAbort { _ -> abort() }
                .andThen(ProgressReportFileTreeCollector())))
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