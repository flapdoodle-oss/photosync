package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class GroupMetaFilesTest {
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

        val groupedPaths = GroupMetaFiles.default()
            .groupMetaFiles(paths, { it }) { base, list -> base to list}

        Assertions.assertThat(groupedPaths)
            .hasSize(1)
            .containsExactly(
                basePath to listOf(
                    root.resolve("IMG_4404_01.CR2.xmp"),
                    root.resolve("IMG_4404_02.CR2.xmp"),
                    root.resolve("IMG_4404_03.CR2.xmp"),
                    root.resolve("IMG_4404_04.CR2.xmp"),
                    root.resolve("IMG_4404.CR2.xmp")
                )
            )

    }

    @Test
    fun usingDefaultMetaFileFactory() {
        val root = Path.of("root")
        val sample = sample(root);

        val result = GroupMetaFiles.default()
            .groupMetaFiles(sample,
                pathOfElement = { it },
                map = { base: Path, list: List<Path> -> base to list }
            )

        Assertions.assertThat(result)
            .hasSize(3)
            .containsExactly(
                root.resolve("file1") to listOf(root.resolve("file1.bak")),
                root.resolve("file3") to emptyList(),
                root.resolve("file2") to listOf(root.resolve("file2.xml"), root.resolve("file2.xml.copy"))
            )
    }

    private fun sample(root: Path): List<Path> {
        return listOf(
            root.resolve("file2.xml"),
            root.resolve("file1"),
            root.resolve("file2.xml.copy"),
            root.resolve("file1.bak"),
            root.resolve("file3"),
            root.resolve("file2")
        )
    }
}