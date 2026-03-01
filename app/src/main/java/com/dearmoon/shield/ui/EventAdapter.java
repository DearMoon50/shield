package com.dearmoon.shield.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.dearmoon.shield.R;
import com.dearmoon.shield.database.SystemEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventAdapter extends ListAdapter<SystemEvent, EventAdapter.EventViewHolder> {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    
    public EventAdapter() {
        super(new DiffUtil.ItemCallback<SystemEvent>() {
            @Override
            public boolean areItemsTheSame(@NonNull SystemEvent oldItem, @NonNull SystemEvent newItem) {
                return oldItem.id == newItem.id;
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull SystemEvent oldItem, @NonNull SystemEvent newItem) {
                return oldItem.timestamp == newItem.timestamp && 
                       oldItem.eventType.equals(newItem.eventType);
            }
        });
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
    
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTimestamp;
        private final TextView tvEventType;
        private final TextView tvDetails;
        private final TextView tvScore;
        private final View severityIndicator;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvEventType = itemView.findViewById(R.id.tvEventType);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvScore = itemView.findViewById(R.id.tvScore);
            severityIndicator = itemView.findViewById(R.id.severityIndicator);
        }
        
        public void bind(SystemEvent event) {
            tvTimestamp.setText(DATE_FORMAT.format(new Date(event.timestamp)));
            tvEventType.setText(event.eventType);
            
            StringBuilder details = new StringBuilder();
            if (event.filePath != null) details.append(event.filePath).append("\n");
            if (event.operation != null) details.append("Op: ").append(event.operation).append("\n");
            if (event.entropy > 0) details.append(String.format("Entropy: %.2f | ", event.entropy));
            if (event.klDivergence > 0) details.append(String.format("KL: %.3f | ", event.klDivergence));
            if (event.sprtState != null) details.append("SPRT: ").append(event.sprtState);
            
            tvDetails.setText(details.toString().trim());
            tvScore.setText(String.valueOf(event.confidenceScore));
            
            int color = getSeverityColor(event.severity);
            severityIndicator.setBackgroundColor(color);
            tvScore.setTextColor(color);
        }
        
        private int getSeverityColor(String severity) {
            if (severity == null) return Color.GRAY;
            switch (severity) {
                case "CRITICAL": return Color.RED;
                case "HIGH": return Color.parseColor("#FF6600");
                case "MEDIUM": return Color.parseColor("#FFA500");
                case "LOW": return Color.parseColor("#4CAF50");
                default: return Color.GRAY;
            }
        }
    }
}
