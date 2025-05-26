package com.github.damienvdb.dcm4junit.utils;

import lombok.experimental.UtilityClass;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.UIDUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.damienvdb.dcm4junit.utils.RandomUtils.*;

@UtilityClass
public class Faker {

    public static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-,.";
    private static final Map<String, String[]> PROPS = new HashMap<>();
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    static {
        setLocale(Locale.ENGLISH);
    }

    private static void setLocale(Locale locale) {
        PROPS.clear();
        ResourceBundle bundle = ResourceBundle.getBundle("name", locale);
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = bundle.getString(key);
            PROPS.put(key, value.split(","));
        }
    }

    public String uid() {
        return UI();
    }

    public String patientID() {
        return LO();
    }

    public String personName() {
        return PN();
    }

    public String description() {
        return String.format("%s %s %s",
                next("bodypart"),
                next("modality"),
                next("axis")
        );
    }

    public String modality() {
        return next("modality");
    }

    //    VRs

    public static String AS() {
        int years = between(1, 80);
        return String.format("%03dY", years);
    }

    public static String UI() {
        return UIDUtils.createUID();
    }

    public static String LO() {
        return string(between(2, 64), ALLOWED_CHARS);
    }

    public static String DA() {
        return DateUtils.formatDA(TimeZone.getDefault(), date());
    }

    private static String PN() {
        return String.format("%s^%s",
                next("firstName"),
                next("lastName")
        );
    }

    public static String TM() {
        LocalTime time = time();
        return time.format(TIME_FORMATTER);
    }

    public static String SH() {
        return string(between(2, 16), ALLOWED_CHARS);
    }

    public static String ST() {
        return string(between(2, 1024), ALLOWED_CHARS);
    }

    private static String next(String key) {
        return RandomUtils.next(PROPS.get(key));
    }
}
