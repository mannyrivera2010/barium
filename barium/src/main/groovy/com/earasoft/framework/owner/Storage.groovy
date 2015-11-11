package com.earasoft.framework.owner;

import com.earasoft.framework.common.MessageBuilder
import com.earasoft.framework.common.StaticUtils
import com.earasoft.framework.http.WebsocketUtils

public class Storage {

	/*
	 * Task Variables
	 */
	private Long taskId = 1 //Used to id task
	
	private long scheduledTaskCounter = 0
	private long completedTaskCounter = 0
	private long inProgressTaskCounter = 0
	
	private List<Map<String, String>> endPoints = new LinkedList<Map<String, String>>()
	private Map<String, Object> webStore = new TreeMap()
	private List<Map> tasksStatus = []
	private Set<String> currentRunningTaskSet = new TreeSet()
	private List taskHistory = []
	private long startTime = System.currentTimeMillis()
	
	public long getScheduledTaskCounter() {
		return scheduledTaskCounter;
	}

	public long getCompletedTaskCounter() {
		return completedTaskCounter;
	}

	public List<Map<String, String>> getEndPoints() {
		return endPoints;
	}

	public Map<String, Object> getWebStore() {
		return webStore;
	}

	public List<Map> getTasksStatus() {
		return tasksStatus;
	}

	public Set<String> getCurrentRunningTaskSet() {
		return currentRunningTaskSet;
	}

	public List getTaskHistory() {
		return taskHistory;
	}

	public long getStartTime() {
		return startTime;
	}

	
	public void addEndPointUrl(String url, String desc){
		this.endPoints.add(['url':url, 'desc': desc])
	}
	
	public List<Map<String, String>> endPoints(){
		return this.endPoints
	}
	
	/**
	 * Method used to get task id
	 * @return
	 */
	public Long getTaskId(){
		return this.taskId
	}
	
	/**
	 * Method used to increment task id
	 */
	public void incrementTaskId(){
		this.taskId ++
	}
	
	/**
	 * Method used to task taking info
	 * @param messageResults
	 */
	public void takingTask(MessageBuilder messageResults){
		inProgressTaskCounter ++
		taskHistory.add(messageResults)
		Map temp = ['_eventType': 'taskStatus',
			'taskId': messageResults.getTaskContext()['_id'],
			'status': 'In Progress',
			'dateTime': StaticUtils.getCurrentTimeString(System.currentTimeMillis()),
			'classTask': messageResults.getTaskClass()]
		
		tasksStatus << temp
		WebsocketUtils.broadcastToAll(temp)
		currentRunningTaskSet.add(messageResults.getNodeId() + "-" + messageResults.getTaskContext()['_id'])
		WebsocketUtils.broadcastToAll(getProgress())		
	}
	
	public Map getProgress(){
		Integer  percentInt = 0
		Double percentDbl = 0
		
		if(scheduledTaskCounter != 0 ){
			percentInt = ((completedTaskCounter/scheduledTaskCounter) *100) as Integer
			percentDbl = ((completedTaskCounter/scheduledTaskCounter) *100)
		}else{
		
		}
		
		
		
		
		
		Map counters = ['_eventType': 'progress',
			'completedTaskCounter': completedTaskCounter,
			'scheduledTaskCounter': scheduledTaskCounter,
			'percentDbl': percentDbl,
			'percentInt': percentInt,
			'inProgressTaskCounter': inProgressTaskCounter,
			'updateString':"${percentInt}% Complete ($completedTaskCounter out of $scheduledTaskCounter scheduled completed)"]
		return counters
	}
	/**
	 * Method used to finished task info
	 * @param messageResults
	 */
	public void finishedTask(MessageBuilder messageResults){
		completedTaskCounter ++
		inProgressTaskCounter --
		taskHistory.add(messageResults)
		currentRunningTaskSet.remove(messageResults.getNodeId() + "-" + messageResults.getTaskContext()['_id'])
		
		Map temp = ['_eventType': 'taskStatus',
			'taskId': messageResults.getTaskContext()['_id'],
			'status': 'Finished',
			'dateTime': StaticUtils.getCurrentTimeString(System.currentTimeMillis()),
			'classTask': messageResults.getTaskClass()]
		
		tasksStatus << temp
		WebsocketUtils.broadcastToAll(temp)
		
		WebsocketUtils.broadcastToAll(getProgress())
	}
	
	/**
	 * Method used to schedule task info
	 * @param messageResults
	 */
	public void scheduleTask(MessageBuilder messageResults){
		scheduledTaskCounter ++
		
		Map temp = ['_eventType': 'taskStatus',
			'taskId': messageResults.getTaskContext()['_id'],
			'status': 'Scheduled',
			'dateTime': StaticUtils.getCurrentTimeString(System.currentTimeMillis()),
			'classTask': messageResults.getTaskClass()]
		
		
		tasksStatus << temp
		WebsocketUtils.broadcastToAll(temp)
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	}

}
