package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.FileTimes
import de.flapdoodle.photosync.diff.BlobWithMeta
import de.flapdoodle.photosync.diff.DiffEntry
import de.flapdoodle.photosync.diff.GroupedBlobs
import de.flapdoodle.photosync.plus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class Diff2SyncCommandsTest {

  private val testee = Diff2SyncCommands(path("src"), path("dst"))

  @Test
  fun `noop gives no commands`() {
    val result = testee.generate(listOf(DiffEntry.Noop))

    assertThat(result).isEmpty()
  }

  @Test
  fun `new entry gives cp commands`() {
    val src = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("src", "base"), 0, FileTimes.now()),
            meta = listOf(
                Blob(path("src", "base.info"), 0, FileTimes.now())
            )
        ))
    )
    val result = testee.generate(listOf(DiffEntry.NewEntry(src)))

    assertThat(result).containsExactly(
        CommandGroup() +
            Command.Copy(path("src", "base"), path("dst", "base")) +
            Command.Copy(path("src", "base.info"), path("dst", "base.info"))
    )
  }

  @Test
  fun `removed entry gives remove commands`() {
    val dst = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("dst", "base"), 0, FileTimes.now()),
            meta = listOf(
                Blob(path("dst", "base.info"), 0, FileTimes.now())
            )
        ))
    )
    val result = testee.generate(listOf(DiffEntry.DeletedEntry(dst)))

    assertThat(result).containsExactly(
        CommandGroup() +
            Command.Remove(path("dst", "base")) +
            Command.Remove(path("dst", "base.info"))
    )
  }

  @Test
  fun `match gives cp commands for meta files`() {
    val dstTimeStamp = FileTimes.now()
    val sourceTimeStamp = dstTimeStamp + 1

    val src = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("src", "base"), 0, sourceTimeStamp),
            meta = listOf(
                Blob(path("src", "base.info"), 0, sourceTimeStamp)
            )
        ))
    )

    val dst = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("dst", "base"), 0, dstTimeStamp),
            meta = listOf(
                Blob(path("dst", "base.info"), 0, dstTimeStamp)
            )
        ))
    )
    val result = testee.generate(listOf(DiffEntry.Match(src, dst)))

    assertThat(result).containsExactly(
        CommandGroup() +
            Command.Copy(path("src", "base.info"), path("dst", "base.info"))
    )
  }

  @Test
  fun `match gives cp commands for new entries and for meta files`() {
    val dstTimeStamp = FileTimes.now()
    val sourceTimeStamp = dstTimeStamp + 1

    val src = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("src", "base"), 0, sourceTimeStamp),
            meta = listOf(
                Blob(path("src", "base.info"), 0, sourceTimeStamp)
            )),
            BlobWithMeta(
                base = Blob(path("src", "second"), 0, sourceTimeStamp),
                meta = listOf(
                    Blob(path("src", "second.info"), 0, sourceTimeStamp)
                ))
        )
    )

    val dst = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("dst", "base"), 0, dstTimeStamp),
            meta = listOf(
                Blob(path("dst", "base.info"), 0, dstTimeStamp)
            )
        ))
    )
    val result = testee.generate(listOf(DiffEntry.Match(src, dst)))

    assertThat(result).containsExactly(
        CommandGroup() +
            Command.Copy(path("src", "base.info"), path("dst", "base.info")),
        CommandGroup() +
            Command.Copy(path("src", "second"), path("dst", "second")) +
            Command.Copy(path("src", "second.info"), path("dst", "second.info"))
    )
  }

  @Test
  fun `match gives cp commands for meta files and remove commands for deleted entries`() {
    val dstTimeStamp = FileTimes.now()
    val sourceTimeStamp = dstTimeStamp + 1

    val src = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("src", "base"), 0, sourceTimeStamp),
            meta = listOf(
                Blob(path("src", "base.info"), 0, sourceTimeStamp)
            ))
        )
    )

    val dst = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("dst", "base"), 0, dstTimeStamp),
            meta = listOf(
                Blob(path("dst", "base.info"), 0, dstTimeStamp)
            )),
            BlobWithMeta(
                base = Blob(path("dst", "second"), 0, dstTimeStamp),
                meta = listOf(
                    Blob(path("dst", "second.info"), 0, dstTimeStamp)
                ))
        )
    )
    val result = testee.generate(listOf(DiffEntry.Match(src, dst)))

    assertThat(result).containsExactly(
        CommandGroup() +
            Command.Copy(path("src", "base.info"), path("dst", "base.info")),
        CommandGroup() +
            Command.Remove(path("dst", "second")) +
            Command.Remove(path("dst", "second.info"))
    )
  }

  @Test
  fun `match gives move commands for moved dest and cp commands for meta files`() {
    val dstTimeStamp = FileTimes.now()
    val sourceTimeStamp = dstTimeStamp + 1

    val src = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("src", "base"), 0, sourceTimeStamp),
            meta = listOf(
                Blob(path("src", "base.info"), 0, sourceTimeStamp)
            ))
        )
    )

    val dst = GroupedBlobs(
        listOf(BlobWithMeta(
            base = Blob(path("dst", "moved-base"), 0, dstTimeStamp),
            meta = listOf(
                Blob(path("dst", "moved-base.info"), 0, dstTimeStamp)
            )
        ))
    )
    val result = testee.generate(listOf(DiffEntry.Match(src, dst)))

    assertThat(result).containsExactly(
        CommandGroup() +
            Command.Move(path("dst", "moved-base"), path("dst", "base")) +
            Command.Move(path("dst", "moved-base.info"), path("dst", "base.info")) +
            Command.Copy(path("src", "base.info"), path("dst", "base.info"))
    )
  }


  private fun path(firstPart: String, vararg parts: String): Path {
    return Path.of(firstPart, *parts)
  }
}