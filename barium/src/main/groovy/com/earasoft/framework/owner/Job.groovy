package com.earasoft.framework.owner;

import com.earasoft.framework.worker.GenericTaskAbstract;

public class Job {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Job job = new Job()
		job.addTask([:], null)
	}
	
	public void addTask(TaskContext taskConext, String taskClass){
		println taskConext
		println taskClass
	}

}
