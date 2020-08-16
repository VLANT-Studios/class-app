package de.vlant.klassenapp;

import android.annotation.SuppressLint;
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
    @SuppressLint("WrongViewCast")
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

            holder.name.setText(message.getMemberName() + ", " + message.getTime());
            holder.messageBody.setText(message.getText());
            GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            if (MsgActivity.usercolors.containsKey(message.getMemberName().toLowerCase() + "@vlant.de")) {
                String userColorString = "empty";
                userColorString = MsgActivity.usercolors.get(message.getMemberName().toLowerCase() + "@vlant.de");
                int[] colors = {Color.rgb(0xf2, 0x7d, 0x7d), Color.rgb(0x7d, 0xf2, 0x8d), Color.rgb(0xee, 0xf2, 0x7d), Color.LTGRAY, Color.rgb(0x26, 0x98, 0xF1)};
                if (TextUtils.isDigitsOnly(userColorString))
                    drawable.setColor(colors[Integer.parseInt(userColorString)-1]);
                else if (userColorString.equals("vlant"))
                    holder.avatar.setImageDrawable(MsgActivity.vlantIcon);
                else if (userColorString.equals("klassensprecher"))
                    holder.avatar.setImageDrawable(MsgActivity.klassensprecherIcon);
                else
                    drawable.setColor(Color.rgb(0,0,0));
            } else
                drawable.setColor(Color.GRAY);
        }

        return convertView;
    }

}

class MessageViewHolder {
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;
}