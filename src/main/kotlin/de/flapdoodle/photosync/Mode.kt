package de.flapdoodle.photosync

import com.github.ajalt.clikt.parameters.groups.OptionGroup

// see https://ajalt.github.io/clikt/quickstart/
sealed class Mode(name: String) : OptionGroup(name) {
  class Merge : Mode("options for merge mode")
  class CopySource : Mode("options for copy source mode")
}