package com.earasoft.framework.worker

import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.runtime.StackTraceUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.earasoft.framework.messaging.MessagingService;

/**
 * GenericTaskWrapper
 * This is a wrapper class that is used to launch threads for ThreadPool from classes that extends GenericTaskAbstract using java reflection
 * 
 * @author riverema
 *
 */
public class GenericTaskWrapper implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(GenericTaskWrapper.class)
    
    final private Map taskContext
    final private Closure doneClosure
    final private String taskClass
    final protected MessagingService messagingService
    final protected Map messagingContext
    /**
     * Constructor
     * @@param genericHazelcastWorker
     * @param taskInfo
     * @param doneClosure
     * @param taskClass
     * @param slaveEvents Optional
     */
    public GenericTaskWrapper(final MessagingService messagingService, final Map taskContext, final Closure doneClosure, final String taskClass,  final Map messagingContext){
        this.messagingService = messagingService
        this.taskContext = taskContext
        this.doneClosure = doneClosure
        this.taskClass = taskClass
        this.messagingContext = messagingContext
    }
    
    @Override
    public void run() {
        long startTimeMs = GeneralUtils.getMillisSeconds()
        Map results = ["success": true]
        results["taskStartDate"] = GeneralUtils.getCurrentTimeString()
        try{
            if(taskClass == null)
                throw new Exception("Missing the task class")
            
            Class.forName(taskClass);
            ClassLoader classLoader = GenericTaskWrapper.class.getClassLoader();
            Class aClass = classLoader.loadClass(taskClass);
            GenericTaskAbstract currentTask = aClass.newInstance()
            logger.debug("New Instance of class type:[" + taskClass + "] Task info: " + this.taskContext)
            
            try{
                currentTask.beforeExecute(this.taskContext, this.messagingContext)
                currentTask.execute(this.taskContext, results, messagingService)
            }catch(Exception e){
                logger.error("GenericTaskI Execution Method")
                throw e
            }
        }catch(Exception e){
            logger.error("Task Error", StackTraceUtils.sanitize(e))
            results["success"] = false
            results["exceptionMessage"] = e.getMessage()
            results["exceptionStacktrace"] = GeneralUtils.toStringList(StackTraceUtils.deepSanitize(e))
        }finally{
            long endTimeMs = GeneralUtils.getMillisSeconds()
            results["taskTookMs"] = endTimeMs - startTimeMs
            results["taskTookMinutes"] = Double.parseDouble(GeneralUtils.formatNumber((endTimeMs - startTimeMs)/1000/60))
            results["taskEndedDate"] = GeneralUtils.getCurrentTimeString()
        }
        
        try{
            doneClosure(this.taskContext, results, Thread.currentThread().getName())
        }catch(Exception e){
            logger.error("Error with the Done closure", StackTraceUtils.sanitize(e))
            throw e
        }
    }
}
