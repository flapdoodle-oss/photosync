package de.flapdoodle.types

import de.flapdoodle.photosync.sync.SyncCommand
import de.flapdoodle.photosync.ui.sync.SyncGroup
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class GroupedTest : WithAssertions {

    @Test
    fun sample() {
        val first = SyncGroup.SyncEntry(
                SyncCommand.Copy(src = Path.of(""), dst = Path.of("")),
                SyncGroup.Status.NotExcuted
        )
        val second = SyncGroup.SyncEntry(
                SyncCommand.Copy(src = Path.of("a"), dst = Path.of("b")),
                SyncGroup.Status.Successful
        )
        val third = SyncGroup.SyncEntry(
                SyncCommand.Copy(src = Path.of("b"), dst = Path.of("b")),
                SyncGroup.Status.Successful
        )

        val firstGroup = SyncGroup(commands = listOf(first, second))
        val secondGroup = SyncGroup(commands = listOf(third))

        val list = listOf(firstGroup, secondGroup)

        val result = Grouped.flatMapGrouped(list) { it -> it to it.commands };

        assertThat(result).size().isEqualTo(5)
        assertThat(result[0]).isEqualTo(Grouped.Parent<SyncGroup,SyncGroup.SyncEntry>(firstGroup))
        assertThat(result[1]).isEqualTo(Grouped.Child<SyncGroup,SyncGroup.SyncEntry>(firstGroup, first))
        assertThat(result[2]).isEqualTo(Grouped.Child<SyncGroup,SyncGroup.SyncEntry>(firstGroup, second))
        assertThat(result[3]).isEqualTo(Grouped.Parent<SyncGroup,SyncGroup.SyncEntry>(secondGroup))
        assertThat(result[4]).isEqualTo(Grouped.Child<SyncGroup,SyncGroup.SyncEntry>(secondGroup, third))
    }
}