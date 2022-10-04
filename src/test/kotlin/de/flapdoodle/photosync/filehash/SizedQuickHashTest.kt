package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.ByteArrays
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class SizedQuickHashTest : AbstractHashTest() {

    @Test
    fun sameContentHashMustMatch(@TempDir tempDir: Path) {
        val content = ByteArrays.random(random.nextInt(512, 2048))
        with(SizedQuickHash, tempDir, content, content) { a, b ->
            Assertions.assertThat(a).isEqualTo(b)
        }
    }

    @Test
    fun sameContentInFirstAndSecondBlockWillHaveCollidingHash(@TempDir tempDir: Path) {
        val firstBlock = ByteArrays.random(512)
        val lastBlock = ByteArrays.random(512)
        val first = firstBlock + ByteArrays.random(512) + lastBlock
        val second = firstBlock + ByteArrays.random(512) + lastBlock
        with(SizedQuickHash, tempDir, first, second) { a, b ->
            Assertions.assertThat(a).isEqualTo(b)
        }
    }

    @Test
    fun differentContentInFirstBlockMustNotMatch(@TempDir tempDir: Path) {
        val first = ByteArrays.zeros(511) + 1
        val second = ByteArrays.zeros(511) + 2
        with(SizedQuickHash, tempDir, first, second) { a, b ->
            Assertions.assertThat(a).isNotEqualTo(b)
        }
    }

    @Test
    fun differentContentInSecondBlockMustNotMatch(@TempDir tempDir: Path) {
        val first = ByteArrays.zeros(512) + 1
        val second = ByteArrays.zeros(512) + 2
        with(SizedQuickHash, tempDir, first, second) { a, b ->
            Assertions.assertThat(a).isNotEqualTo(b)
        }
    }
}