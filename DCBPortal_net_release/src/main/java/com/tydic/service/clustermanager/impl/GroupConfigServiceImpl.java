package com.tydic.service.clustermanager.impl;

import com.tydic.bp.core.service.CoreService;
import com.tydic.service.clustermanager.GroupConfigService;
import com.tydic.util.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置参数管理实现
 */
@Service
@SuppressWarnings("all")
public class GroupConfigServiceImpl implements GroupConfigService {

    private static Logger log = LoggerFactory.getLogger(GroupConfigServiceImpl.class);

    @Resource
    public CoreService coreService;

    /**
     * 添加参数配置
     * @param param
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> addGroupConfig(Map<String, Object> param, String dbKey) throws Exception {
        log.info("添加&修改参数配置，业务参数：{}， dbKey:{}", param, dbKey);
        Map<String, Object> retMap = new HashMap<String, Object>(){{
            put("RST_CODE", "1");
        }};
        String configCode = StringTool.object2String(param.get("CONFIG_CODE"));
        if (StringUtils.isBlank(configCode)) {
            coreService.insertObject2New("config.addGroupConfig", param, dbKey);
        } else {
            coreService.updateObject2New("config.updateGroupConfig", param, dbKey);
        }
        log.info("添加&修改参数配置成功...");
        return retMap;
    }

    /**
     * 参数参数配置
     * @param param
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> delGroupConfig(Map<String, Object> param, String dbKey) throws Exception {
        log.info("删除参数配置，业务参数：{}， dbKey:{}", param, dbKey);
        Map<String, Object> retMap = new HashMap<String, Object>(){{
            put("RST_CODE", "1");
        }};
        coreService.deleteObject2New("config.delGroupConfig", param, dbKey);
        log.info("删除参数配置成功...");
        return retMap;
    }
}
