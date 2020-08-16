package de.vlant.klassenapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static de.vlant.klassenapp.MsgActivity.capitalize;

public class NotificationService extends Service {
    public static List<Long> alreadySentNotes = new ArrayList<>();

    FirebaseAuth auth;

    public NotificationService() {
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
    public IBinder onBind(Intent intent) {
        return null;
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
                        Message message = new Message(Long.parseLong(child.getKey()), msg, sender, null, sender_time[0].equals(auth.getCurrentUser().getEmail()));
                        if (!alreadySentNotes.contains(message.getID())) {
                            alreadySentNotes.add(message.getID());
                            MsgActivity.sendNotification(getApplicationContext(), message);
                            try {
                                (new FileWriter(new File(getFilesDir() + "/alreadySentNotes.vlant"))).append(";" + String.valueOf(message.getID()));
                            } catch (IOException e) {
                                e.printStackTrace();
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
        File aSNFile = new File("/data/data/de.vlant.klassenapp/alreadySentNotes.vlant");
        try {
            Scanner myReader = new Scanner(aSNFile);
            for (String str : myReader.toString().split(";")) {
                long id = Long.parseLong(str);
                alreadySentNotes.add(id);
            }
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
