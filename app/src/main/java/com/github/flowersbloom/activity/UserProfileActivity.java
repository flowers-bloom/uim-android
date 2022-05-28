package com.github.flowersbloom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.flowersbloom.databinding.UserProfileActivityBinding;
import com.github.flowersbloom.util.ToastUtil;

import io.github.flowersbloom.udp.entity.User;

public class UserProfileActivity extends BaseActivity {
    public static final String KEY_USER = "user";

    private UserProfileActivityBinding binding;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = UserProfileActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        bindData();
    }

    private void bindData() {
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra(KEY_USER);
        if (user != null) {
            int resId = 0;
            try {
                resId = Integer.parseInt(user.getUserAvatar());
            }catch (Exception e) {
                e.printStackTrace();
            }
            binding.avatarIv.setImageResource(resId);
            binding.nicknameTv.setText(user.getUserNickname());
        }
    }

    private void initView() {
        initTitle();

        binding.sendMsgBtn.setOnClickListener(v -> {
            ToastUtil.toast("click");
        });
        binding.videoChatBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(UserProfileActivity.this, VideoChatActivity.class);
            intent.putExtra(VideoChatActivity.KEY_IS_CALLER, true);
            intent.putExtra(VideoChatActivity.KEY_USER, user);
            startActivity(intent);
            finish();
        });
    }

    private void initTitle() {
        binding.titleLayout.titleNameTv.setText("");
        binding.titleLayout.actionBarIv.setVisibility(View.GONE);
        binding.titleLayout.backIc.setOnClickListener(v -> finish());
    }
}
