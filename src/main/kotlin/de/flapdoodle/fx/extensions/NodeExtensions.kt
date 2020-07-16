package de.flapdoodle.fx.extensions

import de.flapdoodle.fx.lazy.LazyValue
import de.flapdoodle.fx.lazy.bindFrom
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.Parent
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

val Node.property: ObservableMapExtensions.TypedMap
  get() = ObservableMapExtensions.TypedMap(this.properties)

val Node.constraint: ObservableMapExtensions.TypedMap
  get() = object : ObservableMapExtensions.TypedMap(this.properties) {
    override fun <T : Any> set(key: Key<T>, value: T?): T? {
      return super.set(key, value).also {
        this@constraint.parent?.requestLayout()
      }
    }

    override fun <T : Any> set(type: KClass<T>, value: T?): T? {
      return super.set(type, value).also {
        this@constraint.parent?.requestLayout()
      }
    }
  }

fun <T : Any> Parent.findAllInTree(type: KClass<T>): List<T> {
  return childrenUnmodifiable.flatMap {
    val nodeAsList = if (type.isInstance(it)) {
      @Suppress("UNCHECKED_CAST")
      listOf(it as T)
    } else
      emptyList()

    val sub = if (it is Parent) it.findAllInTree(type) else emptyList()

    nodeAsList + sub
  }
}

fun Node.parentPath(child: Node): List<Node> {
  return if (child.parent == this) {
    listOf(this, child)
  } else {
    val parentOfChild = child.parent
    return this.parentPath(parentOfChild) + child
  }
}


fun <T : Node> Node.parentOfType(type: KClass<T>): T? {
  println("parent of $this -> $parent (search $type)")
  if (parent == null) return null
  type.safeCast(parent)?.also { return it }
  return parent?.parentOfType(type)
}

fun Node.widthLimits(): Pair<Double, Double> {
  val minW = this.minWidth(-1.0)
  return if (isResizable) {
    val maxW = maxWidth(-1.0)
    Pair(minW, if (maxW>0.0) maxW else Double.MAX_VALUE)
  } else {
    Pair(minW, minW)
  }
}

fun Node.heightLimits(): Pair<Double, Double> {
  val minH = minHeight(-1.0)
  return if (isResizable) {
    val maxH = maxHeight(-1.0)
    Pair(minH, if (maxH>0.0) maxH else Double.MAX_VALUE)
  } else {
    Pair(minH, minH)
  }
}

interface LazyNodeContainer<S: Any> {
  fun nodes(): List<Node>
}

interface LazyNodeFactory<S: Any, M: LazyNodeContainer<S>> {
  fun create(index: Int, source: S, old: M?): M
}

fun <S : Any, K: Any, M: LazyNodeContainer<S>> ObservableList<Node>.bindFromFactory(
        src: LazyValue<List<S>>,
        keyOf: (source: S) -> K,
        factory: LazyNodeFactory<S, M>
) {
  this.bindFrom(src, keyOf, LazyNodeContainer<S>::nodes, factory::create);
}