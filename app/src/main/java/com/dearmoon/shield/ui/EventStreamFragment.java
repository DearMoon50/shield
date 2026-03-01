package com.dearmoon.shield.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dearmoon.shield.R;
import com.dearmoon.shield.viewmodel.EventStreamViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EventStreamFragment extends Fragment {
    private EventStreamViewModel viewModel;
    private EventAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_stream, container, false);
        viewModel = new ViewModelProvider(this).get(EventStreamViewModel.class);
        
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEvents);
        Spinner spinnerFilter = view.findViewById(R.id.spinnerFilter);
        
        adapter = new EventAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        String[] filters = {"ALL", "DETECTION", "ACCESSIBILITY", "FILE_SYSTEM", "NETWORK"};
        spinnerFilter.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, filters));
        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                viewModel.setFilter(filters[position]);
            }
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        viewModel.getFilteredEvents().observe(getViewLifecycleOwner(), events -> adapter.submitList(events));
        
        return view;
    }
}
