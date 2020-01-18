package de.flapdoodle.photosync.collector

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class FileVisitorAdapter(
    private val collector: PathCollector
) : FileVisitor<Path> {
  override fun preVisitDirectory(path: Path, attributes: BasicFileAttributes): FileVisitResult {
    require(attributes.isDirectory) { "$path is not a directory" }

    collector.addDir(path)

    return FileVisitResult.CONTINUE
  }

  override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
    if (attributes.isRegularFile) {
      collector.add(path, attributes.size(), attributes.lastModifiedTime())
    }
    return FileVisitResult.CONTINUE
  }

  override fun visitFileFailed(path: Path, exception: IOException?): FileVisitResult {
    exception?.printStackTrace()
    return FileVisitResult.TERMINATE
  }

  override fun postVisitDirectory(path: Path, exception: IOException?): FileVisitResult {
    if (exception!=null) {
      exception.printStackTrace()
      return FileVisitResult.TERMINATE
    }
    return FileVisitResult.CONTINUE
  }
}