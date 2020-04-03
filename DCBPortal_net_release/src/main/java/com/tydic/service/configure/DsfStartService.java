package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface DsfStartService {

    /**
     * 启动dsf
     * @param param
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> startDsf(List<Map<String, String>> param, final String dbKey)throws Exception;


    /**
     * 停止dsf
     * @param param
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> stopDsf(List<Map<String, String>> param, final String dbKey)throws Exception;

}
