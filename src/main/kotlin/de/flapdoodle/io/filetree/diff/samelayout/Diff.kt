package de.flapdoodle.io.filetree.diff.samelayout

import de.flapdoodle.io.filetree.Node
import de.flapdoodle.photosync.filehash.Hash
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

        data class HashChanged(
            val src: Node.File,
            val dest: Node.File,
            val srcHash: Hash<Any>,
            val destHash: Hash<Any>
        ) : Entry()

        data class DirectoryChanged(
            val src: Node.Directory,
            val dest: Node.Directory,
            val entries: List<Entry>
        ) : Entry()
    }

    companion object {
        fun diff(src: Node.Top, dest: Node.Top): Diff {
            return Diff(src.path, dest.path, diff(src.children, dest.children))
        }

        fun diff(src: List<Node>, dest: List<Node>): List<Diff.Entry> {
            val srcByName = src.associateBy(Node::name)
            val destByName = dest.associateBy(Node::name)

            require(srcByName.size == src.size) { "key collision in $src" }
            require(destByName.size == dest.size) { "key collision in $dest" }

            val both = srcByName.keys.intersect(destByName.keys)
            val onlyInSrc = srcByName.keys - destByName.keys
            val onlyInDest = destByName.keys - srcByName.keys

            val changed = both.map { diffNodes(srcByName[it]!!, destByName[it]!!) }

            val missing = src.filter { onlyInSrc.contains(it.name) }
                .map { Entry.Missing(it) }

            val removed = dest.filter { onlyInDest.contains(it.name) }
                .map { Entry.Removed(it) }

            return missing + removed + changed
        }

        private fun diffNodes(src: Node, dest: Node): Entry {
            require(src.name == dest.name) { "different names: $src - $dest" }

            if (src::class != dest::class) {
                return Entry.TypeMismatch(src, dest)
            }
            return when (src) {
                is Node.File -> diff(src, dest as Node.File)
                is Node.SymLink -> diff(src, dest as Node.SymLink)
                is Node.Directory -> diff(src, dest as Node.Directory)
            }
        }

        private fun diff(src: Node.File, dest: Node.File): Entry {
            // TODO check hash
            return if (src == dest)
                Entry.IsEqual(src)
            else
                Entry.Changed(src, dest)
        }

        private fun diff(src: Node.SymLink, dest: Node.SymLink): Entry {
            return if (src == dest)
                Entry.IsEqual(src)
            else
                Entry.Changed(src, dest)
        }

        private fun diff(src: Node.Directory, dest: Node.Directory): Entry {
            return if (src == dest)
                Entry.IsEqual(src)
            else
                Entry.DirectoryChanged(src, dest, diff(src.children, dest.children))
        }
    }
}