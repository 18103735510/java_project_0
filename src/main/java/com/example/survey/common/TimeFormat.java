package com.example.survey.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** 时间格式化：接口用 ISO-8601，给人看的报告用「yyyy-MM-dd HH:mm:ss」。 */
public final class TimeFormat {

    private static final DateTimeFormatter ISO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private TimeFormat() {
    }

    public static String iso(Instant instant) {
        return ISO.format(instant == null ? Instant.now() : instant);
    }

    public static String display(Instant instant) {
        return DISPLAY.format(instant == null ? Instant.now() : instant);
    }
}
