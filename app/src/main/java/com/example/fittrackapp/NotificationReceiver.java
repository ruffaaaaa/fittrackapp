package com.example.fittrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("show_workout_reminder".equals(intent.getStringExtra("action"))) {
            WorkoutReminderNotificationHelper notificationHelper = new WorkoutReminderNotificationHelper(context);
            notificationHelper.showWorkoutReminderNotification();
        }
    }
}
