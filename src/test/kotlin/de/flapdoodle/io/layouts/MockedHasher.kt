package de.flapdoodle.io.layouts

import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

class MockedHasher(val map: Map<Path, MockedHash>) : Hasher<MockedHash> {
    override fun hash(path: Path, size: Long): MockedHash {
        return requireNotNull(map[path]) { "could not get entry for $path" }
    }

    companion object {
        fun failingHasher() = listOf(MockedHasher(emptyMap()))
    }
}