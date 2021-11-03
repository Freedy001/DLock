package com.freedy.dlock.zkLock;

import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Freedy
 * @date 2021/11/3 10:54
 */
public class DistributeLock implements Watcher,
        AsyncCallback.StringCallback, AsyncCallback.Children2Callback,
        AsyncCallback.StatCallback, AsyncCallback.VoidCallback {

    private final ZooKeeper zk = ZKUtils.getZK();
    private final String namespace;
    private final ConcurrentHashMap<String, Thread> whoWatchIt = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Thread, String> threadPathMap = new ConcurrentHashMap<>();
    private transient Thread lockThread;
    @Getter
    @Setter
    private volatile int state;

    public DistributeLock(String namespace) {
        this.namespace = namespace;
        try {
            Stat stat = zk.exists("/" + namespace, false);
            if (stat == null) {
                String s = zk.create("/" + namespace, new byte[]{0}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                System.out.println(s);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Watcher
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {
            String path = event.getPath();
            zk.getChildren("/" + namespace, false, this, whoWatchIt.get(path));
            whoWatchIt.remove(path);
        }
    }

    //AsyncCallback.StringCallback
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name != null) {
            threadPathMap.put(((Thread) ctx), name);
            zk.getChildren("/" + namespace, false, this, ctx);
        }
    }

    //AsyncCallback.Children2Callback
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        Thread mainThread = (Thread) ctx;
        String pathName = threadPathMap.get(mainThread);

        Collections.sort(children);
        int i = children.indexOf(pathName.replace("/" + namespace + "/", ""));
        if (i == 0) {
            lockThread = mainThread;
            System.out.println(mainThread.getName() + "获取锁成功！ node path:" + pathName);
            LockSupport.unpark(mainThread);
        } else {
            String preNode = "/" + namespace + "/" + children.get(i - 1);
            System.out.println("监测前一个元素:" + preNode);
            whoWatchIt.put(preNode, (Thread) ctx);
            zk.exists(preNode, this, this, ctx);

        }
    }

    //StatCallback
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat == null)
            zk.getChildren("/" + namespace, false, this, ctx);
    }


    public void lock() {
        Thread currentThread = Thread.currentThread();
        if (lockThread != currentThread) {
            zk.create("/" + namespace + "/lock", currentThread.getName().getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, currentThread);
            LockSupport.park();
        } else {
            int newState = getState() + 1;
            setState(newState);
            System.out.println("锁重入,当前重入次数" + newState);
        }
    }


    public void unlock() {
        Thread thread = Thread.currentThread();
        if (lockThread == thread) {
            int state = getState();
            if (state == 0) {
                String pathName = threadPathMap.get(thread);
                zk.delete(pathName,-1, this, thread);
            } else {
                setState(state - 1);
            }
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx) {
        System.out.println("解锁成功！");
    }
}
