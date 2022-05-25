package com.github.flowersbloom.activity;

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
    }

    private void initView() {
        initTitle();
    }

    private void initTitle() {
        binding.titleLayout.titleNameTv.setText("聊天信息");
        binding.titleLayout.actionBarIv.setVisibility(View.GONE);
    }
}
