package de.flapdoodle.dirsync.ui.views

import javafx.concurrent.Task
import tornadofx.*

class RunningTaskFragment(task: Task<out Any>) : Fragment() {
    override val root = hbox {
        val thisBox = this

        val bar = progressbar(property = task.progressProperty())
        label(task.messageProperty()) {
            prefWidthProperty().bind(thisBox.widthProperty().minus(bar.prefWidthProperty()))
        }
    }
}