package com.bindraft.amaldhikirtracker.util

import android.icu.util.IslamicCalendar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Hijri date conversion backed by Android's bundled ICU `IslamicCalendar` using the
 * Umm al-Qura calculation type — the same astronomically-computed tables Saudi Arabia's
 * official calendar (and most phones/browsers) use, closer to real moon-sighting than a
 * plain tabular/arithmetic Hijri calculation (e.g. `java.time.chrono.HijrahDate`, which
 * this replaces). Built into the Android SDK since API 24 (minSdk here is 28) — no extra
 * dependency, no network call, works fully offline.
 *
 * Note: even Umm al-Qura can differ by ~1 day from a specific region's local moon-sighting
 * committee — there is no single globally-agreed Hijri date. This is the best single
 * offline-friendly standard available, not a claim of universal correctness.
 */
object HijriCalendar {

    private val MONTH_NAMES = listOf(
        "Muharram", "Safar", "Rabi' al-awwal", "Rabi' al-thani",
        "Jumada al-awwal", "Jumada al-thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhul-Qi'dah", "Dhul-Hijjah"
    )

    fun monthName(month: Int): String = MONTH_NAMES.getOrElse(month - 1) { "Month $month" }

    /** month is 1-12 (Muharram..Dhul-Hijjah), matching [monthName]'s indexing. */
    data class HijriDate(val year: Int, val month: Int, val day: Int)

    private fun newCalendar(): IslamicCalendar =
        IslamicCalendar().apply { calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA }

    /**
     * [offsetDays] is a manual correction (typically -2..+2) to match a specific region's
     * local moon-sighting announcement, which can differ from the Umm al-Qura calculation by
     * a day. Positive shifts the Hijri date later, negative earlier. Must match the offset
     * passed to [toGregorian] for the two to stay exact inverses of each other.
     */
    fun from(date: LocalDate, offsetDays: Int = 0): HijriDate {
        val cal = newCalendar()
        cal.timeInMillis = date.plusDays(offsetDays.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return HijriDate(
            year = cal.get(IslamicCalendar.YEAR),
            month = cal.get(IslamicCalendar.MONTH) + 1,
            day = cal.get(IslamicCalendar.DAY_OF_MONTH)
        )
    }

    fun toGregorian(year: Int, month: Int, day: Int, offsetDays: Int = 0): LocalDate {
        val cal = newCalendar()
        cal.clear()
        cal.set(year, month - 1, day)
        val raw = Instant.ofEpochMilli(cal.timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return raw.minusDays(offsetDays.toLong())
    }

    fun lengthOfMonth(year: Int, month: Int): Int {
        val cal = newCalendar()
        cal.clear()
        cal.set(year, month - 1, 1)
        return cal.getActualMaximum(IslamicCalendar.DAY_OF_MONTH)
    }

    /** Adds [delta] Hijri months to (year, month), wrapping the year as needed. */
    fun addMonths(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        val zeroBased = (month - 1) + delta
        val newYear = year + Math.floorDiv(zeroBased, 12)
        val newMonth = Math.floorMod(zeroBased, 12) + 1
        return newYear to newMonth
    }
}
