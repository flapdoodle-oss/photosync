package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class FileTreeVisitorAdapter(
    private val collector: FileTreeCollector
) : FileVisitor<Path> {

    override fun preVisitDirectory(path: Path, attributes: BasicFileAttributes): FileVisitResult {
        require(attributes.isDirectory) { "$path is not a directory" }
        return if (collector.down(path, LastModified.from(attributes.lastModifiedTime())))
            FileVisitResult.CONTINUE
        else
            FileVisitResult.SKIP_SUBTREE
    }

    override fun postVisitDirectory(path: Path, exception: IOException?): FileVisitResult {
        collector.up(path)
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
        require(!attributes.isDirectory) { "$path is a directory" }
        require(!attributes.isOther) { "$path is a other?" }

        if (attributes.isSymbolicLink) {
            val destination = Files.readSymbolicLink(path);
            collector.addSymlink(path, destination, LastModified.from(attributes.lastModifiedTime()))
        } else {
            collector.add(path, attributes.size(), LastModified.from(attributes.lastModifiedTime()))
        }
        return FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(path: Path, exception: IOException?): FileVisitResult {
        if (exception != null) {
            exception.printStackTrace()
            return FileVisitResult.TERMINATE
        } else
            return FileVisitResult.CONTINUE
    }
}