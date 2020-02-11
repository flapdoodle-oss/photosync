package de.flapdoodle.photosync.sync

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class SyncCommandKtTest {

  @Test
  fun `empty command list is no bulk move`() {
    val commands = listOf<SyncCommand>()
    assertThat(commands.bulkMove()).isNull()
  }

  @Test
  fun `single move is bulk move`() {
    val commands = listOf(
        SyncCommand.Move(Path.of("a", "sample.txt"), Path.of("b", "sample.txt"))
    )
    assertThat(commands.bulkMove())
        .isEqualTo(Command.BulkMove(Path.of("a"), Path.of("b"),commands))
  }

  @Test
  fun `multiple files are bulk move if src directory and destination directory matches`() {
    val commands = listOf(
        SyncCommand.Move(Path.of("a", "sample.txt"), Path.of("b", "sample.txt")),
        SyncCommand.Move(Path.of("a", "other.txt"), Path.of("b", "other.txt"))
    )
    assertThat(commands.bulkMove())
        .isEqualTo(Command.BulkMove(Path.of("a"), Path.of("b"),commands))
  }

  @Test
  fun `filename changes disable bulk moves`() {
    val commands = listOf<SyncCommand>(
        SyncCommand.Move(Path.of("a", "sample.txt"), Path.of("b", "renamed.txt")),
        SyncCommand.Move(Path.of("a", "other.txt"), Path.of("b", "other.txt"))
    )
    assertThat(commands.bulkMove()).isNull()
  }

  @Test
  fun `different destination disable bulk moves`() {
    val commands = listOf<SyncCommand>(
        SyncCommand.Move(Path.of("a", "sample.txt"), Path.of("b", "sample.txt")),
        SyncCommand.Move(Path.of("a", "other.txt"), Path.of("c", "other.txt"))
    )
    assertThat(commands.bulkMove()).isNull()
  }

  @Test
  fun `different sources disable bulk moves`() {
    val commands = listOf<SyncCommand>(
        SyncCommand.Move(Path.of("d", "sample.txt"), Path.of("b", "sample.txt")),
        SyncCommand.Move(Path.of("a", "other.txt"), Path.of("b", "other.txt"))
    )
    assertThat(commands.bulkMove()).isNull()
  }
}