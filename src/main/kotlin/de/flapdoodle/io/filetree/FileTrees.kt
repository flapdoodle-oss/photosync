package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.progress.Statistic
import java.nio.file.Files
import java.nio.file.Path

object FileTrees {
    private val VISIT_DIR=Statistic.property("FileTrees.Dir", Long::class.java, Long::plus) { "$it entries"}
    private val VISIT_FILE=Statistic.property("FileTrees.File", Long::class.java, Long::plus) { "$it files"}
    private val VISIT_SYMLINK=Statistic.property("FileTrees.SymLink", Long::class.java, Long::plus) { "$it symlinks"}

    fun <C : FileTreeCollector, T> walkFileTree(
        start: Path,
        collector: () -> C,
        extract: (C) -> T,
        listener: (Path) -> Unit = {},
    ): T {
        val currentCollector = collector()
        Files.walkFileTree(start, FileTreeVisitorAdapter(currentCollector.andThen(object : FileTreeCollector {
            override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
                Statistic.increment(VISIT_DIR)
                listener(path)
                return true
            }

            override fun up(path: Path) {

            }

            override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
                Statistic.increment(VISIT_FILE)
                listener(path)
            }

            override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
                Statistic.increment(VISIT_SYMLINK)
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