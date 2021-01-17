package de.flapdoodle.io.layouts.metainfo

interface UseMetaViewHelper {
    fun MetaView.Directory.nodeByName(name: String): MetaView.Node {
        return childByName(name) as MetaView.Node
    }

    fun MetaView.Directory.directoryByName(name: String): MetaView.Directory {
        return childByName(name) as MetaView.Directory
    }

    fun MetaView.Directory.childByName(name: String): MetaView {
        return this.children.single { it.path.fileName.toString() == name }
    }
}