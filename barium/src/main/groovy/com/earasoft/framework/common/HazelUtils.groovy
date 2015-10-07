package com.earasoft.framework.common;

import com.hazelcast.core.Cluster
import com.hazelcast.core.Member

public class HazelUtils {
 
    public static List<Map> getClusterMemberInfo(Cluster cluster){
        List members = []
		
		String localMembersUuid = cluster.getLocalMember().getUuid()
		
        cluster.getMembers().each{Member member ->
         
            
			Map temp = [:]
			temp['uuid'] = member.getUuid()
			temp['sameUuid'] = member.getUuid() == localMembersUuid
            temp['attributes'] = member.getAttributes()
			if(temp['attributes'].get('nodeId') != null){
				temp['nodeId'] = temp['attributes'].get('nodeId')
				//temp['attributes'].remove('nodeId')
			}
            temp['socketAddress']= member.getSocketAddress().toString()
			members << temp
        }
        return members
    }
}
