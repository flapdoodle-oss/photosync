package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

data class SizeHash(private val size: Long) : Hash<SizeHash> {
    companion object : Hasher<SizeHash> {
        override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): SizeHash {
            return SizeHash(size)
        }

        override fun toString(): String {
            return SizeHash::class.java.simpleName
        }
    }
}