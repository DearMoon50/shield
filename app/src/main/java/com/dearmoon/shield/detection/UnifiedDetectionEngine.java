package com.dearmoon.shield.detection;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.dearmoon.shield.alert.AlertManager;
import com.dearmoon.shield.data.FileSystemEvent;
import com.dearmoon.shield.database.ShieldDatabase;
import com.dearmoon.shield.database.SystemEvent;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnifiedDetectionEngine {
    private static final String TAG = "UnifiedDetectionEngine";

    private final Context context;
    private final EntropyAnalyzer entropyAnalyzer;
    private final KLDivergenceCalculator klCalculator;
    private final SPRTDetector sprtDetector;
    private final ShieldDatabase database;
    private final AlertManager alertManager;

    private final HandlerThread detectionThread;
    private final Handler detectionHandler;

    private final ConcurrentLinkedQueue<String> recentModifications = new ConcurrentLinkedQueue<>();
    private long lastModificationTime = 0;
    private int modificationsInWindow = 0;
    private static final long TIME_WINDOW_MS = 1000;

    public UnifiedDetectionEngine(Context context, AlertManager alertManager) {
        this.context = context;
        this.alertManager = alertManager;
        this.entropyAnalyzer = new EntropyAnalyzer();
        this.klCalculator = new KLDivergenceCalculator();
        this.sprtDetector = new SPRTDetector();
        this.database = ShieldDatabase.getInstance(context);

        detectionThread = new HandlerThread("DetectionThread");
        detectionThread.start();
        detectionHandler = new Handler(detectionThread.getLooper());
    }

    public void processFileEvent(FileSystemEvent event) {
        detectionHandler.post(() -> analyzeFileEvent(event));
    }

    private void analyzeFileEvent(FileSystemEvent event) {
        long startTime = System.currentTimeMillis();
        try {
            String operation = event.toJSON().optString("operation", "");
            String filePath = event.toJSON().optString("filePath", "");

            Log.d(TAG, "Analyzing file event: " + operation + " on " + filePath);

            // Only analyze modifications
            if (!operation.equals("MODIFY") && !operation.equals("CLOSE_WRITE") && !operation.equals("CREATE")) {
                Log.d(TAG, "Skipping operation: " + operation);
                return;
            }

            File file = new File(filePath);
            if (!file.exists() || file.length() < 100) return;

            updateModificationRate();

            double entropy = entropyAnalyzer.calculateEntropy(file);
            logIntermediateStep(filePath, "ENTROPY_CALCULATED", entropy, 0, "");
            
            double klDivergence = klCalculator.calculateDivergence(file);
            logIntermediateStep(filePath, "KL_CALCULATED", entropy, klDivergence, "");
            
            if (entropy == 0.0) return;

            double modRate = (double) modificationsInWindow / (TIME_WINDOW_MS / 1000.0);
            SPRTDetector.SPRTState sprtState = sprtDetector.addObservation(modRate);
            logIntermediateStep(filePath, "SPRT_UPDATED", entropy, klDivergence, sprtState.name());
            
            int confidenceScore = calculateConfidenceScore(entropy, klDivergence, sprtState);
            long latency = System.currentTimeMillis() - startTime;

            logToDatabase(filePath, operation, entropy, klDivergence, sprtState, confidenceScore, latency);

            DetectionResult result = new DetectionResult(
                    entropy, klDivergence, sprtState.name(), confidenceScore, filePath);

            if (result.isHighRisk()) {
                Log.w(TAG, "HIGH RISK DETECTED: " + result.toJSON().toString());
                alertManager.showHighRiskAlert(filePath, confidenceScore);
            }

            // Reset SPRT if decision reached
            if (sprtState != SPRTDetector.SPRTState.CONTINUE) {
                sprtDetector.reset();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing file event", e);
        }
    }
    
    private void logIntermediateStep(String filePath, String step, double entropy, double kl, String sprt) {
        SystemEvent event = new SystemEvent(System.currentTimeMillis(), "PIPELINE_STEP", "DetectionEngine");
        event.filePath = filePath;
        event.operation = step;
        event.entropy = entropy;
        event.klDivergence = kl;
        event.sprtState = sprt;
        event.severity = "LOW";
        detectionHandler.post(() -> database.systemEventDao().insert(event));
    }

    private void updateModificationRate() {
        long currentTime = System.currentTimeMillis();
        recentModifications.add(String.valueOf(currentTime));

        while (!recentModifications.isEmpty()) {
            long oldTime = Long.parseLong(recentModifications.peek());
            if (currentTime - oldTime > TIME_WINDOW_MS) {
                recentModifications.poll();
            } else {
                break;
            }
        }

        modificationsInWindow = recentModifications.size();
        lastModificationTime = currentTime;
    }

    private int calculateConfidenceScore(double entropy, double klDivergence,
            SPRTDetector.SPRTState sprtState) {
        int score = 0;

        if (entropy > 7.8)
            score += 40;
        else if (entropy > 7.5)
            score += 30;
        else if (entropy > 7.0)
            score += 20;
        else if (entropy > 6.0)
            score += 10;

        if (klDivergence < 0.05)
            score += 30;
        else if (klDivergence < 0.1)
            score += 20;
        else if (klDivergence < 0.2)
            score += 10;

        if (sprtState == SPRTDetector.SPRTState.ACCEPT_H1)
            score += 30;
        else if (sprtState == SPRTDetector.SPRTState.CONTINUE)
            score += 10;

        return Math.min(score, 100);
    }

    private void logToDatabase(String filePath, String operation, double entropy, 
                               double klDivergence, SPRTDetector.SPRTState sprtState, 
                               int confidenceScore, long latency) {
        SystemEvent event = new SystemEvent(System.currentTimeMillis(), "DETECTION", "UnifiedDetectionEngine");
        event.filePath = filePath;
        event.operation = operation;
        event.entropy = entropy;
        event.klDivergence = klDivergence;
        event.sprtState = sprtState.name();
        event.confidenceScore = confidenceScore;
        event.severity = confidenceScore >= 70 ? "CRITICAL" : confidenceScore >= 50 ? "HIGH" : "MEDIUM";
        event.rawData = "latency_ms:" + latency;
        
        detectionHandler.post(() -> database.systemEventDao().insert(event));
        
        Log.i(TAG, String.format("Detection: entropy=%.2f kl=%.3f sprt=%s score=%d latency=%dms",
            entropy, klDivergence, sprtState, confidenceScore, latency));
    }

    public void shutdown() {
        detectionThread.quitSafely();
    }
}
