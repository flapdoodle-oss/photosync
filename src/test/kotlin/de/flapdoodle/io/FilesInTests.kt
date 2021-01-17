package de.flapdoodle.io

import de.flapdoodle.photosync.LastModified
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime

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
    }

    override fun close() {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    class Helper(val current: Path) {
        fun mkDir(name: String): Helper {
            val newPath = current.resolve(name)
            Files.createDirectory(newPath)
            return Helper(newPath)
        }

        fun mkDir(name: String, context: Helper.(Path) -> Unit): Helper {
            val newHelper = mkDir(name)
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