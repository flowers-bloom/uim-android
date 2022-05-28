package com.github.flowersbloom.client.packet;

import com.github.flowersbloom.client.BizCommand;

import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class VideoCallResultPacket extends BasePacket {
    /**
     * if status value equals 1 is accepted, else is rejected.
     */
    byte status;

    public VideoCallResultPacket() {
        this.command = BizCommand.VIDEO_CALL_RESULT_PACKET;
    }

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        byteBuf.writeByte(status);
        return byteBuf;
    }
}
