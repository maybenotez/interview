package com.win.app.locks;

import sun.misc.Unsafe;

import java.util.concurrent.locks.LockSupport;

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

                final Node predecessor() throws NullPointerException{
                    Node p = prev;
                    if(p == null){
                        throw new NullPointerException();
                    }
                    else{
                        return p;
                    }
                }

                Node(){}

                Node(Thread thread,Node mode){
                    this.thread = thread;
                    this.nextWaiter = mode;
                }

                Node(Thread thread,int waitStatus){
                    this.waitStatus = waitStatus;
                    this.thread = thread;
                }

            }

            private transient volatile Node head;

            private transient volatile Node tail;

            private volatile int state;


            protected final int getState(){
                return state;
            }

            protected final void setState(int newState){
                state = newState;
            }

            protected final boolean compareAndSetState(int expect,int update){
                return unsage.compareAndSwapInt(this,stateOffset,expect,update);
            }
        static final long spinForTimeoutThreshold = 1000L;

            private Node enq(final Node node){
                for (;;){
                    Node t = tail;
                    if( t == null){
                        if (compareAndSetHead(new Node())){
                            tail = head;
                        }else {
                            node.prev = t;
                            if (compareAndSetTail(t,node)){
                                t.next = node;
                                return t;
                            }
                        }
                    }
                }
            }


            private Node addWaiter(Node mode){
                Node node = new Node(Thread.currentThread(),mode);
                Node pred = null;
                if (pred!= null){
                    node.prev  = pred;
                    if (compareAndSetTail(pred,node)){
                        pred.next =node;
                        return node;
                    }
                }
                enq(node);
                return node;
            }

            private void setHead(Node node){
                head = node;
                node.thread =null;
                node.prev = null;
            }

            private void  unparkSuccessor(Node node){
                int ws  = node.waitStatus;
                if(ws< 0 ){
                    compareAndSetWaitStatus(node,ws,0);
                }
                Node  s = node.next;
                if (s == null ||s.waitStatus>0){
                    s = null;
                    for (Node t= tail;t!= null && t!= node;t=t.prev){
                        if (t.waitStatus<= 0){
                            s = t;
                        }
                    }
                }
                if (s != null){
                    LockSupport.unpark(s.thread);
                }
            }

            private void doReleaseShared(){
                for (;;){
                    Node h = head;
                    if (h != null && h != tail){
                        int ws =  h.waitStatus;
                        if (ws == Node.SIGNAL){
                            if (!compareAndSetWaitStatus(h,Node.SIGNAL,0)){
                                continue;
                            }
                            unparkSuccessor(h);
                        }
                        else if (ws == 0 && !compareAndSetWaitStatus(h,0,Node.PROPAGATE)){
                            continue;
                        }
                    }
                    if (h == head){
                        break;
                    }
                }
            }

            private void setHeadAndPropagate(Node node,int propagate){
                    Node h = head;
                    setHead(node);

                    if (propagate>0 ||h ==null || h.waitStatus<0 ||
                            (h =head)==null ||h.waitStatus<0){
                        Node s = node.next;
                        if (s == null|| s.isShared()){
                            doReleaseShared();
                        }
                    }
            }

            private void cancelAcquire(Node node){
                if (node == null){
                    return;
                }
                node.thread = null;
                Node pred= node.prev;
                while (pred.waitStatus>0){
                    node.prev = pred = pred.prev;
                }
                Node preNext = pred.next;
                node.waitStatus  =Node.CANCELLED;
                if (node ==tail && compareAndSetTail(node,pred)){
                    compareAndSetNext(pred,preNext,null);
                }else {
                    int ws;
                    if (pred !=head &&
                            ((ws = pred.waitStatus)==Node.SIGNAL ||
                                    (ws = pred.waitStatus) == Node.SIGNAL ||
                                    (ws<= 0 && compareAndSetWaitStatus(pred,ws,Node.SIGNAL))) &&
                            pred.thread != null){
                        Node next  = node.next;
                        if (next != null && next.waitStatus <= 0){
                            compareAndSetNext(pred,preNext,next);
                        }
                        else {
                            unparkSuccessor(node);
                        }
                        node.next =node;
                    }
                }
            }

            private  static boolean shouldParkAfterFailedAcquire(Node pred,Node node){
                int ws = pred.waitStatus;
                if (ws == Node.SIGNAL){
                    return true;
                }
                if (ws >0 ){
                    do {
                        node.prev = pred = pred.prev;
                    }while (pred.waitStatus > 0 );
                    pred.next = node;
                }else {
                    compareAndSetWaitStatus(pred,ws,Node.SIGNAL);
                }
                return false;
            }

            static void  selfInterrupt(){
                Thread.currentThread().interrupt();
            }

            private final boolean parkAndCheckInterrupt(){
                LockSupport.park(this);
                return Thread.interrupted();
            }

            final boolean acquireQueued(final Node node,int arg){
                boolean failed  = true;
                try{
                    boolean interuptted = false;
                    for (;;){
                        final Node  p = node.predecessor();
                        if (p  == head && tryAcquire(arg)){
                            setHead(node);
                            p.next =null;
                            failed = false;
                            return  interuptted;
                        }
                        if (shouldParkAfterFailedAcquire(p,node) && parkAndCheckInterrupt()){
                            interuptted = true;
                        }
                    }
                }finally {
                    if (failed){
                        cancelAcquire(node);
                    }
                }
            }
            protected boolean tryAcquire(int args){
                throw new UnsupportedOperationException();
            }
            private static final Unsafe unsage = Unsafe.getUnsafe();
            private static final long stateOffset;
            private static final long headOffset;
            private static final long tailOffset;
            private static final long waitStatusOffset;
            private static final long nextOffset;
            static {
                try {
                    stateOffset = unsage.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("state"));
                    headOffset =unsage.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("head"));
                    tailOffset = unsage.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
                    waitStatusOffset = unsage.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
                    nextOffset = unsage.objectFieldOffset(Node.class.getDeclaredField("next"));
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            }


            private final boolean compareAndSetHead(Node update){
                return unsage.compareAndSwapObject(this,headOffset,null,update);
            }

            private final boolean compareAndSetTail(Node expect,Node update){
                return unsage.compareAndSwapObject(this,tailOffset,expect,update);
            }

            private static final boolean compareAndSetWaitStatus(Node node,int expect,int update){
                return unsage.compareAndSwapInt(node,waitStatusOffset,expect,update);
            }

            private static final boolean compareAndSetNext(Node node,Node expect,Node update){
                return unsage.compareAndSwapObject(node,nextOffset,expect,update);
            }
}
