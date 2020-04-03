package com.tydic.service.nodecluster.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.nodecluster.NodeClusterDeployService;
import com.tydic.service.versiondeployment.util.NodeVerUtil;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.*;

/**
 * Auther: Yuanh、wxp
 * Date: 2019-09-19 09:53
 * Description:
 */
@Service
public class NodeClusterDeployServiceImpl implements NodeClusterDeployService {

    private Logger logger = LoggerFactory.getLogger(NodeClusterDeployServiceImpl.class);

    @Autowired
    private CoreService coreService;

    /**
     * 节点集群查询程序列表
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryNodeClusterProgramList(Map<String, Object> params, String dbKey) throws Exception {
        logger.debug("节点集群查询程序列表，业务参数:{}, dbKey:{}", params, dbKey);

        Map<String, Object> resultMap = new HashMap<String, Object>(){{
            put("retCode", BusinessConstant.PARAMS_BUS_1);
        }};
        List<Map<String, Object>> programList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeClusterProgramList", null, dbKey);
        resultMap.put("DATA", programList);
        logger.debug("节点集群部署查询程序列表，返回结果:{}", resultMap);
        return resultMap;
    }

    /**
     * 查询节点集群程序版本列表&主机列表
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryNodeClusterVersionHostList(Map<String, Object> params, String dbKey) throws Exception {
        logger.debug("查询节点集群程序版本列表&主机列表，业务参数:{}, dbKey:{}", params, dbKey);
        Map<String, Object> resultMap = new HashMap<String, Object>(){{
            put("retCode", BusinessConstant.PARAMS_BUS_1);
        }};

        //查询程序上传版本列表
        List<Map<String, Object>> versionList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeTypeVersionList", params, dbKey);
        resultMap.put("VERSION_LIST", versionList);

        //查询程序部署节点列表
        List<Map<String, Object>> nodeList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeTypeHostList", params, dbKey);
        resultMap.put("NODE_LIST", nodeList);

        //判断节点是否为WEB程序
        Map<String,Object> runWeb=coreService.queryForObject2New("nodeClusterDeployMapper.queryRunWeb",params,dbKey);

        //如果为WEB程序，则获取web容器列表
        if(NodeConstant.RUN_WEB.equals(StringTool.object2String(runWeb.get("RUN_WEB")))){
            resultMap.put("isWeb", true);

            //获取web模板
            FtpDto ftpParams = SessionUtil.getFtpParams();
            ftpParams.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
            Trans ftpClient = FTPUtils.getFtpInstance(ftpParams);
            FTPUtils.tryLogin(ftpClient);

            String web_dir = SystemProperty.getContextProperty("node.manager.tomcat_temp_dir");
            web_dir = BlankUtil.isBlank(web_dir) ? "templates" : web_dir;
            String webContainerPath = FileTool.exactPath(ftpParams.getFtpRootPath()) + FileTool.exactPath(web_dir);
            Vector<FileRecord> fileList = ftpClient.getFileList(webContainerPath);

            List<Map<String,Object>> webList=new ArrayList<>();
            Map<String,Object> webTemplates=null;
            for(int i=0;i<fileList.size();++i){
                webTemplates = new HashMap<String,Object>();
                webTemplates.put("WEB_TEMPLATES",fileList.get(i).getFileName());
                webList.add(webTemplates);
            }

            resultMap.put("webContainers", webList);
        }

        logger.debug("查询节点集群程序版本列表&主机列表，返回结果:{}", resultMap);
        return resultMap;
    }

    /**
     * 非web程序的节点部署
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> startNodeDeploy(Map<String, Object> params, String dbKey) throws Exception {

        logger.debug("节点部署，业务参数:{}, dbKey:{}", params, dbKey);
        Map<String, Object> resultMap = new HashMap<String, Object>(){{
            put("retCode", BusinessConstant.PARAMS_BUS_1);
        }};

        //程序编码
        String programCode = ObjectUtils.toString(params.get("NODE_TYPE_CODE"));
        //版本信息
        String version = ObjectUtils.toString(params.get("VERSION_NAME"));
        //节点程序ID
        String nodeTypeId = ObjectUtils.toString(params.get("NODE_TYPE_ID"));
        //版本包上传包名称
        String fileName = ObjectUtils.toString(params.get("FILE_NAME"));

        StringBuffer deployBuffer = new StringBuffer();
        Trans remoteFtpClient = null;
        Trans ftpClient = null;
        boolean nodeVersionFirstDeploy = false;
        boolean nodeFirstDeploy = false;
        String nodePath = null;
        ShellUtils shellUtils = null;
        try {
            //部署节点列表
            List<Map<String, Object>> nodeList = (List<Map<String, Object>>) params.get("NODE_LIST");

            deployBuffer.append("当前部署的节点名称："+StringTool.object2String(nodeList.get(0).get("NODE_NAME"))+"，");
            deployBuffer.append("节点所在主机："+StringTool.object2String(nodeList.get(0).get("HOST_TEXT"))+"，");
            deployBuffer.append("节点路径："+StringTool.object2String(nodeList.get(0).get("NODE_PATH")));

            //存储nodeID，为页面显示成功失败做准备
            resultMap.put("nodeId", StringTool.object2String(nodeList.get(0).get("NODE_ID")));
            resultMap.put("hostText", StringTool.object2String(nodeList.get(0).get("HOST_TEXT")));
            resultMap.put("nodePath", StringTool.object2String(nodeList.get(0).get("NODE_PATH")));

            //根据程序类型，版本号查询包名称
            if (StringUtils.isBlank(fileName)) {
                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put("VERSION", version);
                queryMap.put("NODE_TYPE_ID", nodeTypeId);
                List<Map<String, Object>> versionList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeTypeVersionList", queryMap, dbKey);
                if (CollectionUtils.isNotEmpty(versionList)) {
                    fileName = ObjectUtils.toString(versionList.get(0).get("FILE_NAME"));
                } else {
                    resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
                    resultMap.put("retMsg", "获取程序包失败，请检查!<br/>部署结果：<span style='color:red;font-weight:bold;'>失败</span><br/>");
                    return resultMap;
                }
            }

            if (CollectionUtils.isNotEmpty(nodeList)) {
                FtpDto ftpDto = SessionUtil.getFtpParams();
                ftpDto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
                ftpClient = FTPUtils.getFtpInstance(ftpDto);
                for (Map<String, Object> nodeMap : nodeList) {
                    nodePath=ObjectUtils.toString(nodeMap.get("NODE_PATH"));

                    long startTimes = System.currentTimeMillis();
                    //获取版本发布服务器程序
                    String deploy_dir = SystemProperty.getContextProperty("node.manager.deploy_dir");
                    deploy_dir = BlankUtil.isBlank(deploy_dir) ? "node_data" : deploy_dir;
                    String versionPublishPath = FileTool.exactPath(ftpDto.getFtpRootPath()) + FileTool.exactPath(deploy_dir) + FileTool.exactPath(programCode) + FileTool.exactPath(version) + fileName;
                    logger.info("部署版本发布服务器，主机IP:{},用户名:{}，路径:{}", ftpDto.getHostIp(), ftpDto.getUserName(), versionPublishPath);
                    deployBuffer.append("<br/>开始连接部署版本发布服务器，准备获取版本包文件...");
                    FTPUtils.tryLogin(ftpClient);
                    InputStream inputStream = ftpClient.get(versionPublishPath);

                    deployBuffer.append("<br/>部署版本发布服务器连接成功...");
                    logger.info("部署版本发布服务器连接成功...");
                    deployBuffer.append("<br/>部署开始时间: ").append(DateUtil.getCurrent(DateUtil.allPattern));
                    deployBuffer.append("<br/>版本发布服务器连接就绪...");

                    //推送版本发布服务器程序到远程主机对应目录
                    String nodeId = ObjectUtils.toString(nodeMap.get("NODE_ID"));

                    String remoteDeployFilePath = FileTool.exactPath(ObjectUtils.toString(nodeMap.get("NODE_PATH"))) + FileTool.exactPath(version);
                    String remoteDeployPath = remoteDeployFilePath + fileName;

                    Map<String, Object> nodeQueryMap = new HashMap<>();
                    nodeQueryMap.put("NODE_ID", nodeId);
                    nodeQueryMap.put("NODE_HOST_ID", nodeMap.get("NODE_HOST_ID"));
                    List<Map<String, Object>> nodeHostList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeHostList", nodeQueryMap, dbKey);
                    if (CollectionUtils.isNotEmpty(nodeHostList)) {

                        String hostIp = ObjectUtils.toString(nodeHostList.get(0).get("HOST_IP"));
                        String userName = ObjectUtils.toString(nodeHostList.get(0).get("SSH_USER"));
                        String passwd = ObjectUtils.toString(nodeHostList.get(0).get("SSH_PASSWD"));
                        passwd = DesTool.dec(passwd);
                        deployBuffer.append("<br/>部署主机: ").append(hostIp).append(", 用户名: ").append(userName)
                                .append(", 程序:").append(programCode)
                                .append(", 版本:").append(version)
                                .append(", 路径:").append(remoteDeployPath);

                        logger.info("部署主机，主机IP:{}, 用户名:{}, 部署路径:{}, 部署程序:{}, 部署版本:{}", hostIp, userName, remoteDeployPath, programCode, version);
                        Long uploadStartTimes = System.currentTimeMillis();
                        remoteFtpClient = FTPUtils.getFtpInstance(hostIp, userName, passwd, ftpDto.getFtpType(),ftpDto.getTimeout());
                        FTPUtils.tryLogin(remoteFtpClient);

                        //判断是否部署过
                        Map<String, Object> deployMap = new HashMap<>();
                        deployMap.put("VERSION", version);
                        deployMap.put("NODE_ID", nodeId);
                        deployMap.put("NODE_TYPE_ID", nodeTypeId);
                        Map<String, Object> nodeVersionDeployMap = coreService.queryForObject2New("nodeClusterDeployMapper.queryNodeDeployList", deployMap, dbKey);
                        List<Map<String,Object>> nodeDeployList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeOnNodeDeployList", deployMap, dbKey);

                        //判断该版本的节点是否处于运行状态
                        String runState = StringTool.object2String(nodeVersionDeployMap.get("STATE"));
                        if(NodeConstant.RUNNING.equals(runState)){

                            throw new RuntimeException("<br/>程序正在运行中...不能再部署");
                        }

                        nodeFirstDeploy = CollectionUtils.isEmpty(nodeDeployList);
                        nodeVersionFirstDeploy = MapUtils.isEmpty(nodeVersionDeployMap);

                        //将压缩的版本包，移动到“节点路径”下
                        remoteFtpClient.put(inputStream, remoteDeployPath);

                        Long uploadEndTimes = System.currentTimeMillis();
                        deployBuffer.append("<br/>程序包上传完成，耗时:[ ").append((uploadEndTimes - uploadStartTimes) / 1000).append(" ]秒");
                        logger.info("部署主机成功，主机IP:{}", hostIp);

                        //远程主机执行解压缩命令
                        shellUtils = new ShellUtils(hostIp, userName, passwd);

                        //是否采用历史版本
                        boolean useHistoryCfg = StringTool.object2String(params.get("USE_HISTORY_CFG")).equals("true");

                        //是否解压配置文件
                        boolean decompCfg = true;

                        Map<String,Object> remoteNodeInfo = coreService.queryForObject2New("nodeClusterDeployMapper.queryDeployNodeOnNodeTypeAndVersion",deployMap,dbKey);
                        boolean nodeTypeVersionFirstDeploy = MapUtils.isEmpty(remoteNodeInfo);

                        //采用历史版本 && （版本的再部署 || 新程序的部署 && 该类型该版本的非第一次部署）
                        if(useHistoryCfg && (!nodeVersionFirstDeploy || nodeFirstDeploy && !nodeTypeVersionFirstDeploy)){
                            decompCfg = false;
                            logger.info("版本的再部署或程序第一次部署，且采用历史配置，所以不解压配置文件");
                            deployBuffer.append("<br/>版本的再部署或程序第一次部署，且采用历史配置，所以不解压配置文件");
                        }
                        String extractCmd = this.getExtractCommand(fileName, remoteDeployFilePath,decompCfg);
                        logger.info("部署成功，解压缩程序，命令:{}", extractCmd);
                        deployBuffer.append("<br/>执行解压缩命令，命令: ").append(extractCmd);
                        String resultMsg = shellUtils.execMsg(extractCmd);
                        logger.debug("执行解压缩命名，返回结果:{}", resultMsg);

                        if (resultMsg.toLowerCase().indexOf(Constant.ERROR) > -1 || resultMsg.toLowerCase().indexOf(Constant.FAILED) > -1) {

                            Map<String,String> pathInfo = new HashMap<>();
                            pathInfo.put("nodePath",nodePath);
                            pathInfo.put("version",version);
                            pathInfo.put("compPath",remoteDeployFilePath);
                            pathInfo.put("fileName",fileName);
                            extraFailHandle(shellUtils,pathInfo,resultMsg,nodeFirstDeploy,nodeVersionFirstDeploy,deployBuffer,resultMap);
                            return resultMap;
                        } else {
                            deployBuffer.append("<br/>解压缩命令成功");

                            //用历史版本配置文件来覆盖“本版本配置”，即采用“历史配置文件”
                            if(useHistoryCfg && !nodeFirstDeploy && nodeVersionFirstDeploy){
                                Map<String, String> mergeInfo = new HashMap<>();
                                mergeInfo.put("NODE_PATH", nodePath);
                                mergeInfo.put("VERSION", version);
                                String latestVersionPath = getLinuxAbsPath(ftpDto.getFtpRootPath(),deploy_dir,programCode,"${latestVersion}","${latestVersionFileName}");
                                mergeInfo.put("latestVersionPath", latestVersionPath);
                                mergeHistoryConfig(ftpClient,remoteFtpClient,mergeInfo,nodeDeployList,shellUtils,deployBuffer);

                            }else if (useHistoryCfg && nodeFirstDeploy && !nodeTypeVersionFirstDeploy) {
                                Map<String,Object> pathInfo = new HashMap<>();
                                pathInfo.put("nodePath",nodePath);
                                pathInfo.put("version",version);
                                mergeOtherNodeCfg(NodeConstant.NOT_RUN_WEB,remoteFtpClient,shellUtils,ftpDto,remoteNodeInfo,pathInfo,deployBuffer);
                            }

                            deployBuffer.append("<br/> 同步数据库开始...");
                            //添加部署主机信息表
                            if (MapUtils.isNotEmpty(nodeVersionDeployMap)) {
                                //修改节点部署记录
                                deployMap.put("UPDATE_USER", params.get("EMPEE_NAME"));
                                coreService.updateObject2New("nodeClusterDeployMapper.updateNodeDeployList", deployMap, dbKey);
                                deployBuffer.append("<br/>程序版本部署信息更新成功!");
                            } else {
                                //添加节点部署记录
                                deployMap.put("CREATED_USER", params.get("EMPEE_NAME"));
                                coreService.insertObject2New("nodeClusterDeployMapper.addNodeDeployList", deployMap, dbKey);
                                deployBuffer.append("<br/>程序版本部署信息新增成功!");
                            }
                        }
                        long endTimes = System.currentTimeMillis();
                        long costTimes = (endTimes - startTimes) / 1000;
                        logger.debug("节点部署结束，部署主机:{}, 本次部署总耗时:{}秒", hostIp, costTimes);
                        deployBuffer.append("<br/>节点部署完成，耗时: ").append(costTimes).append("秒");
                        deployBuffer.append("<br/>部署主机:").append(hostIp);
                        deployBuffer.append(BusinessConstant.DEPLOY_SUCCESS);
                    } else {
                        deployBuffer.append("<br/>节点主机不存在，请检查!");
                        deployBuffer.append(BusinessConstant.DEPLOY_FAIL);
                        resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
                        resultMap.put("retMsg", deployBuffer.toString());
                        return resultMap;
                    }
                }
            }
        }catch (Exception e){
            logger.debug("部署发生异常！");
            e.printStackTrace();
            deployBuffer.append("<br/>部署发生异常！异常信息：<font color='red'>"+e.getMessage()+"</font>");

            //如果为新版本的第一次部署，且节点目录已经生成，则删除节点目录
            if(nodeVersionFirstDeploy && remoteFtpClient!=null){
                exceptionDelNodePath(remoteFtpClient,nodePath,version,shellUtils,nodeFirstDeploy,deployBuffer);
            }

            deployBuffer.append(BusinessConstant.DEPLOY_FAIL);
            resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
            resultMap.put("retMsg", deployBuffer.toString());
            return resultMap;

        }finally {
            if(ftpClient != null) {
                ftpClient.close();
            }
            if (remoteFtpClient != null) {
                remoteFtpClient.close();
            }
        }

        resultMap.put("retMsg", deployBuffer.toString());
        logger.debug("节点部署结束，返回结果:{}", resultMap);
        return resultMap;
    }
    /**
     * web类型节点部署
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> startWebNodeDeploy(Map<String, Object> params, String dbKey) throws Exception {

        logger.debug("web类型节点部署，业务参数:{}, dbKey:{}", params, dbKey);
        Map<String, Object> resultMap = new HashMap<String, Object>(){{
            put("retCode", BusinessConstant.PARAMS_BUS_1);
        }};

        //程序编码
        String programCode = ObjectUtils.toString(params.get("NODE_TYPE_CODE"));
        //版本信息
        String version = ObjectUtils.toString(params.get("VERSION_NAME"));
        //节点程序ID
        String nodeTypeId = ObjectUtils.toString(params.get("NODE_TYPE_ID"));
        //版本包上传包名称
        String fileName = ObjectUtils.toString(params.get("FILE_NAME"));
        //web容器模板名称
        String webTemplatesName = ObjectUtils.toString(params.get("WEB_TEMPLATES"));

        StringBuffer deployBuffer = new StringBuffer();
        Trans ftpClient = null;
        Trans remoteFtpClient = null;
        boolean nodeVersionFirstDeploy = false;
        boolean nodeFirstDeploy = false;
        String nodePath = null;
        ShellUtils shellUtils = null;
        try {
            //部署节点列表
            List<Map<String, Object>> nodeList = (List<Map<String, Object>>) params.get("NODE_LIST");

            deployBuffer.append("当前部署的节点名称："+StringTool.object2String(nodeList.get(0).get("NODE_NAME"))+"，");
            deployBuffer.append("节点所在主机："+StringTool.object2String(nodeList.get(0).get("HOST_TEXT"))+"，");
            deployBuffer.append("节点路径："+StringTool.object2String(nodeList.get(0).get("NODE_PATH")));

            //存储nodeID，为页面显示成功失败做准备
            resultMap.put("nodeId", StringTool.object2String(nodeList.get(0).get("NODE_ID")));
            resultMap.put("hostText", StringTool.object2String(nodeList.get(0).get("HOST_TEXT")));
            resultMap.put("nodePath", StringTool.object2String(nodeList.get(0).get("NODE_PATH")));

            String startCmd = StringTool.object2String(nodeList.get(0).get("START_CMD"));

            List<Map<String, Object>> versionList = null;
            String contextCfg = null;
            //根据程序类型，版本号查询“程序版本包”的名称
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("VERSION", version);
            queryMap.put("NODE_TYPE_ID", nodeTypeId);
            versionList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeTypeVersionList", queryMap, dbKey);

            //前端没有传递版本包名称时，从查询结果中获得
            if (StringUtils.isBlank(fileName)) {
                if (CollectionUtils.isNotEmpty(versionList)) {
                    fileName = ObjectUtils.toString(versionList.get(0).get("FILE_NAME"));
                } else {
                    resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
                    resultMap.put("retMsg", "获取程序包失败，请检查!"+BusinessConstant.DEPLOY_FAIL);
                    return resultMap;
                }
            }

            contextCfg = ObjectUtils.toString(versionList.get(0).get("CONTEXT_CFG"));

            if (!NodeVerUtil.fileNameRemoveSuffix(fileName).equals(contextCfg)) {
                throw new RuntimeException("要部署的版本包名与约定的包名不一致，请检查!");
            }

            if (CollectionUtils.isNotEmpty(nodeList)) {
                FtpDto ftpDto = SessionUtil.getFtpParams();
                ftpDto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
                ftpClient = FTPUtils.getFtpInstance(ftpDto);

                //遍历进行版本发布的所有节点
                for (Map<String, Object> nodeMap : nodeList) {

                    long startTimes = System.currentTimeMillis();
                    String nodeId = ObjectUtils.toString(nodeMap.get("NODE_ID"));
                    nodePath=ObjectUtils.toString(nodeMap.get("NODE_PATH"));

                    //目标主机上，web容器的webapps目录
                    String temp_dir = NodeVerUtil.getTomcatTempPath();
                    String remoteDeployWebAppsPath = getRemoteWebAppsPath(nodePath,version,temp_dir);

                    //版本包的存储路径：webapps路径 + 压缩包名
                    String remoteDeployPath = remoteDeployWebAppsPath + fileName;

                    //连接版本发布服务器，并获得“程序版本包”的流
                    InputStream releaseInputStream = null;
                    String releaseFileName = contextCfg +".zip";
                    String releasePath = NodeVerUtil.getRemoteReleaseFullPkgStorePath(ftpDto.getFtpRootPath(),programCode,version) + releaseFileName;

                    FTPUtils.tryLogin(ftpClient);
                    if (ftpClient.isExistPath(releasePath)) {
                        releaseInputStream = ftpClient.get(releasePath);
                        fileName = releaseFileName;
                        remoteDeployPath = remoteDeployWebAppsPath + releaseFileName;
                    } else {
                        logger.debug("发布服务器上的release目录下对应文件不存在，文件的绝对路径：{}", releasePath);
                        throw new RuntimeException("发布服务器上的release目录下对应文件不存在");
                    }

                    logger.info("部署版本发布服务器，主机IP:{},用户名:{}，路径:{}", ftpDto.getHostIp(), ftpDto.getUserName(), releasePath);
                    deployBuffer.append("<br/>开始连接部署版本发布服务器，准备获取版本包文件...");
                    deployBuffer.append("<br/>部署版本发布服务器连接成功...");
                    logger.info("部署版本发布服务器连接成功...");
                    deployBuffer.append("<br/>部署开始时间: ").append(DateUtil.getCurrent(DateUtil.allPattern));
                    deployBuffer.append("<br/>版本发布服务器连接就绪...");

                    //查询程序节点的目标主机信息
                    List<Map<String, Object>> nodeHostList = getHostInfoByNodes(nodeId,StringTool.object2String(nodeMap.get("NODE_HOST_ID")),dbKey);

                    //获取主机信息之后
                    if (CollectionUtils.isNotEmpty(nodeHostList)) {

                        String hostIp = ObjectUtils.toString(nodeHostList.get(0).get("HOST_IP"));
                        String userName = ObjectUtils.toString(nodeHostList.get(0).get("SSH_USER"));
                        String passwd = ObjectUtils.toString(nodeHostList.get(0).get("SSH_PASSWD"));
                        passwd = DesTool.dec(passwd);
                        deployBuffer.append("<br/>部署主机: ").append(hostIp).append(", 用户名: ").append(userName)
                                .append(", 程序:").append(programCode)
                                .append(", 版本:").append(version)
                                .append(", 路径:").append(remoteDeployPath);

                        logger.info("部署主机，主机IP:{}, 用户名:{}, 部署路径:{}, 部署程序:{}, 部署版本:{}", hostIp, userName, remoteDeployPath, programCode, version);
                        Long uploadStartTimes = System.currentTimeMillis();
                        remoteFtpClient = FTPUtils.getFtpInstance(hostIp, userName, passwd, ftpDto.getFtpType(),ftpDto.getTimeout());
                        FTPUtils.tryLogin(remoteFtpClient);

                        Map<String, Object> deployMap = new HashMap<>();
                        deployMap.put("VERSION", version);
                        deployMap.put("NODE_ID", nodeId);
                        deployMap.put("NODE_TYPE_ID", nodeTypeId);

                        //判断节点是否部署过
                        List<Map<String,Object>> nodeDeployList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeOnNodeDeployList", deployMap, dbKey);

                        //判断版本是否部署过
                        Map<String,Object> nodeVersionDeployMap = coreService.queryForObject2New("nodeClusterDeployMapper.queryNodeDeployList",deployMap,dbKey);

                        //判断该版本的节点是否处于运行状态
                        String runState = StringTool.object2String(nodeVersionDeployMap.get("STATE"));

                        if(NodeConstant.RUNNING.equals(runState)){

                            throw new RuntimeException("<br/>程序正在运行中...不能再部署");
                        }

                        nodeFirstDeploy = CollectionUtils.isEmpty(nodeDeployList);
                        nodeVersionFirstDeploy = MapUtils.isEmpty(nodeVersionDeployMap);

                        shellUtils = new ShellUtils(hostIp, userName, passwd);
                        String resultMsg=null;
                        //web容器压缩包的上传、解压
                        if (nodeVersionFirstDeploy) {

                            //上传
                            webTempUpload(ftpClient,remoteFtpClient,ftpDto.getFtpRootPath(),webTemplatesName,nodePath,version);
                            Long webUploadEndTimes = System.currentTimeMillis();
                            deployBuffer.append("<br/>web容器模板上传完成，耗时:[ ").append((webUploadEndTimes - uploadStartTimes) / 1000).append(" ]秒");

                            //解压
                            String command = getWebTempExtractCommand(webTemplatesName,FileTool.exactPath(nodePath) + version);
                            resultMsg = shellUtils.execMsg(command);

                            //解压结果的处理
                            logger.debug("对上传的web容器压缩包进行解压，执行命令：{}，执行结果：{}",command,resultMsg);
                            if (resultMsg.toLowerCase().indexOf(Constant.ERROR) > -1 || resultMsg.toLowerCase().indexOf(Constant.FAILED) > -1) {

                                extraWebTempFailHandle(shellUtils,resultMsg,nodePath,version,webTemplatesName,deployBuffer,resultMap);
                                return resultMap;
                            } else {
                                deployBuffer.append("<br/>Web容器解压缩命令执行成功");

                                //修改start.sh文件中的${context_cfg}
                                String replaceCommand = "cd *{version_path};sed -i 's/${context_cfg}/*{context_cfg}/1' *{start_shell_path}";
                                replaceCommand = replaceCommand.replace("*{version_path}", getLinuxAbsPath(nodePath, version))
                                                                .replace("*{context_cfg}", contextCfg)
                                                                .replace("*{start_shell_path}", startCmd);
                                String execResult = shellUtils.execMsg(replaceCommand);

                                logger.debug("替换web容器启动脚本中的start.sh文件中的${context_cfg}，命令：{}，执行结果：{}", replaceCommand, execResult);
                                deployBuffer.append("<br/>替换web容器启动脚本中的start.sh文件中的${context_cfg}，命令：" + replaceCommand);
                            }

                        } else {
                            deployBuffer.append("<br/>节点的版本路径中，web容器模板已存在，无需上传");
                        }

                        //版本包的上传
                        remoteFtpClient.put(releaseInputStream, remoteDeployPath);

                        Long uploadEndTimes = System.currentTimeMillis();
                        deployBuffer.append("<br/>程序包上传完成，耗时:[ ").append((uploadEndTimes - uploadStartTimes) / 1000).append(" ]秒");
                        logger.info("部署主机成功，主机IP:{}", hostIp);

                        //是否采用历史版本
                        boolean useHistoryCfg = StringTool.object2String(params.get("USE_HISTORY_CFG")).equals("true");

                        //是否解压配置文件
                        boolean decompCfg = true;

                        Map<String,Object> remoteNodeInfo = coreService.queryForObject2New("nodeClusterDeployMapper.queryDeployNodeOnNodeTypeAndVersion",deployMap,dbKey);
                        boolean nodeTypeVersionFirstDeploy = MapUtils.isEmpty(remoteNodeInfo);
                        //采用历史版本 && （版本的再部署 || 新程序的部署 && 该类型该版本的非第一次部署）
                        if(useHistoryCfg && (!nodeVersionFirstDeploy || nodeFirstDeploy && !nodeTypeVersionFirstDeploy)){
                                                        //版本的再部署：
                                                        //      采用历史版本：不解压配置文件
                                                        //      不采用历史版本：解压配置文件
                            decompCfg = false;
                            logger.info("版本的再部署或程序第一次部署，且采用历史配置，所以不解压配置文件");
                            deployBuffer.append("<br/>版本的再部署或程序第一次部署，且采用历史配置，所以不解压配置文件");
                        }

                        //解压程序版本包
                        String extractCmd = this.getWebExtractCommand(fileName, remoteDeployWebAppsPath,decompCfg);
                        logger.info("部署成功，解版本包压缩程序，命令:{}", extractCmd);
                        deployBuffer.append("<br/>执行版本包解压缩命令，命令: ").append(extractCmd);

                        resultMsg = shellUtils.execMsg(extractCmd);
                        logger.debug("执行解压缩命名，返回结果:{}", resultMsg);

                        //解压失败的处理
                        if (resultMsg.toLowerCase().indexOf(Constant.ERROR) > -1 || resultMsg.toLowerCase().indexOf(Constant.FAILED) > -1) {
                            Map<String, String> pathInfo = new HashMap<>();
                            pathInfo.put("nodePath", nodePath);
                            pathInfo.put("version", version);
                            pathInfo.put("compPath", remoteDeployWebAppsPath);
                            pathInfo.put("fileName", fileName);
                            extraFailHandle(shellUtils, pathInfo, resultMsg, nodeFirstDeploy, nodeVersionFirstDeploy, deployBuffer, resultMap);
                            return resultMap;
                        } else {
                            deployBuffer.append("<br/>版本包解压缩命令成功");

                            //采用历史配置、有历史版本、新版本的第一次部署：进行最新版本的配置合并
                            if(useHistoryCfg && !nodeFirstDeploy && nodeVersionFirstDeploy) {
                                Map<String, Object> mergeInfo = new HashMap<>();
                                mergeInfo.put("NODE_PATH", nodePath);
                                mergeInfo.put("VERSION", version);
                                mergeInfo.put("temp_dir", temp_dir);
                                mergeInfo.put("contextCfg", contextCfg);
                                mergeInfo.put("webAppsPath", remoteDeployWebAppsPath);
                                //最新版本的版本包的下载路径
                                String historyVersionRelPath = NodeVerUtil.getRemoteReleaseFullPkgStorePath(ftpDto.getFtpRootPath(),programCode,"${latestVersion}") + releaseFileName;
                                mergeInfo.put("historyVersionRelPath",historyVersionRelPath);

                                webMergeHistoryConfig(ftpClient,remoteFtpClient,mergeInfo, nodeDeployList, shellUtils, deployBuffer);
                            } else if (useHistoryCfg && nodeFirstDeploy && !nodeTypeVersionFirstDeploy) {

                                Map<String,Object> pathInfo = new HashMap<>();
                                pathInfo.put("nodePath",nodePath);
                                pathInfo.put("version",version);
                                pathInfo.put("temp_dir",temp_dir);
                                pathInfo.put("contextCfg",contextCfg);
                                mergeOtherNodeCfg(NodeConstant.RUN_WEB,remoteFtpClient,shellUtils,ftpDto,remoteNodeInfo,pathInfo,deployBuffer);
                            }
                                deployBuffer.append("<br/>同步数据库开始...");
                            //添加部署主机信息表
                            toUpdateDeployTable(nodeVersionFirstDeploy,params,deployMap,deployBuffer,dbKey);

                        }
                        long endTimes = System.currentTimeMillis();
                        long costTimes = (endTimes - startTimes) / 1000;
                        logger.debug("节点部署结束，部署主机:{}, 本次部署总耗时:{}秒", hostIp, costTimes);
                        deployBuffer.append("<br/>节点部署完成，耗时: ").append(costTimes).append("秒");
                        deployBuffer.append("<br/>部署主机:").append(hostIp);
                        deployBuffer.append(BusinessConstant.DEPLOY_SUCCESS);
                    } else {
                        deployBuffer.append("<br/>节点主机不存在，请检查!");
                        deployBuffer.append(BusinessConstant.DEPLOY_FAIL);
                        resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
                        resultMap.put("retMsg", deployBuffer.toString());
                        return resultMap;
                    }
                }
            }
        }catch (Exception e){
            logger.debug("部署发生异常！");
            e.printStackTrace();
            deployBuffer.append("<br/>部署发生异常！异常信息：<font color='red'>"+e.getMessage()+"</font>");

            //如果为新版本的第一次部署，且节点目录已经生成，则删除节点目录
            if(nodeVersionFirstDeploy && remoteFtpClient!=null){
                    exceptionDelNodePath(remoteFtpClient,nodePath,version,shellUtils,nodeFirstDeploy,deployBuffer);
            }

            deployBuffer.append(BusinessConstant.DEPLOY_FAIL);
            resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
            resultMap.put("retMsg", deployBuffer.toString());
            return resultMap;
        }finally {
            if(ftpClient != null) {
                ftpClient.close();
            }
            if (remoteFtpClient != null) {
                remoteFtpClient.close();
            }
        }
        resultMap.put("retMsg", deployBuffer.toString());
        logger.debug("节点部署结束，返回结果:{}", resultMap);
        return resultMap;
    }

    /**
     * web程序、非web程序：合并其它节点的配置文件
     * @param shellUtils
     * @param ftpDto
     * @param remoteNodeInfo
     * @param deployBuffer
     * @throws Exception
     */
    private void mergeOtherNodeCfg(String runWeb,Trans ftpClient,ShellUtils shellUtils,FtpDto ftpDto,Map<String,Object> remoteNodeInfo,Map<String,Object> pathInfo,StringBuffer deployBuffer) throws Exception{
        boolean remoteCfgZipExists = false;
        String remoteCfgZipPath = null;
        ShellUtils remoteShellUtils = null;
        try {
            //获得远程主机的登录信息
            String nodeHostIp = StringTool.object2String(remoteNodeInfo.get("HOST_IP"));
            String nodeSshUser = StringTool.object2String(remoteNodeInfo.get("SSH_USER"));
            String nodePwd = StringTool.object2String(remoteNodeInfo.get("SSH_PASSWD"));
            String remoteNodePath = StringTool.object2String(remoteNodeInfo.get("NODE_PATH"));
            String config_dir = NodeVerUtil.getCfgPathSufx();
            nodePwd = DesTool.dec(nodePwd);

            //配置文件的路径
            String cfgZipFile = null;
            String remoteCfgParentPath = null;
            remoteCfgZipPath = null;
            String nodeCfgParentPath = null;
            String nodeCfgZipPath = null;
            String nodePath = StringTool.object2String(pathInfo.get("nodePath"));
            String version = StringTool.object2String(pathInfo.get("version"));
            String zipCmd = null;
            String tmpFlag = new Random().nextInt(9000)+1000+"_tmp";

            if(NodeConstant.NOT_RUN_WEB.equals(runWeb)) {
                cfgZipFile = config_dir +"_bin_"+ tmpFlag +".zip";

                remoteCfgParentPath = getLinuxAbsPath(remoteNodePath, version);
                remoteCfgZipPath = getLinuxAbsPath(remoteCfgParentPath, cfgZipFile);
                nodeCfgParentPath = getLinuxAbsPath(nodePath, version);
                nodeCfgZipPath = getLinuxAbsPath(nodeCfgParentPath, cfgZipFile);

                zipCmd = "cd ${remoteCfgParentPath};zip -qr ${cfgZipFile} ${cfgDir} bin;";
            }else{
                cfgZipFile = config_dir+ "_" + tmpFlag + ".zip";
                String temp_dir = StringTool.object2String(pathInfo.get("temp_dir"));
                String contextCfg = StringTool.object2String(pathInfo.get("contextCfg"));

                remoteCfgParentPath = getLinuxAbsPath(remoteNodePath, version,temp_dir,"webapps",contextCfg);
                remoteCfgZipPath = getLinuxAbsPath(remoteCfgParentPath, cfgZipFile);
                nodeCfgParentPath = getLinuxAbsPath(nodePath, version,temp_dir,"webapps",contextCfg);
                nodeCfgZipPath = getLinuxAbsPath(nodeCfgParentPath, cfgZipFile);

                zipCmd = "cd ${remoteCfgParentPath};zip -qr ${cfgZipFile} ${cfgDir};";
            }

            logger.debug("合并该版本已部署节点的配置文件开始...");
            logger.debug("远程节点信息，主机IP：{}，用户名：{}，节点目录：{}", nodeHostIp, nodeSshUser, remoteNodePath);
            deployBuffer.append("<br/>合并该版本已部署节点的配置文件开始...");
            deployBuffer.append("<br/>节点信息，主机ip：" + nodeHostIp)
                    .append("，用户名：" + nodeSshUser)
                    .append("，配置文件目录：" + remoteNodePath);

            //登录ssh，压缩其配置文件
            remoteShellUtils = new ShellUtils(nodeHostIp, nodeSshUser, nodePwd);
            zipCmd = zipCmd.replace("${remoteCfgParentPath}", remoteCfgParentPath)
                            .replace("${cfgZipFile}", cfgZipFile)
                            .replace("${cfgDir}", config_dir);
            String execMsg = remoteShellUtils.execMsg(zipCmd);
            logger.debug("压缩已部署节点的配置文件，压缩命令：{}，执行结果：{}",zipCmd,execMsg);
            deployBuffer.append("<br/>压缩已部署节点的配置文件，压缩命令：" + zipCmd);
            remoteCfgZipExists = true;

            //配置压缩文件的传输
            Trans remoteNodeFtp = FTPUtils.getFtpInstance(nodeHostIp, nodeSshUser, nodePwd, ftpDto.getFtpType(), ftpDto.getTimeout());
            FTPUtils.tryLogin(remoteNodeFtp);
            logger.debug("连接已部署节点所在主机成功");
            deployBuffer.append("<br/>连接已部署节点所在主机成功");

            InputStream remoteNodeCfgStream = remoteNodeFtp.get(remoteCfgZipPath);
            ftpClient.put(remoteNodeCfgStream, nodeCfgZipPath);
            logger.debug("配置文件已下载到节点目录下，配置文件路径：{}", nodeCfgZipPath);
            deployBuffer.append("<br/>配置文件已下载到节点目录下，配置文件路径：" + nodeCfgZipPath);

            //删除远程主机上的配置压缩文件
            String delCfgZipCmd = "rm -f ${cfgZipPath};".replace("${cfgZipPath}", remoteCfgZipPath);
            execMsg = remoteShellUtils.execMsg(delCfgZipCmd);
            logger.debug("删除已部署节点的压缩配置文件，删除命令：{}，执行结果：{}",delCfgZipCmd,execMsg);
            deployBuffer.append("<br/>删除已部署节点的压缩配置文件，删除命令：" + delCfgZipCmd);
            remoteCfgZipExists = false;

            //解压压缩配置文件
            String unzipCfgCmd = "cd ${cfgParentPath};unzip -qo ${cfgZipFile}"
                                            .replace("${cfgParentPath}",nodeCfgParentPath)
                                            .replace("${cfgZipFile}", cfgZipFile);
            execMsg = shellUtils.execMsg(unzipCfgCmd);
            logger.debug("解压配置文件，解压命令：{}，执行结果：{}", unzipCfgCmd,execMsg);
            deployBuffer.append("<br/>解压配置文件，解压命令：" + unzipCfgCmd);

            //删除压缩文件
            String delZipCfgCmd = "rm -f ${nodeCfgPath}".replace("${nodeCfgPath}", nodeCfgZipPath);
            execMsg = shellUtils.execMsg(delZipCfgCmd);
            logger.debug("删除压缩文件，删除命令：{}，执行结果：{}",delZipCfgCmd,execMsg);
            deployBuffer.append("<br/>删除压缩文件，删除命令：" + delZipCfgCmd);

        } catch (Exception e) {
            if(remoteCfgZipExists) {
                String cmd = "rm ${cfgZipPath}".replace("${cfgZipPath}", remoteCfgZipPath);
                String result = shellUtils.execMsg(cmd);
                logger.debug("部署失败，且远程配置文件的压缩文件存在，则删除，命令：{}，结果：{}",cmd,result);
                deployBuffer.append("<br/>部署失败，删除远程节点配置文件的压缩文件");
            }

            throw e;
        }
    }

    /**
     * web程序：web容器解压失败时的处理
     * @param shellUtils
     * @param resultMsg
     * @param nodePath
     * @param version
     * @param tempName
     * @param deployBuffer
     * @param resultMap
     */
    private void extraWebTempFailHandle(ShellUtils shellUtils,String resultMsg,String nodePath,String version,String tempName,StringBuffer deployBuffer,Map<String,Object> resultMap){
        logger.error("解压缩命令执行失败，详细信息:{}", resultMsg);
        deployBuffer.append("<br/>解压缩命令执行失败，失败信息: ").append(resultMsg);
        deployBuffer.append("<br/>解压缩命令执行失败，请检查(是否安装ZIP命令等)!");
        //压缩包的删除
        String rfCommand = "rm -rf " +FileTool.exactPath(nodePath)+ FileTool.exactPath(version)+tempName;
        String execRes = shellUtils.execMsg(rfCommand);

        logger.debug("删除web模板压缩包，删除命令为：{}，删除结果为：{}", rfCommand, execRes);

        deployBuffer.append(BusinessConstant.DEPLOY_FAIL);
        resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
        resultMap.put("retMsg", deployBuffer.toString());
    }

    /**
     * 非web程序和web程序：程序版本包解压失败的处理
     * @param shellUtils
     * @param pathInfo
     * @param resultMsg
     * @param nodeVersionFirstDeploy
     * @param deployBuffer
     * @return
     * @throws Exception
     */
    private void extraFailHandle(ShellUtils shellUtils,Map<String,String> pathInfo,String resultMsg,boolean nodeFirstDeploy,boolean nodeVersionFirstDeploy,StringBuffer deployBuffer,Map<String,Object> resultMap) throws Exception{

        String nodePath = pathInfo.get("nodePath");
        String version = pathInfo.get("version");
        String compPath = pathInfo.get("compPath");
        String fileName = pathInfo.get("fileName");
        logger.error("解压缩命令执行失败，详细信息:{}", resultMsg);
        deployBuffer.append("<br/>解压缩命令执行失败，失败信息: ").append(resultMsg);
        deployBuffer.append("<br/>解压缩命令执行失败，请检查(是否安装ZIP命令等)!");

        String command = null;
        String execRes = null;
        if(nodeFirstDeploy){
            command = "rm -rf ${node_path}";
            command = command.replace("${version_path}", nodePath);
        }else if(!nodeFirstDeploy && nodeVersionFirstDeploy){   //新版本第一次部署时，部署失败，则删除节点目录
            command = "rm -rf ${version_path}";
            command = command.replace("${version_path}", getLinuxAbsPath(nodePath,version));
        } else {
            //压缩包的删除
            command = "rm -rf ${file_name}";
            command = command.replace("${file_name}", FileTool.exactPath(compPath) + fileName);
        }
        execRes = shellUtils.execMsg(command);
        logger.debug("删除节点压缩包   ，删除命令为：{}，删除结果为：{}", command, execRes);
        deployBuffer.append("<br/>删除压缩包及其相关目录");
        deployBuffer.append(BusinessConstant.DEPLOY_FAIL);
        resultMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
        resultMap.put("retMsg", deployBuffer.toString());

    }

    /**
     *  非web程序：合并历史配置文件
     * @param ftpClient
     * @param remoteFtpClient
     * @param mergeInfo
     * @param nodeDeployList
     * @param shellUtils
     * @param deployBuffer
     * @throws Exception
     */
    private void mergeHistoryConfig(Trans ftpClient,Trans remoteFtpClient,Map<String,String> mergeInfo,List<Map<String,Object>> nodeDeployList,ShellUtils shellUtils,StringBuffer deployBuffer) throws Exception{
        NodeVerUtil.sortDeployVersion(nodeDeployList);
        String latestVersion = StringTool.object2String(nodeDeployList.get(0).get("VERSION"));
        String latestVersionFileName = StringTool.object2String(nodeDeployList.get(0).get("FILE_NAME"));
        String version = mergeInfo.get("VERSION");
        String nodePath = mergeInfo.get("NODE_PATH");

        if(!latestVersion.equals(version)) {    //最新版本的再次部署时，无需复制

            //上传最新版本
            String latestVersionPath = mergeInfo.get("latestVersionPath")
                    .replace("${latestVersion}",latestVersion)
                    .replace("${latestVersionFileName}",latestVersionFileName);
            InputStream latestVersionPkg = ftpClient.get(latestVersionPath);

            String tmpVersionName = latestVersion+"_"+(new Random().nextInt(9000)+1000)+"_tmp";
                String tmpFilePath = getLinuxAbsPath(nodePath,tmpVersionName,latestVersionFileName);
            remoteFtpClient.put(latestVersionPkg,tmpFilePath);

            deployBuffer.append("<br/>上传临时的最新版本包到节点目录下");
            logger.debug("上传临时的最新版本包到节点目录下，版本发布服务器上的目录为：{}，目标目录为：{}",latestVersionPath,tmpFilePath);

            //解压最新版本
            String extraCommand = getExtractCommand(latestVersionFileName,getLinuxAbsPath(nodePath,tmpVersionName),true);
            String execReuslt = shellUtils.execMsg(extraCommand);
            deployBuffer.append("<br/>解压最新版本包,解压命令："+extraCommand);
            logger.debug("解压临时的最新版本包到节点目录下，执行的命令：{}，执行结果：{}", extraCommand, execReuslt);

            //复制配置文件
            String cpCommand = "cp -rf ${latestVersion_config_path}/ ${version_path};";
            String config_dir = NodeVerUtil.getCfgPathSufx();
            cpCommand = cpCommand.replace("${latestVersion_config_path}", getLinuxAbsPath(nodePath, tmpVersionName, config_dir));
            cpCommand = cpCommand.replace("${version_path}", getLinuxAbsPath(nodePath, version));
            execReuslt = shellUtils.execMsg(cpCommand);
            logger.debug("进行历史版本配置文件的合并，执行的命令：{}，执行结果：{}", cpCommand, execReuslt);
            deployBuffer.append("<br/>进行历史版本配置文件的合并，执行的命令：" + cpCommand);

            //删除最新版本包
            String delCommand = "rm -rf ${latestVersionTmpPath};".replace("${latestVersionTmpPath}",getLinuxAbsPath(nodePath,tmpVersionName));
            execReuslt = shellUtils.execMsg(delCommand);
            deployBuffer.append("<br/>删除临时的最新版本包,删除命令："+delCommand);
            logger.debug("删除临时的最新版本包，执行的命令：{}，执行结果：{}", delCommand, execReuslt);

        }else{
            logger.debug("版本为最新版本，不进行历史配置文件的合并");
            deployBuffer.append("<br/>版本为最新版本，不进行历史配置文件的合并");
        }
    }

    /**
     * web程序：合并历史配置文件
     * @param ftpClient
     * @param remoteFtpClient
     * @param mergeInfo
     * @param nodeDeployList
     * @param shellUtils
     * @param deployBuffer
     * @throws Exception
     */
    private void webMergeHistoryConfig(Trans ftpClient,Trans remoteFtpClient,Map<String,Object> mergeInfo,List<Map<String,Object>> nodeDeployList,ShellUtils shellUtils,StringBuffer deployBuffer) throws Exception{
        String nodePath = StringTool.object2String(mergeInfo.get("NODE_PATH"));
        String version = StringTool.object2String(mergeInfo.get("VERSION"));
        String temp_dir = StringTool.object2String(mergeInfo.get("temp_dir"));
        String contextCfg = StringTool.object2String(mergeInfo.get("contextCfg"));
        String historyVersionRelPath = StringTool.object2String(mergeInfo.get("historyVersionRelPath"));
        String webAppsPath = StringTool.object2String(mergeInfo.get("webAppsPath"));
        NodeVerUtil.sortDeployVersion(nodeDeployList);
        String latestVersion = StringTool.object2String(nodeDeployList.get(0).get("VERSION"));

        if (!latestVersion.equals(version)) {    //最新版本的再次部署时，无需复制

            //上传最新的版本包到目标主机
            historyVersionRelPath = historyVersionRelPath.replace("${latestVersion}",latestVersion);
            InputStream historyVersionPkg = ftpClient.get(historyVersionRelPath);

            String tmpFileName = contextCfg+"_"+latestVersion+"_"+(new Random().nextInt(9000)+1000)+"_tmp";
            String tmpFilePkgName = tmpFileName + ".zip";
            String tmpFilePath = getLinuxAbsPath(webAppsPath,tmpFilePkgName);

            remoteFtpClient.put(historyVersionPkg, tmpFilePath);

            deployBuffer.append("<br/>上传临时的最新版本包到webapps目录下");
            logger.debug("上传临时的最新版本包到webapps目录下，release目录为：{}，目标目录为：{}",historyVersionRelPath,tmpFilePath);
            //解压
            String extraCommand ="cd ${webAppsPath};unzip -qo ${tmpFilePkgName} -d ${tmpFileName};rm -f ${tmpFilePkgName}";
            extraCommand = extraCommand.replace("${webAppsPath}",webAppsPath)
                                        .replaceAll("\\$\\{tmpFilePkgName\\}",tmpFilePkgName)
                                        .replace("${tmpFileName}",tmpFileName);
            String execReuslt = shellUtils.execMsg(extraCommand);
            deployBuffer.append("<br/>解压临时的最新版本包,解压命令："+extraCommand);
            logger.debug("解压临时的最新版本包到webapps目录下，执行的命令：{}，执行结果：{}", extraCommand, execReuslt);

            //复制配置文件
            String cpCommand = "cp -rf ${latestVersionTmpPath_config}/ ${version_temp_webapps_node}";
            String config_dir = NodeVerUtil.getCfgPathSufx();
            cpCommand = cpCommand.replace("${latestVersionTmpPath_config}", getLinuxAbsPath(webAppsPath,tmpFileName,contextCfg, config_dir));
            cpCommand = cpCommand.replace("${version_temp_webapps_node}", getLinuxAbsPath(nodePath, version, temp_dir, "webapps", contextCfg));
            execReuslt = shellUtils.execMsg(cpCommand);
            logger.debug("进行历史版本配置文件的合并，执行的命令：{}，执行结果：{}", cpCommand, execReuslt);
            deployBuffer.append("<br/>进行历史版本配置文件的合并，执行的命令：" + cpCommand);

            //删除最新版本包
            String delCommand = "rm -rf ${latestVersionTmpPath}".replace("${latestVersionTmpPath}",getLinuxAbsPath(webAppsPath,tmpFileName));
            execReuslt = shellUtils.execMsg(delCommand);
            deployBuffer.append("<br/>删除临时的最新版本包,删除命令："+delCommand);
            logger.debug("删除临时的最新版本包，执行的命令：{}，执行结果：{}", delCommand, execReuslt);

        } else {
            logger.debug("版本为最新版本，不进行历史配置文件的合并");
            deployBuffer.append("<br/>版本为最新版本，不进行历史配置文件的合并");
        }

    }

    /**
     * 非web程序和web程序：部署过程有异常，删除节点目录
     * @param remoteFtpClient
     * @param nodePath
     * @param version
     * @param shellUtils
     * @param nodeFirstDeploy
     * @param deployBuffer
     * @throws Exception
     */
    private void exceptionDelNodePath(Trans remoteFtpClient,String nodePath,String version,ShellUtils shellUtils,boolean nodeFirstDeploy,StringBuffer deployBuffer) throws Exception{
        if(nodeFirstDeploy){
            if(remoteFtpClient.isExistPath(nodePath)){
                deployBuffer.append("<br/>程序的第一次部署，且部署失败，程序目录已生成，则删除程序目录...");
                String command = "rm -rf ${node_path}";
                command = command.replace("${node_path}", nodePath);
                String execRes = shellUtils.execMsg(command);
                logger.debug("程序的第一次部署，且部署失败，删除程序目录，命令：{}，执行结果：{}", command, execRes);
            }

        } else {
            String versionPath = getLinuxAbsPath(nodePath,version);
            if(remoteFtpClient.isExistPath(versionPath)) {
                deployBuffer.append("<br/>新版本的第一次部署，且部署失败，版本目录已生成，则删除版本目录...");
                String command = "rm -rf ${version_path}";
                command = command.replace("${version_path}", versionPath);
                String execRes = shellUtils.execMsg(command);
                logger.debug("新版本的第一次部署，且部署失败，删除版本目录，命令：{}，执行结果：{}", command, execRes);
            }
        }
    }

    /**
     * 把传递的参数从左到右拼接成一个路径
     * @param paths
     * @return
     */
    private String getLinuxAbsPath(String... paths){
        StringBuffer absolutePath = new StringBuffer();

        for(String path:paths){
            absolutePath.append(FileTool.exactPath(StringTool.object2String(path)));
        }
        return absolutePath.deleteCharAt(absolutePath.lastIndexOf("/")).toString();
    }

    /**
     *  web程序：获得web程序的部署路径
     * @param nodePath
     * @param version
     * @param temp_dir
     * @return
     */
    private String getRemoteWebAppsPath(String nodePath,String version,String temp_dir){

        return FileTool.exactPath(nodePath)
                + FileTool.exactPath(version)
                + FileTool.exactPath(temp_dir)
                + FileTool.exactPath("webapps");
    }

    /**
     * 获得节点的远程主机信息
     * @param nodeId
     * @param nodeTypeId
     * @param dbKey
     * @return
     */
    private List<Map<String, Object>> getHostInfoByNodes(String nodeId,String nodeTypeId,String dbKey){
        Map<String, Object> nodeQueryMap = new HashMap<>();
        nodeQueryMap.put("NODE_ID", nodeId);
        nodeQueryMap.put("NODE_HOST_ID", nodeTypeId);
        List<Map<String, Object>> nodeHostList = coreService.queryForList3New("nodeClusterDeployMapper.queryNodeHostList", nodeQueryMap, dbKey);

        return nodeHostList;
    }

    /**
     * web程序：web容器模板从版本发布服务器上传到远程主机
     * @param ftpClient
     * @param remoteFtpClient
     * @param ftpRootPath
     * @param webTempName
     * @param nodePath
     * @param version
     * @throws Exception
     */
    private void webTempUpload(Trans ftpClient,Trans remoteFtpClient,String ftpRootPath,String webTempName,String nodePath,String version) throws Exception{
        String web_dir = SystemProperty.getContextProperty("node.manager.tomcat_temp_dir");
        web_dir = BlankUtil.isBlank(web_dir) ? "templates" : web_dir;
        String webContainerPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(web_dir);
        InputStream webInputStream = ftpClient.get(webContainerPath + webTempName);
        //上传
        remoteFtpClient.put(webInputStream, FileTool.exactPath(nodePath)  + FileTool.exactPath(version) + webTempName);
    }


    /**
     * 部署后续操作：更新部署表的记录
     * @param nodeVersionFirstDeploy
     * @param params
     * @param deployMap
     * @param deployBuffer
     * @param dbKey
     */
    private void toUpdateDeployTable(boolean nodeVersionFirstDeploy,Map<String,Object> params,Map<String,Object> deployMap,StringBuffer deployBuffer,String dbKey){
        if (!nodeVersionFirstDeploy) {
            //修改节点部署记录
            deployMap.put("UPDATE_USER", params.get("EMPEE_NAME"));
            coreService.updateObject2New("nodeClusterDeployMapper.updateNodeDeployList", deployMap, dbKey);
            deployBuffer.append("<br/>程序版本部署信息更新成功!");
        } else {
            //添加节点部署记录
            deployMap.put("CREATED_USER", params.get("EMPEE_NAME"));
            coreService.insertObject2New("nodeClusterDeployMapper.addNodeDeployList", deployMap, dbKey);
            deployBuffer.append("<br/>程序版本部署信息新增成功!");
        }
    }

    /**
     * 非web程序：获取远程解压缩命令
     * @param fileName
     * @param remotePath
     * @return
     */
    private String getExtractCommand(String fileName, String remotePath,boolean decompCfg) {
        logger.debug("程序部署，获取解压缩命名，文件名称:{}, 远程目录:{}", fileName, remotePath);

        String configDir= NodeVerUtil.getCfgPathSufx();

        String extractCmd = "tar -xzf " + fileName;
        if (!decompCfg) {
            extractCmd = extractCmd + " --exclude ${config_dir} --exclude bin";
            extractCmd = extractCmd.replace("${config_dir}",configDir);
        }

        if (fileName.toUpperCase().endsWith(".TAR")) {

            extractCmd = "tar -xf " + fileName;
            if (!decompCfg) {
                extractCmd = extractCmd + " --exclude ${config_dir} --exclude bin";
                extractCmd = extractCmd.replace("${config_dir}",configDir);
            }
        } else if (fileName.toUpperCase().endsWith(".ZIP")) {

            extractCmd = "unzip -qo " + fileName;
            if (!decompCfg) {
                extractCmd = extractCmd + " -x ${config_dir}/* bin/*";
                extractCmd = extractCmd.replace("${config_dir}",configDir);

            }
        } else if (fileName.toUpperCase().endsWith(".WAR")) {

            extractCmd = "unzip -qo " + fileName+" -d "+ NodeVerUtil.fileNameRemoveSuffix(fileName);
            if (!decompCfg) {
                extractCmd = extractCmd + " -x ${config_dir}/* bin/*";
                extractCmd = extractCmd.replace("${config_dir}",configDir);
            }
        }

        return "cd " + remotePath + ";" + extractCmd+";"+"rm "+FileTool.exactPath(remotePath)+fileName+";chmod 755 bin/* "+configDir+"/*;";
    }

    /**
     * web程序：获取web容器解压命令
     * @param fileName
     * @param remotePath
     * @return
     */
    private String getWebTempExtractCommand(String fileName, String remotePath) {
        logger.debug("程序部署，获取web容器解压缩命名，文件名称:{}, 远程目录:{}", fileName, remotePath);

        String temp_dir = NodeVerUtil.getTomcatTempPath();
        String extractCmd = "mkdir "+temp_dir+";tar -xzf " + fileName;
        if (fileName.toUpperCase().endsWith(".TAR")) {
            extractCmd = "mkdir "+temp_dir+";tar -xf " + fileName;
        } else if (fileName.toUpperCase().endsWith(".ZIP")) {
            extractCmd = "unzip -qo " + fileName + " -d "+temp_dir;
        }

        return "cd " + remotePath + ";" + extractCmd+";rm "+FileTool.exactPath(remotePath)+fileName+";chmod -R 755 "+temp_dir+"/bin "+temp_dir+"/conf;";
    }

    /**
     * web程序：获取远程解压缩命令
     * @param fileName
     * @param remotePath
     * @return
     */
    private String getWebExtractCommand(String fileName, String remotePath,boolean decompCfg) {
        logger.debug("程序部署，获取web程序压缩包的解压缩命名，文件名称:{}, 远程目录:{}", fileName, remotePath);

        String configDir= NodeVerUtil.getCfgPathSufx();
        String rmSuffixFileName=NodeVerUtil.fileNameRemoveSuffix(fileName);

        //为zip时
        String extractCmd = "unzip -qo " + fileName;

        //不解压配置文件时
        if(!decompCfg){
            extractCmd = extractCmd + " -x ${config_dir}/*";
            extractCmd = extractCmd.replace("${config_dir}",rmSuffixFileName + "/" + configDir);
        }

        if (fileName.toUpperCase().endsWith(".TAR")) {

            extractCmd = "tar -xf " + fileName;

            if (!decompCfg) {
                extractCmd = extractCmd + " --exclude ${config_dir}";
                extractCmd = extractCmd.replace("${config_dir}",rmSuffixFileName + "/" + configDir);
            }

        } else if (fileName.toUpperCase().endsWith(".TAR.GZ")) {

            extractCmd = "tar -zxf " + fileName;

            if (!decompCfg) {
                extractCmd = extractCmd + " --exclude ${config_dir}";
                extractCmd = extractCmd.replace("${config_dir}",rmSuffixFileName + "/" + configDir);
            }

        } else if (fileName.toUpperCase().endsWith(".WAR")) {
            extractCmd = "unzip -qo " + fileName + " -d " + rmSuffixFileName;

            if (!decompCfg) {
                extractCmd = extractCmd + " -x "+configDir +"/*";
            }
        }

        //切换目录、解压文件到指定目录、更改bin、config权限、删除压缩文件
        return "cd " + remotePath + ";" + extractCmd+";rm "+FileTool.exactPath(remotePath)+fileName+";chmod -R 755 "+FileTool.exactPath(rmSuffixFileName)+configDir+";";
    }

}
