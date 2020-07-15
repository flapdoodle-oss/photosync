package de.flapdoodle.photosync.ui

import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.asBinding
import de.flapdoodle.fx.lazy.map
import de.flapdoodle.photosync.Scanner
import de.flapdoodle.photosync.sync.SyncCommandGroup
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.time.Duration
import java.time.LocalDateTime

class SyncModalView : View("Sync") {
    private val result = ChangeableValue<Scanner.Result<List<SyncCommandGroup>>>(Scanner.Result(
            result = emptyList(),
            dstDiskSpaceUsed = 0L,
            srcDiskSpaceUsed = 0L,
            start = LocalDateTime.now(),
            end = LocalDateTime.now()
    ))

    private val timeUsedInSeconds = result.map { "${Duration.between(it.start, it.end).toSeconds()}s" }

    override val root = borderpane {
        top {
            label(timeUsedInSeconds.asBinding())
        }
        center {

        }
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.width = 800.0
            it.height = 600.0
        }
    }

    companion object {
        fun openModalWith(result: Scanner.Result<List<SyncCommandGroup>>) {
            val view = find(SyncModalView::class)
            view.result.value(result)
            val stage = view.openModal(stageStyle = javafx.stage.StageStyle.DECORATED)
//            if (stage!=null) {
//                stage.width = 800.0
//                stage.height = 600.0
//            }
        }
    }
}