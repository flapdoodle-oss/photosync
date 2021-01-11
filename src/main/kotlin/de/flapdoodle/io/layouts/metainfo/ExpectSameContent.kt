package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.layouts.common.Diff
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.io.tree.childWithPath
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.paths.expectParent
import de.flapdoodle.photosync.paths.rewrite
import java.nio.file.Path

object ExpectSameContent {
    sealed class MetaDiff {
        data class DirectoryMissing(val src: MetaView.Directory, val expectedDestionation: Path) : MetaDiff()
        data class SourceIsMissing(val expectedSource: Path, val dst: MetaView) : MetaDiff()
        data class TypeMissmatch(val src: MetaView, val dst: MetaView): MetaDiff()
        data class MultipleMappings(val src: List<MetaView.Node>, val dst: List<MetaView.Node>): MetaDiff()
        data class ChangeMetaFiles(val src: MetaView.Node, val dst: MetaView.Node, val metaFileDiff: List<Diff>): MetaDiff()
        data class Moved(val src: MetaView.Node, val dst: MetaView.Node, val metaFileDiff: List<Diff>, val expectedDestionation: Path): MetaDiff() {
            fun sameBaseName(): Boolean {
                return src.path.fileName == dst.path.fileName;
            }
        }
    }

    fun diff(
        src: MetaView.Directory,
        dst: MetaView.Directory,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        require(src.path.toAbsolutePath() != dst.path.toAbsolutePath()) { "same path: ${src.path} ? ${dst.path}" }
        val srcBaseFiles = src.flatMap {
            listOf(baseFile(it.base) to it)
        }.toMap()
        val dstBaseFiles = dst.flatMap {
            listOf(baseFile(it.base) to it)
        }.toMap()
        val srcFiles = srcBaseFiles.keys
        val dstFiles = dstBaseFiles.keys
        val groupedByHash = HashStrategy.groupBy(hashers, srcFiles + dstFiles)
        val sameHashMapping = buildMappingTable(srcBaseFiles, dstBaseFiles, groupedByHash)
//        sameHashMapping.values.forEach {
//            println("--------------")
//            it.src.forEach { println("src: ${it.base.path}") }
//            it.dst.forEach { println("dst: ${it.base.path}") }
//        }
        return diff(src.path, dst.path, src, dst, sameHashMapping, hashers)
    }

    private fun diff(
        srcBase: Path,
        dstBase: Path,
        src: MetaView.Directory,
        dst: MetaView.Directory,
        sameHashMap: SameHashMap,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        var diffs = emptyList<MetaDiff>()

        src.children.forEach { srcChild ->
            val childPath = srcBase.relativize(srcChild.path)
            val expectedDestination = dstBase.resolve(childPath)

            when (srcChild) {
                is MetaView.Node -> {
                    val sameHash = sameHashMap.get(srcChild)
                    val dstNode = dst.children.childWithPath(expectedDestination)
                    if (sameHash.isSingleSource() && sameHash.isSingleDestination()) {
                        val sameHashDstNode = sameHash.singleDestination()

                        val metaFileDiff = Diff.diff(
                            srcChild.path.expectParent(),
                            sameHashDstNode.path.expectParent(),
                            sameHash.singleSource().metaFiles,
                            sameHashDstNode.metaFiles,
                            hashers,
                        ) { _,_ ->
                            throw IllegalArgumentException("should not be called")
                        }

                        if (dstNode!=sameHashDstNode) {
                            diffs = diffs + MetaDiff.Moved(srcChild, sameHashDstNode, metaFileDiff, expectedDestination)
                        } else {
                            if (!metaFileDiff.isEmpty()) {
                                diffs = diffs + MetaDiff.ChangeMetaFiles(srcChild, sameHashDstNode, metaFileDiff)
                            }
                        }
                    } else {
                        diffs = diffs + MetaDiff.MultipleMappings(sameHash.src, sameHash.dst)
                    }
                }
                is MetaView.Directory -> {
                    val dstChild = dst.children.childWithPath(expectedDestination)
                    when (dstChild) {
                        is MetaView.Directory -> {
                            diffs = diffs + diff(srcBase,dstBase,srcChild,dstChild,sameHashMap, hashers)
                        }
                        is MetaView.Node -> {
                            diffs = diffs + MetaDiff.TypeMissmatch(srcChild, dstChild)
                        }
                        else -> {
                            diffs = diffs + MetaDiff.DirectoryMissing(srcChild, expectedDestination)
                        }
                    }
                }
            }
        }

        dst.children.forEach { dstChild ->
            val expectedSource = dstChild.path.rewrite(dstBase,srcBase)
            val srcChild = src.children.childWithPath(expectedSource)
            if (srcChild == null) {
                diffs = diffs + MetaDiff.SourceIsMissing(expectedSource, dstChild)
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

    private fun buildMappingTable(
        srcMap: Map<Tree.File, MetaView.Node>,
        dstMap: Map<Tree.File, MetaView.Node>,
        groupedByHash: Map<Hash<*>, List<Tree.File>>
    ): SameHashMap {
        val sameHash = groupedByHash.map { (hash, files) ->
            val srcNodes = files.mapNotNull(srcMap::get)
            val dstNodes = files.mapNotNull(dstMap::get)
            SameHash(srcNodes, dstNodes, hash)
        }
        return SameHashMap(sameHash.flatMap { sameHash ->
            sameHash.src.map { it to sameHash } + sameHash.dst.map { it to sameHash }
        }.toMap())
    }

    class SameHashMap(private val map: Map<MetaView.Node, SameHash>) {
        fun get(node: MetaView.Node): SameHash {
            return requireNotNull(map[node]) { "no entry found fot $node" }
        }
    }

    class SameHash(
        val src: List<MetaView.Node>,
        val dst: List<MetaView.Node>,
        val hash: Hash<*>
    ) {
        init {
            require(src.isNotEmpty() || dst.isNotEmpty()) { "src and dst is empty" }
        }

        fun isSingleSource() = src.size == 1
        fun isSingleDestination() = dst.size == 1
        fun singleDestination() = dst.single()
        fun singleSource() = src.single()
    }
}