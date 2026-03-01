package com.dearmoon.shield.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.dearmoon.shield.R;
import com.dearmoon.shield.services.ShieldProtectionService;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlertActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        
        String filePath = getIntent().getStringExtra("filePath");
        int score = getIntent().getIntExtra("score", 0);
        
        TextView tvAlertMessage = findViewById(R.id.tvAlertMessage);
        TextView tvFilePath = findViewById(R.id.tvFilePath);
        Button btnKillSwitch = findViewById(R.id.btnKillSwitch);
        Button btnDismiss = findViewById(R.id.btnDismiss);
        
        tvAlertMessage.setText("RANSOMWARE DETECTED\nConfidence: " + score + "%");
        tvFilePath.setText(filePath);
        
        btnKillSwitch.setOnClickListener(v -> {
            stopAllServices();
            killSuspiciousProcesses();
            finish();
        });
        
        btnDismiss.setOnClickListener(v -> finish());
    }
    
    private void stopAllServices() {
        stopService(new Intent(this, ShieldProtectionService.class));
    }
    
    private void killSuspiciousProcesses() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.killBackgroundProcesses(getPackageName());
        }
    }
}
