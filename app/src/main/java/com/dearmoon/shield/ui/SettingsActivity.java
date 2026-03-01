package com.dearmoon.shield.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.dearmoon.shield.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        prefs = getSharedPreferences("shield_settings", MODE_PRIVATE);
        
        setupEntropyThreshold();
        setupKLThreshold();
        setupConfidenceThreshold();
    }
    
    private void setupEntropyThreshold() {
        SeekBar seekBar = findViewById(R.id.seekBarEntropy);
        TextView textView = findViewById(R.id.tvEntropyValue);
        
        float current = prefs.getFloat("entropy_threshold", 7.5f);
        seekBar.setProgress((int)((current - 6.0f) * 50));
        textView.setText(String.format("%.1f", current));
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = 6.0f + (progress / 50.0f);
                textView.setText(String.format("%.1f", value));
                prefs.edit().putFloat("entropy_threshold", value).apply();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void setupKLThreshold() {
        SeekBar seekBar = findViewById(R.id.seekBarKL);
        TextView textView = findViewById(R.id.tvKLValue);
        
        float current = prefs.getFloat("kl_threshold", 0.1f);
        seekBar.setProgress((int)(current * 500));
        textView.setText(String.format("%.2f", current));
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress / 500.0f;
                textView.setText(String.format("%.2f", value));
                prefs.edit().putFloat("kl_threshold", value).apply();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void setupConfidenceThreshold() {
        SeekBar seekBar = findViewById(R.id.seekBarConfidence);
        TextView textView = findViewById(R.id.tvConfidenceValue);
        
        int current = prefs.getInt("confidence_threshold", 70);
        seekBar.setProgress(current);
        textView.setText(String.valueOf(current));
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
                prefs.edit().putInt("confidence_threshold", progress).apply();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}
