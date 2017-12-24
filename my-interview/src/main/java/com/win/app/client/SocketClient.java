package com.win.app.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by Administrator on 2017/12/9 0009.
 */
public class SocketClient  {
    public static void main(String[] args) {
        try {

            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
             InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 10001);
            socketChannel.socket().bind(inetSocketAddress);

            ByteBuffer allocate = ByteBuffer.allocate(1024);
            String s = "i am win and u?";
            byte[] bytes = s.getBytes();
            allocate.put(bytes);
            socketChannel.write(allocate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
