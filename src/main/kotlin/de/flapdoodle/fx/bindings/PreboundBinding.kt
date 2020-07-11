package de.flapdoodle.fx.bindings

import javafx.beans.binding.ObjectBinding
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import org.fxmisc.easybind.monadic.MonadicBinding

/**
 * Object binding that binds to its dependencies on creation
 * and unbinds from them on dispose.
 */
abstract class PreboundBinding<T>(
    private vararg val dependencies: ObservableValue<*>
) : ObjectBinding<T>(), MonadicBinding<T> {

  private val changeToInvalidListener = ChangeListener<Any> { _,_,new ->
    invalidate()
  }

  init {
    bind(*dependencies)
    val weakListener = changeToInvalidListener.wrapByWeakChangeListener()
    dependencies.forEach { it.addListener(weakListener) }
  }

  override fun dispose() {
    unbind(*dependencies)
  }
}
