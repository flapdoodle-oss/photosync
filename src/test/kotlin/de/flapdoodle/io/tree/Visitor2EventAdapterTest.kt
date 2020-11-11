package de.flapdoodle.io.tree

import de.flapdoodle.io.FilesInTests
import de.flapdoodle.photosync.LastModified
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

internal class Visitor2EventAdapterTest {
    @Test
    fun emptyDirectoryGivesDownAndUp() {
        var events = emptyList<FileTreeEvent>()
        val testee = Visitor2EventAdapter(onFileTreeEvent = { it ->
            events = events + it
            OnFileTreeEvent.Action.Continue
        })

        FilesInTests.withTempDirectory("visitor-test") { startingPoint ->
            val result = Files.walkFileTree(startingPoint, testee)

            assertThat(result).isSameAs(startingPoint);
            assertThat(events).containsExactly(FileTreeEvent.Down(startingPoint), FileTreeEvent.Up(startingPoint))
        }
    }

    @Test
    fun subDirectoryGivesDownUpForEachSub() {
        var events = emptyList<FileTreeEvent>()
        val testee = Visitor2EventAdapter(onFileTreeEvent = { it ->
            events = events + it
            OnFileTreeEvent.Action.Continue
        })

        FilesInTests.withTempDirectory("visitor-test") { startingPoint ->
            val sub = mkDir("sub").current

            val result = Files.walkFileTree(startingPoint, testee)

            assertThat(result).isSameAs(startingPoint);
            assertThat(events).containsExactly(
                    FileTreeEvent.Down(startingPoint),
                    FileTreeEvent.Down(sub),
                    FileTreeEvent.Up(sub),
                    FileTreeEvent.Up(startingPoint)
            )
        }
    }

    @Test
    fun fileGivesFileWithAttributes() {
        var events = emptyList<FileTreeEvent>()
        val testee = Visitor2EventAdapter(onFileTreeEvent = { it ->
            events = events + it
            OnFileTreeEvent.Action.Continue
        })

        FilesInTests.withTempDirectory("visitor-test") { startingPoint ->
            val filePath = createFile("test", ByteArray(123))
            var timeStamp = Files.getLastModifiedTime(filePath);

            val result = Files.walkFileTree(startingPoint, testee)

            assertThat(result).isSameAs(startingPoint);
            assertThat(events).containsExactly(
                    FileTreeEvent.Down(startingPoint),
                    FileTreeEvent.File(filePath, 123, LastModified.from(timeStamp)),
                    FileTreeEvent.Up(startingPoint)
            )
        }
    }

    @Test
    fun symlinkGivesSymlink() {
        var events = emptyList<FileTreeEvent>()
        val testee = Visitor2EventAdapter(onFileTreeEvent = { it ->
            events = events + it
            OnFileTreeEvent.Action.Continue
        })

        FilesInTests.withTempDirectory("visitor-test") { startingPoint ->
            val filePath = createFile("test", ByteArray(123))
            val timeStamp = Files.getLastModifiedTime(filePath)

            val symLink = createSymLink("sym", filePath)
            val symLinkTimeStamp = Files.getLastModifiedTime(symLink)

            val result = Files.walkFileTree(startingPoint, testee)

            assertThat(result).isSameAs(startingPoint);
            assertThat(events).containsExactly(
                    FileTreeEvent.Down(startingPoint),
                    FileTreeEvent.File(filePath, 123, LastModified.from(timeStamp)),
                    FileTreeEvent.SymLink(symLink, filePath, LastModified.from(symLinkTimeStamp)),
                    FileTreeEvent.Up(startingPoint)
            )
        }
    }

    @Test
    fun filterPathMustSkipDirectoryContent() {
        var events = emptyList<FileTreeEvent>()

        val onFileTreeEvent = OnFileTreeEvent { it ->
            events = events + it
            OnFileTreeEvent.Action.Continue
        }.withFilter { path -> path.fileName.toString() != "sub" }

        val testee = Visitor2EventAdapter(onFileTreeEvent)

        FilesInTests.withTempDirectory("visitor-test") { startingPoint ->
            val sub = mkDir("sub")
            val filePath = sub.createFile("test", ByteArray(123))
            sub.createSymLink("sym", filePath)

            val result = Files.walkFileTree(startingPoint, testee)

            assertThat(result).isSameAs(startingPoint);
            assertThat(events).containsExactly(
                    FileTreeEvent.Down(startingPoint),
                    FileTreeEvent.Up(startingPoint)
            )
        }
    }
}