package com.earasoft.framework.owner;

import com.earasoft.framework.common.MessageBuilder;
import com.hazelcast.core.Message;

public interface TaskOwnerService {

    /**
     * Handle Worker Topic Events
     * @param message
     */
    public abstract void workerEventsMessageHandler(Message<MessageBuilder> message);

    /**
     * Handle Finished Task Event
     * @param messageResults
     */
    abstract public void handleFinishedTaskEvent(MessageBuilder messageResults)

    /**
     * Handle Taking Task Event
     * @param messageResults
     */
    abstract public void handleTakingTaskEvent(MessageBuilder messageResults)

    /**
     * Used to Handle misc events
     * @param messageResults
     */
    abstract public void handleMiscEvents(MessageBuilder messageResults)

    /**
     * Shutdown
     */
    public abstract void shutdown();

    /**
     * Shutdown method for Subclass that extend this class
     */
    abstract public void shutdownChild()
    
    abstract public void startServiceSystem();
    abstract public void startServiceSystem(int sparkPort);
    abstract public void startServiceSystem(int sparkPort, boolean startWorkerDaemon);
    
    abstract public void startDaemonWorker();
    abstract public void startWork(Map settings);

}