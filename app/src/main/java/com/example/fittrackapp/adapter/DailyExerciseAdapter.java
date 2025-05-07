package com.example.fittrackapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittrackapp.R;
import com.example.fittrackapp.StartExerciseActivity;
import com.example.fittrackapp.model.DailyExercise;
import com.example.fittrackapp.model.WorkoutDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DailyExerciseAdapter extends RecyclerView.Adapter<DailyExerciseAdapter.ViewHolder> {

    private final List<DailyExercise> exerciseList;
    private final Context context;

    public DailyExerciseAdapter(List<DailyExercise> exerciseList, Context context) {
        this.exerciseList = exerciseList;
        this.context = context;
    }

    @NonNull
    @Override
    public DailyExerciseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercise_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyExerciseAdapter.ViewHolder holder, int position) {
        DailyExercise exercise = exerciseList.get(position);
        String dayLabel = exercise.getDay();
        String fitnessGoal = exercise.getFitnessGoal();
        List<WorkoutDetail> workoutDetails = exercise.getWorkoutDetails();

        if (dayLabel.equalsIgnoreCase("Rest Day")) {
            holder.exerciseTextView.setText("REST DAY");
        } else {
            holder.exerciseTextView.setText("DAY " + dayLabel);
        }
        holder.emojiTextView.setText(dayLabel.equalsIgnoreCase("Rest Day") ? "💤" : "🔥");


        StringBuilder workoutText = new StringBuilder();
        for (WorkoutDetail detail : workoutDetails) {
            workoutText.append(detail.getName())
                    .append(" - Time: ").append(detail.getTime())
                    .append(" - Instruction: ").append(detail.getInstruction())
                    .append("\n");
        }

        holder.itemView.setEnabled(true);
        holder.itemView.setAlpha(1f);
        holder.itemView.setOnClickListener(null);

        SharedPreferences prefs = context.getSharedPreferences("completed_exercises", Context.MODE_PRIVATE);
        boolean isCompleted = prefs.getBoolean(dayLabel, false);

        if (dayLabel.equalsIgnoreCase("Rest Day") || isCompleted) {
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.5f);
        } else {

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, StartExerciseActivity.class);
                intent.putExtra("day", exercise.getDay());

                JSONArray workoutArray = new JSONArray();
                for (WorkoutDetail workout : workoutDetails) {
                    try {
                        JSONObject workoutObject = new JSONObject();
                        workoutObject.put("name", workout.getName());
                        workoutObject.put("time", workout.getTime());
                        workoutObject.put("instruction", workout.getInstruction());
                        workoutArray.put(workoutObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                intent.putExtra("workoutDetails", workoutArray.toString());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseTextView;
        TextView emojiTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseTextView = itemView.findViewById(R.id.exerciseTextView);
            emojiTextView = itemView.findViewById(R.id.emojiTextView);

        }
    }
}
