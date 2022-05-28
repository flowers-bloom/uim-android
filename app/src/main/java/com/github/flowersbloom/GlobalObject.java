package com.github.flowersbloom;

import java.net.InetSocketAddress;

import io.github.flowersbloom.udp.NettyClient;
import io.github.flowersbloom.udp.entity.User;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class GlobalObject {
    public static NettyClient nettyClient;
    public static NioDatagramChannel channel;

    public static final InetSocketAddress serverAddress =
            new InetSocketAddress("192.168.1.11", 8080);
    public static final User ME = new User("2",
            "xhh", String.valueOf(R.drawable.boy_avatar),
            new InetSocketAddress(9002));
}
