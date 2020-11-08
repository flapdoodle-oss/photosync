package de.flapdoodle.io.tree

fun interface OnFileTreeEvent {
    enum class Action {
        Continue,
        Skip,
        Abort
    };

    fun onEvent(event: FileTreeEvent) : Action;
}