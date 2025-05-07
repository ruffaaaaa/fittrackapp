package com.example.fittrackapp.api;

import com.example.fittrackapp.model.WorkoutPlan;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface WorkoutPlanAPI {

    @GET("exercises")
    Call<List<WorkoutPlan>> getExercisesByGoalAndLevel(
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Header("x-rapidapi-host") String host,
            @Header("x-rapidapi-key") String apiKey,
            @Query("equipment") String equipment

    );
}
