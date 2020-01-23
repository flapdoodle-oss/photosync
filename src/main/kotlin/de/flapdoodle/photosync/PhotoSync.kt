package de.flapdoodle.photosync

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.diff.ScanDiffAnalyzer
import de.flapdoodle.photosync.diff.Scan
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filetree.TreeCollectorAdapter
import de.flapdoodle.photosync.filetree.FileTreeVisitorAdapter
import de.flapdoodle.photosync.filetree.ProgressReportFileTreeCollector
import de.flapdoodle.photosync.filetree.Tree
import de.flapdoodle.photosync.filetree.mapFiles
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.photosync.sync.SyncCommand2Command
import de.flapdoodle.photosync.sync.Diff2SyncCommands
import de.flapdoodle.photosync.sync.UnixCommandListRenderer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime

object PhotoSync {

  @JvmStatic
  fun main(vararg args: String) {
    require(args.size > 1) { "usage: <src> <dst>" }

    val start = LocalDateTime.now()
    var srcDiskSpaceUsed = 0L
    var dstDiskSpaceUsed = 0L

    val srcPath = Path.of(args[0])
    val dstPath = Path.of(args[1])

    val commands = Monitor.execute {
      val srcTree = Monitor.scope("scan files") {
        Monitor.message(srcPath.toString())
        tree(srcPath)
      }

      val src = Monitor.scope("scan") {
        scan(srcTree)
      }

      srcDiskSpaceUsed = src.diskSpaceUsed()

      val dstTree = Monitor.scope("scan files") {
        Monitor.message(dstPath.toString())
        tree(dstPath)
      }

      val dst = Monitor.scope("scan") {
        Monitor.message(dstPath.toString())
        scan(dstTree)
      }

      dstDiskSpaceUsed = dst.diskSpaceUsed()

      val diff = Monitor.scope("diff") {
        Monitor.message("src: ${src.diskSpaceUsed()}, dst: ${dst.diskSpaceUsed()}")
        ScanDiffAnalyzer.scan(src, dst, QuickHash)
      }

      val syncCommands = Diff2SyncCommands(srcPath, dstPath).generate(diff)

      SyncCommand2Command.map(syncCommands, srcTree, dstTree)
    }

    println()


    println()

    UnixCommandListRenderer.execute(commands)

    val end = LocalDateTime.now()

    println("- - - - - - - - - - - - - - - - -")
    println("Speed: ${Duration.between(start, end).toSeconds()}s")
    println("Source: ${srcDiskSpaceUsed / (1024 * 1024)} MB")
    println("Backup: ${dstDiskSpaceUsed / (1024 * 1024)} MB")
  }

  private fun scan(
      tree: Tree.Directory,
      hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash) }
  ): Scan {
    val blobs = tree.mapFiles { Blob(it.path, it.size, it.lastModifiedTime) }

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

}

