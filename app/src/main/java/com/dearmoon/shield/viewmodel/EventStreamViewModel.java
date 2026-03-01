package com.dearmoon.shield.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.dearmoon.shield.database.SystemEvent;
import com.dearmoon.shield.repository.EventRepository;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EventStreamViewModel extends ViewModel {
    private final EventRepository repository;
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>("ALL");
    private final MediatorLiveData<List<SystemEvent>> filteredEvents = new MediatorLiveData<>();
    
    @Inject
    public EventStreamViewModel(EventRepository repository) {
        this.repository = repository;
        
        LiveData<List<SystemEvent>> allEvents = repository.getAllEvents();
        
        filteredEvents.addSource(allEvents, events -> applyFilter(events, currentFilter.getValue()));
        filteredEvents.addSource(currentFilter, filter -> applyFilter(allEvents.getValue(), filter));
    }
    
    private void applyFilter(List<SystemEvent> events, String filter) {
        if (events == null || filter == null) {
            filteredEvents.setValue(new ArrayList<>());
            return;
        }
        
        if ("ALL".equals(filter)) {
            filteredEvents.setValue(events);
            return;
        }
        
        List<SystemEvent> filtered = new ArrayList<>();
        for (SystemEvent event : events) {
            if (filter.equals(event.eventType)) {
                filtered.add(event);
            }
        }
        filteredEvents.setValue(filtered);
    }
    
    public void setFilter(String filter) {
        currentFilter.setValue(filter);
    }
    
    public LiveData<List<SystemEvent>> getFilteredEvents() {
        return filteredEvents;
    }
}
