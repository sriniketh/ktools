#!/usr/bin/env bash
# Build the debug native binary for the host and print its path.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
case "$(uname -s)-$(uname -m)" in
  Darwin-arm64) T=MacosArm64; D=macosArm64 ;;
  Darwin-x86_64) T=MacosX64; D=macosX64 ;;
  Linux-x86_64) T=LinuxX64; D=linuxX64 ;;
  *) echo "unsupported host" >&2; exit 2 ;;
esac
./gradlew "linkDebugExecutable$T" -q >&2
# The build regenerates this tracked file; restore it so verification leaves no diff.
git checkout -- src/nativeMain/resources/librariesandlicenses.json 2>/dev/null || true
echo "$PWD/build/bin/$D/debugExecutable/ktools.kexe"
