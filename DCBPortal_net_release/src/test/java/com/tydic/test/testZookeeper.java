package com.tydic.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

/**
 * Auther: Yuanh
 * Date: 2019-10-17 11:19
 * Description:
 */
public class testZookeeper {

    public static void main(String[] args)  {

        try {
            //获取配置文件 zk.properties 创建客户端参数
            int baseSleepTimeMs = 1000;
            int maxRetries = 5;
            String connectString = "192.168.161.25:22810,192.168.161.26:22810,192.168.161.27:22810";
            //String connectString = "192.168.161.25:22811,192.168.161.26:22811,192.168.161.27:22811";
            int sessionTimeoutMs = 5000;
            int connectionTimeoutMs = 3000;
            //创建client对象
            //RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries, maxSleepMs);
            RetryPolicy retryPolicy = new RetryNTimes(maxRetries, baseSleepTimeMs);
            CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString(connectString)
                    .sessionTimeoutMs(sessionTimeoutMs)
                    .connectionTimeoutMs(connectionTimeoutMs)
                    .retryPolicy(retryPolicy)
                    .build();

            client.start();
            System.out.println("成功过了.....");

            String zkRegPathPrefix = "/codelast/service-provider-";
            String regContent = "5AAAAA";

            MyConnectionStateListener stateListener = new MyConnectionStateListener(zkRegPathPrefix, regContent);
            client.getConnectionStateListenable().addListener(stateListener);

            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(zkRegPathPrefix, regContent.getBytes("UTF-8"));

            byte[] bytes = client.getData().forPath("/DCA01/RedisCfg");
            System.out.println(new String(bytes));


            Thread.currentThread().interrupt();
            //Thread.currentThread().join();
            int aa = 1/0;
            System.out.println("aaaa");
        } catch (Exception e) {
            System.out.println("异常了呢");


        }
    }
}
