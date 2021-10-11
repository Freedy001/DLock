package com.freedy.dlock.bio.DistributedLock;

import java.net.Socket;

/**
 * @author Freedy
 * @date 2021/10/9 9:49
 */
public abstract class AbstractDLock {
    /**
     * 每个线程对应一个SocketProxy，用于区分不同的服务以及数据返回
     */
    private final ThreadLocal<SocketProxy> socketHolderThreadLocal = new ThreadLocal<>();

    abstract boolean tryAcquire();

    abstract boolean tryRelease();

    public SocketProxy getSocketProxy(){
        return socketHolderThreadLocal.get();
    }

    public void setSocketProxy(SocketProxy socketProxy){
        this.socketHolderThreadLocal.set(socketProxy);
    }

    public void setSocket(Socket socket){
        this.socketHolderThreadLocal.set(new SocketProxy(socket));
    }
}
