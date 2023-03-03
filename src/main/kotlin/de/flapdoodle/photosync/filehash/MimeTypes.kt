package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.progress.Statistic
import org.apache.tika.Tika
import java.nio.file.Path

class MimeTypes(
  private val knownExtensions: Map<String, String>
) {
  fun mimeTypeOf(path: Path): String {
    val extension = fileExtension(path)

    return knownExtensions[extension] ?: mimeType(path)
  }

  companion object {
    private val tika = Tika()

    private val MIMETYPES = Statistic.property("MimeTypes.Set", MimeTypeSet::class.java, MimeTypeSet::merge) { "$it" }

    private data class MimeTypeSet(val set: Map<String, Long> = mapOf()) {
      fun merge(other: MimeTypeSet): MimeTypeSet {
        return MimeTypeSet(buildMap {
          (set.keys + other.set.keys).forEach {
            put(it, (set[it] ?: 0L) + (other.set[it] ?: 0L))
          }
        })
      }
    }

    private fun fileExtension(path: Path): String {
      val fileName = path.fileName.toString()
      val idx = fileName.lastIndexOf('.')
      return if (idx != -1) fileName.substring(idx + 1).lowercase()
      else ""
    }

    private fun mimeType(path: Path): String {
      val mimeType = tika.detect(path)
      val extension = fileExtension(path)
      Statistic.set(MIMETYPES, MimeTypeSet(mapOf("$extension->$mimeType" to 1L)))
      return mimeType
    }

    fun defaultMapping() = MimeTypes(knownExtensions = mapOf(
      "cr2" to "image/x-canon-cr2",
      "jpg" to "image/jpeg",
      "jpeg" to "image/jpeg",
      "png" to "image/png",
      "tiff" to "image/tiff",
      "tif" to "image/tiff",
      "mov" to "video/quicktime",
      "mp4" to "video/mp4",
      "xmp" to "application/xml",
      "xml" to "application/xml",
      "txt" to "text/plain",
      "bib" to "application/x-bibtex-text-file"
    ))
  }
}