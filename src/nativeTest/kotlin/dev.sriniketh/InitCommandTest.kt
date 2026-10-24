package dev.sriniketh

import com.github.ajalt.clikt.testing.test
import dev.sriniketh.ktools.BuildConfig
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InitCommandTest {

    private val home = "/home/user".toPath()
    private val claudeSkills = home / ".claude" / "skills"
    private val sharedSkills = home / ".agents" / "skills"
    private val fileSystem = FakeFileSystem()
    private val skills = listOf(
        BundledSkill("ktools-hash", mapOf("SKILL.md" to "hash skill")),
        BundledSkill("ktools-uuid", mapOf("SKILL.md" to "uuid skill")),
    )

    private fun initCommand(version: String = "v0.7.0") = InitCommand(
        fileSystem = fileSystem,
        home = { home },
        environment = { null },
        skills = { skills },
        version = version,
    )

    private fun installAgents(vararg configDirs: String) =
        configDirs.forEach { fileSystem.createDirectories(home / it) }

    private fun skillFile(skillsDir: Path, name: String) = skillsDir / name / "SKILL.md"

    @Test
    fun `init installs skills for every agent found`() {
        installAgents(".claude", ".codex", ".gemini")
        val result = initCommand().test("")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "Claude Code: ~/.claude/skills\n")
        assertContains(result.stdout, "Codex, Gemini CLI: ~/.agents/skills\n")
        assertContains(result.stdout, "install ktools-uuid")
        assertContains(result.stdout, "Restart your agents to load the changes.")
        assertTrue(fileSystem.exists(skillFile(claudeSkills, "ktools-uuid")))
        assertTrue(fileSystem.exists(skillFile(sharedSkills, "ktools-hash")))
    }

    @Test
    fun `init names the agents sharing a directory without the catch-all entry`() {
        installAgents(".codex", ".agents")
        assertContains(initCommand().test("").stdout, "Codex: ~/.agents/skills\n")
    }

    @Test
    fun `init names the catch-all entry when no other agent shares the directory`() {
        installAgents(".agents")
        assertContains(initCommand().test("").stdout, "Other agents: ~/.agents/skills\n")
    }

    @Test
    fun `init skips agents that are not installed`() {
        installAgents(".codex")
        val result = initCommand().test("")
        assertEquals(0, result.statusCode)
        assertFalse(result.stdout.contains("Claude Code"))
        assertFalse(fileSystem.exists(claudeSkills))
    }

    @Test
    fun `init fails with the agent list when no agent is found`() {
        val result = initCommand().test("")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "No supported agents found. Pick one with --agent: claude, codex, gemini")
    }

    @Test
    fun `init --agent installs for the chosen agent even when it is not found`() {
        installAgents(".codex")
        val result = initCommand().test("--agent claude")
        assertEquals(0, result.statusCode)
        assertTrue(fileSystem.exists(skillFile(claudeSkills, "ktools-uuid")))
        assertFalse(fileSystem.exists(sharedSkills))
    }

    @Test
    fun `init --agent rejects an unknown agent`() {
        val result = initCommand().test("--agent vim")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "invalid value for --agent")
    }

    @Test
    fun `init reports skills that are already up to date and does not ask for a restart`() {
        installAgents(".claude")
        initCommand().test("")
        val result = initCommand(version = "v0.8.0").test("")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "ktools-uuid is up to date")
        assertFalse(result.stdout.contains("Restart"))
    }

    @Test
    fun `init --dry-run reports changes without writing them`() {
        installAgents(".claude")
        val result = initCommand().test("--dry-run")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "would install ktools-uuid")
        assertFalse(fileSystem.exists(claudeSkills))
    }

    @Test
    fun `init skips a skill directory it did not install and exits 1`() {
        installAgents(".claude")
        fileSystem.createDirectories(claudeSkills / "ktools-uuid")
        fileSystem.write(skillFile(claudeSkills, "ktools-uuid")) { writeUtf8("my own skill") }
        val result = initCommand().test("")
        assertEquals(1, result.statusCode)
        assertContains(result.stdout, "skip ktools-uuid: not installed by ktools")
        assertContains(result.stderr, "Re-run with --force to replace them.")
        assertTrue(fileSystem.exists(skillFile(claudeSkills, "ktools-hash")))
        assertEquals("my own skill", fileSystem.read(skillFile(claudeSkills, "ktools-uuid")) { readUtf8() })
    }

    @Test
    fun `init --force replaces a skill directory it did not install`() {
        installAgents(".claude")
        fileSystem.createDirectories(claudeSkills / "ktools-uuid")
        fileSystem.write(skillFile(claudeSkills, "ktools-uuid")) { writeUtf8("my own skill") }
        val result = initCommand().test("--force")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "replace ktools-uuid")
        assertEquals("uuid skill", fileSystem.read(skillFile(claudeSkills, "ktools-uuid")) { readUtf8() })
    }

    @Test
    fun `init --uninstall removes the skills it installed`() {
        installAgents(".claude", ".codex")
        initCommand().test("")
        val result = initCommand().test("--uninstall")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "remove ktools-uuid")
        assertFalse(fileSystem.exists(claudeSkills / "ktools-uuid"))
        assertFalse(fileSystem.exists(sharedSkills / "ktools-hash"))
    }

    @Test
    fun `init --uninstall with nothing installed says so`() {
        installAgents(".claude")
        val result = initCommand().test("--uninstall")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "no ktools skills installed")
    }

    @Test
    fun `bundled skills are named after their directory and say when to use them`() {
        val bundled = parseBundledSkills(BuildConfig.BUNDLED_SKILLS_JSON)
        assertEquals(
            listOf("ktools-encoding", "ktools-hash", "ktools-prettyprint", "ktools-unixtime", "ktools-uuid"),
            bundled.map { it.name },
        )
        bundled.forEach { skill ->
            val skillMd = skill.files.getValue("SKILL.md")
            assertContains(skillMd, "\nname: ${skill.name}\n")
            assertContains(skillMd, "\ndescription: Use when ")
        }
    }
}
