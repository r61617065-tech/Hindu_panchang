package com.example.hindupanchang;

import java.time.ZonedDateTime;

public class SolarLunarMath {

    public static ZonedDateTime approxSunrise(ZonedDateTime dateTime, double latitude, double longitude) {
        ZonedDateTime base = dateTime.withHour(6).withMinute(0).withSecond(0).withNano(0);
        int correction = (int) ((longitude / 180.0) * 30.0) - (int) ((latitude / 90.0) * 20.0);
        return base.plusMinutes(correction);
    }

    public static ZonedDateTime approxSunset(ZonedDateTime dateTime, double latitude, double longitude) {
        ZonedDateTime base = dateTime.withHour(18).withMinute(0).withSecond(0).withNano(0);
        int correction = (int) ((longitude / 180.0) * 30.0) + (int) ((latitude / 90.0) * 10.0);
        return base.plusMinutes(correction);
    }

    public static ZonedDateTime approxMoonrise(ZonedDateTime dateTime, int moonAge) {
        int riseHour = (6 + moonAge * 24 / 30) % 24;
        return dateTime.withHour(riseHour).withMinute(20).withSecond(0).withNano(0);
    }

    public static ZonedDateTime approxMoonset(ZonedDateTime dateTime, int moonAge) {
        int setHour = (18 + moonAge * 24 / 30) % 24;
        return dateTime.withHour(setHour).withMinute(10).withSecond(0).withNano(0);
    }
}
