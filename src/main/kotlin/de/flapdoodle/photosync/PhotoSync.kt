package de.flapdoodle.photosync

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.diff.ScanDiffAnalyzer
import de.flapdoodle.photosync.collector.BlobCollector
import de.flapdoodle.photosync.collector.FileVisitorAdapter
import de.flapdoodle.photosync.collector.ProgressReportPathCollector
import de.flapdoodle.photosync.collector.TreeCollector
import de.flapdoodle.photosync.diff.FileTree
import de.flapdoodle.photosync.diff.Scan
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.progress.Monitor
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
      val src = Monitor.scope("scan") {
        Monitor.message(srcPath.toString())
        scan(srcPath)
      }

      srcDiskSpaceUsed = src.diskSpaceUsed()

      val dst = Monitor.scope("scan") {
        Monitor.message(dstPath.toString())
        scan(dstPath)
      }

      dstDiskSpaceUsed = dst.diskSpaceUsed()

      val diff = Monitor.scope("diff") {
        Monitor.message("src: ${src.diskSpaceUsed()}, dst: ${dst.diskSpaceUsed()}")
        ScanDiffAnalyzer.scan(src, dst, QuickHash)
      }

      Diff2SyncCommands(src.path, dst.path).generate(diff)
    }

    println()

    Monitor.execute {
      val src = Monitor.scope("scan tree") {
        Monitor.message(srcPath.toString())

        val treeCollector = TreeCollector()
        Files.walkFileTree(srcPath, FileVisitorAdapter(treeCollector.andThen(ProgressReportPathCollector())))
        FileTree.of(treeCollector.dirs, treeCollector.files)
      }

      val dst = Monitor.scope("scan tree") {
        Monitor.message(dstPath.toString())

        val treeCollector = TreeCollector()
        Files.walkFileTree(dstPath, FileVisitorAdapter(treeCollector.andThen(ProgressReportPathCollector())))
        FileTree.of(treeCollector.dirs, treeCollector.files)
      }

      //CommandListSimplifier.rewrite(commands, src.dirs, dst.dirs)
    }

    println()
    
    UnixCommandListRenderer.execute(commands)

    val end = LocalDateTime.now()

    println("- - - - - - - - - - - - - - - - -")
    println("Speed: ${Duration.between(start, end).toSeconds()}s")
    println("Source: ${srcDiskSpaceUsed / (1024 * 1024)} MB")
    println("Backup: ${dstDiskSpaceUsed / (1024 * 1024)} MB")
  }

  private fun scan(
      path: Path,
      hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash) }
  ): Scan {
    val blobCollector = BlobCollector()
    Monitor.scope("collect") {
      Files.walkFileTree(path, FileVisitorAdapter(blobCollector.andThen(ProgressReportPathCollector())))
    }

    val groupMeta = GroupMetaData(blobCollector.blobs())
    val groupedByContent = GroupSameContent(
        blobs = groupMeta.baseBlobs(),
        hashStrategy = hashStrategy
    )

    return Scan.of(path, groupedByContent, groupMeta)
  }

}