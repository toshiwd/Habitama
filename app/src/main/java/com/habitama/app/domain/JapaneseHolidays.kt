package com.habitama.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import kotlin.math.floor

object JapaneseHolidays {
    fun isHoliday(date: LocalDate): Boolean = date in holidays(date.year)

    fun holidays(year: Int): Set<LocalDate> {
        require(year in 2000..2099) { "対応年は2000〜2099年です" }
        val base = mutableSetOf(
            LocalDate.of(year, Month.JANUARY, 1),
            nthMonday(year, Month.JANUARY, 2),
            LocalDate.of(year, Month.FEBRUARY, 11),
            LocalDate.of(year, Month.APRIL, 29),
            LocalDate.of(year, Month.MAY, 3),
            LocalDate.of(year, Month.MAY, 4),
            LocalDate.of(year, Month.MAY, 5),
            nthMonday(year, Month.JULY, 3),
            LocalDate.of(year, Month.AUGUST, 11),
            nthMonday(year, Month.SEPTEMBER, 3),
            nthMonday(year, Month.OCTOBER, 2),
            LocalDate.of(year, Month.NOVEMBER, 3),
            LocalDate.of(year, Month.NOVEMBER, 23),
            LocalDate.of(year, Month.MARCH, vernalEquinoxDay(year)),
            LocalDate.of(year, Month.SEPTEMBER, autumnEquinoxDay(year)),
        )
        if (year >= 2020) base += LocalDate.of(year, Month.FEBRUARY, 23)
        if (year in 2000..2018) base += LocalDate.of(year, Month.DECEMBER, 23)

        // 2020・2021年は東京大会に伴う特例日。
        if (year == 2020) {
            base -= nthMonday(year, Month.JULY, 3)
            base -= LocalDate.of(year, Month.AUGUST, 11)
            base -= nthMonday(year, Month.OCTOBER, 2)
            base += setOf(LocalDate.of(2020, 7, 23), LocalDate.of(2020, 7, 24), LocalDate.of(2020, 8, 10))
        } else if (year == 2021) {
            base -= nthMonday(year, Month.JULY, 3)
            base -= LocalDate.of(year, Month.AUGUST, 11)
            base -= nthMonday(year, Month.OCTOBER, 2)
            base += setOf(LocalDate.of(2021, 7, 22), LocalDate.of(2021, 7, 23), LocalDate.of(2021, 8, 8))
        }

        // 国民の休日: 祝日に挟まれた平日。
        var cursor = LocalDate.of(year, 1, 2)
        while (cursor.year == year) {
            if (cursor !in base && cursor.minusDays(1) in base && cursor.plusDays(1) in base) base += cursor
            cursor = cursor.plusDays(1)
        }

        // 振替休日: 日曜の祝日の直後で、祝日ではない最初の日。
        base.filter { it.dayOfWeek == DayOfWeek.SUNDAY }.sorted().forEach { sunday ->
            var substitute = sunday.plusDays(1)
            while (substitute in base) substitute = substitute.plusDays(1)
            if (substitute.year == year) base += substitute
        }
        return base
    }

    private fun nthMonday(year: Int, month: Month, nth: Int): LocalDate {
        var date = LocalDate.of(year, month, 1)
        while (date.dayOfWeek != DayOfWeek.MONDAY) date = date.plusDays(1)
        return date.plusWeeks((nth - 1).toLong())
    }

    private fun vernalEquinoxDay(year: Int): Int = floor(20.8431 + 0.242194 * (year - 1980) - floor((year - 1980) / 4.0)).toInt()
    private fun autumnEquinoxDay(year: Int): Int = floor(23.2488 + 0.242194 * (year - 1980) - floor((year - 1980) / 4.0)).toInt()
}
