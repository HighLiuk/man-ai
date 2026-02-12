# Emulator Management

Start the Android emulator, install and launch the app. Use this before returning to the user after any code change.

## Available AVDs

```bash
emulator -list-avds    # Currently: Nexus_6_Android_33
```

## Start Emulator

```bash
# Start with visible window (NO -no-window flag — user needs to see it)
emulator -avd Nexus_6_Android_33 -no-audio &>/dev/null &
```

Then wait for boot:

```bash
adb wait-for-device && sleep 20 && adb shell getprop sys.boot_completed
# Returns "1" when ready
```

**Important**: The `emulator` command MUST run in the background (`&`). It never terminates on its own. Do NOT use `run_in_background: true` for the Bash tool — just append `&` and redirect output.

## Install and Launch App

```bash
# Install (after assembleDebug)
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.highliuk.manai/.MainActivity
```

## Full Workflow (build + install + launch)

This is the standard workflow to run after every code change:

```bash
# 1. Build (use gradle skill pattern — run in background)
cd android && ./gradlew assembleDebug --no-daemon --console=plain 2>&1

# 2. Install and launch
adb install -r android/app/build/outputs/apk/debug/app-debug.apk && \
adb shell am start -n com.highliuk.manai/.MainActivity
```

## Check Emulator Status

```bash
adb devices                          # List connected devices
adb shell getprop sys.boot_completed # "1" if booted
```

## Stop Emulator

```bash
adb emu kill
```

## Restart Emulator (if stuck)

```bash
adb emu kill; sleep 2; emulator -avd Nexus_6_Android_33 -no-audio &>/dev/null &
adb wait-for-device && sleep 20 && adb shell getprop sys.boot_completed
```

## Gotchas

- **Never use `-no-window`** — the user needs to see the emulator
- The emulator process runs indefinitely. Don't wait for it to finish.
- `adb wait-for-device` returns as soon as ADB connects, but the OS may not be fully booted yet. Always follow with `sleep 20` + `getprop sys.boot_completed` check.
- If `adb devices` shows "offline", kill and restart the emulator.
- APK path after build: `android/app/build/outputs/apk/debug/app-debug.apk`
- App package: `com.highliuk.manai`, main activity: `.MainActivity`
