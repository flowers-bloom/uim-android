package com.github.flowersbloom.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.flowersbloom.R;
import com.github.flowersbloom.activity.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

import io.github.flowersbloom.udp.entity.User;

public class ActiveAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Activity activity;
    private List<User> userList;

    public ActiveAdapter(Activity activity, List<User> userList) {
        this.activity = activity;
        this.userList = new ArrayList<>();
        this.userList.addAll(userList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.adapter_active_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView avatarIV = holder.itemView.findViewById(R.id.avatar_iv);
        TextView nicknameTV = holder.itemView.findViewById(R.id.nickname_tv);
        User user = userList.get(position);
        int resId = 0;
        try {
            resId = Integer.parseInt(user.getUserAvatar());
        }catch (Exception e) {
            e.printStackTrace();
        }
        avatarIV.setImageResource(resId);
        nicknameTV.setText(user.getUserNickname());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(activity, UserProfileActivity.class);
            intent.putExtra(UserProfileActivity.KEY_USER, user);
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
