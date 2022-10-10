package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.filetree.FileTrees
import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.filetree.diff.Action
import de.flapdoodle.io.filetree.diff.samelayout.Diff
import de.flapdoodle.io.filetree.diff.samelayout.Sync
import de.flapdoodle.photosync.filehash.*
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.types.Either
import java.nio.file.Path

object DirDiff2 {
  sealed class SyncMode(name: String, val copy: Sync.Copy, val leftover: Sync.Leftover) : OptionGroup(name) {
    class OnlyNew : SyncMode("copy new, dont remove leftovers", Sync.Copy.ONLY_NEW, Sync.Leftover.IGNORE)
    class Changes : SyncMode("copy if changed, remove leftovers", Sync.Copy.IF_CHANGED, Sync.Leftover.IGNORE)
  }

  class Args : CliktCommand() {
    init {
      context {
        allowInterspersedArgs = false
      }
    }

    val source by argument("source").path(
      mustExist = true, canBeFile = false, canBeDir = true
    ).validate {
      require(it.toFile().isDirectory) { "is not a directory" }
    }

    val destination by argument("destination").path(
      mustExist = true, canBeFile = false, canBeDir = true
    ).validate {
      require(it.toFile().isDirectory) { "is not a directory" }
    }

    val hashMode by option(
      "-H", "--hash", help = "hash (default is mimetype)"
    ).groupChoice(
      "quick" to HashMode.Quick(),
      "full" to HashMode.Full()
    )

    val syncMode by option(
      "-S", "--sync", help = "sync mode (default is only new)"
    ).groupChoice(
      "onlyNew" to SyncMode.OnlyNew(),
      "changes" to SyncMode.Changes()
    )

    sealed class Mode(name: String) : OptionGroup(name) {
      object Report : Mode("report")
      object Sync : Mode("sync")
    }

    val mode by option(
      "-m","--mode", help = "mode (default is report)"
    ).groupChoice(
      "report" to Mode.Report,
      "sync" to Mode.Sync
    )

    override fun run() {
      val hashSelector = when (hashMode) {
        is HashMode.Full -> HashSelector.always(FullHash)
        is HashMode.Quick -> HashSelector.always(SizedQuickHash)
        else -> FastHashSelector.defaultMapping()
      }

      val copy = (syncMode ?: SyncMode.OnlyNew()).copy
      val leftover = (syncMode ?: SyncMode.OnlyNew()).leftover

      val diff = Monitor.execute {
        val src = FileTrees.walkFileTree(source, listener = {
          Monitor.message("source $it")
        })
        val dest = FileTrees.walkFileTree(destination, listener = {
          Monitor.message("destination $it")
        })
        Monitor.message("DONE")
        if (src!=null && dest!=null) {
          Diff.diff(src, dest, MonitoringHashSelector(hashSelector))
        } else {
          Diff(source,destination, emptyList())
        }
      }

      val actions = Sync(copy, leftover)
        .actions(diff)

      println()

      when (mode ?: Mode.Report) {
        is Mode.Report -> printReport(actions)
        is Mode.Sync -> sync(diff)
      }
    }

    private fun printReport(actions: List<Action>) {
      actions.forEach { action ->
        when (action) {
          is Action.CopyFile ->
            println("cp ${action.src} ${action.dest} #size=${action.size}")
          is Action.SetLastModified ->
            println("touch ${action.dest} ${action.lastModified}")
          is Action.MakeDirectory ->
            println("mkdir ${action.dest}")
          is Action.Remove ->
            println("rm ${action.dest}")
        }
      }
    }

    private fun printReport(diff: Diff) {
      printReport(diff.src, diff.dest, diff.entries)
    }

    private fun printReport(src: Path, dest: Path, entries: List<Diff.Entry>) {
      entries.forEach { entry ->
        when (entry) {
          is Diff.Entry.TypeMismatch -> println("${asPath(src, entry.src)} != ${asPath(dest, entry.dest)}")
          is Diff.Entry.Missing -> println("${asPath(src, entry.src)} --X ${asPath(dest, entry.src)}")
          is Diff.Entry.Leftover -> println("${asPath(src, entry.dest)} X-- ${asPath(dest, entry.dest)}")
          is Diff.Entry.FileChanged -> {
            if (entry.src.lastModifiedTime > entry.dest.lastModifiedTime) {
              println("${asPath(src, entry.src)} --> ${asPath(dest, entry.dest)}")
            } else {
              println("${asPath(src, entry.src)} <-- ${asPath(dest, entry.dest)}")
            }
          }
          is Diff.Entry.SymLinkChanged -> {
            if (entry.src.lastModifiedTime > entry.dest.lastModifiedTime) {
              println("${asPath(src, entry.src)} --> ${asPath(dest, entry.dest)}")
              println("  ${asPath(src, entry.src)} == ${asPath(src, entry.src.destination)}")
              println("  ${asPath(dest, entry.dest)} == ${asPath(dest, entry.dest.destination)}")
            } else {
              println("${asPath(src, entry.src)} <-- ${asPath(dest, entry.dest)}")
            }
          }
          is Diff.Entry.DirectoryChanged -> {
            if (entry.src.lastModifiedTime > entry.dest.lastModifiedTime) {
              println("${asPath(src, entry.src)} --> ${asPath(dest, entry.dest)}")
            } else {
              println("${asPath(src, entry.src)} <-- ${asPath(dest, entry.dest)}")
            }
            printReport(asPath(src, entry.src), asPath(dest, entry.dest), entry.entries)
          }
          else -> {
            // skip
          }
        }
      }
    }

    private fun asPath(base: Path, destination: Either<Node.NodeReference, Path>): Path {
      throw NotImplementedError()
    }

    private fun asPath(base: Path, node: Node): Path {
      return base.resolve(node.name)
    }

    private fun sync(diff: Diff) {
      sync(diff.src, diff.dest, diff.entries)
    }

    private fun sync(src: Path, dest: Path, entries: List<Diff.Entry>) {
      // create list of IO operations
      TODO("not implemented")
    }
  }

  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}