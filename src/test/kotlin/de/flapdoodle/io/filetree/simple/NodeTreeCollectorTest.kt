package de.flapdoodle.io.filetree.simple

import de.flapdoodle.io.FilesInTests
import de.flapdoodle.io.filetree.FileTreeVisitorAdapter
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.Either
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

internal class NodeTreeCollectorTest {

    @Test
    fun directoryWithEachPossibleType(@TempDir tempDir: Path) {
        val now = LastModified.now()
        val lastModifiedTempDir = LastModified.from(tempDir)

        var symLinkLastModified: LastModified? = null

        val result: Node.Directory? = FilesInTests.withDirectory(tempDir) {
            val file = createFile("file", "content", now - 1)
            val symlink = createSymLink("symlink", file)
            symLinkLastModified = LastModified.from(symlink)
            val dir = mkDir("sub", now - 3)

            val collector = NodeTreeCollector()
            Files.walkFileTree(current, FileTreeVisitorAdapter(collector))

            collector.root()
        }

        assertThat(result).isNotNull
        result?.let {
            assertThat(it.name).isEmpty()
            assertThat(it.lastModifiedTime).isEqualTo(lastModifiedTempDir)

            assertThat(it.children).hasSize(3)
            assertThat(it.children).containsExactlyInAnyOrder(
                Node.File("file", now - 1, "content".length.toLong()),
                Node.SymLink("symlink", symLinkLastModified!!, Either.right(tempDir.resolve("file"))),
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