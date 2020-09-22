package de.flapdoodle.dirsync.ui.views

import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.bindFrom
import javafx.concurrent.Task
import tornadofx.Fragment
import tornadofx.vbox

class TaskListFragment(tasks: LazyValue<List<Task<out Any>>>) : Fragment() {
    override val root = vbox {
        children.bindFrom(tasks) { RunningTaskFragment(it).root }
    }
}