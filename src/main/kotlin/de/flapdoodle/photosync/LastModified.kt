package de.flapdoodle.photosync

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.io.path.isSymbolicLink

data class LastModified(
    val value: ZonedDateTime
) : Comparable<LastModified> {

    override fun compareTo(other: LastModified): Int {
        return value.compareTo(other.value)
    }

    operator fun plus(offsetInSeconds: Int): LastModified {
        return LastModified(value.plus(offsetInSeconds.toLong(), ChronoUnit.SECONDS))
    }

    operator fun minus(offsetInSeconds: Int): LastModified {
        return LastModified(value.minus(offsetInSeconds.toLong(), ChronoUnit.SECONDS))
    }

    fun epochSecond(): Long {
        return value.toEpochSecond()
    }

    fun asHumanReadable(): String {
        return Companion.toString(this)
    }

    companion object {
        val customFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

        fun from(path: Path): LastModified {
            return from(
                Files.getLastModifiedTime(
                    path,
                    LinkOption.NOFOLLOW_LINKS
                )
            )
        }

        fun to(path: Path, lastModified: LastModified) {
            // this code fails on symlinks
            // Files.getFileAttributeView(path, BasicFileAttributeView::class.java, LinkOption.NOFOLLOW_LINKS)
            //   .setTimes(fileTime, null, null)

            require(!path.isSymbolicLink()) { "not supported on symlinks: see https://stackoverflow.com/questions/17308363/symlink-lastmodifiedtime-in-java-1-7" }
            Files.setLastModifiedTime(path, asFileTime(lastModified))
        }

        fun from(fileTime: FileTime): LastModified {
            val instant = ZonedDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault())
            return LastModified(instant.truncatedTo(ChronoUnit.SECONDS))
        }

        fun now(): LastModified {
            return LastModified(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS))
        }

        fun asFileTime(lastModified: LastModified): FileTime {
            return FileTime.from(lastModified.value.toInstant())
        }

        fun toString(lastModified: LastModified): String {
            return customFormatter.format(lastModified.value)
        }

        fun fromString(lastModifiedAsString: String): LastModified {
            return LastModified(ZonedDateTime.parse(lastModifiedAsString, customFormatter))
        }
    }
}