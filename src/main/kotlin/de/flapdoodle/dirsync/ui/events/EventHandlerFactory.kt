package de.flapdoodle.dirsync.ui.events

import tornadofx.EventContext

interface EventHandlerFactory<T> {
    fun instance(): EventContext.(T) -> Unit
}