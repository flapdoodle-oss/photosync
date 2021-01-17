package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.Tree

interface UseTreeHelper {
    fun Tree.Directory.directoryByName(name: String): Tree.Directory {
        return childByName(name) as Tree.Directory
    }

    fun Tree.Directory.fileByName(name: String): Tree.File {
        return childByName(name) as Tree.File
    }

    fun Tree.Directory.childByName(name: String): Tree {
        return this.children.single { it.path.fileName.toString() == name }
    }

    fun List<Tree>.fileByName(name: String): Tree.File {
        return childByName(name) as Tree.File
    }

    fun List<Tree>.childByName(name: String): Tree {
        return this.single { it.path.fileName.toString() == name }
    }
}