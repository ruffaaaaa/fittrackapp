package com.example.fittrackapp.model;

import java.util.List;

public class WorkoutPlan {
    private String name;
    private String muscleGroup;
    private String instruction;
    private String difficulty;
    private String fitnessGoal;
    private List<String> excludeForMedicalCondition;
    private String time;
    private String location;

    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }
    public String getInstruction() { return instruction; }
    public String getDifficulty() { return difficulty; }
    public String getFitnessGoal() { return fitnessGoal; }
    public List<String> getExcludeForMedicalCondition() { return excludeForMedicalCondition; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
}
