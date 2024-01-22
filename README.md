[![build](https://github.com/sriniketh/ktools/actions/workflows/build.yml/badge.svg)](https://github.com/sriniketh/ktools/actions/workflows/build.yml)

# ktools

ktools is a command line application that provides useful developer tools. It's built as a kotlin multiplatform project
and is currently available for macos (both X64 and ARM), linux and windows targets.

## Commands

- `ktools --help` - View all available commands
- `ktools uuid` - Creates a random UUID
- `ktools unixtime` - Unix time conversions
- `ktools hash` - Provides hash value for given file or string
- `ktools encode` - Encodes some text content to specified format
- `ktools decode` - Decodes some text content from specified format
- `ktools prettyprint` - Pretty print commonly used text like json
- ... and growing

Commands are written using [Clikt](https://ajalt.github.io/clikt/) and have help messages that can be viewed
using `--help` option. Learn more about these commands [here](https://sriniketh.dev/ktools/commands/).

## Docs

The function definitions for common code can be viewed [here](https://sriniketh.dev/ktools/dokka/ktools/dev.sriniketh/).

## Installation

Download the latest executable from the [releases](https://github.com/sriniketh/ktools/releases) page. You may have to
provide executable permission for the file based on your setup.

Note for MacOS: The OS may prevent opening the executable since the app has not been notarized. You can temporarily
override the settings to allow execution by heading to privacy and security settings and allowing execution as
mentioned [here](https://support.apple.com/en-us/HT202491).

## Why?

For fun, mostly. I use the terminal extensively but often have to switch away from it to do time unit conversions,
hashing, etc. I wanted a handy tool that runs in the terminal and also wanted to test kotlin multiplatform as a solution
for scalable cross-platform applications.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details
