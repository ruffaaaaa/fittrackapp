package com.example.fittrackapp.model;

// model/WorkoutDetail.java
public class WorkoutDetail {
    private String name;
    private String time;
    private String instruction;
    private String dateTime;

    public WorkoutDetail(String name, String time, String instruction) {
        this.name = name;
        this.time = time;
        this.instruction = instruction;
    }

    public WorkoutDetail(String name, String time, String instruction, String dateTime) {
        this.name = name;
        this.time = time;
        this.instruction = instruction;
        this.dateTime = dateTime;
    }

    public String getName() { return name; }
    public String getTime() { return time; }
    public String getInstruction() { return instruction; }
    public String getDateTime() { return dateTime; }

    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
}

