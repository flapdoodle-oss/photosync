package de.flapdoodle.io

import java.nio.file.Path

fun interface PathCheck {
    fun check(path: Path): Boolean

    operator fun plus(other: PathCheck): PathCheck {
        return And(this,other)
    }

    companion object {
        class And(val first: PathCheck, val second: PathCheck): PathCheck {
            override fun check(path: Path) = first.check(path) && second.check(path)

        }
    }
}