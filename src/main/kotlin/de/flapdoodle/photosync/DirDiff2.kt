package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.filetree.FileTrees
import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.filetree.NodeTreeCollector
import de.flapdoodle.io.filetree.diff.samelayout.Diff
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

    override fun run() {
      val diff = Monitor.execute {
        val src = FileTrees.walkFileTree(source, listener = {
          Monitor.message("source $it")
        })
        val dest = FileTrees.walkFileTree(destination, listener = {
          Monitor.message("destination $it")
        })
        Monitor.message("DONE")
        if (src!=null && dest!=null) {
          Diff.diff(src, dest, MonitoringHasher(SizedQuickHash))
        } else {
          Diff(source,destination, emptyList())
        }
      }

      println()
      
      printReport(diff)
//      diff.forEach {
//        when (it) {
//          is Diff.SourceIsMissing -> println("${it.expectedPath}? - ${it.dst.path}")
//
//          is Diff.DestinationIsMissing -> println("${it.src.path} - ${it.expectedPath}?")
//
//          is Diff.TypeMismatch -> println("${it.src.javaClass.simpleName} (${it.src.path}) != ${it.dst.javaClass.simpleName} (${it.dst.path})")
//
//          is Diff.SymLinkMissmatch -> {
//            println("${it.src.path} (1) != ${it.dst.path} (2)")
//            println("1)-> ${it.src.destination}")
//            println("2)-> ${it.dst.destination}")
//          }
//
//          is Diff.ContentMismatch -> {
//            println("${it.src.path} != ${it.dst.path}")
//          }
//
//          is Diff.TimeStampMissmatch -> {
//            println("${it.src.path} != ${it.dst.path} (${it.src.lastModified} != ${it.dst.lastModified}")
//          }
//        }
//      }
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
  }

  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}