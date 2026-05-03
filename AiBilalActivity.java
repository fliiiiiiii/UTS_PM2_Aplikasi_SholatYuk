package com.raply.tai;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class AiBilalActivity extends AppCompatActivity {

    private TextView tvAiMessage;
    private EditText etUserQuery;
    private Button btnTalkToAi;
    private ImageButton btnVoiceInput;
    private TextToSpeech tts;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_bilal);

        tvAiMessage = findViewById(R.id.tv_ai_message);
        etUserQuery = findViewById(R.id.et_user_query);
        btnTalkToAi = findViewById(R.id.btn_talk_ai);
        btnVoiceInput = findViewById(R.id.btn_voice_input);

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(new Locale("id", "ID"));
            }
        });

        btnTalkToAi.setOnClickListener(v -> prosesPertanyaanLokal());
        btnVoiceInput.setOnClickListener(v -> mulaiBicara());
    }

    private void mulaiBicara() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Silakan bicara, AI Bilal mendengarkan...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "HP Anda tidak mendukung Voice Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etUserQuery.setText(result.get(0));
            prosesPertanyaanLokal();
        }
    }

    private void prosesPertanyaanLokal() {
        String query = etUserQuery.getText().toString().toLowerCase().trim();
        if (query.isEmpty()) return;

        String jawaban = "";

        // ================================================================
        // LOGIKA TANYA JAWAB ISLAMI (100+ KEYWORDS MAPPING)
        // ================================================================

        // 1. SALAM & SAPAAN
        if (query.contains("assalamu") || query.contains("halo") || query.contains("hai")) {
            jawaban = "Wa'alaikumussalam Warahmatullahi Wabarakatuh! Saya AI Bilal. Ada yang bisa saya bantu terkait ibadah hari ini?";
        }

        // 2. SHALAT WAJIB (WAKTU & RAKAAT)
        else if (query.contains("subuh")) {
            jawaban = "Shalat Subuh itu dua rakaat. Waktunya dari fajar shadiq sampai matahari terbit. Shalat ini disaksikan langsung oleh para malaikat.";
        } else if (query.contains("zhuhur") || query.contains("dzuhur")) {
            jawaban = "Shalat Zhuhur itu empat rakaat. Waktunya saat matahari mulai tergelincir ke barat sampai bayangan benda sama panjang dengan tingginya.";
        } else if (query.contains("ashar")) {
            jawaban = "Shalat Ashar itu empat rakaat. Waktunya berakhir saat matahari terbenam. Jangan menunda Ashar karena ini adalah waktu yang kritis.";
        } else if (query.contains("maghrib")) {
            jawaban = "Shalat Maghrib itu tiga rakaat. Waktunya sangat singkat, dari matahari terbenam sampai awan merah hilang.";
        } else if (query.contains("isya")) {
            jawaban = "Shalat Isya itu empat rakaat. Waktunya paling panjang, dari awan merah hilang sampai terbit fajar.";
        }

        // 3. RUKUN ISLAM & IMAN
        else if (query.contains("rukun islam")) {
            jawaban = "Rukun Islam ada 5: Syahadat, Shalat, Puasa Ramadhan, Zakat, dan Haji bagi yang mampu.";
        } else if (query.contains("rukun iman")) {
            jawaban = "Rukun Iman ada 6: Iman kepada Allah, Malaikat, Kitab-Kitab, Rasul-Rasul, Hari Kiamat, serta Qada dan Qadar.";
        } else if (query.contains("rukun shalat")) {
            jawaban = "Rukun shalat ada 13, mulai dari niat, takbiratul ihram, sampai salam secara tertib.";
        }

        // 4. MALAIKAT & TUGASNYA
        else if (query.contains("malaikat")) {
            jawaban = "Malaikat diciptakan dari cahaya. 10 yang wajib diketahui: Jibril, Mikail, Israfil, Izrail, Munkar, Nakir, Raqib, Atid, Malik, dan Ridwan.";
        } else if (query.contains("jibril")) {
            jawaban = "Malaikat Jibril bertugas menyampaikan wahyu dari Allah kepada para Rasul.";
        } else if (query.contains("izrail")) {
            jawaban = "Malaikat Izrail bertugas mencabut nyawa seluruh makhluk hidup.";
        } else if (query.contains("ridwan")) {
            jawaban = "Malaikat Ridwan bertugas menjaga pintu Surga.";
        }

        // 5. NABI & RASUL
        else if (query.contains("nabi") || query.contains("rasul")) {
            jawaban = "Ada 25 Nabi dan Rasul yang wajib diketahui. Nabi pertama adalah Adam alaihissalam, dan penutup para Nabi adalah Muhammad shallallahu alaihi wasallam.";
        } else if (query.contains("muhammad")) {
            jawaban = "Nabi Muhammad adalah khatamul anbiya atau penutup para nabi. Beliau lahir di Makkah dan membawa risalah Islam untuk seluruh alam.";
        } else if (query.contains("musa")) {
            jawaban = "Nabi Musa diberikan mukjizat tongkat yang bisa membelah laut merah dan menerima kitab Taurat.";
        }

        // 6. KITAB-KITAB ALLAH
        else if (query.contains("kitab")) {
            jawaban = "Ada 4 Kitab: Zabur kepada Nabi Dawud, Taurat kepada Nabi Musa, Injil kepada Nabi Isa, dan Al-Quran kepada Nabi Muhammad.";
        } else if (query.contains("alquran") || query.contains("al quran")) {
            jawaban = "Al-Quran adalah kalam Allah yang diturunkan kepada Nabi Muhammad sebagai mukjizat terbesar dan pedoman hidup manusia.";
        }

        // 7. PENTINGNYA SHALAT & DOSA
        else if (query.contains("penting") || query.contains("kenapa shalat")) {
            jawaban = "Shalat adalah tiang agama. Shalat adalah amalan pertama yang dihisab di akhirat. Barangsiapa menjaga shalat, maka agamanya tegak.";
        } else if (query.contains("dosa") || query.contains("maksiat")) {
            jawaban = "Dosa adalah penghalang rezeki. Segeralah bertaubat dengan istighfar. Allah Maha Pengampun bagi hamba yang bersungguh-sungguh.";
        }

        // 8. SHALAT SUNNAH
        else if (query.contains("tahajjud")) {
            jawaban = "Shalat Tahajjud dilakukan di sepertiga malam terakhir. Ini adalah waktu terbaik untuk memohon doa agar dikabulkan Allah.";
        } else if (query.contains("dhuha")) {
            jawaban = "Shalat Dhuha minimal dua rakaat di pagi hari. Manfaatnya adalah untuk melancarkan rezeki dan pengganti sedekah persendian.";
        }

        // 9. AKHLAQ & ADAB
        else if (query.contains("orang tua") || query.contains("ibu") || query.contains("ayah")) {
            jawaban = "Berbakti kepada orang tua adalah kewajiban besar setelah tauhid. Ridha Allah terletak pada ridha kedua orang tua.";
        } else if (query.contains("sabar")) {
            jawaban = "Sabar itu pahalanya tanpa batas. Allah bersama orang-orang yang sabar menghadapi cobaan hidup.";
        } else if (query.contains("ikhlas")) {
            jawaban = "Ikhlas adalah melakukan kebaikan semata-mata hanya karena Allah, bukan karena ingin dipuji manusia.";
        }

        // 10. HARI KIAMAT & AKHIRAT
        else if (query.contains("kiamat") || query.contains("akhirat")) {
            jawaban = "Hari Kiamat adalah pasti. Tanda-tandanya sudah banyak terlihat. Siapkanlah bekal iman dan amal saleh sebanyak-banyaknya.";
        } else if (query.contains("surga")) {
            jawaban = "Surga adalah tempat kemuliaan bagi orang bertaqwa. Di dalamnya terdapat kenikmatan yang belum pernah dilihat mata manusia.";
        } else if (query.contains("neraka")) {
            jawaban = "Neraka adalah tempat siksaan bagi mereka yang ingkar. Mari berlindung kepada Allah dari panasnya api neraka.";
        }

        // 11. REZEKI & MOTIVASI
        else if (query.contains("rezeki") || query.contains("kaya")) {
            jawaban = "Rezeki sudah diatur oleh Allah. Carilah dengan jalan yang halal, perbanyak sedekah, dan jangan lupa shalat Dhuha.";
        } else if (query.contains("malas")) {
            jawaban = "Lawan rasa malas dengan berdzikir. Syaitan suka membisikkan rasa malas agar kita jauh dari Allah. Ayo semangat!";
        }

        // 12. PERTANYAAN UMUM
        else if (query.contains("siapa kamu") || query.contains("penciptamu")) {
            jawaban = "Saya AI Bilal, asisten pintarmu untuk belajar agama. Saya diciptakan untuk membantu mengingatkanmu pada kebaikan.";
        } else if (query.contains("terima kasih") || query.contains("syukron")) {
            jawaban = "Sama-sama saudaraku! Semoga Allah memberkahimu dan memudahkan urusanmu.";
        }

        // JAWABAN DEFAULT
        if (jawaban.isEmpty()) {
            jawaban = "Maaf, AI Bilal belum mempelajari itu. Coba tanyakan hal lain seperti waktu shalat, rukun iman, atau nama malaikat.";
        }

        // TAMPILKAN TEKS & BERSIKAN INPUT
        tvAiMessage.setText(jawaban);
        etUserQuery.setText("");

        // JAWAB PAKAI SUARA (VOICE OUTPUT)
        if (tts != null) {
            tts.speak(jawaban, TextToSpeech.QUEUE_FLUSH, null, "ID_BILAL");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}