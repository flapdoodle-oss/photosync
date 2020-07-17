package de.flapdoodle.fx.lazy

class ChangeableValue<T : Any>(
    initialValue: T
) : AbstractLazy<T>(), LazyValue<T>, Changeable<T> {
  private var current = initialValue

  override fun value() = current
  override fun value(value: T) {
    current = value
    changeListener.forEach { it.hasChanged(this) }
  }

  fun value(reducer: (T) -> T) {
    value(reducer(value()))
  }
}