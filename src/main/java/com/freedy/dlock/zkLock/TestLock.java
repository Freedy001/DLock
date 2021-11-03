package com.freedy.dlock.zkLock;

/**
 * @author Freedy
 * @date 2021/11/3 10:52
 */
public class TestLock {

    public static void main(String[] args) {
        DistributeLock lock = new DistributeLock("freedy");
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                lock.lock();
                System.out.println("do something");
                lock.unlock();
            }).start();
        }
    }
}
