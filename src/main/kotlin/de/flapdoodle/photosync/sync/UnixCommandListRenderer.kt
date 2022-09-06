package de.flapdoodle.photosync.sync

import java.nio.file.Path

object UnixCommandListRenderer : CommandExecutor {
  override fun execute(commands: List<Command>) {
    val removeCommands = commands.filter { it is Command.Remove }
    val copyBackCommands = commands.filter { it is Command.CopyBack }
    val moveAndCopy = commands - removeCommands - copyBackCommands

    require(moveAndCopy.size + removeCommands.size + copyBackCommands.size == commands.size) {
      "filtering is wrong for $commands"
    }

    moveAndCopy.forEach {
      when (it) {
        is Command.Copy -> {
          if (!it.sameContent) {
            printCommand("cp --preserve=timestamps", it.src, it.dst)
          }
          printCommand("touch -r", it.src, it.dst)
        }
//        is Command.CopyBack -> {}
        is Command.Move -> printCommand("mv", it.src, it.dst)
        is Command.MkDir -> printCommand("mkdir", it.dst)
        is Command.BulkMove -> printCommand("mv", it.src.resolve("*"), it.dst)
//        is Command.Remove -> { }
        else -> {
          throw IllegalArgumentException("should not happen")
        }
      }
    }

    if (copyBackCommands.isNotEmpty()) {
      println("---------------------------")
      println("destination is newer")
      println("---------------------------")
      copyBackCommands.forEach {
        when (it) {
          is Command.CopyBack -> {
            if (!it.sameContent) {
              printCommand("cp --preserve=timestamps", it.dst, it.src)
            }
            printCommand("touch -r", it.dst, it.src)
          }
          else -> {
            throw IllegalArgumentException("should not happen")
          }
        }
      }
    }

    if (removeCommands.isNotEmpty()) {
      println("---------------------------")
      println("unused image copies")
      println("---------------------------")
      removeCommands.forEach {
        when (it) {
          is Command.Remove -> if (it.cause == Command.Cause.CopyRemovedFromSource) printCommand("rm", it.dst)
          else -> {
            throw IllegalArgumentException("should not happen")
          }
        }
      }

      println("---------------------------")
      println("removed source images")
      println("---------------------------")
      removeCommands.forEach {
        when (it) {
          is Command.Remove -> if (it.cause == Command.Cause.DeletedEntry) printCommand("rm", it.dst)
          else -> {
            throw IllegalArgumentException("should not happen")
          }
        }
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
    println("$command ${src.escaped()} ${dst.escaped()}")
  }

  private fun printCommand(command: String, src: Path) {
    println("$command ${src.escaped()}")
  }

  private fun Path.escaped(): String {
    return this.toString().replace(" ","\\ ").replace("(","\\(").replace(")","\\)")
  }
}