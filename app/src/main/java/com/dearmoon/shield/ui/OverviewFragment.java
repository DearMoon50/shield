package com.dearmoon.shield.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.dearmoon.shield.R;
import com.dearmoon.shield.monitoring.PerformanceMonitor;
import com.dearmoon.shield.viewmodel.DashboardViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OverviewFragment extends Fragment {
    private DashboardViewModel viewModel;
    private PerformanceMonitor performanceMonitor;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        
        TextView tvTotalEvents = view.findViewById(R.id.tvTotalEvents);
        TextView tvActiveThreats = view.findViewById(R.id.tvActiveThreats);
        TextView tvLatency = view.findViewById(R.id.tvLatency);
        TextView tvCpu = view.findViewById(R.id.tvCpu);
        TextView tvMemory = view.findViewById(R.id.tvMemory);
        TextView tvHeap = view.findViewById(R.id.tvHeap);
        
        viewModel.getTotalEventCount().observe(getViewLifecycleOwner(), count -> 
            tvTotalEvents.setText(String.valueOf(count != null ? count : 0)));
        
        viewModel.getActiveThreats().observe(getViewLifecycleOwner(), threats -> 
            tvActiveThreats.setText(String.valueOf(threats != null ? threats : 0)));
        
        viewModel.getDetectionLatency().observe(getViewLifecycleOwner(), latency -> 
            tvLatency.setText(latency + " ms"));
        
        viewModel.getCpuUsage().observe(getViewLifecycleOwner(), cpu -> 
            tvCpu.setText(String.format("%.1f%%", cpu)));
        
        viewModel.getMemoryUsage().observe(getViewLifecycleOwner(), memory -> 
            tvMemory.setText(memory + " MB"));
        
        viewModel.getHeapUsage().observe(getViewLifecycleOwner(), heap -> 
            tvHeap.setText(heap + " MB"));
        
        performanceMonitor = new PerformanceMonitor(requireContext());
        performanceMonitor.setCallback((cpu, memory, heap) -> 
            viewModel.updatePerformanceMetrics(cpu, memory, heap));
        performanceMonitor.start();
        
        return view;
    }
    
    @Override
    public void onDestroyView() {
        if (performanceMonitor != null) {
            performanceMonitor.stop();
        }
        super.onDestroyView();
    }
}
