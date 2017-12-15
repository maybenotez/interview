package com.win.app.locks;

/**
 * Created by Administrator on 2017/12/15 0015.
 */
public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer implements java.io.Serializable {

            protected AbstractQueuedSynchronizer(){}

            static final class Node{
                static final Node SHARED  = new Node();

                static final Node exclusive = null;


                static final int CANCELLED =1;

                static final int SIGNAL =-1;

                static final int CONDITION = -2;

                static final int PROPAGATE  =-3;


                volatile int waitStatus;

                volatile Node prev;


                volatile  Node next;

                volatile Thread thread;

                Node nextWaiter;


                final boolean isShared(){
                    return nextWaiter ==SHARED;
                }



            }
}
