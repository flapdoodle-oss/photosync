package de.flapdoodle.photosync.filetree

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class FileTreeVisitorAdapter(
    private val collector: FileTreeCollector
) : FileVisitor<Path> {

  override fun preVisitDirectory(path: Path, attributes: BasicFileAttributes): FileVisitResult {
    require(attributes.isDirectory) { "$path is not a directory" }
    collector.down(path)
    return FileVisitResult.CONTINUE
  }

  override fun postVisitDirectory(path: Path, exception: IOException?): FileVisitResult {
    collector.up(path)
    return FileVisitResult.CONTINUE
  }

  override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
    require(!attributes.isDirectory) { "$path is a directory" }
    require(!attributes.isOther) { "$path is a other?" }

    collector.add(path, attributes.isSymbolicLink)
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