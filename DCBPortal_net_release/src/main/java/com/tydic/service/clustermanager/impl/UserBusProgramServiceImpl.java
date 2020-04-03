package com.tydic.service.clustermanager.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bean.RecursiveFile;
import com.tydic.bean.UserPrivilegeNode;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.common.BusException;
import com.tydic.service.clustermanager.UserBusProgramService;
import com.tydic.service.configure.RunDiffIPService;
import com.tydic.service.configure.RunSameIPService;
import com.tydic.service.configure.RunTopologyService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.Collator;
import java.util.*;

/**
 * Auther: Yuanh
 * Date: 2018-07-09 15:57
 * Description:
 */
@Service
public class UserBusProgramServiceImpl implements UserBusProgramService {

    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(UserBusProgramServiceImpl.class);

    /**
     * 核心Service对象
     */
    @Autowired
    private CoreService coreService;

    /**
     * 相同IP业务程序启停
     */
    @Autowired
    private RunSameIPService runSameIPService;

    /**
     * 不同IP业务程序启停
     */
    @Autowired
    private RunDiffIPService runDiffIPService;

    /**
     * 运行Topology程序启停
     */
    @Autowired
    private RunTopologyService runTopologyService;

    /**
     * 查询用户功能程序列表
     * @return
     */
    @Override
    public List<UserPrivilegeNode> queryUserProgramPrivilegeList(Map<String, Object> params, String dbKey) {
        logger.debug("查询用户功能列表信息， 业务参数：" + params.toString() + ", dbKey: " + dbKey);

        List<HashMap<String, Object>> busClusterList = null;

        //查询业务主集群
        String clusterSwitch = SystemProperty.getContextProperty("cluster.permission.switch");
        Map<String, Object> queryMap = new HashMap<String, Object>();
        /* queryMap.put("EMPEE_ID", params.get("EMPEE_ID"));*/
        queryMap.put("ROLE_ID", params.get("ROLE_ID"));
        if (StringUtils.equals(clusterSwitch.trim().toLowerCase(), "yes")) {
            busClusterList = coreService.queryForList2New("busMainCluster.queryRoleBusMainClusterList", queryMap, dbKey);
        } else {
            busClusterList = coreService.queryForList2New("busMainCluster.queryBusMainClusterListByState", null, dbKey);
        }
        logger.debug("业务主集群列表信息: " + busClusterList.toString());


        //用户权限分配
        List<UserPrivilegeNode> userPrivilegeNodes = new ArrayList<UserPrivilegeNode>();

        if (CollectionUtils.isNotEmpty(busClusterList)) {
            //查询所有的业务权限
            List<HashMap<String, Object>> userProgramList = null;
            if (StringUtils.equals(clusterSwitch.trim(), "yes")) {
                userProgramList = coreService.queryForList2New("userProgramListMapper.queryRoleUserProgramList", queryMap, dbKey);
            } else {
                userProgramList = coreService.queryForList2New("userProgramListMapper.queryAllUserProgramList", null, dbKey);
            }

            //业务主集群
            for (int x=0; x<busClusterList.size(); x++) {
                Map<String, Object> busClusterMap = busClusterList.get(x);
                String levelNode1Id= UUID.randomUUID().toString().replaceAll("-", "");
                String parentId = null;
                String busClusterId = String.valueOf(busClusterMap.get("BUS_CLUSTER_ID"));
                String busClusterName = String.valueOf(busClusterMap.get("BUS_CLUSTER_NAME"));
                UserPrivilegeNode busClusterNode = new UserPrivilegeNode(levelNode1Id, parentId, busClusterId, null, busClusterName, "MAIN_CLUSTER", "1");
                userPrivilegeNodes.add(busClusterNode);

                //集群业务节点
                List<UserPrivilegeNode> clusterNodeList = addClusterList(busClusterNode, userProgramList);
                if (CollectionUtils.isNotEmpty(clusterNodeList)) {
                    userPrivilegeNodes.addAll(clusterNodeList);

                    for (int y=0; y<clusterNodeList.size(); y++) {
                        UserPrivilegeNode clusterNode = clusterNodeList.get(y);
                        //获取业务集群版本信息
                        List<UserPrivilegeNode> versionNodeList = addVersionList(clusterNode, userProgramList);
                        if (CollectionUtils.isNotEmpty(versionNodeList)) {
                            userPrivilegeNodes.addAll(versionNodeList);
                            for (int z=0; z<versionNodeList.size(); z++) {
                                UserPrivilegeNode versionNode = versionNodeList.get(z);
                                //获取版本下程序列表
                                List<UserPrivilegeNode> programNodeList = addProgramList(versionNode, userProgramList);
                                userPrivilegeNodes.addAll(programNodeList);
                            }
                        }

                    }
                }

            }
        }
        return userPrivilegeNodes;
    }

    /**
     * 添加业务程序节点
     * @param clusterNode
     * @param userProgramList
     * @return
     */
    private List<UserPrivilegeNode> addProgramList(UserPrivilegeNode clusterNode, List<HashMap<String, Object>> userProgramList) {
        //业务集群
        for (int i=0; i<userProgramList.size(); i++) {
            Map<String, Object> clusterMap = userProgramList.get(i);
            String taskId = String.valueOf(clusterMap.get("TASK_ID"));
            String taskProgramId = String.valueOf(clusterMap.get("ID"));
            String clusterId = String.valueOf(clusterMap.get("CLUSTER_ID"));
            if (StringUtils.equals(clusterNode.getBusId(), taskId)
                    && StringUtils.equals(clusterNode.getClusterId(), clusterId)
                    && !this.isNodeExist(clusterNode.getChildrens(), taskProgramId)) {
                String levelNode4Id= UUID.randomUUID().toString().replaceAll("-", "");
                String programName = String.valueOf(clusterMap.get("PROGRAM_NAME")) + "(" + String.valueOf(clusterMap.get("PROGRAM_CODE")) +  ")";
                UserPrivilegeNode programNode = new UserPrivilegeNode(levelNode4Id, clusterNode.getId(), taskProgramId, clusterNode.getClusterId(), programName, "PROGRAM", "4");
                clusterNode.getChildrens().add(programNode);
            }
        }
        return clusterNode.getChildrens();
    }

    /**
     * 添加版本节点信息
     * @param clusterNode
     * @param userProgramList
     * @return
     */
    private List<UserPrivilegeNode> addVersionList(UserPrivilegeNode clusterNode, List<HashMap<String, Object>> userProgramList) {
        //业务集群
        for (int i=0; i<userProgramList.size(); i++) {
            Map<String, Object> clusterMap = userProgramList.get(i);
            String taskClusterId = String.valueOf(clusterMap.get("CLUSTER_ID"));
            String taskId = String.valueOf(clusterMap.get("TASK_ID"));
            if (StringUtils.equals(clusterNode.getBusId(), taskClusterId)
                    && !this.isNodeExist(clusterNode.getChildrens(), taskId)) {
                String levelNode3Id= UUID.randomUUID().toString().replaceAll("-", "");
                String taskVersion = String.valueOf(clusterMap.get("VERSION"));
                UserPrivilegeNode versionNode = new UserPrivilegeNode(levelNode3Id, clusterNode.getId(), taskId, clusterNode.getClusterId(), taskVersion, "VERSION", "3");
                clusterNode.getChildrens().add(versionNode);
            }
        }
        return clusterNode.getChildrens();
    }

    /**
     * 获取集群业务节点
     * @param busClusterNode
     * @param userProgramList
     * @return
     */
    private List<UserPrivilegeNode> addClusterList(UserPrivilegeNode busClusterNode, List<HashMap<String, Object>> userProgramList) {
        //业务集群
        for (int i=0; i<userProgramList.size(); i++) {
            Map<String, Object> clusterMap = userProgramList.get(i);
            String taskBusClusterId = String.valueOf(clusterMap.get("BUS_CLUSTER_ID"));
            String clusterId = String.valueOf(clusterMap.get("CLUSTER_ID"));
            if (StringUtils.equals(busClusterNode.getBusId(), taskBusClusterId) && !this.isNodeExist(busClusterNode.getChildrens(), clusterId)) {
                String levelNode2Id= UUID.randomUUID().toString().replaceAll("-", "");
                String clusterName = String.valueOf(clusterMap.get("CLUSTER_NAME")) + "(" + String.valueOf(clusterMap.get("CLUSTER_TYPE")) +")";
                UserPrivilegeNode clusterNode = new UserPrivilegeNode(levelNode2Id, busClusterNode.getId(), clusterId, clusterId, clusterName, "CLUSTER", "2");
                busClusterNode.getChildrens().add(clusterNode);
            }
        }
        return busClusterNode.getChildrens();
    }

    /**
     * 判断是否存在，如果存在返回true，否则返回false
     * @param userPrivilegeNodes
     * @param busId
     * @return
     */
    private Boolean isNodeExist(List<UserPrivilegeNode> userPrivilegeNodes, String busId) {
        for (int i=0; i<userPrivilegeNodes.size(); i++) {
            String childBusId = userPrivilegeNodes.get(i).getBusId();
            if (StringUtils.equals(childBusId, busId)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 添加用户业务程序权限
     * @param params
     * @param dbKey
     */
    @Override
    public void addUserBusProgramList(Map<String, Object> params, String dbKey) {
        logger.debug("添加用户功能列表信息， 业务参数：" + params.toString() + ", dbKey: " + dbKey);

        //删除用户业务程序权限
        Map<String, Object> delParams = new HashMap<String, Object>();
        delParams.put("ROLE_ID", params.get("ROLE_ID"));
        int delCount = coreService.deleteObject2New("userProgramListMapper.delUserProgramListByRole", delParams, dbKey);
        logger.debug("删除用户业务程序权限成功， 业务参数：" + delParams.toString() + ", 删除记录数：" + delCount);


        //添加用户业务程序权限
        List<Map<String, Object>> privilegeList = (List<Map<String, Object>>)params.get("PRIVILEGE_LIST");
        if (CollectionUtils.isNotEmpty(privilegeList)) {
            int addCount = coreService.insertBatchObject2New("userProgramListMapper.addUserProgramList", privilegeList, dbKey);
            logger.debug("添加用户业务程序权限成功， 业务参数： " + privilegeList.toString() + "， 新增记录数：" + addCount);
        }
    }

    /**
     * 查询业务程序启停列表
     * @param params
     * @param dbKey
     * @param  request
     * @return
     */
    @Override
    public Map<String, Object> queryUserProgramStartStopList(Map<String, Object> params, String dbKey, HttpServletRequest request) {
        logger.debug("查询业务程序启停列表, 业务参数： " + params.toString() + ", dbkey: " + dbKey);
        //返回结果对象
        Map<String, Object> retMap = new HashMap<String, Object>();
        //当前登录用户ID
        Map<String, Object> userMap = (Map<String, Object>)request.getSession().getAttribute("userMap");
        String empeeId = String.valueOf(userMap.get("EMPEE_ID"));

        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("EMPEE_ID", empeeId);
        queryMap.put("QUERY_PROGRAM_NAME", params.get("QUERY_PROGRAM_NAME"));

        String latnStr = String.valueOf(params.get("QUERY_LATN_ID"));
        String latnMsg = "";
        if (StringUtils.isNotBlank(latnStr)) {
            String [] latnStrArray = latnStr.split(",");
            for (int i=0; i<latnStrArray.length; i++) {
                latnMsg += "'" + latnStrArray[i] + "',";
            }
            if (StringUtils.isNotBlank(latnMsg)) {
                latnMsg = latnMsg.substring(0, latnMsg.length() - 1);
            }
        }
        queryMap.put("QUERY_LATN_IDS", latnMsg);
        queryMap.put("QUERY_PROGRAM_STATE", params.get("QUERY_PROGRAM_STATE"));

        String hostStr = String.valueOf(params.get("QUERY_HOST_ID"));
        String hostMsg = "";
        if (StringUtils.isNotBlank(hostStr)) {
            String [] hostStrArray = hostStr.split(",");
            for (int i=0; i<hostStrArray.length; i++) {
                hostMsg += "'" + hostStrArray[i] + "',";
            }
            if (StringUtils.isNotBlank(hostMsg)) {
                hostMsg = hostMsg.substring(0, hostMsg.length() - 1);
            }
        }
        queryMap.put("QUERY_HOST_IDS", hostMsg);
        List<HashMap<String, Object>> programList = coreService.queryForList2New("userProgramListMapper.queryUserBusProgramListForStart", queryMap, dbKey);

        int runStatus = 0 ;
        int stopStatus = 0;
        int rowCount = 0;
        if (CollectionUtils.isNotEmpty(programList)) {
            rowCount = programList.size();
            for (int i = 0; i < rowCount; i++) {
                if (BusinessConstant.PARAMS_STOP_STATE_ACTIVE.equals(StringTool.object2String(programList.get(i).get("RUN_STATE")))) {
                    stopStatus++;
                }else{
                    runStatus ++;
                }
            }
        }
        retMap.put("PROGRAM_LIST", programList);
        retMap.put("stopStatus", stopStatus);
        retMap.put("runStatus", runStatus);
        retMap.put("countRow", rowCount);
        logger.debug("查询业务程序启停列表结束，总记录数：" + rowCount + ", 运行状态记录数: " + runStatus + "， 停止状态记录数: " + stopStatus);

        return retMap;
    }

    /**
     * 业务程序启停
     * @param params
     * @param dbKey
     */
    @Override
    public Map<String, Object> addRunStopProgram(Map<String, Object> params, String dbKey) throws BusException {
        logger.debug("业务程序启停， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

        //结果对象
        Map<String, Object> rstMap = new HashMap<String, Object>();
        List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();

        //启动标识
        String flag = String.valueOf(params.get("flag"));

        //每一条业务实例作为一个单独的程序启动
        List<Map<String, Object>> hostList = (List<Map<String, Object>>)params.get("HOST_LIST");
        Map<String, Object> runTopoMap = null;
        for (int i=0; i<hostList.size(); i++) {
            Map<String, Object> startStopMap = new HashMap<String, Object>();
            Map<String, Object> hostMap = hostList.get(i);
            startStopMap.put("CLUSTER_ID", hostMap.get("CLUSTER_ID"));
            startStopMap.put("CLUSTER_TYPE", hostMap.get("CLUSTER_TYPE"));
            startStopMap.put("EMPEE_ID", params.get("EMPEE_ID"));
            startStopMap.put("flag", flag);
            startStopMap.put("RUN_STATE", params.get("RUN_STATE"));

            List<Map<String, Object>> programList = new ArrayList<Map<String, Object>>();
            programList.add(hostMap);
            startStopMap.put("HOST_LIST", programList);

            //当前程序启动方式
            Map<String, Object> returnMap = null;
            try {
                String diffIp = String.valueOf(hostMap.get("DIFF_IP"));
                String runJstorm = String.valueOf(hostMap.get("RUN_JSTORM"));
                if (StringUtils.equals(diffIp, BusinessConstant.PARAMS_BUS_1)) {    //启停不同IP业务程序
                    returnMap = runDiffIPService.updateRunAndStopHost(startStopMap, dbKey);
                    rstList.add(returnMap);
                } if (StringUtils.equals(runJstorm, BusinessConstant.PARAMS_BUS_1)) {   //启停Topo图

                    //设置Topology参数
                    runTopoMap = new HashMap<String, Object>();
                    runTopoMap.put("EMPEE_ID", params.get("EMPEE_ID"));
                    runTopoMap.put("flag", flag);
                    runTopoMap.put("RUN_STATE", params.get("RUN_STATE"));
                    runTopoMap.put("queryParam", hostMap);
                    returnMap = runTopologyService.updateRunAndStopHost(runTopoMap, dbKey);
                    rstList.add(returnMap);

                } else {
                    returnMap = runSameIPService.updateRunAndStopHost(startStopMap, dbKey);
                    rstList.add(returnMap);
                }
            } catch (BusException e) {
                returnMap = new HashMap<String, Object>();
                returnMap.put("info", e.getErrorMsg());
                returnMap.put("reason", e.getErrorReason());
                returnMap.put("flag", BusinessConstant.PARAMS_RST_ERROR);
                rstList.add(returnMap);
            } catch (Exception e) {
                returnMap = new HashMap<String, Object>();
                returnMap.put("info", e.getMessage() + "\n");
                returnMap.put("reason", e.getMessage() + "\n");
                returnMap.put("flag", BusinessConstant.PARAMS_RST_ERROR);
                rstList.add(returnMap);
            }
            returnMap.put("PROGRAM_NAME", hostMap.get("PROGRAM_NAME"));
            returnMap.put("HOST_IP", hostMap.get("HOST_IP"));
        }

        int successCount = 0;
        int failCount = 0;
        int totalCount = 0;
        //List<String> retMsg = new ArrayList<String>();
        String retMessage = "";
        String flagStr = StringUtils.equals(flag, BusinessConstant.PARAMS_STOP_FLAG) ? "停止" : "启动";
        if (CollectionUtils.isNotEmpty(rstList)) {
            totalCount = rstList.size();
            for (int i=0; i<rstList.size(); i++) {
                Map<String, Object> programRstMap = rstList.get(i);
                String programName = String.valueOf(programRstMap.get("PROGRAM_NAME"));
                String retFlag = String.valueOf(programRstMap.get("flag"));
                String retFlagStr = StringUtils.equals(retFlag, BusinessConstant.PARAMS_RST_ERROR) ? "失败" : "成功";
                String reason = String.valueOf(programRstMap.get("reason"));
                String info = String.valueOf(programRstMap.get("info"));
                String hostIp = programRstMap.get("HOST_IP") == null ? "" : String.valueOf(programRstMap.get("HOST_IP"));
                if (StringUtils.equals(retFlag, BusinessConstant.PARAMS_RST_ERROR)) {
                    String hostStr = StringUtils.isBlank(hostIp) ? "" : "主机IP：" + hostIp + "，";
                    retMessage += "<p>" + (i+1) + "、程序名：<font style='color:green;font-weight:bold;' color='red'>" + programName + "</font>，" + hostStr
                            + flagStr + "结果：<font color='red'>" + retFlagStr + "</font>， 失败信息：<font color='red'>" + reason + "</font></p>";
                    failCount++;
                } else {
                    String hostStr = StringUtils.isBlank(hostIp) ? "" : "主机IP：" + hostIp + "，";
                    retMessage += "<p>" + (i+1) + "、程序名：<font style='color:green;font-weight:bold;'>" + programName + "</font>， " + hostStr
                            + flagStr + "结果：<font color='green'>" + retFlagStr + "</font>， 成功信息：" + info + "</p>";
                    successCount++;
                }
                //retMsg.add(retMessage);
            }
        }
        String totalDesc = "本次共" + flagStr + "业务进程：<font style='font-weight:bold;'>"
                + totalCount + "</font>个，其中成功：<font style='color:green;font-weight:bold;'>"
                + successCount + "</font>个，失败：<font style='color:red;font-weight:bold;'>" + failCount + "</font>个，具体" + flagStr + "信息如下：";
        rstMap.put("TOTAL_DESC", totalDesc);
        //rstMap.put("TOTAL_MSG", retMsg.toString());
        rstMap.put("TOTAL_MSG", retMessage);
        logger.debug("业务程序启停完成， 返回结果：" + rstMap.toString());

        return rstMap;
    }

    /**
     * 业务程序状态检查
     * @param paramsList
     * @param dbKey
     * @return
     */
    @Override
    public Map<String, Object> checkRunStopProgram(List<Map<String, Object>> paramsList, String dbKey) throws BusException {
        logger.debug("业务程序状态检查， 业务参数: " + paramsList.toString() + ", dbKey: " + dbKey);

        //程序检查返回对象
        Map<String, Object> rstMap = new HashMap<String, Object>();

        //程序检查结果列表
        List<Map<String, Object>> checkRetList = new ArrayList<Map<String, Object>>();

        for(int i=0; i<paramsList.size(); i++) {
            Map<String, Object> programMap = paramsList.get(i);
            String runJstorm = String.valueOf(programMap.get("RUN_JSTORM"));
            String diffIP = String.valueOf(programMap.get("DIFF_IP"));

            Map<String, Object> retMap = new HashMap<String, Object>();
            retMap.put("PROGRAM_NAME", programMap.get("PROGRAM_NAME"));
            try {
                //Jstorm程序
                if (StringUtils.equals(runJstorm, BusinessConstant.PARAMS_BUS_1)) {
                    Map<String, Object> runTopoMap = new HashMap<String, Object>();
                    runTopoMap.put("queryParam", programMap);
                    runTopoMap.put("flag", BusinessConstant.PARAMS_CHECK_FLAG);
                    retMap = runTopologyService.updateCheckProgramState(runTopoMap, dbKey);
                } else if (StringUtils.equals(diffIP, BusinessConstant.PARAMS_BUS_1)) {
                    retMap = runDiffIPService.updateCheckHostState(programMap, dbKey);
                } else {
                    retMap = runSameIPService.updateCheckHostState(programMap, dbKey);
                }
                retMap.put("PROGRAM_NAME", programMap.get("PROGRAM_NAME"));
                checkRetList.add(retMap);
            } catch (BusException e) {
                retMap.put("state", BusinessConstant.PARAMS_BUS_4);
                retMap.put("reason", e.getErrorReason());
                checkRetList.add(retMap);
            } catch (Exception e) {
                retMap.put("state", BusinessConstant.PARAMS_BUS_4);
                retMap.put("reason", e.getMessage());
                checkRetList.add(retMap);
            }
        }

        int successCount = 0;
        int failCount = 0;
        int totalCount = 0;
        String retMessage = "";
        if (CollectionUtils.isNotEmpty(checkRetList)) {
            totalCount = checkRetList.size();
            for (int i=0; i<totalCount; i++) {
                Map<String, Object> checkMap = checkRetList.get(i);
                //状态检查结果状态
                String state = String.valueOf(checkMap.get("state"));
                //程序名称
                String programName = String.valueOf(checkMap.get("PROGRAM_NAME"));
                //状态检查进程
                String processId = StringTool.object2String(checkMap.get("process"));
                //失败原因
                String reason = String.valueOf(checkMap.get("reason"));

                if (StringUtils.equals(state, BusinessConstant.PARAMS_BUS_4)) {
                    retMessage += "<p>" + (i+1) + "、程序名：<font style='color:green;font-weight:bold;' color='red'>" + programName + "</font>"
                            + "，检查结果：<font color='red'>失败</font>， 原因：<font color='red'>" + reason + "</font></p>";
                    failCount++;
                } else if (StringUtils.equals(state, BusinessConstant.PARAMS_BUS_0)) {
                    retMessage += "<p>" + (i+1) + "、程序名：<font style='color:green;font-weight:bold;' color='red'>" + programName + "</font>"
                            + "，检查结果：当前程序未运行，已同步数据库</p>";
                    successCount++;
                } else if (StringUtils.equals(state, BusinessConstant.PARAMS_BUS_1)) {
                    retMessage += "<p>" + (i+1) + "、程序名：<font style='color:green;font-weight:bold;' color='red'>" + programName + "</font>"
                            + "，检查结果：当前程序正在运行";
                    if (StringUtils.isNotBlank(processId)) {
                        retMessage += "，其进程号：<font color='green'>" + processId + "</font>";
                    }
                    retMessage += "</p>";
                    successCount++;
                } else if (StringUtils.equals(state, BusinessConstant.PARAMS_BUS_3)){
                    retMessage += "<p>" + (i+1) + "、程序名：<font style='color:green;font-weight:bold;'>" + programName + "</font>"
                            + "，检查结果：该进程在对应主机不存在</p>";
                    successCount++;
                }
            }
        }
        String totalDesc = "本次共检查业务进程：<font style='font-weight:bold;'>"
                + totalCount + "</font>个，其中成功：<font style='color:green;font-weight:bold;'>"
                + successCount + "</font>个，失败：<font style='color:red;font-weight:bold;'>" + failCount + "</font>个，进程状态都已同步数据库，具体检查信息如下：";
        rstMap.put("TOTAL_DESC", totalDesc);
        rstMap.put("TOTAL_MSG", retMessage);
        return rstMap;
    }

    /**
     * 查询配置文件信息
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public List<FileRecord> queryCfgFileList(Map<String, Object> params, String dbKey) throws Exception {

        logger.debug("获取部署主机配置文件目录， 参数: " + params.toString() + ", dbKey: " + dbKey);

        //获取部署主机信息
        FtpDto ftpDto = SessionUtil.getFtpParams();

        String packageType = StringTool.object2String(params.get("packageType"));
        String path = ftpDto.getFtpRootPath() + Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR + packageType;

        logger.debug("部署主机配置文件目录:" + path);

        List<FileRecord> allFilesList = new ArrayList<FileRecord>();
        Trans trans = null;
        // 协议判断
        try {
            //登录部署主机
            trans = FTPUtils.getFtpInstance(ftpDto);
            trans.login();


            // 创建一个根目录
            FileRecord rootFile = new FileRecord();
            String rootId = UUID.randomUUID().toString().replaceAll("-", "");
            rootFile.setCurrId(rootId);
            rootFile.setFileName(params.get("packageType") +"（"+params.get("packageTypeName") +"）");
            rootFile.setFilePath(path);
            rootFile.setParentId(Constant.ROOT_NODE_FLAG);
            rootFile.setFileType('D');
            rootFile.setFileLevel("R");
            rootFile.setDesc(params.get("packageType")+"");
            allFilesList.add(rootFile);

            //判断目录是否存在，如果存在获取目录下的子节点数据
            boolean isExists = trans.isExistPath(path);
            logger.debug("当前目录: " + path + ", 在部署主机是否存在: " + isExists);
            if (isExists) {
                RecursiveFile recursiveFiles = new RecursiveFile(allFilesList);
                recursiveFiles.treeList(trans, path, rootId, null);
            }

            //映射目录中文名称
            if(CollectionUtils.isNotEmpty(allFilesList)){
                List<HashMap<String,Object>> busClusterList = coreService.queryForList2New("busMainCluster.queryRoleMainClusterInfo", params, FrameConfigKey.DEFAULT_DATASOURCE);
                Map<String,Object> queryMap = new HashMap<>();
                queryMap.put("GROUP_CODE","LATN_LIST");
                List<HashMap<String,Object>> latnList = coreService.queryForList2New("config.queryConfigList", queryMap, FrameConfigKey.DEFAULT_DATASOURCE);

                for(int i=0;i<allFilesList.size();++i){
                    FileRecord file = allFilesList.get(i);

                    if(file.isDirectory()){
                        String fileName = file.getFileName();

                        for(int j=0;j<busClusterList.size();++j){
                            Map<String,Object> map = busClusterList.get(j);

                            String busClusterId = ObjectUtils.toString(map.get("BUS_CLUSTER_ID"));
                            String busClusterName = ObjectUtils.toString(map.get("BUS_CLUSTER_NAME"));
                            String busClusterCode = ObjectUtils.toString(map.get("BUS_CLUSTER_CODE"));

                            if(StringUtils.equals(fileName, busClusterCode)){
                                file.setFileName(busClusterName + "(" + busClusterCode + ")");
                                file.setBusMainClusterId(busClusterId);
                                file.setClusterId(busClusterId);
                                break;
                            }
                        }
                        for(int j=0;j<latnList.size();++j){
                            Map<String,Object> map = latnList.get(j);

                            String NAME = ObjectUtils.toString(map.get("CONFIG_NAME"));
                            String CODE = ObjectUtils.toString(map.get("CONFIG_VALUE"));

                            if(StringUtils.equals(fileName, CODE)){
                                file.setFileName(NAME + "(" + CODE + ")");
                                break;
                            }
                        }
                    }
                }
            }

            //根据角色权限过滤
            if (!BlankUtil.isBlank(allFilesList)) {
                //根据角色权限过滤
                String clusterSwitch = SystemProperty.getContextProperty("cluster.permission.switch");
                if (StringUtils.equals(clusterSwitch.toLowerCase(), "yes")) {
                    Map<String, Object> roleMap = new HashMap<String, Object>();
                    roleMap.put("ROLE_ID" , StringTool.object2String(params.get("ROLE_ID")));
                    List<HashMap<String, Object>> clusterList = coreService.queryForList2New("busMainCluster.queryRoleBusMainClusterList", roleMap, dbKey);
                    if(CollectionUtils.isEmpty(clusterList)){
                        return new ArrayList<>();
                    }
                    for (HashMap clusterMap : clusterList) {
                        String busClusterId = StringTool.object2String(clusterMap.get("BUS_CLUSTER_ID"));
                        for (FileRecord file : allFilesList) {
                            String fileBusClusterId = file.getBusMainClusterId();
                            if (StringUtils.equals(busClusterId, fileBusClusterId)) {
                                getNodeParentNoFilter(allFilesList, file);
                                getNodeChildrenNoFilter(allFilesList, file);
                            }
                        }
                    }
                    for (int i=0; i< allFilesList.size(); i++) {
                        if (!StringUtils.equals(allFilesList.get(i).getFilterFlag(), BusinessConstant.PARAMS_BUS_1)) {
                            allFilesList.remove(allFilesList.get(i));
                            i--;
                        }
                    }
                }
            }

            //对结果文件进行排序
            Collections.sort(allFilesList, new Comparator<FileRecord>() {
                @Override
                public int compare(FileRecord fileObj1, FileRecord fileObj2) {
                    String prevFileName = fileObj1.getFileName();
                    String nextFileName = fileObj2.getFileName();
                    Collator collator = Collator.getInstance(Locale.CHINA);
                    return collator.compare(nextFileName, prevFileName);
                }
            });

        } catch (Exception e) {
            logger.error("获取服务器上所有文件列表失败-->" , e);
            throw new Exception(e.getMessage());
        } finally {
            if (trans != null) {
                trans.close();
            }
        }

        return allFilesList;
    }

    /**
     * 当前节点的父节点不过滤移除
     *
     * @param allFilesList
     * @param file
     */
    private void getNodeParentNoFilter(List<FileRecord> allFilesList, FileRecord file) {
        if (file != null) {
            file.setFilterFlag(BusinessConstant.PARAMS_BUS_1);
            FileRecord parentFileRecord = this.getCurrFileRecord(allFilesList, file.getParentId());
            getNodeParentNoFilter(allFilesList, parentFileRecord);
        }
    }

    /**
     * 获取当前文件父文件
     *
     * @param allFilesList
     * @param fileId
     * @return
     */
    private FileRecord getCurrFileRecord(List<FileRecord> allFilesList, String fileId) {
        FileRecord currFile = null;
        for (FileRecord fileRecord : allFilesList) {
            if (StringUtils.equals(fileRecord.getCurrId(), fileId)) {
                currFile = fileRecord;
                break;
            }
        }
        return currFile;
    }

    /**
     * 当前节点的字节点不过滤移除
     *
     * @param allFilesList
     * @param file
     */
    private void getNodeChildrenNoFilter(List<FileRecord> allFilesList, FileRecord file) {
        if (file != null) {
            file.setFilterFlag(BusinessConstant.PARAMS_BUS_1);
            for (FileRecord fileRecord : allFilesList) {
                if (StringUtils.equals(fileRecord.getParentId(), file.getCurrId())) {
                    getNodeChildrenNoFilter(allFilesList, fileRecord);
                }
            }
        }
    }

    /**
     * 添加用户配置文件权限
     * @param params
     * @param dbKey
     */
    @Override
    public void addCfgFileList(Map<String, Object> params, String dbKey) {
        logger.debug("添加用户配置文件列表信息， 业务参数：" + params.toString() + ", dbKey: " + dbKey);

        //删除用户业务程序权限
        Map<String, Object> delParams = new HashMap<String, Object>();
        delParams.put("ROLE_ID", params.get("ROLE_ID"));
        int delCount = coreService.deleteObject2New("userCfgFileMapper.delCfgFileByRoleId", delParams, dbKey);
        logger.debug("删除用户配置文件权限成功， 业务参数：" + delParams.toString() + ", 删除记录数：" + delCount);


        //添加用户业务程序权限
        List<Map<String, Object>> privilegeList = (List<Map<String, Object>>)params.get("PRIVILEGE_LIST");
        if (CollectionUtils.isNotEmpty(privilegeList)) {
            int addCount = coreService.insertBatchObject2New("userCfgFileMapper.addCfgFileList", privilegeList, dbKey);
            logger.debug("添加用户配置文件权限成功， 业务参数： " + privilegeList.toString() + "， 新增记录数：" + addCount);
        }
    }
}
