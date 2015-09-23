package com.earasoft.framework.template

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.earasoft.framework.common.TaskMetadata
import com.earasoft.framework.messaging.MessagingService;
import com.earasoft.framework.worker.GenericTaskAbstract

@TaskMetadata(version =  1)
public class MathTemplateTask extends GenericTaskAbstract{
    private static final Logger logger = LoggerFactory.getLogger(MathTemplateTask.class)
    

    @Override
    public void execute(Map<Object, Object> taskContext, Map<Object, Object> results, MessagingService messagingService) {
            
            println "taskContext:" + taskContext
            results['sum'] = taskContext['num1'] + taskContext['num2']
            
        
    }



    
}
