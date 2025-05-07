package com.example.fittrackapp.model;

import java.util.List;

public class WorkoutPlanResponse {
    private List<WorkoutPlan> exercises;

    public List<WorkoutPlan> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutPlan> exercises) {
        this.exercises = exercises;
    }
}
