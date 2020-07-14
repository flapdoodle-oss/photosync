package de.flapdoodle.photosync.ui.tasks

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.events.ActionEvent
import javafx.concurrent.Task
import javafx.scene.control.Alert
import tornadofx.*
import java.util.*

class TaskList : Fragment() {

    val runningTasks = ChangeableValue(emptyMap<UUID, Task<out Any>>())

    override val root = hbox {
        button("TaskList") {
            
        }
    }

    class RunningTask : Fragment() {
        override val root = vbox {
            button("running...")
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