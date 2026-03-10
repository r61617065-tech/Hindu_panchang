# हिंदू पंचांग Android ऐप (Java + XML)

यह प्रोजेक्ट Android Studio के लिए **Java और XML** में तैयार किया गया है (Kotlin/Compose नहीं)।

## प्रोजेक्ट फोल्डर स्ट्रक्चर

```text
Hindu_panchang/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/hindupanchang/
        │   ├── MainActivity.java
        │   ├── ReadActivity.java
        │   ├── PanchangCalculator.java
        │   ├── PanchangData.java
        │   └── SolarLunarMath.java
        └── res/
            ├── layout/
            │   ├── activity_main.xml
            │   └── activity_read.xml
            ├── values/
            │   ├── colors.xml
            │   ├── strings.xml
            │   └── themes.xml
            ├── mipmap-anydpi-v26/
            │   ├── ic_launcher.xml
            │   └── ic_launcher_round.xml
            └── drawable/
                └── ic_launcher_foreground.xml
```

## ऐप में क्या-क्या है
- पूरी हिंदी UI
- लाइव RTC (हर सेकंड)
- पूर्णिमांत / अमावस्यांत स्विच
- लोकेशन परमिशन + Last known location
- तिथि, नक्षत्र, मास, ऋतु, संवत्सर, विक्रम संवत
- सूर्योदय, सूर्यास्त, चंद्रोदय, चंद्रास्त
- घटी-पल-विपल समय
- “पढ़ें” पेज (हिंदी में समझ)

## आपके `ic_launcher.png` को कैसे लगाना है

आपने बताया आपके पास `ic_launcher.png` है। Android Studio में ऐसे लगाएं:

1. Android Studio में **app > res** पर right click करें.
2. **New > Image Asset** चुनें.
3. Asset Type = **Image**.
4. Path में अपना `ic_launcher.png` चुनें.
5. Name: `ic_launcher` और round icon भी generate होने दें.
6. Finish.

इसके बाद manifest में `@mipmap/ic_launcher` अपने आप काम करेगा।

## महत्वपूर्ण नोट (Exact precision)
यह वर्शन अभी बेस/approximation इंजन पर है।
सच में “Exact” पंचांग के लिए आगे Swiss Ephemeris या JPL offline data integrate करना होगा।
