package de.flapdoodle.io.tree

import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class TreeCollectorTest {
    private val testee = TreeCollector()

    @Test
    fun noRootDirectoryIfNoEvent() {
        assertThat(testee.rootDirectory()).isNull()
    }

    @Test
    fun failOnLeaveEventAsFirstEvent() {
        assertThatThrownBy { testee.onEvent(FileTreeEvent.Leave(Path.of("foo"))) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("not expected")
    }

    @Test
    fun failOnFileEventAsFirstEvent() {
        assertThatThrownBy { testee.onEvent(FileTreeEvent.File(Path.of("foo"), 123L, LastModified.now())) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("not expected")
    }

    @Test
    fun failOnSymLinkEventAsFirstEvent() {
        assertThatThrownBy { testee.onEvent(FileTreeEvent.SymLink(Path.of("foo"), Path.of("bar"), LastModified.now())) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("not expected")
    }

    @Test
    fun failIfLeaveDoesNotMatchPath() {
        testee.onEvent(FileTreeEvent.Enter(Path.of("foo")))
        assertThatThrownBy { testee.onEvent(FileTreeEvent.Leave(Path.of("bar"))) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("path mismatch: bar != foo")
    }

    @Test
    fun failIfLeaveIsMissing() {
        testee.onEvent(FileTreeEvent.Enter(Path.of("foo")))
        assertThatThrownBy { testee.rootDirectory() }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("unexpected collector")
    }

    @Test
    fun emptyRootDirOnEnterAndLeave() {
        val dir = Path.of("foo")
        testee.onEvent(FileTreeEvent.Enter(dir))
        testee.onEvent(FileTreeEvent.Leave(dir))
        assertThat(testee.rootDirectory()).isEqualTo(Tree.Directory(dir))
    }

    @Test
    fun addFileToCurrentDirectory() {
        val dir = Path.of("dir")
        val file = Path.of("file")
        val size = 12L
        val lastModified = LastModified.now()

        testee.onEvent(FileTreeEvent.Enter(dir))
        testee.onEvent(FileTreeEvent.File(file, size, lastModified))
        testee.onEvent(FileTreeEvent.Leave(dir))
        assertThat(testee.rootDirectory())
                .isEqualTo(Tree.Directory(dir, listOf(Tree.File(file, size, lastModified))))
    }

    @Test
    fun addSymlinkToCurrentDirectory() {
        val dir = Path.of("dir")
        val file = Path.of("file")
        val size = 12L
        val lastModified = LastModified.now()

        testee.onEvent(FileTreeEvent.Enter(dir))
        testee.onEvent(FileTreeEvent.SymLink(file, dir, lastModified))
        testee.onEvent(FileTreeEvent.Leave(dir))
        assertThat(testee.rootDirectory())
                .isEqualTo(Tree.Directory(dir, listOf(Tree.SymLink(file, dir, lastModified))))
    }

    @Test
    fun addFileToSubDirectory() {
        val dir = Path.of("dir")
        val sub = Path.of("sub")
        val file = Path.of("file")
        val size = 12L
        val lastModified = LastModified.now()

        testee.onEvent(FileTreeEvent.Enter(dir))
        testee.onEvent(FileTreeEvent.Enter(sub))
        testee.onEvent(FileTreeEvent.File(file, size, lastModified))
        testee.onEvent(FileTreeEvent.Leave(sub))
        testee.onEvent(FileTreeEvent.Leave(dir))
        assertThat(testee.rootDirectory())
                .isEqualTo(Tree.Directory(dir, listOf(Tree.Directory(sub, listOf(Tree.File(file, size, lastModified))))))
    }

}