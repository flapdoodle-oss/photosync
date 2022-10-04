package de.flapdoodle.photosync.filehash

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

object Hashing {
    fun sha256(input: ByteArray) = hash("SHA-256", input)

    private fun hash(type: String, input: ByteArray) =
        bytesToHex(
            MessageDigest
                .getInstance(type)
                .digest(input)
        )

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