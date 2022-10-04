package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.layouts.same.ExpectSameLayout
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filehash.SizeHash
import de.flapdoodle.photosync.filehash.SizedQuickHash
import de.flapdoodle.photosync.progress.Monitor

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
        val src = de.flapdoodle.io.filetree.FileTrees.walkFileTree(source, listener = {
          Monitor.message("source $it")
        })
        val dest = de.flapdoodle.io.filetree.FileTrees.walkFileTree(destination, listener = {
          Monitor.message("destination $it")
        })
        Monitor.message("DONE")
        if (src!=null && dest!=null) {
          de.flapdoodle.io.filetree.diff.samelayout.Diff.diff(src, dest, MonitoringHasher(SizedQuickHash))
        } else {
          de.flapdoodle.io.filetree.diff.samelayout.Diff(source,destination, emptyList())
        }
      }

//      val diff = Monitor.execute {
//        val srcTree = FileTrees.asTree(source, listener = {
//          Monitor.message("source $it")
//        })
//        val dstTree = FileTrees.asTree(destination, listener = {
//          Monitor.message("destination $it")
//        })
//        Monitor.message("DONE")
//
//        ExpectSameLayout.diff(srcTree, dstTree, listOf(MonitoringHasher(SizeHash), MonitoringHasher(QuickHash)))
//      }
      println()
      

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
  }

  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}