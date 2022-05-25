package com.github.flowersbloom.activity;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.github.flowersbloom.GlobalConstant;
import com.github.flowersbloom.adapter.ChatMsgAdapter;
import com.github.flowersbloom.client.handler.MessageAcceptHandler;
import com.github.flowersbloom.databinding.MainActivityBinding;
import com.github.flowersbloom.entity.ChatMsg;
import com.github.flowersbloom.util.SoftInputListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.flowersbloom.udp.NettyClient;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.handler.MessageListener;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.github.flowersbloom.udp.packet.BroadcastDataPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class MainActivity extends BaseActivity
        implements Handler.Callback, MessageListener {
    public static final int SCROLL_BOTTOM_SIGNAL = 1;
    public static final String TAG = "MainActivity";

    private MainActivityBinding binding;
    private ChatMsgAdapter adapter;
    public Handler handler;
    private static NettyClient nettyClient;
    public static NioDatagramChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, 1);

        handler = new Handler(this);

        initView();
        bindData();
        initClient();
        initListener();
    }

    private void initClient() {
        new Thread(() -> {
            nettyClient = new NettyClient(GlobalConstant.user,
                    GlobalConstant.serverAddress,
                    Arrays.asList(new MessageAcceptHandler()));
            channel = nettyClient.datagramChannel;
            Log.i(TAG, "nettyClient start");
        }).start();
    }

    public void initView() {
        initTitle();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.msgListRv.setLayoutManager(layoutManager);
    }

    public void bindData() {
        List<ChatMsg> msgList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ChatMsg chatMsg = new ChatMsg();
            msgList.add(chatMsg);
            chatMsg.setItemType(ChatMsg.LEFT_TYPE);
            chatMsg.setUserId("1");
            chatMsg.setUserNickname("测试用户");
            chatMsg.setContent("hello");
            chatMsg.setSendTime(new Date());
        }
        adapter = new ChatMsgAdapter(this, "", msgList);
        binding.msgListRv.setAdapter(adapter);
    }

    public void initListener() {
        MessageCallback.subscribe(this);
        binding.sendBtn.setOnClickListener(v -> {
            String content = binding.inputEt.getText().toString();
            ChatMsg chatMsg = new ChatMsg();
            adapter.insertItem(chatMsg);
            chatMsg.setItemType(ChatMsg.RIGHT_TYPE);
            chatMsg.setUserId("");
            chatMsg.setUserAvatar("");
            chatMsg.setUserNickname("flowers-bloom");
            chatMsg.setContent(content);
            chatMsg.setSendTime(new Date());
            binding.inputEt.setText("");
            msgListScrollToBottom();

            BroadcastDataPacket broadcastDataPacket = new BroadcastDataPacket();
            broadcastDataPacket.setSenderId(GlobalConstant.user.getUserId());
            broadcastDataPacket.setContent(content);
            String out = JSON.toJSONString(broadcastDataPacket);
            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(out.getBytes()), GlobalConstant.serverAddress
            ));
        });
        SoftInputListener.setListener(this, new SoftInputListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                recyclerLayoutMoveUp();
                msgListScrollToBottom();
            }

            @Override
            public void keyBoardHide(int height) {
                recyclerLayoutMoveDown();
                msgListScrollToBottom();
            }
        });
    }

    public void initTitle() {
        binding.titleLayout.backIc.setVisibility(View.GONE);
        binding.titleLayout.titleNameTv.setText("UIM");
        binding.titleLayout.actionBarIv.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, VideoChatActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isTouchView(binding.msgListRv, ev)) {
            hideSoftInputClearFocus(binding.inputEt);
        }else if (isTouchView(binding.bottomLayout, ev)) {
            handler.sendEmptyMessage(SCROLL_BOTTOM_SIGNAL);
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isTouchView(View view, MotionEvent event) {
        if (view == null || event == null) {
            return false;
        }
        int[] leftTop = {0, 0};
        view.getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + view.getHeight();
        int right = left + view.getWidth();
        if (event.getRawX() > left && event.getRawX() < right
                && event.getRawY() > top && event.getRawY() < bottom) {
            return true;
        }
        return false;
    }

    private void msgListScrollToBottom() {
        binding.msgListRv.scrollToPosition(adapter.getItemCount()-1);
    }

    private void recyclerLayoutMoveUp() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.msgListRv.getLayoutParams();
        layoutParams.height = binding.msgListRv.getHeight();
        layoutParams.weight = 0.0f;
    }

    private void recyclerLayoutMoveDown() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.msgListRv.getLayoutParams();
        layoutParams.height = 0;
        layoutParams.weight = 1.0f;
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        switch (message.what) {

        }
        return false;
    }

    @Override
    public void handle(BasePacket basePacket) {
        if (basePacket instanceof BroadcastDataPacket) {
            BroadcastDataPacket broadcastDataPacket = (BroadcastDataPacket) basePacket;
            ChatMsg chatMsg = new ChatMsg();
            if (broadcastDataPacket.getSenderId().equals(GlobalConstant.user.getUserId())) {
                chatMsg.setItemType(ChatMsg.RIGHT_TYPE);
            }else {
                chatMsg.setItemType(ChatMsg.LEFT_TYPE);
            }
            chatMsg.setMsgType("");
            chatMsg.setUserId(broadcastDataPacket.getSenderId());
            chatMsg.setUserNickname("未知");
            chatMsg.setContent(broadcastDataPacket.getContent());
            chatMsg.setSendTime(new Date());
            runOnUiThread(() -> {
                adapter.insertItem(chatMsg);
                msgListScrollToBottom();
            });
        }
    }
}