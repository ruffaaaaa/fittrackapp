package com.example.fittrackapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView textUsername, textWorkoutDays, textTotalMinutes, textLastWorkout;
    private Button buttonSetName;
    private SharedPreferences namePrefs;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        textUsername = findViewById(R.id.textUsername);
        textWorkoutDays = findViewById(R.id.textWorkoutDays);
        textTotalMinutes = findViewById(R.id.textTotalMinutes);
        textLastWorkout = findViewById(R.id.textLastWorkout);
        buttonSetName = findViewById(R.id.buttonSetName);

        namePrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String savedName = namePrefs.getString(KEY_USERNAME, "Your name");
        textUsername.setText(savedName);

        buttonSetName.setOnClickListener(v -> showSetNameDialog());

        Button buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(v -> {
            deleteAllSharedPreferences();

            Toast.makeText(ProfileActivity.this, "All data has been reset.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadWorkoutStats();
        Button buttonGenerate = findViewById(R.id.buttonGenerate);
        Button buttonForm = findViewById(R.id.buttonForm);

        buttonGenerate.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CreateWorkoutPlanActivity.class);
            startActivity(intent);
        });

        buttonForm.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FitnessFormActivity.class);
            startActivity(intent);
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(ProfileActivity.this, HomeDashboardActivity.class));
                    return true;
                } else if (id == R.id.nav_my_workout) {
                    startActivity(new Intent(ProfileActivity.this, ExerciseDashboardActivity.class));
                    return true;
                } else if (id == R.id.nav_statistics) {
                    startActivity(new Intent(ProfileActivity.this, WorkoutGraphActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });
    }

    private void showSetNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Username");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Set", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                saveUsername(newName);
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Reset", (dialog, which) -> {
            saveUsername("Your Username");
            Toast.makeText(this, "Username reset to default", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void saveUsername(String name) {
        SharedPreferences.Editor editor = namePrefs.edit();
        editor.putString(KEY_USERNAME, name);
        editor.apply();
        textUsername.setText(name);
    }

    private void loadWorkoutStats() {
        SharedPreferences workoutPrefs = getSharedPreferences("WorkoutHistory", MODE_PRIVATE);
        Map<String, ?> allEntries = workoutPrefs.getAll();

        int completedDays = 0;
        int totalSeconds = 0;
        String latestWorkoutDay = null;
        long latestTimestamp = 0;

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String day = entry.getKey();
            String workoutJson = (String) entry.getValue();
            completedDays++;

            try {
                JSONArray workoutArray = new JSONArray(workoutJson);
                for (int i = 0; i < workoutArray.length(); i++) {
                    JSONObject workout = workoutArray.getJSONObject(i);
                    String time = workout.getString("time");

                    String numeric = time.replaceAll("[^0-9]", "");
                    if (!numeric.isEmpty()) {
                        totalSeconds += Integer.parseInt(numeric);
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                try {
                    Date parsedDate = sdf.parse(day);
                    if (parsedDate != null && parsedDate.getTime() > latestTimestamp) {
                        latestTimestamp = parsedDate.getTime();
                        latestWorkoutDay = day;
                    }
                } catch (Exception e) {
                    latestWorkoutDay = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(new Date());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        int totalMinutes = totalSeconds / 60;
        textWorkoutDays.setText("Workout Days Completed: " + completedDays);
        textTotalMinutes.setText("Total Minutes: " + totalMinutes);
        textLastWorkout.setText("Last Workout: " + (latestWorkoutDay != null ? latestWorkoutDay : "N/A"));
    }

    private void deleteAllSharedPreferences() {
        String[] prefsList = {"WorkoutHistory", "UserPrefs", "fitness_form_responses", "generated_workout_plan" };
        for (String name : prefsList) {
            SharedPreferences prefs = getSharedPreferences(name, MODE_PRIVATE);
            prefs.edit().clear().apply();
        }

        textUsername.setText("Your Username");
        textWorkoutDays.setText("Workout Days Completed: 0");
        textTotalMinutes.setText("Total Minutes: 0");
        textLastWorkout.setText("Last Workout: N/A");
    }

}
