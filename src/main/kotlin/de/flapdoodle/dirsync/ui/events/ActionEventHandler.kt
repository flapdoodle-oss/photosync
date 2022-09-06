package de.flapdoodle.dirsync.ui.events

import de.flapdoodle.dirsync.ui.config.SyncConfig
import de.flapdoodle.dirsync.ui.config.SyncEntry
import de.flapdoodle.dirsync.ui.io.Scanner
import de.flapdoodle.dirsync.ui.tasks.Tasks
import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.photosync.progress.Monitor
import javafx.scene.control.Alert
import tornadofx.EventContext
import java.lang.Exception
import java.nio.file.Path
import java.nio.file.Paths

class ActionEventHandler(
        private val tasks: Tasks,
        private val currentConfig: LazyValue<SyncConfig>
) : EventHandlerFactory<ActionEvent> {
    override fun instance(): EventContext.(ActionEvent) -> Unit = { event ->
        when (event.action) {
            is ActionEvent.Action.StartScan -> {
                try {
                    startScan(currentConfig.value().entry(event.action.id))
                } catch (ex: Exception) {
                    val alert = Alert(Alert.AlertType.ERROR, ex.message)
                    alert.headerText = "scan"
                    alert.showAndWait()

                    ActionEvent.scanAborted(event.action.id)
                }
            }
            is ActionEvent.Action.ScanFinished -> {
                println("---------------------------------")
                println("diff")
                event.action.diff.diffEntries.forEach {
                    println(it)
                }
                println("---------------------------------")
            }
            is ActionEvent.Action.StopScan -> {
                println("stop scap called")
                tasks.stop(event.action.id)
            }
            else -> throw IllegalArgumentException("unexpected")
        }
    }

    private fun startScan(config: SyncEntry) {
        val id = config.id
        val srcPath = Paths.get(config.src)
        val dstPath = Paths.get(config.dst)
        val filter: (Path) -> Boolean =
                if (config.excludes.isNotEmpty()) {
                    { path ->
                        val asString = path.fileName.toString()
                        val visitPath = config.excludes.none { part -> asString.contains(part) }
                        visitPath
                    }
                } else
                    { it -> true }

        val scanner = Scanner(srcPath, dstPath, filter = filter)
        tasks.async(id, {
            val result = scanner.sync(
                    reporter = Monitor.Reporter { updateMessage(it) },
                    abort = { isCancelled },
                    progress = { current, max -> updateProgress(current.toLong(), max.toLong()) }
            )
            result
        }, onSuccess = {
            ActionEvent.scanFinished(id, it).fire()
        }, onCancel = {
            ActionEvent.scanAborted(id).fire()
        }, onFail = {
            it.printStackTrace()
            ActionEvent.scanAborted(id).fire()
        })
    }
}