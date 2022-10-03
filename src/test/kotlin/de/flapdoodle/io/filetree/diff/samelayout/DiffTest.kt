package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.io.layouts.MockedHash
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.MockedHasher
import de.flapdoodle.types.Either
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class DiffTest {

    @Test
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
            .contains(Diff.Entry.Changed(
                src = Node.File("changed-size", now, 100L),
                dest = Node.File("changed-size", now, 200L)
            ))
            .contains(Diff.Entry.Changed(
                src = Node.File("changed-time", now + 1, 100L),
                dest = Node.File("changed-time", now, 100L)
            ))
            .contains(Diff.Entry.HashChanged<MockedHasher.MockHash>(
                src = Node.File("changed-hash", now + 1, 100L),
                dest = Node.File("changed-hash", now + 1, 100L),
                srcHash = MockedHasher.MockHash("hash#1"),
                destHash = MockedHasher.MockHash("hash#2")
            ))
            .contains(Diff.Entry.Missing(src=Node.File("new-file", now + 1, 10L)))
            .contains(Diff.Entry.Removed(dest = Node.File("removed-file", now - 10, 10L)))
            .contains(Diff.Entry.Changed(
                src = Node.SymLink("changed-sym-link", now, Either.left(Node.NodeReference(listOf("same-file")))),
                dest = Node.SymLink("changed-sym-link", now + 1, Either.left(Node.NodeReference(listOf("same-file")))),
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
}