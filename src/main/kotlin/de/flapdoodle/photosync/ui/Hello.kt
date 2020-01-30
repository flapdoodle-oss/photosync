package de.flapdoodle.photosync.ui

import tornadofx.*

class Hello : View("My View") {
  override val root = borderpane {
    center = label {
      text = "PhotoSync App"
    }
  }
}
