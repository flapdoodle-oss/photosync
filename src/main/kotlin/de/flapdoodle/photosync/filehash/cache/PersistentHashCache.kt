package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.file.FileAttributeCache
import de.flapdoodle.photosync.filehash.*
import de.flapdoodle.photosync.io.FileIO
import de.flapdoodle.photosync.io.Humans
import de.flapdoodle.photosync.progress.Monitor
import de.flapdoodle.photosync.progress.Statistic
import java.nio.file.Files
import java.nio.file.Path

class PersistentHashCache(
  private val cache: FileAttributeCache,
  private val minPersistableSize: Long = 512L,
  private val persistHashAdapterLookup: PersistHashAdapterLookup = PersistHashAdapterLookup.defaultAdapter()
) : HashCache {

  override fun <T : Hash<T>> hash(path: Path, size: Long, lastModifiedTime: LastModified, hasher: Hasher<T>): T {
    if (size > minPersistableSize) {
      val adapter = persistHashAdapterLookup.adapterFor(hasher)
      if (adapter != null) {
        return hash(adapter, path, size, lastModifiedTime, hasher)
      } else {
        Statistic.increment(HASHED_MISSING_ADAPTER)
      }
    }
    return hasher.hash(path, size, lastModifiedTime)
  }

  fun <T : Hash<T>> hash(adapter: PersistHashAdapter<T>, path: Path, size: Long, lastModifiedTime: LastModified, hasher: Hasher<T>): T {
    Monitor.message("hash cache lookup for $path")
    
    Statistic.increment(HASHED)
    Statistic.set(HASHED_SIZE, size)
    
    val key = "hash_${adapter.key()}"
    val content = cache.get(path, size, lastModifiedTime, key)
    if (content!=null) {
      Statistic.increment(HASHED_READ)
      val hashContent = content.toString(Charsets.UTF_8)
      val hash = adapter.fromString(hashContent)
      if (hash!=null) {
        Statistic.increment(HASHED_HIT)
        return hash
      }
    }

    val hash = hasher.hash(path, size,lastModifiedTime)
    cache.set(path, size, lastModifiedTime, key, adapter.toString(hash).toByteArray(Charsets.UTF_8))
    Statistic.increment(HASHED_WRITE)
    return hash
  }

  companion object {
    private val HASHED = Statistic.property("PersistHashCache", Long::class.java, Long::plus) { "$it" }
    private val HASHED_READ = Statistic.property("PersistHashCache.read", Long::class.java, Long::plus) { "$it" }
    private val HASHED_HIT = Statistic.property("PersistHashCache.hit", Long::class.java, Long::plus) { "$it" }
    private val HASHED_WRITE = Statistic.property("PersistHashCache.write", Long::class.java, Long::plus) { "$it" }
    private val HASHED_SIZE = Statistic.property("PersistHashCache.size", Long::class.java, Long::plus) { Humans.humanReadableByteCount(it) }
    private val HASHED_MISSING_ADAPTER = Statistic.property("PersistHashCache.missingAdapter", Long::class.java, Long::plus) { "$it" }

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