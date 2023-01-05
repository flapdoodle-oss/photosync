package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.ThreadLocalRandom

abstract class AbstractHashTest {
    val random = ThreadLocalRandom.current();

    internal fun <H: Hash<H>, T: Hasher<H>> with(testee: T, tempDir: Path, first: ByteArray, second: ByteArray, check: (H, H) -> Unit) {
        writeFile(tempDir, "A", first)
            .use { a ->
                writeFile(tempDir, "B", second)
                    .use { b ->
                        val firstHash = testee.hash(a.path, a.size, LastModified.from(a.path))
                        val secondHash = testee.hash(b.path, b.size, LastModified.from(b.path))
                        check(firstHash,secondHash)
                    }
            }
    }

    data class TempFile(val path: Path, val size: Long) : AutoCloseable {
        override fun close() {
            Files.delete(path);
        }
    }

    internal fun writeFile(base: Path, name: String, content: ByteArray): TempFile {
        val file = base.resolve(name)
        Files.write(file, content, StandardOpenOption.CREATE_NEW)
        return TempFile(file, content.size.toLong())
    }

}