# Kegels

A clean, distraction-free Android app for Kegel exercise training. Track your sessions, build streaks, and unlock achievements as you progress.

## Features

- **Guided exercise sessions** — animated circle pulses at your chosen tempo to guide contract/relax phases
- **Customisable settings** — adjust kegels per minute (10–120) and total reps per session (10–300)
- **Statistics dashboard** — 91-day heatmap calendar, hourly breakdown bar chart, and 7-day weekly trend bar chart
- **Achievement medals** — 9 milestone medals (100 → 100,000 reps) that unlock with a congratulation popup
- **Smart reminders** — configurable notification interval with Do Not Disturb window (default: 22:00–07:00)
- **Language support** — English and Chinese (简体中文), switchable at runtime
- **Onboarding** — guided introduction on first launch

## Screenshots

_Coming soon_

## Requirements

- Android 8.0 (API 31) or higher
- No internet permission required — all data stays on device

## Tech Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Hilt DI |
| Navigation | Navigation Compose |
| Database | Room |
| Preferences | DataStore |
| Background | WorkManager |

## Building

### Debug APK

```bash
./gradlew assembleDebug
```

### Release APK (local, unsigned)

```bash
./gradlew assembleRelease
```

### Release APK (signed, for distribution)

Set the following environment variables before building:

```bash
export KEYSTORE_PATH=/path/to/release.keystore
export SIGNING_STORE_PASSWORD=your_store_password
export SIGNING_KEY_ALIAS=your_key_alias
export SIGNING_KEY_PASSWORD=your_key_password
./gradlew assembleRelease
```

## CI / CD

Pushing a version tag triggers a GitHub Actions workflow that builds a signed APK and publishes it as a GitHub Release automatically.

```bash
git tag v1.2.3
git push origin v1.2.3
```

The tag format `vMAJOR.MINOR.PATCH` is used to derive `versionName` and `versionCode` automatically.

Required repository secrets:

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded release keystore |
| `STORE_PASSWORD` | Keystore store password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Key password |

## License

MIT
