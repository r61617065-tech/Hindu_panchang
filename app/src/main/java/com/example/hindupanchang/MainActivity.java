package com.example.hindupanchang;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

    private TextView tvLocation;
    private TextView tvRtc;
    private TextView tvTithi;
    private TextView tvNakshatra;
    private TextView tvMaas;
    private TextView tvRitu;
    private TextView tvSamvatsar;
    private TextView tvVikram;
    private TextView tvSunrise;
    private TextView tvSunset;
    private TextView tvMoonrise;
    private TextView tvMoonset;
    private TextView tvHinduTime;

    private PanchangCalculator.MonthSystem monthSystem = PanchangCalculator.MonthSystem.PURNIMANT;
    private double latitude = 28.6139;
    private double longitude = 77.2090;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshPanchang();
            handler.postDelayed(this, 1000);
        }
    };

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    loadLastKnownLocation();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupControls();
        requestOrLoadLocation();
    }

    private void bindViews() {
        tvLocation = findViewById(R.id.tvLocation);
        tvRtc = findViewById(R.id.tvRtc);
        tvTithi = findViewById(R.id.tvTithi);
        tvNakshatra = findViewById(R.id.tvNakshatra);
        tvMaas = findViewById(R.id.tvMaas);
        tvRitu = findViewById(R.id.tvRitu);
        tvSamvatsar = findViewById(R.id.tvSamvatsar);
        tvVikram = findViewById(R.id.tvVikram);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvMoonrise = findViewById(R.id.tvMoonrise);
        tvMoonset = findViewById(R.id.tvMoonset);
        tvHinduTime = findViewById(R.id.tvHinduTime);
    }

    private void setupControls() {
        RadioGroup rgMonthSystem = findViewById(R.id.rgMonthSystem);
        Button btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
        Button btnReadPage = findViewById(R.id.btnReadPage);

        rgMonthSystem.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPurnimant) {
                monthSystem = PanchangCalculator.MonthSystem.PURNIMANT;
            } else if (checkedId == R.id.rbAmavasyant) {
                monthSystem = PanchangCalculator.MonthSystem.AMAVASYANT;
            }
            refreshPanchang();
        });

        btnRefreshLocation.setOnClickListener(v -> requestOrLoadLocation());
        btnReadPage.setOnClickListener(v -> startActivity(new Intent(this, ReadActivity.class)));
    }

    private void requestOrLoadLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            loadLastKnownLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadLastKnownLocation() {
        LocationManager locationManager = getSystemService(LocationManager.class);
        if (locationManager == null) {
            return;
        }

        Location bestLocation = null;
        String[] providers = new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};

        for (String provider : providers) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null && (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy())) {
                    bestLocation = location;
                }
            } catch (SecurityException ignored) {
                // handled by permission check
            }
        }

        if (bestLocation != null) {
            latitude = bestLocation.getLatitude();
            longitude = bestLocation.getLongitude();
        }

        refreshPanchang();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(ticker);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(ticker);
    }

    private void refreshPanchang() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        tvRtc.setText("वर्तमान समय: " + now.format(dateTimeFormat));
        tvLocation.setText(String.format(Locale.getDefault(), "स्थान: %.4f, %.4f", latitude, longitude));

        PanchangData data = PanchangCalculator.calculate(now, latitude, longitude, monthSystem);

        tvTithi.setText(data.tithi);
        tvNakshatra.setText(data.nakshatra);
        tvMaas.setText(data.maas);
        tvRitu.setText(data.ritu);
        tvSamvatsar.setText(data.samvatsar);
        tvVikram.setText(String.valueOf(data.vikramSamvat));
        tvSunrise.setText(data.sunrise);
        tvSunset.setText(data.sunset);
        tvMoonrise.setText(data.moonrise);
        tvMoonset.setText(data.moonset);
        tvHinduTime.setText(data.hinduTime);
    }
}
