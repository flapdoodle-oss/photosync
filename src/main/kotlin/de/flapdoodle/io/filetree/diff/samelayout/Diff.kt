package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

data class Diff(
    val src: Path,
    val dest: Path,
    val entries: List<Entry>
) {

    sealed class Entry {
        data class IsEqual(val node: Node) : Entry()
        data class Missing(val src: Node) : Entry()
        data class Removed(val dest: Node) : Entry()
        data class TypeMismatch(val src: Node, val dest: Node) : Entry()
        data class Changed(
            val src: Node,
            val dest: Node
        ) : Entry()

        data class HashChanged<T: Hash<T>>(
            val src: Node.File,
            val dest: Node.File,
            val srcHash: T,
            val destHash: T
        ) : Entry()

        data class DirectoryChanged(
            val src: Node.Directory,
            val dest: Node.Directory,
            val entries: List<Entry>
        ) : Entry()
    }

    companion object {
        fun <T: Hash<T>> diff(src: Node.Top, dest: Node.Top, hasher: Hasher<T>): Diff {
            return Diff(src.path, dest.path, diff(src.path, src.children, dest.path, dest.children, hasher))
        }

        fun <T: Hash<T>> diff(srcPath: Path, src: List<Node>, destPath: Path, dest: List<Node>, hasher: Hasher<T>): List<Entry> {
            val srcByName = src.associateBy(Node::name)
            val destByName = dest.associateBy(Node::name)

            require(srcByName.size == src.size) { "key collision in $src" }
            require(destByName.size == dest.size) { "key collision in $dest" }

            val both = srcByName.keys.intersect(destByName.keys)
            val onlyInSrc = srcByName.keys - destByName.keys
            val onlyInDest = destByName.keys - srcByName.keys

            val changed = both.map { diffNodes(srcPath, srcByName[it]!!, destPath, destByName[it]!!, hasher) }

            val missing = src.filter { onlyInSrc.contains(it.name) }
                .map { Entry.Missing(it) }

            val removed = dest.filter { onlyInDest.contains(it.name) }
                .map { Entry.Removed(it) }

            return missing + removed + changed
        }

        private fun <T: Hash<T>> diffNodes(srcPath: Path, src: Node, destPath: Path, dest: Node, hasher: Hasher<T>): Entry {
            require(src.name == dest.name) { "different names: $src - $dest" }

            if (src::class != dest::class) {
                return Entry.TypeMismatch(src, dest)
            }
            return when (src) {
                is Node.File -> diff(srcPath, src, destPath, dest as Node.File, hasher)
                is Node.SymLink -> diff(src, dest as Node.SymLink)
                is Node.Directory -> diff(srcPath, src, destPath, dest as Node.Directory, hasher)
            }
        }

        private fun <T: Hash<T>> diff(srcPath: Path, src: Node.File, destPath: Path, dest: Node.File, hasher: Hasher<T>): Entry {
            return if (src.size == dest.size) {
                // check for content change
                val srcHash = hasher.hash(srcPath.resolve(src.name), src.size)
                val destHash = hasher.hash(destPath.resolve(dest.name), dest.size)
                if (srcHash == destHash) {
                    // same content
                    if (src == dest) Entry.IsEqual(src) else
                        Entry.Changed(src,dest)
                } else {
                    Entry.HashChanged(src,dest,srcHash,destHash)
                }
            } else {
                Entry.Changed(src,dest)
            }
        }

        private fun diff(src: Node.SymLink, dest: Node.SymLink): Entry {
            return if (src == dest)
                Entry.IsEqual(src)
            else
                Entry.Changed(src, dest)
        }

        private fun <T: Hash<T>> diff(srcPath: Path, src: Node.Directory, destPath: Path, dest: Node.Directory, hasher: Hasher<T>): Entry {
            return if (src == dest)
                Entry.IsEqual(src)
            else
                Entry.DirectoryChanged(src, dest, diff(srcPath.resolve(src.name), src.children, destPath.resolve(dest.name), dest.children, hasher))
        }
    }
}