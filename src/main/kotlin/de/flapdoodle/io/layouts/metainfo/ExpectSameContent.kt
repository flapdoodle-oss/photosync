package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.io.tree.childWithPath
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.paths.expectParent
import de.flapdoodle.photosync.paths.rewrite
import java.nio.file.Path

object ExpectSameContent {
    sealed class MetaDiff {
        data class SourceIsMissing(val expectedSource: Path, val dst: MetaView) : MetaDiff()
        data class DestinationIsMissing(val src: MetaView, val expectedDestionation: Path) : MetaDiff()
        data class ChangeMetaFiles(val src: MetaView.Node, val dst: MetaView.Node, val metaFileDiff: List<Diff>) :
            MetaDiff()

        data class TypeMissmatch(val src: MetaView, val dst: MetaView) : MetaDiff()
        data class MultipleMappings(val element: MetaView.Node, val src: List<MetaView.Node>, val dst: List<MetaView.Node>) : MetaDiff()
        data class Moved(
            val src: MetaView.Node,
            val dst: MetaView.Node,
            val expectedDestionation: Path,
            val metaFileDiff: List<Diff>
        ) : MetaDiff() {
            init {
                require(src.path.fileName == dst.path.fileName) { "different file names: ${src.path} != ${dst.path}" }
            }
        }

        data class Renamed(val src: MetaView.Node, val dst: MetaView.Node, val expectedDestionation: Path) : MetaDiff()
    }

    fun diff(
        src: MetaView.Directory,
        dst: MetaView.Directory,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        require(src.path.toAbsolutePath() != dst.path.toAbsolutePath()) { "same path: ${src.path} ? ${dst.path}" }
        val srcBaseFiles = baseFileMap(src)
        val dstBaseFiles = baseFileMap(dst)
        val groupedByHash = HashStrategy.groupBy(hashers, srcBaseFiles.keys + dstBaseFiles.keys)
        val sameHashMapping: SameHashMap<MetaView.Node> = SameHashMap.from(srcBaseFiles, dstBaseFiles, groupedByHash)
        return diff(src.path, dst.path, src, dst, sameHashMapping, hashers)
    }

    private fun baseFileMap(src: MetaView.Directory) = src.flatMap { listOf(baseFile(it.base) to it) }.toMap()

    private fun diff(
        srcBase: Path,
        dstBase: Path,
        src: MetaView.Directory,
        dst: MetaView.Directory,
        sameHashMap: SameHashMap<MetaView.Node>,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        var diffs = emptyList<MetaDiff>()

        src.children.forEach { srcChild ->
            val expectedDestination = srcChild.path.rewrite(srcBase, dstBase)

            when (srcChild) {
                is MetaView.Node -> {
                    val sameHash = sameHashMap[srcChild]
                    when (sameHash) {
                        is SameHashMap.SameHash.Direct -> {
                            val sameHashDstNode = sameHash.dst
                            if (srcChild.path.fileName == sameHashDstNode.path.fileName) {

                                val metaFileDiff = Diff.diff(
                                    srcChild.path.expectParent(),
                                    sameHashDstNode.path.expectParent(),
                                    srcChild.metaFiles,
                                    sameHashDstNode.metaFiles,
                                    hashers,
                                ) { _, _ ->
                                    throw IllegalArgumentException("should not be called")
                                }

                                val dstNode = dst.children.childWithPath(expectedDestination)
                                if (dstNode != sameHashDstNode) {
                                    diffs =
                                        diffs + MetaDiff.Moved(
                                            srcChild,
                                            sameHashDstNode,
                                            expectedDestination,
                                            metaFileDiff
                                        )
                                } else {
                                    if (!metaFileDiff.isEmpty()) {
                                        diffs =
                                            diffs + MetaDiff.ChangeMetaFiles(srcChild, sameHashDstNode, metaFileDiff)
                                    }
                                }
                            } else {
                                diffs = diffs + MetaDiff.Renamed(srcChild, sameHashDstNode, expectedDestination)
                            }
                        }
                        is SameHashMap.SameHash.OnlySource -> {
                            val dstNode = dst.children.childWithPath(expectedDestination)
                            if (dstNode==null) {
                                diffs = diffs + MetaDiff.DestinationIsMissing(sameHash.src, expectedDestination)
                            } else {
                                diffs = diffs + MetaDiff.TypeMissmatch(sameHash.src, dstNode)
                            }
                        }
                        is SameHashMap.SameHash.Multi -> {
                            diffs = diffs + MetaDiff.MultipleMappings(srcChild, sameHash.src, sameHash.dst)
                        }
                        else -> {
                            throw IllegalArgumentException("unexpected: $sameHash")
                        }
                    }
                }
                is MetaView.Directory -> {
                    val dstChild = dst.children.childWithPath(expectedDestination)
                    when (dstChild) {
                        is MetaView.Directory -> {
                            diffs = diffs + diff(srcBase, dstBase, srcChild, dstChild, sameHashMap, hashers)
                        }
                        is MetaView.Node -> {
                            diffs = diffs + MetaDiff.TypeMissmatch(srcChild, dstChild)
                        }
                        else -> {
                            diffs = diffs + MetaDiff.DestinationIsMissing(srcChild, expectedDestination)
                        }
                    }
                }
            }
        }

        dst.children.forEach { dstChild ->
            val expectedSource = dstChild.path.rewrite(dstBase, srcBase)
            val srcChild = src.children.childWithPath(expectedSource)
            if (srcChild == null) {
                when (dstChild) {
                    is MetaView.Node -> {
                        val sameHash: SameHashMap.SameHash<MetaView.Node> = sameHashMap[dstChild];
                        when (sameHash) {
                            is SameHashMap.SameHash.OnlyDestination -> {
                                diffs = diffs + MetaDiff.SourceIsMissing(expectedSource, dstChild)
                            }
                            is SameHashMap.SameHash.Multi -> {
                                diffs = diffs + MetaDiff.MultipleMappings(dstChild, sameHash.src, sameHash.dst)
                            }
                            is SameHashMap.SameHash.Direct -> {
                                // ignore, because direct mappings are already processed
                            }
                            else -> {
                                throw IllegalArgumentException("not expected: $sameHash")
                            }
                        }
                    }
                    is MetaView.Directory -> {
                        diffs = diffs + MetaDiff.SourceIsMissing(expectedSource, dstChild)
                    }
                }
            }
        }
        return diffs
    }

    private fun baseFile(base: Tree): Tree.File {
        return when (base) {
            is Tree.File -> base
            else -> throw IllegalArgumentException("not supported: $base")
        }
    }
}