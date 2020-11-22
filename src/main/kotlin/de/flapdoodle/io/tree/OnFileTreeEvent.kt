package de.flapdoodle.io.tree

import java.nio.file.Path

fun interface OnFileTreeEvent {
    enum class Action {
        Continue,
        Skip,
        Abort
    };

    fun onEvent(event: FileTreeEvent): Action

    fun andThen(delegate: OnFileTreeEvent): OnFileTreeEvent {
        val that = this
        return OnFileTreeEvent {
            val first = that.onEvent(it)
            val second = delegate.onEvent(it)
            when {
                first==Action.Abort || second==Action.Abort -> Action.Abort
                first==Action.Skip || second==Action.Skip -> Action.Skip
                else -> Action.Continue
            }
        }
    }

    fun withFilter(filter: (Path) -> Boolean): OnFileTreeEvent {
        val that = this
        return OnFileTreeEvent { it ->
            when (it) {
                is FileTreeEvent.Enter -> {
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