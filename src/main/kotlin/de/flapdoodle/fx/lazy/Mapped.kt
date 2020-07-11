package de.flapdoodle.fx.lazy

class Mapped<S : Any, T : Any>(
    private val source: LazyValue<S>,
    private val map: (S) -> T
) : AbstractLazy<T>(), LazyValue<T> {

  private var lastSource = source.value()
  private var sourceHasChanged = false
  private var current = map(lastSource)

  private val listener = ChangedListener<S> {
    sourceHasChanged = true
    changeListener.forEach {
      it.hasChanged(this)
    }
  }

  init {
    source.addListener(WeakChangeListenerDelegate(listener))
  }

  override fun value(): T {
    val currentSource = source.value()
    if (currentSource != lastSource || sourceHasChanged) {
      current = map(currentSource)
      lastSource = currentSource
      sourceHasChanged = false
    }
    return current
  }
}