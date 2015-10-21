package com.earasoft.framework.http;

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.group.ChannelGroup
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

import com.earasoft.framework.common.StaticUtils

public class WebSocketMessageHander extends WebsocketMessageHandlerI {

	public WebSocketMessageHander(ChannelHandlerContext ctx,
	ChannelGroup channels) {
		super(ctx, channels);
	}


	
	@Override
	public void handleMessage(String message) {
		Map response = [:]
		try{
			Map incomingObj = message.jsonToObject();
			
			StaticUtils.checkMapRequiredKeys(incomingObj, ['_eventType'])
			
			String eventType = incomingObj.get('_eventType')

			response.put('_eventType',eventType)
			//System.err.printf("%s received %s%n", ctx.channel(),  message);
			if(eventType.equals('transform')){
				handleTransform(incomingObj, response)
				return;
				
			}else{
				throw new Exception("Unknown event type: " + eventType)
			}
			throw new Exception("Reached Bottom")
		}catch(Exception e){
			response['error'] = true
			response['exception']= e.toString()
			System.err.println(e)
		}finally{
			handleResponse(response)
		}
	}

	public void handleTransform(Map request, Map response){
		StaticUtils.checkMapRequiredKeys(request, ['message'])
		response["message"] =  request["message"].toUpperCase();
	}
	
	public void handleResponse(Map message){
		broadcastToAll(message);
	}
}
