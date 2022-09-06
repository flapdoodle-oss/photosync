package de.flapdoodle.photosync

import java.util.concurrent.ThreadLocalRandom

object ByteArrays {
    fun random(size: Int): ByteArray {
        val random = ThreadLocalRandom.current();

        return ByteArray(size).apply {
            (0 until size).forEach {
                set(it, random.nextInt(-128, 127).toByte())
            }
        }
    }

    fun zeros(size: Int): ByteArray {
        return ByteArray(size)
    }
}

