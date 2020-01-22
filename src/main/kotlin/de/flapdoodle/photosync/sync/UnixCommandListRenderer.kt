package de.flapdoodle.photosync.sync

import java.nio.file.Path

object UnixCommandListRenderer : CommandExecutor {
  override fun execute(commands: List<SyncCommandGroup>) {
    commands.forEach {
      it.commands.forEach {
        when (it) {
          is SyncCommand.Copy -> printCommand("cp", it.src, it.dst)
          is SyncCommand.Move -> printCommand("mv", it.src, it.dst)
          is SyncCommand.Remove -> {}
        }
      }
    }

    println("---------------------------")
    println("unused image copies")
    println("---------------------------")
    commands.forEach {
      it.commands.forEach {
        when (it) {
          is SyncCommand.Remove -> if (it.cause==SyncCommand.Cause.CopyRemovedFromSource) printCommand("rm", it.dst)
        }
      }
    }

    println("---------------------------")
    println("removed source images")
    println("---------------------------")
    commands.forEach {
      it.commands.forEach {
        when (it) {
          is SyncCommand.Remove -> if (it.cause==SyncCommand.Cause.DeletedEntry) printCommand("rm", it.dst)
        }
      }
    }
  }

  private fun asString(cause: SyncCommand.Cause): String {
    return when (cause) {
      SyncCommand.Cause.DeletedEntry -> "deleted"
      SyncCommand.Cause.CopyRemovedFromSource -> "copy removed from source"
    }
  }

  private fun printCommand(command: String, src: Path, dst: Path) {
    println("$command $src $dst")
  }

  private fun printCommand(command: String, src: Path) {
    println("$command $src")
  }
}