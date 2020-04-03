package com.tydic.service.clustermanager;

import com.tydic.bean.DeployViewDTO;

import java.util.List;
import java.util.Map;

public interface DeployViewService {

	/**
	 * 查询业务部署视图
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<DeployViewDTO> queryDeployViewList(Map<String, Object> params, String dbKey) throws Exception;
}
