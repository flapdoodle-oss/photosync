package de.flapdoodle.dirsync.ui.views

import de.flapdoodle.dirsync.ui.config.SyncEntry
import de.flapdoodle.dirsync.ui.events.ModelEvent
import de.flapdoodle.fx.extensions.fire
import javafx.scene.layout.Priority
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.nio.file.Paths
import java.util.*

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
                    label("Exclude")
                    hbox {
                        textfield(model.excludes) {
                            hgrow = Priority.ALWAYS
                        }
                    }
                }
                fieldset {
                    button {
                        text = "Add"
                        action {
                            if (model.commit()) {
                                val config = model.item
                                ModelEvent.addOrChangeConfig(
                                        SyncEntry(
                                                id = config.id ?: UUID.randomUUID(),
                                                src = config.src!!,
                                                dst = config.dst!!,
                                                excludes = config.excludes?.split(" ")?.toList()
                                                        ?: emptyList()
                                        )
                                ).fire()
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

    private fun setModel(source: SyncEntry?) {
        if (source!=null) {
            model.id.value = source.id
            model.src.value = source.src
            model.dst.value = source.dst
            model.excludes.value = source.excludes.joinToString(separator = " ")
        }
    }

    companion object {
        // put instance creation here
        fun openModal(source: SyncEntry? = null) {
            val view = find(AddSyncConfigView::class)
            view.clearInputs()
            view.setModel(source)
            view.openModal(stageStyle = javafx.stage.StageStyle.UTILITY)
        }
    }

    private fun directoryChooser(directory: String?): DirectoryChooser {
        return DirectoryChooser().apply {
            if (directory != null) {
                val toFile = Paths.get(directory).toFile()
                initialDirectory = if (toFile.exists()) toFile else null
            }
//            extensionFilters.addAll(
//                    FileChooser.ExtensionFilter("All Files", "*.*"),
//                    FileChooser.ExtensionFilter("Tab File", "*.tab")
//            )
        }
    }

    class MutableSyncConfig(
            val id: UUID? = null,
            var src: String? = null,
            var dst: String? = null,
            var excludes: String? = null
    )

    class SyncConfigModel(initialValue: MutableSyncConfig) : ItemViewModel<MutableSyncConfig>(initialValue) {
        val id = bind(MutableSyncConfig::id)
        val src = bind(MutableSyncConfig::src)
        val dst = bind(MutableSyncConfig::dst)
        val excludes = bind(MutableSyncConfig::excludes)
    }
}