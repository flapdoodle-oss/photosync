package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.filetree.FileTrees
import de.flapdoodle.io.layouts.splitted.FindFileInDestination
import de.flapdoodle.photosync.filehash.*
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

object FindFiles {

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

    val destination by argument("destination")
      .path(
        mustExist = true,
        canBeFile = false,
        canBeDir = true
      ).validate {
        require(it.toFile().isDirectory) { "is not a directory" }
      }

    val hashMode by option(
      "-H", "--hash", help = "hash (default is mimetype)"
    ).groupChoice(
      "quick" to HashMode.Quick(),
      "full" to HashMode.Full()
    )

    override fun run() {
      val hashSelector = when (hashMode) {
        is HashMode.Full -> HashSelector.always(FullHash)
        is HashMode.Quick -> HashSelector.always(SizedQuickHash)
        else -> FastHashSelector.defaultMapping()
      }

      val matches = Monitor.execute {
        val src = FileTrees.walkFileTree(source, listener = {
          Monitor.message("source $it")
        })
        val dest = FileTrees.walkFileTree(destination, listener = {
          Monitor.message("destination $it")
        })
        Monitor.message("DONE")
        if (src!=null && dest!=null) {
          FindFileInDestination.find(src, dest, MonitoringHashSelector(hashSelector))
        } else {
          Monitor.message("Nothing found")
          emptyList()
        }
      }

      val withoutMatches = matches.filter { it.dest.isEmpty() }
      val withMatches = matches.filter { it.dest.isNotEmpty() }

      println("match found:")
      println("----------------------------")
      withMatches.forEach { m ->
        println(m.src)
        m.dest.forEach { it: Path ->
          println(" -> $it")
        }
        println()
      }

      println("no matches for:")
      println("----------------------------")
      withoutMatches.forEach { m ->
        println(m.src)
      }
      
//      val diff = Monitor.execute {
//        val srcTree = FileTrees.asTree(source, listener = {
//          Monitor.message("source $it")
//        })
//        Monitor.message("DONE")
//
//        FindSameContent.find(srcTree, listOf(MonitoringHasher(SizeHash), MonitoringHasher(QuickHash)))
//      }
      println()
    }
  }

  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}