package de.vlant.klassenapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static de.vlant.klassenapp.MsgActivity.CHANNEL_ID;
import static de.vlant.klassenapp.MsgActivity.DATE_FORMAT;
import static de.vlant.klassenapp.MsgActivity.capitalize;

public class NotificationService extends Service {
    public static HashMap<Integer, String> currentNotifications = new HashMap<>();
    public static List<Long> alreadySentNotes = new ArrayList<>();
    public static int lastNoteID = 1530;
    public static final String EXTRA_NOTE_ID = "de.vlant.intent.extra.NOTIFICATION_ID";

    FirebaseAuth auth;

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null)
                    stopSelf();
            }
        });
        readAlreadySentNotes();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (auth.getCurrentUser() != null) {
            DatabaseReference msgRef = MsgActivity.msgRef;
            msgRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String[] msg_sender = child.getValue(String.class).split("OOOvlaOOO");
                        StringBuilder msgBuilder = new StringBuilder();
                        for (String string : Arrays.copyOfRange(msg_sender, 0, msg_sender.length - 1))
                            msgBuilder.append(string + " ");
                        String msg = msgBuilder.toString();
                        String[] sender_time = msg_sender[msg_sender.length - 1].split(", time:");
                        String sender = capitalize(sender_time[0].replace("@vlant.de", ""));
                        Message message = new Message(Long.parseLong(child.getKey()), msg, sender, sender_time[1], sender_time[0].equals(auth.getCurrentUser().getEmail()));
                        if (!alreadySentNotes.contains(message.getID())) {
                            alreadySentNotes.add(message.getID());
                            ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
                            ActivityManager.getMyMemoryState(myProcess);
                            if (myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                                sendNotification(getApplicationContext(), message);
                            try {
                                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(getFilesDir() + "/alreadySentNotes.vlant", true)));
                                out.print(";" + message.getID());
                                out.close();
                            }catch (IOException ignored) {
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public static void sendNotification(Context context, Message message) {
        int id = lastNoteID + 1;
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Intent delIntent = new Intent(context, NotificationDeleteReceiver.class).putExtra(EXTRA_NOTE_ID, id);
        PendingIntent deletePendingIntent = PendingIntent.getActivity(context, 0, delIntent, 0);
        String contentText = message.getUserName() + ", " + message.getTime() + ": " + message.getText();
        NotificationCompat.Builder noteBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        noteBuilder.setSmallIcon(R.drawable.notification_small)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (currentNotifications.size() > 0) {
            NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("");
            int i = 0;
            for (String value : currentNotifications.values()) {
                try {
                    messagingStyle.addMessage(value, DATE_FORMAT.parse(message.getTime()).getTime(), (Person) null);
                } catch (ParseException e) {
                    messagingStyle.addMessage(value, new Date().getTime(), (Person) null);
                }
                notificationManager.cancel((int) currentNotifications.keySet().toArray()[i]);
                i++;
            }
            try {
                messagingStyle.addMessage(contentText, DATE_FORMAT.parse(message.getTime()).getTime(), (Person) null);
            } catch (ParseException e) {
                messagingStyle.addMessage(contentText, new Date().getTime(), (Person) null);
            }
            noteBuilder.setStyle(messagingStyle);
        } else
            noteBuilder.setContentText(contentText);
        Notification notification = noteBuilder.build();
        notificationManager.notify(id, notification);
        currentNotifications.put(id, contentText);
        lastNoteID++;
    }


    private void readAlreadySentNotes() {
        File aSNFile = new File(getFilesDir() + "/alreadySentNotes.vlant");
        if (!aSNFile.exists())
            return;
        try {
            Scanner myReader = new Scanner(aSNFile);
            for (String str : myReader.nextLine().split(";")) {
                try {
                    long id = Long.parseLong(str);
                    alreadySentNotes.add(id);
                } catch (NumberFormatException ignored) {}
                Log.e("VLANTlog", alreadySentNotes.toString());
            }
            myReader.close();
        } catch (IOException|java.util.NoSuchElementException e) {
            e.printStackTrace();
        }
    }
}
