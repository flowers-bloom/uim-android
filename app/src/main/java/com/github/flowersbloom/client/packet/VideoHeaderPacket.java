package com.github.flowersbloom.client.packet;

import com.github.flowersbloom.client.BizCommand;

import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class VideoHeaderPacket extends BasePacket {
    int bytesLength;
    int totalCount;

    public VideoHeaderPacket() {
        this.command = BizCommand.VIDEO_HEADER_PACKET;
    }

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        int totalCount = (bytesLength / DEFAULT_SLICE_LENGTH) +
                (bytesLength % DEFAULT_SLICE_LENGTH == 0 ? 0 : 1);
        this.setTotalCount(totalCount);
        byteBuf.writeInt(totalCount);
        return byteBuf;
    }
}
