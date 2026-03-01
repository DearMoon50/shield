package com.dearmoon.shield.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "system_events")
public class SystemEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long timestamp;
    public String eventType;
    public String source;
    public String filePath;
    public String operation;
    public String packageName;
    public String severity;
    public double entropy;
    public double klDivergence;
    public String sprtState;
    public int confidenceScore;
    public String rawData;
    
    public SystemEvent(long timestamp, String eventType, String source) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.source = source;
    }
}
