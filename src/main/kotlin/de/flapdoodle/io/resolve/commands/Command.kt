package de.flapdoodle.io.resolve.commands

import java.nio.file.Path

sealed class Command(
    open val intention: Intention,
    open val precondition: Precondition
) {
    data class Manual(val source: Path, val destination: Path, val cause: String, override val intention: Intention) :
        Command(intention, Precondition.AlwaysOk)

    data class Copy(
        val source: Path,
        val destination: Path,
        override val intention: Intention,
        override val precondition: Precondition = Precondition.AlwaysOk
    ) : Command(intention, precondition)

    data class CopyTimestamp(
        val source: Path,
        val destination: Path,
        override val intention: Intention,
        override val precondition: Precondition = Precondition.AlwaysOk
    ) : Command(intention, precondition)

    data class Move(
        val source: Path,
        val destination: Path,
        override val intention: Intention,
        override val precondition: Precondition = Precondition.AlwaysOk
    ) : Command(intention, precondition)
}
