package com.github.flowersbloom.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.flowersbloom.GlobalObject;
import com.github.flowersbloom.R;
import com.github.flowersbloom.client.packet.VideoCallPacket;
import com.github.flowersbloom.client.packet.VideoCallResultPacket;

import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.handler.MessageListener;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseActivity extends AppCompatActivity implements MessageListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageCallback.subscribe(this);
    }

    public void hideSoftInputClearFocus(final View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm =
                (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if (view instanceof EditText) {
            view.clearFocus();
        }
    }

    @Override
    public void handle(BasePacket basePacket) {
        if (basePacket instanceof VideoCallPacket) {
            VideoCallPacket videoCallPacket = (VideoCallPacket) basePacket;
            log.info("VideoCallPacket user address:{}", videoCallPacket.getSender().getAddress());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_alert_info)
                    .setTitle("视频通话提醒")
                    .setMessage(String.format("来自%s的视频通话请求", videoCallPacket.getSender().getUserNickname()));
            builder.setPositiveButton("接收", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.setClass(BaseActivity.this, VideoChatActivity.class);
                    intent.putExtra(VideoChatActivity.KEY_IS_CALLER, false);
                    intent.putExtra(VideoChatActivity.KEY_USER, videoCallPacket.getSender());
                    startActivity(intent);
                    sendCallResultPacket(videoCallPacket.getSender(), (byte) 1);
                }
            });
            builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sendCallResultPacket(videoCallPacket.getSender(), (byte) 0);
                }
            });
            runOnUiThread(() -> {
                AlertDialog alertDialog = builder.create();
                if (!this.isDestroyed()) {
                    alertDialog.show();
                }
            });
        }
    }

    private void sendCallResultPacket(User sender, byte result) {
        log.info("sendCallResultPacket sender:{}, result:{}", sender, result);
        VideoCallResultPacket resultPacket = new VideoCallResultPacket();
        resultPacket.setStatus(result);
        ByteBuf byteBuf = resultPacket.toNewBuf(0);
        GlobalObject.channel.writeAndFlush(new DatagramPacket(
                byteBuf, sender.getAddress()
        ));
    }
}
