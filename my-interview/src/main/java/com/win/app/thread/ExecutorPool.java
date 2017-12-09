package com.win.app.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/12/9 0009.
 */
public class ExecutorPool {

    public static  ExecutorService executorService;
    private static final Object object = new Object();
    private static final LinkedBlockingQueue queue = new LinkedBlockingQueue();
    public static ExecutorService getInstance(){
        if(executorService == null){
            synchronized (object){
                if (executorService == null){
                    executorService = new ThreadPoolExecutor(10,10,0L, TimeUnit.MILLISECONDS,queue);
                }
            }
        }
        return executorService;
    }

}
