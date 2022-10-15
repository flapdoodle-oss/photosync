package de.flapdoodle.photosync.paths.meta

import de.flapdoodle.photosync.paths.expectParent
import java.nio.file.Path
import java.util.regex.Pattern

object DarktableRule : MetafileRule {
    // base file -> XYZ.ext --> XYZ_01.ext.fooBar
    private val BASE_PATTERN = Pattern.compile("(?<name>[A-Za-z0-9]+)\\.(?<ext>[A-Za-z0-9]+)");
    private val META_PATTERN = Pattern.compile("(?<name>[A-Za-z0-9]+)(?<version>[_0-9]*)\\.(?<ext>[A-Za-z0-9.]+)");

    override fun matcher(path: Path): Matcher? {
        val fileName = fileName(path)
        val matcher = BASE_PATTERN.matcher(fileName)
        return if (matcher.matches()) {
            Matcher(path, matcher.group("name"), matcher.group("ext"))
        } else null
    }

    private fun fileName(path: Path): String = path.fileName.toString()

    class Matcher(val basePath: Path, val name: String, val extension: String) : MetafileMatcher {
        override fun matches(paths: List<Path>): List<Path> {
            return paths.filter { it.parent == basePath.parent }
                .filter { isMetaButNotBase(it) }
        }

        private fun isMetaButNotBase(path: Path): Boolean {
            val fileName = fileName(path)
            return META_PATTERN.matcher(fileName).matches() && !BASE_PATTERN.matcher(fileName).matches()
        }

        override fun rename(expectedBase: Path): MetafileRename {
            val expectedFileName = fileName(expectedBase)
            val matcher = BASE_PATTERN.matcher(expectedFileName)
            require(
                matcher.matches()
            ) { "expected path is not a basePath: $expectedBase" }

            val newName = matcher.group("name")
            val newExtension = matcher.group("ext")

            require(extension == newExtension) { "new extension does not match: $extension != $newExtension" }

            return Rename(this, expectedBase.expectParent(), newName)
        }
    }

    class Rename(val matcher: Matcher,val expectedParent: Path, val newName: String) : MetafileRename {
        override fun rename(path: Path): Path {
            val currentFileName = fileName(path)
            require(!BASE_PATTERN.matcher(currentFileName).matches()) {"is baseFile: $path"}

            val metaMatcher = META_PATTERN.matcher(fileName(path))
            require(metaMatcher.matches()) {"is not meta file: $path"}

            val metaFileName = metaMatcher.group("name")
            require(metaFileName == matcher.name) {"meta file base name is different: ${matcher.name} != $metaFileName"}

            return expectedParent.resolve(newName + currentFileName.substring(matcher.name.length))

        }

    }
}