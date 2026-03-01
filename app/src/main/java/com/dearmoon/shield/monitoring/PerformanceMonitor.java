package com.dearmoon.shield.monitoring;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PerformanceMonitor {
    private final Context context;
    private final Handler handler;
    private final Runnable monitorTask;
    private final PerformanceLogger logger;
    private MetricsCallback callback;
    private long lastCpuTime = 0;
    private long lastAppTime = 0;
    private int sampleCount = 0;
    
    public interface MetricsCallback {
        void onMetricsUpdated(float cpuPercent, long memoryMb, long heapMb);
    }
    
    public PerformanceMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
        this.monitorTask = this::collectMetrics;
        this.logger = new PerformanceLogger(context);
    }
    
    public void setCallback(MetricsCallback callback) {
        this.callback = callback;
    }
    
    public void start() {
        handler.post(monitorTask);
    }
    
    public void stop() {
        handler.removeCallbacks(monitorTask);
        logger.shutdown();
    }
    
    private void collectMetrics() {
        float cpuPercent = getCpuUsage();
        long memoryMb = getMemoryUsage();
        long heapMb = getHeapUsage();
        
        if (callback != null) {
            callback.onMetricsUpdated(cpuPercent, memoryMb, heapMb);
        }
        
        if (++sampleCount % 15 == 0) {
            logger.logMetrics(cpuPercent, memoryMb, heapMb);
        }
        
        handler.postDelayed(monitorTask, 2000);
    }
    
    private float getCpuUsage() {
        try {
            long currentCpuTime = getTotalCpuTime();
            long currentAppTime = getAppCpuTime();
            
            if (lastCpuTime == 0) {
                lastCpuTime = currentCpuTime;
                lastAppTime = currentAppTime;
                return 0f;
            }
            
            long cpuDelta = currentCpuTime - lastCpuTime;
            long appDelta = currentAppTime - lastAppTime;
            
            lastCpuTime = currentCpuTime;
            lastAppTime = currentAppTime;
            
            if (cpuDelta == 0) return 0f;
            return (float) (appDelta * 100.0 / cpuDelta);
        } catch (Exception e) {
            return 0f;
        }
    }
    
    private long getTotalCpuTime() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
        String line = reader.readLine();
        reader.close();
        
        String[] tokens = line.split("\\s+");
        long total = 0;
        for (int i = 1; i < tokens.length; i++) {
            total += Long.parseLong(tokens[i]);
        }
        return total;
    }
    
    private long getAppCpuTime() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/proc/self/stat"));
        String line = reader.readLine();
        reader.close();
        
        String[] tokens = line.split("\\s+");
        long utime = Long.parseLong(tokens[13]);
        long stime = Long.parseLong(tokens[14]);
        return utime + stime;
    }
    
    private long getMemoryUsage() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.getMemoryInfo(memInfo);
            return (memInfo.totalMem - memInfo.availMem) / (1024 * 1024);
        }
        return 0;
    }
    
    private long getHeapUsage() {
        return Debug.getNativeHeapAllocatedSize() / (1024 * 1024);
    }
}
