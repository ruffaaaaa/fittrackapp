package com.example.fittrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkoutGraphActivity extends AppCompatActivity {

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_graph);

        lineChart = findViewById(R.id.lineChart);
        loadWorkoutDataToChart();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_statistics);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(WorkoutGraphActivity.this, HomeDashboardActivity.class));

                    return true;
                } else if (id == R.id.nav_my_workout) {
                    startActivity(new Intent(WorkoutGraphActivity.this, ExerciseDashboardActivity.class));
                    return true;
                } else if (id == R.id.nav_statistics) {
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(WorkoutGraphActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;

            }
        });
        MaterialButton buttonGraph = findViewById(R.id.button);
        MaterialButton buttonWorkHistory = findViewById(R.id.buttonWorkHistory);

        buttonGraph.setOnClickListener(v -> {
            if (!(WorkoutGraphActivity.this instanceof WorkoutGraphActivity)) {
                Intent intent = new Intent(WorkoutGraphActivity.this, WorkoutGraphActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonWorkHistory.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutGraphActivity.this, WorkoutHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void loadWorkoutDataToChart() {
        SharedPreferences sharedPreferences = getSharedPreferences("WorkoutHistory", MODE_PRIVATE);

        Map<String, ?> allEntries = sharedPreferences.getAll();

        Map<String, Integer> monthWorkoutCount = new LinkedHashMap<>();
        String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            monthWorkoutCount.put(month, 0);
        }

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            String workoutDataJson = (String) entry.getValue();

            try {
                JSONArray workoutArray = new JSONArray(workoutDataJson);
                for (int i = 0; i < workoutArray.length(); i++) {
                    JSONObject workout = workoutArray.getJSONObject(i);

                    long timestamp = System.currentTimeMillis();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    int monthIndex = calendar.get(Calendar.MONTH); // 0 = Jan, 11 = Dec
                    String monthName = months[monthIndex];

                    monthWorkoutCount.put(monthName, monthWorkoutCount.get(monthName) + 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        List<Entry> entries = new ArrayList<>();
        List<String> monthLabels = new ArrayList<>();
        int index = 0;

        for (String month : months) {
            entries.add(new Entry(index, monthWorkoutCount.get(month)));
            monthLabels.add(month);
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(this, R.color.color_primary));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Description description = new Description();
        description.setText("Monthly Workout Count");
        lineChart.setDescription(description);

        lineChart.invalidate(); // Refresh chart
    }
}

