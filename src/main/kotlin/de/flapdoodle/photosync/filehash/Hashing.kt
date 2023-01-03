package de.flapdoodle.photosync.filehash

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

object Hashing {
  fun sha256(input: ByteArray) = hash("SHA-256", input)
  fun sha256(updater: MessageDigest.() -> Unit) = hash("SHA-256", updater)

  private fun hash(type: String, input: ByteArray): String {
    return hash(type) {
      update(input)
    }
  }

  private fun hash(type: String, updater: MessageDigest.() -> Unit): String {
    val instance: MessageDigest = hashInstance(type)
    updater(instance)
    return bytesToHex(
      instance
        .digest()
    )
  }

  private fun hashInstance(type: String): MessageDigest =
    MessageDigest.getInstance(type)

  private fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuffer()
    for (i in hash.indices) {
      val hex = Integer.toHexString(0xff and hash[i].toInt())
      if (hex.length == 1) hexString.append('0')
      hexString.append(hex)
    }
    return hexString.toString()
  }

  internal fun read(path: Path, offset: Long, len: Int): ByteArray {
    return Files.newByteChannel(path).use {
      val buffer = ByteBuffer.allocate(len)
      it.position(offset)
      val readSize = it.read(buffer)
      buffer.flip()

      val block = ByteArray(readSize)
      buffer.get(block, 0, readSize)
      block
    }
  }
}