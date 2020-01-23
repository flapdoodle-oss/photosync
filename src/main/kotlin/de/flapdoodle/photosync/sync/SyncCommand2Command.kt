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

  private fun map2BulkMove(commands: List<SyncCommand.Move>, root: Tree.Directory): List<Command> {
    val bulkMove = bulkMove(commands, root)

    return if (bulkMove != null) {
      if (root.find(bulkMove.dst) == null) {
        listOf(Command.MkDir(bulkMove.dst), bulkMove)
      } else {
        listOf(bulkMove)
      }
    } else {
      emptyList()
    }
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

  fun map(commands: List<SyncCommandGroup>, src: Tree.Directory, dst: Tree.Directory): List<Command> {
    val moveCommands = commands.flatMap { it.commands.filterIsInstance<SyncCommand.Move>() }

//    println("move commands:")
//    moveCommands.forEach { println("-> $it") }

    val movesForSameOrigin = moveCommands.groupBy {
      it.src.expectParent()
    }

    val bulkMoves = movesForSameOrigin.entries.flatMap { (_, commands) ->
      map2BulkMove(commands, dst)
    }

    val moveCommandsReplacedByBulkMove = bulkMoves.flatMap { if (it is Command.BulkMove) it.cause else emptyList() }

    val result = commands.flatMap { commandGroup ->
      map(commandGroup) { moveCommandsReplacedByBulkMove.contains(it) }
    }

    return bulkMoves + result
  }

}