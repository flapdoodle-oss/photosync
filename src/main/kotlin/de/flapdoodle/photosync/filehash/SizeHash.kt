package de.flapdoodle.photosync.filehash

import java.nio.file.Path

data class SizeHash(private val size: Long) : Hash<SizeHash> {
    companion object : Hasher<SizeHash> {
        override fun hash(path: Path, size: Long): SizeHash {
            return SizeHash(size)
        }

        override fun toString(): String {
            return SizeHash::class.java.simpleName
        }
    }
}