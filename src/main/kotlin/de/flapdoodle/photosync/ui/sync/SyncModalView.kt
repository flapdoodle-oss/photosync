package de.flapdoodle.photosync.ui.sync

import de.flapdoodle.fx.extensions.bindFromFactory
import de.flapdoodle.fx.lazy.ChangeableValue
import de.flapdoodle.fx.lazy.asBinding
import de.flapdoodle.fx.lazy.map
import tornadofx.*
import java.time.Duration
import java.time.LocalDateTime

class SyncModalView : View("Sync") {
    private val result = ChangeableValue(SyncList(
            groups = emptyList(),
            dstDiskSpaceUsed = 0L,
            srcDiskSpaceUsed = 0L,
            start = LocalDateTime.now(),
            end = LocalDateTime.now()
    ))

    private val timeUsedInSeconds = result.map { "Scanned in ${Duration.between(it.start, it.end).toSeconds()}s" }

    private val syncCommandGroups = result.map { it.groups }

    override val root = borderpane {
        top {
            hbox {
                label(timeUsedInSeconds.asBinding())
                label(result.map {
                    "Diskspace used: ${it.srcDiskSpaceUsed / (1024 * 1024)} MB - ${it.dstDiskSpaceUsed / (1024 * 1024)} MB"
                }.asBinding())
            }
        }
        center {
            vbox {
                children.bindFromFactory(syncCommandGroups, SyncGroup::id, SyncGroupFactory())
            }
        }
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.width = 800.0
            it.height = 600.0
        }
    }

    companion object {
        fun openModalWith(result: SyncList) {
            val view = find(SyncModalView::class)
            view.result.value(result)
            view.openModal(stageStyle = javafx.stage.StageStyle.DECORATED)
        }
    }

}