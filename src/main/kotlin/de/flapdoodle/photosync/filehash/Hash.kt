package de.flapdoodle.photosync.filehash

interface Hash<T> {
  fun append(other: Hash<*>) : JoinedHash = JoinedHash(this,other)

  companion object {
    fun prepend(hash: Hash<*>, other: Hash<*>?): Hash<*> {
      return other?.append(hash) ?: hash
    }
  }
}