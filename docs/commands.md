# Commands

## ktools --help
```dtd
Usage: ktools [<options>] <command> [<args>]...

Options:
  -v, --version  Show the version and exit
  -a, --about    Learn more about ktools and exit
  -h, --help     Show this message and exit

Commands:
  uuid         Create a random UUID
  unixtime     Unix time conversions
  hash         Get hash value for given file or string
  encode       Encode text content
  decode       Decode text content
  prettyprint  Pretty print content
```

## ktools --version
```dtd
ktools version 0.1.0
```

## ktools --about
```dtd
ktools is a command line application that provides useful developer tools. It's built as a kotlin multiplatform project.
Learn more at https://github.com/sriniketh/ktools.

Libraries used:
Clikt : com.github.ajalt.clikt:clikt : 5.0.3
Clikt Core : com.github.ajalt.clikt:clikt-core : 5.0.3
Colormath : com.github.ajalt.colormath:colormath : 3.6.0
Mordant : com.github.ajalt.mordant:mordant : 3.0.1
Mordant Core : com.github.ajalt.mordant:mordant-core : 3.0.1
okio : com.squareup.okio:okio : 3.10.2
ktor-client-core : io.ktor:ktor-client-core : 3.1.1
ktor-events : io.ktor:ktor-events : 3.1.1
ktor-http : io.ktor:ktor-http : 3.1.1
ktor-http-cio : io.ktor:ktor-http-cio : 3.1.1
ktor-io : io.ktor:ktor-io : 3.1.1
ktor-serialization : io.ktor:ktor-serialization : 3.1.1
ktor-sse : io.ktor:ktor-sse : 3.1.1
ktor-utils : io.ktor:ktor-utils : 3.1.1
ktor-websocket-serialization : io.ktor:ktor-websocket-serialization : 3.1.1
ktor-websockets : io.ktor:ktor-websockets : 3.1.1
Kotlin Stdlib : org.jetbrains.kotlin:kotlin-stdlib : 2.1.10
atomicfu : org.jetbrains.kotlinx:atomicfu : 0.23.1
kotlinx-coroutines-core : org.jetbrains.kotlinx:kotlinx-coroutines-core : 1.10.1
kotlinx-datetime : org.jetbrains.kotlinx:kotlinx-datetime : 0.6.2
kotlinx-io-bytestring : org.jetbrains.kotlinx:kotlinx-io-bytestring : 0.6.0
kotlinx-io-core : org.jetbrains.kotlinx:kotlinx-io-core : 0.6.0
kotlinx-serialization-core : org.jetbrains.kotlinx:kotlinx-serialization-core : 1.8.0
kotlinx-serialization-json : org.jetbrains.kotlinx:kotlinx-serialization-json : 1.8.0

Licenses:
Apache-2.0 : Apache License 2.0 : https://spdx.org/licenses/Apache-2.0.html
MIT : MIT License : https://spdx.org/licenses/MIT.html
```

## ktools uuid
```dtd
Usage: ktools uuid [<options>]

  Create a random UUID

Options:
  -c, --case=(lower|upper)  Use upper or lower case. Default is lower.
  -h, --help                Show this message and exit
```

## ktools unixtime
```dtd
Usage: ktools unixtime [<options>]

  Unix time conversions

Convert time in millis to ISO 8601 format:
  --millis=<text>

Convert time in ISO 8601 format to millis:
  --iso=<text>

Options:
  -o, --operation=(nowmillis|nowiso|millistoiso|isotomillis)  Choose operation to perform: [nowmillis | nowiso | millistoiso | isotomillis]
  -h, --help                                                  Show this message and exit
```

## ktools hash
```dtd
Usage: ktools hash [<options>] <algorithm>

  Get hash value for given file or string

Options:
  -f, --file=<value>    File to get hash value for (provide filepath)
  -s, --string=<value>  String to get hash value for
  -h, --help            Show this message and exit

Arguments:
  <algorithm>  Algorithm to use for hashing: [MD5 | SHA1 | SHA256 | SHA512]
```

## ktools encode
```dtd
Usage: ktools encode [<options>] <format>

  Encode text content

Options:
  -s, --string=<text>  Content that needs to be encoded
  -h, --help           Show this message and exit

Arguments:
  <format>  Format to encode to: [utf8 | base64]
```

## ktools decode
```dtd
Usage: ktools decode [<options>] <format>

  Decode text content

Options:
  -s, --string=<text>  Content that needs to be decoded
  -h, --help           Show this message and exit

Arguments:
  <format>  Format to decode from: [utf8 | base64]
```

## ktools prettyprint
```dtd
Usage: ktools prettyprint [<options>] <contenttype>

  Pretty print content

Options:
  -s, --string=<text>  Content that needs to be formatted
  -h, --help           Show this message and exit

Arguments:
  <contenttype>  Pretty print content type: [json | cookie-header]
```
