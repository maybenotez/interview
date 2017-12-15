package com.win.app.locks;

/**
 * Created by Administrator on 2017/12/15 0015.
 */
public  abstract class AbstractOwnableSynchronizer  implements java.io.Serializable{

    protected AbstractOwnableSynchronizer(){}

    private transient Thread exclusiveOwnerThread;

    protected final void setExclusiveOwnerThread(Thread thread){
        exclusiveOwnerThread = thread;
    }

    protected final Thread getExclusiveOwnerThread(){
        return exclusiveOwnerThread;
    }
}
