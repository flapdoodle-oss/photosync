package de.flapdoodle.photosync

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.diff.Scan
import de.flapdoodle.photosync.diff.ScanDiffAnalyzer
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filetree.FileTreeVisitorAdapter
import de.flapdoodle.photosync.filetree.ProgressReportFileTreeCollector
import de.flapdoodle.photosync.filetree.Tree
import de.flapdoodle.photosync.filetree.TreeCollectorAdapter
import de.flapdoodle.photosync.filetree.mapFiles
import de.flapdoodle.photosync.paths.matches
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.photosync.sync.Diff2SyncCommands
import de.flapdoodle.photosync.sync.SyncCommand2Command
import de.flapdoodle.photosync.sync.UnixCommandListRenderer
import de.flapdoodle.photosync.ui.PhotoSyncUI
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.regex.Pattern

object PhotoSync {

  @JvmStatic
  fun main(vararg args: String) {
    if (args.isEmpty()) {
      launch<PhotoSyncUI>(*args)
    }
    require(args.size > 1) { "usage: <src> <dst> <regex pattern?>" }

    val start = LocalDateTime.now()
    var srcDiskSpaceUsed = 0L
    var dstDiskSpaceUsed = 0L

    val srcPath = Path.of(args[0])
    val dstPath = Path.of(args[1])

    require(srcPath.toFile().isDirectory) { "$srcPath is not a directory" }
    require(dstPath.toFile().isDirectory) { "$dstPath is not a directory" }

    val filter: ((Path) -> Boolean)? = if (args.size > 2) {
      val pattern: Pattern = Pattern.compile(args[2])
      val ret: ((Path) -> Boolean)? = { path: Path -> path.matches(pattern) }
      println("path filter enabled: $pattern (${args[2]})")
      ret
    } else
      null


    val hasher = QuickHash

    val commands = Monitor.execute {
      val srcTree = Monitor.scope("scan files") {
        Monitor.message(srcPath.toString())
        tree(srcPath)
      }

      val src = Monitor.scope("scan") {
        scan(srcTree, filter = filter)
      }

      srcDiskSpaceUsed = src.diskSpaceUsed()

      val dstTree = Monitor.scope("scan files") {
        Monitor.message(dstPath.toString())
        tree(dstPath)
      }

      val dst = Monitor.scope("scan") {
        Monitor.message(dstPath.toString())
        scan(dstTree, filter = filter)
      }

      dstDiskSpaceUsed = dst.diskSpaceUsed()

      val diff = Monitor.scope("diff") {
        Monitor.message("src: ${src.diskSpaceUsed()}, dst: ${dst.diskSpaceUsed()}")
        ScanDiffAnalyzer.scan(src, dst, hasher)
      }

      val syncCommands = Diff2SyncCommands(srcPath, dstPath,
          sameContent = Diff2SyncCommands.sameContent(hasher)
      ).generate(diff)

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
      hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash) },
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

}

