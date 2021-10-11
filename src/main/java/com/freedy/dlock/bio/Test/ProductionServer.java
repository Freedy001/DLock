package com.freedy.dlock.bio.Test;



import com.freedy.dlock.bio.DistributedLock.SocketProxy;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Freedy
 * @date 2021/10/7 17:49
 */
@SuppressWarnings("InfiniteLoopStatement")
public class ProductionServer {

    private static Integer count = 250000;

    public static void main(String[] args) throws Exception {
        System.out.println("Production server start success,wait for to be connected!");
        ServerSocket socket = new ServerSocket(4321);
        for (; ; ) {
            Socket accept = socket.accept();
            new Thread(new Task(accept)).start();
        }
    }

    static class Task implements Runnable {
        private final SocketProxy holder;
        private final Socket socket;

        public Task(Socket socket) {
            System.out.println("receive one connection [" + socket + "]");
            this.holder = new SocketProxy(socket);
            this.socket = socket;
        }

        @Override
        public void run() {
            while (!socket.isClosed()) {
                String message = holder.readMessage();
                if (message.contains("reduce")) {
                    String reduce = message.replace("reduce", "");
                    try {
                        count -= Integer.parseInt(reduce);
                    } catch (NumberFormatException e) {
                        holder.writeMessage(e.getMessage());
                    }
                }
                holder.writeMessage("remain count is " + count);
                System.out.println("remain count is " + count);
            }
        }
    }

}
