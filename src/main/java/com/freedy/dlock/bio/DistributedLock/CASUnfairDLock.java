package com.freedy.dlock.bio.DistributedLock;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Freedy
 * @date 2021/10/9 10:33
 */
public class CASUnfairDLock extends AbstractDLock {
    /**
     * cas锁
     */
    private final AtomicBoolean lock = new AtomicBoolean(false);
    /**
     * 当前占锁的socket
     */
    private SocketProxy currentProxy;
    /**
     * 等待队列
     */
    private final Queue<SocketProxy> retain = new LinkedList<>();

    @Override
    boolean tryAcquire() {
        SocketProxy proxy = getSocketProxy();
        if (lock.compareAndSet(false, true)) {
            currentProxy = proxy;
            proxy.writeMessage("ok");
            return true;
        } else {
            synchronized (retain) {
                retain.add(proxy);
            }
            return false;
        }
    }

    @Override
    boolean tryRelease() {
        SocketProxy proxy = getSocketProxy();
        if (proxy == currentProxy) {
            lock.set(false);
            currentProxy=null;
            proxy.writeMessage("ok");
            synchronized (retain) {
                if (!retain.isEmpty()) {
                    retain.poll().writeMessage("retry");
                    System.out.println("[" + proxy.getRemoteSocketAddress() + "] ==> 唤醒下个线程！");
                }
            }
            return true;
        } else {
            proxy.writeMessage("failed");
        }
        return false;
    }
}
