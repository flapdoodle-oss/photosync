package de.flapdoodle.fx.lazy

abstract class AbstractLazy<T: Any> : LazyValue<T>{
  protected var changeListener: List<ChangedListener<T>> = emptyList()

  override fun addListener(listener: ChangedListener<T>) {
    changeListener = changeListener + listener
  }

  override fun removeListener(listener: ChangedListener<T>) {
    changeListener = changeListener - listener
  }
}