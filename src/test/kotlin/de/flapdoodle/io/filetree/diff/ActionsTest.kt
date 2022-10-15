package de.flapdoodle.io.filetree.diff

import de.flapdoodle.io.FilesInTests
import de.flapdoodle.io.FilesInTests.Companion.withDirectory
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.div

internal class ActionsTest {

  @Nested
  inner class Copy {

    @Test
    fun copyFile(@TempDir tempDir: Path) {
      val fileContent = "stuff"

      withDirectory(tempDir) {
        fileInSource(fileContent)

        execute(
          Action.CopyFile(
            src = tempDir / "src" / "file",
            dest = tempDir / "dst" / "file",
            size = fileContent.length.toLong(),
            replace = false
          )
        )

        assertThat(tempDir / "dst" / "file").exists()
          .isRegularFile
          .hasContent(fileContent)
      }
    }

    @Test
    fun copyFileshouldFailIfDestinationExists(@TempDir tempDir: Path) {
      val fileContent = "stuff"

      withDirectory(tempDir) {
        fileInSourceAndDest(fileContent)

        assertThatThrownBy {
          execute(
            Action.CopyFile(
              src = tempDir / "src" / "file",
              dest = tempDir / "dst" / "file",
              size = fileContent.length.toLong(),
              replace = false
            )
          )
        }.isInstanceOf(FileAlreadyExistsException::class.java)
      }
    }

    @Test
    fun replaceFile(@TempDir tempDir: Path) {
      val fileContent = "stuff"

      withDirectory(tempDir) {
        fileInSourceAndDest(fileContent)

        execute(
          Action.CopyFile(
            src = tempDir / "src" / "file",
            dest = tempDir / "dst" / "file",
            size = fileContent.length.toLong(),
            replace = true
          )
        )

        assertThat(tempDir / "dst" / "file").exists()
          .isRegularFile
          .hasContent(fileContent)
      }
    }
  }

  @Nested
  inner class MkDir {

    @Test
    fun createDirectory(@TempDir tempDir: Path) {
      withDirectory(tempDir) {
        nothingInDest()

        execute(
          Action.MakeDirectory(
            dest = tempDir / "dst" / "sub"
          )
        )

        assertThat(tempDir / "dst" / "sub").exists()
          .isDirectory
      }

    }

    @Test
    fun createDirectoryFailsIfAlreadyThere(@TempDir tempDir: Path) {
      withDirectory(tempDir) {
        subDirInDest()

        assertThatThrownBy {
          execute(
            Action.MakeDirectory(
              dest = tempDir / "dst" / "sub"
            )
          )
        }.isInstanceOf(FileAlreadyExistsException::class.java)
      }
    }
  }

  @Nested
  inner class SetLastModified {
    @Test
    fun setTimeStampOnFile(@TempDir tempDir: Path) {
      val now = LastModified.now()

      withDirectory(tempDir) {
        fileInDir("dst","file", "content", now)

        execute(
          Action.SetLastModified(
            dest = tempDir / "dst" / "file",
            lastModified = now + 1
          )
        )

        assertThat(tempDir / "dst" / "file").exists()
          .isRegularFile
          .matches { LastModified.from(it) == (now +1) }
      }

    }

    @Test
    fun setTimeStampOnDirectory(@TempDir tempDir: Path) {
      val now = LastModified.now()

      withDirectory(tempDir) {
        subDirInDest(now)

        execute(
          Action.SetLastModified(
            dest = tempDir / "dst" / "sub",
            lastModified = now + 1
          )
        )

        assertThat(tempDir / "dst" / "sub").exists()
          .isDirectory
          .matches { LastModified.from(it) == (now +1) }
      }

    }

  }

  @Nested
  inner class Remove {
    @Test
    fun removeFile(@TempDir tempDir: Path) {
      withDirectory(tempDir) {
        fileInDir("dst","file", "content")

        execute(
          Action.Remove(
            dest = tempDir / "dst" / "file"
          )
        )

        assertThat(tempDir / "dst" / "file").doesNotExist()
      }

    }

    @Test
    fun removeDirectory(@TempDir tempDir: Path) {
      withDirectory(tempDir) {
        subDirInDest()

        execute(
          Action.Remove(
            dest = tempDir / "dst" / "sub",
          )
        )

        assertThat(tempDir / "dst" / "sub").doesNotExist()
      }
    }

    @Test
    fun removeFailsIfNotExist(@TempDir tempDir: Path) {
      withDirectory(tempDir) {
        subDirInDest()

        assertThatThrownBy {
          execute(
            Action.Remove(
              dest = tempDir / "dst" / "file"
            )
          )
        }.isInstanceOf(NoSuchFileException::class.java)
      }
    }

    @Test
    fun removeFailsIfNotEmpty(@TempDir tempDir: Path) {
      withDirectory(tempDir) {
        withMkDir("dst") {
          withMkDir("sub") {
            createFile("file", "stuff")
          }
        }

        assertThatThrownBy {
          execute(
            Action.Remove(
              dest = tempDir / "dst" / "sub"
            )
          )
        }.isInstanceOf(DirectoryNotEmptyException::class.java)
      }
    }
  }


  private fun execute(action: Action) {
    Actions.execute(listOf(action))
  }

  private fun FilesInTests.Helper.nothingInDest() {
    mkDir("dst")
  }

  private fun FilesInTests.Helper.subDirInDest(lastModified: LastModified? = null) {
    withMkDir("dst") {
      mkDir("sub", lastModified)
    }
  }

  private fun FilesInTests.Helper.fileInSource(fileContent: String) {
    fileInDir("src", "file", fileContent)
    mkDir("dst")
  }

  private fun FilesInTests.Helper.fileInSourceAndDest(fileContent: String) {
    fileInDir("src", "file", fileContent)
    fileInDir("dst", "file", "old $fileContent")
  }

  private fun FilesInTests.Helper.fileInDir(
    dirName: String,
    fileName: String,
    content: String,
    lastModified: LastModified? = null
  ) {
    withMkDir(dirName) {
      createFile(fileName, content, lastModified)
    }
  }
}