package de.flapdoodle.photosync.filehash

import org.apache.tika.Tika
import java.nio.file.Path

class MimeTypes(
  private val knownExtensions: Map<String, String>
) {
  private val tika = Tika()

  fun mimeTypeOf(path: Path): String {
    val fileName = path.fileName.toString()
    val idx = fileName.lastIndexOf('.')
    val extension = if (idx != -1) fileName.substring(idx + 1).lowercase()
    else ""

    return knownExtensions[extension] ?: tika.detect(path)
  }

  companion object {
    fun defaultMapping() = MimeTypes(knownExtensions = mapOf(
      "cr2" to "image/x-canon-cr2",
      "xmp" to "application/xml",
      "txt" to "text/plain"
    ))
  }
}