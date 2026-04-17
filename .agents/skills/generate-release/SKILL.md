---
name: generate-release
description: Use when creating a new ktools release version — runs tests, updates dependency metadata and docs, generates changelog from commit history, tags, pushes, and updates GitHub release
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

```bash
./gradlew exportLibraryDefinitions
cp build/generated/aboutLibraries/librariesandlicenses.json src/nativeMain/resources/librariesandlicenses.json
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

### 4. Trigger dokka docs deployment

The dokka docs are generated and deployed via the `docs.yml` workflow (not committed to the repo). Trigger it:

```bash
gh workflow run docs.yml
```

This runs asynchronously — no need to wait for it to complete before continuing.

### 5. Generate changelog

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

Save the full changelog text for use in steps 6 and 8.

### 6. Create annotated tag

```bash
git tag -a <version> -m "<changelog>"
```

Use the changelog from step 5 as the tag annotation.

### 7. Push commits and tag

```bash
git push && git push --tags
```

This triggers the `release.yml` CI workflow.

### 8. Wait for CI

Poll the release workflow until it completes:

```bash
gh run list --workflow=release.yml --limit=1 --json status,conclusion
```

Re-check every 60 seconds until `status` is `completed`. Then check `conclusion` — if it is not `success`, report the failure and stop.

### 9. Update GitHub release body

Verify the release exists first:

```bash
gh release view <version>
```

If the release does not exist yet (CI may still be finalizing), wait and retry.

Then update the release body:

```bash
gh release edit <version> --notes "<changelog>"
```

Use the same changelog from step 5. This ensures the changelog appears in both the tag annotation and the GitHub release page.

## Error Recovery

| Failure | What to do |
|---------|-----------|
| Tests fail (step 2) | Fix tests, re-run from step 1 |
| Gradle task fails (step 3) | Check Gradle output, fix, re-run the failed step |
| Docs workflow fails (step 4) | Investigate via `gh run list --workflow=docs.yml --limit=1`, re-trigger with `gh workflow run docs.yml` |
| Tag already exists (step 1) | User must choose a different version or delete the existing tag |
| Push fails (step 7) | Check remote status, resolve conflicts, retry push |
| CI fails (step 8) | Investigate the workflow run via `gh run view`, fix and re-tag if needed |
| Release edit fails (step 9) | Retry manually: `gh release edit <version> --notes "<changelog>"` |
