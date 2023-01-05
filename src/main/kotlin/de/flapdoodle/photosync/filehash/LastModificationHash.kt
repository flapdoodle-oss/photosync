package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

data class LastModificationHash(private val lastModified: LastModified) : Hash<LastModificationHash> {
    companion object : Hasher<LastModificationHash> {
        override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): LastModificationHash {
            return LastModificationHash(LastModified.from(path))
        }

        override fun toString(): String {
            return LastModificationHash::class.java.simpleName
        }
    }
}