package de.flapdoodle.dirsync.ui.events

import de.flapdoodle.dirsync.ui.config.SyncConfig
import de.flapdoodle.dirsync.ui.io.SyncConfigIO
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.photosync.ui.events.IOEvent
import javafx.stage.FileChooser
import tornadofx.EventContext
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class IOEventHandler(
        val currentStage: () -> javafx.stage.Stage?,
        val currentConfig: ChangeableValue<SyncConfig>
) {
    fun instance(): EventContext.(IOEvent) -> Unit = { event ->
        when (event.action) {
            IOEvent.Action.Load -> {
                val fileChooser = fileChooser()
                fileChooser.title = "Open File"
                val file = fileChooser.showOpenDialog(currentStage())
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
                fileChooser.initialFileName = "sample.dsync"
                val file = fileChooser.showSaveDialog(currentStage())
                println("write to $file")
                if (file != null) {
                    val json = SyncConfigIO.asJson(currentConfig.value())
                    Files.write(file.toPath(), json.toByteArray(Charsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                }
            }
        }

    }

    private fun fileChooser(): FileChooser {
        return FileChooser().apply {
            extensionFilters.addAll(
                    FileChooser.ExtensionFilter("All Files", "*.*"),
                    FileChooser.ExtensionFilter("DirSync File", "*.dsync")
            )
        }
    }
}