package com.tydic.bp.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties("redis")
@Component
public class RedisProperties {
    private String cluster;
    private int maxIdle = 30;
    private int maxTotal = 500;
    private int maxWaitMillis = 100;
    private int maxRedirections = 6;
    private String passWord;
}
