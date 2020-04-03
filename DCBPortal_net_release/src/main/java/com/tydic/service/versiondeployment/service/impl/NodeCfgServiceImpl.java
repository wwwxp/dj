package com.tydic.service.versiondeployment.service.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.versiondeployment.bean.PubCfg2Enty;
import com.tydic.service.versiondeployment.bean.PubCfgLog;
import com.tydic.service.versiondeployment.dao.VersionOptDao;
import com.tydic.service.versiondeployment.service.NodeCfgService;
import com.tydic.util.Constant;
import com.tydic.util.FileUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class NodeCfgServiceImpl implements NodeCfgService {

    private static Logger log = Logger.getLogger(NodeCfgServiceImpl.class);


    @Autowired
    VersionOptDao versionOptDao;

    @Autowired
    CoreService coreService;

    @Override
    public List<PubCfg2Enty> queryCfgInfo(String nodeTypeId, String version, String dbKey) {
        List<PubCfg2Enty> pubCfgEntyList = new ArrayList<>();
        //查询数据库中节点类型信息
        List<PubCfg2Enty> nodeTypeCfgPubList = queryNodeTypeCfg(nodeTypeId, version, dbKey);
        if (BlankUtil.isBlank(nodeTypeCfgPubList)) {
            return pubCfgEntyList;
        }
        pubCfgEntyList.addAll(nodeTypeCfgPubList);

        //查询版本信息
        List<PubCfg2Enty> versionCfgPubList = queryVersionCfgPubList(version, dbKey, nodeTypeCfgPubList);
        if (BlankUtil.isBlank(versionCfgPubList)) {
            return pubCfgEntyList;
        }
        pubCfgEntyList.addAll(versionCfgPubList);
        //ftp命令上去配置文件路径 bin config遍历数组消息

        List<PubCfg2Enty> fileEntyList = null;
        try {
            fileEntyList = getFilePubCfgEnties(nodeTypeId, version, versionCfgPubList);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (BlankUtil.isBlank(fileEntyList)) {
            return pubCfgEntyList;
        }
        pubCfgEntyList.addAll(fileEntyList);
        //组装树状图消息结构，拼接json树响应

        return pubCfgEntyList;
    }


    @Override
    public String updateCfgAndPublish(PubCfg2Enty pubCfg2Enty, String fileContent, String dbkey) {

        PubCfgLog pubCfgLog = new PubCfgLog();

        //文件覆盖版本发布服务
        FtpDto pubFtpDto = SessionUtil.getFtpParams();
        pubFtpDto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
        String ftpType = pubFtpDto.getFtpType();
        String filePath = null;
        Trans trans = FTPUtils.getFtpInstance(pubFtpDto);
        try {
            trans.login();
            if (!trans.isExistPath(pubCfg2Enty.getTargetPath())) {
                log.debug(pubCfgLog.appendLine("版本发布服务器：文件不存在，创建文件"));
            }
            filePath = FileUtil.exactPath(pubCfg2Enty.getFilePath())+pubCfg2Enty.getFileName();
            log.debug(pubCfgLog.appendLine("版本发布服务器：文件上传,"+filePath));
            trans.put(new StringInputStream(fileContent), filePath);
        } catch (Exception e) {
            log.debug(pubCfgLog.appendLine("版本发布服务器：上传文件异常"));
            return pubCfgLog.getStringBuffer().toString();
        } finally {
            trans.close();
        }

        //文件覆盖部署节点

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_TYPE_ID", pubCfg2Enty.getNodeTypeId());
        paramMap.put("VERSION", pubCfg2Enty.getVersion());
        List<HashMap<String, String>> nodeDeployVersionDetailList = versionOptDao.queryNodeDeployVersionDetail(paramMap, dbkey);
        if (BlankUtil.isBlank(nodeDeployVersionDetailList)) {
            log.debug(pubCfgLog.appendLine("节点部署服务器：当前没有部署节点"));
            return pubCfgLog.getStringBuffer().toString();
        }
        log.debug(pubCfgLog.appendLine("节点部署服务器：开始"));

        for (HashMap<String, String> nodeDepVerMap : nodeDeployVersionDetailList) {
            Trans nodeTrans = null;
            try {
                String hostIp = StringTool.object2String(nodeDepVerMap.get("HOST_IP"));
                String user = StringTool.object2String(nodeDepVerMap.get("SSH_USER"));
                String nodePath = nodeDepVerMap.get("NODE_PATH");

                log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]节点部署服务器：文件上传开始 nodePath[%s]", hostIp, user, nodePath)));
                FtpDto ftpDtoOne = new FtpDto();
                ftpDtoOne.setFtpType(ftpType);
                ftpDtoOne.setHostIp(hostIp);
                ftpDtoOne.setUserName(user);
                ftpDtoOne.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
                ftpDtoOne.setPassword(DesTool.dec(StringTool.object2String(nodeDepVerMap.get("SSH_PASSWD"))));
                String pubFilePath = filePath;
                String version = pubCfg2Enty.getVersion();
                String sufxPath = pubFilePath.substring(pubFilePath.indexOf(version));// {版本}/config/xxxx.yml
                //获取节点发布服务器实际的 配置文件路径
                nodePath = FileUtil.exactPath(nodePath) +sufxPath;

                log.debug(pubCfgLog.appendLine(String.format("节点部署服务器：文件上传 NODE_CFG_PATH[%s]", nodePath)));

                nodeTrans = FTPUtils.getFtpInstance(ftpDtoOne);
                nodeTrans.login();
                log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]节点部署服务器：文件上传开始 文件路径[%s]", hostIp, user, nodePath)));
                nodeTrans.put(new StringInputStream(fileContent), nodePath);
                log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]节点部署服务器：文件上传完成 文件路径[%s]", hostIp, user, nodePath)));
            } catch (Exception e) {
                log.debug(pubCfgLog.appendLine("节点部署服务器：上传文件异常--->" + e.getMessage()));
                continue;
            } finally {
                nodeTrans.close();
            }
            log.debug(pubCfgLog.appendLine("节点部署服务器：结束"));
        }
        log.debug(pubCfgLog.appendLine("配置上传更新和分发部署：完成"));


        return pubCfgLog.getStringBuffer().toString();
    }

    /**
     * 获取部署主机配置文件内容
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map 返回对象
     */
    @Override
    public Map<String, String> getFileContent(Map<String, String> params, String dbKey) throws Exception {
        log.debug("获取配置文件内容， 参数: " + params + ", dbKey: " + dbKey);
        // 获取所有参数信息
        FtpDto ftpDto = SessionUtil.getFtpParams();
        String webRootPath = params.get("webRootPath");

        Map<String, String> cont = new HashMap<String, String>();

        //文件名称
        String fileName = params.get("fileName");
        //文件路径
        String filePath = params.get("filePath");
        // 目录拼接
        String path = FileTool.exactPath(filePath) + fileName;
        log.debug("配置文件路径: " + path);
//
//		//获取部署真实目录
//		String clusterId = StringTool.object2String(params.get("clusterId"));
//		String clusterType = StringTool.object2String(params.get("clusterType"));
//		if (!BlankUtil.isBlank(clusterId)) {
//			Map<String, Object> queryClusterMap = new HashMap<String, Object>();
//			queryClusterMap.put("CLUSTER_ID", clusterId);
//			queryClusterMap.put("CLUSTER_TYPE", clusterType);
//			Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryServiceTypeList", queryClusterMap, dbKey);
//			if (!BlankUtil.isBlank(clusterMap) && !BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
//				String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
//				clusterType = StringTool.object2String(clusterMap.get("CLUSTER_TYPE"));
//				cont.put("REAL_DEPLOY_PATH", appRootPath + Constant.Tools + Constant.CONF + clusterType);
//			}
//		}

        // 文件内容
        String fileContent = "";
        String localPath = webRootPath + Constant.TMP + UUID.randomUUID().toString() + "/";
        Trans trans = null;
        try {
            trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType(),FTPUtils.TIMEOUT_DEF_MS);
            trans.login();
            if (trans.isExistPath(path)) {
                cont.put("REAL_DEPLOY_PATH",ftpDto.getHostIp()+" -> "+path);
            }
            trans.get(path, localPath + fileName);
            // 将文件转成字符串
            fileContent = FileUtil.readFileUnicode(localPath + fileName);
            // 去掉尾部所有空格
            int len = fileContent.length();
            int st = 0;
            char[] val = fileContent.toCharArray();
            while ((st < len) && (val[len - 1] <= ' ')) {
                len--;
            }
            fileContent = (len < fileContent.length()) ? fileContent.substring(st, len) : fileContent;
            log.debug("获取" + path + "文件内容成功！");
        } catch (Exception e) {
            log.error("获取" + path + "文件内容失败-->", e);
            throw new Exception(e.getMessage());
        } finally {
            if (trans != null) {
                trans.close();
            }
        }
        cont.put("fileContent", fileContent);
        try {
            //删除本地文件
            FileUtil.deleteFile(localPath + fileName);
            FileUtil.deleteFile(localPath);
            log.debug("删除本地文件目录/文件成功:" + localPath);
        } catch (Exception e) {
            log.error("删除本地文件目录/文件失败:" + localPath);
        }
        log.debug("获取配置文件内容结束...");
        return cont;
    }


    private List<PubCfg2Enty> getFilePubCfgEnties(String nodeTypeId, String version, List<PubCfg2Enty> versionCfgPubList) throws InvocationTargetException, IllegalAccessException {
        List<PubCfg2Enty> fileEntyList = new ArrayList<>();
        if (BlankUtil.isBlank(versionCfgPubList)) {
            return fileEntyList;
        }
        FtpDto ftpDto = SessionUtil.getFtpParams();
        Trans trans = null;
        for (PubCfg2Enty verCfgEnty : versionCfgPubList) {
            if (!PubCfg2Enty.CFG_TYPE_VERSION.equals(verCfgEnty.getCfgType())) {
                continue;
            }            //登录部署主机
            ftpDto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
            trans = FTPUtils.getFtpInstance(ftpDto);
            try {
                trans.login();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String filePath = verCfgEnty.getTargetPath();
            if (BlankUtil.isBlank(filePath) || !trans.isExistPath(filePath)) {
                log.debug("目录不存在" + filePath);
                continue;
            }
            List<FileRecord> fileRecordList = null;
            try {
                fileRecordList = new ArrayList<>();
                trans.getAllFileList(fileRecordList, filePath, verCfgEnty.getCurrId());
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                trans.close();
            }
            if (fileRecordList == null || fileRecordList.isEmpty()) {
                continue;
            }
            for (FileRecord fileRecord : fileRecordList) {
                PubCfg2Enty cfgEnty = new PubCfg2Enty();
                BeanUtils.copyProperties(cfgEnty, fileRecord);

                cfgEnty.setVersion(verCfgEnty.getVersion());
                cfgEnty.setNodeTypeId(verCfgEnty.getNodeTypeId());
                //目录或文件
                cfgEnty.setCfgType(fileRecord.isDirectory() ? PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH : PubCfg2Enty.CFG_TYPE_CFG_FILE_PATH);
                fileEntyList.add(cfgEnty);
            }
        }
        return fileEntyList;
    }

    private List<PubCfg2Enty> queryVersionCfgPubList(String version, String dbKey, List<PubCfg2Enty> nodeTypeCfgPubList) {
        List<PubCfg2Enty> versionCfgPubList = new ArrayList<>();
        if (nodeTypeCfgPubList == null) {
            return versionCfgPubList;
        }
        //查询版本信息
        for (PubCfg2Enty nodeTypePubCfg : nodeTypeCfgPubList) {
            if (nodeTypePubCfg == null || !PubCfg2Enty.CFG_TYPE_NODETYPE.equals(nodeTypePubCfg.getCfgType())) {
                //树结构，父类型为 节点(程序)类型
                continue;
            }
            String nodeTypeId = nodeTypePubCfg.getNodeTypeId();
            Map<String, String> queryParam = new HashMap<>();
            queryParam.put("VERSION", version);
            queryParam.put("NODE_TYPE_ID", nodeTypeId);
            log.debug(String.format("查询数据库中节点类型信息 NODE_TYPE_ID[%s] VERSION[%s]", nodeTypeId, version));
            List<HashMap<String, String>> nodeTypeVersionList = versionOptDao.queryNodeTypeVersionListTbl(queryParam, dbKey);
            for (Map<String, String> verMap : nodeTypeVersionList) {
                String node_version = StringTool.object2String(verMap.get("VERSION"));
                String file_desc = StringTool.object2String(verMap.get("FILE_DESC"));
                String cfg_path = StringTool.object2String(verMap.get("CFG_PATH"));
                PubCfg2Enty verPubCfg2Enty = new PubCfg2Enty();
                verPubCfg2Enty.setCurrId(PubCfg2Enty.appendChildId(nodeTypePubCfg, node_version));
                verPubCfg2Enty.setFileName(node_version);
                verPubCfg2Enty.setNodeTypeId(nodeTypeId);
                verPubCfg2Enty.setDesc(file_desc);
                verPubCfg2Enty.setVersion(node_version);
                verPubCfg2Enty.setCfgType(PubCfg2Enty.CFG_TYPE_VERSION);
                verPubCfg2Enty.setParentId(nodeTypeId);
                verPubCfg2Enty.setFilePath(cfg_path.replace(node_version+"/",""));
                verPubCfg2Enty.setTargetPath(cfg_path);
                versionCfgPubList.add(verPubCfg2Enty);
            }
        }
        return versionCfgPubList;
    }

    private List<PubCfg2Enty> queryNodeTypeCfg(String nodeTypeId, String version, String dbKey) {
        List<PubCfg2Enty> pubCfgEntyList = new ArrayList<>();
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("ID", nodeTypeId);
        log.debug(String.format("查询数据库中节点类型信息 nodeTypeId[%s] version[%s]", nodeTypeId, version));
        List<HashMap<String, String>> noteTypeConfigList = versionOptDao.queryNoteTypeConfig(queryParam, dbKey);
        if (BlankUtil.isBlank(noteTypeConfigList)) {
            //节点类型未找到
            return pubCfgEntyList;
        }
        for (Map<String, String> nodeTypeMap : noteTypeConfigList) {
            String node_type_config_id = StringTool.object2String(nodeTypeMap.get("ID"));
            String node_type_name = StringTool.object2String(nodeTypeMap.get("NAME"));
            String node_type_code = StringTool.object2String(nodeTypeMap.get("CODE"));
            String node_type_desc = StringTool.object2String(nodeTypeMap.get("DESC"));

            PubCfg2Enty pubCfgEnty = new PubCfg2Enty();
            pubCfgEnty.setCurrId(node_type_config_id);
            pubCfgEnty.setFileName(node_type_name + "(" + node_type_code + ")");
            pubCfgEnty.setNodeTypeId(node_type_config_id);
            pubCfgEnty.setDesc(node_type_desc);
            pubCfgEnty.setCfgType(PubCfg2Enty.CFG_TYPE_NODETYPE);
            pubCfgEnty.setParentId("0");
            pubCfgEntyList.add(pubCfgEnty);
        }
        return pubCfgEntyList;
    }
}
