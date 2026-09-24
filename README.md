# Amal & Dhikir Tracker

An offline-first Android app for tracking daily Salah (prayers), Dhikir (remembrance), and Nafl fasting — built around the Hijri "spiritual day," which rolls over at Maghrib (sunset) rather than midnight.

## Features

- **Salah tracking** — the 5 obligatory Fard prayers with independent Sunnah completion, plus user-extensible Nafl (voluntary) prayers. A streak counter tracks consecutive days with all Fard prayers completed.
- **Dhikir tracking** — a predefined list plus custom entries, tap-to-count with haptic feedback, an "add manually" flow for counts done outside the app, and an optional per-Dhikir daily target with progress tracking.
- **Nafl fasting tracking** — a Hijri-month calendar you log fasts against directly. Highlights the 5 standard Sunnah fasts (Mondays & Thursdays, Ayyam al-Bidh, Ashura, Arafah, Six of Shawwal) with their next-occurrence dates, marks Ramadan as mandatory, blocks logging on the days fasting is forbidden (Eid al-Fitr, Eid al-Adha, the days of Tashreeq) and on future dates, and shows a missed/fasted/remaining summary for Ramadan.
- **Hijri calendar** — backed by Android's bundled ICU `IslamicCalendar` (Umm al-Qura calculation), with a manual day-offset setting to match a specific region's local moon-sighting announcement.
- **History** — 7/30-day Salah completion and Dhikir count charts, plus a detailed activity log.
- **Google Sign-In** — via Credential Manager / Google Identity Services. Currently local-session only (no backend yet — see [Roadmap](#roadmap)).
- **Light/dark theming** with a custom design system (see `docs/`), Poppins + Noto Naskh Arabic typography.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Room (local persistence) + DataStore (preferences)
- MVVM: `ViewModel` + `Flow`/`StateFlow`, manual `ViewModelProvider.Factory` (no DI framework)
- Compose Navigation
- Credential Manager + Google Identity Services (Google Sign-In)

## Project structure

```
app/src/main/java/com/example/amaldhikirtracker/
├── data/
│   ├── local/          # Room database, DAOs, entities, DataStore preferences
│   └── repository/      # Thin repository layer over the DAO
├── ui/
│   ├── navigation/       # Screen routes
│   ├── screens/          # Composable screens
│   ├── theme/            # Design tokens, typography
│   └── viewmodel/        # Per-screen ViewModels
└── util/                 # Hijri calendar, fasting rules, sunset calculation, spiritual date
```

## Getting started

1. Clone the repo and open it in Android Studio (or build from the CLI — see below).
2. `minSdk` is 28 (Android 9+); no special setup needed for most features.
3. **Google Sign-In requires an OAuth 2.0 Web Client ID** from [Google Cloud Console](https://console.cloud.google.com/) (APIs & Services → Credentials), registered against this app's package name and SHA-1 fingerprint (debug and, later, release). Replace the placeholder `GOOGLE_WEB_CLIENT_ID` in `ui/viewmodel/LoginViewModel.kt` with your real client ID. Until then, everything else in the app works fully offline without it.

### Build from the command line

```
./gradlew :app:assembleDebug
```

## Design

The design tokens and screen layouts follow a design handoff generated from the product requirements in `docs/amal_dhikr_tracker_project.md`. The Nafl fasting screen has no design mockup and was built to match the app's existing design system.

## Roadmap

- Real backend + sync (currently local-only; offline-first by design, sync is a planned future phase)
- Nafl fasting: broader flexibility for custom recurring fast types
- Real Adhan/prayer-time calculation (currently not shown — no fabricated times)
- A proper Room migration path (currently `fallbackToDestructiveMigration`, fine pre-release only)
- Automated test coverage
- Swap the placeholder "Sign in with Google" icon for Google's official brand asset before any store listing
