package com.freedy.dlock.zkLock;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Freedy
 * @date 2021/11/1 17:24
 */
public class ZKUtils {

    private static volatile ZooKeeper zk;

//    private final static String address = "wslhost:2181/testConf";
    private final static String address = "wslhost:2181/testLock";


    public static ZooKeeper getZK() {
        if (zk!=null) return zk;
        synchronized (ZKUtils.class) {
            try {
                if (zk!=null) return zk;
                Thread currentThread = Thread.currentThread();
                zk = new ZooKeeper(address,1000,event -> {
                    System.out.println(event);
                    switch (event.getState()) {
                        case Unknown, Disconnected, NoSyncConnected, AuthFailed, ConnectedReadOnly, SaslAuthenticated, Expired, Closed -> {}
                        case SyncConnected -> LockSupport.unpark(currentThread);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            LockSupport.park();
        }
        return zk;
    }

}
