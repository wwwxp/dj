package com.tydic.service.versiondeployment.dao;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.util.NodeConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VersionOptDao {
    @Autowired
    CoreService coreService;

    public List<HashMap<String, String>> queryNoteTypeConfig(Map<String, String> paramMap, String dbkey) {
        return coreService.queryForList("versionOptService.queryNodeTypeCfg", paramMap, dbkey);
    }

    public Map<String, String> queryNoteTypeConfigById(String nodeTypeCfgId, String dbkey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ID", nodeTypeCfgId);
        List<HashMap<String, String>> list = queryNoteTypeConfig(paramMap, dbkey);
        return BlankUtil.isBlank(list) ? null : list.get(0);
    }

    /**
     * 程序是否web类型
     *
     * @param nodeTypeId
     * @param dbKey
     * @return
     */
    public boolean isNodeTypeRunWeb(String nodeTypeId, String dbKey) {
        boolean isRunweb = false;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ID", nodeTypeId);
        List<HashMap<String, String>> noteTypeConfig = queryNoteTypeConfig(paramMap, dbKey);
        if (!BlankUtil.isBlank(noteTypeConfig)) {
            HashMap<String, String> nodeType = noteTypeConfig.get(0);
            isRunweb = NodeConstant.RUN_WEB.endsWith(StringTool.object2String(nodeType.get("RUN_WEB")));
        }
        return isRunweb;
    }


    /**
     * 更新版本信息
     *
     * @param currVersion
     * @param updateUser
     * @param cfgId
     * @param dbKey
     * @return
     */
    public int updateNodeTypeCfgVersion(String currVersion, String updateUser, String cfgId, String dbKey) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("CURR_VERSION", currVersion);
        paramMap.put("UPDATE_USER", updateUser);
        paramMap.put("ID", cfgId);
        return coreService.updateObject2New("versionOptService.updateNodeTypeCfgVersion", paramMap, dbKey);
    }

    /**
     * 新增版本信息
     */
    public int insertNodeTypeVersionListTbl(String fileName, String filePath, String cfgPath, String version, String uploadUser, String nodeTypeId, String isFullVersion, String fileDesc, String dbKey) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("FILE_NAME", fileName);
        paramMap.put("FILE_PATH", filePath);
        paramMap.put("CFG_PATH", cfgPath);
        paramMap.put("VERSION", version);
        paramMap.put("UPLOAD_USER", uploadUser);
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        paramMap.put("IS_FULL_VERSION", isFullVersion);// 0 增量  1全量
        paramMap.put("FILE_DESC", StringUtils.substring(fileDesc, 0, 300));
        return insertNodeTypeVersionListTbl(paramMap, dbKey);
    }

    /**
     * 新增版本信息
     */
    public int insertNodeTypeVersionListTbl(Map<String, Object> paramMap, String dbKey) {
        return coreService.insertObject2New("versionOptService.insertNodeTypeVersionListTbl", paramMap, dbKey);
    }

    /**
     * 查询单个版本信息表
     *
     * @param paramMap
     * @param dbKey
     * @return
     */
    public Map<String, String> queryOneNodeTypeVersionListTbl(Map<String, String> paramMap, String dbKey) {
        List<HashMap<String, String>> list = coreService.queryForList("versionOptService.queryNodeTypeVersionListTbl", paramMap, dbKey);
        return BlankUtil.isBlank(list) ? null : list.get(0);
    }

    /**
     * 查询版本信息表 列表
     *
     * @param paramMap
     * @param dbKey
     * @return
     */
    public List<HashMap<String, String>> queryNodeTypeVersionListTbl(Map<String, String> paramMap, String dbKey) {
        List<HashMap<String, String>> list = coreService.queryForList("versionOptService.queryNodeTypeVersionListTbl", paramMap, dbKey);
        return list;
    }

    /**
     * 查询节点部署信息表
     *
     * @param paramMap
     * @param dbKey
     * @return
     */
    public List<HashMap<String, String>> queryNodeDeployListTbl(Map<String, String> paramMap, String dbKey) {
        return coreService.queryForList("versionOptService.queryNodeDeployListTbl", paramMap, dbKey);
    }

    /**
     * 使用id查询主机列表
     *
     * @param hostId
     * @param dbKey
     * @return
     */
    public List<HashMap<String, String>> queryHostInfoById(String hostId, String dbKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("HOST_ID", hostId);
        return coreService.queryForList("versionOptService.queryHostInfoById", paramMap, dbKey);
    }

    /**
     * 查询启停程序信息表
     *
     * @param paramMap
     * @param dbKey
     * @return
     */
    public List<HashMap<String, String>> queryNodeStartList(Map<String, String> paramMap, String dbKey) {
        return coreService.queryForList("versionOptService.queryNodeStartList", paramMap, dbKey);
    }

    /**
     * 查询 节点部署的版本信息
     *
     * @param paramMap
     * @param dbKey
     * @return
     */
    public List<HashMap<String, String>> queryNodeDeployVersionDetail(Map<String, String> paramMap, String dbKey) {
        return coreService.queryForList("versionOptService.queryNodeDeployVersionDetail", paramMap, dbKey);
    }

    /**
     * 删除 节点部署版本信息
     */

    public int deleteNodeDeployListTbl(Map<String, String> paramMap, String dbKey) {
        return coreService.deleteObject("versionOptService.deleteNodeDeployListTbl", paramMap, dbKey);
    }

    /**
     * 写入节点操作日志
     *
     * @param operatorModule
     * @param operatorName
     * @param createdUser
     * @param logContent
     * @param dbKey
     * @return
     */
    public int insertNodeOptLog(String operatorModule, String operatorName, String createdUser, String logContent, String dbKey) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("OPERATOR_MODULE", operatorModule);
        paramMap.put("OPERATOR_NAME", operatorName);
        paramMap.put("CREATED_USER", createdUser);
        paramMap.put("LOG_CONTENT", logContent);
        return insertNodeOptLog(paramMap, dbKey);
    }

    public int insertNodeOptLog(Map<String, Object> paramMap, String dbKey) {
        return coreService.insertObject2New("versionOptService.insertNodeOptLog", paramMap, dbKey);
    }

    public int deleteNodeStartList(Map<String, String> paramMap, String dbKey) {
        return coreService.deleteObject("versionOptService.deleteNodeStartList", paramMap, dbKey);
    }

    public int deleteNodeTypeVersionById(String id, String dbKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ID", id);
        return coreService.deleteObject("versionOptService.deleteNodeTypeVersionById", paramMap, dbKey);
    }

    public int updateNodeTypeCfgContextCfg(String nodeTypeId, String contextCfg, String dbKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        paramMap.put("CONTEXT_CFG", contextCfg);
        return coreService.deleteObject("versionOptService.updateNodeTypeCfgContextCfg", paramMap, dbKey);
    }

}
