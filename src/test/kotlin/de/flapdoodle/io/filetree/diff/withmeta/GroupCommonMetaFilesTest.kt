package de.flapdoodle.io.filetree.diff.withmeta

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GroupCommonMetaFilesTest {

  @Test
  fun nameAndExtensions() {
    assertThat(GroupCommonMetaFiles.nameAndExtensions("IMG_0001.CR2"))
      .isEqualTo(Triple("IMG_0001", "CR2", null))
    assertThat(GroupCommonMetaFiles.nameAndExtensions("foo"))
      .isEqualTo(Triple("foo", null, null))
    assertThat(GroupCommonMetaFiles.nameAndExtensions("foo.bar.baz"))
      .isEqualTo(Triple("foo", "bar", "baz"))
    assertThat(GroupCommonMetaFiles.nameAndExtensions("foo.bar.baz.plop"))
      .isEqualTo(Triple("foo", "bar", "baz.plop"))
  }

  @Test
  fun groupDarktableMetafiles() {
    val result = GroupCommonMetaFiles.group(MetaFileMap.of(
      "IMG_0001.CR2",
      "IMG_0002.CR2",
      "IMG_0002.CR2.xmp",
      "IMG_0002_01.CR2.xmp",
      "IMG_0003.CR2.xmp",
    ))

    assertThat(result.asMap())
      .containsOnlyKeys("IMG_0001.CR2", "IMG_0002.CR2", "IMG_0003.CR2.xmp")

    assertThat(result.asMap())
      .hasSize(3)
      .containsEntry("IMG_0001.CR2", emptySet())
      .containsEntry("IMG_0002.CR2", setOf("IMG_0002.CR2.xmp", "IMG_0002_01.CR2.xmp"))
      .containsEntry("IMG_0003.CR2.xmp", emptySet())
  }
}