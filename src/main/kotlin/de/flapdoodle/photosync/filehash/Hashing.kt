package de.flapdoodle.photosync.filehash

import java.security.MessageDigest

object Hashing {
  fun sha256(input: ByteArray) = hash("SHA-256",input)

  private fun hash(type: String, input: ByteArray) =
      MessageDigest
          .getInstance(type)
          .digest(input)
          .joinToString(separator = "") { Integer.toHexString(it.toInt()) }
}