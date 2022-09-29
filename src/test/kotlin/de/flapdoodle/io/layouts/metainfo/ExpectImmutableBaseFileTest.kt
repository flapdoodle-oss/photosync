package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.FilesInTests
import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.tree.FileTrees
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.MonitoringHasher
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filehash.SizeHash
import de.flapdoodle.photosync.progress.Monitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class ExpectImmutableBaseFileTest : UseMetaViewHelper, UseTreeHelper {

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

            ExpectImmutableBaseFile.diff(
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
                is ExpectImmutableBaseFile.MetaDiff.Missmatch -> {
                    println("-------")
                    println("Missmatch")
                    if (it.baseDiff.isNotEmpty()) {
                        println("base file changed")
                        it.baseDiff.forEach { baseDiff ->
                            println(baseDiff)
                        }
                    }
                    if (it.metaDiff.isNotEmpty()) {
                        println("meta files changed")
                        it.metaDiff.forEach { baseDiff ->
                            println(baseDiff)
                        }
                    }
                    println("-------")
                }
                else -> println(it)
            }
        }
        println()
    }

    @Test
    fun sameContentMustGiveNoDiff() {
        val now = LastModified.now()

        FilesInTests.withTempDirectory("immutable-base") {
            val src = withMkDir("src") {
                createFile("foo", "some content", now)
                createFile("foo.info", "meta", now.plus(23))
            }
            val dst = withMkDir("dst") {
                createFile("foo.info", "meta", now.plus(23))
                createFile("foo", "some content", now)
            }
            val srcMetaTree = MetaView.map(FileTrees.asTree(src.current), GroupMetaFiles.default())
            val dstMetaTree = MetaView.map(FileTrees.asTree(dst.current), GroupMetaFiles.default())
            val diff = diff(srcMetaTree, dstMetaTree)

            assertThat(diff)
                .isEmpty()
        }
    }

    @Test
    fun metaFileChangeMustGiveMissmatch() {
        val now = LastModified.now()

        FilesInTests.withTempDirectory("immutable-base") {
            val src = withMkDir("src") {
                createFile("foo", "some content", now)
                createFile("foo.info", "meta change", now.plus(23))
            }
            val dst = withMkDir("dst") {
                createFile("foo.info", "meta", now.plus(23))
                createFile("foo", "some content", now)
            }
            val srcMetaTree = MetaView.map(FileTrees.asTree(src.current), GroupMetaFiles.default())
            val dstMetaTree = MetaView.map(FileTrees.asTree(dst.current), GroupMetaFiles.default())
            
            val diff = diff(srcMetaTree, dstMetaTree)

            assertThat(diff)
                .containsExactlyInAnyOrder(
                    ExpectImmutableBaseFile.MetaDiff.Missmatch(
                        src = srcMetaTree.nodeByName("foo"),
                        dst = dstMetaTree.nodeByName("foo"),
                        baseDiff = emptyList(),
                        metaDiff = listOf(
                            Diff.ContentMismatch(
                                src = srcMetaTree.nodeByName("foo")
                                    .metaFiles.fileByName("foo.info"),
                                dst = dstMetaTree.nodeByName("foo")
                                    .metaFiles.fileByName("foo.info")
                            )
                        )
                    )
                )
        }
    }

    private fun diff(
        srcMetaTree: MetaView.Directory,
        dstMetaTree: MetaView.Directory
    ) = ExpectImmutableBaseFile.diff(
        srcMetaTree, dstMetaTree, listOf(
            SizeHash,
            QuickHash
        )
    )
}