package de.vlant.klassenapp;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MsgActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "vlant_messages_channel";
    private static final String TIME_FORMAT = "HH:mm, dd.MM.yy";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(TIME_FORMAT, Locale.GERMANY);
    EditText newMsg;
    ImageButton send;

    static DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    static DatabaseReference msgRef = rootRef.child("messages");
    static DatabaseReference colorRef = rootRef.child("colors");

    MessageAdapter messageAdapter;
    ListView messagesView;

    Message lastMsg;

    public static HashMap<String, String> usercolors = new HashMap<>();
    public static Drawable vlantIcon;
    public static Drawable klassensprecherIcon;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);
        messageAdapter = new MessageAdapter(this);
        messagesView = findViewById(R.id.messages_view);
        auth = FirebaseAuth.getInstance();
        messagesView.setAdapter(messageAdapter);
        send = findViewById(R.id.sendButton);
        newMsg = findViewById(R.id.msgInput);
        colorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot usercolor : snapshot.getChildren()) {
                    String mail = usercolor.getKey() + "@vlant.de";
                    String color = usercolor.getValue(String.class);
                    usercolors.put(mail, color);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        vlantIcon = ContextCompat.getDrawable(this, R.drawable.vlant_round);
        klassensprecherIcon = ContextCompat.getDrawable(this, R.drawable.klassensprecher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Nachricht";
            String description = "Für Nachrichten";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null)
                notificationManager.createNotificationChannel(channel);
        }
        registerForContextMenu(messagesView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        msgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String[] msg_sender = child.getValue(String.class).split("OOOvlaOOO");
                    StringBuilder msgBuilder = new StringBuilder();
                    for (String string : Arrays.copyOfRange(msg_sender, 0, msg_sender.length - 1))
                        msgBuilder.append(string + " ");
                    String msg = msgBuilder.toString().trim();
                    String[] sender_time = msg_sender[msg_sender.length - 1].split(", time:");
                    String sender = capitalize(sender_time[0].replace("@vlant.de", ""));
                    String time = sender_time[1];
                    Message message;
                    if (msg.startsWith("reply:") && msg.contains("|;|") && msg.contains("|end;reply|")) {
                        String[] split = msg.split("\\|;\\|");
                        String[] end_reply = split[1].split("\\|end;reply\\|");
                        message = new Message(Long.parseLong(child.getKey()), end_reply[1], sender, time, sender_time[0].equals(auth.getCurrentUser().getEmail()),
                                Message.fromReply(split[0].replace("reply:", ""), end_reply[0]));
                        Log.e("VLANTlog", message.toString());
                    } else
                        message = new Message(Long.parseLong(child.getKey()), msg, sender, time, sender_time[0].equals(auth.getCurrentUser().getEmail()));
                    if (!checkForId(message.getID())) {
                        messageAdapter.add(message);
                        lastMsg = message;
                    }
                    // scroll the ListView to the last added element
                    messagesView.setSelection(messagesView.getCount() - 1);
                }
            }

            private boolean checkForId(long id) {
                for (Message msg : messageAdapter.messages) {
                    if (msg.getID() == id) return true;
                }
                return false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();
                msgRef.child(String.valueOf(lastMsg.getID() + 1))
                        .setValue(newMsg.getText().toString() + "OOOvlaOOO" + auth.getCurrentUser().getEmail() + ", time:" + DATE_FORMAT.format(date));
                newMsg.setText("");
            }
        });
        NotificationService.currentNotifications.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                (new File(getFilesDir() + "/credentials.vlant")).delete();
                startActivity(new Intent(this, LoginActivity.class));
                auth.signOut();
                finish();
                return true;
            case R.id.credits:
                showCredits();
                return true;
            case R.id.delnotes:
                File delete = (new File(getFilesDir() + "/alreadySentNotes.vlant"));
                delete.delete();
                try {
                    delete.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == messagesView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.msg_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.reply_menu_entry:
                if (newMsg.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Nachricht leer!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                View message = info.targetView;
                TextView replySender  = message.findViewById(R.id.name);
                TextView replyMessage = message.findViewById(R.id.message_body);
                String replyToName    = (replySender  == null) ? auth.getCurrentUser().getEmail().replace("@vlant.de", "") : replySender.getText().toString();
                String replyToMsg     = (replyMessage != null) ? replyMessage.getText().toString().trim() : "???";
                if (replyToMsg.contains("|end;reply|"))
                    replyToMsg = replyToMsg.split("\\|end;reply\\|")[1];
                Date date = new Date();
                msgRef.child(String.valueOf(lastMsg.getID() + 1))
                        .setValue("reply:" + capitalize(replyToName.replaceAll(",.*", "")) + "|;|" + replyToMsg + "|end;reply|"
                                + newMsg.getText().toString() + "OOOvlaOOO" + auth.getCurrentUser().getEmail() + ", time:" + DATE_FORMAT.format(date));
                newMsg.setText("");
                return true;
            case R.id.copy_menu_entry:
                TextView messageTv = info.targetView.findViewById(R.id.message_body);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (messageTv != null)
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Message from VLANT", messageTv.getText().toString().trim()));
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showCredits() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Info")
                .setMessage("Klassenapp, erstellt von VLANT Studios \n" + "Version: " + AppInfos.VERSION)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(null, null);
        dialogBuilder.create().show();
    }

    public static String capitalize(String str) {
        if(str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}