package de.flapdoodle.fx.lazy

class Merged<A : Any, B : Any, T : Any>(
    private val sourceA: LazyValue<A>,
    private val sourceB: LazyValue<B>,
    private val map: (A, B) -> T
) : AbstractLazy<T>(), LazyValue<T> {

  private var lastSourceA = sourceA.value()
  private var lastSourceB = sourceB.value()
  private var sourceAHasChanged = false
  private var sourceBHasChanged = false

  private var current = map(lastSourceA, lastSourceB)

  private val listenerA = ChangedListener<A> {
    sourceAHasChanged = true
    changeListener.forEach {
      it.hasChanged(this)
    }
  }
  private val listenerB = ChangedListener<B> {
    sourceBHasChanged = true
    changeListener.forEach {
      it.hasChanged(this)
    }
  }

  init {
    sourceA.addListener(WeakChangeListenerDelegate(listenerA))
    sourceB.addListener(WeakChangeListenerDelegate(listenerB))
  }

  override fun value(): T {
    val currentSourceA = sourceA.value()
    val currentSourceB = sourceB.value()
    if (currentSourceA != lastSourceA || currentSourceB != lastSourceB || sourceAHasChanged || sourceBHasChanged) {
      current = map(currentSourceA, currentSourceB)
      lastSourceA = currentSourceA
      lastSourceB = currentSourceB
      sourceAHasChanged = false
      sourceBHasChanged = false
    }
    return current
  }
}