package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.filetree.diff.Action
import de.flapdoodle.io.filetree.diff.Sync
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.div

internal class SyncTest {
  private val srcPath = Paths.get("src")
  private val destPath = Paths.get("dest")
  private val now = LastModified.now()

  @Nested
  inner class Missing {
    @Test
    fun copyFileIfMissing() {
      val actions = SameLayoutSync.create(
        srcPath, destPath, Diff.Entry.Missing.MissingFile(
          Node.File("s", now, 123L)
        )
      )

      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s", destPath / "s", 123L, false),
          Action.SetLastModified(destPath / "s", now)
        )
    }

    @Test
    fun copyDirectoryIfMissing() {
      val actions = SameLayoutSync.create(
        srcPath, destPath, Diff.Entry.Missing.MissingDirectory(
          Node.Directory("sub", now + 1),
          listOf(Diff.Entry.Missing.MissingFile(
            Node.File("s", now, 123L)
          ))
        )
      )

      assertThat(actions)
        .hasSize(4)
        .containsExactlyInAnyOrder(
          Action.MakeDirectory(destPath / "sub"),
          Action.CopyFile(srcPath / "sub" / "s", destPath / "sub" / "s", 123L, false),
          Action.SetLastModified(destPath / "sub" / "s", now),
          Action.SetLastModified(destPath / "sub", now + 1)
        )
    }
  }

  @Nested
  inner class Leftovers {
    @Test
    fun removeFile() {
      val actions = SameLayoutSync.remove(
        destPath, Diff.Entry.Leftover.LeftoverFile(
          Node.File("d", now, 123L)
        )
      )

      assertThat(actions)
        .hasSize(1)
        .containsExactlyInAnyOrder(
          Action.Remove(destPath / "d")
        )
    }

    @Test
    fun removeSymlink() {
      val actions = SameLayoutSync.remove(
        destPath, Diff.Entry.Leftover.LeftoverSymLink(
          Node.SymLink("d", now, Node.NodeReference("x"))
        )
      )

      assertThat(actions)
        .hasSize(1)
        .containsExactlyInAnyOrder(
          Action.Remove(destPath / "d")
        )
    }

    @Test
    fun removeDirectory() {
      val actions = SameLayoutSync.remove(
        destPath, Diff.Entry.Leftover.LeftoverDirectory(
          Node.Directory("sub", now, emptyList()),
          listOf(
            Diff.Entry.Leftover.LeftoverFile(
              Node.File("d", now, 123L)
            )
          )
        )
      )

      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.Remove(destPath / "sub" / "d"),
          Action.Remove(destPath / "sub")
        )
    }

    @Test
    fun copyFileBack() {
      val actions = SameLayoutSync.copyBack(srcPath, destPath, Diff.Entry.Leftover.LeftoverFile(
        Node.File("d", now, 123L)
      ))
      
      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.CopyFile(destPath / "d", srcPath / "d", 123L, false),
          Action.SetLastModified(srcPath / "d", now)
        )
    }

    @Test
    fun copyDirectoryBack() {
      val actions = SameLayoutSync.copyBack(srcPath, destPath, Diff.Entry.Leftover.LeftoverDirectory(
        Node.Directory("sub", now + 1, emptyList()),
        listOf(
          Diff.Entry.Leftover.LeftoverFile(
            Node.File("d", now, 123L)
          )
        )
      ))

      assertThat(actions)
        .hasSize(4)
        .containsExactlyInAnyOrder(
          Action.MakeDirectory(srcPath / "sub"),
          Action.CopyFile(destPath / "sub" / "d", srcPath / "sub" / "d", 123L, false),
          Action.SetLastModified(srcPath / "sub" / "d", now),
          Action.SetLastModified(srcPath / "sub" , now + 1)
        )
    }
  }

  @Nested
  inner class FileChanges {

    @Test
    fun copyFileIfIsNewerAndContentHasChanged() {
      val actions = SameLayoutSync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now + 1, 123L),
          Node.File("d", now, 123L),
          true
        ), Sync.Copy.ONLY_NEW
      )

      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s", destPath / "d", 123L, true),
          Action.SetLastModified(destPath / "d", now + 1)
        )
    }

    @Test
    fun setLastModifiedIfContentHasNotChangedButIsNewer() {
      val actions = SameLayoutSync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now + 1, 123L),
          Node.File("d", now, 123L),
          false
        ), Sync.Copy.ONLY_NEW
      )

      assertThat(actions)
        .hasSize(1)
        .containsExactlyInAnyOrder(
          Action.SetLastModified(destPath / "d", now + 1)
        )
    }

    @Test
    fun dontCopyFileOrSetLastModifiedIfNotNewer() {
      val actions = SameLayoutSync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now, 123L),
          Node.File("d", now, 123L),
          true
        ), Sync.Copy.ONLY_NEW
      )

      assertThat(actions).isEmpty()
    }

    @Test
    fun copyFileIfContentHasChanged() {
      val actions = SameLayoutSync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now, 123L),
          Node.File("d", now, 123L),
          true
        ), Sync.Copy.IF_CHANGED
      )

      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s", destPath / "d", 123L, true),
          Action.SetLastModified(destPath / "d", now)
        )
    }

  }

  @Nested
  inner class DirectoryChanges {
    @Test
    fun restoreLastModifiedAsLastEntry() {
      val actions = SameLayoutSync.copyDirectory(
        srcPath, destPath, Diff.Entry.DirectoryChanged(
          Node.Directory("s", now, emptyList()),
          Node.Directory("d", now + 2, emptyList()),
          listOf(
            Diff.Entry.FileChanged(
              Node.File("file", now + 1, 123L),
              Node.File("file", now, 123L),
              true
            )
          )
        ),
        Sync.Copy.ONLY_NEW,
        Sync.Leftover.IGNORE
      )

      assertThat(actions)
        .hasSize(3)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s" / "file", destPath / "d" / "file", 123L, true),
          Action.SetLastModified(destPath / "d" / "file", now + 1),
          Action.SetLastModified(destPath / "d", now + 2)
        )
    }

    @Test
    fun setLastModifiedAfterCopy() {
      val actions = SameLayoutSync.copyDirectory(
        srcPath, destPath, Diff.Entry.DirectoryChanged(
          Node.Directory("s", now, emptyList()),
          Node.Directory("d", now + 2, emptyList()),
          listOf(
            Diff.Entry.FileChanged(
              Node.File("file", now, 123L),
              Node.File("file", now + 1, 123L),
              true
            )
          )
        ),
        Sync.Copy.IF_CHANGED,
        Sync.Leftover.IGNORE
      )

      assertThat(actions)
        .hasSize(3)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s" / "file", destPath / "d" / "file", 123L, true),
          Action.SetLastModified(destPath / "d" / "file", now),
          Action.SetLastModified(destPath / "d", now)
        )
    }

    @Test
    fun setLastModifiedIfNewerEvenIfNoOtherChanges() {
      val actions = SameLayoutSync.copyDirectory(
        srcPath, destPath, Diff.Entry.DirectoryChanged(
          Node.Directory("s", now + 1, emptyList()),
          Node.Directory("d", now, emptyList()),
          emptyList()
        ),
        Sync.Copy.ONLY_NEW,
        Sync.Leftover.IGNORE
      )

      assertThat(actions)
        .hasSize(1)
        .containsExactlyInAnyOrder(
          Action.SetLastModified(destPath / "d", now + 1)
        )
    }

    @Test
    fun noActionsIfDestinationIsNewer() {
      val actions = SameLayoutSync.copyDirectory(
        srcPath, destPath, Diff.Entry.DirectoryChanged(
          Node.Directory("s", now, emptyList()),
          Node.Directory("d", now + 1, emptyList()),
          emptyList()
        ),
        Sync.Copy.ONLY_NEW,
        Sync.Leftover.IGNORE
      )

      assertThat(actions)
        .isEmpty()
    }
  }
}