package com.earasoft.framework.template.web

import groovy.json.JsonSlurper

import java.util.Map;
import java.util.concurrent.Future

import org.json.XML
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.TaskMetadata
import com.earasoft.framework.messaging.MessagingService
import com.earasoft.framework.worker.GenericTaskAbstract
import com.ning.http.client.*

@TaskMetadata(version =  1)
public class WebTemplateTask extends GenericTaskAbstract{
    private static final Logger logger = LoggerFactory.getLogger(WebTemplateTask.class)
	final JsonSlurper slurper = new JsonSlurper()
	
	private Map messagingContext
	
	/**
	 * Send Message to the owner
	 * @param messagingService
	 * @param eventMessage
	 */
	private void sendMessageToOwner(final MessagingService messagingService, final Map eventMessage){
		if(messagingService != null){
			MessageBuilder message = new MessageBuilder().build().setOwner(this.messagingContext['currentOwner']).setNodeId(this.messagingContext['fullNodeID'])
					.setEventType('notification')
					.setResults(eventMessage)
			
			messagingService.publishWorkerEvents(message)
		}else{
			loggerMisc.info eventMessage.toJsonString()
		}
		
	}
	
	@Override
	public void beforeExecute(final Map<Object, Object> taskContext, Map<Object, Object> messagingContext){
		this.messagingContext = messagingContext
		//conf.setProperty('storage.batch-loading', 'true')
	}
	

	/*
	 * ['baseUrl': url, 
		'directoryName':  currentDirectory,
		'directoryUrl': url+currentDirectory+"/",
		'elasticSearchHost': elasticSearchHost,
		'elasticSearchPort': elasticSearchPort]
	 */
    @Override
    public void execute(Map<Object, Object> taskContext, Map<Object, Object> results, MessagingService messagingService) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		
            String directoryUrl = taskContext['directoryUrl']
			String elasticSearchHost = taskContext['elasticSearchHost']
			String elasticSearchPort = taskContext['elasticSearchPort']
			


			logger.info("-+-+-+-+-+-+-+-" + taskContext.toString())
			//println directoryUrl
			
			
			
			
			try{
				Future<Response> f = asyncHttpClient.prepareGet(directoryUrl).setHeader("Accept", "application/json").execute();
				Response res = f.get();
				
				//logger.info( "----+-----+---+" + res.getResponseBody('utf-8'))
				
				List<String> files = slurper.parseText(res.getResponseBody('utf-8'))
				
				files.each{ String file ->
					if(file.contains(".nxml")){
						
						
						//println directoryUrl+""+file
						
						Future<Response> fa = asyncHttpClient.prepareGet(directoryUrl+""+file).execute();
						Response resa = fa.get();
						
						//println resa.getResponseBody('utf-8')
						
						String xmlToJsonString = XML.toJSONObject(resa.getResponseBody('utf-8')).toString()
						
						
						
						Map inputObj =  ["output":xmlToJsonString]
//						
//						println inputObj
//						
						
						sendMessageToOwner(messagingService, inputObj)
						
						
//						Future<Response> fes= asyncHttpClient.preparePost("http://"+elasticSearchHost+':'+elasticSearchPort+"/"+"test1/type1").setBody(xmlToJsonString).execute();
//						Response fres = fes.get();
//						println fres.getResponseBody('utf-8')
						
						
						
					}
				}
		
				
			}catch(Exception e){
				logger.error("Error", e)
				throw e
			}
			
			
//			List<String> directories = slurper.parseText(res.getResponseBody('utf-8'))
//			
//			println directories
//			
			asyncHttpClient.close()
            
        
    }
 
	
	
	static main(args) {
		
		// Given an XML string
		def xml = '''<root>
            |    <node>Tim</node>
            |    <node>Tom</node>
            |    <node>
            |      <anotherNode>another</anotherNode>
            |    </node>
            |</root>'''.stripMargin()
		
			//println xml
			println XML.toJSONObject(xml)
			//xmlToJson(xml)
	}


    
}
