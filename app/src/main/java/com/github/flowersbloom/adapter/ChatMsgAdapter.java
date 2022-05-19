package com.github.flowersbloom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.flowersbloom.R;
import com.github.flowersbloom.entity.ChatMsg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatMsgAdapter extends RecyclerView.Adapter<ChatMsgAdapter.ViewHolder> {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM.dd HH:mm");

    private Context context;
    private String myId;
    private List<ChatMsg> msgList;

    public ChatMsgAdapter(Context context, String myId, List<ChatMsg> msgList) {
        this.context = context;
        this.myId = myId;
        this.msgList = new ArrayList<>(msgList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ChatMsg.LEFT_TYPE) {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_left_text_msg, parent, false);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_right_text_msg, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMsg chatMsg = msgList.get(position);
        if (chatMsg != null) {
            View itemView = holder.itemView;
            TextView nameTV = itemView.findViewById(R.id.chatter_name);
            ImageView avatarIV = itemView.findViewById(R.id.chat_avatar);
            TextView msgTV = itemView.findViewById(R.id.chat_text);
            nameTV.setText(chatMsg.getUserNickname());
            //TODO: avatarTV

            msgTV.setText(chatMsg.getContent());
            if (position == 0 || chatMsg.getSendTime().getTime() -
                    msgList.get(position-1).getSendTime().getTime() > 1000*10*60) {
                TextView timeTV = itemView.findViewById(R.id.chat_time);
                timeTV.setVisibility(View.VISIBLE);
                timeTV.setText(SDF.format(chatMsg.getSendTime()));
            }else {
                itemView.findViewById(R.id.chat_time).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return msgList.get(position).getItemType();
    }

    public void insertItem(ChatMsg chatMsg) {
        msgList.add(chatMsg);
        notifyItemInserted(msgList.size()-1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
