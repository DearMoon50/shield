package com.dearmoon.shield.monitoring;

import android.content.Context;
import com.dearmoon.shield.database.ShieldDatabase;
import com.dearmoon.shield.database.SystemEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerformanceLogger {
    private final ShieldDatabase database;
    private final ExecutorService executor;
    
    public PerformanceLogger(Context context) {
        this.database = ShieldDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void logMetrics(float cpuPercent, long memoryMb, long heapMb) {
        executor.execute(() -> {
            SystemEvent event = new SystemEvent(System.currentTimeMillis(), "PERFORMANCE", "PerformanceMonitor");
            event.severity = "LOW";
            event.confidenceScore = 0;
            event.rawData = String.format("cpu:%.1f%%,memory:%dMB,heap:%dMB", cpuPercent, memoryMb, heapMb);
            database.systemEventDao().insert(event);
        });
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
