package de.flapdoodle.fx.lazy

interface Changeable<T: Any> {
  fun value(value: T)
}