package de.vlant.klassenapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static de.vlant.klassenapp.NotificationService.EXTRA_NOTE_ID;
import static de.vlant.klassenapp.NotificationService.currentNotifications;

public class NotificationDeleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(EXTRA_NOTE_ID, -153);
        if (id != -153) {
            currentNotifications.remove(id);
        }
    }
}
