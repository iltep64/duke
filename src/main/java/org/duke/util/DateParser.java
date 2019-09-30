package org.duke.util;

import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalQuery;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Helper class to parse rough date/time strings.
 */
public class DateParser {

    private static final DateTimeFormatter weekdayFormatter =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive().parseLenient()
                    .optionalStart()
                    .appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT)
                    .optionalEnd()
                    .optionalStart()
                    .appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL)
                    .optionalEnd()
                    .toFormatter();
    private static final DateTimeFormatter dmFormatter =
            new DateTimeFormatterBuilder()
                    .parseLenient()
                    .appendValue(ChronoField.DAY_OF_MONTH)
                    .appendLiteral(' ')
                    .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT).toFormatter();
    private static final DateTimeFormatter mdFormatter =
            new DateTimeFormatterBuilder()
                    .parseLenient()
                    .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
                    .appendLiteral(' ')
                    .appendValue(ChronoField.DAY_OF_MONTH).toFormatter();
    private static final DateTimeFormatter[] dateFormatters = new DateTimeFormatter[]{
            dmFormatter, addYear(dmFormatter),
            mdFormatter, addYear(mdFormatter),
            DateTimeFormatter.ISO_LOCAL_DATE
    };
    private static final DateTimeFormatter[] timeFormatters = new DateTimeFormatter[]{
            DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM),
            DateTimeFormatter.ofPattern("kkmm'h'"),
            new DateTimeFormatterBuilder()
                    .appendPattern("h[[':']['.']mm][' ']")
                    .parseCaseInsensitive()
                    .optionalStart().appendText(ChronoField.AMPM_OF_DAY).optionalEnd()
                    .toFormatter(),
            DateTimeFormatter.ISO_LOCAL_TIME
    };

    private final String text;
    private final ParsePosition pos;

    private DateParser(String input) {
        this.text = input;
        this.pos = new ParsePosition(0);
    }

    private static DateTimeFormatter addYear(DateTimeFormatter fmt) {
        return new DateTimeFormatterBuilder()
                .append(fmt)
                .parseLenient()
                .appendLiteral(' ')
                .appendValueReduced(ChronoField.YEAR, 2, 4, 2000)
                .toFormatter();
    }

    /**
     * Given an input string describing a date+time,
     * return a {@link java.time.LocalDateTime} representing it.
     *
     * If the input specifies a specific date, we use that date.
     * Else, it returns the next matching date in the future.
     *
     * @param input Rough date input
     * @return {@link java.time.LocalDateTime} object, or null if no valid parse.
     */
    public static LocalDateTime parse(String input) {
        return new DateParser(input).parseDateTime();
    }

    private void eatWhitespace() {
        int i;
        for (i = pos.getIndex(); i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                break;
            }
        }
        pos.setIndex(i);
    }

    private <R> R tryParse(DateTimeFormatter formatter,
                           TemporalQuery<R> query) {
        eatWhitespace();
        int ix = pos.getIndex();
        int errIx = pos.getErrorIndex();
        try {
            TemporalAccessor parsed = formatter.parse(text, pos);
            R value = query.queryFrom(parsed);
            if (value == null) {
                pos.setIndex(ix);
                pos.setErrorIndex(errIx);
            }
            return value;
        } catch (DateTimeException e) {
            pos.setIndex(ix);
            pos.setErrorIndex(errIx);
            return null;
        }
    }

    @SafeVarargs
    private <R> R tryParses(DateTimeFormatter formatter,
                            TemporalQuery<R>... queries) {
        return tryParse(formatter, parsed -> {
            for (TemporalQuery<R> q : queries) {
                try {
                    R value = q.queryFrom(parsed);
                    if (value != null) {
                        return value;
                    }
                } catch (DateTimeException e) {
                    //Swallow any failed parse.
                }
            }
            return null;
        });
    }

    private Stream<LocalDate> parseWeekday(DayOfWeek weekday) {
        TemporalAdjuster nextOrSameWeekday = TemporalAdjusters.nextOrSame(weekday);
        TemporalAdjuster nextWeekday = TemporalAdjusters.next(weekday);
        LocalDate first = LocalDate.now().with(nextOrSameWeekday);
        return Stream.iterate(first, prev ->
                LocalDate.from(nextWeekday.adjustInto(prev))
        );
    }

    private Stream<LocalDate> parseMonthDay(MonthDay md) {
        LocalDate now = LocalDate.now();
        int startYear = now.getYear();
        IntStream yearStream = IntStream.range(startYear, 9999);
        if (md.equals(MonthDay.of(2, 29))) {
            yearStream = yearStream.filter(y -> Year.of(y).isLeap());
        }
        return yearStream.mapToObj(md::atYear)
                .filter(date -> date.isAfter(now));
    }

    private LocalDateTime tryDates(Stream<LocalDate> possibleDates, LocalTime exactTime) {
        if (possibleDates == null || exactTime == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return possibleDates.map(d -> d.atTime(exactTime))
                .filter(d -> d.isAfter(now))
                .findFirst().orElse(null);
    }

    /**
     * Parse into a {@link LocalDateTime} object based off the given user input.
     * @return Best-guess {@link LocalDateTime} representing the input
     */
    public LocalDateTime parseDateTime() {
        for (DateTimeFormatter dt : dateFormatters) {
            LocalDateTime date = tryParses(dt,
                    acc -> {
                        LocalDate exactDate = LocalDate.from(acc);
                        if (exactDate == null) {
                            return null;
                        }
                        LocalTime exactTime = parseTime();
                        if (exactTime == null) {
                            return null;
                        }
                        return exactDate.atTime(exactTime);
                    },
                    acc -> {
                        Stream<LocalDate> possibleDates = parseMonthDay(MonthDay.from(acc));
                        LocalTime exactTime = parseTime();
                        return tryDates(possibleDates, exactTime);
                    });
            if (date != null) {
                return date;
            }
        }
        for (DateTimeFormatter dt : dateFormatters) {
            LocalDateTime date = tryParses(dt,
                    acc -> {
                        LocalDate exactDate = LocalDate.from(acc);
                        if (exactDate == null) {
                            return null;
                        }
                        return exactDate.atTime(LocalTime.MIDNIGHT);
                    },
                    acc -> {
                        Stream<LocalDate> possibleDates = parseMonthDay(MonthDay.from(acc));
                        return tryDates(possibleDates, LocalTime.MIDNIGHT);
                    });
            if (date != null) {
                return date;
            }
        }
        Stream<LocalDate> possibleDates = tryParse(weekdayFormatter,
                acc -> parseWeekday(DayOfWeek.from(acc))
        );
        LocalTime exactTime = parseTime();
        if (exactTime == null) {
            exactTime = LocalTime.MIDNIGHT;
        }
        return tryDates(possibleDates, exactTime);
    }

    private LocalTime parseTime() {
        for (DateTimeFormatter dt : timeFormatters) {
            LocalTime time = tryParse(dt, LocalTime::from);
            if (time != null) {
                return time;
            }
        }
        return null;
    }
}
