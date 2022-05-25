package com.github.flowersbloom;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.github.flowersbloom.activity.MainActivity;
import com.github.flowersbloom.util.YuvUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import io.github.flowersbloom.udp.packet.VideoDataPacket;
import io.github.flowersbloom.udp.packet.VideoHeaderPacket;
import io.github.flowersbloom.udp.transfer.PacketTransfer;
import io.github.flowersbloom.udp.transfer.TransferFuture;

public class H264Encoder {
    private static final String TAG = "H264Encoder";
    private MediaCodec mediaCodec;
    int width = 1080;
    int height = 1920;
    //    nv21转换成nv12的数据
    byte[] nv12;
    //    旋转之后的yuv数据
    byte[] yuv;

    public static final int NAL_SPS = 7;
    public static final int NAL_I = 5;
    private byte[] sps_buf;
    int frameIndex;


    public H264Encoder(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void startLive() {
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

            //预览图宽和高是经过旋转的，原始画面是横着的宽高，所以在编码时原宽高就需要对调一下
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, height, width);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 1920);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2); //IDR帧刷新时间
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
            // yuv
            int bufferLength = width * height * 3 / 2;
            nv12 = new byte[bufferLength];
            yuv = new byte[bufferLength];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///摄像头调用
    public int encodeFrame(byte[] input) {
//        旋转 nv21-nv12
        nv12 = YuvUtil.nv21toNV12(input);
        YuvUtil.portraitData2Raw(nv12, yuv, width, height);

        int inputBufferIndex = mediaCodec.dequeueInputBuffer(100000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(yuv);
            //编码时加入时间戳
            long presentationTimeUs = computePresentationTime(frameIndex);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
            frameIndex++;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            dealFrame(outputBuffer, bufferInfo);
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
        return 0;
    }

    // pts，通过每一帧
    private long computePresentationTime(long frameIndex) {
        //132 为偏移量，可以自定义，保证PTS 不从0开始，因为编码器初始化需要时间，视频里时间单位是微秒，而帧率是15 所以每一帧播放的时间戳是frameIndex*1000000/15
        return 132 + frameIndex * 1000000 / 15;
    }

    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo) {
        int offset = 4;
        if (bb.get(2) == 0x01) {
            offset = 3;
        }
        int type = (bb.get(offset) & 0x1F);
        if (type == NAL_SPS) {
            sps_buf = new byte[bufferInfo.size];
            bb.get(sps_buf);
        } else if (type == NAL_I) {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            byte[] newBuf = new byte[sps_buf.length + bytes.length];
            System.arraycopy(sps_buf, 0, newBuf, 0, sps_buf.length);
            System.arraycopy(bytes, 0, newBuf, sps_buf.length, bytes.length);
            sendData(newBuf);
        } else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            sendData(bytes);
        }
    }

    public void sendData(byte[] bytes) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "encode bytes length: " + bytes.length);
        VideoHeaderPacket videoHeaderPacket = new VideoHeaderPacket();
        videoHeaderPacket.setBytesLength(bytes.length);
        VideoDataPacket videoDataPacket = new VideoDataPacket();
        videoDataPacket.setBytes(bytes);

        long cur = System.currentTimeMillis();
        PacketTransfer transfer = new PacketTransfer();
        TransferFuture future = transfer.channel(MainActivity.channel)
                .dstAddress(new InetSocketAddress(GlobalConstant.DST_ADDRESS, GlobalConstant.DST_PORT))
                .headerPacket(videoHeaderPacket)
                .dataPacket(videoDataPacket)
                .isSlice(true)
                .execute();
        future.addListener(f -> {
            if (f.isSuccess()) {
                Log.i(TAG, "videoPacket send success, serialNumber, "+
                        videoHeaderPacket.getSerialNumber()+" ,cost: ms"+
                        (System.currentTimeMillis() - cur));
            }
        });
    }
}
