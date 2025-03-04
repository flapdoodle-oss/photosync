package de.flapdoodle.io.filetree.diff.graph

import de.flapdoodle.io.filetree.diff.Action
import de.flapdoodle.io.filetree.diff.Sync
import de.flapdoodle.io.filetree.diff.samelayout.SameLayoutSync

class GraphSync(copy: Sync.Copy, leftover: Sync.Leftover) {
  fun actions(diff: HashTreeDiff): List<Action> {
    val src = diff.src
    val dest = diff.dest


    return emptyList()
  }
}