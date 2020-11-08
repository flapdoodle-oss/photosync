package de.flapdoodle.io

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

        fun createFile(name: String, content: ByteArray): Path {
            val newPath = current.resolve(name)
            Files.write(newPath, content, StandardOpenOption.CREATE_NEW);
            return newPath
        }

        fun createSymLink(name: String, destination: Path): Path {
            val newPath = current.resolve(name);
            Files.createSymbolicLink(newPath, destination);
            return newPath
        }
    }
}