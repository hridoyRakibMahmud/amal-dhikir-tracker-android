# Design Mockup Prompt

Prompt for Claude Design (Artifact design type) to generate a mockup of the app.

```
Design a mobile app mockup for "Amal & Dhikir Tracker" — an Android app (Kotlin, Jetpack Compose, Material 3) that helps Muslims track daily Salah (prayers), Dhikir (remembrance counts), and (later) Nafl fasting, all organized by the Hijri day boundary (day starts at Maghrib/sunset, not midnight).

Style: calm, spiritual, uncluttered. Warm neutral/earthy palette (deep green, gold/amber accents, soft cream/off-white background) with full light + dark mode support. Arabic script used for Dhikir names alongside English/transliteration. Generous whitespace, rounded cards, soft shadows — not a busy dashboard.

Screens to design:

1. **Home / Tracker** — top header shows the current spiritual (Hijri) date and a Gregorian sub-label. Below: a Salah checklist — 5 Fard prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) as tappable rows/checkboxes marking completed/not-completed, plus a section for Nafl (voluntary) prayers (e.g. Tahajjud, Duha) with an "add voluntary prayer" affordance. Below that, a horizontal or grid list of tracked Dhikir cards, each showing Dhikir name (+ Arabic script), today's completed count, and — if the user has set one — a target with a progress ring/bar and remaining count; cards with no target just show the running count. Bottom navigation: Home, History, Settings.

2. **Dhikir Counter screen** — full-screen counter opened by tapping a Dhikir card. Large circular tap target in the center showing the current session count, haptic feedback on tap. Above it, today's total-completed count. If a target is set, show remaining and a progress indicator; if not, show an inline "Set a daily target" affordance. Bottom bar: "Reset Session" and "Complete/Save" actions. Top bar: Dhikir name + Arabic name, back button, edit-total icon.

3. **History screen** — date-range selector (e.g. 7/30 days), a completion-rate summary stat for Salah, total Dhikir count summary, then a scrollable per-day breakdown (grouped by spiritual/Hijri date) showing which prayers were completed and Dhikir counts logged that day.

4. **Manage Dhikirs screen** — list of all Dhikir (predefined + custom) with edit/delete actions and a per-item field to set/change/clear the personal daily target; a "+ Add custom Dhikir" button opening a form (name, Arabic name, category).

5. **Settings screen** — grouped list: Profile (placeholder for future account/login), Preferences (Calendar type: Gregorian/Hijri toggle), Content (Manage Dhikirs), About (app version).

6. **Login/Account screen (new, not yet built)** — simple sign-in/sign-up screen reflecting that the app is explorable without an account, but saving/syncing tracked data requires creating one. Keep it minimal: email/password or similar, a "Continue without account" option, brief copy explaining local data still works offline and syncs once signed in.

Show phone-width layouts, light and dark variants side by side if possible, and keep interaction affordances (tap targets, checkboxes, progress rings) clearly legible.
```
