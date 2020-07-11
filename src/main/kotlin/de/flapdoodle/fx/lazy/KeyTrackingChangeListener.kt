package de.flapdoodle.fx.lazy

import javafx.collections.ObservableList

class KeyTrackingChangeListener<S: Any, K: Any, M: Any, D: Any>(
    private val src: LazyValue<List<S>>,
    private val children: ObservableList<D>,
    private val keyOf: (source: S) -> K,
    private val extract: (M) -> List<D>,
    private val map: (index: Int, Source: S, old: M?) -> M
) : ChangedListener<List<S>> {
  private var mapped = src.value().mapIndexed { index, it -> keyOf(it) to map(index, it, null) }

  init {
    children.addAll(mapped.flatMap { extract(it.second) })
  }

  override fun hasChanged(value: LazyValue<List<S>>) {
    val new = src.value()

    val current = mapped

    val merged = new.mapIndexed { index, source ->
      val key = keyOf(source)
      val alreadyMapped = current.find { it.first == key }
      key to map(index, source, alreadyMapped?.second)
    }

    val mergedKeys = merged.map { it.first }.toSet()
    val removed = current.filter { !mergedKeys.contains(it.first) }
    children.removeAll(removed.flatMap { extract(it.second) })

    val currentChildren = merged.flatMap { extract(it.second) }

    children.removeAll(currentChildren)
    children.addAll(currentChildren)

    mapped = merged
  }
}