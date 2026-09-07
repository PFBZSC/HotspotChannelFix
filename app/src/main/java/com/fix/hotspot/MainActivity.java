package com.fix.hotspot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends Activity {
    private static final String PREF_NAME = "config";
    private RadioGroup rgBand;
    private RadioButton rb24g;
    private RadioButton rb5g;
    private EditText etChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rgBand = findViewById(R.id.rg_band);
        rb24g = findViewById(R.id.rb_24g);
        rb5g = findViewById(R.id.rb_5g);
        etChannel = findViewById(R.id.et_channel);
        Button btnSave = findViewById(R.id.btn_save);

        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int band = sp.getInt("band", 2); // 默认 5G (SoftApConfiguration.BAND_5GHZ)
        int channel = sp.getInt("channel", 149);

        if (band == 1) {
            rb24g.setChecked(true);
        } else {
            rb5g.setChecked(true);
        }
        etChannel.setText(String.valueOf(channel));

        btnSave.setOnClickListener(v -> saveConfig());
    }

    private void saveConfig() {
        String input = etChannel.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "请输入信道", Toast.LENGTH_SHORT).show();
            return;
        }

        int channel = Integer.parseInt(input);
        int band = rb24g.isChecked() ? 1 : 2; // 1: BAND_2GHZ, 2: BAND_5GHZ

        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit()
          .putInt("band", band)
          .putInt("channel", channel)
          .commit();

        fixPermissions();
        Toast.makeText(this, "保存成功，请开关一次热点", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void fixPermissions() {
        File dataDir = new File(getApplicationInfo().dataDir);
        File prefsDir = new File(dataDir, "shared_prefs");
        File prefFile = new File(prefsDir, PREF_NAME + ".xml");

        dataDir.setExecutable(true, false);
        dataDir.setReadable(true, false);
        prefsDir.setExecutable(true, false);
        prefsDir.setReadable(true, false);
        if (prefFile.exists()) {
            prefFile.setReadable(true, false);
        }
    }
}
