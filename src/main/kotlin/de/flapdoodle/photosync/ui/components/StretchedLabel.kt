package de.flapdoodle.photosync.ui.components

import javafx.beans.value.ObservableValue
import javafx.scene.Parent
import javafx.scene.paint.Color
import tornadofx.*

class StretchedLabel(textProperty: ObservableValue<String>) : Fragment() {

    override val root = hbox {
        style {
            borderWidth += box(1.0.px)
            borderColor += box(Color.RED)
        }

        label(textProperty)
    }
}