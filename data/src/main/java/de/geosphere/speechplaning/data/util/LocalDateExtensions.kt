package de.geosphere.speechplaning.data.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Überprüft, ob dieses Datum in derselben Kalenderwoche wie ein anderes Datum liegt.
 *
 * @param anderesDatum Das Datum, mit dem verglichen werden soll.
 * @param locale Die zu verwendende Locale zur Bestimmung der Wochenregeln (Standard ist die System-Locale).
 * @return true, wenn beide Daten in derselben Kalenderwoche desselben Jahres liegen, sonst false.
 */
fun LocalDate.isInSameCalendarWeek(anderesDatum: LocalDate, locale: Locale = Locale.getDefault()): Boolean {
    val weekFields = WeekFields.of(locale)

    val weekOfYear1 = this.get(weekFields.weekOfWeekBasedYear())
    val weekOfYear2 = anderesDatum.get(weekFields.weekOfWeekBasedYear())

    val weekBasedYear1 = this.get(weekFields.weekBasedYear())
    val weekBasedYear2 = anderesDatum.get(weekFields.weekBasedYear())

    return weekBasedYear1 == weekBasedYear2 && weekOfYear1 == weekOfYear2
}

/**
 * Überprüft, ob das Datum in der aktuellen Woche liegt. */
fun LocalDate.isInCurrentWeek(locale: Locale = Locale.getDefault()): Boolean {
    return this.isInSameCalendarWeek(LocalDate.now(), locale)
}

/**
 * Formatiert das Datum in einen lokalisierten, lesbaren String (z.B. "11. Januar 2026").
 */
fun LocalDate.toGermanDateString(): String {
    val formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.GERMANY)
    return this.format(formatter)
}

/**
 * Gibt den Wochentag auf Deutsch zurück (z.B. "Sonntag").
 */
fun LocalDate.getGermanDayOfWeek(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.GERMANY)
    return this.format(formatter)
}
