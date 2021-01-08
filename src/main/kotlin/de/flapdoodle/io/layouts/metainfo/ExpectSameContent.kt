package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

object ExpectSameContent {
    sealed class MetaDiff {
        
    }

    fun diff(
        src: MetaView.Directory,
        dst: MetaView.Directory,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        return diff(src.path, dst.path, src, dst, hashers)
    }

    fun diff(
        srcBase: Path,
        dstBase: Path,
        src: MetaView.Directory,
        dst: MetaView.Directory,
        hashers: List<Hasher<*>>
    ): List<MetaDiff> {
        val srcBaseFiles = src.flatMap {
            listOf(baseFile(it.base) to it)
        }.toMap()

        val dstBaseFiles = dst.flatMap {
            listOf(baseFile(it.base) to it)
        }.toMap()

        val srcFiles = srcBaseFiles.keys
        val dstFiles = dstBaseFiles.keys

        val groupedByHash = HashStrategy.groupBy(hashers, srcFiles + dstFiles)

        groupedByHash.forEach { hash, list: List<Tree.File> ->
            println("hash: $hash")
            list.forEach {
                println("--> ${it.path}")
            }
        }
        //HashStrategy.groupBy()

        val srcBase = src.path
        val dstBase = dst.path

        // hash main files
        // group by hast
        // calc diff
        return emptyList()
    }

    private fun baseFile(base: Tree): Tree.File {
        return when (base) {
            is Tree.File -> base
            else -> throw IllegalArgumentException("not supported: $base")
        }
    }
}