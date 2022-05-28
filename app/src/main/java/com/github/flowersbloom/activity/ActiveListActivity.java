package com.github.flowersbloom.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.flowersbloom.GlobalObject;
import com.github.flowersbloom.adapter.ActiveAdapter;
import com.github.flowersbloom.client.packet.ActiveDataPacket;
import com.github.flowersbloom.client.packet.ActiveQueryPacket;
import com.github.flowersbloom.databinding.ActiveListActivityBinding;

import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveListActivity extends BaseActivity {
    private ActiveListActivityBinding binding;
    private ActiveAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActiveListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        bindData();
    }

    private void bindData() {
        ActiveQueryPacket activeQueryPacket = new ActiveQueryPacket();
        ByteBuf byteBuf = activeQueryPacket.toNewBuf(0);
        GlobalObject.channel.writeAndFlush(new DatagramPacket(
                byteBuf, GlobalObject.serverAddress
        ));
    }

    private void initView() {
        initTitle();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.activeListRv.setLayoutManager(manager);
    }

    private void initTitle() {
        binding.titleLayout.titleNameTv.setText("在线列表");
        binding.titleLayout.actionBarIv.setVisibility(View.GONE);
        binding.titleLayout.backIc.setOnClickListener(v -> finish());
    }

    @Override
    public void handle(BasePacket basePacket) {
        super.handle(basePacket);
        if (basePacket instanceof ActiveDataPacket) {
            ActiveDataPacket activeDataPacket = (ActiveDataPacket) basePacket;
            log.info("init active adapter");
            adapter = new ActiveAdapter(this, activeDataPacket.getActiveList());
            runOnUiThread(() -> {
                binding.activeListRv.setAdapter(adapter);
            });
        }
    }
}
