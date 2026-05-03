package com.raply.tai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

// Ganti nama kelas dari bahasaActivity menjadi PilihBahasaActivity agar sesuai dengan nama file
// Namun, berdasarkan konteks Anda, sepertinya Anda memiliki dua file:
// 1. Sebuah file yang belum Anda tunjukkan (mungkin bernama bahasaActivity.java)
// 2. pilihbahasaActivity.java
// Saya akan asumsikan file pertama bernama bahasaActivity.java
public class bahasaActivity extends AppCompatActivity {

    private Button selectLanguageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bahasa);

        // Menghubungkan variabel Java dengan elemen Button di XML
        // Pastikan ID 'btn_pilih_bahasa' ada di file layout 'activity_bahasa.xml' Anda
        selectLanguageButton = findViewById(R.id.btn_select_language); // Ganti R.id.btn_select_language dengan ID tombol Anda

        // Menetapkan listener untuk menangani klik tombol
        selectLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Membuat Intent untuk pindah dari bahasaActivity ke pilihbahasaActivity
                Intent intent = new Intent(bahasaActivity.this, pilihbahasaActivity.class);
                startActivity(intent);
            }
        });
    }
}
