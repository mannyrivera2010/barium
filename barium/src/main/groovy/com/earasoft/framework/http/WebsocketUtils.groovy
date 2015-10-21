package com.earasoft.framework.http

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

import com.earasoft.framework.common.MetaClassesBase

class WebsocketUtils {

	static{
		MetaClassesBase.load()
	}
	/**
	 * Broadcast the specified message to all registered channels except the sender.
	 *
	 * @param context Message context
	 * @param message Message to broadcast
	 */
	public static void broadcastToAll(Map message) {
		System.out.println(">>"+WebSocketServerHandler.channels);
		for (Channel channel: WebSocketServerHandler.channels) {
			System.out.println(">>>"+channel);
			//if (channel != context.channel()) {
				channel.writeAndFlush(new TextWebSocketFrame(message.toJsonString()));
			//}
		
			
		}
	}
}
