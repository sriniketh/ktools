---
name: generate-release
description: Use when creating a new ktools release version — runs tests, updates dependency metadata and docs, generates changelog from commit history, tags, pushes, and updates GitHub release
metadata:
  internal: true
---

# Generate Release

Automates the ktools release process. Accepts a version name (e.g., `v0.4.0`) as input.

## Input

- **Version** (required): Semver string prefixed with `v` (e.g., `v0.4.0`)

## Prerequisites

- On the `main` branch with a clean working tree
- `gh` CLI authenticated
- JDK 17 available

## Steps

Follow each step in order. Stop immediately if any step fails.

### 1. Validate version format

Verify the version matches `v<major>.<minor>.<patch>` (e.g., `v0.4.0`).

Verify the tag does not already exist:

```bash
git tag -l <version>
```

If it returns output, the tag exists — stop and warn the user.

### 2. Run tests

```bash
./gradlew allTests
```

If tests fail, stop. Report which tests failed.

### 3. Update libraries and licenses JSON

The aboutlibraries plugin is configured to export directly to `src/nativeMain/resources/librariesandlicenses.json` (see `aboutLibraries { export { outputFile = ... } }` in `build.gradle.kts`), so no intermediate copy step is needed:

```bash
./gradlew exportLibraryDefinitions
```

Check if the file changed:

```bash
git diff --exit-code src/nativeMain/resources/librariesandlicenses.json
```

If changed (exit code 1), commit:

```bash
git add src/nativeMain/resources/librariesandlicenses.json
git commit -m "chore: update librariesandlicenses.json for <version>"
```

If unchanged (exit code 0), skip the commit.

### 4. Bump the plugin manifest versions

The ktools plugin manifests carry the release version without the leading `v` (e.g. `0.4.0` for `v0.4.0`). Set the
`"version"` field in all three, so plugin installs see the new release:

- `plugins/ktools/plugin.json`
- `plugins/ktools/.claude-plugin/plugin.json`
- `plugins/ktools/.codex-plugin/plugin.json`

If any changed, commit:

```bash
git add plugins/ktools
git commit -m "chore: bump ktools plugin version for <version>"
```

### 5. Trigger dokka docs deployment

The dokka docs are generated and deployed via the `docs.yml` workflow (not committed to the repo). Trigger it:

```bash
gh workflow run docs.yml
```

This runs asynchronously — no need to wait for it to complete before continuing.

### 6. Generate changelog

Get the previous tag:

```bash
git describe --tags --abbrev=0
```

Generate the changelog, excluding docs/superpowers commits:

```bash
git log <previous-tag>..HEAD --oneline -- . ':!docs/superpowers'
```

Format each line as a markdown list item:

```markdown
- <commit hash> <commit subject>
```

Save the full changelog text for use in steps 7 and 10.

### 7. Create annotated tag

```bash
git tag -a <version> -m "<changelog>"
```

Use the changelog from step 6 as the tag annotation.

### 8. Push commits and tag

```bash
git push && git push --tags
```

This triggers the `release.yml` CI workflow.

### 9. Wait for CI

Poll the release workflow until it completes:

```bash
gh run list --workflow=release.yml --limit=1 --json status,conclusion
```

Re-check every 60 seconds until `status` is `completed`. Then check `conclusion` — if it is not `success`, report the failure and stop.

### 10. Update GitHub release body

Verify the release exists first:

```bash
gh release view <version>
```

If the release does not exist yet (CI may still be finalizing), wait and retry.

Then update the release body:

```bash
gh release edit <version> --notes "<changelog>"
```

Use the same changelog from step 6. This ensures the changelog appears in both the tag annotation and the GitHub release page.

## Error Recovery

| Failure | What to do |
|---------|-----------|
| Tests fail (step 2) | Fix tests, re-run from step 1 |
| Gradle task fails (step 3) | Check Gradle output, fix, re-run the failed step |
| Docs workflow fails (step 5) | Investigate via `gh run list --workflow=docs.yml --limit=1`, re-trigger with `gh workflow run docs.yml` |
| Tag already exists (step 1) | User must choose a different version or delete the existing tag |
| Push fails (step 8) | Check remote status, resolve conflicts, retry push |
| CI fails (step 9) | Investigate the workflow run via `gh run view`, fix and re-tag if needed |
| Release edit fails (step 10) | Retry manually: `gh release edit <version> --notes "<changelog>"` |
