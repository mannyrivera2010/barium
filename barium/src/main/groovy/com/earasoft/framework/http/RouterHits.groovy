package com.earasoft.framework.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import static io.netty.handler.codec.http.HttpResponseStatus.OK
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.CharsetUtil

import com.earasoft.framework.common.MetaClassesBase

public class RouterHits {
	private static Map<HttpMethod, Map<String, RouteHit>> rounterMap = new HashMap<HttpMethod, Map<String, RouteHit>>();


	static{
		MetaClassesBase.load()
	}
	/**
	 * Map the route for HTTP GET requests
	 *
	 * @param path        the path
	 * @param acceptType  the accept type
	 * @param route       The route
	 * @param transformer the response transformer
	 */
	public static synchronized void get(String path, RouteHit route) {
		//addRoute(HttpMethod.get.name(), ResponseTransformerRouteImpl.create(path, acceptType, route, transformer));
		if(rounterMap.get(HttpMethod.GET)){
			if(rounterMap.get(HttpMethod.GET).get(path)){
				println "Path $path for GET already exist"
			}else{
				rounterMap.get(HttpMethod.GET).put(path, route)
			}
		}else{
			println 'test'
			Map temp = [:]
			temp[path] = route
			rounterMap.put(HttpMethod.GET, temp);
		}
	}

	public static boolean checkIfMappingExit(FullHttpRequest request){
		if(rounterMap.get(HttpMethod.GET)){
			if(rounterMap.get(HttpMethod.GET).get(request.getUri())){
				return true
			}
		}
		return false
	}
	
	public static void execute(ChannelHandlerContext ctx, FullHttpRequest request){
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.FORBIDDEN);

		if(rounterMap.get(HttpMethod.GET)){
			if(rounterMap.get(HttpMethod.GET).get(request.getUri())){

				RouteHit current = rounterMap.get(HttpMethod.GET).get(request.getUri())
				response = current.handle(ctx, request)
			}else{
				response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
			}
		}else{
			response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
			println "not found...."
		}

		//println "FullHttpRequest + " + response
		sendHttpResponse(ctx, request, response);
	}

	public static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	public static String getWebSocketLocation(FullHttpRequest req) {
		String location =  req.headers().get(HOST) + WebSocketServerHandler.WEBSOCKET_PATH;
		if (WebSocketServer.SSL) {
			return "wss://" + location;
		} else {
			return "ws://" + location;
		}
	}

	static main(args) {

	}
}
