package com.earasoft.framework.template

import org.apache.commons.lang.BooleanUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.MetaClassesBase
import com.earasoft.framework.messaging.HazelcastMessagingService
import com.earasoft.framework.messaging.MessagingService
import com.earasoft.framework.owner.TaskOwnerBase

class MathTemplateOwner extends TaskOwnerBase {
	
	static{
		MetaClassesBase.load()
	}
	
    private static final Logger logger = LoggerFactory.getLogger(MathTemplateOwner.class)
    
    public MathTemplateOwner(MessagingService messagingService){
        super(messagingService)
    }
    
    @Override
    public void startWork(Map settings) {
        Random r = new Random();
        
        100.times{ //Loop
            //MathTaskContext currentTaskConext = new MathTaskContext().setNum1(r.nextInt(101)).setNum2(r.nextInt(101))
            
            Map currentTaskConext = ['num1': r.nextInt(101) , 'num2':r.nextInt(101)]
                
            println currentTaskConext
            
            putTaskIntoQueue(currentTaskConext, MathTemplateTask.class.getCanonicalName(), false)
        }

        println "Waiting Till Tasks Finished"
        waitTillTasksFinished()
        println "DONE"
    }
    
    @Override
    public void handleFinishedTaskEvent(MessageBuilder messageResults) {
        boolean success = BooleanUtils.toBoolean(messageResults.getResults().get('success'))
        if(success){
            logger.info('Slave Task Results Success: ' + messageResults)
        }else{
            println "*****************" + messageResults.getTaskContext()['_failureCount']
            
            if(messageResults.getTaskContext()['_failureCount'] < 3){
                logger.warn('Slave Task Results Failed: ' + messageResults + ' - Rescheduling Task')
                this.putTaskIntoQueue(messageResults.getTaskContext(), messageResults.getTaskClass()) //Reschedule
            }else{
                logger.warn('Slave Task Results Failed: ' + messageResults + ' - Task Went over the failure limit *****')
            }
        }
    }
    
    @Override
    public void handleTakingTaskEvent(MessageBuilder messageResults) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void handleMiscEvents(MessageBuilder messageResults) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void shutdownChild() {
        // TODO Auto-generated method stub
        
    }
    
    static main(args) {
        MessagingService messagingService = new HazelcastMessagingService()  //http://127.0.0.1:8189/queue.json To View Tasks Status
        MathTemplateOwner shellTaskOwnerBase = new MathTemplateOwner(messagingService)
        shellTaskOwnerBase.startServiceSystem()
        shellTaskOwnerBase.startDaemonWorker() //This is Optional
        
        //shellTaskOwnerBase.startWork(null)
        
        println "********************** SHUTDOWN ********************************"
        
        
        
        
        //
        //        Injector injector = Guice.createInjector(new MessagingModule(), new OwnerModule() );
        //        TaskOwnerService taskOwner = injector.getInstance(ShellTaskOwnerBase.class)
        //        taskOwner.startServiceSystem()
        //        taskOwner.startDaemonWorker()
        
        
        //taskOwner.startWork()
        
        
        
        //taskOwner.shutdown()
    }
    
    
}
