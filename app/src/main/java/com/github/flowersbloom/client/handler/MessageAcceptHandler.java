package com.github.flowersbloom.client.handler;

import com.alibaba.fastjson.JSON;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.NettyConstant;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.packet.AckPacket;
import io.github.flowersbloom.udp.packet.ConfirmPacket;
import io.github.flowersbloom.udp.packet.P2PDataPacket;
import io.github.flowersbloom.udp.packet.VideoDataPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MessageAcceptHandler extends SimpleChannelInboundHandler<DatagramPacket>
                        implements MessageCallback {
    private static final ConcurrentHashMap<Long, VideoContainer> MULTIPLE_SLICE_CACHE
            = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        long serialNumber = byteBuf.readLong();
        byte command = byteBuf.readByte();

        switch (command) {
            case Command.ACK_PACKET:
                AckPacket ackPacket = new AckPacket();
                ackPacket.setSerialNumber(serialNumber);
                notice(ackPacket);
                log.info("serialNumber:{} ack", serialNumber);
                break;
            case Command.P2P_DATA_PACKET:
                byte[] dst = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(dst);
                P2PDataPacket p2PDataPacket = JSON.parseObject(new String(dst), P2PDataPacket.class);

                ConfirmPacket confirmPacket = new ConfirmPacket();
                confirmPacket.setSenderId(p2PDataPacket.getSenderId());
                String out = JSON.toJSONString(confirmPacket);
                byteBuf = ByteBufAllocator.DEFAULT.buffer();
                byteBuf.writeLong(serialNumber);
                byteBuf.writeByte(confirmPacket.getCommand());
                byteBuf.writeBytes(out.getBytes());
                ctx.channel().writeAndFlush(new DatagramPacket(byteBuf, msg.sender()));
                break;
            case Command.VIDEO_HEADER_PACKET:
                int totalCount = byteBuf.readInt();
                VideoContainer container = new VideoContainer(totalCount);
                MULTIPLE_SLICE_CACHE.put(serialNumber, container);
                sendAckPacket(serialNumber, ctx.channel(), msg.sender());
                log.info("serialNumber:{} totalCount:{}", serialNumber, totalCount);
                new Thread(() -> {
                    try {
                        Thread.sleep(NettyConstant.MSG_SEND_TIMEOUT_SECONDS * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (MULTIPLE_SLICE_CACHE.containsKey(serialNumber)) {
                        log.error("serialNumber timeout:{}", serialNumber);
                        MULTIPLE_SLICE_CACHE.remove(serialNumber);
                    }
                }).start();
                break;
            case Command.VIDEO_DATA_PACKET:
                int sliceId = byteBuf.readInt();
                dst = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(dst);
                VideoSlice videoSlice = new VideoSlice();
                videoSlice.setId(sliceId);
                videoSlice.setBytes(dst);
                container = MULTIPLE_SLICE_CACHE.get(serialNumber);
                if (container == null) {
                    log.warn("container not found");
                }else {
                    container.getQueue().offer(videoSlice);
                    if (container.getQueue().size() == container.getTotalCount()) {
                        sendAckPacket(serialNumber, ctx.channel(), msg.sender());
                        log.info("serialNumber:{} recv all slice", serialNumber);

                        //merge
                        PriorityQueue<VideoSlice> queue = container.getQueue();
                        byteBuf = ByteBufAllocator.DEFAULT.buffer();
                        while (!queue.isEmpty()) {
                            VideoSlice slice = queue.poll();
                            byteBuf.writeBytes(slice.getBytes());
                        }

                        dst = new byte[byteBuf.readableBytes()];
                        byteBuf.readBytes(dst);
                        VideoDataPacket videoDataPacket = new VideoDataPacket();
                        videoDataPacket.setBytes(dst);
                        notice(videoDataPacket);

                        MULTIPLE_SLICE_CACHE.remove(serialNumber);
                    }
                }
                break;
            default:
                log.warn("command not found:{}", command);
        }
    }

    public void sendAckPacket(long serialNumber, Channel channel, InetSocketAddress address) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeLong(serialNumber);
        byteBuf.writeByte(Command.ACK_PACKET);
        channel.writeAndFlush(new DatagramPacket(byteBuf, address));
    }


    @Data
    public static class VideoContainer {
        int totalCount;
        PriorityQueue<VideoSlice> queue;

        public VideoContainer(int totalCount) {
            this.totalCount = totalCount;
            queue = new PriorityQueue<>(Comparator.comparingInt(VideoSlice::getId));
        }
    }

    @Data
    public static class VideoSlice {
        int id;
        byte[] bytes;
    }
}
