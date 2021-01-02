package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class MetaTreeTest {

    @Test
    fun usingDefaultMetaFileFactory() {
        val root = Path.of("root")
        val lastModified = LastModified.now()

        val sample = sample(root, lastModified);

        val result = MetaTree.map(sample, GroupMetaFiles.default())
        assertThat(result.path).matches { it === root }
        assertThat(result.children)
            .hasSize(5)
            .containsExactly(
                MetaTree.Directory(root.resolve("sub")),
                MetaTree.Directory(root.resolve("other")),
                MetaTree.File(root.resolve("file1"), 1L, lastModified.plus(2),
                    metaFiles = listOf(MetaTree.File(root.resolve("file1.bak"), 1L, lastModified.plus(4)))),
                MetaTree.File(root.resolve("file3"), 1L, lastModified.plus(5)),
                MetaTree.File(root.resolve("file2"), 1L, lastModified.plus(6),
                    metaFiles = listOf(
                        MetaTree.File(root.resolve("file2.xml"), 1L, lastModified.plus(1)),
                        MetaTree.File(root.resolve("file2.xml.copy"), 1L, lastModified.plus(3)),
                    ))
            )
    }

    private fun sample(root: Path, lastModified: LastModified): Tree.Directory {
        return Tree.Directory(
            root,
            listOf(
                Tree.Directory(root.resolve("sub")),
                Tree.File(root.resolve("file2.xml"), 1L, lastModified.plus(1)),
                Tree.File(root.resolve("file1"), 1L, lastModified.plus(2)),
                Tree.Directory(root.resolve("other")),
                Tree.File(root.resolve("file2.xml.copy"), 1L, lastModified.plus(3)),
                Tree.File(root.resolve("file1.bak"), 1L, lastModified.plus(4)),
                Tree.File(root.resolve("file3"), 1L, lastModified.plus(5)),
                Tree.File(root.resolve("file2"), 1L, lastModified.plus(6)),
            )
        )
    }
}
