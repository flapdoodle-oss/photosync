package de.flapdoodle.photosync.sync

import com.sun.nio.file.ExtendedCopyOption
import de.flapdoodle.photosync.progress.Progress
import de.flapdoodle.photosync.ui.sync.SyncGroup
import de.flapdoodle.photosync.ui.sync.SyncGroupID
import de.flapdoodle.photosync.ui.sync.SyncList
import java.nio.file.*
import java.util.*

class NIOSynchronizer : Synchronizer {
    override fun sync(
            set: SyncList,
            enableCopyBack: Boolean,
            enableRemove: Boolean,
            listener: (id: SyncGroupID, command: SyncCommand, status: SyncGroup.Status) -> Unit,
            progressListener: (Progress) -> Unit
    ) {
        val max = set.groups.size;

        FileIO.ensureBasicFileOperationOn(set.dstPath)

        set.groups.forEachIndexed { index, group ->
            syncGroup(group, enableCopyBack, enableRemove, listener)
//            group.commands.forEach {
//                when (it.status) {
//                    SyncGroup.Status.NotExcuted -> {
//                        listener(group.id, it.command, SyncGroup.Status.Failed)
//                        Thread.sleep(100);
//                    }
//                }
//            }
            progressListener(Progress(index.toLong(), max.toLong()))
        }
    }

    private fun syncGroup(
            group: SyncGroup,
            enableCopyBack: Boolean,
            enableRemove: Boolean,
            listener: (id: SyncGroupID, command: SyncCommand, status: SyncGroup.Status) -> Unit
    ) {
        println("sync ${group.id}")
        group.commands.forEach { entry ->
            when (entry.status) {
                SyncGroup.Status.NotExcuted -> {
                    try {
                        val skipCommand = (entry.command is SyncCommand.CopyBack && !enableCopyBack) ||
                                (entry.command is SyncCommand.Remove && !enableRemove)
                        if (!skipCommand) {
                            execute(entry.command)
                            listener(group.id, entry.command, SyncGroup.Status.Successful)
                        } else {
                            listener(group.id, entry.command, SyncGroup.Status.NotExcuted)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        listener(group.id, entry.command, SyncGroup.Status.Failed)
                    }
                }
            }
        }
    }

    private fun execute(
            command: SyncCommand
    ) {
        println("execute " + command)
        when (command) {
            is SyncCommand.Move -> move(command)
            is SyncCommand.Copy -> copy(command)
            is SyncCommand.CopyBack -> copyBack(command)
            is SyncCommand.Remove -> remove(command)
            else -> throw IllegalArgumentException("not implemented: $command")
        }
    }

    private fun copy(command: SyncCommand.Copy) {
        makeDestinationDirectory(command.dst)

        if (command.sameContent) {
            require(Files.exists(command.dst)) { "destination does not exist: ${command.dst}" }
            Files.setLastModifiedTime(command.dst, Files.getLastModifiedTime(command.src))
        } else {
            Files.copy(command.src, command.dst,
                    StandardCopyOption.COPY_ATTRIBUTES,
                    StandardCopyOption.REPLACE_EXISTING
            )
        }
        FileIO.ensureLastModifiedMatches(command.src, command.dst)
    }

    private fun copyBack(command: SyncCommand.CopyBack) {
        require(Files.exists(command.dst)) { "file to copy back does not exist: ${command.dst}" }
        require(Files.exists(command.src)) { "file to copy back to does not exist: ${command.dst}" }

        if (command.sameContent) {
            Files.setLastModifiedTime(command.src, Files.getLastModifiedTime(command.dst))
        } else {
            Files.copy(command.dst, command.src,
                    StandardCopyOption.COPY_ATTRIBUTES,
                    StandardCopyOption.REPLACE_EXISTING
            )
        }
        FileIO.ensureLastModifiedMatches(command.src, command.dst)
    }

    private fun move(command: SyncCommand.Move) {
        require(!Files.exists(command.dst)) { "destination already exist: ${command.dst}" }
        makeDestinationDirectory(command.dst)

        Files.move(command.src, command.dst,
                StandardCopyOption.ATOMIC_MOVE,
                LinkOption.NOFOLLOW_LINKS
        )
    }

    private fun remove(command: SyncCommand.Remove) {
        require(Files.exists(command.dst)) { "destination does not exist: ${command.dst}" }

        Files.delete(command.dst)
    }

    private fun makeDestinationDirectory(pathWithFilename: Path) {
        val pathOnly = pathWithFilename.parent
        if (pathOnly != null) {
            if (!Files.exists(pathOnly, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectories(pathOnly)
            }
            require(Files.isDirectory(pathOnly, LinkOption.NOFOLLOW_LINKS)) { "is not a directory: $pathOnly" }
        }
    }
}