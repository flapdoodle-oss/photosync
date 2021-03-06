package de.flapdoodle.dirsync.ui.tasks

import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.mapToList
import javafx.concurrent.Task
import tornadofx.*
import java.util.*
import kotlin.collections.LinkedHashMap

class Tasks {
    private val runningTasks = ChangeableValue<Map<UUID, Task<out Any>>>(LinkedHashMap())
    private val tasks: LazyValue<List<Task<out Any>>> = runningTasks.mapToList { it.values.toList() }

    fun <R : Any> async(
            id: UUID,
            action: FXTask<*>.() -> R,
            onSuccess: (R) -> Unit,
            onFail: (Throwable) -> Unit = { it.printStackTrace() },
            onCancel: () -> Unit = { }
    ) {
        val task = runAsync {
            action()
        } success {
            runningTasks.value { it - id }
            onSuccess(it)
        } fail { ex ->
            runningTasks.value { it - id }
            onFail(ex)
        } cancel {
            runningTasks.value { it - id }
            onCancel()
        }
        runningTasks.value { it + (id to task) }
    }

    fun stop(id: UUID) {
        runningTasks.value { map ->
            map.get(id)?.cancel()
            map - id
        }
    }

    fun list() = tasks
}