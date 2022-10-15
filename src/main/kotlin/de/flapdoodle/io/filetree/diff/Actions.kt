package de.flapdoodle.io.filetree.diff

import de.flapdoodle.photosync.LastModified
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Actions {
    fun execute(actions: List<Action>, progressListener: (Int, Int, Action) -> Unit = { _,_,_ -> }) {
        actions.forEachIndexed { index, action ->
            progressListener(index, actions.size, action)
            execute(action)
        }
    }

    fun asHumanReadable(actions: List<Action>): List<String> {
        return actions.map { action ->
            when (action) {
                is Action.CopyFile ->
                    "cp ${action.src} ${action.dest} #size=${action.size}, replace=${action.replace}"
                is Action.SetLastModified ->
                    "touch ${action.dest} ${action.lastModified}"
                is Action.MakeDirectory ->
                    "mkdir ${action.dest}"
                is Action.Remove ->
                    "rm ${action.dest}"
            }
        }
    }

    private fun execute(action: Action) {
        when (action) {
            is Action.CopyFile -> {
                Files.copy(
                    action.src,
                    action.dest,
                    *(if (action.replace)
                        arrayOf(StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING)
                    else
                        arrayOf(StandardCopyOption.COPY_ATTRIBUTES))
                )
                val size = Files.size(action.dest)
                if (size != action.size) throw IllegalStateException("copy file size does not match: $size != ${action.size}")
            }

            is Action.SetLastModified -> Files.setLastModifiedTime(
                action.dest,
                LastModified.asFileTime(action.lastModified)
            )

            is Action.MakeDirectory -> Files.createDirectory(action.dest)

            is Action.Remove -> Files.delete(action.dest)
        }
    }
}