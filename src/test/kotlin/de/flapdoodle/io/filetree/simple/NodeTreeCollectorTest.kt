package de.flapdoodle.io.filetree.simple

import de.flapdoodle.io.FilesInTests
import de.flapdoodle.io.filetree.FileTreeVisitorAdapter
import de.flapdoodle.io.filetree.FileTrees
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.Either
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

internal class NodeTreeCollectorTest {

    @Test
    fun directoryWithEachPossibleType(@TempDir tempDir: Path) {
        val now = LastModified.now()
        val lastModifiedTempDir = LastModified.from(tempDir)

        var symLinkLastModified: LastModified? = null
        var other_symLinkLastModified: LastModified? = null

        val result: Node.Directory? = FilesInTests.withDirectory(tempDir) {
            withMkDir("other", now + 2) {
                val other_file = createFile("other-file", "content", now - 1)
            }
            withMkDir("collect-this", now - 10) {
                val file = createFile("file", "content", now - 1)
                val symlink = createSymLink("symlink", file)
                val other_symlink = createSymLink("other-symlink", tempDir.resolve("other").resolve("other-file"))
                symLinkLastModified = LastModified.from(symlink)
                other_symLinkLastModified = LastModified.from(other_symlink)
                val dir = mkDir("sub", now - 3)
            }

            FileTrees.walkFileTree(current.resolve("collect-this"), ::NodeTreeCollector, NodeTreeCollector::root)
        }

        assertThat(result).isNotNull
        result?.let {
            assertThat(it.name).isEqualTo("collect-this")
            assertThat(it.lastModifiedTime).isEqualTo(now - 10)

            assertThat(it.children).hasSize(4)
            assertThat(it.children).containsExactlyInAnyOrder(
                Node.File("file", now - 1, "content".length.toLong()),
                Node.SymLink("symlink", symLinkLastModified!!, Either.right(tempDir.resolve("collect-this").resolve("file"))),
                Node.SymLink("other-symlink", other_symLinkLastModified!!, Either.right(tempDir.resolve("other").resolve("other-file"))),
                Node.Directory("sub", now -3)
            )
        }
    }

    @Test
    fun walkFileMustThrowException(@TempDir tempDir: Path) {
        FilesInTests.withDirectory(tempDir) {
            val file = createFile("file", "content", LastModified.now())

            val collector = NodeTreeCollector()
            assertThatThrownBy {
                Files.walkFileTree(file, FileTreeVisitorAdapter(collector))
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("no parent directory")
        }
    }

}