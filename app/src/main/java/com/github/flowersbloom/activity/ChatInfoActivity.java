package com.github.flowersbloom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.flowersbloom.databinding.ChatInfoActivityBinding;


public class ChatInfoActivity extends BaseActivity {
    private ChatInfoActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatInfoActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        initListener();
    }

    private void initListener() {
        binding.activeItemLayout.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(ChatInfoActivity.this, ActiveListActivity.class);
            startActivity(intent);
        });
    }

    private void initView() {
        initTitle();
        binding.activeItemLayout.leftTv.setText("在线列表");
    }

    private void initTitle() {
        binding.titleLayout.titleNameTv.setText("聊天信息");
        binding.titleLayout.actionBarIv.setVisibility(View.GONE);
        binding.titleLayout.backIc.setOnClickListener(v -> finish());
    }
}
