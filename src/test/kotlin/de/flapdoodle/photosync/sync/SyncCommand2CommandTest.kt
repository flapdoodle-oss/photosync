package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.FileTimes
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filetree.Tree
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class SyncCommand2CommandTest {

  @Test
  fun `simplify move commands if source dir and dest dir are the same and all files are moved`() {
    val srcDir = Path.of("src")
    val dstDir = Path.of("dst")


    val commands = listOf(SyncCommandGroup(listOf(
        SyncCommand.Move(dstDir.resolve("name").resolve("one"), dstDir.resolve("newName").resolve("one")),
        SyncCommand.Move(dstDir.resolve("name").resolve("two"), dstDir.resolve("newName").resolve("two"))
    )))

    val srcTree = Tree.Directory(srcDir, emptyList())
    val dstTree = Tree.Directory(dstDir, listOf(
        Tree.Directory(dstDir.resolve("name"), listOf(
            Tree.File(dstDir.resolve("name").resolve("one"), 0, LastModified.now()),
            Tree.File(dstDir.resolve("name").resolve("two"), 0, LastModified.now())
        ))
    ))

    SyncCommand2Command.map(commands,srcTree,dstTree)
  }
}