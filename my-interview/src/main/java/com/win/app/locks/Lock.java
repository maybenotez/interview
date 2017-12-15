package com.win.app.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * Created by Administrator on 2017/12/15 0015.
 */
public interface Lock {

 void lock();


 void lockInterruptibly() throws InterruptedException;


 boolean tryLock();


 boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

 void unlcok();

 Condition newCondition();
}
