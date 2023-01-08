package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.HashCache
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.filehash.Hashing
import de.flapdoodle.photosync.io.FileIO
import java.nio.file.Files
import java.nio.file.Path

class PersistentHashCache(
  private val cacheDir: Path,
  private val minPersistableSize: Long = 1024L,
  private val persistHashAdapterLookup: PersistHashAdapterLookup = PersistHashAdapterLookup.Companion.Default()
) : HashCache {

  init {
    FileIO.createDirectoryIfNotExist(cacheDir)
  }

  override fun <T : Hash<T>> hash(path: Path, size: Long, lastModifiedTime: LastModified, hasher: Hasher<T>): T {
    if (size > minPersistableSize) {
      val adapter = persistHashAdapterLookup.adapterFor(hasher)
      if (adapter != null) {
        val hashFile = cacheDir.resolve(hashPath(path, size))
        return hash(hashFile, adapter, path, size, lastModifiedTime, hasher)
      }
    }
    return hasher.hash(path, size, lastModifiedTime)
  }

  fun <T : Hash<T>> hash(hashFile: Path, adapter: PersistHashAdapter<T>, path: Path, size: Long, lastModifiedTime: LastModified, hasher: Hasher<T>): T {
    if (Files.exists(hashFile)) {
      val hashContent = read(hashFile)
      val parts = hashContent.split('|')
      val persistedLastModified = LastModified.fromString(parts[0])
      if (persistedLastModified == lastModifiedTime) {
        val persistedHash = adapter.fromString(parts[1])
        if (persistedHash != null) {
          return persistedHash
        }
      }
    }

    val hash = hasher.hash(path, size,lastModifiedTime)
    val hashFileContent = "${LastModified.toString(lastModifiedTime)}|$hash"
    persist(hashFile, hashFileContent)
    return hash
  }

  private fun read(hashFile: Path): String {
    val bytes = Files.readAllBytes(hashFile)
    return bytes.toString(Charsets.UTF_8)
  }

  private fun persist(hashFile: Path, content: String) {
    val parent = hashFile.parent
    FileIO.createDirectoriesIfNotExist(parent)
    Files.write(hashFile, content.toByteArray(Charsets.UTF_8))
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