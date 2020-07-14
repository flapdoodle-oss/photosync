package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.extensions.fire
import de.flapdoodle.fx.extensions.subscribeEvent
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.config.SyncConfig
import de.flapdoodle.photosync.ui.events.ActionEvent
import de.flapdoodle.photosync.ui.events.IOEvent
import de.flapdoodle.photosync.ui.events.ModelEvent
import de.flapdoodle.photosync.ui.io.SyncConfigIO
import de.flapdoodle.photosync.ui.tasks.TaskList
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import tornadofx.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*

class StartView : View("PhotoSync") {

    private val currentConfig = ChangeableValue(SyncConfig())

    private val taskList = TaskList()

    override val root = borderpane {
        subscribeEvent<ModelEvent> { event ->
            when (event.data) {
                is ModelEvent.EventData.AddConfig -> {
                    currentConfig.value(event.data.applyTo(currentConfig.value()))
                }
                is ModelEvent.EventData.DeleteConfig -> {
                    currentConfig.value(event.data.applyTo(currentConfig.value()))
                }
                else -> {
                    throw IllegalArgumentException("unknown event: ${event}")
                }
            }
        }

        subscribeEvent<ActionEvent> { event ->
            println("Action found: $event")

            when (event.action) {
                is ActionEvent.Action.StartSync -> {
                    taskList.startSync(event.action.id) { id -> startSync(currentConfig.value(), id)}

//                    val task: Task<String> = runAsync {
//                        val result = startSync(currentConfig.value(), event.action.id)
//
//                        (0..100L).forEach {
//                            if (!isCancelled) {
//                                updateMessage("running")
//                                updateProgress(it, 100)
//                                Thread.sleep(30)
//                            }
//                        }
//
//                        result
//                    } success {
//                        runningTasks = runningTasks - event.action.id
//                        println("result for ${event.action.id} -> $it")
//
//                        ActionEvent.syncFinished(event.action.id).fire()
//                    } fail {
//                        runningTasks = runningTasks - event.action.id
//
//                        val alert = Alert(Alert.AlertType.ERROR, it.message)
//                        alert.headerText = "Error Loading Images"
//                        alert.showAndWait()
//
//                        ActionEvent.syncAborted(event.action.id).fire()
//                    } cancel {
//                        runningTasks = runningTasks - event.action.id
//
//                        val alert = Alert(Alert.AlertType.INFORMATION, "Operation Cancelled")
//                        alert.headerText = "Loading Images"
//                        alert.showAndWait()
//
//                        ActionEvent.syncAborted(event.action.id).fire()
//                    }
//
//                    runningTasks = runningTasks + (event.action.id to task)
//                    ActionEvent.syncStarted(event.action.id).fire()
                }
                is ActionEvent.Action.StopSync -> {
                    taskList.stopSync(event.action.id)
                }
            }
        }

        subscribeEvent<IOEvent> { event ->
            when (event.action) {
                IOEvent.Action.Load -> {
                    val fileChooser = fileChooser()
                    fileChooser.title = "Open File"
                    val file = fileChooser.showOpenDialog(currentStage)
                    println("load $file")
                    if (file != null) {
                        val content = Files.readAllBytes(file.toPath())
                        //model.value(TabModel())
                        val newConfig = SyncConfigIO.fromJson(String(content, Charsets.UTF_8))
                        currentConfig.value(newConfig)
                    }
                }

                IOEvent.Action.Save -> {
                    val fileChooser = fileChooser()
                    fileChooser.title = "Save File"
                    fileChooser.initialFileName = "sample.psync"
                    val file = fileChooser.showSaveDialog(currentStage)
                    println("write to $file")
                    if (file != null) {
                        val json = SyncConfigIO.asJson(currentConfig.value())
                        Files.write(file.toPath(), json.toByteArray(Charsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                    }
                }
            }
        }

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

        center = SyncConfigView(currentConfig).root

        bottom {
            this += taskList
        }

    }

    private fun startSync(config: SyncConfig, id: UUID): String {
        val matching = config.entries.filter { it.id == id }
        require(matching.size == 1) { "could not find entry $id in $config" }

        println("sync $matching")

        return "DONE"
    }

    private fun fileChooser(): FileChooser {
        return FileChooser().apply {
            extensionFilters.addAll(
                    FileChooser.ExtensionFilter("All Files", "*.*"),
                    FileChooser.ExtensionFilter("Tab File", "*.psync")
            )
        }
    }

}