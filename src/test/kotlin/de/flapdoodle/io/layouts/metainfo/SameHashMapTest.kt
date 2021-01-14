package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.io.layouts.MockedHash
import de.flapdoodle.photosync.filehash.Hash
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class SameHashMapTest {

    @Test
    fun emptySameHashMapMustFailOnAnyCall() {
        val testee = SameHashMap<String>(emptyList())

        assertThatThrownBy { testee.get("foo") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun buildMapShouldExtractCases() {
        val src = mapOf(1 to "src/one", 2 to "src/only-2", 3 to "src/multi-1", 4 to "src/multi-2")
        val dst = mapOf(5 to "dst/one", 6 to "dst/only-3", 7 to "dst/multi-1", 8 to "dst/multi-2")
        val groupedByHash = mapOf<Hash<*>, List<Int>>(
            MockedHash(1) to listOf(1,5),
            MockedHash(2) to listOf(2),
            MockedHash(3) to listOf(6),
            MockedHash(4) to listOf(3,4,7,8),
        )
        val testee = SameHashMap.from(src,dst,groupedByHash)

        assertThat(testee.get("src/one"))
            .isEqualTo(SameHashMap.SameHash.Direct("src/one","dst/one", MockedHash(1)))
        assertThat(testee.get("src/only-2"))
            .isEqualTo(SameHashMap.SameHash.OnlySource("src/only-2", MockedHash(2)))
        assertThat(testee.get("dst/only-3"))
            .isEqualTo(SameHashMap.SameHash.OnlyDestination("dst/only-3", MockedHash(3)))
        assertThat(testee.get("src/multi-1"))
            .isEqualTo(SameHashMap.SameHash.Multi(listOf("src/multi-1", "src/multi-2"), listOf("dst/multi-1","dst/multi-2"),
                MockedHash(4)
            ))
    }

}