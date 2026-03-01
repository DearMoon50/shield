package com.dearmoon.shield.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SystemEventDao {
    @Insert
    void insert(SystemEvent event);
    
    @Query("SELECT * FROM system_events ORDER BY timestamp DESC LIMIT 1000")
    LiveData<List<SystemEvent>> getAllEvents();
    
    @Query("SELECT * FROM system_events WHERE eventType = :type ORDER BY timestamp DESC LIMIT 500")
    LiveData<List<SystemEvent>> getEventsByType(String type);
    
    @Query("SELECT * FROM system_events WHERE confidenceScore >= :threshold ORDER BY timestamp DESC")
    LiveData<List<SystemEvent>> getHighRiskEvents(int threshold);
    
    @Query("SELECT * FROM system_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    LiveData<List<SystemEvent>> getEventsSince(long startTime);
    
    @Query("DELETE FROM system_events WHERE timestamp < :cutoffTime")
    void deleteOldEvents(long cutoffTime);
    
    @Query("SELECT COUNT(*) FROM system_events")
    LiveData<Integer> getEventCount();
}
