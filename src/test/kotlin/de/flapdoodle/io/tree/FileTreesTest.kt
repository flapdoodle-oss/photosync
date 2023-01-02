package de.flapdoodle.io.tree

import de.flapdoodle.io.FilesInTests
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ListAssert
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

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

    @Test
    fun sampleTreeWithFilter() {
        withSampleTree {
            val subPath = it.resolve("sub")

            val tree = FileTrees.asTree(it, filter = { current ->
                it == current || subPath == current || subPath == current.parent
            })
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
                    }
        }
    }

    @Test
    fun sampleTreeAbortMustExposeException() {
        val counter = AtomicInteger()

        withSampleTree {
            assertThatThrownBy {
                FileTrees.asTree(it, checkAbort = {
                    if (counter.incrementAndGet() > 3) throw IllegalArgumentException("abort")
                })
            }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("abort")
        }
    }

    @Test
    fun sampleTreeListenerMustReportEveryEvent() {
        val events = mutableListOf<FileTreeEvent>()
        val lastModified = LastModified.now()

        withSampleTree(lastModified) {
            FileTrees.asTree(it, listener = {
                events.add(it)
            })

            assertThat(events)
                    .hasSize(10)
                    .satisfies(Consumer { list ->
                        val subPath = it.resolve("sub")
                        val subSubPath = subPath.resolve("sub-sub")

                        val enter = FileTreeEvent.Enter(it)
                        val enterSub = FileTreeEvent.Enter(subPath)
                        val fileTxt = FileTreeEvent.File(subPath.resolve("file.txt"), 7, lastModified)
                        val otherTxt = FileTreeEvent.File(subPath.resolve("other.txt"), 0, lastModified)
                        val symLink = FileTreeEvent.SymLink(subPath.resolve("symlink"), subPath.resolve("file.txt"), lastModified)
                        val enterSubSub = FileTreeEvent.Enter(subSubPath)
                        val testTxt = FileTreeEvent.File(subSubPath.resolve("test.txt"), 4, lastModified)
                        val leaveSubSub = FileTreeEvent.Leave(subSubPath)
                        val leaveSub = FileTreeEvent.Leave(subPath)
                        val leave = FileTreeEvent.Leave(it)

                        assertThat(list)
                                .containsExactlyInAnyOrder(enter, enterSub, fileTxt, otherTxt, symLink, enterSubSub,
                                        testTxt, leaveSubSub, leaveSub, leave)

                        assertThat(list[0]).isEqualTo(enter)
                        assertThat(list[1]).isEqualTo(enterSub)

                        assertThat(list).itemIsBetween(fileTxt,enterSub,leaveSub)
                        assertThat(list).itemIsBetween(otherTxt,enterSub,leaveSub)
                        assertThat(list).itemIsBetween(symLink,enterSub,leaveSub)
                        assertThat(list).itemIsBetween(enterSubSub,enterSub,leaveSub)
                        assertThat(list).itemIsBetween(leaveSubSub,enterSub,leaveSub)

                        assertThat(list).itemIsNotBetween(fileTxt, enterSubSub, leaveSubSub)
                        assertThat(list).itemIsNotBetween(otherTxt, enterSubSub, leaveSubSub)
                        assertThat(list).itemIsNotBetween(symLink, enterSubSub, leaveSubSub)

                        assertThat(list).itemIsBetween(testTxt,enterSubSub,leaveSubSub)

                        assertThat(list[8]).isEqualTo(leaveSub)
                        assertThat(list[9]).isEqualTo(leave)
                    })
        }
    }

    private fun <T> ListAssert<T>.itemIsBetween(it: T, start: T, stop: T):ListAssert<T> {
        satisfies(Consumer { list ->
            val idx = list.indexOf(it)
            val startIdx = list.indexOf(start)
            val stopIdx = list.indexOf(stop)
            assertThat(startIdx)
                    .`as`("%s is before %s", start,stop)
                    .isLessThan(stopIdx)
            assertThat(idx)
                    .`as`("%s is after %s",it,start)
                    .isGreaterThan(startIdx)
            assertThat(idx)
                    .`as`("%s is before %s",it,stop)
                    .isLessThan(stopIdx)
        })
        return this
    }
    private fun <T> ListAssert<T>.itemIsNotBetween(it: T, start: T, stop: T):ListAssert<T> {
      satisfies(Consumer { list ->
            val idx = list.indexOf(it)
            val startIdx = list.indexOf(start)
            val stopIdx = list.indexOf(stop)
            assertThat(startIdx)
                    .`as`("%s is before %s", start,stop)
                    .isLessThan(stopIdx)
            assertThat(idx < startIdx || idx > stopIdx)
                    .`as`("%s is not between %s and %s",it,start,stop)
                    .isTrue()
        })
        return this
    }

    private fun withSampleTree(lastModified: LastModified? = null, action: (Path) -> Unit) {
        FilesInTests.withTempDirectory("tree-walk") {
            withMkDir("sub") {
                val first = createFile("file.txt", "content".toByteArray(Charsets.UTF_8), lastModified)
                createFile("other.txt", "".toByteArray(Charsets.UTF_8), lastModified)
                createSymLink("symlink", first)
                withMkDir("sub-sub") {
                    createFile("test.txt", "test".toByteArray(Charsets.UTF_8), lastModified)
                }
            }

            action(current)
        }
    }
}