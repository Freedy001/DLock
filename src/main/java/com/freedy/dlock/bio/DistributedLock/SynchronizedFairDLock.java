package com.freedy.dlock.bio.DistributedLock;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Freedy
 * @date 2021/10/9 9:51
 */
public class SynchronizedFairDLock extends AbstractDLock {

    private static final Queue<SocketProxy> retain = new LinkedList<>();


    @Override
    synchronized boolean tryAcquire() {
        SocketProxy proxy = getSocketProxy();
            assert proxy != null;
        retain.add(proxy);
        if (proxy == retain.peek()) {
            proxy.writeMessage("ok");
            return true;
        } else {
            proxy.writeMessage("occupied");
            return false;
        }
    }

    @Override
    synchronized boolean tryRelease() {
        SocketProxy proxy = getSocketProxy();
        assert proxy != null;
        if (proxy == retain.peek()) {
            retain.poll();
            proxy.writeMessage("ok");
            if (!retain.isEmpty()) {
                SocketProxy peek = retain.peek();
                System.out.println("[" + peek.getRemoteSocketAddress() + "] <== 唤醒下个进程！");
                peek.writeMessage("ok");
            }
            return true;
        } else {
            proxy.writeMessage("failed");
            return false;
        }
    }

}
