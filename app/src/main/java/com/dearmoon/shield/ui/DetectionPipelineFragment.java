package com.dearmoon.shield.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.dearmoon.shield.R;
import com.dearmoon.shield.database.SystemEvent;
import com.dearmoon.shield.viewmodel.DashboardViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetectionPipelineFragment extends Fragment {
    private DashboardViewModel viewModel;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_pipeline, container, false);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        
        TextView tvPipelineStatus = view.findViewById(R.id.tvPipelineStatus);
        TextView tvLastDetection = view.findViewById(R.id.tvLastDetection);
        
        viewModel.getRecentEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                SystemEvent latest = events.get(0);
                String pipeline = String.format("File Event → Entropy(%.2f) → KL(%.3f) → SPRT(%s) → Score(%d)",
                    latest.entropy, latest.klDivergence, latest.sprtState, latest.confidenceScore);
                tvPipelineStatus.setText(pipeline);
                tvLastDetection.setText("Last: " + latest.filePath);
            }
        });
        
        return view;
    }
}
