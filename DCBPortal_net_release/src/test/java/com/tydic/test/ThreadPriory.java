package com.tydic.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Auther: Yuanh
 * Date: 2019-08-22 11:14
 * Description:
 */
public class ThreadPriory {

    public static void main(String[] args) {
        Thread tt = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("打印测试一下有限家");
            }
        });
        tt.setPriority(69);
//        tt.start();


        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        executorService.execute(tt);
        System.out.println("bbbbbbbbb");

    }
}
