package de.flapdoodle.dirsync.ui.views

import javafx.concurrent.Task
import javafx.scene.layout.Region
import tornadofx.*

class RunningTaskFragment(task: Task<out Any>) : Fragment() {
    override val root = hbox {
        val thisBox = this

        val bar = progressbar(property = task.progressProperty())
        val button = button("stop") {
            minWidth = Region.USE_PREF_SIZE
            action {
                task.cancel()
            }
        }
        label(task.messageProperty()) {
//            useMaxWidth = true
            prefWidthProperty().bind(thisBox.widthProperty().minus(bar.prefWidthProperty()).minus(button.prefWidthProperty()))
        }
    }
}