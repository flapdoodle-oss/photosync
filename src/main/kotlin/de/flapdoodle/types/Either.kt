package de.flapdoodle.types

sealed class Either<L, R> {
    data class Left<L, R>(val value: L) : Either<L, R>()
    data class Right<L, R>(val value: R) : Either<L, R>()

    companion object {
        fun <L, R> left(left: L) = Left<L, R>(left)
        fun <L, R> right(right: R) = Right<L, R>(right)
    }
}