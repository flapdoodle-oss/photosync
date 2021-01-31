package de.flapdoodle.io.resolve.commands

import de.flapdoodle.io.PathCheck
import java.nio.file.Path

fun interface Precondition {
    fun check(src: Path, destination: Path): Boolean

    operator fun plus(other: Precondition): Precondition {
        val that = this
        return Precondition { src, destination ->
            that.check(src, destination) && other.check(src, destination)
        }
    }

    companion object {
        val AlwaysOk = Precondition { _, _ -> true }

        fun expectSource(condition: PathCheck): Precondition = ExpectSource(condition)
        fun expectDestination(condition: PathCheck): Precondition = ExpectDestination(condition)

        private data class ExpectSource(val condition: PathCheck) : Precondition {
            override fun check(src: Path, destination: Path) = condition.check(src)
        }

        private data class ExpectDestination(val condition: PathCheck) : Precondition {
            override fun check(src: Path, destination: Path) = condition.check(destination)
        }
    }
}