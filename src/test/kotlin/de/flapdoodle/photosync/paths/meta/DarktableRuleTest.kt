package de.flapdoodle.photosync.paths.meta

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class DarktableRuleTest {

    @Test
    fun isDarktableBaseFile() {
        val matcher = DarktableRule.matcher(Path.of("stuff", "foo.bar"))
        assertThat(matcher).isNotNull
        assertThat(matcher?.name).isEqualTo("foo")
        assertThat(matcher?.extension).isEqualTo("bar")
    }

    @Test
    fun notDarktableBaseFile() {
        assertThat(DarktableRule.matcher(Path.of("stuff", "foo_01.bar"))).isNull()
        assertThat(DarktableRule.matcher(Path.of("stuff", "foo.bar.blob"))).isNull()
    }

    @Test
    fun matchMetaFiles() {
        val matcher = DarktableRule.matcher(Path.of("stuff", "foo.bar"))
        assertThat(matcher).isNotNull

        val metaFiles = matcher!!.matches(listOf(
            Path.of("stuff", "foo.bar"),
            Path.of("stuff", "foo.bar.xmp"),
            Path.of("stuff", "foo_01.bar.xmp"),
            Path.of("stuff", "foo_01.bar"),
        ))

        assertThat(metaFiles).containsExactly(
            Path.of("stuff", "foo.bar.xmp"),
            Path.of("stuff", "foo_01.bar.xmp"),
            Path.of("stuff", "foo_01.bar"),
        )
    }

    @Test
    fun renameMetaFiles() {
        val matcher = DarktableRule.matcher(Path.of("stuff", "foo.bar"))
        assertThat(matcher).isNotNull

        val expectedPath = Path.of("new","stuff","blob.bar")
        val rename = matcher!!.rename(expectedPath)

        assertThat(rename.rename(Path.of("stuff", "foo.bar.xmp")))
            .isEqualTo(Path.of("new","stuff","blob.bar.xmp"))

        assertThat(rename.rename(Path.of("stuff", "foo_01.bar")))
            .isEqualTo(Path.of("new","stuff","blob_01.bar"))
    }
}