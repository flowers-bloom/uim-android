package com.github.flowersbloom.client;

import io.github.flowersbloom.udp.Command;

public class BizCommand extends Command {
    /**
     * 从 20 开始
     */
    public static final byte P2P_DATA_PACKET = 20;
    public static final byte BROADCAST_DATA_PACKET = 21;
    public static final byte VIDEO_DATA_PACKET = 22;
    public static final byte VIDEO_HEADER_PACKET = 23;
    public static final byte ACTIVE_QUERY_PACKET = 24;
    public static final byte ACTIVE_DATA_PACKET = 25;
    public static final byte ADDRESS_QUERY_PACKET = 26;
    public static final byte ADDRESS_DATA_PACKET = 27;
    public static final byte VIDEO_CALL_PACKET = 28;
    public static final byte VIDEO_CALL_RESULT_PACKET = 29;
}
