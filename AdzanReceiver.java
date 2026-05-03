package com.raply.tai;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AdzanReceiver extends BroadcastReceiver {

    // MediaPlayer static agar bisa diakses untuk diberhentikan
    private static MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Cek jika aksi adalah memberhentikan adzan
        if ("STOP_ADZAN".equals(action)) {
            stopAdzan();
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(100); // Hapus notifikasi setelah tombol berhenti diklik
            return;
        }

        String prayerName = intent.getStringExtra("prayerName");
        if (prayerName == null) prayerName = "Shalat";

        // 1. Munculkan Toast
        Toast.makeText(context, "Allahu Akbar! Waktunya Shalat " + prayerName, Toast.LENGTH_LONG).show();

        // 2. Munculkan Notifikasi dengan Tombol Berhenti
        showNotification(context, prayerName);

        // 3. Bunyikan Suara Adzan MP3
        playAdzanMp3(context);

        // 4. Getarkan HP
        vibratePhone(context);
    }

    private void showNotification(Context context, String prayerName) {
        String channelId = "adzan_channel";

        // Intent untuk membuka aplikasi
        Intent mainIntent = new Intent(context, LandakActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Intent untuk tombol BERHENTI
        Intent stopIntent = new Intent(context, AdzanReceiver.class);
        stopIntent.setAction("STOP_ADZAN");

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 1, stopIntent, flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Pengingat Adzan", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Panggilan Shalat " + prayerName)
                .setContentText("Hayya 'alas-shalah! Waktunya shalat " + prayerName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(Color.parseColor("#2E7D32"))
                .setAutoCancel(true)
                .setContentIntent(mainPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "BERHENTI", stopPendingIntent) // Tombol Berhenti
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(100, builder.build());
            }
        } else {
            notificationManager.notify(100, builder.build());
        }
    }

    private void playAdzanMp3(Context context) {
        // Hentikan jika adzan sebelumnya masih bunyi
        stopAdzan();

        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.adzan_audio);
            if (mediaPlayer != null) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            // Cadangan jika MP3 gagal: Bunyikan Alarm Default
            playDefaultAlarm(context);
        }
    }

    private void stopAdzan() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void playDefaultAlarm(Context context) {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alert == null) alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            android.media.Ringtone r = RingtoneManager.getRingtone(context, alert);
            if (r != null) r.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void vibratePhone(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 500, 200, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }
}