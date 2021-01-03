package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path
import kotlin.contracts.contract

object ExpectSameContent {
    sealed class DiffEntry {
        data class ContentMissmatch(val src: MetaTree.File, val dst: MetaTree.File) : DiffEntry()
        data class SourceIsMissing(val expectedPath: Path, val dst: MetaTree) : DiffEntry()
        data class DestinationIsMissing(val src: MetaTree, val expectedPath: Path) : DiffEntry()
        data class TypeMissmatch(val src: MetaTree, val dst: MetaTree) : DiffEntry()
        data class SymLinkMissmatch(val src: MetaTree.SymLink, val dst: MetaTree.SymLink) : DiffEntry()
    }

    fun diff(
        src: MetaTree.Directory,
        dst: MetaTree.Directory,
        hashers: List<Hasher<*>>
    ): List<DiffEntry> {
        requireNoSymLinks(src)
        requireNoSymLinks(dst)

        val srcFiles = allFiles(src)
        val dstFiles = allFiles(dst)
        val groupedByHash = HashStrategy.groupBy(hashers, srcFiles + dstFiles)

        //HashStrategy.groupBy()

        val srcBase = src.path
        val dstBase = dst.path

        // hash main files
        // group by hast
        // calc diff
        return emptyList()
    }

    private fun allFiles(dir: MetaTree.Directory): List<MetaTree.File> {
        return dir.children.flatMap {
            when (it) {
                is MetaTree.File -> listOf(it)
                is MetaTree.Directory -> allFiles(it)
                is MetaTree.SymLink -> throw IllegalArgumentException("not supported")
            }
        }
    }

    private fun requireNoSymLinks(dir: MetaTree.Directory) {
        dir.children.forEach {
            require(it !is MetaTree.SymLink) { "symLinks not supported: $it" }
            if (it is MetaTree.Directory) {
                requireNoSymLinks(it)
            }
        }
    }

    private fun dirDiff(srcBase: Path, dstBase: Path, src: MetaTree.Directory?, dst: MetaTree.Directory?, hashers: List<Hasher<*>>): List<DiffEntry> {
        require(src != null || dst != null) { "invalid state" }

        if (src != null && dst != null) {
            val relSrc = srcBase.relativize(src.path)
            val relDst = dstBase.relativize(dst.path)

            require(relSrc == relDst) { "path mismatch: $relSrc != $relDst" }

        }

        return emptyList()
    }


}