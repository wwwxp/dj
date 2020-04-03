package com.tydic.util;

import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池单例对象
 * @author Yuanh
 *
 */
public class SingletonThreadPool {

	//日志对象
	private static Logger logger = Logger.getLogger(SingletonThreadPool.class);
	
	// 初始化线程池线程数量
	private static final int THREAD_SIZE = Runtime.getRuntime().availableProcessors();

	private static ThreadPoolExecutor service = null;

	private SingletonThreadPool() {

	}

	/**
	 * 获取线程池对象
	 * 如果是CPU密集型应用，则线程池大小设置为N+1
	 * 如果是IO密集型应用，则线程池大小设置为2N+1
	 * 
	 * @return
	 */
	public static synchronized ThreadPoolExecutor getExecutorService() {
		if (service == null) {
			int threadCount = (THREAD_SIZE * 2) + 1;
			//new ThreadPoolExecutor.CallerRunsPolicy()如果添加到线程池失败，那么主线程会自己去执行该任务
			service = new ThreadPoolExecutor(threadCount, 2 * threadCount, 10, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(200), new ThreadPoolExecutor.CallerRunsPolicy());
			logger.debug("获取线程池对象, 对象地址: " + service + ", 线程池大小: " + threadCount);
		}
		logger.debug("获取线程池对象，线程池大小||正在运行线程数: " + service.getCorePoolSize() + "||" + service.getActiveCount() + "，队列中等待执行的任务数目：" +
				service.getQueue().size() + "，已执行完的任务数目：" + service.getCompletedTaskCount());

		System.out.println("获取线程池对象，线程池大小||正在运行线程数: " + service.getCorePoolSize() + "||" + service.getActiveCount() + "，队列中等待执行的任务数目：" +
				service.getQueue().size() + "，已执行完的任务数目：" + service.getCompletedTaskCount());
		return service;
	}

	public static void main(String[] args) throws InterruptedException {
		for (int i=0; i<5; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					ExecutorService executorService = SingletonThreadPool.getExecutorService();
					System.out.println("executorService ----> " + executorService);
					executorService.submit(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(20000);
								System.out.println("我不停止了...");
								//Thread.currentThread().join();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							System.out.println("执行了一次....");
						}
					});
				}
			}).start();
		}

		while(true) {
			ExecutorService executorService = SingletonThreadPool.getExecutorService();
			Thread.sleep(2000);
		}
		
	}
}
