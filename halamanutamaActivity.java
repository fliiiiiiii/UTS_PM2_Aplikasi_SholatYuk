package com.raply.tai; // Nama package sudah disesuaikan dengan proyek Anda

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class halamanutamaActivity extends AppCompatActivity {

    // Durasi splash screen dalam milidetik (misal: 3000ms = 3 detik)
    private static final int SPLASH_SCREEN_TIMEOUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halamanutama);

        // Gunakan Handler untuk menunda perpindahan ke activity berikutnya
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Buat Intent untuk memulai activity berikutnya
                // MENGARAHKAN KE HALAMAN PEMILIHAN BAHASA SETELAH SPLASH SCREEN
                Intent intent = new Intent(halamanutamaActivity.this, bahasaActivity.class);
                startActivity(intent);

                // Tutup activity splash screen ini agar tidak bisa kembali dengan tombol "back"
                finish();
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }
}
