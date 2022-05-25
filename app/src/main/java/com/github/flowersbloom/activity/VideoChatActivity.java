package com.github.flowersbloom.activity;

import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;

import com.github.flowersbloom.H264Decoder;
import com.github.flowersbloom.databinding.VideoChatActivityBinding;

import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.handler.MessageListener;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.github.flowersbloom.udp.packet.VideoDataPacket;

public class VideoChatActivity extends BaseActivity implements MessageListener {
    private static final String TAG = "VideoChatActivity";
    private VideoChatActivityBinding binding;
    private H264Decoder h264Decoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = VideoChatActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        initListener();
    }

    public void initView() {
        binding.localCameraSv.setZOrderOnTop(true);
        binding.remoteCameraSv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Surface surface = holder.getSurface();
                h264Decoder = new H264Decoder();
                h264Decoder.initDecoder(surface);
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
        MessageCallback.subscribe(this);
        binding.callBtn.setOnClickListener(v -> {
            binding.localCameraSv.startCapture();
        });
    }

    @Override
    public void handle(BasePacket basePacket) {
        if (basePacket instanceof VideoDataPacket) {
            VideoDataPacket videoDataPacket = (VideoDataPacket) basePacket;
            h264Decoder.callBack(videoDataPacket.getBytes());
        }
    }
}
