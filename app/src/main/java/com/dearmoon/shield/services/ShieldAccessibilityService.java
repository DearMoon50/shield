package com.dearmoon.shield.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.dearmoon.shield.database.ShieldDatabase;
import com.dearmoon.shield.database.SystemEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShieldAccessibilityService extends AccessibilityService {
    private static final String TAG = "AccessibilityService";
    private ShieldDatabase database;
    private ExecutorService executor;
    
    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        setServiceInfo(info);
        
        database = ShieldDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        Log.i(TAG, "Accessibility service connected");
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        
        String packageName = event.getPackageName().toString();
        String className = event.getClassName() != null ? event.getClassName().toString() : "";
        int eventType = event.getEventType();
        
        if (isLockScreenAbuse(packageName, className) || isOverlayAbuse(eventType, packageName)) {
            logAccessibilityEvent(packageName, className, eventType, "HIGH");
        } else {
            logAccessibilityEvent(packageName, className, eventType, "LOW");
        }
    }
    
    private boolean isLockScreenAbuse(String packageName, String className) {
        return className.contains("LockScreen") || className.contains("Keyguard");
    }
    
    private boolean isOverlayAbuse(int eventType, String packageName) {
        return eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && 
               !packageName.equals(getPackageName());
    }
    
    private void logAccessibilityEvent(String packageName, String className, int eventType, String severity) {
        executor.execute(() -> {
            SystemEvent event = new SystemEvent(System.currentTimeMillis(), "ACCESSIBILITY", "AccessibilityService");
            event.packageName = packageName;
            event.rawData = className;
            event.severity = severity;
            event.confidenceScore = "HIGH".equals(severity) ? 50 : 10;
            database.systemEventDao().insert(event);
        });
    }
    
    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted");
    }
    
    @Override
    public void onDestroy() {
        if (executor != null) executor.shutdown();
        super.onDestroy();
    }
}
