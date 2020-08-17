package de.vlant.klassenapp;

import org.jetbrains.annotations.NotNull;

public class Message {
    private final long id;
    private String text; // message body
    private String user; // data of the user that sent this message
    private String time;
    private boolean belongsToCurrentUser; // is this message sent by us?
    private boolean reply;
    private Message replyTo;

    public Message(long id, String text, String user, String time, boolean belongsToCurrentUser) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.reply = false;
    }

    public Message(long id, String text, String user, String time, boolean belongsToCurrentUser, Message replyTo) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.reply = true;
        this.replyTo = replyTo;
    }

    public String getText() {
        return text;
    }

    public String getUserName() {
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

    public Message getReplyTo() {
        return replyTo;
    }

    public boolean isReply() {
        return reply;
    }

    public static Message fromReply(String sender, String msg) {
        return new Message(0, msg, sender, null, false);
    }

    @NotNull
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", user='" + user + '\'' +
                ", time='" + time + '\'' +
                ", belongsToCurrentUser=" + belongsToCurrentUser +
                ", reply=" + reply +
                ", replyTo=" + ((reply) ? replyTo : "none") +
                '}';
    }
}