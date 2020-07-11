package de.flapdoodle.fx.lazy

interface LazyValue<T: Any> {
  fun value(): T

  fun addListener(listener: ChangedListener<T>)
  fun removeListener(listener: ChangedListener<T>)

  class Wrapper<T: Any>(
      delegate: LazyValue<T>,
      private val keepReference: Any
  ) : LazyValue<T> by delegate

}