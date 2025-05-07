package com.example.fittrackapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;

import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fittrackapp.adapter.DailyExercisePreviewAdapter;
import com.example.fittrackapp.model.DailyExercise;
import com.example.fittrackapp.model.WorkoutDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class HomeDashboardActivity extends AppCompatActivity {

    private ListView homeWorkoutHistoryListView;
    private com.example.fittrackapp.adapter.WorkoutHistoryAdapter historyAdapter;
    private static final String CHANNEL_ID = "fitness_channel";

    private ArrayList<WorkoutDetail> homeHistoryList;
    private RecyclerView homeDailyExerciseListView;
    private DailyExercisePreviewAdapter previewAdapter;
    private List<DailyExercise> homeExerciseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_dashboard);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_my_workout) {
                    startActivity(new Intent(HomeDashboardActivity.this, ExerciseDashboardActivity.class));
                    return true;
                } else if (id == R.id.nav_statistics) {
                    startActivity(new Intent(HomeDashboardActivity.this, WorkoutGraphActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(HomeDashboardActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM},
                        1002);
            }
        }


        homeWorkoutHistoryListView = findViewById(R.id.homeWorkoutHistoryListView);
        homeHistoryList = new ArrayList<>();
        loadWorkoutHistoryForHome();
        historyAdapter = new com.example.fittrackapp.adapter.WorkoutHistoryAdapter(this, homeHistoryList);
        homeWorkoutHistoryListView.setAdapter(historyAdapter);

        TextView seeAll = findViewById(R.id.seeAll);

        seeAll.setOnClickListener(v -> {
            Intent intent = new Intent(HomeDashboardActivity.this, WorkoutHistoryActivity.class);
            startActivity(intent);
        });


        homeDailyExerciseListView = findViewById(R.id.homeDailyExerciseListView);
        homeDailyExerciseListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        homeExerciseList = parsePlan();
        if (!homeExerciseList.isEmpty()) {
            previewAdapter = new DailyExercisePreviewAdapter(homeExerciseList);
            homeDailyExerciseListView.setAdapter(previewAdapter);
        } else {
            Toast.makeText(this, "All workouts completed! 🎉", Toast.LENGTH_SHORT).show();
        }

        TextView cardQuote = findViewById(R.id.cardQuote);

        try {
            InputStream is = getAssets().open("quotes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONArray quotesArray = new JSONArray(json);

            if (quotesArray.length() > 0) {
                int randomIndex = (int) (Math.random() * quotesArray.length());
                JSONObject quoteObject = quotesArray.getJSONObject(randomIndex);

                String quoteText = quoteObject.getString("quote");

                cardQuote.setText("''" + quoteText + "''");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            cardQuote.setText("Keep pushing forward!");
        }

        scheduleWorkoutReminder();
        new WorkoutReminderNotificationHelper(this).showWorkoutReminderNotification();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleWorkoutReminder();
            } else {
                Toast.makeText(this, "Permission denied to schedule alarms", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadWorkoutHistoryForHome() {
        SharedPreferences sharedPreferences = getSharedPreferences("WorkoutHistory", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        ArrayList<WorkoutDetail> tempList = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String historyJson = (String) entry.getValue();
            try {
                JSONArray workoutArray = new JSONArray(historyJson);
                for (int i = 0; i < workoutArray.length(); i++) {
                    JSONObject workoutObject = workoutArray.getJSONObject(i);
                    String name = workoutObject.getString("name");
                    String time = workoutObject.getString("time");
                    String instruction = workoutObject.getString("instruction");

                    tempList.add(new WorkoutDetail(name, time, instruction));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        int start = Math.max(0, tempList.size() - 5);
        homeHistoryList.addAll(tempList.subList(start, tempList.size()));

    }

    private List<DailyExercise> parsePlan() {
        List<DailyExercise> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("fitness_form_responses", MODE_PRIVATE);
        SharedPreferences completedPrefs = getSharedPreferences("completed_exercises", MODE_PRIVATE);

        String rawPlan = prefs.getString("generated_workout_plan", null);

        if (rawPlan != null) {
            try {
                JSONArray jsonArray = new JSONArray(rawPlan);
                int daysToLoad = Math.min(7, jsonArray.length());

                for (int i = 0; i < daysToLoad; i++) {
                    JSONObject dayData = jsonArray.getJSONObject(i);
                    String day = dayData.getString("day");

                    Log.d("WorkoutPlan", "Checking " + day + ", completed: " + completedPrefs.getBoolean(day, false));

                    if (completedPrefs.getBoolean(day, false)) continue;

                    JSONArray workoutsArray = dayData.getJSONArray("workouts");
                    List<WorkoutDetail> workoutDetails = new ArrayList<>();
                    String goal = dayData.optString("fitnessGoal", "");

                    if (workoutsArray.length() == 0) {
                        list.add(new DailyExercise("Rest Day", "Rest and recovery", new ArrayList<>()));
                    } else {
                        for (int j = 0; j < workoutsArray.length(); j++) {
                            JSONObject workout = workoutsArray.getJSONObject(j);
                            workoutDetails.add(new WorkoutDetail(
                                    workout.getString("name"),
                                    workout.getString("time"),
                                    workout.getString("instruction")));
                        }
                        list.add(new DailyExercise(day, goal, workoutDetails));
                    }
                }

                while (list.size() < 7) {
                    list.add(new DailyExercise("Rest Day", "Rest and recovery", new ArrayList<>()));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
    private void showWorkoutReminderNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Time for Your Workout!")
                .setContentText("Don't forget to complete your daily workout!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
    private void scheduleWorkoutReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                    == PackageManager.PERMISSION_GRANTED) {
                scheduleAlarm();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM},
                        1002);
            }
        } else {
            scheduleAlarm();
        }
    }

    private void scheduleAlarm() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (currentHour >= 7 && (currentHour > 7 || currentMinute > 0)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerTime = calendar.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("action", "show_workout_reminder");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        }
    }
}
