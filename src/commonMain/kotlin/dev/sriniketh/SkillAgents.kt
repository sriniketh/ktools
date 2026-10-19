package dev.sriniketh

import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * Id of the catch-all agent for any other tool that reads the shared `~/.agents/skills` directory.
 */
internal const val SHARED_SKILLS_AGENT_ID = "agents"

/**
 * An AI coding agent that loads skills from a user-level directory.
 *
 * @property[id] Name used to pick the agent on the command line, e.g. `claude`.
 * @property[displayName] Human-readable name, e.g. `Claude Code`.
 * @property[configDir] Directory whose existence shows the agent is installed.
 * @property[skillsDir] Directory the agent loads user-level skills from.
 */
internal data class SkillAgent(val id: String, val displayName: String, val configDir: Path, val skillsDir: Path)

/**
 * Agents ktools can install skills for, in the order they are reported.
 *
 * Codex, Gemini CLI, GitHub Copilot, Cursor and OpenCode all read the shared `~/.agents/skills` directory, so they
 * share one copy of the skills. Claude Code only reads its own directory.
 *
 * @param[home] User's home directory.
 * @param[environment] Looks up an environment variable, returning null when it is unset.
 * @return Known agents.
 */
internal fun knownSkillAgents(home: Path, environment: (String) -> String?): List<SkillAgent> {
    val sharedSkillsDir = home / ".agents" / "skills"
    val claudeDir = environment("CLAUDE_CONFIG_DIR")?.toPath() ?: (home / ".claude")
    val configHome = environment("XDG_CONFIG_HOME")?.toPath() ?: (home / ".config")
    return listOf(
        SkillAgent("claude", "Claude Code", claudeDir, claudeDir / "skills"),
        SkillAgent("codex", "Codex", environment("CODEX_HOME")?.toPath() ?: (home / ".codex"), sharedSkillsDir),
        SkillAgent("gemini", "Gemini CLI", home / ".gemini", sharedSkillsDir),
        SkillAgent("copilot", "GitHub Copilot", home / ".copilot", sharedSkillsDir),
        SkillAgent("cursor", "Cursor", home / ".cursor", sharedSkillsDir),
        SkillAgent("opencode", "OpenCode", configHome / "opencode", sharedSkillsDir),
        SkillAgent(SHARED_SKILLS_AGENT_ID, "Other agents", home / ".agents", sharedSkillsDir),
    )
}

/**
 * A skills directory together with the agents that read it.
 *
 * @property[skillsDir] Skills directory, resolved through symlinks when it exists.
 * @property[agents] Agents that load skills from [skillsDir].
 */
internal data class SkillTarget(val skillsDir: Path, val agents: List<SkillAgent>)

/**
 * Groups [agents] by the directory they load skills from, so each directory is written once.
 *
 * Directories are compared after resolving symlinks, so `~/.claude/skills` linked to `~/.agents/skills` is one
 * target.
 *
 * @param[fileSystem] File system holding the skills directories.
 * @param[agents] Agents to install skills for.
 * @return One target per distinct skills directory, in the order of [agents].
 */
@Throws(IOException::class)
internal fun skillTargets(fileSystem: FileSystem, agents: List<SkillAgent>): List<SkillTarget> =
    agents
        .groupBy { if (fileSystem.exists(it.skillsDir)) fileSystem.canonicalize(it.skillsDir) else it.skillsDir }
        .map { (skillsDir, agentsForDir) -> SkillTarget(skillsDir, agentsForDir) }
