package de.flapdoodle.io.layouts.common

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.io.tree.childWithPath
import de.flapdoodle.photosync.Comparision
import de.flapdoodle.photosync.compare
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.paths.rewrite
import java.nio.file.Path

sealed class Diff {
    data class TimeStampMissmatch(val src: Tree.IsFileLike, val dst: Tree.IsFileLike) : Diff()
    data class ContentMismatch(val src: Tree.File, val dst: Tree.File) : Diff()
    data class SourceIsMissing(val expectedPath: Path, val dst: Tree) : Diff()
    data class DestinationIsMissing(val src: Tree, val expectedPath: Path) : Diff()
    data class TypeMismatch(val src: Tree, val dst: Tree) : Diff()
    data class SymLinkMissmatch(val src: Tree.SymLink, val dst: Tree.SymLink) : Diff()

    companion object {
        fun diff(
            src: Path,
            dst: Path,
            srcChildren: List<Tree>,
            dstChildren: List<Tree>,
            hashers: List<Hasher<*>>,
            dirDiff: (Tree.Directory, Tree.Directory) -> List<Diff>
        ): List<Diff> {
            return diff(Tree.Directory(src, srcChildren), Tree.Directory(dst, dstChildren), hashers, dirDiff)
        }

        fun diff(
            src: Tree.Directory,
            dst: Tree.Directory,
            hashers: List<Hasher<*>>,
            dirDiff: (Tree.Directory, Tree.Directory) -> List<Diff>
        ): List<Diff> {
            require(hashers.isNotEmpty()) { "no hasher"}
            
            var diffs = emptyList<Diff>()
            val srcChildren = src.children
            srcChildren.forEach { srcChild ->
                val expectedDestination = srcChild.path.rewrite(src.path, dst.path)
                val dstChild = dst.children.childWithPath(expectedDestination)

                if (dstChild != null) {
                    when (srcChild) {
                        is Tree.Directory -> when (dstChild) {
                            is Tree.Directory -> diffs = diffs + dirDiff(srcChild, dstChild)
                            else -> diffs = diffs + TypeMismatch(srcChild, dstChild)
                        }
                        is Tree.File -> when (dstChild) {
                            is Tree.File -> diffs = diffs + fileDiff(srcChild, dstChild, hashers)
                            else -> diffs = diffs + TypeMismatch(srcChild, dstChild)
                        }
                        is Tree.SymLink -> when (dstChild) {
                            is Tree.SymLink -> {
                                val srcSymLink = src.path.relativize(srcChild.destination)
                                val dstSymLink = dst.path.relativize(dstChild.destination)
                                if (srcSymLink != dstSymLink) {
                                    diffs = diffs + SymLinkMissmatch(srcChild, dstChild)
                                } else {
                                    if (srcChild.lastModified.compare(dstChild.lastModified) != Comparision.Equal) {
                                        diffs = diffs + TimeStampMissmatch(srcChild,dstChild)
                                    }
                                }
                            }
                            else -> diffs = diffs + TypeMismatch(srcChild, dstChild)
                        }
                    }
                } else {
                    diffs = diffs + DestinationIsMissing(srcChild, expectedDestination)
                }
            }

            dst.children.forEach { dstChild ->
                val expectedSource = dstChild.path.rewrite(dst.path, src.path)
                val srcChild = srcChildren.childWithPath(expectedSource)
                if (srcChild == null) {
                    diffs = diffs + SourceIsMissing(expectedSource, dstChild)
                }
            }
            return diffs
        }

        private fun fileDiff(srcChild: Tree.File, dstChild: Tree.File, hashers: List<Hasher<*>>): List<Diff> {
            return if (isDifferent(srcChild, dstChild, hashers))
                listOf(ContentMismatch(srcChild, dstChild))
            else
                if (srcChild.lastModified.compare(dstChild.lastModified) != Comparision.Equal)
                    listOf(TimeStampMissmatch(srcChild, dstChild))
                else
                    emptyList()
        }

        fun isDifferent(srcEntry: Tree.File, dstEntry: Tree.File, hashers: List<Hasher<*>>): Boolean {
            val grouped = HashStrategy.groupBy(hashers, listOf(srcEntry, dstEntry))
            return grouped.keys.size == 2
        }
    }
}