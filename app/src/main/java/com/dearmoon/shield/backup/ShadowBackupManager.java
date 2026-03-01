package com.dearmoon.shield.backup;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShadowBackupManager {
    private static final String TAG = "ShadowBackup";
    private final File backupDir;
    private final Map<String, Long> fileSnapshots = new HashMap<>();
    
    public ShadowBackupManager(Context context) {
        backupDir = new File(context.getFilesDir(), "shadow_backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }
    
    public boolean createSnapshot(File originalFile) {
        if (!originalFile.exists() || !originalFile.canRead()) return false;
        
        try {
            String backupName = originalFile.getName() + "_" + System.currentTimeMillis();
            File backupFile = new File(backupDir, backupName);
            
            copyFile(originalFile, backupFile);
            fileSnapshots.put(originalFile.getAbsolutePath(), System.currentTimeMillis());
            
            Log.i(TAG, "Snapshot created: " + backupName);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Snapshot failed", e);
            return false;
        }
    }
    
    public boolean restoreFile(String originalPath) {
        File[] backups = backupDir.listFiles((dir, name) -> 
            name.startsWith(new File(originalPath).getName()));
        
        if (backups == null || backups.length == 0) return false;
        
        File latestBackup = backups[0];
        for (File backup : backups) {
            if (backup.lastModified() > latestBackup.lastModified()) {
                latestBackup = backup;
            }
        }
        
        try {
            copyFile(latestBackup, new File(originalPath));
            Log.i(TAG, "File restored: " + originalPath);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Restore failed", e);
            return false;
        }
    }
    
    private void copyFile(File src, File dst) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
    
    public File[] listBackups() {
        return backupDir.listFiles();
    }
    
    public void cleanOldBackups(long maxAgeMs) {
        File[] backups = backupDir.listFiles();
        if (backups == null) return;
        
        long cutoff = System.currentTimeMillis() - maxAgeMs;
        for (File backup : backups) {
            if (backup.lastModified() < cutoff) {
                backup.delete();
            }
        }
    }
}
