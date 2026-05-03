package com.raply.tai;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";

    /**
     * Dipanggil saat token baru dihasilkan (instal ulang atau hapus data).
     * Token ini digunakan untuk mengirim pesan ke perangkat ini.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // Di sini Anda bisa mengirim token ke database server Anda jika diperlukan
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 1. Tangani jika ada payload Notifikasi
        if (remoteMessage.getNotification() != null) {
            tampilkanNotifikasi(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }

        // 2. Tangani jika ada payload Data (biasanya digunakan untuk kustomisasi lebih lanjut)
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String message = data.get("message");
            if (title != null && message != null) {
                tampilkanNotifikasi(title, message);
            }
        }
    }

    private void tampilkanNotifikasi(String title, String message) {
        String channelId = "notif_shalat";

        // Intent untuk membuka LandakActivity saat notifikasi diklik
        Intent intent = new Intent(this, LandakActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Buat Notification Channel untuk Android Oreo ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Jadwal Shalat & Informasi",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi dari AI Bilal");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            notificationManager.createNotificationChannel(channel);
        }

        // Membangun notifikasi dengan gaya mewah
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder) // Ikon notifikasi
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.parseColor("#2E7D32")) // Hijau Emerald Mewah
                .setAutoCancel(true) // Hilang saat diklik
                .setContentIntent(pendingIntent) // Aksi saat diklik
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Menggunakan System.currentTimeMillis() agar setiap notifikasi tidak saling menimpa
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}