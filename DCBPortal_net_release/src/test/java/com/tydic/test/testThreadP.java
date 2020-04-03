package com.tydic.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Auther: Yuanh
 * Date: 2019-10-16 09:43
 * Description:
 */
public class testThreadP {

    public static void main(String[] args) {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        try {
            final List<String> resultList = new ArrayList<>();



            int paramLength = 10;
            for (int i = 0; i < paramLength; i++) {
//                Thread tt = new Thread(new Runnable() {
//                    private Integer priority = Thread.NORM_PRIORITY;
//                    public Runnable changePriority(Integer priority) {
//                        System.out.println("priority ---> " + priority);
//                        //this.setPriority(priority);
//                        //Thread.currentThread().setPriority(priority);
//                        this.priority = priority;
//                        return this;
//                    }
//
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.currentThread().setPriority(priority);
//                            System.out.println("进入了线程，线程优先级: " + Thread.currentThread().getPriority());
//                            Thread.sleep(1000);
//                            //int exInt = 1 / 0;
//                            System.out.println("线程异常了呢....");
//                            resultList.add(String.valueOf(1));
//                        } catch (Exception e) {
//                            resultList.add(String.valueOf(1));
//                        }
//                    }
//                });
//                tt.setPriority(10);
//                executorService.execute(tt);

                //tt.start();
                executorService.execute(new Runnable() {
                    private Integer priority = Thread.NORM_PRIORITY;
                    public Runnable changePriority(Integer priority) {
                        System.out.println("priority ---> " + priority);
                        this.priority = priority;
                        //this.setPriority(priority);
                        Thread.currentThread().setPriority(priority);
                        return this;
                    }

                    @Override
                    public void run() {
                        try {
                            int i=0;
                            while(true) {
                                System.out.println("当前打印了进程好: " + Thread.currentThread().getName());
                                Thread.sleep(2000);
                                i++;
                                if (i == 3) {
                                    System.out.println("我要跳出来了");
                                    Thread.currentThread().interrupt();
                                }
                            }
//                            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
//                            System.out.println("进入了线程，线程优先级: " + Thread.currentThread().getPriority());
//                            Thread.sleep(1000);
//                            //int exInt = 1 / 0;
//                            System.out.println("线程异常了呢....");
//                            resultList.add(String.valueOf(1));
                        } catch (Exception e) {
                            resultList.add(String.valueOf(1));
                        }
                    }
                }.changePriority(10));
            }
            //轮询等待所有线程执行完成
            System.out.println("resultList size: " + resultList.size());
//            while (resultList.size() < paramLength) {
//                System.out.println("本次总启动DCA进程数:" + paramLength + ", 已经启动完成DCA进程数:" + resultList.size());
//                Thread.sleep(2000);
//            }
            System.out.println("当前主线程中....");
        } catch (Exception e) {
            System.out.println("异常了........");
        }

    }
}
