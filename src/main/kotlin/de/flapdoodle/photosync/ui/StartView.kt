package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.config.SyncEntry
import tornadofx.*

class StartView : View("PhotoSync") {

    private val currentConfig = ChangeableValue(SyncConfig(listOf(
            SyncEntry(src = "src", dst="dst"),
            SyncEntry(src = "foo/2020", dst="bar/2020")
    )))

    override val root = borderpane {
        top {
            menubar {
                menu("Files") {
                    item("Open") {
                        action {
                            //IOEvent.load().fire()
                        }
                    }

                    item("Save") {
                        action {
                            //IOEvent.save().fire()
                        }
                    }
                }
            }
        }

        center = SyncConfigView(currentConfig).root
        
    }
}