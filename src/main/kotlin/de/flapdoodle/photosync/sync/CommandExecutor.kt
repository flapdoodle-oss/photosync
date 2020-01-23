package de.flapdoodle.photosync.sync

interface CommandExecutor {
  fun execute(commands: List<Command>)
}