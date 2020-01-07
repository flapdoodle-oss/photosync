package de.flapdoodle.photosync.filehash

data class JoinedHash(val first: Hash<*>, val second: Hash<*>) : Hash<JoinedHash>