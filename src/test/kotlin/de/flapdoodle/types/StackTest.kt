package de.flapdoodle.types

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StackTest {
    private val testee = Stack<String>()

    @Test
    fun popAndPeekShouldGivePushedElement() {
        testee.push("a")

        assertThat(testee.peek()).isEqualTo("a")
        assertThat(testee.pop()).isEqualTo("a")

        assertThat(testee.peek()).isNull()
    }

    @Test
    fun popAndPeekShouldGivePushedElements() {
        testee.push("a")
        testee.push("b")
        testee.push("c")

        assertThat(testee.peek()).isEqualTo("c")
        assertThat(testee.pop()).isEqualTo("c")
        assertThat(testee.peek()).isEqualTo("b")
        assertThat(testee.pop()).isEqualTo("b")
        assertThat(testee.peek()).isEqualTo("a")
        assertThat(testee.pop()).isEqualTo("a")

        assertThat(testee.peek()).isNull()
    }

    @Test
    fun replaceLastElement() {
        testee.push("a")

        assertThat(testee.peek()).isEqualTo("a")

        testee.replace {
            assertThat(it).isEqualTo("a")
            "b"
        }
        
        assertThat(testee.pop()).isEqualTo("b")

        assertThat(testee.peek()).isNull()
    }

}