package com.earasoft.framework.owner

import com.google.inject.AbstractModule

class OwnerModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(TaskOwnerService.class).to(TaskOwnerBase.class);
    }
    
}
