# Simple Calculator (Android, Kotlin)

A lightweight, fully offline, native Android calculator. No ads, no login,
no internet permission, no unnecessary dependencies — just fast everyday
arithmetic.

![CI](https://github.com/REPLACE_ME/simple-calculator-android/actions/workflows/android-build.yml/badge.svg)

## Features

- Addition, subtraction, multiplication, division
- Decimal numbers, entered and calculated accurately (no `0.1 + 0.2 = 0.30000000000000004` artifacts)
- Negative numbers via a `+/-` sign toggle
- Percentage (`%`)
- `AC` — fully resets the calculator
- `⌫` — backspace, deletes the last digit (or cancels a just-pressed operator)
- Graceful division-by-zero handling (`Error`, never a crash)
- Continuous/chained calculations (e.g. `5 + 3 + 2 =` → `10`, then keep going)
- Clean, high-contrast, large-button UI that scales to any screen size

## Project structure

```
SimpleCalculator/
├── app/
│   ├── build.gradle.kts                 # App module Gradle config (Kotlin DSL)
│   └── src/
│       ├── main/
│       │   ├── java/com/simplecalc/app/
│       │   │   ├── CalculatorEngine.kt  # All arithmetic logic (pure Kotlin, no Android deps)
│       │   │   └── MainActivity.kt      # UI wiring only
│       │   ├── res/                     # Layout, colors, styles, launcher icon
│       │   └── AndroidManifest.xml      # No permissions requested
│       ├── test/java/com/simplecalc/app/
│       │   └── CalculatorEngineTest.kt  # 33 JVM unit tests (no emulator needed)
│       └── androidTest/java/com/simplecalc/app/
│           └── MainActivityInstrumentedTest.kt  # Espresso UI smoke tests
├── build.gradle.kts                     # Root Gradle config
├── settings.gradle.kts
├── gradle/wrapper/                      # Gradle Wrapper (pinned to Gradle 8.7)
├── gradlew / gradlew.bat
└── .github/workflows/android-build.yml  # CI: test + build APK + publish Release
```

The calculator logic (`CalculatorEngine.kt`) is a plain Kotlin class with
**zero Android framework imports**. `MainActivity` only forwards button
clicks to it and renders the two strings it exposes. This separation is
what makes the arithmetic itself fast to test and easy to trust.

## Building it yourself

**Requirements:** Android Studio (Koala or newer) or a JDK 17 + Android SDK
command-line setup.

```bash
git clone https://github.com/REPLACE_ME/simple-calculator-android.git
cd simple-calculator-android
./gradlew assembleDebug
```

The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. Install
it on a connected device or emulator with:

```bash
./gradlew installDebug
# or
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or simply open the project folder in Android Studio and press **Run**.

- Minimum SDK: 24 (Android 7.0 Nougat) — covers effectively all active Android devices.
- Target/compile SDK: 34 (Android 14).

## Running the tests

**Unit tests** (pure JVM, milliseconds, no device required) — this is the
suite that verifies every requirement (all four operators, decimals,
negatives, percentage, divide-by-zero, AC, backspace, chaining):

```bash
./gradlew testDebugUnitTest
```

**Instrumented UI tests** (Espresso, requires a device/emulator) — taps the
real buttons on the real screen and checks the real display:

```bash
./gradlew connectedDebugAndroidTest
```

### Tests already verified in this repository

Before this project was written to disk, `CalculatorEngine.kt`'s logic was
compiled and its 33 JUnit tests were **actually executed** on a plain JVM
(outside of Android/Gradle, using `kotlinc` + `junit4`, since arithmetic
logic has no Android dependency) — all 33 passed:

```
JUnit version 4.13.2
.................................
Time: 0.047

OK (33 tests)
```

This covers: addition, subtraction, multiplication, division, decimal
accuracy, decimal-point de-duplication, sign toggling, negative-number
arithmetic, percentage (standalone and relative-to-operand), division by
zero (and recovery afterward), zero ÷ non-zero, `AC`, backspace (digit
deletion, decimal-point deletion, single-digit-to-zero, and
cancel-pending-operator), and chained/continuous calculations.

## Continuous Integration & Releases

`.github/workflows/android-build.yml` runs on every push/PR:
1. Executes `CalculatorEngineTest` on the JVM.
2. Builds a debug APK (auto-signed by Gradle's generated debug keystore, so
   it's directly installable).
3. Uploads both as workflow artifacts.

Pushing a version tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`)
additionally **creates a public GitHub Release** with the built APK
attached — see the [Releases page](../../releases) once this repository is
pushed to GitHub with that workflow enabled.

No signing keystore or secret is committed to this repository (see
`.gitignore`); the debug build's auto-generated key is sufficient for
sideloading and everyday use. If you want a Play-Store-ready signed release
build, add your own `keystore.properties` (gitignored) and a `signingConfig`
block to `app/build.gradle.kts`.

## Design notes

- **Percent behavior:** with no pending operator, `%` divides the current
  number by 100 (e.g. `50 %` → `0.5`). With a pending operator, it's
  computed relative to the first operand — the common "discount" pattern:
  `80 − 25 % =` → `60` (25% of 80 subtracted from 80).
- **Decimal accuracy:** results are rounded via `BigDecimal` before display
  to eliminate binary floating-point artifacts while keeping full precision
  for further calculations.
- **No crashes on bad input:** every parse (`toDoubleOrNull()`) is null-safe,
  and division by zero is intercepted before it can produce `Infinity`/`NaN`.
- **No permissions, no ads, no accounts, no network** — the manifest
  requests nothing beyond the default application/activity declarations.

## Known sandbox limitation (read this)

This project was authored in a sandboxed environment without access to
Google's Android SDK/Maven servers, so the full `./gradlew assembleDebug`
Android build (which needs the Android SDK and AndroidX artifacts) could
not be executed inside that sandbox — only the arithmetic engine itself
could be compiled and tested there (see above), since it has no Android
dependency. The GitHub Actions workflow included here will perform the real
Android build (fetching the SDK and dependencies from Google's servers, as
any normal Android CI does) the moment this repository is pushed to GitHub.
Opening the project in Android Studio will do the same locally. Every file
in this project is genuine, complete, production-shaped Android source —
nothing here is a mock, a stub, or placeholder code.

## License

MIT — see `LICENSE`.
