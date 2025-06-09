package io.github.damienvdb.dcm4junit.utils;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    private static final Random RANDOM = ThreadLocalRandom.current();

    public static String string(int length, String allowedChars) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(allowedChars.charAt(RANDOM.nextInt(allowedChars.length())));
        }

        return sb.toString();
    }

    public static int between(int startInclusive, int endInclusive) {
        return startInclusive + RANDOM.nextInt(endInclusive - startInclusive + 1);
    }

    public static <T> T next(T[] values) {
        return values[RANDOM.nextInt(values.length)];
    }

    public static LocalTime time() {
        return LocalTime.of(RANDOM.nextInt(24), RANDOM.nextInt(60), RANDOM.nextInt(60));
    }

    public static Date date() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);
        calendar.add(Calendar.DAY_OF_YEAR, RANDOM.nextInt(4 * 365));
        return calendar.getTime();
    }
}
