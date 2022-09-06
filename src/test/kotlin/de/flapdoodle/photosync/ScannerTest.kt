package de.flapdoodle.photosync

import de.flapdoodle.photosync.filehash.FullHash
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.sync.Command
import de.flapdoodle.photosync.sync.SyncCommand2Command
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime
import java.time.Instant

internal class ScannerTest {

    @Test
    fun diffExpected(@TempDir tempDir: Path) {
        val fileSize = 2048

        val one = ByteArrays.random(fileSize)
        val two = ByteArrays.random(fileSize)
        val three = ByteArrays.random(fileSize)
        val new = ByteArrays.random(fileSize)
        val old = ByteArrays.random(fileSize)

        val now = Instant.now()
        val before = now.minusSeconds(60)
        val after = now.plusSeconds(120)

        with(tempDir, "src") { src ->
            val src_sub = mkDir(src, "sub")
            val src_one = writeFile(src_sub,"one", one, FileTime.from(now))
            val src_new = writeFile(src_sub,"new", new, FileTime.from(after))

            val src_two = writeFile(src,"two", two, FileTime.from(before))
            val src_three = writeFile(src,"three", three, FileTime.from(after))

            with(tempDir,"dst") { dst ->
                val dst_sub = mkDir(dst, "sub")
                val dst_one = writeFile(dst_sub,"one", one, FileTime.from(now))
                val dst_two = writeFile(dst,"two", two, FileTime.from(after))
                val dst_three = writeFile(dst,"three", three, FileTime.from(before))
                val dst_old = writeFile(dst,"old", old, FileTime.from(before))

                val testee = Scanner(
                    srcPath = src,
                    dstPath = dst,
                    filter = null,
                    map = SyncCommand2Command::map,
                    mode = Mode.Merge(),
                    hasher = FullHash
                )

                val result: Scanner.Result<List<Command>> = testee.sync()

                assertThat(result.result).haveExactly(1, copy(src_three, dst_three))
                assertThat(result.result).haveExactly(1, copy(src_new, dst.resolve("sub").resolve("new"), false))
                assertThat(result.result).haveExactly(1, copyBack(src_two, dst_two))
                assertThat(result.result).haveExactly(1, remove(dst_old))

                assertThat(result.result).hasSize(4)
            }
        }
    }

    internal fun copy(src: Path, dst: Path, sameContent: Boolean = true): Condition<Command> {
        val predicate = { it: Command ->
            it is Command.Copy && it.src == src && it.dst == dst && it.sameContent == sameContent
        }
        return Condition(predicate, "copy $src to $dst ($sameContent)")
    }

    internal fun remove(dst: Path, cause: Command.Cause = Command.Cause.DeletedEntry): Condition<Command> {
        val predicate = { it: Command ->
            it is Command.Remove && it.dst == dst && it.cause == cause
        }
        return Condition(predicate, "remove $dst ($cause)")
    }

    internal fun copyBack(src: Path, dst: Path, sameContent: Boolean = true): Condition<Command> {
        val predicate = { it: Command ->
            it is Command.CopyBack && it.src == src && it.dst == dst && it.sameContent == sameContent
        }
        return Condition(predicate, "copy back $src to $dst ($sameContent)")
    }

    internal fun with(base: Path,name: String, onDirectory: (Path) -> Unit) {
        val dir = mkDir(base, name)
        onDirectory(dir)
        deleteAll(dir)
    }

    internal fun deleteAll(base: Path) {
        Files.walk(base)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete)
    }

    internal fun mkDir(base: Path, name: String): Path {
        return Files.createDirectory(base.resolve(name))
    }

    internal fun writeFile(
        base: Path,
        name: String,
        content: ByteArray,
        time: FileTime
    ): Path {
        val file = base.resolve(name)
        Files.write(file, content, StandardOpenOption.CREATE_NEW)
        Files.setLastModifiedTime(file, time)
        return file
    }
}