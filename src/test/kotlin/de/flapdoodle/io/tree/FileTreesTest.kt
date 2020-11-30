package de.flapdoodle.io.tree

import de.flapdoodle.io.FilesInTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class FileTreesTest {

    @Test
    fun sampleTreeWithoutFilter() {
        withSampleTree {
            val tree = FileTrees.asTree(it)
            assertThat(tree)
                    .isInstanceOf(Tree.Directory::class.java)
                    .extracting { it.path }.isEqualTo(it)

            assertThat(tree.children)
                    .hasSize(1)
                    .element(0)
                    .isInstanceOfSatisfying(Tree.Directory::class.java) { sub ->
                        assertThat(sub.path).isEqualTo(it.resolve("sub"))
                        assertThat(sub.children)
                                .hasSize(4)
                                .anySatisfy { sub_child ->
                                    assertThat(sub_child)
                                            .isInstanceOfSatisfying(Tree.File::class.java) {
                                                assertThat(it.path).isEqualTo(sub.path.resolve("file.txt"))
                                                assertThat(it.size).isEqualTo(7)
                                            }
                                }
                                .anySatisfy {
                                    assertThat(it)
                                            .isInstanceOfSatisfying(Tree.File::class.java) {
                                                assertThat(it.path).isEqualTo(sub.path.resolve("other.txt"))
                                                assertThat(it.size).isEqualTo(0)
                                            }
                                }
                                .anySatisfy {
                                    assertThat(it)
                                            .isInstanceOfSatisfying(Tree.SymLink::class.java) {
                                                assertThat(it.path).isEqualTo(sub.path.resolve("symlink"))
                                                assertThat(it.destination).isEqualTo(sub.path.resolve("file.txt"))
                                            }
                                }
                                .anySatisfy {
                                    assertThat(it)
                                            .isInstanceOfSatisfying(Tree.Directory::class.java) {
                                                assertThat(it.path).isEqualTo(sub.path.resolve("sub-sub"))
                                                assertThat(it.children).hasSize(1)
                                                        .element(0)
                                                        .isInstanceOfSatisfying(Tree.File::class.java) {
                                                            assertThat(it.path).isEqualTo(sub.path.resolve("sub-sub").resolve("test.txt"))
                                                            assertThat(it.size).isEqualTo(4)
                                                        }
                                            }
                                }
                    }
        }
    }

    private fun withSampleTree(action: (Path) -> Unit) {
        FilesInTests.withTempDirectory("tree-walk") {
            mkDir("sub") {
                val first = createFile("file.txt", "content".toByteArray(Charsets.UTF_8))
                createFile("other.txt", "".toByteArray(Charsets.UTF_8))
                createSymLink("symlink", first)
                mkDir("sub-sub") {
                    createFile("test.txt", "test".toByteArray(Charsets.UTF_8))
                }
            }

            action(current)
        }
    }
}