package de.flapdoodle.dirsync.ui.events

import de.flapdoodle.dirsync.ui.tasks.Tasks
import de.flapdoodle.fx.extensions.fire
import tornadofx.EventContext

class ActionEventHandler(private val tasks: Tasks) : EventHandlerFactory<ActionEvent> {
    override fun instance(): EventContext.(ActionEvent) -> Unit = { event ->
        when (event.action) {
            is ActionEvent.Action.StartScan -> {
                tasks.async(event.action.id, {
                    Thread.sleep(1000)
                    this.updateMessage("One")
                    Thread.sleep(1000)
                    this.updateMessage("Two")
                    Thread.sleep(1000)
                    this.updateMessage("3")
                    "works"
                }, onSuccess = { result ->
                    ActionEvent.scanFinished(event.action.id).fire()
                });
            }
        }
    }
}