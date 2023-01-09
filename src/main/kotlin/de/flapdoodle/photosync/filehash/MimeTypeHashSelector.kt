package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.lang.IllegalArgumentException
import java.nio.file.Path

class MimeTypeHashSelector(
  private val mimeTypeHasherMap: Map<String, Hasher<*>>,
  private val fallbackHasher: List<Pair<LongRange, Hasher<*>>>,
  private val mimeTypes: MimeTypes = MimeTypes.defaultMapping()
) : HashSelector {

  override fun hasherFor(path: Path, size: Long, lastModifiedTime: LastModified): Hasher<*> {
    val mimeType = mimeTypes.mimeTypeOf(path)
    val matchingHasher = mimeTypeHasherMap[mimeType]
    if (matchingHasher!=null) {
      return matchingHasher
    }

    val idx = mimeType.indexOf('/')
    if (idx!=-1) {
      val firstMimePart = mimeType.substring(0, idx+1)
      val matchingGenericHasher = mimeTypeHasherMap[firstMimePart]
      if (matchingGenericHasher!=null) {
        return matchingGenericHasher
      }
    }

    val fallback = fallbackHasher.filter { it.first.contains(size) }
      .map { it.second }
      .firstOrNull()

    return fallback ?: throw IllegalArgumentException("no hasher found for: $path (size=$size)")
  }

  companion object {
    fun defaultConfig(): MimeTypeHashSelector {
      return MimeTypeHashSelector(
        mimeTypeHasherMap = mapOf(
          "text/" to FullHash,
          "image/gif" to FullHash,
          "image/" to SizedQuickHash,
          "video/" to SizedQuickHash,
          "application/xml" to FullHash
        ),
        fallbackHasher = listOf(
          LongRange(0L, 4*1024L) to FullHash,
          LongRange(4*1024L, Long.MAX_VALUE) to SizedQuickHash
        ))
    }
  }
}