package com.example.fittrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittrackapp.adapter.DailyExerciseAdapter;
import com.example.fittrackapp.model.DailyExercise;
import com.example.fittrackapp.model.WorkoutDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private List<DailyExercise> exerciseList = new ArrayList<>();
    private DailyExerciseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences("fitness_form_responses", MODE_PRIVATE);

        String rawPlan = sharedPreferences.getString("generated_workout_plan", null);

        if (rawPlan != null) {
            exerciseList = parsePlan(rawPlan);

            adapter = new DailyExerciseAdapter(exerciseList, this);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No workout plan found. Please generate one first!", Toast.LENGTH_LONG).show();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_my_workout);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(ExerciseDashboardActivity.this, HomeDashboardActivity.class));
                    return true;
                } else if (id == R.id.nav_my_workout) {
                    return true;
                } else if (id == R.id.nav_statistics) {
                    startActivity(new Intent(ExerciseDashboardActivity.this, WorkoutGraphActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(ExerciseDashboardActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });


    }

    private List<DailyExercise> parsePlan(String rawPlan) {
        List<DailyExercise> list = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(rawPlan);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject dayData = jsonArray.getJSONObject(i);
                String day = dayData.getString("day");
                JSONArray workoutsArray = dayData.getJSONArray("workouts");

                List<WorkoutDetail> workoutDetails = new ArrayList<>();
                String fitnessGoal = dayData.optString("fitnessGoal", "No goal set");

                if (workoutsArray.length() == 0) {
                    list.add(new DailyExercise("Rest Day", "Rest and recovery", new ArrayList<>()));
                } else {
                    for (int j = 0; j < workoutsArray.length(); j++) {
                        JSONObject workout = workoutsArray.getJSONObject(j);
                        String exerciseName = workout.getString("name");
                        String time = workout.getString("time");
                        String instruction = workout.getString("instruction");

                        workoutDetails.add(new WorkoutDetail(exerciseName, time, instruction));
                    }
                    list.add(new DailyExercise(day, fitnessGoal, workoutDetails));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String rawPlan = sharedPreferences.getString("generated_workout_plan", null);
        if (rawPlan != null) {
            exerciseList = parsePlan(rawPlan);
            adapter = new DailyExerciseAdapter(exerciseList, this);
            recyclerView.setAdapter(adapter);
        }
    }


}
