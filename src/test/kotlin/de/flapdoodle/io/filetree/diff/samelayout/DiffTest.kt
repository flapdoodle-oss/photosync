package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.MockedHasher
import de.flapdoodle.photosync.filehash.HashSelector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.div

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

            Node.SymLink("changed-sym-link", now, Node.NodeReference("same-file")),

            Node.Directory("sub", now, children = listOf(
                Node.File("file", now, 123L),
            )),
            Node.SymLink("type-changed", now, Node.NodeReference("same-file"))
        ))

        val destPath = Path.of("dest")
        val destTree = Node.Top(
            destPath, now, children = listOf(
            Node.File("same-file", now, 123L),
            Node.File("changed-size", now, 200L),
            Node.File("changed-time", now, 100L),
            Node.File("changed-hash", now + 1, 100L),

            Node.File("removed-file", now -10, 10L),

            Node.SymLink("changed-sym-link", now + 1, Node.NodeReference("same-file")),

            Node.Directory("sub", now + 1, children = listOf(
                Node.File("file", now, 123L),
            )),
            Node.Directory("type-changed", now, emptyList())
        ))

        val hasher = MockedHasher()
            .addRule(srcPath / "same-file", 123L, "same-file-hash")
            .addRule(destPath / "same-file", 123L, "same-file-hash")
            .addRule(srcPath / "changed-time", 100L, "changed-time-hash")
            .addRule(destPath / "changed-time", 100L, "changed-time-hash")
            .addRule(srcPath / "changed-hash", 100L, "hash#1")
            .addRule(destPath / "changed-hash", 100L, "hash#2")
            .addRule(srcPath / "sub" / "file", 123L, "same-file-hash")
            .addRule(destPath / "sub" / "file", 123L, "same-file-hash")

        val diff = Diff.diff(srcTree, destTree, HashSelector.always(hasher))

        assertThat(diff.src).isEqualTo(srcPath)
        assertThat(diff.dest).isEqualTo(destPath)
        assertThat(diff.entries)
            .hasSize(9)
            .contains(Diff.Entry.IsEqual(node = Node.File("same-file", now, 123L)))
            .contains(Diff.Entry.FileChanged(
                src = Node.File("changed-size", now, 100L),
                dest = Node.File("changed-size", now, 200L),
                contentChanged= false
            ))
            .contains(Diff.Entry.FileChanged(
                src = Node.File("changed-time", now + 1, 100L),
                dest = Node.File("changed-time", now, 100L),
                contentChanged= false
            ))
            .contains(Diff.Entry.FileChanged(
                src = Node.File("changed-hash", now + 1, 100L),
                dest = Node.File("changed-hash", now + 1, 100L),
                contentChanged= true
            ))
            .contains(Diff.Entry.Missing.MissingFile(src=Node.File("new-file", now + 1, 10L)))
            .contains(Diff.Entry.Leftover.LeftoverFile(dest = Node.File("removed-file", now - 10, 10L)))
            .contains(Diff.Entry.SymLinkChanged(
                src = Node.SymLink("changed-sym-link", now, Node.NodeReference("same-file")),
                dest = Node.SymLink("changed-sym-link", now + 1, Node.NodeReference("same-file"))
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
            .contains(Diff.Entry.TypeMismatch(
                Node.SymLink("type-changed", now, Node.NodeReference("same-file")),
                Node.Directory("type-changed", now, emptyList())
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
                .addRule(srcPath / "same", 1L, "same-file-hash")
                .addRule(destPath / "same", 1L, "same-file-hash")

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now, 1L),
                HashSelector.always(hasher)
            )

            assertThat(result).isEqualTo(Diff.Entry.IsEqual(Node.File("same", now, 1L)))
        }

        @Test
        fun sizeChanged() {
            val hasher = MockedHasher()

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now, 2L),
                HashSelector.always(hasher)
            )

            assertThat(result).isEqualTo(Diff.Entry.FileChanged(
                Node.File("same", now, 1L),
                Node.File("same", now, 2L),
                false
            ))
        }

        @Test
        fun timeStampChanged() {
            val hasher = MockedHasher()
                .addRule(srcPath / "same", 1L, "same-file-hash")
                .addRule(destPath / "same", 1L, "same-file-hash")

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now + 1, 1L),
                HashSelector.always(hasher)
            )

            assertThat(result).isEqualTo(Diff.Entry.FileChanged(
                Node.File("same", now, 1L),
                Node.File("same", now + 1, 1L),
                false
            ))
        }

        @Test
        fun contentChanged() {
            val hasher = MockedHasher()
                .addRule(srcPath / "same", 1L, "hash#1")
                .addRule(destPath / "same", 1L, "hash#2")

            val result = Diff.diff(
                srcPath,Node.File("same", now, 1L),
                destPath,Node.File("same", now , 1L),
                HashSelector.always(hasher)
            )

            assertThat(result).isEqualTo(Diff.Entry.FileChanged(
                Node.File("same", now, 1L),
                Node.File("same", now , 1L),
                true
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
                Node.SymLink("same", now, Path.of("somewhere"))
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
                Node.SymLink("same", now + 1, Node.NodeReference("dest"))
            ))
        }
    }

    @Nested
    inner class DirDiffs {
        private val now = LastModified.now()
        private val srcPath = Path.of("src")
        private val destPath = Path.of("dest")

        @Test
        fun isEqual() {
            val hasher = MockedHasher()
                .addRule(srcPath / "same" / "same-same", 1L, "same-file-hash")
                .addRule(destPath / "same" / "same-same", 1L, "same-file-hash")

            val result = Diff.diff(
                srcPath,Node.Directory("same", now, listOf(
                    Node.File("same-same", now, 1L)
                )),
                destPath,Node.Directory("same", now, listOf(
                    Node.File("same-same", now, 1L)
                )),
                HashSelector.always(hasher)
            )

            assertThat(result)
                .isEqualTo(
                    Diff.Entry.IsEqual(
                        Node.Directory("same", now, listOf(
                                Node.File("same-same", now, 1L)
                            ))
                    ))
        }

        @Test
        fun childChanged() {
            val hasher = MockedHasher()
                .addRule(srcPath / "same" / "same-same", 1L, "same-file-hash")
                .addRule(destPath / "same" / "same-same", 1L, "same-file-hash")

            val result = Diff.diff(
                srcPath,Node.Directory("same", now, listOf(
                    Node.File("same-same", now, 1L)
                )),
                destPath,Node.Directory("same", now, listOf(
                    Node.File("same-same", now + 1, 1L)
                )),
                HashSelector.always(hasher)
            )

            assertThat(result)
                .isEqualTo(
                    Diff.Entry.DirectoryChanged(
                        Node.Directory("same", now, listOf(
                            Node.File("same-same", now, 1L)
                        )),
                        Node.Directory("same", now, listOf(
                            Node.File("same-same", now + 1, 1L)
                        )),
                        listOf(
                            Diff.Entry.FileChanged(
                                Node.File("same-same", now, 1L),
                                Node.File("same-same", now + 1, 1L),
                                false
                            )
                        )
                    ))
        }

        @Test
        fun timeStampChanged() {
            val hasher = MockedHasher()
                .addRule(srcPath / "same" / "same-same", 1L, "same-file-hash")
                .addRule(destPath / "same" / "same-same", 1L, "same-file-hash")

            val result = Diff.diff(
                srcPath,Node.Directory("same", now, listOf(
                    Node.File("same-same", now, 1L)
                )),
                destPath,Node.Directory("same", now + 1, listOf(
                    Node.File("same-same", now, 1L)
                )),
                HashSelector.always(hasher)
            )

            assertThat(result)
                .isEqualTo(
                    Diff.Entry.DirectoryChanged(
                        Node.Directory("same", now, listOf(
                            Node.File("same-same", now, 1L)
                        )),
                        Node.Directory("same", now + 1, listOf(
                            Node.File("same-same", now, 1L)
                        )),
                        listOf(
                            Diff.Entry.IsEqual(
                                Node.File("same-same", now, 1L)
                            )
                        )
                    ))
        }
    }
}