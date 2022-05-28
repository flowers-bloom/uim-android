package com.github.flowersbloom.client.packet;

import com.alibaba.fastjson.JSON;
import com.github.flowersbloom.client.BizCommand;

import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点对点数据报文
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class P2PDataPacket extends BasePacket {
    String senderId;
    String receiverId;
    String content;

    public P2PDataPacket() {
        this.command = BizCommand.P2P_DATA_PACKET;
    }

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        String json = JSON.toJSONString(this);
        byteBuf.writeBytes(json.getBytes());
        return byteBuf;
    }
}
