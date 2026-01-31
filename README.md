# Pomodoro CLI

Simple lightweight terminal-based Pomodoro timer written in Java. It relies on [picocli](https://picocli.info/) for the command-line interface.

## Features

- Countdown timer with minute/second precision (`mm:ss`)
- ANSI-colored live display for clear progress feedback
- Lightweight design - no audio dependencies
- Chain with any command for custom notifications (e.g., `pomodoro && play sound.mp3`)
- Runnable as a fat JAR (`maven-shade-plugin`) or as a GraalVM Native Image via the `native` Maven profile

## Requirements

- JDK 17 or later
- Maven 3.9+ (or simply use the provided `mvnw` wrapper)
- Optional: GraalVM with `native-image` installed for the native build path

## Quick Start

```bash
./mvnw clean package
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25
```

You should see a ticking timer in the terminal. When it reaches `00:00`, the application exits with status code 0, allowing you to chain commands.

## Usage

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar [OPTIONS]
```

| Option | Description |
| --- | --- |
| `-t, --time <value>` | Timer length either in minutes (`25`) or `minutes:seconds` (`0:30`). Defaults to `25:00`. |

### Examples

Start a classic Pomodoro:

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25
```

Run a short 5-minute focus sprint:

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 5
```

Run a 5-second test cycle:

```bash
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 0:05
```

### Chaining Commands

Since the timer exits with status code 0 on completion, you can chain it with any command for custom notifications:

```bash
# Play a sound when timer completes (macOS)
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25 && afplay /path/to/sound.mp3

# Play a sound (Linux with mpg123)
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25 && mpg123 /path/to/sound.mp3

# Send a system notification (macOS)
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25 && osascript -e 'display notification "Pomodoro finished!" with title "Timer"'

# Send a system notification (Linux)
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25 && notify-send "Pomodoro finished!"

# Run any custom script
java -jar target/pomodoro-cli-1.0-SNAPSHOT.jar --time 25 && ./my-custom-script.sh
```

## Development

- Source lives under `src/main/java/cz/bernhard/pomodoro`.
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
