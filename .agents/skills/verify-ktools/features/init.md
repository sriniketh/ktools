# init

`ktools init` installs the agent skills bundled into the binary (from `plugins/ktools/skills/`) into the skills
directory of every AI coding agent it finds under `$HOME`, and updates or removes the ones it installed before.

## Sub-features

- `init-install` first install, per target directory, with agents that share `~/.agents/skills` grouped.
- `init-rerun` second run reports every skill up to date and writes nothing.
- `init-conflict` an existing skill directory without a `.ktools-skill` marker is skipped (exit 1); `--force` replaces it.
- `init-agent` `--agent` installs only for the named agent, even when it is not found.
- `init-dry-run` reports with `would` and writes nothing.
- `init-uninstall` removes only directories with the marker.
- `init-none` no agent found.

## How to get to it (user POV)

- `ktools init [--agent <id>]... [--dry-run] [--force] [--uninstall]`

## Driving it with verify-ktools

Preconditions: doctor prints `OK`. Never drive `init` against your real home: export `HOME` to a temp directory for
every step, for example `export HOME=$(mktemp -d /tmp/kt-verify-home.XXXX)` in the shell that runs `drive.sh`. Unset
`CLAUDE_CONFIG_DIR`, `CODEX_HOME` and `XDG_CONFIG_HOME` too, or ktools follows them out of the temp home.

- **No agents.** With an empty `HOME`: `drive.sh init none -- init`. Exit 1; stderr
  `No supported agents found. Pick one with --agent: claude, codex, gemini, copilot, cursor, opencode, agents`.
- **Install.** `mkdir -p $HOME/.claude $HOME/.codex $HOME/.gemini`, then `drive.sh init install -- init`. Exit 0;
  stdout has `Claude Code: ~/.claude/skills` and `Codex, Gemini CLI: ~/.agents/skills`, each followed by
  `install ktools-<skill>` for all five skills (encoding, hash, prettyprint, unixtime, uuid), and ends with
  `Restart your agents to load the changes.` Each `<dir>/ktools-<skill>/SKILL.md` is byte-identical to
  `plugins/ktools/skills/ktools-<skill>/SKILL.md`, and `.ktools-skill` holds `ktools --version`'s version.
- **Rerun.** `drive.sh init rerun -- init`. Exit 0; every line is `ktools-<skill> is up to date`; no restart line.
  The shared directory's label stays `Codex, Gemini CLI` even though `~/.agents` now exists.
- **Conflict.** Replace `$HOME/.claude/skills/ktools-hash` with a directory holding only your own `SKILL.md`, then
  `drive.sh init conflict -- init --agent claude`. Exit 1; stdout `skip ktools-hash: not installed by ktools`;
  stderr ends `Re-run with --force to replace them.`; your file is unchanged.
- **Force.** `drive.sh init force -- init --agent claude --force`. Exit 0; `replace ktools-hash`; the bundled
  `SKILL.md` is back.
- **Linked directories.** Replace `$HOME/.claude/skills` with a symlink to `../.agents/skills`, then
  `drive.sh init linked -- init`. One target, `Claude Code, Codex, Gemini CLI: ~/.agents/skills`.
- **Dry run.** In a fresh `HOME` with `.claude`: `drive.sh init dry -- init --dry-run`. Exit 0; `would install` lines;
  `$HOME/.claude/skills` does not exist afterwards.
- **Uninstall.** `drive.sh init uninstall -- init --uninstall`. Exit 0; `remove ktools-<skill>` lines; skill
  directories without a marker (your own skills) are still there.

## Gotchas

- The skills are compiled into the binary. After editing `plugins/ktools/skills/`, rebuild with `scripts/launch.sh`
  or `init` installs the old text.
- Exit 1 after a conflict is deliberate: the other skills were still installed.
