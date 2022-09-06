package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.ByteArrays
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class FullHashTest : AbstractHashTest() {

    @Test
    fun sameContentHashMustMatch(@TempDir tempDir: Path) {
        val content = ByteArrays.random(random.nextInt(512, 2048))
        with(FullHash, tempDir, content, content) { a, b ->
            assertThat(a).isEqualTo(b)
        }
    }

    @Test
    fun differentContentInFirstBlockMustNotMatch(@TempDir tempDir: Path) {
        val size = random.nextInt(1, 1024)
        val secondSize = random.nextInt(1, 1024)
        val first = ByteArrays.zeros(size) + 1 + ByteArrays.zeros(secondSize)
        val second = ByteArrays.zeros(size) + 2 + ByteArrays.zeros(secondSize)
        with(FullHash, tempDir, first, second) { a, b ->
            assertThat(a).isNotEqualTo(b)
        }
    }
}