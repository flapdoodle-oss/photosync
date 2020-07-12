package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.photosync.ui.config.SyncEntry
import de.flapdoodle.photosync.ui.events.ModelEvent
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Parent
import tornadofx.*
import java.math.BigDecimal
import kotlin.reflect.KClass

class AddSyncConfigView : View("Add SyncConfig") {
    override val root = borderpane {
        center {
            val source = SimpleStringProperty()
            val destination = SimpleStringProperty()
            form {
                fieldset {
                    label("Source")
                    textfield(source) {  }
                }
                fieldset {
                    label("Destination")
                    textfield(destination) {  }
                }
                fieldset {
                    button {
                        text = "Add"
                        action {
                            ModelEvent.addConfig(SyncEntry(src = source.value, dst = destination.value)).fire()
                            close()
                        }
                    }
                }
            }
        }
    }

    companion object {
        // put instance creation here
        fun openModal() {
            val view = find(AddSyncConfigView::class)
            view.openModal(stageStyle = javafx.stage.StageStyle.UTILITY)
        }
    }
}