package com.example.fittrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.model.WorkoutDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private com.example.fittrackapp.adapter.WorkoutHistoryAdapter adapter;
    private ArrayList<WorkoutDetail> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_history);

        historyListView = findViewById(R.id.historyListView);
        historyList = new ArrayList<>();

        loadWorkoutHistory();

        adapter = new com.example.fittrackapp.adapter.WorkoutHistoryAdapter(this, historyList);
        historyListView.setAdapter(adapter);

        MaterialButton buttonGraph = findViewById(R.id.buttonGraph);
        MaterialButton buttonWorkHistory = findViewById(R.id.buttonWorkHistory);

        buttonGraph.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutHistoryActivity.this, WorkoutGraphActivity.class);
            startActivity(intent);
        });


        buttonWorkHistory.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutHistoryActivity .this, WorkoutHistoryActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_statistics);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(WorkoutHistoryActivity.this, HomeDashboardActivity.class));

                    return true;
                } else if (id == R.id.nav_my_workout) {
                    startActivity(new Intent(WorkoutHistoryActivity.this, ExerciseDashboardActivity.class));
                    return true;
                } else if (id == R.id.nav_statistics) {
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(WorkoutHistoryActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;

            }
        });
    }

    private void loadWorkoutHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("WorkoutHistory", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String day = entry.getKey();
            String historyJson = (String) entry.getValue();

            try {
                JSONArray workoutArray = new JSONArray(historyJson);
                for (int i = 0; i < workoutArray.length(); i++) {
                    JSONObject workoutObject = workoutArray.getJSONObject(i);
                    String name = workoutObject.getString("name");
                    String time = workoutObject.getString("time");
                    String instruction = workoutObject.getString("instruction");

                    historyList.add(new WorkoutDetail( name, time, instruction));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading history", Toast.LENGTH_SHORT).show();
            }
        }

        if (historyList.isEmpty()) {
            Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show();
        }
    }

}
