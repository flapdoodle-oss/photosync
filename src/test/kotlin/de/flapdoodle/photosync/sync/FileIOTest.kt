package de.flapdoodle.photosync.sync

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class FileIOTest {
    @Test
    fun `can set lastModifiedTime on tempfile`(@TempDir tempDir: Path) {
        FileIO.tempFile(tempDir).use {
            Assertions.assertThat(it.canSetLastModifiedTime())
                    .isTrue()
        }
    }
}