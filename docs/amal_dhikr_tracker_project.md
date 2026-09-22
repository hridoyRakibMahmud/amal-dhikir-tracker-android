# Amal & Dhikr Tracker — Project Description

## Main Purpose

The main purpose of the app is to help users track their daily Islamic activities, such as:

- Dhikr
- Salah (prayers)
- Nafl prayers
- Nafl fasting
- Other daily acts of worship that may be added later

## User & Account

Initially, a user can open and explore the app. However, to actually save and track their activities, they need to create an account and log in.

After creating an account, the user can configure the activities they want to track. Their tracking data should be stored locally on the device and eventually synchronized with an online backend.

## Offline-First Architecture

The application should primarily work **offline-first**.

For the Android application:

- Use **Room Database** for local data storage.
- Users should be able to use the core tracking features without an internet connection.
- All tracking actions should initially be stored locally.
- When the internet connection becomes available, the locally stored changes should be synchronized with the online backend.
- The synchronization mechanism should handle situations where the user has been offline for an extended period.
- The local database should be treated as the primary source for the user's immediate interactions, rather than requiring an internet connection for every action.

I would also prefer to use a **free or low-cost backend/database solution initially**, if there is a suitable option.

## Hijri Calendar and Daily Boundary

A very important requirement is that the app should follow the **Hijri/Islamic concept of a day**.

In the Islamic calendar, a new day begins at **sunset (Maghrib)** rather than at midnight.

Therefore, the app should not simply use the standard Gregorian date from `00:00` to `23:59` to determine the user's daily tracking data.

For example:

- Before Maghrib → the current Hijri day is still active.
- After Maghrib → the next Hijri day begins.

The daily tracking system should be designed around this concept.

The implementation should also consider the user's **location/time zone and accurate Maghrib/sunset time**, rather than using a fixed time such as 6:00 PM.

## Dhikr Tracking

The app should initially contain a list of **predefined Dhikr**.

A user should be able to:

1. View the predefined Dhikr list.
2. Select the Dhikr they want to track.
3. Set a daily target/count.
4. Increment the count while performing the Dhikr.
5. Add multiple counts at once if necessary.
6. See how many they have completed.
7. See how many remain from their daily target.
8. See the total completed count for the current Hijri day.

For example:

If the user sets a target of 100 repetitions for a Dhikr:

- Target: 100
- Completed: 60
- Remaining: 40

If the user adds another 20, the completed count becomes 80.

The daily count should be associated with the appropriate **Hijri date/day**.

## Salah Tracking

The app should allow users to track their daily prayers.

Initially, it should include the **five obligatory prayers (Fard)**:

- Fajr
- Dhuhr
- Asr
- Maghrib
- Isha

The user should be able to mark each prayer as completed or not completed.

The app should also support **Nafl prayers**, allowing the user to add and track optional prayers.

Prayer records should also follow the app's Hijri-day boundary rather than simply resetting at midnight.

## Nafl Fasting Tracking

The app should also provide a way to track **Nafl fasting**.

The system should support fasting days based on the Hijri calendar and allow the user to mark whether they completed a particular Nafl fast.

The design should be flexible enough to support different types of recommended Nafl fasting days throughout the Hijri months.

## Android Technology

For the initial version, I want to build this as an **Android application** using:

- **Kotlin**
- **Jetpack Compose**
- **Room Database**
- Offline-first architecture
- Online synchronization when an internet connection is available

The architecture should be clean, scalable, and maintainable so that additional features can be added later.

## Development Approach

When helping me build this application, please consider the above requirements as the initial product requirements.

We can refine individual features, architecture, database design, UI, synchronization strategy, and implementation details step by step as development progresses.
