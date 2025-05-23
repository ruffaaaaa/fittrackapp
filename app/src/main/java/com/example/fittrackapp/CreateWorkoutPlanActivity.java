package com.example.fittrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fittrackapp.helper.FittrackDBHelper;
import com.example.fittrackapp.model.WorkoutPlan;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class CreateWorkoutPlanActivity extends AppCompatActivity {

    private TextView workoutPlanTextView;
    private Button generateWorkoutButton;
    private Button doneButton;

    private SharedPreferences sharedPreferences;

    private String fitnessGoal;
    private String fitnessLevel;
    private String workoutDays;
    private String medicalConditions;
    private String exerciseLocation;
    private ProgressBar progressBar;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_workout_plan);

        workoutPlanTextView = findViewById(R.id.workoutPlanTextView);
        generateWorkoutButton = findViewById(R.id.generateWorkoutButton);
        doneButton = findViewById(R.id.doneButton);

        sharedPreferences = getSharedPreferences("fitness_form_responses", MODE_PRIVATE);

        fitnessGoal = sharedPreferences.getString("fitnessGoal", "Not answered");
        fitnessLevel = sharedPreferences.getString("fitnessLevel", "Not answered");
        workoutDays = sharedPreferences.getString("workoutDays", "Not answered");
        medicalConditions = sharedPreferences.getString("medicalConditions", "Not answered");
        exerciseLocation = sharedPreferences.getString("exerciseLocation", "Not answered");
        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingText);

        generateWorkoutButton.setVisibility(View.GONE);
        doneButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);


        generateWorkoutButton.setOnClickListener(v -> {
            generateWorkoutPlan(fitnessGoal, fitnessLevel, workoutDays, medicalConditions, exerciseLocation);
            Toast.makeText(this, "Generating a new workout plan...", Toast.LENGTH_SHORT).show();
        });

        doneButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("formCompleted", true);
            editor.apply();

            Toast.makeText(this, "Workout plan saved!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CreateWorkoutPlanActivity.this, HomeDashboardActivity.class);
            startActivity(intent);
            finish();
        });
;

        if (fitnessGoal.equals("Not answered") || fitnessLevel.equals("Not answered") || workoutDays.equals("Not answered")) {
            Toast.makeText(this, "Please complete the fitness form first!", Toast.LENGTH_LONG).show();
        } else {
            generateWorkoutPlan(fitnessGoal, fitnessLevel, workoutDays, medicalConditions, exerciseLocation);
        }
    }

    private void generateWorkoutPlan(String goal, String level, String workoutDays, String medicalConditions, String exerciseLocation) {
        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);


        new Handler().postDelayed(() -> {
            int workoutDayCount = 5;
            int totalWorkoutDays = workoutDayCount * 4;
            int exercisesPerDay = 8;
            int daysInCurrentMonth = getDaysInCurrentMonth();

            List<WorkoutPlan> allWorkouts = loadWorkoutPlansFromJson();
            if (allWorkouts != null) {
                List<List<WorkoutPlan>> workoutsPerDay = getRandomWorkoutPerDay(allWorkouts, totalWorkoutDays, exercisesPerDay, medicalConditions, exerciseLocation);
                List<List<WorkoutPlan>> finalSchedule = addRestDays(workoutsPerDay, daysInCurrentMonth);
                displayWorkoutPlan(finalSchedule);
                saveWorkoutPlanToSharedPreferences(finalSchedule);
            } else {
                Toast.makeText(CreateWorkoutPlanActivity.this, "Error loading JSON data.", Toast.LENGTH_SHORT).show();
            }

            progressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);

            generateWorkoutButton.setVisibility(View.VISIBLE);
            doneButton.setVisibility(View.VISIBLE);

        }, 5000);
    }

    private int getDaysInCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private List<WorkoutPlan> loadWorkoutPlansFromJson() {
        try {
            InputStreamReader reader = new InputStreamReader(getAssets().open("exercise.json"));
            Gson gson = new Gson();
            Type workoutListType = new TypeToken<List<WorkoutPlan>>() {}.getType();
            return gson.fromJson(reader, workoutListType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<List<WorkoutPlan>> getRandomWorkoutPerDay(List<WorkoutPlan> allWorkouts, int workoutDays, int exercisesPerDay, String medicalConditions, String locationFilter) {
        List<List<WorkoutPlan>> schedule = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < workoutDays; i++) {
            List<WorkoutPlan> dailyWorkout = new ArrayList<>();
            Set<String> usedExerciseNames = new HashSet<>();
            int attempts = 0;

            while (dailyWorkout.size() < exercisesPerDay && attempts < 1000) {
                WorkoutPlan candidate = allWorkouts.get(random.nextInt(allWorkouts.size()));

                if (usedExerciseNames.contains(candidate.getName())) {
                    attempts++;
                    continue;
                }

                if (!candidate.getLocation().equalsIgnoreCase(locationFilter)) {
                    attempts++;
                    continue;
                }

                if (shouldExcludeExercise(candidate, medicalConditions)) {
                    attempts++;
                    continue;
                }

                dailyWorkout.add(candidate);
                usedExerciseNames.add(candidate.getName());
                attempts++;
            }

            if (!dailyWorkout.isEmpty()) {
                schedule.add(dailyWorkout);
            }
        }

        return schedule;
    }

    private boolean shouldExcludeExercise(WorkoutPlan workout, String medicalConditions) {
        if (workout.getExcludeForMedicalCondition() == null || workout.getExcludeForMedicalCondition().isEmpty()) {
            return false;
        }

        String[] userConditions = medicalConditions.split(",");
        for (String condition : userConditions) {
            if (workout.getExcludeForMedicalCondition().contains(condition.trim())) {
                return true;
            }
        }

        return false;
    }

    private List<List<WorkoutPlan>> addRestDays(List<List<WorkoutPlan>> workoutDays, int totalDaysInMonth) {
        List<List<WorkoutPlan>> fullSchedule = new ArrayList<>(Collections.nCopies(totalDaysInMonth, null));

        int index = 0;
        if (index < workoutDays.size()) fullSchedule.set(0, workoutDays.get(index++));
        if (index < workoutDays.size()) fullSchedule.set(1, workoutDays.get(index++));

        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 2; i < totalDaysInMonth; i++) {
            availableIndices.add(i);
        }
        Collections.shuffle(availableIndices);

        for (; index < workoutDays.size(); index++) {
            int insertIndex = availableIndices.remove(0);
            fullSchedule.set(insertIndex, workoutDays.get(index));
        }

        return fullSchedule;
    }

    private void displayWorkoutPlan(List<List<WorkoutPlan>> fullSchedule) {
        LinearLayout container = findViewById(R.id.workoutPlanContainer);
        container.removeAllViews();

        for (int i = 0; i < fullSchedule.size(); i++) {
            List<WorkoutPlan> dayWorkouts = fullSchedule.get(i);

            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setPadding(32, 24, 32, 24);
            dayLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.day_container_background));
            dayLayout.setElevation(8f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 16, 16, 16);
            dayLayout.setLayoutParams(params);

            TextView dayTitle = new TextView(this);
            dayTitle.setText("Day " + (i + 1));
            dayTitle.setTextSize(20);
            dayTitle.setTypeface(null, Typeface.BOLD);
            dayTitle.setTextColor(ContextCompat.getColor(this, R.color.color_cream));

            dayTitle.setPadding(0, 0, 0, 16);
            dayLayout.addView(dayTitle);

            if (dayWorkouts == null || dayWorkouts.isEmpty()) {
                TextView restText = new TextView(this);
                restText.setText("REST DAY 💤");
                restText.setTextSize(16);
                restText.setTypeface(null, Typeface.BOLD);

                restText.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
                dayLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.rest_day_background));
                dayLayout.addView(restText);
            } else {
                for (WorkoutPlan plan : dayWorkouts) {
                    TextView workoutText = new TextView(this);
                    workoutText.setText("• " + plan.getName());
                    workoutText.setTextSize(16);
                    workoutText.setTextColor(ContextCompat.getColor(this, R.color.color_cream));
                    workoutText.setPadding(8, 8, 8, 8);
                    dayLayout.addView(workoutText);
                }
            }

            container.addView(dayLayout);
        }
    }

    private void saveWorkoutPlanToSharedPreferences(List<List<WorkoutPlan>> workoutSchedule) {
        Gson gson = new Gson();
        List<Map<String, Object>> structuredPlan = new ArrayList<>();

        FittrackDBHelper dbHelper = new FittrackDBHelper(this);
        dbHelper.clearWorkoutPlans();

        for (int i = 0; i < workoutSchedule.size(); i++) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", i + 1);

            List<WorkoutPlan> plans = workoutSchedule.get(i);
            String type = (plans == null) ? "rest" : "workout";
            List<WorkoutPlan> workoutsForJson = (plans == null) ? new ArrayList<>() : plans;

            List<String> exerciseNames = new ArrayList<>();
            for (WorkoutPlan plan : workoutsForJson) {
                exerciseNames.add(plan.getName());
            }

            String exercisesJson = gson.toJson(exerciseNames);
            dbHelper.insertWorkoutPlan(i + 1, type, exercisesJson);

            dayData.put("type", type);
            dayData.put("workouts", workoutsForJson);
            structuredPlan.add(dayData);
        }

        String jsonPlan = gson.toJson(structuredPlan);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("generated_workout_plan", jsonPlan);
        editor.apply();

        Toast.makeText(this, "Workout plan saved!", Toast.LENGTH_SHORT).show();
    }



}
