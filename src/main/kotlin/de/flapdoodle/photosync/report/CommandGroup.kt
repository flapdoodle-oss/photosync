package de.flapdoodle.photosync.report

data class CommandGroup(
    val commands: List<Command> = emptyList()
) {

  operator fun plus(other: CommandGroup): CommandGroup {
    return CommandGroup(this.commands + other.commands)
  }

  operator fun plus(command: Command): CommandGroup {
    return CommandGroup(this.commands + command)
  }
}