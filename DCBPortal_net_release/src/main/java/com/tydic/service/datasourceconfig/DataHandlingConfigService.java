package com.tydic.service.datasourceconfig;

import java.util.List;
import java.util.Map;

public interface DataHandlingConfigService {
	
	/**
	 * 插入新表数据处理的数据
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public int insertNewHandling(Map<String, Object> params, String dbKey) throws Exception;	
	
	/**
	 * 修改表数据处理的数据
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public int updateHandling(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 删除表数据处理
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public int deleteHandling(Map<String, String> params, String dbKey) throws Exception;	
}
