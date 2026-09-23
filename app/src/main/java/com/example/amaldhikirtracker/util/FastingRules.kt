package com.example.amaldhikirtracker.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Hijri-calendar rules for Nafl fasting: which days fasting is forbidden on (scholarly
 * consensus, safe to hardcode), and informational "next occurrence" text for the standard
 * seeded fast types. Never auto-creates a log — purely for display/validation. Dates come
 * from [HijriCalendar] (Umm al-Qura), not a plain tabular calendar. Every function takes the
 * same [offsetDays] manual regional correction [HijriCalendar] does — pass the user's
 * `PreferencesManager.hijriDateOffset` through consistently.
 */
object FastingRules {

    fun hijriMonthName(month: Int): String = HijriCalendar.monthName(month)

    data class NextOccurrence(val date: LocalDate, val text: String)

    /** Eid al-Fitr (1 Shawwal), Eid al-Adha + the 3 days of Tashreeq (10-13 Dhul-Hijjah). */
    fun isFastingForbidden(date: LocalDate, offsetDays: Int = 0): Boolean {
        val hijri = HijriCalendar.from(date, offsetDays)
        return (hijri.month == 10 && hijri.day == 1) || (hijri.month == 12 && hijri.day in 10..13)
    }

    /** Ramadan (Hijri month 9) — every day is obligatory (Fard) fasting, not a Nafl recommendation. */
    fun isMandatoryFastDay(date: LocalDate, offsetDays: Int = 0): Boolean {
        return HijriCalendar.from(date, offsetDays).month == 9
    }

    /**
     * True if this date matches one of the standard Sunnah (Nafl) fasts — for calendar
     * highlighting. Ramadan is excluded (that's [isMandatoryFastDay], a different category).
     * Shawwal is only flagged for its own Mon/Thu or Ayyam al-Bidh days, not the whole month —
     * "Six of Shawwal" is flexible and not tied to specific dates, so it isn't highlighted here.
     */
    fun isRecommendedFastDay(date: LocalDate, offsetDays: Int = 0): Boolean {
        if (isFastingForbidden(date, offsetDays)) return false
        val hijri = HijriCalendar.from(date, offsetDays)
        if (hijri.month == 9) return false
        return date.dayOfWeek == DayOfWeek.MONDAY ||
            date.dayOfWeek == DayOfWeek.THURSDAY ||
            hijri.day in 13..15 ||
            (hijri.month == 1 && hijri.day == 10) ||
            (hijri.month == 12 && hijri.day == 9)
    }

    /** Next occurrence (date + display text like "10 Muharram · in 12 days") for the 5 seeded types; null for custom ones. */
    fun nextOccurrence(typeName: String, from: LocalDate, offsetDays: Int = 0): NextOccurrence? {
        val hijriFrom = HijriCalendar.from(from, offsetDays)

        return when (typeName) {
            "Mondays & Thursdays" -> {
                var d = from
                while (d.dayOfWeek != DayOfWeek.MONDAY && d.dayOfWeek != DayOfWeek.THURSDAY) {
                    d = d.plusDays(1)
                }
                val label = d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                describe(d, from, label)
            }
            "Ayyam al-Bidh (White Days)" -> {
                val (targetYear, targetMonth) = if (hijriFrom.day <= 15) {
                    hijriFrom.year to hijriFrom.month
                } else {
                    HijriCalendar.addMonths(hijriFrom.year, hijriFrom.month, 1)
                }
                val targetDate = HijriCalendar.toGregorian(targetYear, targetMonth, 13, offsetDays)
                describe(targetDate, from, "13th-15th ${hijriMonthName(targetMonth)}")
            }
            "Ashura" -> describeYearly(hijriFrom.year, 1, 10, from, offsetDays)
            "Arafah" -> describeYearly(hijriFrom.year, 12, 9, from, offsetDays)
            "Six of Shawwal" -> {
                val (targetYear, targetMonth) = if (hijriFrom.month <= 10) {
                    hijriFrom.year to 10
                } else {
                    hijriFrom.year + 1 to 10
                }
                val targetDate = HijriCalendar.toGregorian(targetYear, targetMonth, 2, offsetDays)
                describe(targetDate, from, "Any 6 days in Shawwal")
            }
            else -> null
        }
    }

    private fun describeYearly(currentHijriYear: Int, targetMonth: Int, targetDay: Int, from: LocalDate, offsetDays: Int): NextOccurrence {
        var targetDate = HijriCalendar.toGregorian(currentHijriYear, targetMonth, targetDay, offsetDays)
        if (targetDate.isBefore(from)) {
            targetDate = HijriCalendar.toGregorian(currentHijriYear + 1, targetMonth, targetDay, offsetDays)
        }
        return describe(targetDate, from, "$targetDay ${hijriMonthName(targetMonth)}")
    }

    private fun describe(targetGregorian: LocalDate, from: LocalDate, label: String): NextOccurrence {
        val days = ChronoUnit.DAYS.between(from, targetGregorian)
        val whenText = when {
            days <= 0L -> "today"
            days == 1L -> "tomorrow"
            else -> "in $days days"
        }
        return NextOccurrence(targetGregorian, "$label · $whenText")
    }
}
