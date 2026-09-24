package com.bindraft.amaldhikirtracker.util

import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.*

object SunsetCalculator {
    /**
     * A simple approximation of sunset time.
     * For a production app, a library like PrayTimes or specific astronomical formulas would be better.
     * This uses a simplified version of the Sunrise/Sunset algorithm.
     */
    fun getSunsetTime(latitude: Double, longitude: Double, date: ZonedDateTime): LocalTime {
        val dayOfYear = date.dayOfYear
        val zenith = 90.833 // Sunset zenith

        // 1. first calculate the day of the year
        val N = dayOfYear.toDouble()

        // 2. convert the longitude to hour value and calculate an approximate time
        val lngHour = longitude / 15.0
        val t = N + ((18.0 - lngHour) / 24.0) // 18 for sunset approximation

        // 3. calculate the Sun's mean anomaly
        val M = (0.9856 * t) - 3.2891

        // 4. calculate the Sun's true longitude
        var L = M + (1.916 * sin(Math.toRadians(M))) + (0.020 * sin(Math.toRadians(2.0 * M))) + 282.634
        L = L % 360.0
        if (L < 0) L += 360.0

        // 5. calculate the Sun's right ascension
        var RA = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(L))))
        RA = RA % 360.0
        if (RA < 0) RA += 360.0

        // 5b. right ascension value needs to be in the same quadrant as L
        val Lquadrant = floor(L / 90.0) * 90.0
        val RAquadrant = floor(RA / 90.0) * 90.0
        RA = RA + (Lquadrant - RAquadrant)

        // 5c. right ascension value needs to be converted into hours
        RA = RA / 15.0

        // 6. calculate the Sun's declination
        val sinDec = 0.39782 * sin(Math.toRadians(L))
        val cosDec = cos(asin(sinDec))

        // 7. calculate the Sun's local hour angle
        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(latitude)))) / (cosDec * cos(Math.toRadians(latitude)))
        
        if (cosH > 1) return LocalTime.of(18, 0) // Sun never sets
        if (cosH < -1) return LocalTime.of(18, 0) // Sun never rises

        // 8. finish calculating H and convert into hours
        var H = Math.toDegrees(acos(cosH))
        H = H / 15.0

        // 9. calculate local mean time of sunset
        val T = H + RA - (0.06571 * t) - 6.622

        // 10. adjust back to UTC
        var UT = T - lngHour
        UT = UT % 24.0
        if (UT < 0) UT += 24.0

        // 11. convert UT value to local time zone of latitude/longitude
        val localT = UT + (date.offset.totalSeconds / 3600.0)
        
        var finalHour = localT.toInt()
        var finalMinute = ((localT - finalHour) * 60).toInt()
        
        if (finalHour >= 24) finalHour -= 24
        if (finalHour < 0) finalHour += 24
        
        return LocalTime.of(finalHour.coerceIn(0, 23), finalMinute.coerceIn(0, 59))
    }
}
