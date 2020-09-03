package de.flapdoodle.photosync.ui.tasks

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.bindFrom
import de.flapdoodle.fx.lazy.mapToList
import de.flapdoodle.photosync.ui.config.SyncEntry
import de.flapdoodle.photosync.ui.events.ActionEvent
import de.flapdoodle.photosync.Scanner
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.photosync.sync.NIOSynchronizer
import de.flapdoodle.photosync.sync.Synchronizer
import de.flapdoodle.photosync.ui.sync.SyncGroup
import de.flapdoodle.photosync.ui.sync.SyncList
import javafx.concurrent.Task
import javafx.scene.control.Alert
import tornadofx.*
import java.nio.file.Paths
import java.util.UUID
import kotlin.collections.LinkedHashMap

class TaskList : Fragment() {

    val runningTasks = ChangeableValue<Map<UUID, Task<out Any>>>(LinkedHashMap<UUID, Task<out Any>>())
    val tasks = runningTasks.mapToList { it.values.toList() }

    private val synchronizer: Synchronizer = NIOSynchronizer()

    override val root = vbox {
        children.bindFrom(tasks) { RunningTask(it).root }
    }

    class RunningTask(task: Task<out Any>) : Fragment() {
        override val root = hbox {
            val thisBox = this

            val bar = progressbar(property = task.progressProperty())
            label(task.messageProperty()) {
                prefWidthProperty().bind(thisBox.widthProperty().minus(bar.prefWidthProperty()))
            }
        }
    }

    fun startSync(config: SyncList, enableCopyBack: Boolean = false, enableRemove: Boolean = false) {
        val task = runAsync {
            synchronizer.sync(config,
                    enableCopyBack = enableCopyBack,
                    enableRemove = enableRemove,
                    listener = { id, command, status ->
                        runLater {
                            ActionEvent.synced(id, command, status).fire()
                        }
                    },
                    progressListener = { progress ->
                        updateProgress(progress.current, progress.max)
                    }
            )
        } success {
            ActionEvent.syncDone().fire()
        } fail {
            it.printStackTrace()
            ActionEvent.syncDone().fire()
        }
    }

    fun startScan(config: SyncEntry) {
        val id = config.id
        val srcPath = Paths.get(config.src)
        val dstPath = Paths.get(config.dst)
        val scanner = Scanner(srcPath, dstPath, filter = null, map = { commands, _, _ -> commands })

        val task = runAsync {
            val result = scanner.sync(
                    reporter = Monitor.Reporter { updateMessage(it) },
                    abort = { isCancelled },
                    progress = { current, max -> updateProgress(current.toLong(), max.toLong()) }
            )
            SyncList.map(srcPath, dstPath, result)
        } success {
            runningTasks.value { it - id }
            println("result for ${id} -> $it")

            ActionEvent.scanFinished(id, it).fire()
        } fail {
            runningTasks.value { it - id }

            val alert = Alert(Alert.AlertType.ERROR, it.message)
            alert.headerText = "sync"
            alert.showAndWait()

            ActionEvent.scanAborted(id).fire()
        } cancel {
            runningTasks.value { it - id }

            val alert = Alert(Alert.AlertType.INFORMATION, "Operation Cancelled")
            alert.headerText = "sync"
            alert.showAndWait()

            ActionEvent.scanAborted(id).fire()
        }

        runningTasks.value { it + (id to task) }
        ActionEvent.scanStarted(id).fire()

    }

    fun stopScan(id: UUID) {
        val task = runningTasks.value()[id]
        runningTasks.value { it - id }
        if (task != null && task.isRunning) {
            task.cancel()
        }
    }
}