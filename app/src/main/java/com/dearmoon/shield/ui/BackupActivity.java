package com.dearmoon.shield.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dearmoon.shield.R;
import com.dearmoon.shield.backup.ShadowBackupManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupActivity extends AppCompatActivity {
    private ShadowBackupManager backupManager;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        
        backupManager = new ShadowBackupManager(this);
        listView = findViewById(R.id.listViewBackups);
        Button btnCleanup = findViewById(R.id.btnCleanup);
        
        loadBackups();
        
        btnCleanup.setOnClickListener(v -> {
            backupManager.cleanOldBackups(86400000);
            loadBackups();
            Toast.makeText(this, "Old backups cleaned", Toast.LENGTH_SHORT).show();
        });
        
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = adapter.getItem(position);
            Toast.makeText(this, "Selected: " + fileName, Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadBackups() {
        File[] backups = backupManager.listBackups();
        List<String> names = new ArrayList<>();
        
        if (backups != null) {
            for (File backup : backups) {
                names.add(backup.getName());
            }
        }
        
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
    }
}
