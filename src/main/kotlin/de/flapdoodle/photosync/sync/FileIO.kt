package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.LastModified
import java.nio.file.*
import java.util.*

object FileIO {
    internal fun tempFile(path: Path): TempFile {
        val id = UUID.randomUUID().toString()
        val filename = "write-check-temp-filename"+ id

        val result = Files.writeString(path.resolve(filename), id, StandardOpenOption.CREATE_NEW)
        return TempFile(result)
    }

    fun ensureBasicFileOperationOn(path: Path) {
        tempFile(path).use {
            require(it.canSetLastModifiedTime()) {"can not set lastModifiedTime on $path"}
        }
    }

    fun ensureLastModifiedMatches(src: Path, dst: Path) {
        val srcTimestamp = LastModified.from(Files.getLastModifiedTime(src, LinkOption.NOFOLLOW_LINKS))
        val dstTimestamp = LastModified.from(Files.getLastModifiedTime(dst, LinkOption.NOFOLLOW_LINKS))

        require(srcTimestamp == dstTimestamp) {"lastModifiedTime missmatch $srcTimestamp != $dstTimestamp"}
    }

}