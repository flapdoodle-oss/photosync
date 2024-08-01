package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.filetree.FileTrees
import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.layouts.splitted.FindDuplicatesByHash
import de.flapdoodle.photosync.filehash.*
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.photosync.progress.Statistic
import java.util.regex.Pattern

object FindDuplicates {

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

    val safeHash by option(
      "-S", "--safe", help = "use fullhash"
    ).flag()

    val filter by option(
      "-F","--filter", help = "filter by regex"
    )

    val negateFilter by option(
      "-N", "--negate", help = "negate filter"
    ).flag()

    val debug by option("-D", "--debug").flag()

    override fun run() {
      Statistic.collectAndReport {
        val matches = Monitor.execute {
          val src = FileTrees.walkFileTree(source, listener = {
            Monitor.message("source $it")
          })
          val filtered = if (filter != null) {
            filter(src, filter!!, negateFilter)
          } else {
            src
          }

          Monitor.message("DONE")
          if (filtered != null) {
            FindDuplicatesByHash.find(filtered, safeHash)
          } else {
            Monitor.message("Nothing found")
            emptyMap()
          }
        }

        matches.forEach { (grouped, paths) ->
          when (grouped) {
            is FindDuplicatesByHash.Grouped.BySize -> {
              require(paths.size == 1)
              if (debug) println("single: ${paths[0]}")
            }

            is FindDuplicatesByHash.Grouped.ByHash -> {
              if (paths.size > 1) {
                if (debug) println("hash: ${grouped.hash}")
                else println("--")
                paths.forEach {
                  println("  $it")
                }
                println()
              } else {
                if (debug) println("single: ${paths[0]}")
              }
            }
          }
        }
      }
    }
  }

  private fun filter(top: Node.Top?, filter: String, negate: Boolean): Node.Top? {
    if (top!=null) {
      val pattern = Pattern.compile(filter)
      return filter(top, pattern!!, negate)
    }
    return top
  }

  private fun filter(top: Node.Top, filter: Pattern, negate: Boolean): Node.Top {
    return top.copy(children = filter(top.children, filter, negate))
  }

  private fun filter(children: List<Node>, filter: Pattern, negate: Boolean): List<Node> {
    return children.filter { c ->
      when (c) {
        is Node.File -> {
          negate.xor(filter.matcher(c.name).find())
        }
        else -> true
      }
    }
      .map { c ->
        when (c) {
          is Node.Directory -> {
            c.copy(children = filter(c.children, filter, negate))
          }
          else -> c
        }
      }
  }

  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}