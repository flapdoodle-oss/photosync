package de.flapdoodle.io.tree

import de.flapdoodle.photosync.LastModified
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class Visitor2EventAdapter(
        private val onFileTreeEvent: OnFileTreeEvent
) : FileVisitor<Path> {

    override fun preVisitDirectory(path: Path, attributes: BasicFileAttributes): FileVisitResult {
        require(attributes.isDirectory) { "$path is not a directory" }
        return when (onFileTreeEvent.onEvent(FileTreeEvent.Down(path))) {
            OnFileTreeEvent.Action.Skip -> FileVisitResult.SKIP_SUBTREE;
            OnFileTreeEvent.Action.Abort -> FileVisitResult.TERMINATE;
            else -> FileVisitResult.CONTINUE
        }
    }

    override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
        require(!attributes.isDirectory) { "$path is a directory" }
        require(!attributes.isOther) { "$path is a other?" }

        val action = if (attributes.isSymbolicLink) {
            onFileTreeEvent.onEvent(FileTreeEvent.SymLink(path, Files.readSymbolicLink(path), LastModified.from(attributes.lastModifiedTime())))
        } else {
            onFileTreeEvent.onEvent(FileTreeEvent.File(path, attributes.size(), LastModified.from(attributes.lastModifiedTime())))
        }
        return when (action) {
            OnFileTreeEvent.Action.Continue -> FileVisitResult.CONTINUE
            else -> throw IllegalArgumentException("unexpected result: $action for $path")
        }
    }

    override fun visitFileFailed(path: Path, exception: IOException?): FileVisitResult {
        return if (exception != null) {
            exception.printStackTrace()
            FileVisitResult.TERMINATE
        } else
            FileVisitResult.CONTINUE
    }

    override fun postVisitDirectory(path: Path, exception: IOException?): FileVisitResult {
        return if (exception != null) {
            exception.printStackTrace()
            FileVisitResult.TERMINATE
        } else {
            val action = onFileTreeEvent.onEvent(FileTreeEvent.Up(path))
            when (action) {
                OnFileTreeEvent.Action.Continue -> FileVisitResult.CONTINUE;
                else -> throw IllegalArgumentException("unexpected result: $action for $path")
            }
        }
    }
}