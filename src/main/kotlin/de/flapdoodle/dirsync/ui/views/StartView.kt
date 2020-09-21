package de.flapdoodle.dirsync.ui.views

import de.flapdoodle.dirsync.ui.config.SyncConfig
import de.flapdoodle.dirsync.ui.events.IOEventHandler
import de.flapdoodle.dirsync.ui.events.ModelEventHandler
import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.extensions.subscribeEvent
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.events.IOEvent
import javafx.application.Platform
import tornadofx.*

class StartView : View("DirSync") {
    private val currentConfig = ChangeableValue(SyncConfig())

    init {
        primaryStage.width = 1024.0
        primaryStage.height = 768.0
    }
    
    override val root = borderpane {
        subscribeEvent(IOEventHandler({ currentStage }, currentConfig).instance())
        subscribeEvent(ModelEventHandler(currentConfig).instance())
        
        top {
            menubar {
                menu("Files") {
                    item("Open") {
                        action {
                            IOEvent.load().fire()
                        }
                    }

                    item("Save") {
                        action {
                            IOEvent.save().fire()
                        }
                    }

                    separator()

                    item("Quit") {
                        action {
                            Platform.exit()
                        }
                    }
                }
            }
        }
        center {
            this+= SyncConfigFragment(currentConfig)
        }
    }
}