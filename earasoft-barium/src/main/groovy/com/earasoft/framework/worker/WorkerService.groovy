package com.earasoft.framework.worker

import java.util.concurrent.CountDownLatch



public interface WorkerService {

    /**
     * Used to start Worker
     */
    public abstract start();

    /**
     * Used to start Worker
     */
    public abstract start(final CountDownLatch waitTillHazelcastInit);
    
    /**
     * Used to stop worker
     */
    public void stop();
}