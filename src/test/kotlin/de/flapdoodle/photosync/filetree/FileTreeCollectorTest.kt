package de.flapdoodle.photosync.filetree

import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class FileTreeCollectorTest : WithAssertions {
    @Test
    fun mustCallFilter() {
        val mock = CollectCalls()
        val secondMock = CollectCalls()

        var filterCalls: List<Path> = emptyList()
        val withFilter = mock.withFilter {
            path ->
            filterCalls = filterCalls + path
            true
        }
        val andThen = withFilter.andThen(secondMock)

        assertThat(andThen.down(Path.of("one"))).isTrue()
        assertThat(andThen.down(Path.of("two"))).isTrue()

        assertThat(filterCalls).containsExactly(Path.of("one"), Path.of("two"))
        assertThat(mock.calls).containsExactly(
                CollectCalls.Action.DOWN to Path.of("one"),
                CollectCalls.Action.DOWN to Path.of("two")
        )
        assertThat(secondMock.calls).containsExactly(
                CollectCalls.Action.DOWN to Path.of("one"),
                CollectCalls.Action.DOWN to Path.of("two")
        )
    }

    class CollectCalls : FileTreeCollector {
        enum class Action {
            DOWN, UP, ADD, ADD_SYMLINK
        }
        var calls: List<Pair<Action, Path>> = emptyList()

        override fun down(path: Path): Boolean {
            calls = calls + (Action.DOWN to path)
            return true
        }

        override fun up(path: Path) {
            calls = calls + (Action.UP to path)
        }

        override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
            calls = calls + (Action.ADD to path)
        }

        override fun addSymlink(path: Path) {
            calls = calls + (Action.ADD_SYMLINK to path)

        }

    }
}