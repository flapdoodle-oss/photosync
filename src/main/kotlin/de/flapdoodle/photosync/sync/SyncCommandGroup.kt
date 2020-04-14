package de.flapdoodle.photosync.sync

data class SyncCommandGroup(
    val commands: List<SyncCommand> = emptyList()
) {

  operator fun plus(other: SyncCommandGroup): SyncCommandGroup {
    return SyncCommandGroup(this.commands + other.commands)
  }

  operator fun plus(command: SyncCommand?): SyncCommandGroup {
    return if (command!=null)
      SyncCommandGroup(this.commands + command)
    else
      this
  }
}