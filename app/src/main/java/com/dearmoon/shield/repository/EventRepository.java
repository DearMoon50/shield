package com.dearmoon.shield.repository;

import androidx.lifecycle.LiveData;
import com.dearmoon.shield.database.SystemEvent;
import com.dearmoon.shield.database.SystemEventDao;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventRepository {
    private final SystemEventDao dao;
    
    @Inject
    public EventRepository(SystemEventDao dao) {
        this.dao = dao;
    }
    
    public LiveData<List<SystemEvent>> getAllEvents() {
        return dao.getAllEvents();
    }
    
    public LiveData<List<SystemEvent>> getEventsSince(long timestamp) {
        return dao.getEventsSince(timestamp);
    }
    
    public LiveData<List<SystemEvent>> getEventsByType(String eventType) {
        return dao.getEventsByType(eventType);
    }
    
    public LiveData<List<SystemEvent>> getHighRiskEvents(int minScore) {
        return dao.getHighRiskEvents(minScore);
    }
    
    public LiveData<Integer> getEventCount() {
        return dao.getEventCount();
    }
}
