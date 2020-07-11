package de.flapdoodle.fx.bindings

import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import org.fxmisc.easybind.monadic.MonadicBinding

fun <S : Any, T : Any> ObservableValue<S>.mapNullable(map: (S?) -> T?): MonadicBinding<T> {
  return Bindings.map(this, map)
}

object Bindings {

  fun <T, U> map(
      src: ObservableValue<T>,
      f: (T?) -> U?): MonadicBinding<U> {
    return object : PreboundBinding<U>(src) {
      override fun computeValue(): U? {
        return f(src.value)
      }
    }
  }

  abstract class ObservableListWrapper<T>(
      private val delegate: ObservableList<T>
  ) : ObservableList<T> by delegate

}