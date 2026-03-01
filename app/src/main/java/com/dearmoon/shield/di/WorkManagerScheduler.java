package com.dearmoon.shield.di;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.dearmoon.shield.workers.MaintenanceWorker;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class WorkManagerScheduler {
    
    @Inject
    public WorkManagerScheduler(@ApplicationContext Context context) {
        scheduleMaintenanceWork(context);
    }
    
    private void scheduleMaintenanceWork(Context context) {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
            MaintenanceWorker.class, 24, TimeUnit.HOURS)
            .setConstraints(new Constraints.Builder().build())
            .build();
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "shield_maintenance", ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }
}
