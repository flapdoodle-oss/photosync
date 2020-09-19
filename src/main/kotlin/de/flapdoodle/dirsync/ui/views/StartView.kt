package de.flapdoodle.dirsync.ui.views

import javafx.scene.Parent
import tornadofx.View
import tornadofx.hbox

class StartView : View("DirSync") {
    init {
        primaryStage.width = 1024.0
        primaryStage.height = 768.0
    }
    
    override val root = hbox {
        
    }
}