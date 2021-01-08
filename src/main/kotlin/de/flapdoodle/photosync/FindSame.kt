package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.layouts.same.ExpectSameLayout
import de.flapdoodle.io.layouts.single.FindSameContent
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filehash.SizeHash
import de.flapdoodle.photosync.progress.Monitor

object FindSame {

  class Args : CliktCommand() {
    init {
      context {
        allowInterspersedArgs = false
      }
    }

    val source by argument("source")
            .path(
                    mustExist = true,
                    canBeFile = false,
                    canBeDir = true
            ).validate {
              require(it.toFile().isDirectory) { "is not a directory" }
            }

    override fun run() {
      val diff = Monitor.execute {
        val srcTree = FileTrees.asTree(source, listener = {
          Monitor.message("source $it")
        })
        Monitor.message("DONE")

        FindSameContent.find(srcTree, listOf(MonitoringHasher(SizeHash), MonitoringHasher(QuickHash)))
      }
      println()
    }
  }

  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}