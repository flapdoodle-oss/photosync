package de.flapdoodle.io.layouts.common

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.io.tree.childWithPath
import de.flapdoodle.photosync.Comparision
import de.flapdoodle.photosync.compare
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

sealed class Diff {
    data class TimeStampMissmatch(val src: Tree.File, val dst: Tree.File) : Diff()
    data class ContentMissmatch(val src: Tree.File, val dst: Tree.File) : Diff()
    data class SourceIsMissing(val expectedPath: Path, val dst: Tree) : Diff()
    data class DestinationIsMissing(val src: Tree, val expectedPath: Path) : Diff()
    data class TypeMissmatch(val src: Tree, val dst: Tree) : Diff()
    data class SymLinkMissmatch(val src: Tree.SymLink, val dst: Tree.SymLink) : Diff()

    companion object {
        fun diff(
            srcBase: Path,
            dstBase: Path,
            srcChildren: List<Tree>,
            dstChildren: List<Tree>,
            hashers: List<Hasher<*>>,
            dirDiff: (Tree.Directory, Tree.Directory) -> List<Diff>
        ): List<Diff> {
            var diffs = emptyList<Diff>()
            srcChildren.forEach { srcChild ->
                val childPath = srcBase.relativize(srcChild.path)
                val expectedDestination = dstBase.resolve(childPath)
                val dstChild = dstChildren.childWithPath(expectedDestination)

                if (dstChild != null) {
                    when (srcChild) {
                        is Tree.Directory -> {
                            when (dstChild) {
                                is Tree.Directory -> diffs = diffs + dirDiff(srcChild, dstChild)
                                else -> diffs = diffs + TypeMissmatch(srcChild, dstChild)
                            }
                        }
                        is Tree.File -> {
                            when (dstChild) {
                                is Tree.File -> diffs = diffs + fileDiff(srcChild, dstChild, hashers)
                                else -> diffs = diffs + TypeMissmatch(srcChild, dstChild)
                            }
                        }
                        is Tree.SymLink -> {
                            when (dstChild) {
                                is Tree.SymLink -> {
                                    val srcSymLink = srcBase.relativize(srcChild.destination)
                                    val dstSymLink = dstBase.relativize(dstChild.destination)
                                    if (srcSymLink != dstSymLink) {
                                        diffs = diffs + SymLinkMissmatch(srcChild, dstChild)
                                    }
                                }
                                else -> diffs = diffs + TypeMissmatch(srcChild, dstChild)
                            }
                        }
                    }
                } else {
                    diffs = diffs + DestinationIsMissing(srcChild, expectedDestination)
                }
            }

            dstChildren.forEach { dstChild ->
                val childPath = dstBase.relativize(dstChild.path)
                val expectedSource = srcBase.resolve(childPath)
                val srcChild = srcChildren.childWithPath(expectedSource)
                if (srcChild == null) {
                    diffs = diffs + SourceIsMissing(expectedSource, dstChild)
                }
            }
            return diffs
        }

        private fun fileDiff(srcChild: Tree.File, dstChild: Tree.File, hashers: List<Hasher<*>>): List<Diff> {
            return if (isDifferent(srcChild, dstChild, hashers))
                listOf(ContentMissmatch(srcChild, dstChild))
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