package de.flapdoodle.fx.lazy

import de.flapdoodle.fx.bindings.Registration
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.WeakListChangeListener


fun <S : Any, D : Any> ObservableList<D>.syncFrom(src: ObservableList<S>, map: (S) -> D): Registration {
  return ObservableLists.syncFrom(src, this, map)
}


object ObservableLists {
  fun <S : Any, D : Any> syncFrom(src: ObservableList<S>, children: ObservableList<D>, map: (S) -> D): Registration {
    children.setAll(src.map(map))

    val listener = ListChangeListener<S> { _ ->
      val mapped = src.map(map)
      children.setAll(mapped)
    }
    val weakListener = WeakListChangeListener(listener)
    val keepReferenceListener = KeepReference<D>(listOf(src, listener))

    src.addListener(weakListener)
    children.addListener(keepReferenceListener)

    return Registration {
      src.removeListener(weakListener)
      children.removeListener(keepReferenceListener)
    }
  }

}