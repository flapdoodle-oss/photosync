package de.flapdoodle.io.layouts.common

import de.flapdoodle.io.layouts.MockedHash
import de.flapdoodle.io.layouts.MockedHasher
import de.flapdoodle.io.layouts.MockedHasher.Companion.failingHasher
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class DiffTest {
    val srcBase = Path.of("some", "strange", "src")
    val dstBase = Path.of("other", "dst")
    val now = LastModified.now()

    @Test
    fun emptyDirectoryGivesNothing() {
        val src = Tree.Directory(srcBase)
        val dst = Tree.Directory(dstBase)
        val diff = Diff.diff(src, dst, failingHasher()) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff).isEmpty()
    }

    @Test
    fun srcContainsFileAndDstDont() {
        val srcFile = Tree.File(srcBase.resolve("file"), 123L, now)
        val src = Tree.Directory(srcBase, listOf(srcFile))
        val dst = Tree.Directory(dstBase)
        val diff = Diff.diff(src, dst, failingHasher()) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .containsExactly(Diff.DestinationIsMissing(src = srcFile, expectedPath = dstBase.resolve("file")))
    }

    @Test
    fun dstContainsFileAndSrcDont() {
        val src = Tree.Directory(srcBase)
        val dstFile = Tree.File(dstBase.resolve("file"), 123L, now)
        val dst = Tree.Directory(dstBase, listOf(dstFile))
        val diff = Diff.diff(src, dst, failingHasher()) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .containsExactly(Diff.SourceIsMissing(dst = dstFile, expectedPath = srcBase.resolve("file")))
    }

    @Test
    fun wrongTypes() {
        val srcLink = Tree.SymLink(srcBase.resolve("a"), srcBase.resolve("..").resolve("foo"), now)
        val srcFile = Tree.File(srcBase.resolve("b"), 123, now)
        val srcDir = Tree.Directory(srcBase.resolve("c"))
        val src = Tree.Directory(srcBase, listOf(srcLink, srcFile, srcDir))

        val dstLink = Tree.SymLink(dstBase.resolve("c"), dstBase.resolve("..").resolve("foo"), now)
        val dstFile = Tree.File(dstBase.resolve("a"), 123, now)
        val dstDir = Tree.Directory(dstBase.resolve("b"))
        val dst = Tree.Directory(dstBase, listOf(dstLink, dstFile, dstDir))

        val diff = Diff.diff(src, dst, failingHasher()) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .containsExactlyInAnyOrder(
                Diff.TypeMismatch(src = srcLink, dst = dstFile),
                Diff.TypeMismatch(src = srcFile, dst = dstDir),
                Diff.TypeMismatch(src = srcDir, dst = dstLink)
            )
    }

    @Test
    fun contentMissmatch() {
        val srcFile = Tree.File(srcBase.resolve("file"), 123L, now)
        val src = Tree.Directory(srcBase, listOf(srcFile))
        val dstFile = Tree.File(dstBase.resolve("file"), 123L, now)
        val dst = Tree.Directory(dstBase, listOf(dstFile))
        val hasher = listOf(
            MockedHasher(
                emptyMap<Path, MockedHash>()
                        + (srcFile.path to MockedHash(1))
                        + (dstFile.path to MockedHash(2))
            )
        )

        val diff = Diff.diff(src, dst, hasher) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .containsExactly(Diff.ContentMismatch(srcFile,dstFile))
    }

    @Test
    fun sameContentNoDiff() {
        val srcFile = Tree.File(srcBase.resolve("file"), 123L, now)
        val src = Tree.Directory(srcBase, listOf(srcFile))
        val dstFile = Tree.File(dstBase.resolve("file"), 123L, now)
        val dst = Tree.Directory(dstBase, listOf(dstFile))
        val hasher = listOf(
            MockedHasher(
                emptyMap<Path, MockedHash>()
                        + (srcFile.path to MockedHash(1))
                        + (dstFile.path to MockedHash(1))
            )
        )

        val diff = Diff.diff(src, dst, hasher) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .isEmpty()
    }

    @Test
    fun sameContentButDifferentTimeStamp() {
        val srcFile = Tree.File(srcBase.resolve("file"), 123L, now)
        val src = Tree.Directory(srcBase, listOf(srcFile))
        val dstFile = Tree.File(dstBase.resolve("file"), 123L, now.plus(1))
        val dst = Tree.Directory(dstBase, listOf(dstFile))
        val hasher = listOf(
            MockedHasher(
                emptyMap<Path, MockedHash>()
                        + (srcFile.path to MockedHash(1))
                        + (dstFile.path to MockedHash(1))
            )
        )

        val diff = Diff.diff(src, dst, hasher) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .containsExactly(Diff.TimeStampMissmatch(srcFile,dstFile))
    }

    @Test
    fun symlinkMissmatch() {
        val srcFile = Tree.SymLink(srcBase.resolve("file"), srcBase.resolve(Path.of("..","foo")), now)
        val src = Tree.Directory(srcBase, listOf(srcFile))
        val dstFile = Tree.SymLink(dstBase.resolve("file"), dstBase.resolve(Path.of("..", "bar")), now)
        val dst = Tree.Directory(dstBase, listOf(dstFile))
        val diff = Diff.diff(src, dst, failingHasher()) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .containsExactly(Diff.SymLinkMissmatch(srcFile,dstFile))
    }

    @Test
    fun symlinkWithSameDestinationNoDiff() {
        val srcFile = Tree.SymLink(srcBase.resolve("file"), srcBase.resolve(Path.of("..","foo")), now)
        val src = Tree.Directory(srcBase, listOf(srcFile))
        val dstFile = Tree.SymLink(dstBase.resolve("file"), dstBase.resolve(Path.of("..", "foo")), now)
        val dst = Tree.Directory(dstBase, listOf(dstFile))
        val diff = Diff.diff(src, dst, failingHasher()) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .isEmpty()
    }

    @Test
    fun symlinkWithSameDestinationButDifferentTimeStamp() {
        val srcFile = Tree.SymLink(srcBase.resolve("file"), srcBase.resolve(Path.of("..","foo")), now)
        val src = Tree.Directory(srcBase, listOf(srcFile))
        val dstFile = Tree.SymLink(dstBase.resolve("file"), dstBase.resolve(Path.of("..", "foo")), now.plus(1))
        val dst = Tree.Directory(dstBase, listOf(dstFile))
        val diff = Diff.diff(src, dst, failingHasher()) { _, _ ->
            throw IllegalArgumentException("must not be called");
        }

        assertThat(diff)
            .containsExactly(Diff.TimeStampMissmatch(srcFile,dstFile))
    }

    @Test
    fun traverseDirectory() {
        val srcSubDir = Tree.Directory(srcBase.resolve("sub"))
        val src = Tree.Directory(srcBase, listOf(srcSubDir))
        val dstSubDir = Tree.Directory(dstBase.resolve("sub"))
        val dst = Tree.Directory(dstBase, listOf(dstSubDir))
        val diff = Diff.diff(src, dst, failingHasher()) { s, d ->
            assertThat(s).isSameAs(srcSubDir)
            assertThat(d).isSameAs(dstSubDir)
            // fake entry
            listOf(Diff.TypeMismatch(s,d))
        }

        assertThat(diff)
            .containsExactly(Diff.TypeMismatch(srcSubDir,dstSubDir))
    }
}