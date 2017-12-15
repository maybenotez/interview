package com.win.app.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;

/**
 * Created by Administrator on 2017/12/15 0015.
 */
public class ReentrantLock implements Lock, java.io.Serializable {


    abstract static class Sync extends AbstractQueuedSynchronizer{

    }
    public void lock() {

    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public void unlcok() {

    }

    public Condition newCondition() {
        return null;
    }
}
