package org.example.pre;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class test {

    public static void main(String[] args) throws IOException {

        //创建一个服务端套接字
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //将服务端套接字跟一个端口进行绑定
        serverSocketChannel.bind(new InetSocketAddress(8080));
        //将其设置为非阻塞
        serverSocketChannel.configureBlocking(false);

        //创建一个选择器
        Selector selector = Selector.open();
        //指定服务端套接字由哪个选择器选择，并指定什么事件可以被选择
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(true){
            //阻塞直到有服务端套接字发送事件
            selector.select();

            //这一次选择器选择的所有事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while(iterator.hasNext()){

                //拿出一个事件
                SelectionKey key = iterator.next();
                //将拿出的事件从集合中删除
                selectionKeys.remove(key);

                //判断事件类型

                //1. 事件类型是连接事件
                if(key.isAcceptable()){
                    //获取客户端连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //将其注册进一个选择器
                    socketChannel.register(selector,SelectionKey.OP_READ,SelectionKey.OP_WRITE);
                    //将客户端连接设置为非阻塞形式
                    socketChannel.configureBlocking(false);
                }

                // 2. 事件类型是读事件
                if(key.isReadable()){
                    //获取客户端连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int i = socketChannel.read(byteBuffer);

                    if(i!=-1){
                        //将缓冲区切换成读模式
                        byteBuffer.flip();
                        System.out.println(StandardCharsets.UTF_8.decode(byteBuffer));
                        byteBuffer.clear();
                    }
                    //数据全读过来了
                    else{
                        key.cancel();
                        socketChannel.close();
                    }
                }
            }

        }
    }
}
