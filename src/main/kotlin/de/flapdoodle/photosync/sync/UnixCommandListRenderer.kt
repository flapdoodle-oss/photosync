package de.flapdoodle.photosync.sync

import java.nio.file.Path

object UnixCommandListRenderer : CommandExecutor {
  override fun execute(commands: List<Command>) {
    commands.forEach {
      when (it) {
        is Command.Copy -> printCommand("cp", it.src, it.dst)
        is Command.Move -> printCommand("mv", it.src, it.dst)
        is Command.MkDir -> printCommand("mkdir", it.dst)
        is Command.BulkMove -> printCommand("mv", it.src.resolve("*"), it.dst)
        is Command.Remove -> { }
      }
    }

    println("---------------------------")
    println("unused image copies")
    println("---------------------------")
    commands.forEach {
      when (it) {
        is Command.Remove -> if (it.cause == Command.Cause.CopyRemovedFromSource) printCommand("rm", it.dst)
      }
    }

    println("---------------------------")
    println("removed source images")
    println("---------------------------")
    commands.forEach {
      when (it) {
        is Command.Remove -> if (it.cause == Command.Cause.DeletedEntry) printCommand("rm", it.dst)
      }
    }
  }

  private fun asString(cause: Command.Cause): String {
    return when (cause) {
      Command.Cause.DeletedEntry -> "deleted"
      Command.Cause.CopyRemovedFromSource -> "copy removed from source"
    }
  }

  private fun printCommand(command: String, src: Path, dst: Path) {
    println("$command $src $dst")
  }

  private fun printCommand(command: String, src: Path) {
    println("$command $src")
  }
}