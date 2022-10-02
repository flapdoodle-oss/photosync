package de.flapdoodle.io.filetree

import java.nio.file.Files
import java.nio.file.Path

object FileTrees {
    fun <C : FileTreeCollector, T> walkFileTree(
        start: Path,
        collector: () -> C,
        extract: (C) -> T
    ): T {
        val currentCollector = collector()
        Files.walkFileTree(start, FileTreeVisitorAdapter(currentCollector))
        return extract(currentCollector)
    }
}