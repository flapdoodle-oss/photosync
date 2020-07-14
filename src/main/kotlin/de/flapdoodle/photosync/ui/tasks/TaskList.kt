package de.flapdoodle.photosync.ui.tasks

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.bindFrom
import de.flapdoodle.fx.lazy.mapToList
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.events.ActionEvent
import javafx.concurrent.Task
import javafx.scene.control.Alert
import javafx.scene.control.ProgressBar
import tornadofx.*
import java.util.*
import kotlin.collections.LinkedHashMap

class TaskList : Fragment() {

    val runningTasks = ChangeableValue<Map<UUID, Task<out Any>>>(LinkedHashMap<UUID, Task<out Any>>())
    val tasks = runningTasks.mapToList { it.values.toList() }

    override val root = vbox {
        button("TaskList") {
            
        }

        children.bindFrom(tasks) { RunningTask(it).root}
    }

    class RunningTask(task: Task<out Any>) : Fragment() {
        override val root = hbox {
            label(task.messageProperty())
            progressbar(property = task.progressProperty())
        }
    }

    fun startSync(id: UUID, startSync: (UUID) -> String) {
        val task: Task<String> = runAsync {
            val result = startSync(id)

            (0..100L).forEach {
                if (!isCancelled) {
                    updateMessage("running")
                    updateProgress(it, 100)
                    Thread.sleep(30)
                }
            }

            result
        } success {
            runningTasks.value { it - id }
            println("result for ${id} -> $it")

            ActionEvent.syncFinished(id).fire()
        } fail {
            runningTasks.value { it - id }

            val alert = Alert(Alert.AlertType.ERROR, it.message)
            alert.headerText = "sync"
            alert.showAndWait()

            ActionEvent.syncAborted(id).fire()
        } cancel {
            runningTasks.value { it - id }

            val alert = Alert(Alert.AlertType.INFORMATION, "Operation Cancelled")
            alert.headerText = "sync"
            alert.showAndWait()

            ActionEvent.syncAborted(id).fire()
        }

        runningTasks.value { it + (id to task) }
        ActionEvent.syncStarted(id).fire()

    }

    fun stopSync(id: UUID) {
        val task = runningTasks.value()[id]
        runningTasks.value { it - id }
        if (task!=null && task.isRunning) {
            task.cancel()
        }
    }
}