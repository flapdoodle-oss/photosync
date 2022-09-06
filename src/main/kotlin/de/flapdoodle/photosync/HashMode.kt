package de.flapdoodle.photosync

import com.github.ajalt.clikt.parameters.groups.OptionGroup

// see https://ajalt.github.io/clikt/quickstart/
sealed class HashMode(name: String) : OptionGroup(name) {
  class Quick : HashMode("quick hash")
  class Full : HashMode("full hash")
}