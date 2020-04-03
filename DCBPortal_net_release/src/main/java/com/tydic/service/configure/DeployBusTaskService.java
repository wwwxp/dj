package com.tydic.service.configure;

import java.util.Map;

public interface DeployBusTaskService {
	
	/**
	 * 运行和停止任务
	 * @param param
	 */
	public  String updateDistribute(Map<String, Object> param, String dbKey) throws Exception;
	
	 
}
