# Project Memory: Amal & Dhikir Tracker

## Project Overview
An Android application built with Jetpack Compose to track "Amal" (good deeds) and "Dhikir" (remembrances).

## Tech Stack
- **UI:** Jetpack Compose with Material 3
- **Navigation:** Compose Navigation
- **Architecture:** MVVM (ViewModel, Repository, Room)
- **Database:** Room with KSP
- **Other Libs:** Coil (Images), Retrofit/Moshi (Networking - though likely for future use as current screens seem local-focused), DataStore.

## Current Project Structure
- `MainActivity.kt`: Main entry point with `NavHost`.
- `AmalApplication.kt`: Application class managing the repository.
- `ui/`:
    - `screens/`: `TrackerScreen`, `CounterScreen`, `HistoryScreen`, `ManageDhikirsScreen`.
    - `viewmodel/`: `TrackerViewModel`, `CounterViewModel`, `HistoryViewModel`.
    - `navigation/`: `Screen` navigation definitions.
- `data/`:
    - `local/`: `AmalDatabase`, DAOs, and Entities.
    - `repository/`: Repository layer.

## Navigation Routes
- `Tracker`: Main dashboard.
- `Counter`: Individual dhikir counter (takes `dhikirId`).
- `History`: History of activities.
- `ManageDhikirs`: Screen to add/edit/delete dhikirs.

## Status
- Core infrastructure (Database, Navigation, ViewModels) appears to be in place.
- Multiple screens are implemented but their specific details (UI/UX) can be explored further as needed.
