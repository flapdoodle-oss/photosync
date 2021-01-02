package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.IsFile
import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

sealed class MetaTree(open val path: Path) {
    data class Directory(override val path: Path, val children: List<MetaTree> = emptyList()) : MetaTree(path) {
        init {
            val pathCollisions = children.groupBy { it.path }.filter { it.value.size > 1 }
            require(pathCollisions.isEmpty()) { "path collisions: $pathCollisions" }
        }

        fun childWithPath(childPath: Path): MetaTree? {
            return children.firstOrNull { it.path == childPath }
        }
    }

    data class File(
        override val path: Path,
        override val size: Long,
        override val lastModified: LastModified,
        val metaFiles: List<MetaTree> = emptyList()
    ) : MetaTree(path), IsFile

    data class SymLink(
        override val path: Path,
        val destination: Path,
        val lastModified: LastModified,
        val metaFiles: List<MetaTree> = emptyList()
    ) : MetaTree(path)

    companion object {
        fun map(tree: Tree.Directory, groupMetaFiles: GroupMetaFiles): Directory {
            return Directory(tree.path, mapChildren(tree.children, groupMetaFiles))
        }

        private fun mapChildren(children: List<Tree>, groupMetaFiles: GroupMetaFiles): List<MetaTree> {
            val (directories, filesAndSymlinks) = children.partition { it is Tree.Directory }

            val mappedDirectories = directories.map {
                require(it is Tree.Directory) { "wrong type: ${it}" }
                map(it, groupMetaFiles)
            }

            val mappedBaseFiles =  groupMetaFiles.groupMetaFiles(filesAndSymlinks, Tree::path) { base, metaFiles ->
                val mappedMetaFiles = metaFiles.map { mapEntry(it, emptyList()) }
                mapEntry(base, mappedMetaFiles)
            }
            
            return mappedDirectories + mappedBaseFiles
        }

        private fun mapEntry(entry: Tree, metaFiles: List<MetaTree>): MetaTree {
            return when (entry) {
                is Tree.File -> File(entry.path, entry.size, entry.lastModified, metaFiles)
                is Tree.SymLink -> SymLink(entry.path, entry.destination, entry.lastModified, metaFiles)
                else -> throw IllegalArgumentException("unexcected entry: $entry")
            }
        }
    }
}