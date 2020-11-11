package de.flapdoodle.io.tree

import java.nio.file.Path

fun interface OnFileTreeEvent {
    enum class Action {
        Continue,
        Skip,
        Abort
    };

    fun onEvent(event: FileTreeEvent): Action;

    fun withFilter(filter: (Path) -> Boolean): OnFileTreeEvent {
        val that = this
        return OnFileTreeEvent { it ->
            when (it) {
                is FileTreeEvent.Down -> {
                    if (filter(it.path))
                        that.onEvent(it)
                    else
                        Action.Skip
                }
                else -> {
                    if (filter(it.path))
                        that.onEvent(it)
                    else
                        Action.Continue
                }
            }
        }
    }
}