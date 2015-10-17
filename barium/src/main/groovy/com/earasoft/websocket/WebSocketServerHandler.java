/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.earasoft.websocket;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.earasoft.websocket.RouterHits;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    public static final String WEBSOCKET_PATH = "/websocket";
    private WebSocketServerHandshaker handshaker;
    
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
    
    }
    
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        
//        // Send the demo page and favicon.ico
//        if ("/".equals(req.getUri()) && req.getMethod() == GET) {
//            ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
//            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
//
//            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
//            HttpHeaders.setContentLength(res, content.readableBytes());
//
//            sendHttpResponse(ctx, req, res);
//            return;
//        }
//        
//        if ("/favicon.ico".equals(req.getUri())) {
//            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
//            sendHttpResponse(ctx, req, res);
//            return;
//        }
        

     
        if ("/websocket".equals(req.getUri())) {
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            	channels.add(ctx.channel());
            }
            return;
        }
        
        
        RouterHits.execute(ctx, req);
        
        //sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
        return;

    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        
        
        // Send the uppercase string back.
        String request = ((TextWebSocketFrame) frame).text();
        System.err.printf("%s received %s%n", ctx.channel(), request);
        
        
    
        broadcast(ctx,request.toUpperCase());
    
       // ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
    }
    
    /**
     * Broadcast the specified message to all registered channels except the sender.
     * 
     * @param context Message context
     * @param message Message to broadcast
     */
    private void broadcast(ChannelHandlerContext context, String message) {
        System.out.println(">>"+channels);
        for (Channel channel: channels) {
        	System.out.println(">>>"+channel);
            //if (channel != context.channel()) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            //}
        
        	
        }
    }
    
    public static void broadcast(String message) {
     
        for (Channel channel: channels) {
        	System.out.println(">>>"+channel);
            //if (channel != context.channel()) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            //}
        
        	
        }
    }
    
    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
        if (WebSocketServer.SSL) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
}
