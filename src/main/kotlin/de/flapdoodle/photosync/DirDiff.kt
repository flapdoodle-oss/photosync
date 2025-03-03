package de.flapdoodle.photosync

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.path
import de.flapdoodle.io.filetree.FileTrees
import de.flapdoodle.io.filetree.diff.Action
import de.flapdoodle.io.filetree.diff.Actions
import de.flapdoodle.io.filetree.diff.Sync
import de.flapdoodle.io.filetree.diff.samelayout.Diff
import de.flapdoodle.io.filetree.diff.samelayout.SameLayoutSync
import de.flapdoodle.photosync.file.PersistentFileAttributeCache
import de.flapdoodle.photosync.filehash.*
import de.flapdoodle.photosync.filehash.cache.PersistentHashCache
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.photosync.progress.Statistic
import kotlin.system.exitProcess

object DirDiff {
  sealed class SyncMode(name: String, val copy: Sync.Copy, val leftover: Sync.Leftover) : OptionGroup(name) {
    class OnlyNew : SyncMode("copy new, dont remove leftovers", Sync.Copy.ONLY_NEW, Sync.Leftover.IGNORE)
    class Changes : SyncMode("copy if changed, remove leftovers", Sync.Copy.IF_CHANGED, Sync.Leftover.IGNORE)
    class AllChanges : SyncMode("copy if changed, copy back removed", Sync.Copy.IF_CHANGED, Sync.Leftover.COPY_BACK)
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

    val cacheDir by option("-C", "--cacheDir", help = "cache dir").path(
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
      "changes" to SyncMode.Changes(),
      "all-changes" to SyncMode.AllChanges()
    )

    sealed class Mode(name: String) : OptionGroup(name) {
      object Report : Mode("report")
      object Sync : Mode("sync")
    }

    val mode by option(
      "-m","--mode", help = "mode (default is sync)"
    ).groupChoice(
      "report" to Mode.Report,
      "sync" to Mode.Sync
    )

    override fun run() {
      val actions = Statistic.collectAndReport {
        val hashSelector = when (hashMode) {
          is HashMode.Full -> HashSelector.always(FullHash)
          is HashMode.Quick -> HashSelector.always(SizedQuickHash)
          else -> MimeTypeHashSelector.defaultConfig()
        }

        val hashCache: HashCache? = if (cacheDir!=null) {
          val fileAttributeCache = PersistentFileAttributeCache(cacheDir!!)
          PersistentHashCache(fileAttributeCache)
        } else {
          null
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
          if (src != null && dest != null) {
            Diff.diff(src, dest, MonitoringHashSelector(hashSelector), hashCache)
          } else {
            Diff(source, destination, emptyList())
          }
        }

        SameLayoutSync(copy, leftover)
          .actions(diff)
      }

      println()

      val succeed: Boolean = when (mode ?: Mode.Sync) {
        is Mode.Report -> {
          printReport(actions)
          true
        }
        is Mode.Sync -> sync(actions)
      }

      if (!succeed) exitProcess(1)
    }

    private fun printReport(actions: List<Action>) {
      Actions.asHumanReadable(actions)
        .forEach(::println)
    }

    private fun sync(actions: List<Action>): Boolean {
      printReport(actions)
      
      return if (actions.isNotEmpty()) {
        println("... proceed? (yes|no)")

        when (readLine()) {
          "yes" -> {
            Monitor.execute {
              Actions.execute(actions) { current, size, action ->
                Monitor.message("$current of $size -> ${Actions.asHumanReadable(action)} ")
              }
            }
            true
          }

          else -> {
            println("... aborted")
            false
          }
        }
      } else {
        true
      }
    }
  }

  @JvmStatic
  fun main(vararg args: String) {
    Args().main(args.toList())
  }
}