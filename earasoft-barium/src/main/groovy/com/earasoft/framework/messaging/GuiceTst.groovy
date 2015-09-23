package com.earasoft.framework.messaging

import com.google.inject.Guice
import com.google.inject.Injector

class GuiceTst {
    
    static main(args){
        Injector injector = Guice.createInjector(new MessagingModule());
        MessagingService messageService = injector.getInstance(MessagingService.class)
        messageService.start()
    }
}
