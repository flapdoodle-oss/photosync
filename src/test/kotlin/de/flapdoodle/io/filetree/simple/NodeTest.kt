package de.flapdoodle.io.filetree.simple

import de.flapdoodle.io.FilesInTests
import de.flapdoodle.io.filetree.FileTreeVisitorAdapter
import de.flapdoodle.photosync.LastModified
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

internal class NodeTest  {

    @Test
    fun sample(@TempDir tempDir: Path) {
        val now = LastModified.now()

        FilesInTests.withDirectory(tempDir) {
            val file = createFile("file", "content", now)
            val symlink = createSymLink("symlink", file, now)
            val dir = mkDir("sub", now)

            val collector = Node.collector()
            Files.walkFileTree(current, FileTreeVisitorAdapter(collector))
            println(collector.root())
        }
    }
}