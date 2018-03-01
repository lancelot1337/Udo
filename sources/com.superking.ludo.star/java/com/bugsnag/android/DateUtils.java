package com.bugsnag.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class DateUtils {
    private static final ThreadLocal<DateFormat> iso8601Holder = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            iso8601.setTimeZone(tz);
            return iso8601;
        }
    };

    DateUtils() {
    }

    static String toISO8601(Date date) {
        return ((DateFormat) iso8601Holder.get()).format(date);
    }
}
