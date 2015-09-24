package com.earasoft.framework.common;

import com.hazelcast.core.Cluster
import com.hazelcast.core.Member

public class HazelUtils {
 
    public static Map getClusterMemberInfo(Cluster cluster){
        Map membersInfo = ["members":[:]]
        cluster.getMembers().each{Member member ->
            if(membersInfo["members"][member.getUuid()]==null)
                membersInfo["members"][member.getUuid()] = [:]
            
            membersInfo["members"][member.getUuid()]['attributes']= member.getAttributes()
            membersInfo["members"][member.getUuid()]['socketAddress']= member.getSocketAddress().toString()
        }
        return membersInfo
    }
}
