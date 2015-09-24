package com.earasoft.framework.owner;

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spark.Request
import spark.Response
import spark.Route
import spark.Spark

import com.earasoft.framework.common.HazelUtils
import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.StaticUtils
import com.earasoft.framework.messaging.HazelcastMessagingService
import com.earasoft.framework.messaging.MessagingService
import com.earasoft.framework.worker.GenericHazelcastWorker
import com.google.inject.Inject
import com.hazelcast.core.MemberAttributeEvent
import com.hazelcast.core.MembershipEvent
import com.hazelcast.core.MembershipListener
import com.hazelcast.core.Message
import com.hazelcast.core.MessageListener

public abstract class TaskOwnerBase implements TaskOwnerService {
    private static final Logger loggerBase = LoggerFactory.getLogger(TaskOwnerBase.class)
    
    protected MessagingService messagingService
    
    @Inject
    public TaskOwnerBase(MessagingService messagingService){
        this.messagingService = messagingService
        this.loaderSlave = new GenericHazelcastWorker(new HazelcastMessagingService())
    }
    
    /*
     * Worker Variables
     */
    protected final GenericHazelcastWorker loaderSlave
    protected final AtomicBoolean loaderWorkerStarted = new AtomicBoolean(false)
    
    /*
     * Task Variables
     */
    protected Long taskId = 1 //Used to id task
    protected List<Map<Object, Object>> endPoints = new LinkedList<Map<Object, Object>>()
    protected Map<String, Object> webStore = new TreeMap()
    protected Map<String, Object> tasksStatus = new TreeMap()
    protected Set<String> currentRunningTaskSet = new TreeSet()
    
    /**
     * Preq Check for Port
     */
    protected void preqCheck(int sparkPort = 8189){
        if(!StaticUtils.checkAvailablePort(sparkPort)){
            loggerBase.error("Port is not Available shutting down. Might mean another master already exist in is machine")
            loggerBase.error("Failed to start master")
            return
        }
    }
    
    /**
     * Register EndPoints
     */
    protected void registerEndPoint(String url, String desc, Closure closure){
        endPoints.add(['url':url, 'desc': desc])
        Spark.get(url, "application/json", new Route() {
                    @Override
                    public Object handle(Request request, Response response) {
                        response.raw().setContentType("application/json");
                        
                        String data = null
                        try{
                            data = closure(request, response).toGsonString()
                        }catch(Exception e){
                            data = ['success': false, "message": e.getMessage()].toGsonString()
                        }
                        return data
                    }
                });
    }
    
    protected void startWebServer(int sparkPort = 8189){
        Spark.port(sparkPort);
        
        registerEndPoint('/','index',
                {Request request, Response response ->
                    Map data = ['message':'Running']
                    data['endpoints'] = endPoints
                    return data
                });
        
        registerEndPoint('/webstore.json','Location of webstore',
                {Request request, Response response ->
                    webStore
                });
        
        
        registerEndPoint('/status.json','Status of Hazelcast',
                {Request request, Response response ->
                    ["status": 'running',
                        'info': HazelUtils.getClusterMemberInfo(messagingService.getCluster()),
                        'local': ['socketAddress': messagingService.getLocalMember().getSocketAddress().toString(),
                            'uuid': messagingService.getLocalMember().getUuid(),
                            'attributes': messagingService.getLocalMember().getAttributes()]]
                });
        
        
        registerEndPoint('/queue.json','queue info',
                {Request request, Response response ->
                    ['queueSize': this.messagingService.getTasksQueue().size() ,
                        'tasksStatus': tasksStatus,
                        'currentRunningTaskSet': currentRunningTaskSet]
                });
        
        registerEndPoint('/status/local.json','Status of local hazelcast instance',
                {Request request, Response response ->
                    Map data = [:]
                    if(messagingService.getLocalMember() !=null){
                        data['socketAddress'] =
                                data['uuid'] = messagingService.getLocalMember().getUuid()
                        data['attributes'] = messagingService.getLocalMember().getAttributes()
                        data['error'] = false
                    }else{
                        data['message'] = 'Not Ready'
                        data['error'] = true
                    }
                    
                    return data
                });
        
        loggerBase.info("Webservice port:" + sparkPort)
        
        println "NON--" + this.messagingService
    }
    
    private boolean taskPutIntoQueue = false
    /**
     * Helper method to put task into queue
     */
    protected void putTaskIntoQueue(Map taskContext, String classStringForTask, boolean delay = true){
        if(taskContext['_id'] == null){
            taskContext['_id'] = taskId
            taskId ++
        }
        
        if(tasksStatus[taskContext['_id']] == null) tasksStatus[taskContext['_id']] = [:]
        tasksStatus[taskContext['_id']]['taskContext'] = taskContext
        tasksStatus[taskContext['_id']]['status'] = 'Scheduled'
        
        MessageBuilder currentMessage = MessageBuilder.build().setTaskContext(taskContext)
                .setTaskClass(classStringForTask)
                .setOwner(messagingService.getFullNodeID())
                .setEventType("TaskGeneration")
                .setDelay(delay)
        
        messagingService.addTaskToQueue(currentMessage)
        taskPutIntoQueue = true
    }
    
    /**
     * Hooks
     */
    protected void membershipMonitoring(){
        messagingService.addClusterMembershipListener(new MembershipListener() {
                    public void memberAdded( MembershipEvent membershipEvent ) {
                        System.out.println( "MemberAdded " + membershipEvent )
                    }
                    
                    public void memberRemoved( MembershipEvent membershipEvent ) {
                        System.out.println( "MemberRemoved " + membershipEvent )
                    }
                    
                    void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent){
                        
                    }
                }
                )
        
        messagingService.addWorkerEventsToOwnerMessageListener(new MessageListener<MessageBuilder>(){
                    @Override
                    public void onMessage(Message<MessageBuilder> message) {
                        workerEventsMessageHandler(message)
                    }
                })
        
    }
    
    /* (non-Javadoc)
     * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#workerEventsMessageHandler(com.hazelcast.core.Message)
     */
    @Override
    public void workerEventsMessageHandler(Message<MessageBuilder> message){
        if(webStore['history'] == null){
            webStore['history']= []
        }
        
        MessageBuilder messageResults = message.getMessageObject()
        println "-------------------INCOMING----------------"  + messageResults
        
        
        if(messageResults.isSameOwner(messagingService.getFullNodeID())){
            loggerBase.info("HISTORY: " + messageResults.toMap().toGsonString())
            //ESClient.logEvent(messageResults.toMap(), 'testing')
            
            if(messageResults.getEventType().equals("TakingTask")){
                webStore['history'].add(messageResults)
                tasksStatus[messageResults.getTaskContext()['_id']]['status'] = 'In Progress'
                currentRunningTaskSet.add(messageResults.getNodeId() + "-" + messageResults.getTaskContext()['_id'])
                
                try{
                    handleTakingTaskEvent(messageResults)
                }catch(Exception E){
                    loggerBase.error("ERROR WITH HANDLING MESSAGE")
                }
                
                
            }else if(messageResults.getEventType().equals("FinishedTask")){
                webStore['history'].add(messageResults)
                currentRunningTaskSet.remove(messageResults.getNodeId() + "-" + messageResults.getTaskContext()['_id'])
                
                if(messageResults.hasResults()){
                    tasksStatus[messageResults.getTaskContext()['_id']]['status'] = 'Finished'
                    
                    try{
                        handleFinishedTaskEvent(messageResults)
                    }catch(Exception E){
                        loggerBase.error("ERROR WITH HANDLING MESSAGE")
                    }
                }else{
                    loggerBase.warn("MESSAGE SHOULD HAVE Results: " + messageResults)
                    tasksStatus[messageResults.getTaskContext()['_id']]['status'] = 'MSG Error'
                }
            }else{
                try{
                    handleMiscEvents(messageResults)
                }catch(Exception E){
                    loggerBase.error("ERROR WITH HANDLING MESSAGE")
                }
            }
        }else{
            loggerBase.warn("Ignored Message because not for this owner - " + messageResults)
        }
    }
    
    /* (non-Javadoc)
     * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#handleFinishedTaskEvent(com.comsort.personify.loading.framework.MessageBuilder)
     */
    @Override
    abstract public void handleFinishedTaskEvent(MessageBuilder messageResults)
    
    /* (non-Javadoc)
     * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#handleTakingTaskEvent(com.comsort.personify.loading.framework.MessageBuilder)
     */
    @Override
    abstract public void handleTakingTaskEvent(MessageBuilder messageResults)
    
    /* (non-Javadoc)
     * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#handleMiscEvents(com.comsort.personify.loading.framework.MessageBuilder)
     */
    @Override
    abstract public void handleMiscEvents(MessageBuilder messageResults)
    /**
     * Method used to start up server
     */
    protected void startMasterServer(){
        messagingService.start(true)
        membershipMonitoring()
        loggerBase.info("HazelcastInstance Socket Address:" + messagingService.getLocalMember().getSocketAddress())
        
        //messagingService.publishOwnerEvents() //"Master Finished Initialization"  //TODO
        
    }
    
    /* (non-Javadoc)
     * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#shutdown()
     */
    @Override
    public void shutdown(){
        
        //messagingService.publishOwnerEvents() //"Master Shutdown"  //TODO
        
        if(loaderWorkerStarted.get()){
            loaderSlave.stop()
        }
        Spark.stop()
        
        messagingService.stop()
        shutdownChild()
    }
    
    /* (non-Javadoc)
     * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#shutdownChild()
     */
    @Override
    abstract public void shutdownChild()
    
    /**
     * Starting Hazelcast and Webserver
     */
    public void startServiceSystem(int sparkPort = 8189, boolean startWorkerDaemon = false){
        preqCheck(sparkPort)
        startWebServer(sparkPort)
        startMasterServer()
        
        if(startWorkerDaemon == true){
            startDaemonWorker()
        }
    }
    
    /**
     * Method used to start a Worker in the same JVM
     */
    public void startDaemonWorker(){
        final CountDownLatch waitTillHazelcastInit = new CountDownLatch(1)
        if(!loaderWorkerStarted.get()){
            Thread slave = new Thread(new Runnable(){
                        public void run(){
                            loaderWorkerStarted.set(true)
                            loaderSlave.start(waitTillHazelcastInit)
                        }
                    })
            slave.setName("Internal Worker")
            slave.setDaemon(true)
            slave.start() //Start a worker on this machine also
            waitTillHazelcastInit.await() //Wait till worker finished initiation of Hazelcast connection
        }
    }
    
    
    protected waitTillTasksFinished() {
        while(!messagingService.getTasksQueue().isEmpty() || !currentRunningTaskSet.isEmpty() || taskPutIntoQueue == true){ // Wait till all task are finished
            Thread.sleep(500)
            taskPutIntoQueue = false
        }
    }
    
}
