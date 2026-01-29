# Pomodoro CLI

Simple terminal-based Pomodoro timer written in Java with optional audio notifications. It relies on [picocli](https://picocli.info/) for the command-line interface and [JLayer](http://www.javazoom.net/javalayer/javalayer.html) for MP3 playback. A default finish sound ships with the app, or you can provide your own file via CLI options.

## Features

- Countdown timer with minute/second precision (`mm:ss`)
- ANSI-colored live display for clear progress feedback
- Optional MP3 alert either bundled (`default-finish-sound.mp3`) or user-specified via `--play-on-finish`
- Runnable as a fat JAR (`maven-shade-plugin`) or as a GraalVM Native Image via the `native` Maven profile

## Requirements

- JDK capable of compiling Java source/target level `25` (adjust `pom.xml` if you need to target an earlier release)
- Maven 3.9+ (or simply use the provided `mvnw` wrapper)
- Optional: GraalVM with `native-image` installed for the native build path

## Quick Start

```bash
./mvnw clean package
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25
```

You should see a ticking timer in the terminal. When it reaches `00:00`, the bundled MP3 plays (if your environment can output sound).

## Usage

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar [OPTIONS]
```

| Option | Description |
| --- | --- |
| `-t, --time <value>` | Timer length either in minutes (`25`) or `minutes:seconds` (`0:30`). Defaults to `25:00`. |
| `--play-on-finish <path>` | Absolute or relative path to a sound file (MP3) that should play when the timer finishes. If omitted, the embedded `default-finish-sound.mp3` is used. |

### Examples

Start a classic Pomodoro:

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25
```

Run a short 5-minute focus sprint with a custom alert:

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 5 --play-on-finish ~/sounds/complete.mp3
```

Run a 5-second test cycle to validate your speaker setup:

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 0:05
```

## Development

- Source lives under `src/main/java/cz/bernhard/pomodoro`.
- Resources (including the bundled MP3) live in `src/main/resources`.
- `pom.xml` configures the shade plugin to embed dependencies and set the entry point to `cz.bernhard.pomodoro.Main`.

Common tasks:

```bash
./mvnw clean package              # build shaded runnable JAR
./mvnw test                       # (when tests are added) run unit tests
```

The project currently has no automated tests; consider adding coverage for timer math and CLI parsing when the feature set grows.

## Native Image (optional)

A GraalVM native build can shrink startup time and remove the dependency on the JVM at runtime.

```bash
./mvnw -Pnative -DskipTests package
./target/pomodoro-cli             # generated native binary
```

Make sure your GraalVM distribution has the `native-image` tool installed and accessible on the `PATH` before invoking the profile.

### Building for Multiple Platforms

GraalVM native-image compiles for the **current platform only**. To build executables for all supported platforms, you need to run the build on each target platform/architecture.

#### Prerequisites (all platforms)

1. Install GraalVM JDK 21+ (or JDK 25 to match the project)
2. Install the `native-image` component: `gu install native-image`
3. Ensure `JAVA_HOME` points to your GraalVM installation

#### macOS (Apple Silicon - ARM64)

```bash
# On an Apple Silicon Mac (M1/M2/M3/M4)
./mvnw clean package -Pnative -DskipTests
mv target/pomodoro-cli target/pomodoro-cli-macos-arm64
```

#### macOS (Intel - x86_64)

```bash
# On an Intel Mac
./mvnw clean package -Pnative -DskipTests
mv target/pomodoro-cli target/pomodoro-cli-macos-x64
```

#### Linux (ARM64)

```bash
# On a Linux ARM64 machine (e.g., AWS Graviton, Raspberry Pi 4 64-bit)
./mvnw clean package -Pnative -DskipTests
mv target/pomodoro-cli target/pomodoro-cli-linux-arm64
```

#### Linux (x86_64)

```bash
# On a Linux x86_64 machine
./mvnw clean package -Pnative -DskipTests
mv target/pomodoro-cli target/pomodoro-cli-linux-x64
```

#### Windows (ARM64)

```powershell
# On a Windows ARM64 machine
# Requires Visual Studio Build Tools with ARM64 support
.\mvnw.cmd clean package -Pnative -DskipTests
move target\pomodoro-cli.exe target\pomodoro-cli-windows-arm64.exe
```

#### Windows (x86_64)

```powershell
# On a Windows x86_64 machine
# Requires Visual Studio Build Tools (2019 or later) with "Desktop development with C++" workload
# Run from "x64 Native Tools Command Prompt for VS"
.\mvnw.cmd clean package -Pnative -DskipTests
move target\pomodoro-cli.exe target\pomodoro-cli-windows-x64.exe
```

#### Using Docker for Linux Builds (from any host)

If you don't have access to native Linux machines, you can use Docker:

```bash
# Linux x86_64 build from any x86_64 host
docker run --rm -v $(pwd):/app -w /app \
  ghcr.io/graalvm/native-image-community:21 \
  ./mvnw clean package -Pnative -DskipTests

# Linux ARM64 build (requires ARM64 host or emulation)
docker run --rm --platform linux/arm64 -v $(pwd):/app -w /app \
  ghcr.io/graalvm/native-image-community:21 \
  ./mvnw clean package -Pnative -DskipTests
```

#### Platform Audio Support

| Platform | Primary Audio | Fallback |
| --- | --- | --- |
| macOS | Java Sound API | `afplay` |
| Linux | Java Sound API | `aplay`, `paplay`, `mpg123`, or `ffplay` |
| Windows | Java Sound API | PowerShell `Media.SoundPlayer` |

### CI/CD with GitHub Actions

The repository includes a GitHub Actions workflow (`.github/workflows/build-native.yml`) that automatically builds native executables for all platforms when you push a version tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers builds for:
- macOS ARM64 (Apple Silicon)
- macOS x64 (Intel)
- Linux x64
- Linux ARM64
- Windows x64
- Windows ARM64 (JAR + launcher script, as native cross-compile isn't available)

Built artifacts are automatically attached to the GitHub Release.
