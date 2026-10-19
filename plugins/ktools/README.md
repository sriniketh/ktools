# ktools agent skills

Skills that teach AI coding agents when and how to use the [ktools](https://github.com/sriniketh/ktools) CLI, so they
run ktools instead of guessing UUIDs, epoch conversions, digests or base64 by hand.

| Skill | Use it for |
| --- | --- |
| [`ktools-uuid`](skills/ktools-uuid/SKILL.md) | Generating v4/v7 UUIDs and inspecting a UUID's version, variant and timestamp |
| [`ktools-unixtime`](skills/ktools-unixtime/SKILL.md) | Converting between epoch milliseconds and ISO 8601, and the current time |
| [`ktools-hash`](skills/ktools-hash/SKILL.md) | MD5, SHA-1, SHA-256 and SHA-512 of strings and files, and checking checksums |
| [`ktools-encoding`](skills/ktools-encoding/SKILL.md) | Base64 and UTF-8 hex encoding and decoding |
| [`ktools-prettyprint`](skills/ktools-prettyprint/SKILL.md) | Formatting and validating JSON, and reading Cookie headers |

The skills need ktools on `PATH`.

## Install

### With ktools (recommended)

```
ktools init
```

This installs the skills into every agent it finds: Claude Code, Codex, Gemini CLI, GitHub Copilot, Cursor, OpenCode
and anything else that reads `~/.agents/skills`. The skills are built into the ktools binary, so they always match
your ktools version. Run `ktools init` again after `ktools update`. See
[`ktools init`](https://sriniketh.dev/ktools/commands/#ktools-init) for options.

### Claude Code plugin

```
/plugin marketplace add sriniketh/ktools
/plugin install ktools@ktools
```

### Codex plugin

```
codex plugin marketplace add sriniketh/ktools
```

Then install `ktools` from the plugin browser.

### Any agent (skills.sh)

```
npx skills add sriniketh/ktools
```

The plugin and skills.sh routes install from the repository's default branch, which can be ahead of your installed
ktools. `ktools init` installs the skills that match your binary.
