package com.wndenis.snipsnap.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// DATE UTILS

fun LocalDateTime.conv(): String {
    val df = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    return this.format(df)
}

fun LocalDateTime.between(
    target: LocalDateTime,
    increment: LocalDateTime.() -> LocalDateTime
): Sequence<LocalDateTime> {
    return generateSequence(
        seed = this,
        nextFunction = {
            val next = it.increment()
            if (next.isBefore(target)) next else null
        }
    )
}

infix fun LocalDateTime.daysBetween(target: LocalDateTime): Sequence<LocalDateTime> {
    val start = truncatedTo(ChronoUnit.DAYS)
    return start.between(target.truncatedTo(ChronoUnit.DAYS).plusDays(1)) {
        plusDays(1)
    }
}

infix fun LocalDateTime.monthsBetween(target: LocalDateTime): Sequence<LocalDateTime> {
    val start = truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
    return start.between(
        target
            .truncatedTo(ChronoUnit.DAYS)
            .withDayOfMonth(1)
            .plusMonths(1)
    ) {
        plusMonths(1)
    }
}

infix fun LocalDateTime.yearsBetween(target: LocalDateTime): Sequence<LocalDateTime> {
    val start = truncatedTo(ChronoUnit.DAYS).withMonth(1)
        .withDayOfMonth(1)
    return start.between(
        target
            .truncatedTo(ChronoUnit.DAYS)
            .withMonth(1)
            .withDayOfMonth(1)
            .plusYears(1)
    ) {
        plusYears(1)
    }
}
