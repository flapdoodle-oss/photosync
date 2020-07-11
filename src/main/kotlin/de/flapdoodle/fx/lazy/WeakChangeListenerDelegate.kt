package de.flapdoodle.fx.lazy

import java.lang.ref.WeakReference

class WeakChangeListenerDelegate<T: Any>(
  delegate: ChangedListener<T>
) : ChangedListener<T> {

  private val weakReference = WeakReference(delegate)

  override fun hasChanged(value: LazyValue<T>) {
    val org = weakReference.get()
    if (org!=null) {
      org.hasChanged(value)
    } else {
      value.removeListener(this)
    }
  }
}