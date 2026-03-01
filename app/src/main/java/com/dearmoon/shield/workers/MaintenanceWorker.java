package com.dearmoon.shield.workers;

import android.content.Context;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.dearmoon.shield.backup.ShadowBackupManager;
import com.dearmoon.shield.database.ShieldDatabase;

public class MaintenanceWorker extends Worker {
    
    public MaintenanceWorker(Context context, WorkerParameters params) {
        super(context, params);
    }
    
    @Override
    public Result doWork() {
        try {
            ShieldDatabase db = ShieldDatabase.getInstance(getApplicationContext());
            db.systemEventDao().deleteOldEvents(System.currentTimeMillis() - 604800000L);
            
            ShadowBackupManager backupManager = new ShadowBackupManager(getApplicationContext());
            backupManager.cleanOldBackups(86400000L);
            
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
