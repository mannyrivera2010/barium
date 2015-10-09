package com.earasoft.framework.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CmdProcessBuilder {
	
	
	  public static void main(String args[])
	     throws InterruptedException,IOException{
		  
		  ex1();
		  //ex2();
		  
	  }
	  
	  public static void ex2() throws IOException{
		  
		  
		  
		  String[] command = {"cmd", "/C", "dir"};
	        ProcessBuilder probuilder = new ProcessBuilder( command );
	        //You can set up your work directory
	        //probuilder.directory(new File("c:\\"));
	        
	        Process process = probuilder.start();
	        
	        
	        //Read out dir output
	        InputStream is = process.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        String line;
	        System.out.printf("Output of running %s is:\n",
	                Arrays.toString(command));
	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	        }
	        
	        //Wait to get exit value
	        try {
	            int exitValue = process.waitFor();
	            System.out.println("\n\nExit Value is " + exitValue);
	        } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        
	  }
	  
	  public static void ex1() throws IOException{

//		    List<String> command = new ArrayList<String>();
//		    command.add(System.getenv("windir") +"\\system32\\"+"tree.com");
//		    command.add("/A");
		    
		    List<String> command = new ArrayList<String>();
		    command.add("C:/Program Files/Java/jdk1.8.0_25/bin/java.exe");
	

		    ProcessBuilder builder = new ProcessBuilder(command);
		    Map<String, String> environ = builder.environment();
		    builder.directory(new File(System.getenv("temp")));

		    System.out.println("Directory : " + System.getenv("temp") );
		    
		    
		    final Process process = builder.start();
		    InputStream is = process.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    String line;
		    while ((line = br.readLine()) != null) {
		      System.out.println(line);
		    }
		    System.out.println("Program terminated!");
		  
	  }
	  
	}