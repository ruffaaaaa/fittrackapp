package com.example.fittrackapp.model;

public class WorkoutPlanDetails {

    private String name;
    private String instruction;
    private String time;
    private String fitnessGoal;

    public WorkoutPlanDetails(String name, String instruction, String time, String fitnessGoal) {
        this.name = name;
        this.instruction = instruction;
        this.time = time;
        this.fitnessGoal = fitnessGoal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }
}
