package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import org.apache.tika.Tika
import java.nio.file.Path

class FastHashSelector(
  private val mimeTypeHasherMap: Map<String,Hasher<*>>,
  private val defaultHasher: Hasher<*>
) : HashSelector {
  private val tika = Tika()

  override fun hasherFor(path: Path, size: Long, lastModifiedTime: LastModified): Hasher<*> {
    val mimeType = tika.detect(path)
    var matchingHasher = mimeTypeHasherMap[mimeType]
    if (matchingHasher==null) {
      val firstMimePart = mimeType.indexOf('/')
      if (firstMimePart!=-1) {
        matchingHasher = mimeTypeHasherMap[mimeType.substring(0, firstMimePart)]
      }
    }
    return matchingHasher ?: defaultHasher
  }

  companion object {
    fun defaultMapping(): FastHashSelector {
      return FastHashSelector(
        mapOf(
          "text/plain" to FullHash,
          "image/" to SizedQuickHash,
          "text/" to FullHash,
          "application/rdf+xml" to FullHash
        ),
        SizedQuickHash
      )
    }
  }
}