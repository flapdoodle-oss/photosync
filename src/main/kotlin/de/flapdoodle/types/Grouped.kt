package de.flapdoodle.types

sealed class Grouped<P,C> {
    data class Parent<P,C>(val parent: P) : Grouped<P,C>()
    data class Child<P,C>(val parent: P, val child: C) : Grouped<P,C>()

    companion object {
        private fun <T: Any, P: Any, C: Any> asList(transform: (T) -> Pair<P, Iterable<C>>): (T) -> Iterable<Grouped<P,C>> {
            return { t ->
                val pair = transform(t)
                listOf<Grouped<P,C>>(Parent(pair.first)) + pair.second.map { Child(pair.first, it) }
            }
        }

        fun <T: Any, P: Any, C: Any> flatMapGrouped(src: Iterable<T>, transform: (T) -> Pair<P, Iterable<C>>): List<Grouped<P, C>> {
            return src.flatMap(asList(transform))
        }
    }
}