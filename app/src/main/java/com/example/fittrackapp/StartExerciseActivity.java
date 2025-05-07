package com.example.fittrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fittrackapp.model.WorkoutDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StartExerciseActivity extends AppCompatActivity {

    private TextView exerciseNameTextView;
    private TextView instructionTextView;
    private TextView timerTextView;
    private ProgressBar progressBar;
    private Button btnPauseResume, btnSkip, btnDone;

    private List<WorkoutDetail> workoutDetails;
    private int currentExerciseIndex = 0;

    private CountDownTimer currentTimer;
    private boolean isPaused = false;
    private long timeLeftInMillis;
    private int totalTimeInSeconds;

    private CountDownTimer getReadyTimer;
    private String day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_exercise);

        exerciseNameTextView = findViewById(R.id.exerciseNameTextView);
        instructionTextView = findViewById(R.id.instructionTextView);
        timerTextView = findViewById(R.id.timerTextView);
        progressBar = findViewById(R.id.progressBar);
        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnSkip = findViewById(R.id.btnSkip);
        btnDone = findViewById(R.id.btnDone);

        btnPauseResume.setOnClickListener(v -> {
            if (isPaused) {
                resumeTimer();
            } else {
                pauseTimer();
            }
        });

        btnSkip.setOnClickListener(v -> {
            if (currentTimer != null) {
                currentTimer.cancel();
            }
            currentExerciseIndex++;
            showExerciseDetails();
        });

        btnDone.setOnClickListener(v -> markDayAsCompleted());

        Intent intent = getIntent();
        day = intent.getStringExtra("day");
        String workoutDetailsJson = intent.getStringExtra("workoutDetails");

        if (workoutDetailsJson != null) {
            try {
                JSONArray workoutArray = new JSONArray(workoutDetailsJson);
                workoutDetails = new ArrayList<>();
                for (int i = 0; i < workoutArray.length(); i++) {
                    JSONObject workoutObject = workoutArray.getJSONObject(i);
                    String name = workoutObject.getString("name");
                    String time = workoutObject.getString("time");
                    String instruction = workoutObject.getString("instruction");
                    workoutDetails.add(new WorkoutDetail(name, time, instruction));
                }
                showExerciseDetails();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading workout details.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No workout details found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExerciseDetails() {
        if (getReadyTimer != null) {
            getReadyTimer.cancel();
            getReadyTimer = null;
        }

        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }

        if (currentExerciseIndex < workoutDetails.size()) {
            WorkoutDetail currentWorkout = workoutDetails.get(currentExerciseIndex);

            exerciseNameTextView.setText("Get Ready: " + currentWorkout.getName());
            instructionTextView.setText("Prepare for: " + currentWorkout.getInstruction());

            int getReadyTime = 10;
            progressBar.setMax(getReadyTime);
            progressBar.setProgress(getReadyTime);
            timerTextView.setText("Starting in: " + getReadyTime + "s");
            btnPauseResume.setEnabled(false);

            getReadyTimer = new CountDownTimer(getReadyTime * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000);
                    timerTextView.setText("Starting in: " + secondsRemaining + "s");
                    progressBar.setProgress(secondsRemaining);
                }

                @Override
                public void onFinish() {
                    btnPauseResume.setEnabled(true);
                    exerciseNameTextView.setText(currentWorkout.getName());
                    instructionTextView.setText(currentWorkout.getInstruction());
                    startCountdown(currentWorkout.getTime());
                }
            }.start();

        } else {
            exerciseNameTextView.setText("Workout Complete!");
            instructionTextView.setText("");
            timerTextView.setText("");
            progressBar.setProgress(0);
            btnPauseResume.setEnabled(false);
            btnSkip.setEnabled(false);
            btnDone.setVisibility(View.VISIBLE);
            saveWorkoutToHistory();
        }
    }

    private void startCountdown(String time) {
        try {
            if (time == null || time.trim().isEmpty()) throw new NumberFormatException("Empty or null time");

            String numericTime = time.replaceAll("[^0-9]", "");
            totalTimeInSeconds = Integer.parseInt(numericTime);
            timeLeftInMillis = totalTimeInSeconds * 1000L;

            startTimer(timeLeftInMillis);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid time format: " + time, Toast.LENGTH_LONG).show();
        }
    }

    private void startTimer(long duration) {
        progressBar.setMax(totalTimeInSeconds);
        progressBar.setProgress(totalTimeInSeconds);

        currentTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerTextView.setText(secondsRemaining + "s");
                progressBar.setProgress(secondsRemaining);
            }

            @Override
            public void onFinish() {
                timerTextView.setText("0s");
                currentExerciseIndex++;
                showExerciseDetails();
            }
        }.start();
    }

    private void pauseTimer() {
        isPaused = true;
        btnPauseResume.setText("Resume");
        if (currentTimer != null) {
            currentTimer.cancel();
        }
    }

    private void resumeTimer() {
        isPaused = false;
        btnPauseResume.setText("Pause");
        startTimer(timeLeftInMillis);
    }

    private void saveWorkoutToHistory() {
        if (day == null || day.isEmpty()) return;

        SharedPreferences sharedPreferences = getSharedPreferences("WorkoutHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray completedWorkouts = new JSONArray();
        for (WorkoutDetail workout : workoutDetails) {
            JSONObject workoutObject = new JSONObject();
            try {
                workoutObject.put("name", workout.getName());
                workoutObject.put("time", workout.getTime());
                workoutObject.put("instruction", workout.getInstruction());
                completedWorkouts.put(workoutObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString(day, completedWorkouts.toString());
        editor.apply();
    }

    private void markDayAsCompleted() {
        if (day != null && !day.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("completed_exercises", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(day, true);
            editor.apply();
        } else {
            Toast.makeText(this, "Day not found. Cannot mark as completed.", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
