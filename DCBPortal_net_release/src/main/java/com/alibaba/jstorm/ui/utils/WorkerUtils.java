package com.alibaba.jstorm.ui.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * worker数据局部刷新
 * @author tu
 *
 */
public class WorkerUtils {
	/**
	 * 存储每次map
	 */
	public static List<Map<String,Object>> workerMetricList = new ArrayList<>();
	
	/**
	 * 每次取出数据后的时间
	 */
	public static List<Date> dateList = new ArrayList<>();

	/**
	 * 添加workerMetric
	 * @param workerMetric
	 */
	public static void addWorkerMetric(Map<String,Object> workerMetric){
		workerMetricList.add(workerMetric);
	}
	
	/**
	 * 添加最新时间
	 */
	public static void addDate(){
		dateList.add(new Date());
	}
	
	/**
	 * 删除workerMetricList中的第一个map
	 */
	public static void removeWorkerMetric(){
		workerMetricList.remove(0);
	}
	
	/**
	 * 时间间隔超过1小时,返回false,否则返回true
	 * @return
	 */
	public static boolean isFull(){
		long intervel = dateList.get(dateList.size() - 1).getTime() - dateList.get(0).getTime();
		return intervel > 1000*60*60 ? false : true;
	}
}
