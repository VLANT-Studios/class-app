package de.vlant.klassenapp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static de.vlant.klassenapp.MsgActivity.capitalize;

public class NotificationService extends Service {
    public static List<Long> alreadySentNotes = new ArrayList<>();

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
                                MsgActivity.sendNotification(getApplicationContext(), message);
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
