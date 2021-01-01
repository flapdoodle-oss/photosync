package de.flapdoodle.io.layouts.metainfo

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class MatchMetaFileFactoryTest {

    @Test
    fun supportDarktableMetaFileLayout() {
        val root = Path.of("root")
        val basePath = root.resolve("IMG_4404.CR2")

        val paths = listOf(
            root.resolve("IMG_4404_01.CR2.xmp"),
            root.resolve("IMG_4404_02.CR2.xmp"),
            root.resolve("IMG_4404_03.CR2.xmp"),
            root.resolve("IMG_4404_04.CR2.xmp"),
            basePath,
            root.resolve("IMG_4404.CR2.xmp")
        )

        val metaFileFactory = MatchMetaFileFactory.default().create(paths)

        assertThat(paths)
            .allSatisfy {
                if (it == basePath) {
                    assertThat(metaFileFactory.basePath(it)).isNull()
                } else {
                    assertThat(metaFileFactory.basePath(it)).isEqualTo(basePath)
                }
            }
    }
}