package de.flapdoodle.io.layouts.same

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

object ExcpectSameLayout {

    sealed class DiffEntry {
        data class ContentMissmatch(val src: Tree.File, val dst: Tree.File) : DiffEntry()
        data class SourceIsMissing(val expectedPath: Path, val dst: Tree) : DiffEntry()
        data class DestinationIsMissing(val src: Tree, val expectedPath: Path) : DiffEntry()
        data class TypeMissmatch(val src: Tree, val dst: Tree) : DiffEntry()
        data class SymLinkMissmatch(val src: Tree.SymLink, val dst: Tree.SymLink) : DiffEntry()
    }

    fun diff(
            src: Tree.Directory,
            dst: Tree.Directory,
            hashers: List<Hasher<*>>
    ): List<DiffEntry> {
        val srcBase = src.path
        val dstBase = dst.path

        return dirDiff(srcBase, dstBase, src, dst, hashers)
    }

    private fun dirDiff(srcBase: Path, dstBase: Path, src: Tree.Directory?, dst: Tree.Directory?, hashers: List<Hasher<*>>): List<DiffEntry> {
        require(src != null || dst != null) { "invalid state" }

        if (src != null && dst != null) {
            val relSrc = srcBase.relativize(src.path)
            val relDst = dstBase.relativize(dst.path)

            require(relSrc == relDst) { "path mismatch: $relSrc != $relDst" }

            var diffs = emptyList<DiffEntry>()

            src.children.forEach { srcChild ->
                val childPath = srcBase.relativize(srcChild.path)
                val expectedDestination = dstBase.resolve(childPath)
                val dstChild = dst.childWithPath(expectedDestination)

                if (dstChild != null) {
                    when (srcChild) {
                        is Tree.Directory -> {
                            when (dstChild) {
                                is Tree.Directory -> diffs = diffs + dirDiff(srcBase, dstBase, srcChild, dstChild, hashers)
                                else -> diffs = diffs + DiffEntry.TypeMissmatch(srcChild, dstChild)
                            }
                        }
                        is Tree.File -> {
                            when (dstChild) {
                                is Tree.File -> diffs = diffs + fileDiff(srcChild, dstChild, hashers)
                                else -> diffs = diffs + DiffEntry.TypeMissmatch(srcChild, dstChild)
                            }
                        }
                        is Tree.SymLink -> {
                            when (dstChild) {
                                is Tree.SymLink -> {
                                    val srcSymLink = srcBase.relativize(srcChild.destination)
                                    val dstSymLink = dstBase.relativize(dstChild.destination)
                                    if (srcSymLink != dstSymLink) {
                                        diffs = diffs + DiffEntry.SymLinkMissmatch(srcChild, dstChild)
                                    }
                                }
                                else -> diffs = diffs + DiffEntry.TypeMissmatch(srcChild, dstChild)
                            }
                        }
                    }
                } else {
                    diffs = diffs + DiffEntry.DestinationIsMissing(srcChild, expectedDestination)
                }
            }

            dst.children.forEach { dstChild ->
                val childPath = dstBase.relativize(dstChild.path)
                val expectedSource = srcBase.resolve(childPath)
                val srcChild = src.childWithPath(expectedSource)
                if (srcChild == null) {
                    diffs = diffs + DiffEntry.SourceIsMissing(expectedSource, dstChild)
                }
            }
            return diffs
        } else {
            if (src != null) {
                return listOf(DiffEntry.DestinationIsMissing(src, dstBase.resolve(srcBase.relativize(src.path))))
            } else if (dst != null) {
                return listOf(DiffEntry.SourceIsMissing(srcBase.resolve(dstBase.relativize(dst.path)), dst))
            }

            return listOf()
        }
    }

    private fun fileDiff(srcChild: Tree.File, dstChild: Tree.File, hashers: List<Hasher<*>>): List<DiffEntry> {
        return if (isDifferent(srcChild, dstChild, hashers))
            listOf(DiffEntry.ContentMissmatch(srcChild, dstChild))
        else
            emptyList()
    }

    private fun isDifferent(srcEntry: Tree.File, dstEntry: Tree.File, hashers: List<Hasher<*>>): Boolean {
        val grouped = HashStrategy.groupBy(hashers, listOf(srcEntry, dstEntry))
        return grouped.keys.size == 2
    }
}