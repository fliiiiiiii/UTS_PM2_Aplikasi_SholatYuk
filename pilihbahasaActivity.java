package com.raply.tai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class pilihbahasaActivity extends AppCompatActivity {

    private MaterialCardView cardIndonesia;
    private MaterialCardView cardUsa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pilihbahasa);

        // Menghubungkan variabel dengan ID elemen di layout XML
        cardIndonesia = findViewById(R.id.card_indonesia);
        cardUsa = findViewById(R.id.card_usa);

        // Membuat listener untuk kedua kartu
        View.OnClickListener languageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Di sini Anda bisa menambahkan logika untuk menyimpan bahasa yang dipilih jika perlu
                // Misalnya menggunakan SharedPreferences

                // Pindah ke LandakActivity
                Intent intent = new Intent(pilihbahasaActivity.this, LandakActivity.class);
                startActivity(intent);

                // (Opsional) Menutup activity ini agar pengguna tidak bisa kembali ke pemilihan bahasa
                // dengan menekan tombol "back"
                finish();
            }
        };

        // Menetapkan listener yang sama ke kedua kartu
        cardIndonesia.setOnClickListener(languageClickListener);
        cardUsa.setOnClickListener(languageClickListener);

        // Kode boilerplate untuk EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
