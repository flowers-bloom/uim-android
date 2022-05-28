package com.github.flowersbloom.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.flowersbloom.GlobalObject;
import com.github.flowersbloom.H264Decoder;
import com.github.flowersbloom.client.packet.VideoCallPacket;
import com.github.flowersbloom.client.packet.VideoCallResultPacket;
import com.github.flowersbloom.client.packet.VideoDataPacket;
import com.github.flowersbloom.databinding.VideoChatActivityBinding;
import com.github.flowersbloom.util.ToastUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoChatActivity extends BaseActivity {
    public static final String KEY_IS_CALLER = "is_caller";
    public static final String KEY_USER = "user";
    private static final int CALL_TIMEOUT_SECONDS = 8;

    private CompletableFuture<Integer> callResultFuture;
    private VideoChatActivityBinding binding;
    private H264Decoder h264Decoder;
    private boolean isCaller;
    private User receiver;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = VideoChatActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        isCaller = intent.getBooleanExtra(KEY_IS_CALLER, false);
        receiver = (User) intent.getSerializableExtra(KEY_USER);
        callResultFuture = new CompletableFuture<>();

        initView();
    }

    public void initView() {
        binding.hangupIv.setOnClickListener(v -> {
            finish();
        });
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
        if (isCaller) {
            dialog = ProgressDialog.show(this, "视频呼叫", "呼叫中，请稍后...",
                    false, true);
            sendVideoCallInvite();
            Thread callAwaitThread = new Thread(() -> {
                try {
                    callResultFuture.get(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        ToastUtil.toast("对方未响应，请稍后重试");
                        callResultFuture = null;
                        finish();
                    });
                }
            });
            callAwaitThread.start();
        }else {
            ToastUtil.toast("开始通话");
            binding.hangupIv.setVisibility(View.VISIBLE);
            binding.localCameraSv.startCapture(this, receiver.getAddress());
        }
    }

    private void sendVideoCallInvite() {
        if (receiver == null) {
            log.error("user is null");
            return;
        }
        log.info("sendVideoCallInvite call, meAddress:{}, dstAddress:{}",
                GlobalObject.ME.getAddress(), receiver.getAddress());
        VideoCallPacket videoCallPacket = new VideoCallPacket();
        User me = GlobalObject.ME;
        videoCallPacket.setSender(me);
        ByteBuf byteBuf = videoCallPacket.toNewBuf(0);
        GlobalObject.channel.writeAndFlush(new DatagramPacket(
                byteBuf, receiver.getAddress()
        ));
    }

    @Override
    public void handle(BasePacket basePacket) {
        super.handle(basePacket);
        if (basePacket instanceof VideoDataPacket) {
            log.info("recv VideoDataPacket");
            VideoDataPacket videoDataPacket = (VideoDataPacket) basePacket;
            h264Decoder.callBack(videoDataPacket.getBytes());
        }else if (basePacket instanceof VideoCallResultPacket) {
            log.info("recv VideoCallResultPacket");
            if (callResultFuture == null || callResultFuture.isCancelled()) {
                return;
            }
            callResultFuture.complete(1);
            runOnUiThread(() -> {
                VideoCallResultPacket resultPacket = (VideoCallResultPacket) basePacket;
                if (resultPacket.getStatus() == 1) {
                    binding.hangupIv.setVisibility(View.VISIBLE);
                    if (!this.isDestroyed()) {
                        dialog.dismiss();
                    }
                    //传输编码数据
                    binding.localCameraSv.startCapture(this, receiver.getAddress());
                }else {
                    ToastUtil.toast("你的视频邀请被拒绝");
                    finish();
                }
            });
        }
    }
}
