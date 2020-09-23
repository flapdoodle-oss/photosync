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

                    ActionEvent.scanFinished(event.action.id)
                }
//
//                tasks.async(event.action.id, {
//                    Thread.sleep(1000)
//                    this.updateMessage("One")
//                    Thread.sleep(1000)
//                    this.updateMessage("Two")
//                    Thread.sleep(1000)
//                    this.updateMessage("3")
//                    "works"
//                }, onSuccess = { result ->
//                    ActionEvent.scanFinished(event.action.id).fire()
//                });
            }
        }
    }

    private fun startScan(config: SyncEntry) {
        val id = config.id
        val srcPath = Paths.get(config.src)
        val dstPath = Paths.get(config.dst)
        val filter: (Path) -> Boolean =
                if (config.excludes.isEmpty())
                    { it -> true }
                else
                    { it -> true }

        val scanner = Scanner(srcPath, dstPath, filter = filter)
        tasks.async(id, {
            val result = scanner.sync(
                    reporter = Monitor.Reporter { updateMessage(it) },
                    abort = { isCancelled },
                    progress = { current, max -> updateProgress(current.toLong(), max.toLong()) }
            )
        }, onSuccess = {
            ActionEvent.scanFinished(id).fire()
        })
    }
}