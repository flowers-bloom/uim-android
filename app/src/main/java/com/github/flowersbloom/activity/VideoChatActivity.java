package com.github.flowersbloom.activity;

import android.os.Bundle;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;

import com.github.flowersbloom.databinding.VideoChatActivityBinding;

public class VideoChatActivity extends BaseActivity {
    private VideoChatActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = VideoChatActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    public void initView() {
        binding.remoteCameraSv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    public void initListener() {
        binding.callBtn.setOnClickListener(v -> {
            binding.localCameraSv.startCapture();
        });
    }
}
