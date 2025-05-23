package com.example.fittrackapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.fittrackapp.R;
import com.example.fittrackapp.model.WorkoutDetail;

import java.util.List;

public class WorkoutHistoryAdapter extends ArrayAdapter<WorkoutDetail> {

    public WorkoutHistoryAdapter(Context context, List<WorkoutDetail> workouts) {
        super(context, 0, workouts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WorkoutDetail workout = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_workout_history, parent, false);
        }

        TextView nameText = convertView.findViewById(R.id.nameTextView);
        TextView timeText = convertView.findViewById(R.id.timeTextView);
        TextView dateTimeText= convertView.findViewById(R.id.textDateTime);

        nameText.setText(workout.getName());
        timeText.setText("Time: " + workout.getTime());
        dateTimeText.setText("Date: " + workout.getDateTime());


        return convertView;
    }
}
