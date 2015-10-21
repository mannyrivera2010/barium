package com.earasoft.framework.http;

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.group.ChannelGroup
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

import com.earasoft.framework.common.MetaClassesBase

public abstract class WebsocketMessageHandlerI {

	static{
		MetaClassesBase.load();
	}
	
	protected ChannelHandlerContext ctx;
	protected static ChannelGroup channels; 
	
	public WebsocketMessageHandlerI(ChannelHandlerContext ctx, ChannelGroup channels ) {
		this.ctx = ctx;
		this.channels = channels;
	}

	abstract void handleMessage(String message);
	
    /**
     * Broadcast the specified message to all registered channels except the sender.
     * 
     * @param context Message context
     * @param message Message to broadcast
     */
    public void broadcastToAll(Map message) {
        System.out.println(">>"+channels);
        for (Channel channel: channels) {
        	System.out.println(">>>"+channel);
            //if (channel != context.channel()) {
                channel.writeAndFlush(new TextWebSocketFrame(message.toJsonString()));
            //}
        
        	
        }
    }
    
    public void broadcast(Map message) {
        for (Channel channel: channels) {
        	System.out.println(">>>"+channel);
            //if (channel != context.channel()) {
                channel.writeAndFlush(new TextWebSocketFrame(message.toJsonString()));
            //}
        
        	
        }
    }
    
}
