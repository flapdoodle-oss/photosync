package de.flapdoodle.io.meta

import de.flapdoodle.io.filetree.diff.graph.DarktableMetafile2Basename
import de.flapdoodle.io.filetree.diff.graph.DarktableMetafile2Basename.ParsedFileName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DarktableMetafile2BaseNameTest {

  @Test
  fun sample() {
    val result = DarktableMetafile2Basename.baseNameMap(setOf(
      "foo.bar",
      "foo.bar.xmp",
      "foo_01.bar.xmp",
      "IMG_2377.CR2.xmp",
      "IMG_2377.CR2",
      "IMG_2377_003.CR2.xmp"
    ))

    assertThat(result)
      .containsEntry("IMG_2377.CR2.xmp","IMG_2377.CR2")
      .containsEntry("IMG_2377_003.CR2.xmp","IMG_2377.CR2")
      .containsEntry("foo.bar.xmp","foo.bar")
      .containsEntry("foo_01.bar.xmp","foo.bar")
      .hasSize(4)
  }

  @Test
  fun parseFileNameBaseNames() {
    assertThat(ParsedFileName("foo",null,listOf("a")).baseNames())
      .containsExactly("foo")
    assertThat(ParsedFileName("foo",null,listOf("a","b")).baseNames())
      .containsExactly("foo.a")
    assertThat(ParsedFileName("foo",null,listOf("a","b","other")).baseNames())
      .containsExactly("foo.a.b")

    assertThat(ParsedFileName("foo","_001",listOf("a","b")).baseNames())
      .containsExactly("foo.a","foo_001.a")
    assertThat(ParsedFileName("foo","_001",listOf()).baseNames())
      .containsExactly("foo","foo_001")
  }

}