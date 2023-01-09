package de.flapdoodle.photosync.file

import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZoneId
import java.time.ZonedDateTime

class PersistentFileAttributeCacheTest {

  private val samplePath= Paths.get("/foo/bar")
  private val sampleSize = 24*1024+1234L
  private val sampleTimeStamp = LastModified(ZonedDateTime.of(2021,6,13,14,35,3,123, ZoneId.systemDefault()))

  @Test
  fun persistKey(@TempDir cacheDir: Path) {
    val testee = PersistentFileAttributeCache(cacheDir)

    assertThat(testee.get(samplePath, sampleSize, sampleTimeStamp, "test"))
      .isNull()

    testee.set(samplePath, sampleSize, sampleTimeStamp, "test", "me".toByteArray(Charsets.UTF_8))

    assertThat(testee.get(samplePath, sampleSize, sampleTimeStamp, "test"))
      .containsExactly("me".toByteArray(Charsets.UTF_8).toTypedArray())

    testee.set(samplePath, sampleSize, sampleTimeStamp, "test", null)

    assertThat(testee.get(samplePath, sampleSize, sampleTimeStamp, "test"))
      .isNull()
  }

  @Test
  fun noCollision(@TempDir cacheDir: Path) {
    val testee = PersistentFileAttributeCache(cacheDir)

    testee.set(samplePath, sampleSize, sampleTimeStamp, "test", "one".toByteArray(Charsets.UTF_8))

    val entryDir = cacheDir.resolve(".fileAttributeCache").resolve(PersistentFileAttributeCache.hashPath(samplePath, sampleSize, sampleTimeStamp))

    assertThat(entryDir)
      .isDirectory()

    assertThat(Files.list(entryDir))
      .containsExactly(entryDir.resolve("test"))
  }

  @Test
  fun pathHash() {
    val hashKey = PersistentFileAttributeCache.pathHash(samplePath)

    assertThat(hashKey)
      .isEqualTo("c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2")
  }

  @Test
  fun shortPath() {
    val path= Paths.get("/foo/bar/Ünicode/%$123/+~.txt")

    val shortPath = PersistentFileAttributeCache.shortPath(path)

    assertThat(shortPath)
      .isEqualTo("foban")
  }

  @Test
  fun hashPath() {
    val path= Paths.get("/foo/bar/Ünicode/%\$123/+~.txt")

    val hashPath = PersistentFileAttributeCache.hashPath(path, sampleSize, sampleTimeStamp)
      .toString()

    assertThat(hashPath)
      .isEqualTo("foban/25/701b0c4e94c9a12ce2e99eae17caa5c2703abfe8e9174075d0e3aa6a752a1f09_25810_1623587703")
  }

  @Test
  fun validateKey() {
    assertThat(PersistentFileAttributeCache.validKey("valid_-key"))
      .isEqualTo("valid_-key")

    Assertions.assertThatThrownBy {
      PersistentFileAttributeCache.validKey("invalid\$key")
    }.isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("invalid key: invalid\$key")
  }
}