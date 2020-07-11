package de.flapdoodle.fx.bindings

class Registration(private val action: () -> Unit) {
  fun remove() {
    action.invoke()
  }

  fun join(vararg registrations: Registration): Registration {
    return Registration {
      this.remove()
      registrations.forEach(Registration::remove)
    }
  }
}
