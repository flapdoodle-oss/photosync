package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.HasPath
import de.flapdoodle.io.tree.Tree
import java.nio.file.Path

fun <T> MetaView.Directory.flatMap(mapper: (MetaView.Node) -> List<T>): List<T> {
    return this.children.flatMap { it ->
        when (it) {
            is MetaView.Node -> mapper(it)
            is MetaView.Directory -> it.flatMap(mapper)
        }
    }
}

fun <T> MetaView.Directory.fold(start: T, reducer: (T, MetaView.Node) -> T): T {
    return this.children.fold(start) { folded, it ->
        when (it) {
            is MetaView.Node -> reducer(folded, it)
            is MetaView.Directory -> it.fold(folded, reducer)
        }
    }
}

sealed class MetaView : HasPath {
    data class Directory(val reference: Tree.Directory, val children: List<MetaView> = emptyList()) : MetaView() {
        override val path: Path
            get() = reference.path
    }

    data class Node(val base: Tree, val metaFiles: List<Tree> = emptyList()) : MetaView() {
        init {
            require(base !is Tree.Directory) { "directory not expected: $base" }
            require(metaFiles.none { it is Tree.Directory }) { "directory not expected: $metaFiles" }
        }

        override val path: Path
            get() = base.path
    }

    companion object {
        fun map(tree: Tree.Directory, groupMetaFiles: GroupMetaFiles = GroupMetaFiles.default()): Directory {
            return Directory(tree, mapChildren(tree.children, groupMetaFiles))
        }

        private fun mapChildren(children: List<Tree>, groupMetaFiles: GroupMetaFiles): List<MetaView> {
            val (directories, filesAndSymlinks) = children.partition { it is Tree.Directory }

            val mappedDirectories = directories.map {
                require(it is Tree.Directory) { "wrong type: ${it}" }
                map(it, groupMetaFiles)
            }

            val mappedBaseFiles = groupMetaFiles.groupMetaFiles(filesAndSymlinks, Tree::path) { base, metaFiles ->
                mapEntry(base, metaFiles)
            }

            return mappedDirectories + mappedBaseFiles
        }

        private fun mapEntry(entry: Tree, metaFiles: List<Tree>): MetaView {
            return when (entry) {
                is Tree.File -> Node(entry, metaFiles)
                is Tree.SymLink -> Node(entry, metaFiles)
                else -> throw IllegalArgumentException("unexcected entry: $entry")
            }
        }
    }

}