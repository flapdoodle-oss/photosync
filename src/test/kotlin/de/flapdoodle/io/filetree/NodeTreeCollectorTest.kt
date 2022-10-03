package de.flapdoodle.io.filetree

import de.flapdoodle.io.FilesInTests
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

        lateinit var symLinkLastModified: LastModified
        lateinit var other_symLinkLastModified: LastModified

        val result: Node.Top? = FilesInTests.withDirectory(tempDir) {
            withMkDir("other", now + 2) {
                val other_file = createFile("other-file", "content", now - 1)
            }
            withMkDir("collect-this", now - 10) {
                withMkDir("stuff", now + 1) {
                    val file = createFile("file", "content", now - 1)
                    val symlink = createSymLink("symlink", file)
                    symLinkLastModified = LastModified.from(symlink)
                }

                val other_symlink = createSymLink("other-symlink", tempDir.resolve("other").resolve("other-file"))
                other_symLinkLastModified = LastModified.from(other_symlink)

                val dir = mkDir("sub", now - 3)
            }

            FileTrees.walkFileTree(current.resolve("collect-this"), ::NodeTreeCollector, NodeTreeCollector::root)
        }

        assertThat(result).isNotNull
        result?.let {
            assertThat(it.path).isEqualTo(tempDir.resolve("collect-this"))
            assertThat(it.lastModifiedTime).isEqualTo(now - 10)

            assertThat(it.children).hasSize(3)
            assertThat(it.children)
                .contains(Node.Directory("sub", now -3))
                .contains(Node.SymLink("other-symlink", other_symLinkLastModified, Either.right(tempDir.resolve("other").resolve("other-file"))))
                .contains(
                    Node.Directory("stuff", now + 1, children = listOf(
                    Node.File("file", now - 1, "content".length.toLong()),
                    Node.SymLink("symlink", symLinkLastModified, Either.left(Node.NodeReference(listOf("stuff","file")))),
                )))
        }
    }

    @Test
    fun useDataClass() {
        val now = LastModified.now()
        val a= Node.Directory("a", now, children = listOf(
            Node.File("file",now + 1, 123L),
            Node.SymLink("link", now + 2, Either.left(Node.NodeReference(listOf("a","b"))))
        ))
        val b= Node.Directory("a", now, children = listOf(
            Node.File("file",now + 1, 123L),
            Node.SymLink("link", now + 2, Either.left(Node.NodeReference(listOf("a","b"))))
        ))

        assertThat(a).isEqualTo(b)
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