package com.tydic.bp.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="redis")
@Data
public class RedisInfo {
    private String ip;
    private Integer port;
    private String slaveIp;
    private Integer slavePort;
    private String acctId;
    private String userName;
    private String passwd;
    private Integer expireTime;

    //key存活时间（单位：天）
    public void setExpireTime(Integer expireTime){
        if(expireTime==null){
            expireTime = 90;
        }
        this.expireTime=expireTime*24*3600;
    }
}
