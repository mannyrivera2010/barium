package com.earasoft.framework.common;

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.List;
import java.util.Map;

public class StaticUtils {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	}
	
	/**
	 * Used to check required keys for a map
	 * @param map
	 * @param keys
	 * @return
	 */
	public static Map checkMapRequiredKeys(Object input, List keys, String extraInfo = ''){
		if(!(input instanceof Map))
			throw new Exception("The input object is not a [Map] type it is a [" + input.class.getSimpleName()+'] type')
		
		Map map = (Map) input
		
		List missingKeys = []
		
		for (key in keys){
			if(!map.containsKey(key))
				missingKeys << key
		}
		
		if(missingKeys)
			throw new Exception(extraInfo + " - Map is missing keys:" + missingKeys)
		
		return map
	}
	
    /**
     * Checks to see if a specific port is available.
     * http://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
     * @param port the port to check for availability
     */
    public static boolean checkAvailablePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }
        
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }
            
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }
        
        return false;
    }
    
    /**
     * Used to get current time as formatted string
     * @return Formatted String
     */
    public static String getCurrentTimeString(Long mills = System.currentTimeMillis(), String format = "yyyy-MM-dd HH:mm:ss"){
        DateFormat dateFormat = new SimpleDateFormat(format)
        return dateFormat.format(mills).toString()
    }
    
    public static String getCurrentTimeString2(String format = "yyyy-MM-dd HH:mm:ss",Long mills = System.currentTimeMillis()){
        DateFormat dateFormat = new SimpleDateFormat(format)
        return dateFormat.format(mills).toString()
    }
    

}
