package de.flapdoodle.dirsync.ui.io

import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.SizeHasher
import de.flapdoodle.io.filetree.Tree
import de.flapdoodle.io.filetree.findChild
import de.flapdoodle.photosync.filehash.SizeHash
import java.nio.file.Path

class TreeDiff(
        val srcTree: Tree.Directory,
        val dstTree: Tree.Directory,
        val diffEntries: List<DiffEntry>
) {

    sealed class DiffEntry {
        data class ContentMissmatch(val src: Tree.File, val dst: Tree.File) : DiffEntry()
        data class OneIsMissing(val src: Tree?, val dst: Tree?): DiffEntry()
        data class TypeMissmatch(val src: Tree, val dst: Tree): DiffEntry()
    }

    companion object {
        fun diff(
                srcTree: Tree.Directory,
                dstTree: Tree.Directory,
                hashStrategy: HashStrategy
        ): TreeDiff {
            val srcBase = srcTree.path
            val dstBase = dstTree.path

            val diffEntries = diffTree(srcBase, srcTree, dstBase, dstTree, HashStrategy { listOf(SizeHash) + hashStrategy.hasher() })

            return TreeDiff(srcTree, dstTree, diffEntries)
        }

        private fun diffTree(
                srcBase: Path,
                srcTree: Tree.Directory,
                dstBase: Path,
                dstTree: Tree.Directory,
                hashStrategy: HashStrategy): List<DiffEntry> {
            val relativeSources = srcTree.children.map { srcBase.relativize(it.path) }
            val relativeDestinations = dstTree.children.map { dstBase.relativize(it.path) }
            val all = (relativeSources + relativeDestinations).toSortedSet()

            var diffEntries = listOf<DiffEntry>()

            all.forEach {
                val srcEntry = srcTree.findChild(srcBase.resolve(it))
                val dstEntry = dstTree.findChild(dstBase.resolve(it));

                if (srcEntry!=null && dstEntry!=null) {
                    if (srcEntry is Tree.File && dstEntry is Tree.File) {
                        if (isDifferent(srcEntry, dstEntry, hashStrategy)) {
                            diffEntries = diffEntries + DiffEntry.ContentMissmatch(srcEntry, dstEntry)
                        }
                    } else {
                        if (srcEntry is Tree.Directory && dstEntry is Tree.Directory) {
                            diffEntries = diffEntries + diffTree(srcBase, srcEntry, dstBase, dstEntry, hashStrategy)
                        } else {
                            diffEntries = diffEntries + DiffEntry.TypeMissmatch(srcEntry, dstEntry)
                        }
                    }
                } else {
                    diffEntries = diffEntries + DiffEntry.OneIsMissing(srcEntry, dstEntry)
                }
            }

            return diffEntries
        }

        private fun isDifferent(srcEntry: Tree.File, dstEntry: Tree.File, hashStrategy: HashStrategy): Boolean {
            for (hasher in hashStrategy.hasher()) {
                val monitoredHasher = hasher.withMonitor()
                
                val srcHash = monitoredHasher.hash(srcEntry.path, srcEntry.size)
                val dstHash = monitoredHasher.hash(dstEntry.path, dstEntry.size)
                if (srcHash != dstHash) {
                    return true
                }
            }
            return false
        }
    }
}