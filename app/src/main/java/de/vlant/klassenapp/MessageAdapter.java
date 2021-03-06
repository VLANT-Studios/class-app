package de.vlant.klassenapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    List<Message> messages = new ArrayList<Message>();
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged(); // to render the list we need to notify
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.isReply()) {
            ReplyMessageViewHolder replyHolder = new ReplyMessageViewHolder();
            if (message.isBelongsToCurrentUser()) {
                convertView = messageInflater.inflate(R.layout.my_message_reply, null);
                replyHolder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                replyHolder.replyToName = (TextView) convertView.findViewById(R.id.reply_sender);
                replyHolder.replyToMsg = (TextView) convertView.findViewById(R.id.reply_message);
                convertView.setTag(replyHolder);
                replyHolder.messageBody.setText(message.getText());
                replyHolder.replyToMsg.setText(message.getReplyTo().getText());
                replyHolder.replyToName.setText(message.getReplyTo().getUserName());
            } else {
                convertView = messageInflater.inflate(R.layout.their_message_reply, null);
                replyHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                replyHolder.name = (TextView) convertView.findViewById(R.id.name);
                replyHolder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                replyHolder.replyToName = (TextView) convertView.findViewById(R.id.reply_sender_their);
                replyHolder.replyToMsg = (TextView) convertView.findViewById(R.id.reply_message_their);
                convertView.setTag(replyHolder);
                getAndSetColor(message, replyHolder.avatar);
                replyHolder.name.setText(message.getUserName());
                replyHolder.messageBody.setText(message.getText());
                replyHolder.replyToMsg.setText(message.getReplyTo().getText());
                replyHolder.replyToName.setText(message.getReplyTo().getUserName());
            }
            return convertView;
        }

        if (message.isBelongsToCurrentUser()) {
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        } else {
            convertView = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            getAndSetColor(message, holder.avatar);
            holder.name.setText(message.getUserName() + ", " + message.getTime());
            holder.messageBody.setText(message.getText());
        }
        return convertView;
    }

    private void getAndSetColor(Message message, ImageView avatar) {
        GradientDrawable drawable = (GradientDrawable) avatar.getBackground();
        if (MsgActivity.usercolors.containsKey(message.getUserName().toLowerCase() + "@vlant.de")) {
            String userColorString = "empty";
            userColorString = MsgActivity.usercolors.get(message.getUserName().toLowerCase() + "@vlant.de");
            int[] colors = {Color.rgb(0xF2, 0x7D, 0x7D),
                    Color.rgb(0x7D, 0xF2, 0x8D),
                    Color.rgb(0xEE, 0xF2, 0x7D),
                    Color.LTGRAY,
                    Color.rgb(0x26, 0x98, 0xF1)};
            if (TextUtils.isDigitsOnly(userColorString))
                drawable.setColor(colors[Integer.parseInt(userColorString)-1]);
            else if (userColorString.equals("vlant"))
                avatar.setImageDrawable(MsgActivity.vlantIcon);
            else if (userColorString.equals("klassensprecher"))
                avatar.setImageDrawable(MsgActivity.klassensprecherIcon);
            else
                drawable.setColor(Color.rgb(0,0,0));
        } else
            drawable.setColor(Color.GRAY);
    }

}

class MessageViewHolder {
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;
}

class ReplyMessageViewHolder {
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;

    public TextView replyToName;
    public TextView replyToMsg;
}