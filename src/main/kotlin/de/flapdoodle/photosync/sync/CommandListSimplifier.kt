package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.expectParent
import de.flapdoodle.photosync.filetree.Tree
import de.flapdoodle.photosync.filetree.containsExactly
import de.flapdoodle.photosync.filetree.find
import de.flapdoodle.photosync.filetree.get

object CommandListSimplifier {

  fun rewrite(commands: List<CommandGroup>, src: Tree.Directory, dst: Tree.Directory) {
    val moveCommands = commands.flatMap { it.commands.filterIsInstance<Command.Move>() }

    println("move commands:")
    moveCommands.forEach { println("-> $it") }

    val movesForSameOrigin = moveCommands.groupBy {
      it.dst.expectParent()
    }

    movesForSameOrigin.forEach {
      val newDestination = it.value.map { it.newDst.expectParent() }.toSet().singleOrNull()
      if (newDestination != null) {
        // same destination
        println("could simplify move for ${it.key}, must check if we see all files")
        val directory = dst.get(it.key)
        println("found directory: ${directory.path}")
        val sourceFiles = it.value.map { it.dst }
        if (directory.containsExactly(sourceFiles)) {
          println("got all files, should check for destination directory")
          if (newDestination.toFile().exists()) {
            println("directory $newDestination exists")
          } else {
            println("directory $newDestination does NOT exist")
          }
        } else {
          println("got only a partial list of files")
        }
      }
    }
  }

}