package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.layouts.MockedHash
import de.flapdoodle.io.layouts.MockedHasher
import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.layouts.metainfo.ExpectSameContent.MetaDiff
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filehash.SizeHash
import de.flapdoodle.photosync.progress.Monitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class ExpectSameContentTest : UseMetaViewHelper, UseTreeHelper {
    val now = LastModified.now()

    val srcBase = Path.of("src")
    val dstBase = Path.of("dst")

    @Test
    @Disabled
    fun sample() {
        val source = Path.of("/media/mosmann/fotoExtreme/foto/2020/")
        val destination = Path.of("/home/mosmann/shortcuts/stuff-foto/2020/")

        val diff = Monitor.execute {
            val srcTree = FileTrees.asTree(source, listener = {
                Monitor.message("source $it")
            })
            val dstTree = FileTrees.asTree(destination, listener = {
                Monitor.message("destination $it")
            })
            Monitor.message("DONE")

            val srcMetaTree = MetaView.map(srcTree, GroupMetaFiles.default())
            val dstMetaTree = MetaView.map(dstTree, GroupMetaFiles.default());

            ExpectSameContent.diff(
                srcMetaTree, dstMetaTree, listOf(
                    MonitoringHasher(SizeHash),
                    MonitoringHasher(QuickHash)
                )
            )
        }
        println()
        println("---------------")
        diff.forEach {
            when (it) {
                is MetaDiff.ChangeMetaFiles -> {
                    println("Changed MetaFiles: ${it.src.path} ->${it.dst.path}")
                    it.metaFileDiff.forEach { delta ->
                        println(" $delta")
                    }
                }
                else -> println(it)
            }
        }
        println()
    }


    @Test
    fun noChildrenNoDiff() {
        val src = MetaView.map(Tree.Directory(srcBase))
        val dst = MetaView.map(Tree.Directory(dstBase))
        val result = ExpectSameContent.diff(src, dst, hashers = MockedHasher.failingHasher())

        assertThat(result).isEmpty()
    }

    @Test
    fun sameHashMustNotGiveAnyDiff() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.File(srcBase.resolve("a.info"), 1, now),
                Tree.File(srcBase.resolve("a_2.info"), 1, now),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.File(dstBase.resolve("a"), 1, now),
                Tree.File(dstBase.resolve("a.info"), 1, now),
                Tree.File(dstBase.resolve("a_2.info"), 1, now),
            ),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("src/a.info") to MockedHash(2),
                    Path.of("src/a_2.info") to MockedHash(3),
                    Path.of("dst/a") to MockedHash(1),
                    Path.of("dst/a.info") to MockedHash(2),
                    Path.of("dst/a_2.info") to MockedHash(3),
                )
            )
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun sameHashInsideSubDirMustNotGiveAnyDiff() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.Directory(
                    srcBase.resolve("sub"), listOf(
                        Tree.File(srcBase.resolve("sub").resolve("a"), 1, now),
                        Tree.File(srcBase.resolve("sub").resolve("a.info"), 1, now),
                        Tree.File(srcBase.resolve("sub").resolve("a_2.info"), 1, now),
                    )
                ),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.Directory(
                    dstBase.resolve("sub"), listOf(
                        Tree.File(dstBase.resolve("sub").resolve("a"), 1, now),
                        Tree.File(dstBase.resolve("sub").resolve("a.info"), 1, now),
                        Tree.File(dstBase.resolve("sub").resolve("a_2.info"), 1, now),
                    )
                ),
            ),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/sub/a") to MockedHash(1),
                    Path.of("src/sub/a.info") to MockedHash(2),
                    Path.of("src/sub/a_2.info") to MockedHash(3),
                    Path.of("dst/sub/a") to MockedHash(1),
                    Path.of("dst/sub/a.info") to MockedHash(2),
                    Path.of("dst/sub/a_2.info") to MockedHash(3),
                )
            )
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun sameBaseHashButDifferentMetaHashesMustDiff() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.File(srcBase.resolve("a.info"), 1, now),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.File(dstBase.resolve("a"), 1, now),
                Tree.File(dstBase.resolve("a.info"), 1, now),
            ),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("src/a.info") to MockedHash(2),
                    Path.of("dst/a") to MockedHash(1),
                    Path.of("dst/a.info") to MockedHash(3),
                )
            )
        )

        assertThat(result).containsExactly(
            MetaDiff.ChangeMetaFiles(
                src = src.childByName("a") as MetaView.Node,
                dst = dst.childByName("a") as MetaView.Node,
                metaFileDiff = listOf(
                    Diff.ContentMismatch(
                        srcTree.childByName("a.info") as Tree.File,
                        dstTree.childByName("a.info") as Tree.File
                    )
                )
            )
        )
    }

    @Test
    fun sameBaseHashButDifferentPathGivesMoved() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.File(srcBase.resolve("a.info"), 1, now),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.Directory(
                    dstBase.resolve("moved"), listOf(
                        Tree.File(dstBase.resolve("moved").resolve("a"), 1, now),
                        Tree.File(dstBase.resolve("moved").resolve("a.info"), 1, now),
                    )
                ),
            ),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("src/a.info") to MockedHash(2),
                    Path.of("dst/moved/a") to MockedHash(1),
                    Path.of("dst/moved/a.info") to MockedHash(3),
                )
            )
        )

        assertThat(result).containsExactly(
            MetaDiff.Moved(
                src = src.nodeByName("a"),
                dst = dst.directoryByName("moved").nodeByName("a"),
                metaFileDiff = listOf(
                    Diff.ContentMismatch(
                        srcTree.fileByName("a.info"),
                        dstTree.directoryByName("moved").fileByName("a.info")
                    )
                ),
                expectedDestionation = dstBase.resolve("a")
            ),
            MetaDiff.SourceIsMissing(
                expectedSource = srcBase.resolve("moved"),
                dst = dst.directoryByName("moved")
            )
        )
    }

    @Test
    fun sameBaseHashButDifferentPathAndNameGivesRenamed() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.File(srcBase.resolve("a.info"), 1, now),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.Directory(
                    dstBase.resolve("moved"), listOf(
                        Tree.File(dstBase.resolve("moved").resolve("renamed"), 1, now),
                        Tree.File(dstBase.resolve("moved").resolve("renamed.info"), 1, now),
                    )
                ),
            ),
        )

        val src = MetaView.map(srcTree, GroupMetaFiles.default())
        val dst = MetaView.map(dstTree, GroupMetaFiles.default())

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("src/a.info") to MockedHash(2),
                    Path.of("dst/moved/renamed") to MockedHash(1),
                    Path.of("dst/moved/renamed.info") to MockedHash(3),
                )
            )
        )

        assertThat(result).containsExactly(
            MetaDiff.Renamed(
                src = src.nodeByName("a"),
                dst = dst.directoryByName("moved").nodeByName("renamed"),
                expectedDestionation = dstBase.resolve("a")
            ),
            MetaDiff.SourceIsMissing(
                expectedSource = srcBase.resolve("moved"),
                dst = dst.directoryByName("moved")
            )
        )
    }

    @Test
    fun sameBaseHashButDifferentNameGivesRenamed() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.File(srcBase.resolve("a.info"), 1, now),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.File(dstBase.resolve("renamed"), 1, now),
                Tree.File(dstBase.resolve("renamed.info"), 1, now),
            ),
        )

        val src = MetaView.map(srcTree, GroupMetaFiles.default())
        val dst = MetaView.map(dstTree, GroupMetaFiles.default())

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("src/a.info") to MockedHash(2),
                    Path.of("dst/renamed") to MockedHash(1),
                    Path.of("dst/renamed.info") to MockedHash(3),
                )
            )
        )

        assertThat(result).containsExactly(
            MetaDiff.Renamed(
                src = src.nodeByName("a"),
                dst = dst.nodeByName("renamed"),
                expectedDestionation = dstBase.resolve("a")
            )
        )
    }

    @Test
    fun sameBaseForSourceFilesMustGiveMulti() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.File(srcBase.resolve("b"), 1, now),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.File(dstBase.resolve("c"), 1, now),
                Tree.File(dstBase.resolve("d"), 1, now),
            ),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("src/b") to MockedHash(1),
                    Path.of("dst/c") to MockedHash(2),
                    Path.of("dst/d") to MockedHash(2),
                )
            )
        )

        assertThat(result).containsExactly(
            MetaDiff.MultipleMappings(
                element = src.nodeByName("a"),
                src = listOf(src.nodeByName("a"), src.nodeByName("b")),
                dst = emptyList()
            ),
            MetaDiff.MultipleMappings(
                element = src.nodeByName("b"),
                src = listOf(src.nodeByName("a"), src.nodeByName("b")),
                dst = emptyList()
            ),
            MetaDiff.MultipleMappings(
                element = dst.nodeByName("c"),
                src = emptyList(),
                dst = listOf(dst.nodeByName("c"), dst.nodeByName("d"))
            ),
            MetaDiff.MultipleMappings(
                element = dst.nodeByName("d"),
                src = emptyList(),
                dst = listOf(dst.nodeByName("c"), dst.nodeByName("d"))
            )
        )
    }

    @Test
    fun destinationIsMissing() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.Directory(srcBase.resolve("b")),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("src/b") to MockedHash(2),
                )
            )
        )

        assertThat(result)
            .containsExactly(
                MetaDiff.DestinationIsMissing(src.childByName("b"), dstBase.resolve("b")),
                MetaDiff.DestinationIsMissing(src.childByName("a"), dstBase.resolve("a")),
            )
    }

    @Test
    fun sourceIsMissing() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.File(dstBase.resolve("a"), 1, now),
                Tree.Directory(dstBase.resolve("b")),
            ),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("dst/a") to MockedHash(1),
                    Path.of("dst/b") to MockedHash(2),
                )
            )
        )

        assertThat(result)
            .containsExactly(
                MetaDiff.SourceIsMissing(srcBase.resolve("b"), dst.childByName("b")),
                MetaDiff.SourceIsMissing(srcBase.resolve("a"), dst.childByName("a")),
            )
    }

    @Test
    fun typeMissmatch() {
        val srcTree = Tree.Directory(
            srcBase,
            listOf(
                Tree.File(srcBase.resolve("a"), 1, now),
                Tree.Directory(srcBase.resolve("b")),
            ),
        )

        val dstTree = Tree.Directory(
            dstBase,
            listOf(
                Tree.Directory(dstBase.resolve("a")),
                Tree.File(dstBase.resolve("b"), 1, now),
            ),
        )

        val src = MetaView.map(srcTree)
        val dst = MetaView.map(dstTree)

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                hasher(
                    Path.of("src/a") to MockedHash(1),
                    Path.of("dst/b") to MockedHash(2),
                )
            )
        )

        assertThat(result)
            .containsExactly(
                MetaDiff.TypeMissmatch(src.childByName("b"), dst.childByName("b")),
                MetaDiff.TypeMissmatch(src.childByName("a"), dst.childByName("a")),
            )
    }

    private fun hasher(vararg pairs: Pair<Path, MockedHash>): MockedHasher {
        return MockedHasher(mapOf(*pairs))
    }
}