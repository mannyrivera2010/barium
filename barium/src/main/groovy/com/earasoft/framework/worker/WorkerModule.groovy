package com.earasoft.framework.worker

import com.google.inject.AbstractModule

class WorkerModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(WorkerService.class).to(GenericHazelcastWorker.class);
    }
    
}
