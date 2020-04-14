package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.diff.DiffEntry
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
import de.flapdoodle.photosync.sync.Diff2CopySourceCommands
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

  // see https://ajalt.github.io/clikt/quickstart/
  sealed class Mode(name: String) : OptionGroup(name) {
    class Merge : Mode("options for merge mode")
    class CopySource : Mode("options for copy source mode")
  }

  class Args : CliktCommand() {
    init {
      context {
        allowInterspersedArgs = false
      }
    }

    val mode by option(
        "-m", "--mode", help = "mode (default is merge)"
    ).groupChoice(
        "merge" to Mode.Merge(),
        "copy-source" to Mode.CopySource()
    )

    val source by argument("source")
        .path(
            mustExist = true,
            canBeFile = false,
            canBeDir = true
        ).validate {
          require(it.toFile().isDirectory) { "is not a directory" }
        }

    val destination by argument("destination")
        .path(
            mustExist = true,
            canBeFile = false,
            canBeDir = true
        ).validate {
          require(it.toFile().isDirectory) { "is not a directory" }
        }

    val pattern by option(help = "file pattern regex").convert {
      Pattern.compile(it)
    }

    override fun run() {
//      echo("done")
//      echo("search in destination: $searchInDestination")
//      echo("$source -- $destination ($pattern)")

      val filter = pattern?.let { asFilter(it) }

      sync(
          source,
          destination,
          filter = filter,
          mode = mode ?: Mode.Merge()
      )
    }
  }

  @JvmStatic
  fun main(vararg args: String) {
    if (false) {
      launch<PhotoSyncUI>(*args)
    }

    Args().main(args.toList())
//    if (true) {
//      return
//    }
//
//    if (args.isEmpty()) {
//      launch<PhotoSyncUI>(*args)
//    }
//    require(args.size > 1) { "usage: <src> <dst> <regex pattern?>" }
//
//
//    val srcPath = Path.of(args[0])
//    val dstPath = Path.of(args[1])
//
//    require(srcPath.toFile().isDirectory) { "$srcPath is not a directory" }
//    require(dstPath.toFile().isDirectory) { "$dstPath is not a directory" }
//
//    val filter: ((Path) -> Boolean)? = if (args.size > 2) {
//      asFilter(Pattern.compile(args[2]))
//    } else
//      null
//
//    sync(srcPath, dstPath, filter)
  }

  private fun asFilter(pattern: Pattern): ((Path) -> Boolean)? {
    return { path: Path -> path.matches(pattern) }
  }

  private fun sync(
      srcPath: Path,
      dstPath: Path,
      filter: ((Path) -> Boolean)?,
      mode: Mode = Mode.Merge()
  ) {
    val start = LocalDateTime.now()
    var srcDiskSpaceUsed = 0L
    var dstDiskSpaceUsed = 0L

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

      when (mode) {
        is Mode.Merge -> {
          val syncCommands = Diff2SyncCommands(srcPath, dstPath,
              sameContent = Diff2SyncCommands.sameContent(hasher)
          ).generate(diff)

          SyncCommand2Command.map(syncCommands, srcTree, dstTree)
        }
        is Mode.CopySource -> {
          val syncCommands = Diff2CopySourceCommands(srcPath, dstPath,
              sameContent = Diff2SyncCommands.sameContent(hasher)
          ).generate(diff)

          SyncCommand2Command.map(syncCommands, srcTree, dstTree)
        }
      }
//      val filteredDiff = diffFilter(diff)
//
//      val syncCommands = Diff2SyncCommands(srcPath, dstPath,
//          sameContent = Diff2SyncCommands.sameContent(hasher),
//          lastModifiedComparision = lastModifiedComparision
//      ).generate(filteredDiff)
//
//      SyncCommand2Command.map(syncCommands, srcTree, dstTree)
    }

    println()


    println()

    println("- - - - - - - - - - - - - - - - -")
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

