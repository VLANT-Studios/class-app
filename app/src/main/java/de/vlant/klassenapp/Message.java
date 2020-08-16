package de.vlant.klassenapp;

import com.google.firebase.auth.FirebaseUser;

public class Message {
    private final long id;
    private String text; // message body
    private String user; // data of the user that sent this message
    private String time;
    private boolean belongsToCurrentUser; // is this message sent by us?

    public Message(long id, String text, String user, String time, boolean belongsToCurrentUser) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public String getMemberName() {
        return user;
    }

    public String getTime() {
        return time;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public long getID() {
        return id;
    }
}