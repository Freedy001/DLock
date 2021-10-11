package com.freedy.dlock.bio.Test;


import com.freedy.dlock.bio.DistributedLock.RLock;
import com.freedy.dlock.bio.DistributedLock.SocketProxy;

import java.util.concurrent.CountDownLatch;

/**
 * @author Freedy
 * @date 2021/10/7 17:53
 */
public class Consumer {
    private final static RLock lock = new RLock("127.0.0.1", 1234);
    private final static SocketProxy holder = new SocketProxy("127.0.0.1", 4321);

    public static void main(String[] args) throws Exception {
        System.in.read(); //阻塞程序
        System.out.println("start to reduce stock");
        long millis = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(5);
        for (int j = 0; j < 5; j++) {
            new Thread(() -> {
                for (int i = 0; i < 10000; i++) {
                    lock.lock();
                    reduceProduct(1);
                    lock.unlock();
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("执行完毕总耗时=>" + (System.currentTimeMillis() - millis));
    }

    public static void reduceProduct(int num) {
        holder.writeMessage("reduce" + num);
        System.out.println(holder.readMessage());
    }
}
