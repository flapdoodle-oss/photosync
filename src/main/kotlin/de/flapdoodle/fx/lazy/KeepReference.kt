package de.flapdoodle.fx.lazy

import javafx.collections.ListChangeListener

class KeepReference<T>(
    private val reference: Any
) : ListChangeListener<T> {
  override fun onChanged(c: ListChangeListener.Change<out T>?) {}
}