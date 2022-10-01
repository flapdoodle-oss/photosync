package de.flapdoodle.io

import de.flapdoodle.photosync.LastModified
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class FilesInTests(private val directory: Path, val cleanUpRoot: Boolean = true) : AutoCloseable {

    companion object {
        fun newTempDirectory(prefix: String): FilesInTests {
            return FilesInTests(Files.createTempDirectory(prefix))
        }

        fun <T> withTempDirectory(prefix: String, withDirectory: Helper.(Path) -> T): T {
            newTempDirectory(prefix).use {
                return withDirectory(Helper(it.directory), it.directory);
            }
        }

        fun <T> withDirectory(base: Path, callback: Helper.(Path) -> T): T {
            FilesInTests(base, false).use {
                return callback(Helper(it.directory), it.directory)
            }
        }
    }

    override fun close() {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .filter { it -> cleanUpRoot || it != directory }
                .map(Path::toFile)
                .forEach(File::delete);
    }

    class Helper(val current: Path) {
        fun mkDir(name: String, lastModified: LastModified? = null): Helper {
            val newPath = current.resolve(name)
            val dir = Files.createDirectory(newPath)
            if (lastModified!=null) {
                LastModified.to(dir, lastModified)
            }
            return Helper(dir)
        }

        fun withMkDir(name: String, context: Helper.(Path) -> Unit): Helper {
            val newHelper = mkDir(name, null)
            context(newHelper, newHelper.current)
            return newHelper
        }

        fun withMkDir(name: String, lastModified: LastModified, context: Helper.(Path) -> Unit): Helper {
            val newHelper = mkDir(name, lastModified)
            context(newHelper, newHelper.current)
            return newHelper
        }

        fun createFile(name: String, content: String, lastModified: LastModified? = null): Path {
            return createFile(name, content.toByteArray(Charsets.UTF_8), lastModified)
        }
        
        fun createFile(name: String, content: ByteArray, lastModified: LastModified? = null): Path {
            val newPath = current.resolve(name)
            val written = Files.write(newPath, content, StandardOpenOption.CREATE_NEW);
            if (lastModified!=null) {
                LastModified.to(written, lastModified)
            }
            return written
        }

        fun createSymLink(name: String, destination: Path): Path {
            val newPath = current.resolve(name);
            val symlink = Files.createSymbolicLink(newPath, destination);
            return symlink
        }
    }
}