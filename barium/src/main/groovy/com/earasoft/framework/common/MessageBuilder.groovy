package com.earasoft.framework.common;

/**
 * Used as message passing object
 * @author riverema
 *
 */
public class MessageBuilder implements Serializable{
    private static final long serialVersionUID = -3224807806746286037L;
    
    @Override
    public String toString() {
        return toMap().toString()
    }
    
    public Map<String, Object> toMap(){
        return ['eventType': eventType, 
                'nodeId': nodeId, 
                'results' : results, 
                'task': taskContext, 
                'taskClass': taskClass,
                'taskVersion': taskVersion,
                'threadName': threadName,
                'owner': owner,
                'delay': delay,
                'messageTimestamp': StaticUtils.getCurrentTimeString()]
    }
    
    private String threadName = null;
    private String eventType = null;
    private String nodeId = null;
    private String taskClass = null;
    private String taskVersion = null;
    private String owner = null;
    private boolean delay = true
    private boolean hasResults = false;
    private Map<String, Object> results = new HashMap();
    
    public String getTaskVersion() {
        return taskVersion;
    }

    public MessageBuilder setTaskVersion(String taskVersion) {
        this.taskVersion = taskVersion;
        return this
    }
    
    public boolean isDelay() {
        return delay;
    }

    public MessageBuilder setDelay(boolean delay) {
        this.delay = delay;
        return this
    }
    
    public String getThreadName() {
        return threadName;
    }

    public MessageBuilder setThreadName(String threadName) {
        this.threadName = threadName
        return this
    }

    public String getOwner() {
        return owner
    }

    public boolean isSameOwner(String outsideOwner){
        return outsideOwner.equals(this.owner)
    }
    
    public MessageBuilder setOwner(String owner) {
        this.owner = owner
        return this
    }
    
    private Map<String, Object> taskContext = new HashMap();
    
    public String getTaskClass() {
        return taskClass;
    }

    public MessageBuilder setTaskClass(String taskClass) {
        this.taskClass = taskClass;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public static MessageBuilder build(){
        return new MessageBuilder();
    }
	
    public MessageBuilder setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getNodeId() {
        return nodeId;
    }

    public MessageBuilder setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }


    public Map<String, Object> getResults() {
        return results;
    }

    public MessageBuilder setResults(Map<String, Object> results) {
        this.results = results;
        hasResults=true;
        return this;
    }

    public boolean hasResults(){
        return this.hasResults;
    }
    
    public Map<String, Object> getTaskContext() {
        return taskContext;
        
    }

    public MessageBuilder setTaskContext(Map<String, Object> task) {
        this.taskContext = task;
        return this;
    }

    public static void main(String[] args) {
        println MessageBuilder.build().setEventType('hello')
    }
    
}
