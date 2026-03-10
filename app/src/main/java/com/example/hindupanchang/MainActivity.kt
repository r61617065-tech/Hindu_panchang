package com.example.hindupanchang

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.hindupanchang.ui.theme.HinduPanchangTheme
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HinduPanchangTheme {
                PanchangApp()
            }
        }
    }
}

@Composable
private fun PanchangApp() {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    var now by remember { mutableStateOf(ZonedDateTime.now(zoneId)) }
    var monthSystem by remember { mutableStateOf(MonthSystem.PURNIMANT) }
    var location by remember { mutableStateOf(LocationInfo(latitude = 28.6139, longitude = 77.2090)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            LocationProvider.getLastKnownLocation(context)?.let { location = it }
        }
    }

    fun updateLocation() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            LocationProvider.getLastKnownLocation(context)?.let { location = it }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(Unit) {
        updateLocation()
        while (true) {
            now = ZonedDateTime.now(zoneId)
            delay(1000)
        }
    }

    val data = PanchangCalculator.calculate(now.toInstant(), zoneId, location, monthSystem)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("हिंदू पंचांग (ऑफ़लाइन)", style = MaterialTheme.typography.headlineSmall)
        Text("स्थान: ${"%.4f".format(location.latitude)}, ${"%.4f".format(location.longitude)}")
        Text("वर्तमान समय: ${now.toLocalDate()} ${now.toLocalTime().withNano(0)}")

        Button(onClick = { updateLocation() }) {
            Text("स्थान रीफ़्रेश करें")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("मास प्रणाली", style = MaterialTheme.typography.titleMedium)
                MonthSystem.entries.forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = monthSystem == it,
                            onClick = { monthSystem = it }
                        )
                        Text(it.labelHindi)
                    }
                }
            }
        }

        PanchangItem("तिथि", data.tithi)
        PanchangItem("नक्षत्र", data.nakshatra)
        PanchangItem("मास", data.maas)
        PanchangItem("ऋतु", data.ritu)
        PanchangItem("संवत्सर", data.samvatsar)
        PanchangItem("विक्रम संवत", data.vikramSamvat.toString())
        PanchangItem("सूर्योदय", data.sunrise)
        PanchangItem("सूर्यास्त", data.sunset)
        PanchangItem("चंद्रोदय", data.moonrise)
        PanchangItem("चंद्रास्त", data.moonset)
        PanchangItem("हिंदू समय", data.hinduTime)

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "नोट: यह शुरुआती वर्शन है। वास्तविक 'Exact' सटीकता के लिए Swiss Ephemeris/JPL जैसे " +
                "खगोलीय डेटा ऑफ़लाइन जोड़ना होगा।",
            style = MaterialTheme.typography.bodySmall
        )

        Text("पढ़ें: 1 घटी = 24 मिनट, 1 पल = 24 सेकंड, 1 विपल = 0.4 सेकंड।")
    }
}

@Composable
private fun PanchangItem(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value)
        }
    }
}

data class LocationInfo(val latitude: Double, val longitude: Double)

enum class MonthSystem(val labelHindi: String) {
    PURNIMANT("पूर्णिमांत"),
    AMAVASYANT("अमावस्यांत")
}

data class PanchangData(
    val tithi: String,
    val nakshatra: String,
    val maas: String,
    val ritu: String,
    val samvatsar: String,
    val vikramSamvat: Int,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val hinduTime: String
)

object PanchangCalculator {
    private val tithiNames = listOf(
        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी", "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
        "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पूर्णिमा/अमावस्या"
    )

    private val nakshatraNames = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु", "पुष्य", "आश्लेषा",
        "मघा", "पूर्वा फाल्गुनी", "उत्तर फाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनूराधा",
        "ज्येष्ठा", "मूल", "पूर्वाषाढ़ा", "उत्तराषाढ़ा", "श्रवण", "धनिष्ठा", "शतभिषा",
        "पूर्वा भाद्रपद", "उत्तर भाद्रपद", "रेवती"
    )

    private val maasNames = listOf(
        "चैत्र", "वैशाख", "ज्येष्ठ", "आषाढ़", "श्रावण", "भाद्रपद", "आश्विन", "कार्तिक", "मार्गशीर्ष", "पौष", "माघ", "फाल्गुन"
    )

    fun calculate(instant: Instant, zoneId: ZoneId, location: LocationInfo, monthSystem: MonthSystem): PanchangData {
        val dateTime = instant.atZone(zoneId)
        val dayOfYear = dateTime.dayOfYear
        val approxMoonAge = (dayOfYear % 30)
        val paksha = if (approxMoonAge < 15) "शुक्ल" else "कृष्ण"
        val tithi = "$paksha ${tithiNames[approxMoonAge % 15]}"

        val nakshatraIndex = ((dayOfYear * 27.0 / 365.2422).toInt()) % 27
        val maasIndex = if (monthSystem == MonthSystem.PURNIMANT) {
            (dateTime.monthValue + 10) % 12
        } else {
            (dateTime.monthValue + 11) % 12
        }
        val maas = maasNames[maasIndex]

        val ritu = when (dateTime.monthValue) {
            1, 2 -> "शिशिर"
            3, 4 -> "वसंत"
            5, 6 -> "ग्रीष्म"
            7, 8 -> "वर्षा"
            9, 10 -> "शरद"
            else -> "हेमंत"
        }

        val vikramSamvat = dateTime.year + 57
        val samvatsar = "विक्रम संवत का वर्ष $vikramSamvat"

        val sunrise = SolarLunarMath.approxSunrise(dateTime, location.latitude, location.longitude)
        val sunset = SolarLunarMath.approxSunset(dateTime, location.latitude, location.longitude)
        val moonrise = SolarLunarMath.approxMoonrise(dateTime, approxMoonAge)
        val moonset = SolarLunarMath.approxMoonset(dateTime, approxMoonAge)

        val hinduTime = toGhatiPalVipal(dateTime.toInstant(), sunrise)

        return PanchangData(
            tithi = tithi,
            nakshatra = nakshatraNames[nakshatraIndex],
            maas = maas,
            ritu = ritu,
            samvatsar = samvatsar,
            vikramSamvat = vikramSamvat,
            sunrise = sunrise.toLocalTime().withNano(0).toString(),
            sunset = sunset.toLocalTime().withNano(0).toString(),
            moonrise = moonrise.toLocalTime().withNano(0).toString(),
            moonset = moonset.toLocalTime().withNano(0).toString(),
            hinduTime = hinduTime
        )
    }

    private fun toGhatiPalVipal(now: Instant, sunrise: ZonedDateTime): String {
        val elapsed = Duration.between(sunrise.toInstant(), now).seconds
        val normalizedSeconds = if (elapsed >= 0) elapsed else elapsed + 24 * 3600
        val ghatiTotal = normalizedSeconds / 1440.0
        val ghati = ghatiTotal.toInt()
        val palTotal = (ghatiTotal - ghati) * 60
        val pal = palTotal.toInt()
        val vipal = ((palTotal - pal) * 60).toInt()
        return "$ghati घटी $pal पल $vipal विपल"
    }
}

object SolarLunarMath {
    fun approxSunrise(dateTime: ZonedDateTime, latitude: Double, longitude: Double): ZonedDateTime {
        val base = dateTime.withHour(6).withMinute(0).withSecond(0).withNano(0)
        val correction = ((longitude / 180.0) * 30).toInt() - ((latitude / 90.0) * 20).toInt()
        return base.plusMinutes(correction.toLong())
    }

    fun approxSunset(dateTime: ZonedDateTime, latitude: Double, longitude: Double): ZonedDateTime {
        val base = dateTime.withHour(18).withMinute(0).withSecond(0).withNano(0)
        val correction = ((longitude / 180.0) * 30).toInt() + ((latitude / 90.0) * 10).toInt()
        return base.plusMinutes(correction.toLong())
    }

    fun approxMoonrise(dateTime: ZonedDateTime, moonAge: Int): ZonedDateTime {
        val riseHour = (6 + moonAge * 24 / 30) % 24
        return dateTime.withHour(riseHour).withMinute(20).withSecond(0).withNano(0)
    }

    fun approxMoonset(dateTime: ZonedDateTime, moonAge: Int): ZonedDateTime {
        val setHour = (18 + moonAge * 24 / 30) % 24
        return dateTime.withHour(setHour).withMinute(10).withSecond(0).withNano(0)
    }
}

object LocationProvider {
    fun getLastKnownLocation(context: android.content.Context): LocationInfo? {
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

        val best = providers.mapNotNull { provider ->
            try {
                locationManager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            }
        }.minByOrNull { it.accuracy }

        return best?.let { LocationInfo(it.latitude, it.longitude) }
    }
}
