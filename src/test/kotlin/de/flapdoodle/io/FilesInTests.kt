package de.flapdoodle.io

import de.flapdoodle.photosync.LastModified
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class FilesInTests(private val directory: Path) : AutoCloseable {

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
            FilesInTests(base).use { 
                return callback(Helper(it.directory), it.directory)
            }
        }
    }

    override fun close() {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    class Helper(val current: Path) {
        fun mkDir(name: String, lastModified: LastModified? = null): Helper {
            val newPath = current.resolve(name)
            Files.createDirectory(newPath)
            if (lastModified!=null) {
                Files.setLastModifiedTime(newPath, LastModified.asFileTime(lastModified))
            }
            return Helper(newPath)
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
            Files.write(newPath, content, StandardOpenOption.CREATE_NEW);
            if (lastModified!=null) {
                Files.setLastModifiedTime(newPath, LastModified.asFileTime(lastModified))
            }
            return newPath
        }

        fun createSymLink(name: String, destination: Path, lastModified: LastModified? = null): Path {
            val newPath = current.resolve(name);
            Files.createSymbolicLink(newPath, destination);
            if (lastModified!=null) {
                Files.setLastModifiedTime(newPath, LastModified.asFileTime(lastModified))
            }
            return newPath
        }
    }
}