package de.flapdoodle.photosync.ui

import tornadofx.View
import tornadofx.borderpane
import tornadofx.label

class Hello : View("My View") {
  override val root = borderpane {
    center = label {
      text = "PhotoSync App"
    }
  }
}
