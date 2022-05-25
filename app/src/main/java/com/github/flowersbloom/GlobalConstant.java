package com.github.flowersbloom;

import java.net.InetSocketAddress;

import io.github.flowersbloom.udp.entity.User;

public class GlobalConstant {
    public static final InetSocketAddress serverAddress =
            new InetSocketAddress("192.168.1.11", 8080);
    public static final User user = new User("1", "kitty", "",
            new InetSocketAddress(9002));
    public static final String DST_ADDRESS = "192.168.1.11";
    public static final int DST_PORT = 65505;
}
