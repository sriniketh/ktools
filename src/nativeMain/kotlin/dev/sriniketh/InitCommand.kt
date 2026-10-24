package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import dev.sriniketh.ktools.BuildConfig
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

internal class InitCommand(
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val home: () -> Path = ::homeDirectory,
    private val environment: (String) -> String? = ::environmentVariable,
    private val skills: () -> List<BundledSkill> = { parseBundledSkills(BuildConfig.BUNDLED_SKILLS_JSON) },
    private val version: String = BuildConfig.VERSION,
) : CliktCommand(name = "init") {

    private val agentIds by option(
        "--agent",
        help = "Agent to install skills for, instead of every agent found. Repeat for more than one.",
    ).choice(AGENT_IDS.associateBy { it }).multiple()
    private val uninstall by option("--uninstall", help = "Remove the skills ktools installed").flag()
    private val force by option("--force", help = "Replace skill directories that ktools did not install").flag()
    private val dryRun by option("--dry-run", help = "Show what would change without writing anything").flag()

    override fun help(context: Context): String = "Install the ktools skills into your AI coding agents"

    override fun run() {
        try {
            init()
        } catch (expectedException: IOException) {
            throw PrintMessage("IOException: ${expectedException.message}", statusCode = 1, printError = true)
        }
    }

    private fun init() {
        val home = home()
        val skills = skills()
        val agents = knownSkillAgents(home, environment)
        val chosen = if (agentIds.isEmpty()) {
            agents.filter { fileSystem.exists(it.configDir) }
        } else {
            agents.filter { it.id in agentIds }
        }
        if (chosen.isEmpty()) {
            throw PrintMessage(
                "No supported agents found. Pick one with --agent: ${AGENT_IDS.joinToString(", ")}",
                statusCode = 1,
                printError = true,
            )
        }

        var changed = false
        var conflicts = false
        for (target in skillTargets(fileSystem, chosen)) {
            val changes = if (uninstall) {
                planSkillUninstall(fileSystem, target.skillsDir)
            } else {
                planSkillInstall(fileSystem, target.skillsDir, skills, force)
            }
            report(target, changes, home)
            if (!dryRun) applySkillChanges(fileSystem, target.skillsDir, skills, changes, version)
            changed = changed || changes.any { it.writes() }
            conflicts = conflicts || changes.any { it is SkillChange.Conflict && !it.replace }
        }

        if (changed && !dryRun) echo("Restart your agents to load the changes.")
        if (conflicts) {
            throw PrintMessage(
                "Some skills were skipped because a directory with the same name exists. " +
                    "Re-run with --force to replace them.",
                statusCode = 1,
                printError = true,
            )
        }
    }

    private fun report(target: SkillTarget, changes: List<SkillChange>, home: Path) {
        val named = target.agents.filter { it.id != SHARED_SKILLS_AGENT_ID }.ifEmpty { target.agents }
        echo("${named.joinToString(", ") { it.displayName }}: ${target.skillsDir.shortenedUnder(home)}")
        if (changes.isEmpty()) echo("  no ktools skills installed")
        changes.forEach { echo("  ${describe(it)}") }
    }

    private fun describe(change: SkillChange): String {
        val would = if (dryRun) "would " else ""
        return when (change) {
            is SkillChange.Install -> "${would}install ${change.name}"
            is SkillChange.Update -> "${would}update ${change.name} (installed by ktools ${change.fromVersion})"
            is SkillChange.UpToDate -> "${change.name} is up to date"
            is SkillChange.Conflict if change.replace -> "${would}replace ${change.name}"
            is SkillChange.Conflict -> "skip ${change.name}: not installed by ktools"
            is SkillChange.Remove -> "${would}remove ${change.name}"
        }
    }

    private fun Path.shortenedUnder(home: Path): String {
        val relative = if (isAbsolute == home.isAbsolute && root == home.root) relativeTo(home) else null
        return if (relative == null || relative.segments.firstOrNull() == "..") {
            toString()
        } else {
            ("~".toPath() / relative).toString()
        }
    }

    private fun SkillChange.writes(): Boolean = when (this) {
        is SkillChange.Install, is SkillChange.Update, is SkillChange.Remove -> true
        is SkillChange.Conflict -> replace
        is SkillChange.UpToDate -> false
    }

    private companion object {
        val AGENT_IDS = knownSkillAgents("/".toPath()) { null }.map { it.id }
    }
}

/**
 * Whether any agent's skills directory holds skills installed by `ktools init`.
 *
 * Returns false when the home directory or a skills directory cannot be read.
 */
internal fun ktoolsSkillsInstalled(): Boolean =
    try {
        knownSkillAgents(homeDirectory(), ::environmentVariable)
            .map { it.skillsDir }
            .distinct()
            .any { hasInstalledSkills(FileSystem.SYSTEM, it) }
    } catch (_: IOException) {
        false
    }
