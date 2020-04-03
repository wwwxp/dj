package com.tydic.bp.util;

import com.tydic.bp.exception.DCFileException;
import com.tydic.bp.exception.ERRORS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class RedisUtil {

    private String redisCluster;
    private int redisMaxIdle = 30;

    private int redisMaxTotal = 500;

    private int redisMaxWaitMillis = 100;

    private int redisMaxRedirections = 6;

    private JedisCluster cluster = null;
    private String redisPassWord;
    private int redisTimeOut = 2000;

    public void init()
            throws DCFileException
    {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxIdle(this.redisMaxIdle);
        config.setMaxTotal(this.redisMaxTotal);
        config.setMaxWaitMillis(this.redisMaxWaitMillis);

        if (StringUtils.isBlank(this.redisCluster)) {
            throw ERRORS.ERR_INVALID_ARGUMENT.ERROR();
        }

        Set<HostAndPort> set = new HashSet();
        String[] servers = this.redisCluster.split(",");
        for (int i = 0; i < servers.length; i++) {
            String[] server = servers[i].split(":");
            set.add(new HostAndPort(server[0], Integer.parseInt(server[1])));
        }

        for (HostAndPort hostAndPort : set) {
            log.info("print redisCluster server: " + hostAndPort.getHost() + ":" + hostAndPort.getPort());
        }
        log.info("Init JedisCluster.timeout:" + this.redisTimeOut + ",redisMaxRedirections:" + this.redisMaxRedirections + ",redisPassWord:" + this.redisPassWord);
        if ((this.redisPassWord == null) || (StringUtils.isEmpty(this.redisPassWord))) {
            this.cluster = new JedisCluster(set, this.redisTimeOut, this.redisTimeOut, this.redisMaxRedirections, config);
        } else {
            this.redisPassWord = DesUtils.decode(this.redisPassWord);
            log.info("redisPassWord decode:" + this.redisPassWord);
            try {
                this.cluster = new JedisCluster(set, this.redisTimeOut, this.redisTimeOut, this.redisMaxRedirections, this.redisPassWord, config);
            } catch (Exception e) {
                e.printStackTrace();
                log.info(e.getMessage(), e);
            }
            log.info("cluster:" + this.cluster);
        }

        if (this.cluster == null) {
            throw ERRORS.ERR_REDIS_CONNECT_ERROR.ERROR();
        }

        log.info("Init JedisCluster success.");
    }

    public void close()
    {
        log.info("Close JedisCluster.]");
        try {
            this.cluster.close();
        } catch (Exception e) {
            log.error("Close JedisCluster exception: " + e.getMessage());
        }
    }

    public String getRedisCluster()
    {
        return this.redisCluster;
    }

    public void setRedisCluster(String redisCluster)
    {
        this.redisCluster = redisCluster;
    }

    public int getRedisMaxIdle()
    {
        return this.redisMaxIdle;
    }

    public void setRedisMaxIdle(int redisMaxIdle)
    {
        this.redisMaxIdle = redisMaxIdle;
    }

    public int getRedisMaxTotal()
    {
        return this.redisMaxTotal;
    }

    public void setRedisMaxTotal(int redisMaxTotal)
    {
        this.redisMaxTotal = redisMaxTotal;
    }

    public int getRedisMaxWaitMillis()
    {
        return this.redisMaxWaitMillis;
    }

    public void setRedisMaxWaitMillis(int redisMaxWaitMillis)
    {
        this.redisMaxWaitMillis = redisMaxWaitMillis;
    }

    public int getRedisMaxRedirections()
    {
        return this.redisMaxRedirections;
    }

    public void setRedisMaxRedirections(int redisMaxRedirections)
    {
        this.redisMaxRedirections = redisMaxRedirections;
    }

    public JedisCluster getCluster()
    {
        return this.cluster;
    }

    public void setCluster(JedisCluster cluster)
    {
        this.cluster = cluster;
    }

    public String getRedisPassWord() {
        return this.redisPassWord;
    }

    public void setRedisPassWord(String redisPassWord) {
        this.redisPassWord = redisPassWord;
    }

    public int getRedisTimeOut() {
        return this.redisTimeOut;
    }

    public void setRedisTimeOut(int redisTimeOut) {
        this.redisTimeOut = redisTimeOut;
    }
}
