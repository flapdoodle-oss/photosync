package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import java.nio.file.Files
import java.nio.file.Path

object FileTrees {
    fun <C : FileTreeCollector, T> walkFileTree(
        start: Path,
        collector: () -> C,
        extract: (C) -> T,
        listener: (Path) -> Unit = {},
    ): T {
        val currentCollector = collector()
        Files.walkFileTree(start, FileTreeVisitorAdapter(currentCollector.andThen(object : FileTreeCollector {
            override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
                listener(path)
                return true
            }

            override fun up(path: Path) {

            }

            override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
                listener(path)
            }

            override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
                listener(path)
            }
        })))
        return extract(currentCollector)
    }

    fun walkFileTree(
        start: Path,
        listener: (Path) -> Unit = {},
    ): Node.Top? {
        return walkFileTree(start, ::NodeTreeCollector, NodeTreeCollector::root, listener)
    }
}