package de.flapdoodle.fx.lazy

class MapNonNull<S: Any, T: Any>(
    private val source: LazyValue<S>,
    private val map: (S) -> T?
) : AbstractLazy<T>(), LazyValue<T> {

  private var lastSource = source.value()
  private var current = map(lastSource) ?: throw IllegalArgumentException("initial value is null")

  private val listener = ChangedListener<S> {
    changeListener.forEach {
      it.hasChanged(this)
    }
  }

  init {
    source.addListener(WeakChangeListenerDelegate(listener))
  }

  override fun value(): T {
    val currentSource = source.value()
    if (currentSource!=lastSource) {
      val mapped = map(currentSource)
      current = mapped ?: current
      lastSource = currentSource
    }
    return current
  }
}