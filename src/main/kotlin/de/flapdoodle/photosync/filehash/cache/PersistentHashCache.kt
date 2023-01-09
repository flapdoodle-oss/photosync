package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.file.FileAttributeCache
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.HashCache
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.filehash.Hashing
import de.flapdoodle.photosync.io.FileIO
import java.nio.file.Files
import java.nio.file.Path

class PersistentHashCache(
  private val cache: FileAttributeCache,
  private val minPersistableSize: Long = 1024L,
  private val persistHashAdapterLookup: PersistHashAdapterLookup = PersistHashAdapterLookup.defaultAdapter()
) : HashCache {

  override fun <T : Hash<T>> hash(path: Path, size: Long, lastModifiedTime: LastModified, hasher: Hasher<T>): T {
    if (size > minPersistableSize) {
      val adapter = persistHashAdapterLookup.adapterFor(hasher)
      if (adapter != null) {
        return hash(adapter, path, size, lastModifiedTime, hasher)
      }
    }
    return hasher.hash(path, size, lastModifiedTime)
  }

  fun <T : Hash<T>> hash(adapter: PersistHashAdapter<T>, path: Path, size: Long, lastModifiedTime: LastModified, hasher: Hasher<T>): T {
    val key = "hash_${adapter.key()}"
    val content = cache.get(path, size, lastModifiedTime, key)
    if (content!=null) {
      val hashContent = content.toString(Charsets.UTF_8)
      val hash = adapter.fromString(hashContent)
      if (hash!=null) {
        return hash
      }
    }

    val hash = hasher.hash(path, size,lastModifiedTime)
    cache.set(path, size, lastModifiedTime, key, adapter.toString(hash).toByteArray(Charsets.UTF_8))
    return hash
  }

  companion object {
    fun pathHash(path: Path): String {
      val absPath = path.toAbsolutePath()
      val pathHash = Hashing.sha256 {
        absPath.forEach { p ->
          update(p.fileName.toString().toByteArray(Charsets.UTF_8))
        }
      }
      return pathHash
    }

    fun shortPath(path: Path): String {
      val absPath = path.toAbsolutePath()
      val longPath = absPath.toString()
      return longPath.replace('/','_')
        .replace('\\','_')
        .replace("[^\\x30-\\x7A]".toRegex(),"")
    }

    fun hashPath(path: Path, size: Long): Path {
      return Path.of("${size / 1024}")
        .resolve("${size % 1024}")
        .resolve(pathHash(path))
        .resolve(shortPath(path))
    }
  }
}