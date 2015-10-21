package com.earasoft.framework.owner;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import static io.netty.handler.codec.http.HttpResponseStatus.OK
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaders
import io.netty.util.CharsetUtil

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.earasoft.framework.common.HazelUtils
import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.StaticUtils
import com.earasoft.framework.http.RouteHit
import com.earasoft.framework.http.RouterHits
import com.earasoft.framework.http.WebSocketServer
import com.earasoft.framework.http.WebSocketServerIndexPage
import com.earasoft.framework.http.WebsocketUtils
import com.earasoft.framework.messaging.HazelcastMessagingService
import com.earasoft.framework.messaging.MessagingService
import com.earasoft.framework.worker.GenericHazelcastWorker
import com.google.inject.Inject
import com.hazelcast.core.MemberAttributeEvent
import com.hazelcast.core.MembershipEvent
import com.hazelcast.core.MembershipListener
import com.hazelcast.core.Message
import com.hazelcast.core.MessageListener

public abstract class TaskOwnerBase implements TaskOwnerService {
	private static final Logger loggerBase = LoggerFactory.getLogger(TaskOwnerBase.class)

	protected MessagingService messagingService

	@Inject
	public TaskOwnerBase(MessagingService messagingService){
		this.messagingService = messagingService
		this.loaderSlave = new GenericHazelcastWorker(new HazelcastMessagingService())
	}

	/*
	 * Worker Variables
	 */
	protected final GenericHazelcastWorker loaderSlave
	protected final AtomicBoolean loaderWorkerStarted = new AtomicBoolean(false)

	/*
	 * Task Variables
	 */
	protected Long taskId = 1 //Used to id task
	protected List<Map<Object, Object>> endPoints = new LinkedList<Map<Object, Object>>()
	protected Map<String, Object> webStore = new TreeMap()
	protected List<Map> tasksStatus = []
	protected Set<String> currentRunningTaskSet = new TreeSet()
	protected List taskHistory = []
	protected long startTime = System.currentTimeMillis()


	/**
	 * Preq Check for Port
	 */
	protected void preqCheck(int sparkPort = 8189){
		if(!StaticUtils.checkAvailablePort(sparkPort)){
			loggerBase.error("Port is not Available shutting down. Might mean another master already exist in is machine")
			loggerBase.error("Failed to start master")
			return
		}
	}

	/**
	 * Register EndPoints
	 */
	protected void registerEndPoint(String url, String desc, Closure closure){
		endPoints.add(['url':url, 'desc': desc])

		RouterHits.get(url, new RouteHit(){
					@Override
					public FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {
						String data = null
						try{
							data = closure(request).toJsonString()
						}catch(Exception e){
							data = ['success': false, "message": e.getMessage()].toJsonString()
							loggerBase.error("Http Error", e)
						}


						ByteBuf content = Unpooled.copiedBuffer(data, CharsetUtil.UTF_8);
						FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

						response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
						HttpHeaders.setContentLength(response, content.readableBytes());
						return response;
					}
				});


		//		Spark.get(url, "application/json", new Route() {
		//					@Override
		//					public Object handle(Request request, Response response) {
		//						response.raw().setContentType("application/json");
		//
		//						String data = null
		//						try{
		//							data = closure(request, response).toJsonString()
		//						}catch(Exception e){
		//							data = ['success': false, "message": e.getMessage()].toJsonString()
		//						}
		//						return data
		//					}
		//				});
	}

	protected void startWebServer(int sparkPort = 8189){

		RouterHits.get("/", new RouteHit(){
					@Override
					public FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {
						ByteBuf content = WebSocketServerIndexPage.getContent(RouterHits.getWebSocketLocation(request));
						FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

						response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
						HttpHeaders.setContentLength(response, content.readableBytes());
						return response;
					}
				});


		RouterHits.get("/test", new RouteHit(){
					@Override
					public FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {
						ByteBuf content = Unpooled.copiedBuffer(['test':'hello'].toJsonPrettyString(), CharsetUtil.UTF_8);
						FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

						response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
						HttpHeaders.setContentLength(response, content.readableBytes());
						return response;
					}
				});

			
			def instance = this
			Thread slave = new Thread()
			
			
		RouterHits.get("/startWork", new RouteHit(){
					@Override
					public FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {

						Map output = [:]
						if(slave.isAlive()){
							output = ['message':'job already working']
						}else{
						
						slave =  new Thread(new Runnable(){
							
													public void run(){
							
														instance.startWork(null)
													}
												})
										slave.setName("Work")
										slave.setDaemon(true)
										
							slave.start() 
							output = ['message':'Started work']
						}
			


						ByteBuf content = Unpooled.copiedBuffer(output.toJsonPrettyString(), CharsetUtil.UTF_8);
						FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

						response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
						HttpHeaders.setContentLength(response, content.readableBytes());
						return response;
					}
				});


		registerEndPoint('/endpoints.json','index',
				{
					FullHttpRequest request//, Response response ->
					//response.header("Access-Control-Allow-Origin", "*")
					return endPoints
				});

		registerEndPoint('/webstore.json','Location of webstore',
				{
					FullHttpRequest request//, Response response ->
					//response.header("Access-Control-Allow-Origin", "*")
					webStore
				});

		registerEndPoint('/taskStore.json','Location of task store',
				{
					FullHttpRequest request//, Response response ->
					//response.header("Access-Control-Allow-Origin", "*")
					taskHistory
				});


		registerEndPoint('/status.json','Status of Hazelcast',
				{
					FullHttpRequest request//, Response response ->
					//response.header("Access-Control-Allow-Origin", "*")
					["status": 'running',
						"uptimeMillis": System.currentTimeMillis() - startTime,
						"version": 1.0,
						'members': HazelUtils.getClusterMemberInfo(messagingService.getCluster())
					]
				});

		registerEndPoint('/queue.json','queue info',
				{
					FullHttpRequest request//, Response response ->
					//response.header("Access-Control-Allow-Origin", "*")
					['queueSize': this.messagingService.getTasksQueue().size() ,
						'tasksStatus': tasksStatus,
						'currentRunningTaskSet': currentRunningTaskSet]
				});


		loggerBase.info("Webservice port:" + sparkPort)

		println "NON--" + this.messagingService
	}

	private boolean taskPutIntoQueue = false
	/**
	 * Helper method to put task into queue
	 */
	protected void putTaskIntoQueue(Map taskContext, String classStringForTask, boolean delay = true){
		if(taskContext['_id'] == null){
			taskContext['_id'] = taskId
			taskId ++
		}

		tasksStatus << ['taskId': taskContext['_id'], 'status': 'Scheduled', 'timeInMillis': System.currentTimeMillis(), 'classTask': classStringForTask]

		MessageBuilder currentMessage = MessageBuilder.build().setTaskContext(taskContext)
				.setTaskClass(classStringForTask)
				.setOwner(messagingService.getFullNodeID())
				.setEventType("TaskGeneration")
				.setDelay(delay)

		messagingService.addTaskToQueue(currentMessage)
		taskPutIntoQueue = true
	}

	/**
	 * Hooks
	 */
	protected void membershipMonitoring(){
		messagingService.addClusterMembershipListener(new MembershipListener() {
					public void memberAdded( MembershipEvent membershipEvent ) {
						System.out.println( "MemberAdded " + membershipEvent )
					}

					public void memberRemoved( MembershipEvent membershipEvent ) {
						System.out.println( "MemberRemoved " + membershipEvent )
					}

					void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent){

					}
				}
				)

		messagingService.addWorkerEventsToOwnerMessageListener(new MessageListener<MessageBuilder>(){
					@Override
					public void onMessage(Message<MessageBuilder> message) {
						workerEventsMessageHandler(message)
					}
				})

	}

	private void workerEventsMessageHandler(Message<MessageBuilder> message){
		MessageBuilder messageResults = message.getMessageObject()
		println "-------------------INCOMING----------------"  + messageResults

		

		if(messageResults.isSameOwner(messagingService.getFullNodeID())){
			loggerBase.info("HISTORY: " + messageResults.toMap().toJsonString())
			//ESClient.logEvent(messageResults.toMap(), 'testing')

			if(messageResults.getEventType().equals("TakingTask")){
				taskHistory.add(messageResults)
				Map temp = ['taskId': messageResults.getTaskContext()['_id'], 'status': 'In Progress', 'timeInMillis': StaticUtils.getCurrentTimeString(System.currentTimeMillis()), 'classTask': messageResults.getTaskClass()]
				tasksStatus << temp
				WebsocketUtils.broadcastToAll(temp)
				currentRunningTaskSet.add(messageResults.getNodeId() + "-" + messageResults.getTaskContext()['_id'])

				try{
					handleTakingTaskEvent(messageResults)
				}catch(Exception E){
					loggerBase.error("ERROR WITH HANDLING MESSAGE")
				}


			}else if(messageResults.getEventType().equals("FinishedTask")){
				taskHistory.add(messageResults)
				currentRunningTaskSet.remove(messageResults.getNodeId() + "-" + messageResults.getTaskContext()['_id'])

				if(messageResults.hasResults()){
					tasksStatus << ['taskId': messageResults.getTaskContext()['_id'], 'status': 'Finished', 'timeInMillis': System.currentTimeMillis(), 'classTask': messageResults.getTaskClass()]

					try{
						handleFinishedTaskEvent(messageResults)
					}catch(Exception E){
						loggerBase.error("ERROR WITH HANDLING MESSAGE")
					}
				}else{
					loggerBase.warn("MESSAGE SHOULD HAVE Results: " + messageResults)
					tasksStatus << ['taskId': messageResults.getTaskContext()['_id'], 'status': 'Error', 'timeInMillis': System.currentTimeMillis(), 'classTask': messageResults.getTaskClass()]
				}
			}else{
				try{
					handleMiscEvents(messageResults)
				}catch(Exception E){
					loggerBase.error("ERROR WITH HANDLING MESSAGE")
				}
			}
		}else{
			loggerBase.warn("Ignored Message because not for this owner - " + messageResults)
		}
	}

	@Override
	abstract public void startWork(Map settings)

	/* (non-Javadoc)
	 * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#handleFinishedTaskEvent(com.comsort.personify.loading.framework.MessageBuilder)
	 */
	@Override
	abstract public void handleFinishedTaskEvent(MessageBuilder messageResults)

	/* (non-Javadoc)
	 * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#handleTakingTaskEvent(com.comsort.personify.loading.framework.MessageBuilder)
	 */
	@Override
	abstract public void handleTakingTaskEvent(MessageBuilder messageResults)

	/* (non-Javadoc)
	 * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#handleMiscEvents(com.comsort.personify.loading.framework.MessageBuilder)
	 */
	@Override
	abstract public void handleMiscEvents(MessageBuilder messageResults)
	/**
	 * Method used to start up server
	 */
	protected void startMasterServer(){
		messagingService.start(true)
		membershipMonitoring()
		loggerBase.info("HazelcastInstance Socket Address:" + messagingService.getLocalMember().getSocketAddress())

		//messagingService.publishOwnerEvents() //"Master Finished Initialization"  //TODO

	}

	/* (non-Javadoc)
	 * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#shutdown()
	 */
	@Override
	public void shutdown(){

		//messagingService.publishOwnerEvents() //"Master Shutdown"  //TODO

		if(loaderWorkerStarted.get()){
			loaderSlave.stop()
		}
		//Spark.stop()

		messagingService.stop()
		shutdownChild()
	}

	/* (non-Javadoc)
	 * @see com.comsort.personify.loading.framework.TaskOwnerBaseI#shutdownChild()
	 */
	@Override
	abstract public void shutdownChild()

	/**
	 * Starting Hazelcast and Webserver
	 */
	public void startServiceSystem(int sparkPort = 8189, boolean startWorkerDaemon = false){
		preqCheck(sparkPort)
		startWebServer(sparkPort)
		startMasterServer()


		//
		Thread slave = new Thread(new Runnable(){
					public void run(){

						WebSocketServer webSocketServer = new WebSocketServer()
						webSocketServer.start()
					}
				})
		slave.setName("Internal Worker")
		slave.setDaemon(false)
		slave.start() //Start a worker on this machine also
		//

		if(startWorkerDaemon == true){
			startDaemonWorker()
		}
	}

	/**
	 * Method used to start a Worker in the same JVM
	 */
	public void startDaemonWorker(){
		final CountDownLatch waitTillHazelcastInit = new CountDownLatch(1)
		if(!loaderWorkerStarted.get()){
			Thread slave = new Thread(new Runnable(){
						public void run(){
							loaderWorkerStarted.set(true)
							loaderSlave.start(waitTillHazelcastInit)
						}
					})
			slave.setName("Internal Worker")
			slave.setDaemon(true)
			slave.start() //Start a worker on this machine also
			waitTillHazelcastInit.await() //Wait till worker finished initiation of Hazelcast connection
		}
	}


	protected waitTillTasksFinished() {
		while(!messagingService.getTasksQueue().isEmpty() || !currentRunningTaskSet.isEmpty() || taskPutIntoQueue == true){ // Wait till all task are finished
			Thread.sleep(500)
			taskPutIntoQueue = false
		}
	}

}
