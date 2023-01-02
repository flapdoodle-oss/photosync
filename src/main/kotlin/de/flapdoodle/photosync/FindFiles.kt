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
import de.flapdoodle.io.layouts.splitted.FindFileInDestination
import de.flapdoodle.photosync.filehash.*
import de.flapdoodle.photosync.progress.Monitor

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

    val compareMode by option(
      "-C", "--compare", help = "compare (default is byName)"
    ).groupChoice(
      "name" to CompareMode.ByName(),
      "hash" to CompareMode.ByHashOnly()
    )

    sealed class CompareMode(name: String, val mode: FindFileInDestination.Compare)  : OptionGroup(name) {
      class ByName : CompareMode("name and hash", FindFileInDestination.Compare.ByName)
      class ByHashOnly : CompareMode("hash only", FindFileInDestination.Compare.ByHashOnly)
    }

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
          FindFileInDestination.find(src, dest, (compareMode?:CompareMode.ByName()).mode, MonitoringHashSelector(hashSelector))
        } else {
          Monitor.message("Nothing found")
          emptyList()
        }
      }

      val withoutMatches = matches.filter { it.dest.isEmpty() }
      val withMatches = matches
        .map { FindFileInDestination.Match(it.src, it.dest.filter { m -> m.type==FindFileInDestination.MatchType.SameContent }) }
        .filter { it.dest.isNotEmpty() }
      val withSizeMismatch = matches.filter { it.dest.isNotEmpty() && it.dest.all { d -> d.type==FindFileInDestination.MatchType.DifferentSize } }

      if (withMatches.isNotEmpty()) {
        println("match found:")
        println("----------------------------")
        withMatches.forEach { m ->
          printMatch(m)
        }
        println()
      }

      if (withSizeMismatch.isNotEmpty()) {
        println("size mismatch:")
        println("----------------------------")
        withSizeMismatch.forEach { m ->
          printMatch(m)
        }
        println()
      }

      if (withoutMatches.isNotEmpty()) {
        println("no matches for:")
        println("----------------------------")
        withoutMatches.forEach { m ->
          printMatch(m)
        }
        println()
      }
    }
  }

  private fun printMatch(match: FindFileInDestination.Match) {
    if (match.dest.size==1) {
      println("${match.src} ${match.dest[0].path}")
    } else {
      println(match.src)
      match.dest.forEach {
        println("  ${it.path}")
      }
      if (match.dest.isNotEmpty()) println()
    }
  }


  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}