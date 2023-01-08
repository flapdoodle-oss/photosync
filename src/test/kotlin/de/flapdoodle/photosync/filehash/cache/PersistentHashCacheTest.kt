package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneOffset

class PersistentHashCacheTest {

  @Test
  fun pathHash() {
    val path= Paths.get("/foo/bar")
    val instant = LocalDateTime.now().toInstant(ZoneOffset.UTC)
    val lastModified = LastModified.from(FileTime.from(instant));

    val hashKey = PersistentHashCache.pathHash(path)

    assertThat(hashKey)
      .isEqualTo("c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2")
  }

  @Test
  fun shortPath() {
    val path= Paths.get("/foo/bar/Ünicode/%$123/+~.txt")

    val shortPath = PersistentHashCache.shortPath(path)

    assertThat(shortPath)
      .isEqualTo("_foo_bar_nicode_123_txt")
  }

  @Test
  fun hashPath() {
    val path= Paths.get("/foo/bar/Ünicode/%\$123/+~.txt")

    val hashPath = PersistentHashCache.hashPath(path, 24*1024+123).toString()

    assertThat(hashPath)
      .isEqualTo("24/123/701b0c4e94c9a12ce2e99eae17caa5c2703abfe8e9174075d0e3aa6a752a1f09/_foo_bar_nicode_123_txt")
  }
}