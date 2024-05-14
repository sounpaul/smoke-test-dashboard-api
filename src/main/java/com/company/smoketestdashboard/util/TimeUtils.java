package com.company.smoketestdashboard.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeUtils {

    public static String getCurrentDateTime(String pattern) {
        ZonedDateTime tzInstance = ZonedDateTime.now(ZoneId.of(ZoneId.systemDefault().getId()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
        return formatter.format(tzInstance);
    }

}
