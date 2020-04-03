package com.tydic.service.flow;

import java.util.Map;

public interface SurplusedFlowService {
	
	public Map flowTransferQuery(Map<String,String> param, String dbKey) throws Exception;;

}
