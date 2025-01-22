package de.flapdoodle.photosync

import de.flapdoodle.io.filetree.FileTrees
import de.flapdoodle.io.filetree.diff.Sync
import de.flapdoodle.io.filetree.diff.graph.DarktableMetafile2Basename
import de.flapdoodle.io.filetree.diff.graph.GraphSync
import de.flapdoodle.io.filetree.diff.graph.HashTree
import de.flapdoodle.io.filetree.diff.graph.HashTreeDiff
import de.flapdoodle.io.filetree.diff.samelayout.SameLayoutSync
import de.flapdoodle.photosync.filehash.HashSelector
import de.flapdoodle.photosync.filehash.MonitoringHashSelector
import de.flapdoodle.photosync.filehash.SizedQuickHash
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class GraphDiffTest {

  private val basePath = (GraphDiffTest::class.java.getResource("/sample/hint.txt")?.file)?.let {
    Path(it).parent
  } ?: throw IllegalArgumentException("sample not found")

  @Test
  fun partialTestWithTestSample() {
    val source = basePath.resolve("src")
    val destination = basePath.resolve("dst")

    val src = FileTrees.walkFileTree(source)
    val dest = FileTrees.walkFileTree(destination)

    require(src != null) {"src is null"}
    require(dest != null) {"src is null"}

    val hashSelector = HashSelector.always(SizedQuickHash)

    val srcHashed = HashTree.asHashTree(src, MonitoringHashSelector(hashSelector), null)
    val destHashed = HashTree.asHashTree(dest, MonitoringHashSelector(hashSelector), null)

    val srcFiltered = HashTree.filterMetaFiles(srcHashed, DarktableMetafile2Basename)
    val destFiltered = HashTree.filterMetaFiles(destHashed, DarktableMetafile2Basename)

//    println("------------------")
//    println(srcFiltered)
//    println("------------------")
//    println(destFiltered)
//    println("------------------")
//
    val diff = HashTreeDiff.diff(srcFiltered, destFiltered)
    val actions = GraphSync(Sync.Copy.ONLY_NEW, Sync.Leftover.IGNORE)
      .actions(diff)

    println(actions)
  }
}