package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.expectParent
import de.flapdoodle.photosync.filetree.Tree
import de.flapdoodle.photosync.filetree.containsExactly
import de.flapdoodle.photosync.filetree.find
import de.flapdoodle.photosync.filetree.get

object SyncCommand2Command {

  private fun bulkMove(commands: List<SyncCommand.Move>, root: Tree.Directory): Command.BulkMove? {
    val bulkMove = commands.bulkMove()
    if (bulkMove != null) {
      val source = root.get(bulkMove.src)
      if (source.containsExactly(commands.map { it.src })) {
        return bulkMove
      }
    }
    return null
  }

  private fun map2BulkMove(commands: List<SyncCommand.Move>, root: Tree.Directory): Command? {
    return bulkMove(commands, root)
  }

  private fun map(group: SyncCommandGroup, removeCommand: (SyncCommand) -> Boolean): List<Command> {
    val ret = group.commands.map {
      when (it) {
        is SyncCommand.Move -> if (!removeCommand(it)) Command.Move(it.src,it.dst) else null
        is SyncCommand.Copy -> Command.Copy(it.src, it.dst)
        is SyncCommand.Remove -> Command.Remove(it.dst, when(it.cause) {
          SyncCommand.Cause.DeletedEntry -> Command.Cause.DeletedEntry
          SyncCommand.Cause.CopyRemovedFromSource -> Command.Cause.CopyRemovedFromSource
        })
      }
    }
    return ret.filterNotNull()
  }

  private fun createMissingDirectories(commands: List<Command>, dst: Tree.Directory): List<Command> {
    val destinationDirectories = commands.flatMap {
      when (it) {
        is Command.Move -> listOf(it.dst.expectParent())
        is Command.Copy -> listOf(it.dst.expectParent())
        is Command.BulkMove -> listOf(it.dst)
        else -> emptyList()
      }
    }.toSet()

    val missingDirectories = destinationDirectories.filter { dst.find(it) == null }

    return missingDirectories.map(Command::MkDir) + commands
  }

  fun map(commands: List<SyncCommandGroup>, src: Tree.Directory, dst: Tree.Directory): List<Command> {
    val moveCommands = commands.flatMap { it.commands.filterIsInstance<SyncCommand.Move>() }

    val movesForSameOrigin = moveCommands.groupBy {
      it.src.expectParent()
    }

    val bulkMoves = movesForSameOrigin.entries.mapNotNull { (_, commands) ->
      map2BulkMove(commands, dst)
    }

    val moveCommandsReplacedByBulkMove = bulkMoves.flatMap { if (it is Command.BulkMove) it.cause else emptyList() }

    val result = commands.flatMap { commandGroup ->
      map(commandGroup) { moveCommandsReplacedByBulkMove.contains(it) }
    }

    return createMissingDirectories(bulkMoves + result, dst)
  }

}