package com.example.fittrackapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittrackapp.ExerciseDashboardActivity;
import com.example.fittrackapp.R;

import java.util.List;
import java.util.Map;

public class WorkoutDaysAdapter extends RecyclerView.Adapter<WorkoutDaysAdapter.ViewHolder> {

    private Context context;
    private List<Map<String, Object>> workoutDays;

    public WorkoutDaysAdapter(Context context, List<Map<String, Object>> workoutDays) {
        this.context = context;
        this.workoutDays = workoutDays;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> dayData = workoutDays.get(position);
        int day = ((Double) dayData.get("day")).intValue(); // Gson parses numbers as Double
        String type = (String) dayData.get("type");

        holder.dayTextView.setText("Day " + day + " - " + (type.equals("rest") ? "Rest" : "Workout"));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExerciseDashboardActivity.class);
            intent.putExtra("dayIndex", position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workoutDays.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
        }
    }
}
