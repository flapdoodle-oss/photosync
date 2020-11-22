package de.flapdoodle.io.tree

import java.nio.file.Path

class TreeCollector : OnFileTreeEvent {
    private var collector: Collector = Collector.Root()

    override fun onEvent(event: FileTreeEvent): OnFileTreeEvent.Action {
        collector = collector.onEvent(event)
        return OnFileTreeEvent.Action.Continue
    }

    public fun rootDirectory(): Tree.Directory? {
        require(collector is Collector.Root) { "unexpected collector: $collector"}
        return (collector as Collector.Root).rootDirectory()
    }

    private sealed class Collector() {
        abstract fun onEvent(event: FileTreeEvent): Collector
        abstract fun leaveWith(tree: Tree): Collector

        class Root : Collector() {
            private var root: Tree.Directory? = null;

            override fun onEvent(event: FileTreeEvent): Collector {
                require(root==null) {"root already set to $root"}
                
                return when (event) {
                    is FileTreeEvent.Enter -> Dir(this, event.path)
                    else -> throw IllegalArgumentException("not expected: $event")
                }
            }

            override fun leaveWith(tree: Tree): Collector {
                require(tree is Tree.Directory) {"unexcpected: $tree"}
                root=tree
                return this;
            }

            fun rootDirectory(): Tree.Directory? {
                return root
            }
        }

        class Dir(val parent: Collector, path: Path) : Collector() {
            private var directory = Tree.Directory(path, emptyList());

            override fun onEvent(event: FileTreeEvent): Collector {
                return when (event) {
                    is FileTreeEvent.Enter -> Dir(this, event.path)
                    is FileTreeEvent.Leave -> parent.leaveWith(directory)
                    is FileTreeEvent.File -> {
                        directory = directory.copy(children = directory.children + Tree.File(event.path, event.size, event.lastModified))
                        this
                    }
                    is FileTreeEvent.SymLink -> {
                        directory = directory.copy(children = directory.children + Tree.SymLink(event.path, event.destination, event.lastModified))
                        this
                    }
                }
            }

            override fun leaveWith(tree: Tree): Collector {
                directory = directory.copy(children = directory.children + tree)
                return this
            }
        }

    }


}