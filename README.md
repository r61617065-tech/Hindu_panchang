# हिंदू पंचांग (Android Studio Project)

यह प्रोजेक्ट **बिलकुल शुरुआत से** बनाया गया Android Studio (Kotlin + Jetpack Compose) ऐप है।

## अभी क्या शामिल है
- ऑफलाइन UI (हिंदी)  
- लाइव RTC (हर सेकंड अपडेट)  
- घटी/पल/विपल समय रूपांतरण  
- पूर्णिमांत / अमावस्यांत स्विच  
- डिवाइस लोकेशन आधारित गणना (Last known GPS/Network location)  
- तिथि, नक्षत्र, मास, ऋतु, संवत्सर, विक्रम संवत, सूर्योदय/सूर्यास्त/चंद्रोदय/चंद्रास्त

## महत्वपूर्ण सटीकता नोट
आपने "Exact" पर जोर दिया है। खगोलीय रूप से सच में exact परिणाम के लिए ये जोड़ना जरूरी होगा:
1. Swiss Ephemeris या JPL DE ephemeris data (offline assets)
2. आयनांश (Lahiri आदि) के सही मॉडल
3. सूर्योदय/चंद्रोदय के लिए atmospheric refraction + elevation correction
4. तिथि/नक्षत्र boundary crossing के लिए high precision longitude solver

> इस शुरुआती संस्करण में गणनाएँ संरचना (architecture) दिखाने के लिए approximate रखी गई हैं।

## Android Studio में चलाने के स्टेप्स
1. Android Studio खोलें
2. `Open` करके यह folder चुनें
3. Gradle sync होने दें
4. Emulator/Device पर Run करें

## आगे exact engine का सुझाव
- `PanchangCalculator` को इंटरफ़ेस बनाकर `ExactPanchangCalculator` लागू करें
- ephemeris data को `app/src/main/assets/ephemeris/` में रखें
- unit tests में ज्ञात पंचांग तिथियों से cross-verify करें
