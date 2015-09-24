package com.earasoft.framework.messaging

import com.google.inject.AbstractModule

class MessagingModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(MessagingService.class).to(HazelcastMessagingService.class);
    }
    
}
