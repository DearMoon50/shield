package com.dearmoon.shield.collectors;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import com.dearmoon.shield.database.ShieldDatabase;
import com.dearmoon.shield.database.SystemEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FifoHoneyfileManager {
    private static final String TAG = "FifoHoneyfile";
    private final ShieldDatabase database;
    private final ExecutorService executor;
    private final List<Future<?>> monitorTasks = new ArrayList<>();
    private volatile boolean running = true;
    
    public FifoHoneyfileManager(Context context) {
        this.database = ShieldDatabase.getInstance(context);
        this.executor = Executors.newCachedThreadPool();
    }
    
    public void createFifoHoneyfiles(String[] directories) {
        String[] honeyNames = {
            ".passwords.txt", ".wallet.dat", ".private_key.pem", 
            ".credentials.json", ".backup.zip"
        };
        
        for (String dir : directories) {
            File directory = new File(dir);
            if (!directory.exists() || !directory.isDirectory()) continue;
            
            for (String name : honeyNames) {
                File fifoPath = new File(directory, name);
                if (createFifo(fifoPath.getAbsolutePath())) {
                    monitorTasks.add(executor.submit(() -> monitorFifo(fifoPath)));
                }
            }
        }
    }
    
    private boolean createFifo(String path) {
        try {
            File file = new File(path);
            if (file.exists()) file.delete();
            
            Os.mkfifo(path, OsConstants.S_IRUSR | OsConstants.S_IWUSR | 
                           OsConstants.S_IRGRP | OsConstants.S_IROTH);
            Log.i(TAG, "Created FIFO honeyfile: " + path);
            return true;
        } catch (ErrnoException e) {
            Log.e(TAG, "Failed to create FIFO: " + path, e);
            return false;
        }
    }
    
    private void monitorFifo(File fifoPath) {
        while (running && fifoPath.exists()) {
            try (FileInputStream fis = new FileInputStream(fifoPath)) {
                byte[] buffer = new byte[1024];
                int bytesRead = fis.read(buffer);
                
                if (bytesRead > 0) {
                    logHoneyfileAccess(fifoPath.getAbsolutePath(), bytesRead);
                }
            } catch (IOException e) {
                if (running) {
                    Log.w(TAG, "FIFO read interrupted: " + fifoPath.getName());
                }
                break;
            }
        }
    }
    
    private void logHoneyfileAccess(String path, int bytesRead) {
        SystemEvent event = new SystemEvent(System.currentTimeMillis(), "HONEYFILE_ACCESS", "FifoHoneyfile");
        event.filePath = path;
        event.severity = "CRITICAL";
        event.confidenceScore = 90;
        event.rawData = "bytes_read:" + bytesRead;
        database.systemEventDao().insert(event);
        Log.w(TAG, "⚠️ HONEYFILE ACCESSED: " + path + " (" + bytesRead + " bytes)");
    }
    
    public void shutdown() {
        running = false;
        for (Future<?> task : monitorTasks) {
            task.cancel(true);
        }
        executor.shutdownNow();
    }
}
