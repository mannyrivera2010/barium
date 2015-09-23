package com.earasoft.framework.messaging;


import java.util.concurrent.BlockingQueue;

import com.earasoft.framework.common.MessageBuilder;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MessageListener;

/**
 * This is used for Messaging
 * @author riverema
 *
 */
public interface MessagingService {
    public void start();
    public void start(boolean owner);
    public void stop();
    
    public String getFullNodeID();
    public Cluster getCluster();
    public Member getLocalMember();
    public ITopic<String> getOwnerEventsToWorkersTopic();
    public ITopic<MessageBuilder> getWorkerEventsToOwnerTopic();
    public BlockingQueue<MessageBuilder> getTasksQueue();
    
    public void addTaskToQueue(MessageBuilder messageBuilder);
    
    public String addClusterMembershipListener(MembershipListener listener);
    
    public String addWorkerEventsToOwnerMessageListener(MessageListener listener);
    public String addOwnerEventsToWorkersMessageListener(MessageListener listener);
    
    public void publishWorkerEvents(MessageBuilder messageBuilder);
    public void publishOwnerEvents(MessageBuilder messageBuilder);
   
}
