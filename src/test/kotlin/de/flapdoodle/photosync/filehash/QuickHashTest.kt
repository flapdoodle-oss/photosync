package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.ByteArrays
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class QuickHashTest : AbstractHashTest() {

    @Test
    fun sameContentHashMustMatch(@TempDir tempDir: Path) {
        val content = ByteArrays.random(random.nextInt(512, 2048))
        with(QuickHash, tempDir, content, content) { a, b ->
            assertThat(a).isEqualTo(b)
        }
    }

    @Test
    fun sameContentInFirstAndSecondBlockWillHaveCollidingHash(@TempDir tempDir: Path) {
        val firstBlock = ByteArrays.random(512)
        val lastBlock = ByteArrays.random(512)
        val first = firstBlock + ByteArrays.random(512) + lastBlock
        val second = firstBlock + ByteArrays.random(512) + lastBlock
        with(QuickHash, tempDir, first, second) { a, b ->
            assertThat(a).isEqualTo(b)
        }
    }

    @Test
    fun differentContentInFirstBlockMustNotMatch(@TempDir tempDir: Path) {
        val first = ByteArrays.zeros(511) + 1
        val second = ByteArrays.zeros(511) + 2
        with(QuickHash, tempDir, first, second) { a, b ->
            assertThat(a).isNotEqualTo(b)
        }
    }

    @Test
    fun differentContentInSecondBlockMustNotMatch(@TempDir tempDir: Path) {
        val first = ByteArrays.zeros(512) + 1
        val second = ByteArrays.zeros(512) + 2
        with(QuickHash, tempDir, first, second) { a, b ->
            assertThat(a).isNotEqualTo(b)
        }
    }
}