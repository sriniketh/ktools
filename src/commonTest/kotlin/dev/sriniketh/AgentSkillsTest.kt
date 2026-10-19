package dev.sriniketh

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgentSkillsTest {

    private val home = "/home/user".toPath()
    private val skillsDir = home / ".agents" / "skills"
    private val fileSystem = FakeFileSystem().apply { createDirectories(skillsDir) }
    private val uuidSkill = BundledSkill("ktools-uuid", mapOf("SKILL.md" to "uuid skill"))
    private val hashSkill = BundledSkill(
        "ktools-hash",
        mapOf("SKILL.md" to "hash skill", "references/algorithms.md" to "algorithms"),
    )
    private val skills = listOf(hashSkill, uuidSkill)

    private fun read(path: String) = fileSystem.read(skillsDir / path) { readUtf8() }

    private fun install(skills: List<BundledSkill> = this.skills, version: String = "v0.7.0", force: Boolean = false) =
        planSkillInstall(fileSystem, skillsDir, skills, force).also {
            applySkillChanges(fileSystem, skillsDir, skills, it, version)
        }

    @Test
    fun `parseBundledSkills groups files by skill directory`() {
        val json = """
            {
              "ktools-uuid/SKILL.md": "uuid skill",
              "ktools-hash/SKILL.md": "hash skill",
              "ktools-hash/references/algorithms.md": "algorithms"
            }
        """.trimIndent()
        assertEquals(skills, parseBundledSkills(json))
    }

    @Test
    fun `knownSkillAgents shares the agents skills directory and respects config overrides`() {
        val environment = mapOf("CLAUDE_CONFIG_DIR" to "/custom/claude", "CODEX_HOME" to "/custom/codex")
        val agents = knownSkillAgents(home, environment::get).associateBy { it.id }
        assertEquals("/custom/claude/skills".toPath(), agents.getValue("claude").skillsDir)
        assertEquals("/custom/codex".toPath(), agents.getValue("codex").configDir)
        listOf("codex", "gemini", "copilot", "cursor", "opencode", "agents").forEach {
            assertEquals(skillsDir, agents.getValue(it).skillsDir)
        }
        assertEquals(home / ".config" / "opencode", agents.getValue("opencode").configDir)
    }

    @Test
    fun `skillTargets writes a shared directory once for every agent that reads it`() {
        val agents = knownSkillAgents(home) { null }.filter { it.id in setOf("claude", "codex", "gemini") }
        val targets = skillTargets(fileSystem, agents)
        assertEquals(listOf(home / ".claude" / "skills", skillsDir), targets.map { it.skillsDir })
        assertEquals(listOf("codex", "gemini"), targets[1].agents.map { it.id })
    }

    @Test
    fun `skillTargets treats a skills directory linked to another as the same target`() {
        fileSystem.allowSymlinks = true
        fileSystem.createDirectories(home / ".claude")
        fileSystem.createSymlink(home / ".claude" / "skills", skillsDir)
        val agents = knownSkillAgents(home) { null }.filter { it.id in setOf("claude", "codex") }
        val targets = skillTargets(fileSystem, agents)
        assertEquals(listOf(skillsDir), targets.map { it.skillsDir })
        assertEquals(listOf("claude", "codex"), targets.single().agents.map { it.id })
    }

    @Test
    fun `install writes every skill file and a marker with the ktools version`() {
        val changes = install()
        assertEquals(listOf(SkillChange.Install("ktools-hash"), SkillChange.Install("ktools-uuid")), changes)
        assertEquals("uuid skill", read("ktools-uuid/SKILL.md"))
        assertEquals("algorithms", read("ktools-hash/references/algorithms.md"))
        assertEquals("v0.7.0\n", read("ktools-uuid/$SKILL_MARKER_FILE_NAME"))
    }

    @Test
    fun `install creates the skills directory when it does not exist`() {
        fileSystem.deleteRecursively(skillsDir)
        install()
        assertEquals("uuid skill", read("ktools-uuid/SKILL.md"))
    }

    @Test
    fun `install again with the same skills reports them up to date`() {
        install()
        val changes = install(version = "v0.8.0")
        assertEquals(listOf(SkillChange.UpToDate("ktools-hash"), SkillChange.UpToDate("ktools-uuid")), changes)
        assertEquals("v0.7.0\n", read("ktools-uuid/$SKILL_MARKER_FILE_NAME"))
    }

    @Test
    fun `install updates changed skills and drops files no longer bundled`() {
        install()
        val newHash = BundledSkill("ktools-hash", mapOf("SKILL.md" to "new hash skill"))
        val changes = install(skills = listOf(newHash, uuidSkill), version = "v0.8.0")
        assertEquals(SkillChange.Update("ktools-hash", "v0.7.0"), changes.first())
        assertEquals("new hash skill", read("ktools-hash/SKILL.md"))
        assertFalse(fileSystem.exists(skillsDir / "ktools-hash" / "references"))
        assertEquals("v0.8.0\n", read("ktools-hash/$SKILL_MARKER_FILE_NAME"))
    }

    @Test
    fun `install updates a skill whose files were edited by hand`() {
        install()
        fileSystem.write(skillsDir / "ktools-uuid" / "SKILL.md") { writeUtf8("edited") }
        assertEquals(SkillChange.Update("ktools-uuid", "v0.7.0"), install().last())
        assertEquals("uuid skill", read("ktools-uuid/SKILL.md"))
    }

    @Test
    fun `install removes skills it installed that are no longer bundled`() {
        install()
        val changes = install(skills = listOf(uuidSkill))
        assertEquals(listOf(SkillChange.UpToDate("ktools-uuid"), SkillChange.Remove("ktools-hash")), changes)
        assertFalse(fileSystem.exists(skillsDir / "ktools-hash"))
    }

    @Test
    fun `install leaves a skill directory it did not install alone`() {
        fileSystem.createDirectories(skillsDir / "ktools-uuid")
        fileSystem.write(skillsDir / "ktools-uuid" / "SKILL.md") { writeUtf8("my own skill") }
        val changes = install()
        assertEquals(SkillChange.Conflict("ktools-uuid", replace = false), changes.last())
        assertEquals("my own skill", read("ktools-uuid/SKILL.md"))
    }

    @Test
    fun `install with force replaces a skill directory it did not install`() {
        fileSystem.createDirectories(skillsDir / "ktools-uuid")
        fileSystem.write(skillsDir / "ktools-uuid" / "SKILL.md") { writeUtf8("my own skill") }
        assertEquals(SkillChange.Conflict("ktools-uuid", replace = true), install(force = true).last())
        assertEquals("uuid skill", read("ktools-uuid/SKILL.md"))
    }

    @Test
    fun `install never removes other skills in the directory`() {
        fileSystem.createDirectories(skillsDir / "other-skill")
        fileSystem.write(skillsDir / "other-skill" / "SKILL.md") { writeUtf8("other") }
        install()
        planSkillUninstall(fileSystem, skillsDir).also { applySkillChanges(fileSystem, skillsDir, skills, it, "v") }
        assertEquals("other", read("other-skill/SKILL.md"))
    }

    @Test
    fun `uninstall removes every skill ktools installed`() {
        install()
        val changes = planSkillUninstall(fileSystem, skillsDir)
        assertEquals(listOf(SkillChange.Remove("ktools-hash"), SkillChange.Remove("ktools-uuid")), changes)
        applySkillChanges(fileSystem, skillsDir, skills, changes, "v0.7.0")
        assertEquals(emptyList(), fileSystem.list(skillsDir))
    }

    @Test
    fun `uninstall of a missing skills directory changes nothing`() {
        assertEquals(emptyList(), planSkillUninstall(fileSystem, home / "missing"))
    }

    @Test
    fun `hasInstalledSkills is true only when ktools installed a skill`() {
        assertFalse(hasInstalledSkills(fileSystem, skillsDir))
        install()
        assertTrue(hasInstalledSkills(fileSystem, skillsDir))
    }
}
