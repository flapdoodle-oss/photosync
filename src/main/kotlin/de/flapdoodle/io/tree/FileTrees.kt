package de.flapdoodle.io.tree

import java.nio.file.Files
import java.nio.file.Path

object FileTrees {
    fun asTree(
            path: Path,
            filter: (Path) -> Boolean = { true },
            listener: (FileTreeEvent) -> Unit = {},
            checkAbort: () -> Unit = {}
    ): Tree.Directory {
        val collector = TreeCollector()

        Files.walkFileTree(path, Visitor2EventAdapter(collector.withFilter(filter)
                .andThen { event ->
                    checkAbort()
                    listener(event)
                    OnFileTreeEvent.Action.Continue
                }))

        val rootDirectory = collector.rootDirectory()

        return requireNotNull(rootDirectory) { "rootDirectory is null for $path"}
    }
}