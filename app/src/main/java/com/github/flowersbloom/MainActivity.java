package com.github.flowersbloom;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.flowersbloom.adapter.ChatMsgAdapter;
import com.github.flowersbloom.databinding.MainActivityBinding;
import com.github.flowersbloom.entity.ChatMsg;
import com.github.flowersbloom.util.SoftInputListener;
import com.github.flowersbloom.util.ToastUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity implements Handler.Callback {
    public static final int SCROLL_BOTTOM_SIGNAL = 1;

    private MainActivityBinding binding;
    private ChatMsgAdapter adapter;
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler(this);

        initView();
        bindData();
        initListener();
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
        binding.sendBtn.setOnClickListener(v -> {
            ChatMsg chatMsg = new ChatMsg();
            adapter.insertItem(chatMsg);
            chatMsg.setItemType(ChatMsg.RIGHT_TYPE);
            chatMsg.setUserId("");
            chatMsg.setUserAvatar("");
            chatMsg.setUserNickname("flowers-bloom");
            chatMsg.setContent(binding.inputEt.getText().toString());
            chatMsg.setSendTime(new Date());
            binding.inputEt.setText("");
            msgListScrollToBottom();
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
            ToastUtil.toast("action click");
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
}