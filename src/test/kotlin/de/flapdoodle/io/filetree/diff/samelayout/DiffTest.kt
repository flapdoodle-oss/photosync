package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.layouts.MockedHash
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.MockedHasher
import de.flapdoodle.types.Either
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class DiffTest {

    @Test
    @Disabled
    fun sample() {
        val now = LastModified.now()

        val srcPath = Path.of("src")
        val srcTree = Node.Top(
            srcPath, now, children = listOf(
            Node.File("same-file", now, 123L),
            Node.File("changed-size", now, 100L),
            Node.File("changed-time", now + 1, 100L),
            Node.File("changed-hash", now + 1, 100L),

            Node.File("new-file", now + 1, 10L),

            Node.SymLink("changed-sym-link", now, Either.left(Node.NodeReference(listOf("same-file")))),

            Node.Directory("sub", now, children = listOf(
                Node.File("file", now, 123L),
            ))
        ))

        val destPath = Path.of("dest")
        val destTree = Node.Top(
            destPath, now, children = listOf(
            Node.File("same-file", now, 123L),
            Node.File("changed-size", now, 200L),
            Node.File("changed-time", now, 100L),
            Node.File("changed-hash", now + 1, 100L),

            Node.File("removed-file", now -10, 10L),

            Node.SymLink("changed-sym-link", now + 1, Either.left(Node.NodeReference(listOf("same-file")))),

            Node.Directory("sub", now + 1, children = listOf(
                Node.File("file", now, 123L),
            ))
        ))

        val hasher = MockedHasher()
            .addRule(srcPath.resolve("same-file"), 123L, "same-file-hash")
            .addRule(destPath.resolve("same-file"), 123L, "same-file-hash")
            .addRule(srcPath.resolve("changed-time"), 100L, "changed-time-hash")
            .addRule(destPath.resolve("changed-time"), 100L, "changed-time-hash")
            .addRule(srcPath.resolve("changed-hash"), 100L, "hash#1")
            .addRule(destPath.resolve("changed-hash"), 100L, "hash#2")
            .addRule(srcPath.resolve("sub").resolve("file"), 123L, "same-file-hash")
            .addRule(destPath.resolve("sub").resolve("file"), 123L, "same-file-hash")

        val diff = Diff.diff(srcTree, destTree, hasher)

        assertThat(diff.src).isEqualTo(srcPath)
        assertThat(diff.dest).isEqualTo(destPath)
        assertThat(diff.entries)
            .hasSize(8)
            .contains(Diff.Entry.IsEqual(node = Node.File("same-file", now, 123L)))
            .contains(Diff.Entry.FileChanged(
                src = Node.File("changed-size", now, 100L),
                dest = Node.File("changed-size", now, 200L),
                changes = listOf(Diff.FileChange.Size(100L, 200L))
            ))
            .contains(Diff.Entry.FileChanged(
                src = Node.File("changed-time", now + 1, 100L),
                dest = Node.File("changed-time", now, 100L),
                changes = listOf(Diff.FileChange.TimeStamp(now + 1, now))
            ))
            .contains(Diff.Entry.FileChanged(
                src = Node.File("changed-hash", now + 1, 100L),
                dest = Node.File("changed-hash", now + 1, 100L),
                changes = listOf(Diff.FileChange.Content(
                src = MockedHasher.MockHash("hash#1"),
                dest = MockedHasher.MockHash("hash#2")
                ))
            ))
            .contains(Diff.Entry.Missing(src=Node.File("new-file", now + 1, 10L)))
            .contains(Diff.Entry.Removed(dest = Node.File("removed-file", now - 10, 10L)))
            .contains(Diff.Entry.SymLinkChanged(
                src = Node.SymLink("changed-sym-link", now, Node.NodeReference("same-file")),
                dest = Node.SymLink("changed-sym-link", now + 1, Node.NodeReference("same-file")),
                changes = listOf(
                    Diff.SymLinkChange.TimeStamp(now, now +1)
                )
            ))
            .contains(Diff.Entry.DirectoryChanged(
                src = Node.Directory("sub", now, children = listOf(
                    Node.File("file", now, 123L),
                )),
                dest = Node.Directory("sub", now + 1, children = listOf(
                    Node.File("file", now, 123L),
                )),
                entries = listOf(
                    Diff.Entry.IsEqual(node = Node.File("file", now, 123L))
                )
            ))
    }

    @Nested
    inner class FileDiffs {
        private val now = LastModified.now()
        private val srcPath = Path.of("src")
        private val destPath = Path.of("dest")

        @Test
        fun isEqual() {
            val hasher = MockedHasher()
                .addRule(srcPath.resolve("same"), 1L, "same-file-hash")
                .addRule(destPath.resolve("same"), 1L, "same-file-hash")

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now, 1L),
                hasher
            )

            assertThat(result).isEqualTo(Diff.Entry.IsEqual(Node.File("same", now, 1L)))
        }

        @Test
        fun sizeChanged() {
            val hasher = MockedHasher()

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now, 2L),
                hasher
            )

            assertThat(result).isEqualTo(Diff.Entry.FileChanged(
                Node.File("same", now, 1L),
                Node.File("same", now, 2L),
                listOf(Diff.FileChange.Size(1L, 2L))
            ))
        }

        @Test
        fun timeStampChanged() {
            val hasher = MockedHasher()
                .addRule(srcPath.resolve("same"), 1L, "same-file-hash")
                .addRule(destPath.resolve("same"), 1L, "same-file-hash")

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now + 1, 1L),
                hasher
            )

            assertThat(result).isEqualTo(Diff.Entry.FileChanged(
                Node.File("same", now, 1L),
                Node.File("same", now + 1, 1L),
                listOf(Diff.FileChange.TimeStamp(now, now + 1))
            ))
        }

        @Test
        fun contentChanged() {
            val hasher = MockedHasher()
                .addRule(srcPath.resolve("same"), 1L, "hash#1")
                .addRule(destPath.resolve("same"), 1L, "hash#2")

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now , 1L),
                hasher
            )

            assertThat(result).isEqualTo(Diff.Entry.FileChanged(
                Node.File("same", now, 1L),
                Node.File("same", now , 1L),
                listOf(Diff.FileChange.Content(MockedHasher.MockHash("hash#1"), MockedHasher.MockHash("hash#2")))
            ))
        }
    }

    @Nested
    inner class SymLinkDiffs {
        private val now = LastModified.now()

        @Test
        fun isEqual() {
            val result = Diff.diff(
                Node.SymLink("same", now, Node.NodeReference("dest")),
                Node.SymLink("same", now, Node.NodeReference("dest"))
            )

            assertThat(result).isEqualTo(Diff.Entry.IsEqual(Node.SymLink("same", now, Node.NodeReference("dest"))))
        }

        @Test
        fun destinationChanged() {
            val result = Diff.diff(
                Node.SymLink("same", now, Node.NodeReference("dest")),
                Node.SymLink("same", now, Path.of("somewhere"))
            )


            assertThat(result).isEqualTo(Diff.Entry.SymLinkChanged(
                Node.SymLink("same", now, Node.NodeReference("dest")),
                Node.SymLink("same", now, Path.of("somewhere")),
                listOf(Diff.SymLinkChange.Destination(Either.left(Node.NodeReference("dest")), Either.right(Path.of("somewhere"))))
            ))
        }

        @Test
        fun timeStampChanged() {
            val result = Diff.diff(
                Node.SymLink("same", now, Node.NodeReference("dest")),
                Node.SymLink("same", now + 1, Node.NodeReference("dest"))
            )

            assertThat(result).isEqualTo(Diff.Entry.SymLinkChanged(
                Node.SymLink("same", now, Node.NodeReference("dest")),
                Node.SymLink("same", now + 1, Node.NodeReference("dest")),
                listOf(Diff.SymLinkChange.TimeStamp(now, now + 1))
            ))
        }
    }
}