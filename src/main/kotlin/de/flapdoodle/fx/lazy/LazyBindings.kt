package de.flapdoodle.fx.lazy

import de.flapdoodle.fx.bindings.Registration
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.WeakListChangeListener

fun <S : Any, D : Any> ObservableList<D>.bindFrom(src: LazyValue<List<S>>, map: (S) -> D): Registration {
  return LazyBindings.bindFrom(src, this, map)
}

@Deprecated("dont use")
fun <S : Any, D : Any> ObservableList<D>.bindFrom(
    src: LazyValue<List<S>>,
    reIndex: (index: Int, source: S, mapped: List<D>) -> Unit,
    map: (index: Int, source: S) -> List<D>): Registration {
  return LazyBindings.bindFrom(src, this, reIndex, map)
}

fun <S : Any, K: Any, M: Any, D : Any> ObservableList<D>.bindFrom(
    src: LazyValue<List<S>>,
    keyOf: (source: S) -> K,
    extract: (M) -> List<D>,
    map: (index: Int, source: S, old: M?) -> M
): Registration {
  return LazyBindings.bindFrom(src, this, keyOf, extract, map)
}

fun <S : Any, D : Any> ObservableList<D>.syncFrom(src: LazyValue<List<S>>, map: (S) -> D): Registration {
  return LazyBindings.syncFrom(src, this, map)
}

fun <S : Any, D : Any> ObservableList<D>.flatMapIndexedFrom(src: LazyValue<List<S>>, map: (Int, S) -> List<D>): Registration {
  return LazyBindings.flatMapFrom(src, this) {
    it.mapIndexed { index, s -> map(index, s) }.flatten()
  }
}

fun <S : Any> ObservableList<S>.asAObservable(): LazyValue<List<S>> {
  return LazyBindings.asAObservable(this)
}

object LazyBindings {
  fun <S : Any, D : Any> bindFrom(src: LazyValue<List<S>>, children: ObservableList<D>, map: (S) -> D): Registration {
    val listener = FactoryTrackingChangeListener(src, children, map)
    val weakListener = WeakChangeListenerDelegate(listener)
    val keepReferenceListener = KeepReference<Any>(listener)

    src.addListener(weakListener)
    children.addListener(keepReferenceListener)

    return Registration {
      src.removeListener(weakListener)
      children.removeListener(keepReferenceListener)
    }
  }

  fun <S : Any, D : Any> bindFrom(
      src: LazyValue<List<S>>,
      children: ObservableList<D>,
      reIndex: (index: Int, source: S, mapped: List<D>) -> Unit,
      map: (index: Int, source: S) -> List<D>
  ): Registration {
    val listener = FlatMapTrackingChangeListener(
        src,
        children,
        map,
        reIndex
    )

    val weakListener = WeakChangeListenerDelegate(listener)
    val keepReferenceListener = KeepReference<Any>(listener)

    src.addListener(weakListener)
    children.addListener(keepReferenceListener)

    return Registration {
      src.removeListener(weakListener)
      children.removeListener(keepReferenceListener)
    }
  }

  fun <S: Any, K: Any, M: Any, D: Any> bindFrom(
      src: LazyValue<List<S>>,
      children: ObservableList<D>,
      keyOf: (source: S) -> K,
      extract: (M) -> List<D>,
      map: (index: Int, source: S, old: M?) -> M
  ): Registration {
    val listener = KeyTrackingChangeListener(
        src,
        children,
        keyOf,
        extract,
        map
    )

    val weakListener = WeakChangeListenerDelegate(listener)
    val keepReferenceListener = KeepReference<Any>(listener)

    src.addListener(weakListener)
    children.addListener(keepReferenceListener)

    return Registration {
      src.removeListener(weakListener)
      children.removeListener(keepReferenceListener)
    }
  }

  fun <S : Any, D : Any> syncFrom(src: LazyValue<List<S>>, children: ObservableList<D>, map: (S) -> D): Registration {
    return flatMapFrom(src, children) { it.map(map) }
  }

  fun <S : Any, D : Any> flatMapFrom(src: LazyValue<S>, children: ObservableList<D>, map: (S) -> List<D>): Registration {
    children.setAll(map(src.value()))

    val listener = ChangedListener<S> { s ->
      val mapped = map(s.value())
      children.setAll(mapped)
    }
    val weakListener = WeakChangeListenerDelegate(listener)
    val keepReferenceListener = KeepReference<D>(listOf(src, listener))

    src.addListener(weakListener)
    children.addListener(keepReferenceListener)

    return Registration {
      src.removeListener(weakListener)
      children.removeListener(keepReferenceListener)
    }
  }

  fun <S : Any> asAObservable(src: ObservableList<S>): LazyValue<List<S>> {
    val ret = ChangeableValue<List<S>>(src.toList())
    val listener = ListChangeListener<S> {
      ret.value(it.list)
    }
    val weakListener = WeakListChangeListener(listener)
    src.addListener(weakListener)

    return LazyValue.Wrapper(ret, listener)
  }

  class FactoryTrackingChangeListener<S : Any, D : Any>(
      private val src: LazyValue<List<S>>,
      private val children: ObservableList<D>,
      private val map: (S) -> D
  ) : ChangedListener<List<S>> {

    private var mapped = src.value().map { it to map(it) }

    init {
      children.addAll(mapped.map { it.second })
    }

    override fun hasChanged(value: LazyValue<List<S>>) {
      val new = src.value()

      val current = mapped
      val currentSources = current.map { it.first }

      val removed = current.filter { !new.contains(it.first) }
      children.removeAll(removed.map { it.second })

      val newIds = new.filter { !currentSources.contains(it) }
      val added = newIds.map { it to map(it) }
      children.addAll(added.map { it.second })

      mapped = current - removed + added
    }
  }

  @Deprecated("replace with TrackingChangeListener")
  class FlatMapTrackingChangeListener<S : Any, D : Any>(
      private val src: LazyValue<List<S>>,
      private val children: ObservableList<D>,
      private val map: (index: Int, source: S) -> List<D>,
      private val reIndex: (index: Int, source: S, mapped: List<D>) -> Unit
  ) : ChangedListener<List<S>> {
    private var mapped = src.value().mapIndexed { index, it -> it to map(index, it) }

    init {
      children.addAll(mapped.flatMap { it.second })
    }

    override fun hasChanged(value: LazyValue<List<S>>) {
//      println("------------------------------")
//      println("children ->")
//      children.forEach {
//        println("-> $it")
//      }

      val new = src.value()
//      println("new ->")
//      new.forEach {
//        println("-> $it")
//      }

      val current = mapped

      val removed = current.filter { !new.contains(it.first) }
//      println("removed ->")
//      removed.forEach {
//        println("-> $it")
//      }

      children.removeAll(removed.flatMap { it.second })

      val merged = new.mapIndexed { index, source ->
        val alreadyMapped = current.find { it.first == source }
        if (alreadyMapped != null) {
          reIndex(index, source, alreadyMapped.second)
          alreadyMapped
        } else {
          val added = map(index, source)
          source to added
        }
      }

      val currentChildren = merged.flatMap { it.second }
//      println("currentChildren ->")
//      currentChildren.forEach {
//        println("-> $it")
//      }

      children.removeAll(currentChildren)
      children.addAll(currentChildren)
//      children.setAll(currentChildren)

//      println("merged-> $merged")
//
//      println("children ->")
//      children.forEach {
//        println("-> $it")
//      }

      mapped = merged
    }
  }
}