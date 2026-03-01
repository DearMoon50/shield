package com.dearmoon.shield.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.dearmoon.shield.database.SystemEvent;
import com.dearmoon.shield.repository.EventRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DashboardViewModel extends ViewModel {
    private final EventRepository repository;
    private final MutableLiveData<Long> detectionLatency = new MutableLiveData<>(0L);
    private final MutableLiveData<Float> cpuUsage = new MutableLiveData<>(0f);
    private final MutableLiveData<Long> memoryUsage = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> heapUsage = new MutableLiveData<>(0L);
    
    @Inject
    public DashboardViewModel(EventRepository repository) {
        this.repository = repository;
    }
    
    public LiveData<List<SystemEvent>> getRecentEvents() {
        return repository.getEventsSince(System.currentTimeMillis() - 3600000);
    }
    
    public LiveData<List<SystemEvent>> getHighRiskEvents() {
        return repository.getHighRiskEvents(70);
    }
    
    public LiveData<Integer> getTotalEventCount() {
        return repository.getEventCount();
    }
    
    public LiveData<Integer> getActiveThreats() {
        return Transformations.map(getHighRiskEvents(), events -> events != null ? events.size() : 0);
    }
    
    public void updateDetectionLatency(long latencyMs) {
        detectionLatency.postValue(latencyMs);
    }
    
    public LiveData<Long> getDetectionLatency() {
        return detectionLatency;
    }
    
    public void updatePerformanceMetrics(float cpu, long memory, long heap) {
        cpuUsage.postValue(cpu);
        memoryUsage.postValue(memory);
        heapUsage.postValue(heap);
    }
    
    public LiveData<Float> getCpuUsage() {
        return cpuUsage;
    }
    
    public LiveData<Long> getMemoryUsage() {
        return memoryUsage;
    }
    
    public LiveData<Long> getHeapUsage() {
        return heapUsage;
    }
}
