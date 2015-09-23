package com.earasoft.framework.messaging

import java.util.concurrent.BlockingQueue

import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.StaticUtils
import com.hazelcast.core.Cluster
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IAtomicLong
import com.hazelcast.core.ITopic
import com.hazelcast.core.Member
import com.hazelcast.core.MembershipListener
import com.hazelcast.core.MessageListener

class HazelcastMessagingService implements MessagingService {
    private HazelcastInstance hazelcastInstance
    private Cluster cluster
    private Member localMember
    
    private ITopic<MessageBuilder> ownerEventsToWorkersTopic
    private ITopic<MessageBuilder> workerEventsToOwnerTopic
    private BlockingQueue<MessageBuilder> tasksQueue
    
    private IAtomicLong nodeCounter
    private long nodeId
    private String fullNodeID
    
    @Override
    public void start(boolean owner = false) {
        println "------------------------------------" + System.getProperty('hazelcast.config')
        hazelcastInstance = Hazelcast.newHazelcastInstance()
        nodeCounter = hazelcastInstance.getAtomicLong("nodeCounter");
        nodeId = nodeCounter.incrementAndGet()
        
        if(owner){
            fullNodeID = "owner-" + StaticUtils.getCurrentTimeString2('yyyyMMddHHmmss') + "-" + nodeId
        }else{
            fullNodeID = "worker-" + StaticUtils.getCurrentTimeString2('yyyyMMddHHmmss') + "-" + nodeId
        }
        
        cluster = hazelcastInstance.getCluster();
        localMember  = cluster.getLocalMember();
        localMember.setStringAttribute("nodeId", fullNodeID)
        
        ownerEventsToWorkersTopic = hazelcastInstance.getTopic("master-listener")
        workerEventsToOwnerTopic = hazelcastInstance.getTopic("slave-events")
        tasksQueue = hazelcastInstance.getQueue("tasks")
    }
    
    @Override
    public String getFullNodeID() {
        return fullNodeID;
    }
    
    @Override
    public void stop() {
        this.hazelcastInstance.shutdown()
    }

    @Override
    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public Member getLocalMember() {
        return localMember;
    }

    @Override
    public ITopic<MessageBuilder> getOwnerEventsToWorkersTopic() {
        return ownerEventsToWorkersTopic;
    }

    @Override
    public ITopic<MessageBuilder> getWorkerEventsToOwnerTopic() {
        return workerEventsToOwnerTopic;
    }

    @Override
    public BlockingQueue<MessageBuilder> getTasksQueue() {
        return tasksQueue;
    }

    @Override
    public void addTaskToQueue(MessageBuilder messageBuilder) {
        tasksQueue.put(messageBuilder)
    }

    @Override
    public String addClusterMembershipListener(MembershipListener listener) {
        return this.cluster.addMembershipListener(listener)
    }

    @Override
    public String addWorkerEventsToOwnerMessageListener(MessageListener listener) {
        return this.workerEventsToOwnerTopic.addMessageListener(listener)
    }

    @Override
    public void publishWorkerEvents(MessageBuilder messageBuilder) {
        this.workerEventsToOwnerTopic.publish(messageBuilder)
    }

    @Override
    public void publishOwnerEvents(MessageBuilder messageBuilder) {
        this.ownerEventsToWorkersTopic.publish(messageBuilder)
    }

    @Override
    public String addOwnerEventsToWorkersMessageListener(MessageListener listener) {
        return this.ownerEventsToWorkersTopic.addMessageListener(listener)
    }
    
}
