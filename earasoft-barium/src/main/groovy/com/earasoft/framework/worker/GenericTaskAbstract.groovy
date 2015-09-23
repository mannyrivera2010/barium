package com.earasoft.framework.worker

import com.earasoft.framework.messaging.MessagingService;


/**
 * This Abstract Class is used for the tasks used by GenericTaskWrapper to make instances
 * Let exception raise up
 * 
 * @author riverema
 */

public abstract class GenericTaskAbstract{
    
    /**
     * This method can used for initiation 
     */
    public void beforeExecute(final Map<Object, Object> taskContext, Map<Object, Object> messagingContext = null){
        
    }
    
    /**
     * Method used to execute task
     * @param taskContext
     * @param results
     * @param slaveEvents
     */
    public abstract void execute(final Map<Object, Object> taskContext, final Map<Object, Object> results, final MessagingService messagingService);
    

}
