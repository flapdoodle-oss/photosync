package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.filetree.diff.Action
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
      val actions = Sync.create(
        srcPath, destPath, Diff.Entry.Missing.MissingFile(
          Node.File("s", now, 123L)
        )
      )

      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s", destPath / "s", 123L),
          Action.SetLastModified(destPath / "s", now)
        )
    }

    @Test
    fun copyDirectoryIfMissing() {
      val actions = Sync.create(
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
          Action.CopyFile(srcPath / "sub" / "s", destPath / "sub" / "s", 123L),
          Action.SetLastModified(destPath / "sub" / "s", now),
          Action.SetLastModified(destPath / "sub", now + 1)
        )
    }
  }

  @Nested
  inner class FileChanges {

    @Test
    fun copyFileIfIsNewerAndContentHasChanged() {
      val actions = Sync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now + 1, 123L),
          Node.File("d", now, 123L),
          true
        ), Sync.Changes.ONLY_NEW
      )

      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s", destPath / "d", 123L),
          Action.SetLastModified(destPath / "d", now + 1)
        )
    }

    @Test
    fun setLastModifiedIfContentHasNotChangedButIsNewer() {
      val actions = Sync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now + 1, 123L),
          Node.File("d", now, 123L),
          false
        ), Sync.Changes.ONLY_NEW
      )

      assertThat(actions)
        .hasSize(1)
        .containsExactlyInAnyOrder(
          Action.SetLastModified(destPath / "d", now + 1)
        )
    }

    @Test
    fun dontCopyFileOrSetLastModifiedIfNotNewer() {
      val actions = Sync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now, 123L),
          Node.File("d", now, 123L),
          true
        ), Sync.Changes.ONLY_NEW
      )

      assertThat(actions).isEmpty()
    }

    @Test
    fun copyFileIfContentHasChanged() {
      val actions = Sync.copyFile(
        srcPath, destPath, Diff.Entry.FileChanged(
          Node.File("s", now, 123L),
          Node.File("d", now, 123L),
          true
        ), Sync.Changes.IF_CHANGED
      )

      assertThat(actions)
        .hasSize(2)
        .containsExactlyInAnyOrder(
          Action.CopyFile(srcPath / "s", destPath / "d", 123L),
          Action.SetLastModified(destPath / "d", now)
        )
    }

  }
}