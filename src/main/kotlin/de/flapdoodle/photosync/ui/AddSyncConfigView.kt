package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.photosync.ui.config.SyncEntry
import de.flapdoodle.photosync.ui.events.ModelEvent
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.nio.file.Paths

class AddSyncConfigView : View("Add SyncConfig") {
    private val source = SimpleStringProperty()
    private val destination = SimpleStringProperty()

    override val root = borderpane {
        center {
            form {
                fieldset {
                    label("Source")
                    textfield(source) {
                        hgrow = Priority.ALWAYS
//                        validator {
//                            if (it.isNullOrBlank()) error("not set") else null
//                        }
                    }
                    button("?") {
                        action {
                            source.value = chooseDirectory("Source", source.value)
                        }
                    }
                }
                fieldset {
                    label("Destination")
                    hbox {
                        textfield(destination) {
                            hgrow = Priority.ALWAYS
//                            validator {
//                                if (it.isNullOrBlank()) error("not set") else null
//                            }
                        }
                        button("?") {
                            action {
                                destination.value = chooseDirectory("Source", destination.value)
                            }
                        }
                    }
                }
                fieldset {
                    button {
                        text = "Add"
                        action {
                            if (!source.value.isNullOrBlank() && !destination.value.isNullOrBlank()) {
                                ModelEvent.addConfig(SyncEntry(src = source.value, dst = destination.value)).fire()
                                close()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun chooseDirectory(title: String, directory: String?): String {
        val fileChooser = directoryChooser(directory)
        fileChooser.title = title
        val file = fileChooser.showDialog(currentStage)
        return file.absolutePath
    }

    private fun clearInputs() {
        source.set("")
        destination.set("")
    }

    companion object {
        // put instance creation here
        fun openModal() {
            val view = find(AddSyncConfigView::class)
            view.clearInputs()
            view.openModal(stageStyle = javafx.stage.StageStyle.UTILITY)
        }
    }

    private fun directoryChooser(directory: String?): DirectoryChooser {
        return DirectoryChooser().apply {
            if (directory!=null) {
                val toFile = Paths.get(directory).toFile()
                initialDirectory = if (toFile.exists()) toFile else null
            }
//            extensionFilters.addAll(
//                    FileChooser.ExtensionFilter("All Files", "*.*"),
//                    FileChooser.ExtensionFilter("Tab File", "*.tab")
//            )
        }
    }
}