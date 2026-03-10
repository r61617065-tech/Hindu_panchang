package com.example.hindupanchang;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PanchangCalculator {

    public enum MonthSystem {
        PURNIMANT,
        AMAVASYANT
    }

    private static final String[] TITHI_NAMES = new String[]{
            "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी", "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
            "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पूर्णिमा/अमावस्या"
    };

    private static final String[] NAKSHATRA_NAMES = new String[]{
            "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु", "पुष्य", "आश्लेषा",
            "मघा", "पूर्वा फाल्गुनी", "उत्तर फाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनूराधा",
            "ज्येष्ठा", "मूल", "पूर्वाषाढ़ा", "उत्तराषाढ़ा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वा भाद्रपद", "उत्तर भाद्रपद", "रेवती"
    };

    private static final String[] MAAS_NAMES = new String[]{
            "चैत्र", "वैशाख", "ज्येष्ठ", "आषाढ़", "श्रावण", "भाद्रपद", "आश्विन", "कार्तिक", "मार्गशीर्ष", "पौष", "माघ", "फाल्गुन"
    };

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());

    public static PanchangData calculate(ZonedDateTime now, double latitude, double longitude, MonthSystem monthSystem) {
        int dayOfYear = now.getDayOfYear();
        int approxMoonAge = dayOfYear % 30;

        String paksha = approxMoonAge < 15 ? "शुक्ल" : "कृष्ण";
        String tithi = paksha + " " + TITHI_NAMES[approxMoonAge % 15];

        int nakshatraIndex = (int) ((dayOfYear * 27.0 / 365.2422)) % 27;
        int maasIndex = monthSystem == MonthSystem.PURNIMANT
                ? (now.getMonthValue() + 10) % 12
                : (now.getMonthValue() + 11) % 12;

        String ritu;
        int month = now.getMonthValue();
        if (month == 1 || month == 2) {
            ritu = "शिशिर";
        } else if (month == 3 || month == 4) {
            ritu = "वसंत";
        } else if (month == 5 || month == 6) {
            ritu = "ग्रीष्म";
        } else if (month == 7 || month == 8) {
            ritu = "वर्षा";
        } else if (month == 9 || month == 10) {
            ritu = "शरद";
        } else {
            ritu = "हेमंत";
        }

        int vikramSamvat = now.getYear() + 57;
        String samvatsar = "विक्रम संवत वर्ष " + vikramSamvat;

        ZonedDateTime sunrise = SolarLunarMath.approxSunrise(now, latitude, longitude);
        ZonedDateTime sunset = SolarLunarMath.approxSunset(now, latitude, longitude);
        ZonedDateTime moonrise = SolarLunarMath.approxMoonrise(now, approxMoonAge);
        ZonedDateTime moonset = SolarLunarMath.approxMoonset(now, approxMoonAge);

        return new PanchangData(
                tithi,
                NAKSHATRA_NAMES[nakshatraIndex],
                MAAS_NAMES[maasIndex],
                ritu,
                samvatsar,
                vikramSamvat,
                sunrise.format(TIME_FORMATTER),
                sunset.format(TIME_FORMATTER),
                moonrise.format(TIME_FORMATTER),
                moonset.format(TIME_FORMATTER),
                toGhatiPalVipal(now, sunrise)
        );
    }

    private static String toGhatiPalVipal(ZonedDateTime now, ZonedDateTime sunrise) {
        long elapsedSec = Duration.between(sunrise, now).getSeconds();
        long normalized = elapsedSec >= 0 ? elapsedSec : elapsedSec + 24L * 3600L;

        double ghatiTotal = normalized / 1440.0;
        int ghati = (int) ghatiTotal;
        double palTotal = (ghatiTotal - ghati) * 60.0;
        int pal = (int) palTotal;
        int vipal = (int) ((palTotal - pal) * 60.0);

        return ghati + " घटी " + pal + " पल " + vipal + " विपल";
    }
}
