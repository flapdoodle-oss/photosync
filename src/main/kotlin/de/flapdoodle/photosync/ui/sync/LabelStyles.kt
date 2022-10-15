package de.flapdoodle.photosync.ui.sync

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.cssclass

class LabelStyles : Stylesheet() {
    companion object {
        val unknown by cssclass("label-unknown")
        val success by cssclass("label-success")
        val failed by cssclass("label-failed")
    }

    init {
        Stylesheet.label {
            backgroundColor += Color.TRANSPARENT
            
            and(success) {
                backgroundColor += Color.DARKGREEN
            }
            and (failed) {
                backgroundColor += Color.DARKRED
            }
        }
    }
}