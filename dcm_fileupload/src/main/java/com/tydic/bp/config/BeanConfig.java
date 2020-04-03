package com.tydic.bp.config;

import com.tydic.bp.service.DFSService;
import com.tydic.bp.service.impl.HdfsDFSServiceImpl;
import com.tydic.bp.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class BeanConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    @Lazy
    public DFSService dfsService() throws Exception{
        HdfsDFSServiceImpl bean=new HdfsDFSServiceImpl();
        bean.init();
        return bean;
    }

    @Bean
    @Lazy
    public RedisUtil redisUtil() throws Exception{
        RedisUtil bean=new RedisUtil();
        bean.setRedisCluster(redisProperties.getCluster());
        bean.setRedisMaxIdle(redisProperties.getMaxIdle());
        bean.setRedisMaxTotal(redisProperties.getMaxTotal());
        bean.setRedisMaxWaitMillis(redisProperties.getMaxWaitMillis());
        bean.setRedisMaxRedirections(redisProperties.getMaxRedirections());
        bean.setRedisPassWord(redisProperties.getPassWord());
        bean.init();
        return bean;
    }
}
