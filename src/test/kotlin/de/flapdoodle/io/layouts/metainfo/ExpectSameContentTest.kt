package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.layouts.MockedHash
import de.flapdoodle.io.layouts.MockedHasher
import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filehash.SizeHash
import de.flapdoodle.photosync.progress.Monitor
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class ExpectSameContentTest {

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
                is ExpectSameContent.MetaDiff.ChangeMetaFiles -> {
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
        val src = MetaView.Directory(reference = Tree.Directory(Path.of("src")), children = emptyList())
        val dst = MetaView.Directory(reference = Tree.Directory(Path.of("dst")), children = emptyList())
        val result = ExpectSameContent.diff(src, dst, hashers = MockedHasher.failingHasher())

        assertThat(result).isEmpty()
    }

    @Test
    fun sameContentMustNotGiveAnyDiff() {
        val now = LastModified.now()

        val srcBase = Path.of("src")
        val dstBase = Path.of("dst")

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

        val src = MetaView.Directory(
            reference = srcTree, children = listOf(
                MetaView.Node(
                    base = srcTree.childByName("a"),
                    metaFiles = listOf(
                        srcTree.childByName("a.info"),
                        srcTree.childByName("a_2.info")
                    )
                )
            )
        )

        val dst = MetaView.Directory(
            reference = dstTree, children = listOf(
                MetaView.Node(
                    base = dstTree.childByName("a"),
                    metaFiles = listOf(
                        dstTree.childByName("a.info"),
                        dstTree.childByName("a_2.info")
                    )
                )
            )
        )

        val result = ExpectSameContent.diff(
            src, dst, hashers = listOf(
                MockedHasher(
                    mapOf(
                        Path.of("src/a") to MockedHash(1),
                        Path.of("src/a.info") to MockedHash(2),
                        Path.of("src/a_2.info") to MockedHash(3),
                        Path.of("dst/a")  to MockedHash(1),
                        Path.of("dst/a.info") to MockedHash(2),
                        Path.of("dst/a_2.info") to MockedHash(3),
                    ),
                )
            )
        )

        assertThat(result).isEmpty()
    }

    private fun Tree.Directory.childByName(name: String): Tree {
        return this.children.single { it.path.fileName.toString() == name }
    }
}