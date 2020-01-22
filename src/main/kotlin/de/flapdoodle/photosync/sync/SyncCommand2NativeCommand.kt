package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.expectParent
import de.flapdoodle.photosync.filetree.Tree
import de.flapdoodle.photosync.filetree.containsExactly
import de.flapdoodle.photosync.filetree.find
import de.flapdoodle.photosync.filetree.get

object SyncCommand2NativeCommand {

  private fun bulkMove(commands: List<SyncCommand.Move>, root: Tree.Directory): SyncCommand.Move? {
    val bulkMove = commands.bulkMove()
    if (bulkMove!=null) {
      val source = root.get(bulkMove.src)
      if (source.containsExactly(commands.map { it.src })) {
        return bulkMove
      }
    }
    return null
  }

  fun rewrite(commands: List<SyncCommandGroup>, src: Tree.Directory, dst: Tree.Directory) {
    val moveCommands = commands.flatMap { it.commands.filterIsInstance<SyncCommand.Move>() }

    println("move commands:")
    moveCommands.forEach { println("-> $it") }

    val movesForSameOrigin = moveCommands.groupBy {
      it.src.expectParent()
    }

    movesForSameOrigin.forEach {
      val bulkMove = bulkMove(it.value, dst)
      if (bulkMove != null) {
        // same destination
        println("got all files, should check for destination directory")
        val newDestination = bulkMove.dst;
        if (dst.find(newDestination)!=null) {
          println("directory $newDestination exists")
          // check if empty?
        } else {
          println("directory $newDestination does NOT exist")
        }
      }
    }
  }

}