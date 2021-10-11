package com.freedy.dlock.bio.DistributedLock;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Freedy
 * @date 2021/10/7 18:35
 */
public class RLock implements Lock {

    private final SocketProxy socketHolder;
    private final ReentrantLock lock = new ReentrantLock();
    private int state = 0;


    public RLock(String host, int port) {
        try {
            socketHolder = new SocketProxy(new Socket(host, port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lock() {
        lock.lock();
        if (state == 0) {  //加进程分布式锁
            socketHolder.writeMessage("acquire");
            for (; ; ) {
                String str = socketHolder.readMessage();
                if (str.equals("ok")) {
                    System.out.println("加锁成功！[" + str + "]");
                    break;
                }
                if (str.equals("retry")) {
                    socketHolder.writeMessage("acquire");
                    System.out.println("重新尝试加锁！[" + str + "]");
                } else {
                    System.out.println("等待！[" + str + "]");
                }
            }
        }
        state++;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("暂不支持当前操作");
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException("暂不支持当前操作");
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("暂不支持当前操作");
    }

    @Override
    public void unlock() {
        state--;
        if (state == 0) {  //释放进程分布式锁
            socketHolder.writeMessage("release");
            String message = socketHolder.readMessage();
            if (message.equals("ok"))
                System.out.println("解锁成功![" + message + "]");
            else
                throw new RuntimeException("解锁失败![" + message + "]");
        }
        lock.unlock();
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("暂不支持当前操作");
    }
}