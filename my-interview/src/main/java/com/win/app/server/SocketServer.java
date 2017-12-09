package com.win.app.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/9 0009.
 */
public class SocketServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel;
        serverSocketChannel = ServerSocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress(10001);
        serverSocketChannel.socket().bind(socketAddress);
        serverSocketChannel.configureBlocking(false);
        Selector open = Selector.open();
        SelectionKey register = serverSocketChannel.register(open, SelectionKey.OP_ACCEPT);
        while(open.select()>0){
            Set<SelectionKey> keys = open.keys();
            Iterator<SelectionKey> it = keys.iterator();
            if(it.hasNext()){
                SelectionKey next = it.next();
                it.remove();
                SelectableChannel channel = next.channel();
            }
        }

    }
}
