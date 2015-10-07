package com.earasoft.framework.worker

import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.earasoft.framework.common.DebuggableThreadPoolExecutor
import com.earasoft.framework.common.HazelUtils
import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.MetaClassesBase
import com.earasoft.framework.messaging.MessagingModule
import com.earasoft.framework.messaging.MessagingService
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.hazelcast.core.Message
import com.hazelcast.core.MessageListener

/**
 * This Class is used to get tasks from Owner's Tasks Queue and report results to Owner
 * Owner is responsible for rescheduling tasks again based on the results (optional)
 * @author riverema
 *
 */
class GenericHazelcastWorker implements WorkerService {
    private static final Logger logger = LoggerFactory.getLogger(GenericHazelcastWorker.class)
    
	private Integer coreSize = Runtime.getRuntime().availableProcessors()
	private Integer scaleSize = 1
	private final int corePoolSize = coreSize
	private final int maxPoolSize  = coreSize
	private final long keepAliveTime = 10
	
    private DebuggableThreadPoolExecutor executor
    
    private final AtomicBoolean keepRunning = new AtomicBoolean(true)
    
    private MessagingService messagingService
    
    static{
		MetaClassesBase.load()
    }
	
    @Inject
    public GenericHazelcastWorker(MessagingService messagingService){
        this.messagingService = messagingService
    }
    
    private initialize() {
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(messagingService.getFullNodeID()+"-pool-graph-executer-%d").build()
		executor = new DebuggableThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), namedThreadFactory)
		logger.info("PoolExecuter[Cores:$coreSize, corePoolSize: $corePoolSize]")
		
		messagingService.start()
		
    }
    
    /* (non-Javadoc)
     * @see com.comsort.personify.loading.framework.worker.WorkerService#start()
     */
    /**
     * Used to start Worker
     *
     */
    @Override
    public start(final CountDownLatch waitTillHazelcastInit = null){
//		
//		
//		
//   Thread daemonThread = new Thread(new Runnable(){
//            @Override
//           public void run(){
//               try{
//               while(true){
//                   System.out.println("Daemon thread is running");
//				   Thread.sleep(1000)
//               }
//                  
//               }catch(Exception e){
//                  
//               }finally{
//                   System.out.println("Daemon Thread exiting"); //never called
//               }
//           }
//       }, "Daemon-Thread");
//      
//       daemonThread.setDaemon(true); //making this thread daemon
//       daemonThread.start();
//
//	   println 'hello'
		//return 
        initialize();
        if(waitTillHazelcastInit)
            waitTillHazelcastInit.countDown()
        
        logger.info(HazelUtils.getClusterMemberInfo(messagingService.getCluster()).toJsonString())
        
        messagingService.addOwnerEventsToWorkersMessageListener(new MessageListener<MessageBuilder>(){
                    @Override
                    public void onMessage(Message<MessageBuilder> message) {
                        System.out.println("Got message " + message.getMessageObject())
                        
                        if(message.getMessageObject().equals("Master Finished Initialization")){
                            
                        }

                    }
                })
        
		
        while(keepRunning.get()){  //Take one task until thread pool is full and report to master so that other slaves do not repeat work.
            int activeThreadCount = this.executor.getActiveCount()
            int maxThreadCount = this.executor.getMaximumPoolSize()
            
            if(activeThreadCount == maxThreadCount){
                Thread.sleep(10000)
                println "++++ Thread Count: (" + activeThreadCount +"/" + maxThreadCount  +") - Queue Size()" + messagingService.getTasksQueue().size()
            }else{
                long ms = System.currentTimeMillis()
                
                MessageBuilder currentTask = messagingService.getTasksQueue().poll(10 ,TimeUnit.SECONDS)
                println "---- Thread Count: (" + activeThreadCount +"/" + maxThreadCount  +") - Queue Size()" + messagingService.getTasksQueue().size()
                println "messagingService.getTasksQueue().poll - " + (System.currentTimeMillis() - ms)
                
                String fullNodeID = messagingService.getFullNodeID()
                
                if (currentTask != null) {
                    MessageBuilder currentTaskToMaster = MessageBuilder.build()
                            .setNodeId(fullNodeID)
                            .setEventType("TakingTask")
                            .setTaskContext(currentTask.getTaskContext())
                            .setTaskClass(currentTask.getTaskClass())
                            .setThreadName(fullNodeID + "-mainThread")
                            .setOwner(currentTask.getOwner())
                            .setDelay(currentTask.isDelay())
                    
                    messagingService.publishWorkerEvents(currentTaskToMaster)
                    
                    logger.debug(currentTaskToMaster.toString())
                    
                    Closure doneClosure = { Map task, Map results, String threadName ->
                        if(results['success'] == false){
                            if(task['_failureCount'] == null){
                                task['_failureCount'] = 1
                            }else{
                                task['_failureCount'] = task['_failureCount'] + 1
                            }
                        }
                        
                        currentTaskToMaster = MessageBuilder.build()
                                .setNodeId(fullNodeID)
                                .setEventType("FinishedTask")
                                .setTaskContext(currentTask.getTaskContext())
                                .setResults(results)
                                .setTaskClass(currentTask.getTaskClass())
                                .setThreadName(threadName)
                                .setOwner(currentTask.getOwner())
                                .setDelay(currentTask.isDelay())
                        
                        messagingService.publishWorkerEvents(currentTaskToMaster)
                        
                        logger.debug(currentTaskToMaster.toString())
                    }
                    
                    Map messagingContext = [:]
                    messagingContext['fullNodeID'] = fullNodeID
                    messagingContext['currentOwner'] = currentTask.getOwner()
                    
                    GenericTaskWrapper currentWrapper = new GenericTaskWrapper(messagingService, currentTask.getTaskContext(), doneClosure, currentTask.getTaskClass(), messagingContext)
                    scheduleJob(currentWrapper, currentTask.isDelay())
                }
            }
        }
        logger.info("LoaderSlave Ending")
    }
    
	public void scheduleJob(Runnable command, boolean wait = true){
		if(this.executor.getActiveCount() < corePoolSize){
			if(wait)
				Thread.sleep(4000)
			this.executor.execute(command)
		}else{
			this.executor.execute(command)
		}
		//println "Scheduled: " + command.class
	}
	
    @Override
    public void stop(){
        keepRunning.set(false)
        this.executor.shutdown()
        messagingService.stop()
    }
    
    static shutdownTimer(GenericHazelcastWorker loaderSlave){
        Thread slave = new Thread(new Runnable(){
                    public void run(){
                        Thread.sleep(25000)
                        loaderSlave.shutdown()
                    }
                })
        slave.setName("shutdownTimer")
        slave.setDaemon(true)
        slave.start() //Start a worker on this machine also
    }
    
    static main(args) {
        Injector injector = Guice.createInjector(new MessagingModule(), new WorkerModule());
        WorkerService loaderSlave = injector.getInstance(WorkerService.class)
        //shutdownTimer(loaderSlave)
        loaderSlave.start()
    }

    
}
