package com.earasoft.framework.template.web

import java.util.concurrent.Future
import org.apache.commons.lang.BooleanUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.MetaClassesBase
import com.earasoft.framework.messaging.HazelcastMessagingService
import com.earasoft.framework.messaging.MessagingService
import com.earasoft.framework.owner.TaskOwnerBase
import com.ning.http.client.AsyncHttpClient

import com.ning.http.client.*;
import groovy.json.JsonSlurper
import java.util.concurrent.Future;


class WebTemplateOwner extends TaskOwnerBase {
	final JsonSlurper slurper = new JsonSlurper()
	final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	
	static{
		MetaClassesBase.load()
	}
	
    private static final Logger logger = LoggerFactory.getLogger(WebTemplateOwner.class)
    
    public WebTemplateOwner(MessagingService messagingService){
        super(messagingService)
    }
    
    @Override
    public void startWork(Map settings) {
		
		String elasticSearchHost = "127.0.0.1"
		String elasticSearchPort = 9200
		
		String url = "http://127.0.0.1:3000/data/"
		Future<Response> f = asyncHttpClient.prepareGet(url).setHeader("Accept", "application/json").execute();
		Response res = f.get();
		
		List<String> directories = slurper.parseText(res.getResponseBody('utf-8'))
		
		directories.each{ String currentDirectory -> 
			Map currentTaskContext = ['baseUrl': url, 
										'directoryName':  currentDirectory,
										'directoryUrl': url+currentDirectory+"/",
										'elasticSearchHost': elasticSearchHost,
										'elasticSearchPort': elasticSearchPort]
			
			println currentTaskContext
			putTaskIntoQueue(currentTaskContext, WebTemplateTask.class.getCanonicalName(), true)
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
    
	File output = new File("output.txt")
    @Override
    public void handleMiscEvents(MessageBuilder messageResults) {
		
		try{
			output << messageResults.getResults()['output'] + '\n'
		}catch(Exception e){
			println e
		}
		
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void shutdownChild() {
        // TODO Auto-generated method stub
        
    }
    
    static main(args) {
        MessagingService messagingService = new HazelcastMessagingService()  //http://127.0.0.1:8189/queue.json To View Tasks Status
        WebTemplateOwner shellTaskOwnerBase = new WebTemplateOwner(messagingService)
        shellTaskOwnerBase.startServiceSystem()
        shellTaskOwnerBase.startDaemonWorker() //This is Optional
        
        shellTaskOwnerBase.startWork(null)
        
        println "********************** SHUTDOWN ********************************"
        //
        //        Injector injector = Guice.createInjector(new MessagingModule(), new OwnerModule() );
        //        TaskOwnerService taskOwner = injector.getInstance(ShellTaskOwnerBase.class)
        //        taskOwner.startServiceSystem()
        //        taskOwner.startDaemonWorker()
        
        
        //shellTaskOwnerBase.startWork()
        
        
        
        //taskOwner.shutdown()
    }
    
    
}
