package com.example.fittrackapp.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittrackapp.R;
import com.example.fittrackapp.model.DailyExercise;
import com.example.fittrackapp.model.WorkoutDetail;

import java.util.List;

public class DailyExercisePreviewAdapter extends RecyclerView.Adapter<DailyExercisePreviewAdapter.ViewHolder> {
    private List<DailyExercise> exercises;

    public DailyExercisePreviewAdapter(List<DailyExercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_day, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyExercise item = exercises.get(position);

        if (item.getDay().equals("Rest Day")) {
            holder.dayTitle.setVisibility(View.GONE);
            holder.exerciseText.setVisibility(View.GONE);
            holder.restDayImage.setVisibility(View.VISIBLE); // Show rest image
        } else {
            holder.restDayImage.setVisibility(View.GONE); // Hide image
            holder.dayTitle.setVisibility(View.VISIBLE);
            holder.exerciseText.setVisibility(View.VISIBLE);

            holder.dayTitle.setText("DAY " + item.getDay());

            StringBuilder builder = new StringBuilder();
            for (WorkoutDetail detail : item.getWorkoutDetails()) {
                builder.append("• ").append(detail.getName()).append("\n");
            }
            holder.exerciseText.setText(builder.toString().trim());
        }
    }



    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTitle, exerciseText;
        ImageView restDayImage;

        ViewHolder(View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.dayTitle);
            exerciseText = itemView.findViewById(R.id.exerciseText);
            restDayImage = itemView.findViewById(R.id.restDayImage);

        }
    }
}
