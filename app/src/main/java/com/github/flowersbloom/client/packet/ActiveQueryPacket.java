package com.github.flowersbloom.client.packet;

import com.github.flowersbloom.client.BizCommand;

import io.github.flowersbloom.udp.packet.BasePacket;

public class ActiveQueryPacket extends BasePacket {
    public ActiveQueryPacket() {
        this.command = BizCommand.ACTIVE_QUERY_PACKET;
    }
}
