package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

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
        val srcBase = src.path
        val dstBase = dst.path

        // hash main files
        // group by hast
        // calc diff
        return emptyList()
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