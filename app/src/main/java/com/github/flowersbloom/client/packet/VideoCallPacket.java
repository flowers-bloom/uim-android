package com.github.flowersbloom.client.packet;

import com.alibaba.fastjson.JSON;
import com.github.flowersbloom.client.BizCommand;

import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class VideoCallPacket extends BasePacket {
//    String senderId;
//    String senderNickname;
//    InetSocketAddress senderAddress;
//    String receiverId;
    User sender;

    public VideoCallPacket() {
        this.command = BizCommand.VIDEO_CALL_PACKET;
    }

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        byteBuf.writeBytes(JSON.toJSONString(this).getBytes());
        return byteBuf;
    }
}
