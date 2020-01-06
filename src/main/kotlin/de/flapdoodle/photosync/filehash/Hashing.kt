package de.flapdoodle.photosync.filehash

import java.security.MessageDigest

object Hashing {
  fun sha256(input: ByteArray) = hash("SHA-256",input)

  private fun hash(type: String, input: ByteArray) =
      bytesToHex(MessageDigest
          .getInstance(type)
          .digest(input))

  private fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuffer()
    for (i in hash.indices) {
      val hex = Integer.toHexString(0xff and hash[i].toInt())
      if (hex.length == 1) hexString.append('0')
      hexString.append(hex)
    }
    return hexString.toString()
  }
}