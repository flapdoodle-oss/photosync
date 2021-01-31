package de.flapdoodle.io.resolve.commands

interface SyncCommandMapper<S> {
    fun syncCommandsFor(src: S): List<Command>
}