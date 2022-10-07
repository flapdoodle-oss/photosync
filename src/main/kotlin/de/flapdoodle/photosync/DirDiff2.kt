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
import de.flapdoodle.io.filetree.NodeTreeCollector
import de.flapdoodle.io.filetree.diff.samelayout.Diff
import de.flapdoodle.photosync.filehash.FullHash
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.SizedQuickHash
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.types.Either
import java.nio.file.Path

object DirDiff2 {

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
      "-H", "--hash", help = "hash (default is quick)"
    ).groupChoice(
      "quick" to HashMode.Quick(),
      "full" to HashMode.Full()
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
      val hash = when (hashMode ?: HashMode.Quick()) {
        is HashMode.Full -> FullHash
        is HashMode.Quick -> SizedQuickHash
      }

      val diff = Monitor.execute {
        val src = FileTrees.walkFileTree(source, listener = {
          Monitor.message("source $it")
        })
        val dest = FileTrees.walkFileTree(destination, listener = {
          Monitor.message("destination $it")
        })
        Monitor.message("DONE")
        if (src!=null && dest!=null) {
          Diff.diff(src, dest, MonitoringHasher(hash))
        } else {
          Diff(source,destination, emptyList())
        }
      }

      println()

      when (mode ?: Mode.Report) {
        is Mode.Report -> printReport(diff)
        is Mode.Sync -> sync(diff)
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
          is Diff.Entry.Removed -> println("${asPath(src, entry.dest)} X-- ${asPath(dest, entry.dest)}")
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