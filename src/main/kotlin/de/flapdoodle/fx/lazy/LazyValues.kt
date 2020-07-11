package de.flapdoodle.fx.lazy

import de.flapdoodle.fx.bindings.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.fxmisc.easybind.monadic.MonadicBinding


fun <S : Any, D : Any> LazyValue<S>.mapNonNull(map: (S) -> D?): LazyValue<D> {
  return LazyValues.mapNonNull(this, map)
}

fun <S : Any, D : Any> LazyValue<S>.map(map: (S) -> D): LazyValue<D> {
  return LazyValues.map(this, map)
}

fun <S : Any, D : Any> LazyValue<List<S>>.mapList(map: (S) -> D): LazyValue<List<D>> {
  return LazyValues.map(this) {
    it.map(map)
  }
}

fun <S : Any, D : Any> LazyValue<S>.mapToList(map: (S) -> List<D>): LazyValue<List<D>> {
  return LazyValues.map(this) {
    map(it)
  }
}

fun <A : Any, B : Any, D : Any> LazyValue<A>.merge(other: LazyValue<B>, map: (A, B) -> D): LazyValue<D> {
  return LazyValues.merge(this, other, map)
}

fun <T : Any> LazyValue<T>.asBinding(): MonadicBinding<T> {
  return LazyValues.LazyAsBinding(this)
}

fun <T : Any> LazyValue<List<T>>.asListBinding(): ObservableList<T> {
  return LazyValues.asListBinding(this)
}


object LazyValues {
  fun <S : Any, D : Any> mapNonNull(src: LazyValue<S>, map: (S) -> D?): LazyValue<D> {
    return MapNonNull(src, map)
  }

  fun <S : Any, D : Any> map(src: LazyValue<S>, map: (S) -> D): LazyValue<D> {
    return Mapped(src, map)
  }

  fun <A : Any, B : Any, D : Any> merge(a: LazyValue<A>, b: LazyValue<B>, map: (A, B) -> D): LazyValue<D> {
    return Merged(a, b, map)
  }

  fun <T: Any> asListBinding(src: LazyValue<List<T>>) : ObservableList<T> {
    val ret = FXCollections.observableArrayList<T>()
    val listener = ChangedListener<List<T>> { _ ->
      ret.setAll(src.value())
    }
    ret.setAll(src.value())
    src.addListener(WeakChangeListenerDelegate(listener))
    return Wrapper(
        delegate = ret,
        listener = listener,
        source = src
    )
  }

  class LazyAsBinding<T : Any>(
      private val src: LazyValue<T>
  ) : ObjectBinding<T>(), MonadicBinding<T> {

    val listener = ChangedListener<T> { _ ->
      invalidate()
    }

    init {
      src.addListener(WeakChangeListenerDelegate(listener))
    }

    override fun computeValue(): T {
      return src.value()
    }
  }

  class Wrapper<T>(
      delegate: ObservableList<T>,
      private val listener: ChangedListener<out Any>,
      private val source: LazyValue<out Any>
  ) : Bindings.ObservableListWrapper<T>(delegate)

}