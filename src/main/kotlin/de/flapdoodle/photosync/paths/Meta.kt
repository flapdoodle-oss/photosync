package de.flapdoodle.photosync.paths

import java.nio.file.Path

@Deprecated("don't use it")
object Meta {

  fun isMeta(path: Path, basePath: Path): Boolean {
    return PathMatcher.sameParent
        .andThen(MetaFileName)
//        .andThen(PathMatcher.notSameFileName)
        .match(path, basePath)
  }

  fun groupByBasePath(paths: Collection<Path>): Map<Path, List<Path>> {
    val baseFiles = paths.filter { thisPath -> paths.none { isMeta(thisPath, it) } }
    val metaFiles = paths.filter { !baseFiles.contains(it) }

    return baseFiles.map {
      it to emptyList<Path>()
    }.toMap() + metaFiles.groupBy { thisPath ->
      baseFiles.find { Meta.isMeta(thisPath, it) }
              ?: throw IllegalArgumentException("basePath not found: $thisPath - $paths")
    }
  }

  fun replaceBase(path: Path, base: Path, newBase: Path): Path {
    require(isMeta(path, base)) { "$this must start with $base" }

    val fileName = path.fileName.toString()
    val baseFileName = base.fileName.toString()
    val append = fileName.substring(baseFileName.length)

    return newBase.parent.resolve(newBase.fileName.toString() + append)
  }

  object MetaFileName : PathMatcher {
    override fun match(first: Path, second: Path): Boolean {
      return Filename.parse(first.fileName.toString()).isMeta(Filename.parse(second.fileName.toString()))
    }
  }

  data class Filename(
      val name: String,
      val extension: String?
  ) {
    init {
      require(!name.contains('.')) { "name part contains .: $name" }
    }

    fun isMeta(base: Filename): Boolean {
      return if (this != base && extension != null) {
        return if (base.extension == null || extension.startsWith(base.extension)) {
          nameMatchesBaseNameWithOptionalCounter(name, base.name)
        } else {
          false
        }
      } else {
        false
      }
    }

    companion object {
      private val counterPattern = "(_[0-9][0-9])".toRegex()

      private fun nameMatchesBaseNameWithOptionalCounter(name: String, baseName: String): Boolean {
        return when {
          name == baseName -> true
          name.startsWith(baseName) -> {
            counterPattern.matches(name.substring(baseName.length))
          }
          else -> false
        }
      }

      fun parse(filename: String): Filename {
        val idx = filename.indexOf('.')
        return if (idx!=-1) {
          Filename(filename.substring(0,idx), filename.substring(idx+1))
        } else {
          Filename(filename, null)
        }
      }
    }
  }
}