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
    private val model = SyncConfigModel(MutableSyncConfig())

    override val root = borderpane {
        center {
            form {
                fieldset {
                    label("Source")
                    hbox {
                        textfield(model.src) {
                            hgrow = Priority.ALWAYS
                            
                            validator {
                                if (it.isNullOrBlank()) error("not set") else null
                            }
                        }
                        button("?") {
                            action {
                                model.src.value = chooseDirectory("Source", model.src.value)
                            }
                        }
                    }
                }
                fieldset {
                    label("Destination")
                    hbox {
                        textfield(model.dst) {
                            hgrow = Priority.ALWAYS
                            validator {
                                if (it.isNullOrBlank()) error("not set") else null
                            }
                        }
                        button("?") {
                            action {
                                model.dst.value = chooseDirectory("Source", model.dst.value)
                            }
                        }
                    }
                }
                fieldset {
                    button {
                        text = "Add"
                        action {
                            if (model.commit()) {
                                val config = model.item
                                ModelEvent.addConfig(SyncEntry(src = config.src!!, dst = config.dst!!)).fire()
                                close()
                            }
//                            if (!model.src.value.isNullOrBlank() && !model.dst.value.isNullOrBlank()) {
//                            }
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
        model.item = MutableSyncConfig()
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

    class MutableSyncConfig(var src: String? = null, var dst: String? = null)

    class SyncConfigModel(initialValue: MutableSyncConfig) : ItemViewModel<MutableSyncConfig>(initialValue) {
        val src = bind(MutableSyncConfig::src)
        val dst = bind(MutableSyncConfig::dst)
    }
}