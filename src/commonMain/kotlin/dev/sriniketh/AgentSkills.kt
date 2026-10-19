package dev.sriniketh

import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.IOException
import okio.Path

/**
 * File written into every skill directory ktools installs, holding the ktools version that installed it.
 *
 * Its presence is what marks a skill directory as ktools' to update or remove.
 */
internal const val SKILL_MARKER_FILE_NAME = ".ktools-skill"

/**
 * A skill bundled into the ktools binary.
 *
 * @property[name] Skill name, which is also its directory name, e.g. `ktools-uuid`.
 * @property[files] File contents by path relative to the skill directory, e.g. `SKILL.md`.
 */
internal data class BundledSkill(val name: String, val files: Map<String, String>)

/**
 * Parses the skills bundled into the binary at build time.
 *
 * @param[json] JSON object of file contents keyed by path relative to the skills directory, e.g.
 * `ktools-uuid/SKILL.md`.
 * @return Bundled skills sorted by name.
 */
internal fun parseBundledSkills(json: String): List<BundledSkill> =
    Json.decodeFromString<Map<String, String>>(json).entries
        .groupBy({ it.key.substringBefore('/') }, { it.key.substringAfter('/') to it.value })
        .map { (name, files) -> BundledSkill(name, files.toMap()) }
        .sortedBy { it.name }

/**
 * A change to one skill directory in a [SkillTarget].
 */
internal sealed interface SkillChange {
    val name: String

    /** The skill is not installed yet. */
    data class Install(override val name: String) : SkillChange

    /** ktools installed the skill before, from [fromVersion], and its files differ from the bundled ones. */
    data class Update(override val name: String, val fromVersion: String) : SkillChange

    /** The installed skill already matches the bundled one. */
    data class UpToDate(override val name: String) : SkillChange

    /** A directory with the skill's name exists that ktools did not install; it is replaced only when forced. */
    data class Conflict(override val name: String, val replace: Boolean) : SkillChange

    /** ktools installed the skill before and it is no longer bundled, or is being uninstalled. */
    data class Remove(override val name: String) : SkillChange
}

/**
 * Works out what installing [skills] into [skillsDir] would change, without writing anything.
 *
 * @param[fileSystem] File system holding [skillsDir].
 * @param[skillsDir] Skills directory to install into.
 * @param[skills] Skills bundled into the running ktools.
 * @param[force] Replace skill directories that ktools did not install.
 * @return One change per bundled skill, then one [SkillChange.Remove] per ktools-installed skill that is no
 * longer bundled.
 */
@Throws(IOException::class)
internal fun planSkillInstall(
    fileSystem: FileSystem,
    skillsDir: Path,
    skills: List<BundledSkill>,
    force: Boolean,
): List<SkillChange> {
    val bundledNames = skills.map { it.name }.toSet()
    val changes = skills.map { skill ->
        val dir = skillsDir / skill.name
        val installedVersion = installedSkillVersion(fileSystem, dir)
        when {
            fileSystem.metadataOrNull(dir) == null -> SkillChange.Install(skill.name)
            installedVersion == null -> SkillChange.Conflict(skill.name, replace = force)
            matchesBundle(fileSystem, dir, skill) -> SkillChange.UpToDate(skill.name)
            else -> SkillChange.Update(skill.name, installedVersion)
        }
    }
    val stale = installedSkillNames(fileSystem, skillsDir).filterNot { it in bundledNames }
    return changes + stale.map { SkillChange.Remove(it) }
}

/**
 * Works out what removing every ktools-installed skill from [skillsDir] would change, without writing anything.
 *
 * @param[fileSystem] File system holding [skillsDir].
 * @param[skillsDir] Skills directory to remove skills from.
 * @return One [SkillChange.Remove] per skill directory ktools installed.
 */
@Throws(IOException::class)
internal fun planSkillUninstall(fileSystem: FileSystem, skillsDir: Path): List<SkillChange> =
    installedSkillNames(fileSystem, skillsDir).map { SkillChange.Remove(it) }

/**
 * Applies [changes] planned for [skillsDir].
 *
 * Installed skill directories are rewritten from scratch, so files dropped from a skill do not linger.
 *
 * @param[fileSystem] File system holding [skillsDir].
 * @param[skillsDir] Skills directory to change.
 * @param[skills] Skills bundled into the running ktools.
 * @param[changes] Changes from [planSkillInstall] or [planSkillUninstall].
 * @param[version] Version of the running ktools, recorded in each installed skill's marker file.
 */
@Throws(IOException::class)
internal fun applySkillChanges(
    fileSystem: FileSystem,
    skillsDir: Path,
    skills: List<BundledSkill>,
    changes: List<SkillChange>,
    version: String,
) {
    val skillsByName = skills.associateBy { it.name }
    for (change in changes) {
        val dir = skillsDir / change.name
        val write = when (change) {
            is SkillChange.Install, is SkillChange.Update -> true
            is SkillChange.Conflict -> change.replace
            is SkillChange.Remove, is SkillChange.UpToDate -> false
        }
        if (write) writeSkill(fileSystem, dir, skillsByName.getValue(change.name), version)
        if (change is SkillChange.Remove) fileSystem.deleteRecursively(dir)
    }
}

/**
 * Whether [skillsDir] holds at least one skill installed by ktools.
 *
 * @param[fileSystem] File system holding [skillsDir].
 * @param[skillsDir] Skills directory to look in.
 */
@Throws(IOException::class)
internal fun hasInstalledSkills(fileSystem: FileSystem, skillsDir: Path): Boolean =
    installedSkillNames(fileSystem, skillsDir).isNotEmpty()

private fun installedSkillVersion(fileSystem: FileSystem, dir: Path): String? {
    val marker = dir / SKILL_MARKER_FILE_NAME
    if (fileSystem.metadataOrNull(marker)?.isRegularFile != true) return null
    return fileSystem.read(marker) { readUtf8() }.trim()
}

private fun installedSkillNames(fileSystem: FileSystem, skillsDir: Path): List<String> =
    fileSystem.listOrNull(skillsDir).orEmpty()
        .filter { installedSkillVersion(fileSystem, it) != null }
        .map { it.name }
        .sorted()

private fun matchesBundle(fileSystem: FileSystem, dir: Path, skill: BundledSkill): Boolean {
    val installed = fileSystem.listRecursively(dir)
        .filter { fileSystem.metadataOrNull(it)?.isRegularFile == true }
        .map { it.relativeTo(dir).segments.joinToString("/") }
        .filter { it != SKILL_MARKER_FILE_NAME }
        .toSet()
    return installed == skill.files.keys &&
        skill.files.all { (path, content) -> fileSystem.read(dir / path) { readUtf8() } == content }
}

private fun writeSkill(fileSystem: FileSystem, dir: Path, skill: BundledSkill, version: String) {
    fileSystem.deleteRecursively(dir)
    for ((path, content) in skill.files) {
        val file = dir / path
        file.parent?.let { fileSystem.createDirectories(it) }
        fileSystem.write(file) { writeUtf8(content) }
    }
    fileSystem.write(dir / SKILL_MARKER_FILE_NAME) { writeUtf8("$version\n") }
}
