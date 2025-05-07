package com.example.fittrackapp.model;

import java.util.List;

public class DailyExercise {
    private String day;
    private String workout;
    private String fitnessGoal;
    private List<WorkoutDetail> workoutDetails;


    public DailyExercise(String day, String fitnessGoal, List<WorkoutDetail> workoutDetails) {
        this.day = day;
        this.fitnessGoal = fitnessGoal;
        this.workoutDetails = workoutDetails;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public List<WorkoutDetail> getWorkoutDetails() {
        return workoutDetails;
    }

    public void setWorkoutDetails(List<WorkoutDetail> workoutDetails) {
        this.workoutDetails = workoutDetails;
    }
}



