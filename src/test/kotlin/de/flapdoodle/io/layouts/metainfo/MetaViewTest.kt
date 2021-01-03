package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.tree.Tree
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class MetaViewTest {
    @Test
    fun usingDefaultMetaFileFactory() {
        val root = Path.of("root")
        val lastModified = LastModified.now()

        val sample = sample(root, lastModified);

        val result = MetaView.map(sample, GroupMetaFiles.default())
        Assertions.assertThat(result.reference.path).matches { it === root }
        Assertions.assertThat(result.children)
            .hasSize(5)
            .containsExactly(
                MetaView.Directory(Tree.Directory(root.resolve("sub"))),
                MetaView.Directory(Tree.Directory(root.resolve("other"))),
                MetaView.Node(Tree.File(root.resolve("file1"), 1L, lastModified.plus(2)),
                    metaFiles = listOf(Tree.File(root.resolve("file1.bak"), 1L, lastModified.plus(4)))),
                MetaView.Node(Tree.File(root.resolve("file3"), 1L, lastModified.plus(5))),
                MetaView.Node(Tree.File(root.resolve("file2"), 1L, lastModified.plus(6)),
                    metaFiles = listOf(
                        Tree.File(root.resolve("file2.xml"), 1L, lastModified.plus(1)),
                        Tree.File(root.resolve("file2.xml.copy"), 1L, lastModified.plus(3)),
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