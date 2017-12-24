package com.win.app.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * Created by Administrator on 2017/12/9 0009.
 */
public class SocketServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel;
        serverSocketChannel = ServerSocketChannel.open();
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        SocketAddress socketAddress = new InetSocketAddress(10001);
        serverSocketChannel.socket().bind(socketAddress);
        serverSocketChannel.configureBlocking(false);
        Selector open = Selector.open();
        SelectionKey register = serverSocketChannel.register(open, SelectionKey.OP_ACCEPT);
       /* while(open.select()>0){
            Set<SelectionKey> keys = open.keys();
            Iterator<SelectionKey> it = keys.iterator();
            if(it.hasNext()){
                SelectionKey next = it.next();
                it.remove();
                SelectableChannel channel = next.channel();
            }
        }*/
        while(true){
            Set<SelectionKey> selectionKeys = open.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey next = iterator.next();
                if ((next.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT){
                    ServerSocketChannel channel = (ServerSocketChannel) next.channel();
                    SocketChannel accept = channel.accept();
                    accept.configureBlocking(false);
                    accept.register(open,SelectionKey.OP_READ);
                    iterator.remove();
                }
                else if ((next.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ){
                   SocketChannel channel = (SocketChannel) next.channel();
                    while (true){
                        allocate.clear();
                        int read = channel.read(allocate);
                        if (read< 0){
                            break;
                        }
                        allocate.flip();
                    }
                    iterator.remove();
                }
            }
        }
    }
}
