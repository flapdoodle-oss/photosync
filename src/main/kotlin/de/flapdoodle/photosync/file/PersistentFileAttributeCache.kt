package de.flapdoodle.photosync.file

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.Hashing
import de.flapdoodle.photosync.io.FileIO
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.math.min

class PersistentFileAttributeCache(
  baseDir: Path
): FileAttributeCache {

  private val cacheDir: Path
  init {
    cacheDir = FileIO.createDirectoryIfNotExist(baseDir.resolve(".fileAttributeCache"))
  }

  override fun get(path: Path, size: Long, lastModifiedTime: LastModified, key: String): ByteArray? {
    val keyFile = keyFile(path, size, lastModifiedTime, key)

    return if (Files.exists(keyFile, LinkOption.NOFOLLOW_LINKS)) {
      Files.readAllBytes(keyFile)
    } else {
      null
    }
  }

  override fun set(path: Path, size: Long, lastModifiedTime: LastModified, key: String, value: ByteArray?) {
    val keyFile = keyFile(path, size, lastModifiedTime, key)

    if (value!=null) {
      FileIO.createDirectoriesIfNotExist(keyFile.parent)
      Files.write(keyFile, value, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    } else {
      if (Files.exists(keyFile)) {
        Files.delete(keyFile)
      }
    }
  }

  private fun keyFile(path: Path, size: Long, lastModifiedTime: LastModified, key: String): Path {
    return cacheDir
      .resolve(hashPath(path, size, lastModifiedTime))
      .resolve(validKey(key))
  }

  companion object {
    private val VALID_PATTERN="^([a-zA-Z0-9_-])+$".toPattern()
    fun validKey(key: String): String {
      require(VALID_PATTERN.matcher(key).matches()) {"invalid key: $key"}
      return key
    }

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
      val shortPath = absPath.joinToString {
        val partAsString = it.fileName.toString()
        partAsString.subSequence(0, min(2, partAsString.length))
      }
      return shortPath.replace("[^\\x30-\\x7A]".toRegex(),"")
    }

    fun hashPath(path: Path, size: Long, lastModifiedTime: LastModified): Path {
      return Path.of(shortPath(path))
        .resolve("${size/1024}")
        .resolve("${pathHash(path)}_${size}_${lastModifiedTime.epochSecond()}")
    }

  }
}