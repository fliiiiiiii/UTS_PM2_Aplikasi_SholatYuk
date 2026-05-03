package com.raply.tai;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LandakActivity extends AppCompatActivity {

    private TextView tvCountdown, tvServerTime, tvCity;
    private TextView tvShubuhToday, tvZhuhurToday, tvAsharToday, tvMaghribToday, tvIsyaToday;
    private TextView tvDateHijri;
    private ProgressBar pbSyncTime;
    private Button btnOpenAi;
    private ImageButton btnMap;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;
    private RequestQueue requestQueue;

    private double lastLat = 0, lastLon = 0;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                pbSyncTime.setVisibility(View.GONE);
                Boolean fineLocation = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocation != null && fineLocation) {
                    getCurrentLocationAndFetchPrayerTimes();
                } else {
                    Toast.makeText(this, "Izin lokasi ditolak, jadwal mungkin tidak akurat.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landak);

        requestQueue = Volley.newRequestQueue(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        initFirebaseMessaging();

        // FIX: Perbaikan tombol Map agar tidak menyangkut
        btnMap.setOnClickListener(v -> {
            if (lastLat != 0 && lastLon != 0) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(Lokasi Saya)", lastLat, lastLon, lastLat, lastLon);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");

                // Flag ini memastikan Maps terbuka sebagai dokumen baru dan tidak merusak tumpukan activity saat Back
                mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    // Fallback jika Maps tidak ada
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lastLat + "," + lastLon)));
                }
            } else {
                Toast.makeText(this, "Mencari lokasi...", Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            }
        });

        btnOpenAi.setOnClickListener(v -> {
            startActivity(new Intent(LandakActivity.this, AiBilalActivity.class));
        });

        View rootLayout = findViewById(R.id.header_bg);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::checkLocationPermission, 500);
        startServerTimeUpdater();
        checkBatteryOptimization();
        checkExactAlarmPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Memastikan countdown langsung berjalan kembali saat user kembali ke aplikasi
        if (tvShubuhToday != null && !tvShubuhToday.getText().toString().equals("--:--")) {
            startCountdownUpdater();
        }
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle("Optimasi Adzan")
                        .setMessage("Agar notifikasi Adzan tidak tertunda, harap setel penggunaan baterai aplikasi ini ke 'Tidak Dibatasi'.")
                        .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Nanti", null)
                        .show();
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Izin Alarm Presisi")
                        .setMessage("Aplikasi memerlukan izin untuk menjadwalkan Adzan tepat waktu.")
                        .setPositiveButton("Beri Izin", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .show();
            }
        }
    }

    private void initFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    Log.d("FCM_TOKEN", task.getResult());
                });
    }

    private void initializeViews() {
        tvCountdown = findViewById(R.id.tv_countdown);
        tvServerTime = findViewById(R.id.tv_server_time);
        tvCity = findViewById(R.id.tv_city);
        tvDateHijri = findViewById(R.id.tv_date_hijri);
        pbSyncTime = findViewById(R.id.pb_sync_time);
        btnMap = findViewById(R.id.btn_map);
        btnOpenAi = findViewById(R.id.btn_open_ai);

        setupPrayerRow(R.id.row_subuh, "Subuh", "shubuh");
        setupPrayerRow(R.id.row_zhuhur, "Zhuhur", "zhuhur");
        setupPrayerRow(R.id.row_ashar, "Ashar", "ashar");
        setupPrayerRow(R.id.row_maghrib, "Maghrib", "maghrib");
        setupPrayerRow(R.id.row_isya, "Isya", "isya");

        // Nilai awal agar tidak error saat kalkulasi pertama
        tvShubuhToday.setText("--:--");
        tvZhuhurToday.setText("--:--");
        tvAsharToday.setText("--:--");
        tvMaghribToday.setText("--:--");
        tvIsyaToday.setText("--:--");
    }

    private void setupPrayerRow(int includeId, String labelName, String prayerKey) {
        View row = findViewById(includeId);
        if (row != null) {
            TextView label = row.findViewById(R.id.tv_label);
            TextView time = row.findViewById(R.id.tv_time);
            if (label != null) label.setText(labelName);
            if (time != null) {
                switch (prayerKey) {
                    case "shubuh": tvShubuhToday = time; break;
                    case "zhuhur": tvZhuhurToday = time; break;
                    case "ashar": tvAsharToday = time; break;
                    case "maghrib": tvMaghribToday = time; break;
                    case "isya": tvIsyaToday = time; break;
                }
            }
        }
    }

    private void fetchPrayerTimesFromApi(double latitude, double longitude) {
        lastLat = latitude; lastLon = longitude;
        String url = String.format(Locale.US, "https://api.aladhan.com/v1/timings?latitude=%f&longitude=%f&method=11", latitude, longitude);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject data = response.getJSONObject("data");
                        JSONObject timings = data.getJSONObject("timings");
                        JSONObject hijriInfo = data.getJSONObject("date").getJSONObject("hijri");

                        String hijriDate = String.format("%s %s %s H",
                                hijriInfo.getString("day"),
                                hijriInfo.getJSONObject("month").getString("en"),
                                hijriInfo.getString("year"));

                        updateUiWithApiData(timings.getString("Fajr"), timings.getString("Dhuhr"),
                                timings.getString("Asr"), timings.getString("Maghrib"),
                                timings.getString("Isha"), hijriDate);
                    } catch (JSONException e) { e.printStackTrace(); }
                }, error -> Log.e("API_ERROR", error.toString()));
        requestQueue.add(jsonObjectRequest);
    }

    private void updateUiWithApiData(String shubuh, String zhuhur, String ashar, String maghrib, String isya, String hijriDate) {
        tvShubuhToday.setText(shubuh);
        tvZhuhurToday.setText(zhuhur);
        tvAsharToday.setText(ashar);
        tvMaghribToday.setText(maghrib);
        tvIsyaToday.setText(isya);
        tvDateHijri.setText(hijriDate);

        scheduleAdzan("Subuh", shubuh);
        scheduleAdzan("Zhuhur", zhuhur);
        scheduleAdzan("Ashar", ashar);
        scheduleAdzan("Maghrib", maghrib);
        scheduleAdzan("Isya", isya);

        startCountdownUpdater();
    }

    private void scheduleAdzan(String prayerName, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date prayerTime = sdf.parse(timeStr);
            Calendar calendar = Calendar.getInstance();
            Calendar now = Calendar.getInstance();

            if (prayerTime != null) {
                calendar.setTime(prayerTime);
                calendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

                if (calendar.after(now)) {
                    Intent intent = new Intent(this, AdzanReceiver.class);
                    intent.putExtra("prayerName", prayerName);
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, prayerName.hashCode(), intent, flags);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                    if (alarmManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void checkLocationPermission() {
        pbSyncTime.setVisibility(View.VISIBLE);
        String[] permissions = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS} :
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFetchPrayerTimes();
        } else {
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void getCurrentLocationAndFetchPrayerTimes() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            pbSyncTime.setVisibility(View.GONE);
            if (location != null) {
                getCityNameFromCoordinates(location.getLatitude(), location.getLongitude());
                fetchPrayerTimesFromApi(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void getCityNameFromCoordinates(double latitude, double longitude) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String cityName = addresses.get(0).getSubAdminArea() != null ? addresses.get(0).getSubAdminArea() : addresses.get(0).getLocality();
                    new Handler(Looper.getMainLooper()).post(() -> tvCity.setText(cityName));
                }
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    private void startCountdownUpdater() {
        if (countdownRunnable != null) countdownHandler.removeCallbacks(countdownRunnable);
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdownLogic();
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void updateCountdownLogic() {
        Map<String, String> prayerTimes = new LinkedHashMap<>();
        prayerTimes.put("Subuh", tvShubuhToday.getText().toString());
        prayerTimes.put("Zhuhur", tvZhuhurToday.getText().toString());
        prayerTimes.put("Ashar", tvAsharToday.getText().toString());
        prayerTimes.put("Maghrib", tvMaghribToday.getText().toString());
        prayerTimes.put("Isya", tvIsyaToday.getText().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date now = new Date();
        Date nextPrayerTime = null;

        try {
            for (Map.Entry<String, String> entry : prayerTimes.entrySet()) {
                if (entry.getValue().equals("--:--")) continue;
                Date pTime = sdf.parse(entry.getValue());
                Calendar pCal = Calendar.getInstance();
                pCal.setTime(pTime);
                Calendar today = Calendar.getInstance();
                pCal.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                if (pCal.getTime().after(now)) {
                    nextPrayerTime = pCal.getTime();
                    break;
                }
            }

            if (nextPrayerTime == null && !prayerTimes.get("Subuh").equals("--:--")) {
                Date sTime = sdf.parse(prayerTimes.get("Subuh"));
                Calendar sCal = Calendar.getInstance();
                sCal.setTime(sTime);
                sCal.add(Calendar.DAY_OF_MONTH, 1);
                nextPrayerTime = sCal.getTime();
            }

            if (nextPrayerTime != null) {
                long diff = nextPrayerTime.getTime() - now.getTime();
                long h = (diff / (1000 * 60 * 60)) % 24;
                long m = (diff / (1000 * 60)) % 60;
                long s = (diff / 1000) % 60;
                tvCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
            }
        } catch (ParseException e) { tvCountdown.setText("00:00:00"); }
    }

    private void startServerTimeUpdater() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvServerTime.setText("Waktu Lokal: " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (countdownRunnable != null) countdownHandler.removeCallbacks(countdownRunnable);
    }
}