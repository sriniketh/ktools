# What is ktools?

ktools is a command line application that provides useful developer tools. It's built as a kotlin multiplatform project and is currently available for macos (both X64 and ARM), linux and windows targets.

## Commands

* `ktools --help` - View all available commands
* `ktools uuid` - Creates a random UUID (v4 or v7), or inspects one with `uuid inspect`
* `ktools unixtime` - Unix time conversions
* `ktools hash` - Provides hash value for given file or string
* `ktools encode` - Encodes some text content to specified format
* `ktools decode` - Decodes some text content from specified format
* `ktools prettyprint` - Pretty print commonly used text like json
* `ktools update` - Updates ktools to the latest release
* `ktools init` - Installs the ktools skills into your AI coding agents
* ... and growing

Commands are written using [Clikt](https://ajalt.github.io/clikt/) and have help messages that can be viewed using `--help`. Learn more about these commands [here](commands.md).

## Agent skills

ktools ships skills that teach AI coding agents (Claude Code, Codex, Gemini CLI, GitHub Copilot, Cursor, OpenCode and others) when and how to use it. Run `ktools init` to install them into every agent on your machine, and run it again after `ktools update` to pick up the skills for the new version. See [`ktools init`](commands.md#ktools-init) for details.

## Releases

ktools is available as an executable for MacOS ARM, MacOS x64, Linux x64 and Windows x64 systems. Find the latest release [here](https://github.com/sriniketh/ktools/releases). 

To update an installed ktools, run `ktools update`. It downloads the latest release for your platform, checks it against the release's SHA-256 checksums to catch a corrupted or truncated download, and replaces the executable. Run `ktools update --check` to only see whether an update is available.

## License

```dtd
Copyright 2024 Sriniketh Ramachandran

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
