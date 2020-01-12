package de.flapdoodle.photosync.sync

import java.nio.file.Path

object UnixCommandListRenderer : CommandExecutor {
  override fun execute(commands: List<CommandGroup>) {
    commands.forEach {
      commandsFor(it)
    }
  }

  private fun commandsFor(group: CommandGroup) {
    group.commands.forEach {
      when (it) {
        is Command.Copy -> printCommand("cp", it.src, it.dst)
        is Command.Move -> printCommand("mv", it.src, it.dst)
        is Command.Remove -> printCommand("#rm", it.path)
      }
    }
  }

  private fun printCommand(command: String, src: Path, dst: Path) {
    println("$command $src $dst")
  }

  private fun printCommand(command: String, src: Path) {
    println("$command $src")
  }
}