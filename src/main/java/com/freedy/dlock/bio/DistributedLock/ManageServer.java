package com.freedy.dlock.bio.DistributedLock;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Freedy
 * @date 2021/10/7 17:10
 */
@SuppressWarnings("InfiniteLoopStatement")
public class ManageServer {

    private static final AbstractDLock distributedLock=new CASUnfairDLock();

    public static void main(String[] args) throws IOException {
        System.out.println("Manager server started , wait for connection!");
        ServerSocket socket = new ServerSocket(1234);
        int num = 0;
        for (; ; ) {
            Socket accept = socket.accept();
            new Thread(new Task(accept), "worker-thread" + num++).start();
        }
    }

    static class Task implements Runnable {
        SocketProxy proxy;

        public Task(Socket socket) {
            System.out.println("receive one connection [" + socket + "]");
            proxy=new SocketProxy(socket);
        }

        @Override
        public void run() {
            distributedLock.setSocketProxy(proxy);
            while (!proxy.isClosed()) {
                try {
                    String message = proxy.readMessage();
                    if (message.equals("acquire")) {
                        System.out.println("[" + proxy.getRemoteSocketAddress() + "] ==> 尝试获取锁");
                        if (distributedLock.tryAcquire())
                            System.out.println("[" + proxy.getRemoteSocketAddress() + "] ==> 获取锁成功！");
                    } else if (message.equals("release")) {
                        System.out.println("[" + proxy.getRemoteSocketAddress() + "] ==> 请求释放锁");
                        if (distributedLock.tryRelease())
                            System.out.println("[" + proxy.getRemoteSocketAddress() + "] ==> 释放锁成功！");
                        else
                            System.out.println("[" + proxy.getRemoteSocketAddress() + "] <== 释放锁失败！");
                    }
                } catch (Exception e) {
                    proxy.close();
                    if (e.getCause() instanceof SocketException)
                        System.out.println("[" + proxy.getRemoteSocketAddress() + "] <== "+e.getCause().getMessage());
                    else
                    e.printStackTrace();
                }
            }
        }
    }
}
