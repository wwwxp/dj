package com.tydic.service.nodemanager.impl;

import clojure.lang.Obj;
import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodemanager.NodeManagerService;
import com.tydic.service.nodemanager.StartNodeService;
import com.tydic.service.versiondeployment.util.NodeVerUtil;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.soap.Node;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 王贤朋
 */
@Service
public class StartNodeServiceImpl implements StartNodeService {

    /**
     * 核心Service对象
     */
    @Resource
    private CoreService coreService;

    private static Logger log = LoggerFactory.getLogger(StartNodeServiceImpl.class);

    /**
     * linux路径为二级及以上的目录返回true
     *
     * @param nodePath
     * @return
     */
    private boolean nodePathValidate(String nodePath) {

        //linux的二级目录及以上的匹配
        Pattern reg = Pattern.compile("^/[^/]+/[^/]+(/[^/]+)*/?$");

        Matcher matcher = reg.matcher(nodePath);

        return matcher.matches();
    }

    /**
     * 通过NODE_ID查找主机信息
     *
     * @param nodeId
     * @param table
     * @return
     */
    private Map<String, Object> getHostInfoById(String nodeId, List<Map<String, Object>> table) {

        for (Map<String, Object> row : table) {
            if (StringTool.object2String(row.get("NODE_ID")).equals(nodeId)) {
                return row;
            }
        }

        return null;
    }

    /**
     * 对多个程序进行启动
     *
     * @param userName
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> startNode(String userName, List<Map<String, Object>> params) throws Exception {

        Map<String, Object> result = new HashMap<>();
        List<String> nodeIds = new ArrayList<>();

        log.debug("循环遍历行记录，移除已启动的程序，获得未启动程序的NODE_ID");
        String state = null;

        for (int i = 0; i < params.size(); ++i) {
            state = StringTool.object2String(params.get(i).get("STATE"));
            if (state.equals("1")) {
                params.remove(params.get(i));
                i--;
                continue;
            }
            nodeIds.add(StringTool.object2String(params.get(i).get("NODE_ID")));
        }

        if (nodeIds.size() == 0) {
            result.put("errorMsg", "选中的节点都已经启动，无需再启动");
            return result;
        }

        log.debug("获得需要启动的程序的程序名称、程序路径、启动命令");

        //查询程序信息的参数的整理
        String nodeIdsStr = StringTool.object2String(nodeIds);
        Map<String, Object> nodeIdParam = new HashMap<>();
        nodeIdParam.put("NODE_IDS", nodeIdsStr.substring(1, nodeIdsStr.length() - 1));

        //通过多个NODE_ID查询启动NODE的相关信息
        List<Map<String, Object>> shellInfos = coreService.queryForList3New("startNodeMapper.queryStartInfoByNodeIds", nodeIdParam, FrameConfigKey.DEFAULT_DATASOURCE);

        Future<Map<String,Object>> startNodeRes = null;
        List<Future<Map<String,Object>>> startFutureList = new ArrayList<>();
        //遍历每一个需要启动的Node，一个个的进行启动
        for (Map<String, Object> needStartNode : params) {

            startNodeRes = NodeVerUtil.executorService.submit(new StartNodeCall(shellInfos,needStartNode,userName));
            startFutureList.add(startNodeRes);
        }
        startOrStopCallHandle(startFutureList,result);

        return result;
    }

    /**
     * 程序启动的线程
     */
    private class StartNodeCall implements Callable<Map<String,Object>> {
        private Map<String,Object> needStartNode = null;
        private List<Map<String, Object>> shellInfos = null;
        private String userName;

        StartNodeCall(List<Map<String, Object>> shellInfos,Map<String,Object> needStartNode,String userName){
            this.shellInfos = shellInfos;
            this.needStartNode = needStartNode;
            this.userName = userName;
        }
        @Override
        public Map<String, Object> call() {
            Map<String,Object> result = new HashMap<>();
            String nodeName = null;
            StringBuffer nodeStartMsg=new StringBuffer();
            try {
                nodeName=StringTool.object2String(needStartNode.get("NODE_NAME"));
                String version=StringTool.object2String(needStartNode.get("VERSION"));
                String nodeId = StringTool.object2String(needStartNode.get("NODE_ID"));

                nodeStartMsg.append("当前启动的程序："+nodeName+"("+version+")");

                Map<String, Object> shellInfo = getHostInfoById(nodeId, shellInfos);
                String pwd = DesTool.dec(StringTool.object2String(shellInfo.get("SSH_PASSWD")));
                String nodePath = StringTool.object2String(shellInfo.get("NODE_PATH"));

                String versionPath = FileTool.exactPath(nodePath) + FileTool.exactPath(version);

                if (!nodePathValidate(versionPath)) {

                    log.debug(shellInfo.get("NODE_NAME") + "的脚本路径不合法，必须为二级及以上的合法的绝对路径。");
                    nodeStartMsg.append("<br/>错误信息：程序的路径不合法，必须为二级及以上的合法的绝对路径！<br/>启动结果：<span style='color:red;font-weight:bold;'>失败</span>");
                    result.put("successNode",nodeStartMsg.toString());
                    return result;
                }

                //连接linux，执行启动脚本
                String hostIp=StringTool.object2String(shellInfo.get("HOST_IP"));
                String sshUser=StringTool.object2String(shellInfo.get("SSH_USER"));

                ShellUtils shellClient = new ShellUtils(hostIp,sshUser , pwd);
                nodeStartMsg.append("<br/>启动程序的主机："+hostIp+",用户名："+sshUser+"<br/>启动时间："+DateUtil.getCurrent(DateUtil.allPattern));

                String shellCommand = "cd ${shell_path};./${shell_file}".replace("${shell_path}", versionPath)
                                                                        .replace("${shell_file}", StringTool.object2String(shellInfo.get("START_CMD")));

                long timeConsuming=System.currentTimeMillis();
                String execRes = shellClient.execMsg(shellCommand);

                timeConsuming=(System.currentTimeMillis()-timeConsuming)/1000;

                nodeStartMsg.append("<br/>程序启动命令："+shellCommand);

                nodeStartMsg.append("<br/>启动脚本执行结果："+execRes);

                //启动成功后，进行后续操作，添加部署的Node到启停表，在启停表中已存在，则更新其停止状态为运行状态
                if (!execRes.contains(NodeConstant.SUCCESS)) {

                    nodeStartMsg.append("<br/>错误信息：启动脚本的执行结果不正确！</span>");
                    nodeStartMsg.append("<br/>启动结果：<span style='color:red;font-weight:bold;'>失败</span>");
                    result.put("failNode",nodeStartMsg.toString());
                    return result;
                }

                log.debug("执行结果为：" + execRes);

                nodeStartMsg.append("<br/>程序启动完成，耗时：["+timeConsuming+"]秒！");

                if (needStartNode.get("START_ID") == null) {

                    Map<String,Object> startRowInfo = coreService.queryForObject2New("startNodeMapper.queryNodeByNodeId", needStartNode, FrameConfigKey.DEFAULT_DATASOURCE);

                    startRowInfo.put("VERSION", needStartNode.get("VERSION"));
                    startRowInfo.put("STATE", "1");
                    startRowInfo.put("CREATED_USER", userName);
                    coreService.insertObject2New("startNodeMapper.insertStartNode", startRowInfo, FrameConfigKey.DEFAULT_DATASOURCE);

                } else {  //存在时，则更新停止状态为运行状态

                    needStartNode.put("STATE", "1");
                    needStartNode.put("CREATED_USER", userName);
                    coreService.updateObject2New("startNodeMapper.updateRunState", needStartNode, FrameConfigKey.DEFAULT_DATASOURCE);
                }

                nodeStartMsg.append("<br/>同步数据库完成...<br/>启动结果：<span style='color:green;font-weight:bold;'>成功</span>");

                result.put("successNode",nodeStartMsg.toString());
            } catch (Exception e) {

                log.debug(nodeName+"节点启动发生异常！");
                nodeStartMsg.append("<br/>错误信息：节点启动发生异常！<br/>启动结果：<span style='color:red;font-weight:bold;'>失败</span>");

                result.put("failNode",nodeStartMsg.toString());
                e.printStackTrace();
            }
            return result;
        }
    }

    /**
     * 对多个程序进行停止
     * params参数：
     * 选中的所有启停表上的行ID
     *
     * @param userName
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> stopNode(String userName, List<Map<String, Object>> params) throws Exception {
        Map<String, Object> result = new HashMap<>();

        StringBuffer ids = new StringBuffer();
        for (Map<String, Object> nodeId : params) {
            ids.append(nodeId.get("DEPLOY_ID"));
            ids.append(",");
        }
        ids.deleteCharAt(ids.length() - 1);

        //查询启停表中的所有START_ID对应的为运行状态的记录（含NODE_ID、STOP_CMD、VERSION字段）
        Map<String, Object> queryRunNodeParam = new HashMap<>();
        queryRunNodeParam.put("DEPLOY_IDS", ids);
        List<Map<String, Object>> needStopNodes = coreService.queryForList3New("startNodeMapper.queryNeedStopNode", queryRunNodeParam, FrameConfigKey.DEFAULT_DATASOURCE);

        Future<Map<String,Object>> stopNodeRes = null;
        List<Future<Map<String,Object>>> stopFutureList = new ArrayList<>();
        //遍历每一个正在运行中的程序，一个个的进行关闭
        for (Map<String, Object> stopNode : needStopNodes) {

            stopNodeRes = NodeVerUtil.executorService.submit(new StopNodeCall(stopNode,userName));
            stopFutureList.add(stopNodeRes);
        }

        startOrStopCallHandle(stopFutureList,result);


        return result;
    }

    /**
     * 对启停结果的操作
     * @param futureList
     * @param result
     */
    private void startOrStopCallHandle( List<Future<Map<String,Object>>> futureList,Map<String, Object> result){
        List<String> successNode = new ArrayList<>();
        List<String> failNode = new ArrayList<>();
        for(Future<Map<String,Object>> future:futureList){
            try{
                Map<String,Object> nodeRes = future.get(3L,TimeUnit.MINUTES);
                String successMsg=(String)nodeRes.get("successNode");
                if(!BlankUtil.isBlank(successMsg)){
                    successNode.add(successMsg);
                }else{
                    String failMsg=(String)nodeRes.get("failNode");
                    failNode.add(failMsg);
                }

            }catch (Exception e){
                log.error("程序启动或停止异常");
                e.printStackTrace();
            }
        }

        result.put("successNode",successNode);
        result.put("failNode",failNode);
    }

    /**
     * 停止程序的线程
     */
    private class StopNodeCall implements Callable<Map<String,Object>> {
        private Map<String, Object> stopNode = null;
        private String userName = null;
        StopNodeCall(Map<String, Object> stopNode,String userName){
            this.stopNode = stopNode;
            this.userName = userName;
        }
        @Override
        public Map<String, Object> call() throws Exception {
            Map<String,Object> result = new HashMap<>();

            String nodeName = null;
            StringBuffer nodeStartMsg=null;
            try {
                nodeName=StringTool.object2String(stopNode.get("NODE_NAME"));
                String version=StringTool.object2String(stopNode.get("VERSION"));

                nodeStartMsg=new StringBuffer();
                nodeStartMsg.append("当前停止的程序："+nodeName+"("+version+")");

                Map<String,Object> shellInfo = coreService.queryForObject2New("startNodeMapper.queryShellInfoByNodeIds", stopNode, FrameConfigKey.DEFAULT_DATASOURCE);

                String pwd = DesTool.dec(StringTool.object2String(shellInfo.get("SSH_PASSWD")));
                String nodePath = StringTool.object2String(shellInfo.get("NODE_PATH"));

                String versionPath = FileTool.exactPath(nodePath) + FileTool.exactPath(version);

                if (!nodePathValidate(versionPath)) {

                    log.debug(shellInfo.get("NODE_NAME") + "的路径不合法，必须为二级及以上的合法的绝对路径");
                    nodeStartMsg.append("<br/>错误信息：程序的路径不合法，必须为二级及以上的合法的绝对路径！<br/>停止结果：<span style='color:red;font-weight:bold;'>失败</span>");
                    result.put("failNode",nodeStartMsg.toString());
                    return result;
                }

                //连接linux，执行启动脚本，从而停止程序
                String hostIp=StringTool.object2String(shellInfo.get("HOST_IP"));
                String sshUser= StringTool.object2String(shellInfo.get("SSH_USER"));
                ShellUtils shellClient = new ShellUtils(hostIp,sshUser, pwd);
                nodeStartMsg.append("<br/>程序停止的主机："+hostIp+",用户名："+sshUser+"<br/>执行停止时间："+DateUtil.getCurrent(DateUtil.allPattern));

                String shellCommand = "cd ${shell_path};./${shell_file}".replace("${shell_path}", versionPath)
                                                                        .replace("${shell_file}", StringTool.object2String(stopNode.get("STOP_CMD")));

                long timeConsuming=System.currentTimeMillis();
                String execRes = shellClient.execMsg(shellCommand);

                timeConsuming=(System.currentTimeMillis()-timeConsuming)/1000;
                nodeStartMsg.append("<br/>程序启动命令："+shellCommand);

                nodeStartMsg.append("<br/>停止脚本执行结果："+execRes);
                log.debug("执行结果为：" + execRes);

                if (!execRes.contains(NodeConstant.SUCCESS)) {
                    nodeStartMsg.append("<br/>错误信息：远程主机停止脚本的执行结果不正确！</span>");
                    nodeStartMsg.append("<br/>停止结果：<span style='color:red;font-weight:bold;'>失败</span>");
                    result.put("failNode",nodeStartMsg.toString());
                    return result;
                }

                nodeStartMsg.append("<br/>程序停止耗时：["+timeConsuming+"]秒！<br/>停止结果：<span style='color:green;font-weight:bold;'>成功</span>");

                //后续操作：将运行状态更新为停止状态，并更新“更新时间”
                stopNode.put("UPDATE_USER", userName);
                coreService.insertObject2New("startNodeMapper.updateStopState", stopNode, FrameConfigKey.DEFAULT_DATASOURCE);

            } catch (Exception e) {

                log.debug(nodeName+"停止失败！");

                nodeStartMsg.append("<br/>错误信息：节点启动发生异常！<br/>停止结果：<span style='color:red;font-weight:bold;'>失败</span>");
                e.printStackTrace();

                result.put("failNode",nodeStartMsg.toString());
                return result;
            }

            result.put("successNode",nodeStartMsg.toString());
            return result;
        }
    }

    private Map<String,Object> getCheckInfoById(String deployId,List<Map<String,Object>> table){

        for(Map<String,Object> row:table){
            if(StringTool.object2String(row.get("DEPLOY_ID")).equals(deployId)){
                return row;
            }
        }

        return null;
    }

    /**
     * 检查程序的运行状态
     *
     * @param params
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> checkNode(List<Map<String, Object>> params) throws Exception {

        if (CollectionUtils.isEmpty(params)) {
            throw new IllegalArgumentException("检查程序，参数不能为空！");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> deployIds = new ArrayList<>();
        List<String> nodeIds = new ArrayList<>();
        for (Map<String, Object> row : params) {
            deployIds.add(StringTool.object2String(row.get("DEPLOY_ID")));
            nodeIds.add(StringTool.object2String(row.get("NODE_ID")));
        }
        Map<String, Object> ids = new HashMap<>();
        ids.put("DEPLOY_IDS", deployIds);

        //获得所有DEPLOY_ID对应的的”VERSION、CHECK_CMD、STATE“
        List<Map<String, Object>> allCheckInfo = coreService.queryForList3New("startNodeMapper.queryCheckInfoById", ids, FrameConfigKey.DEFAULT_DATASOURCE);

        //获得传入参数所有的”shell信息“
        ids.put("NODE_IDS", nodeIds);
        List<Map<String, Object>> allShellInfo = coreService.queryForList3New("startNodeMapper.queryShellInfoByIds", ids, FrameConfigKey.DEFAULT_DATASOURCE);

        String shellCmdModule = "cd ${shell_path};./${shell_file}";
        Future<Map<String,Object>> checkNodeRes = null;
        List<Future<Map<String,Object>>> checkFutureList = new ArrayList<>();
        //对选中的版本程序，进行一一检查
        for (Map<String, Object> versionNode : params) {
            checkNodeRes = NodeVerUtil.executorService.submit(new checkNodeCall(allCheckInfo,allShellInfo,shellCmdModule,versionNode));
            checkFutureList.add(checkNodeRes);
        }

        checkThreadHandle(checkFutureList,result);

        return result;
    }

    /**
     * 获得线程的返回值
     * @param futureList
     * @param result
     */
    private void checkThreadHandle( List<Future<Map<String,Object>>> futureList,List<Map<String, Object>> result){
            for(Future<Map<String,Object>> checkFuture:futureList){
                try{
                    Map<String,Object> checkNodeRes = checkFuture.get(3L,TimeUnit.MINUTES);
                    result.add(checkNodeRes);
                }catch (Exception e){
                    log.error("程序检查异常");
                    e.printStackTrace();
                }
            }
    }

    /**
     * 检查单个程序的线程
     */
    private class checkNodeCall implements Callable<Map<String,Object>> {
        private List<Map<String, Object>> allCheckInfo;
        private List<Map<String, Object>> allShellInfo;
        private String shellCmdModule;
        private Map<String, Object> versionNode;

        private checkNodeCall(List<Map<String, Object>> allCheckInfo,List<Map<String, Object>> allShellInfo,String shellCmdModule,Map<String, Object> versionNode){
                this.allCheckInfo = allCheckInfo;
                this.allShellInfo = allShellInfo;
                this.shellCmdModule = shellCmdModule;
                this.versionNode = versionNode;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            StringBuffer nodeCheckMsg = new StringBuffer();
            Map<String,Object> nodeCheck = new HashMap<>();
            try {
                Map<String, Object> shellInfo = getHostInfoById(StringTool.object2String(versionNode.get("NODE_ID")), allShellInfo);
                String deployId=StringTool.object2String(versionNode.get("DEPLOY_ID"));
                Map<String, Object> checkInfo = getCheckInfoById(deployId, allCheckInfo);

                String nodePath = StringTool.object2String(shellInfo.get("NODE_PATH"));
                String version = StringTool.object2String(checkInfo.get("VERSION"));

                nodeCheck.put("DEPLOY_ID",deployId);
                String versionPath = FileTool.exactPath(nodePath) + FileTool.exactPath(version);

                nodeCheckMsg.append("当前正在检查的节点名称为：" + StringTool.object2String(shellInfo.get("NODE_NAME")) + "，版本号为：" + version);

                //对路径进行检查
                if (!nodePathValidate(versionPath)) {

                    nodeCheckMsg.append("<br/>错误信息：程序的版本路径不正确，请检查！");
                    nodeCheckMsg.append("<br/>检查结果：<span style='color:red;font-weight:bold;'>检查失败</span>");
                    nodeCheck.put("msg", nodeCheckMsg.toString());
                    return nodeCheck;
                }

                String pwd = DesTool.dec(StringTool.object2String(shellInfo.get("SSH_PASSWD")));
                String hostIp = StringTool.object2String(shellInfo.get("HOST_IP"));
                String sshUser = StringTool.object2String(shellInfo.get("SSH_USER"));
                ShellUtils shellClient = new ShellUtils(hostIp, sshUser, pwd);

                nodeCheckMsg.append("<br/>程序检查的主机：" + hostIp + ",用户名：" + sshUser + "<br/>执行检查时间：" + DateUtil.getCurrent(DateUtil.allPattern));

                String shellCmd = shellCmdModule.replace("${shell_path}", versionPath);
                shellCmd = shellCmd.replace("${shell_file}", StringTool.object2String(checkInfo.get("CHECK_CMD")));

                long timeConsuming=System.currentTimeMillis();
                String execRes = shellClient.execMsg(shellCmd);

                timeConsuming = (System.currentTimeMillis() - timeConsuming) / 1000;

                nodeCheckMsg.append("<br/>程序检查命令：" + shellCmd);

                nodeCheckMsg.append("<br/>检查脚本的执行结果：" + execRes);

                log.debug("检查脚本执行结果：" + execRes);

                //判断执行脚本的结果是否正确
                if (execRes.contains("<<<error>>>")) {

                    nodeCheckMsg.append("<br/>错误信息：检查脚本的执行结果不正确！</span>");
                    nodeCheckMsg.append("<br/>检查结果：<span style='color:red;font-weight:bold;'>检查失败</span>");
                    nodeCheck.put("msg", nodeCheckMsg.toString());
                    return nodeCheck;
                }

                nodeCheckMsg.append("<br/>程序停止耗时：["+timeConsuming+"]秒！");

                //获得真正的运行状态，并判断当前状态是否为正确的状态
                String state=StringTool.object2String(checkInfo.get("STATE"));
                if (execRes.contains(NodeConstant.SUCCESS)) {

                    nodeCheckMsg.append("<br/>检查结果：<span style='color:green;font-weight:bold;'>运行中</span>");

                    //数据库状态不正确,则进行状态同步
                    if(state.equals(NodeConstant.STOP)){
                        toRightState(deployId,nodeCheckMsg);
                        nodeCheck.put("updateState", true);
                    }
                    nodeCheck.put("msg", nodeCheckMsg.toString());
                } else {
                    nodeCheckMsg.append("<br/>检查结果：<span style='color:green;font-weight:bold;'>停止</span>");

                    //数据库状态不正确,则进行状态同步
                    if (state.equals(NodeConstant.RUNNING)) {
                        toRightState(deployId, nodeCheckMsg);
                        nodeCheck.put("updateState", true);
                    }
                    nodeCheck.put("msg", nodeCheckMsg.toString());
                }
            }catch (Exception e){

                log.debug("检查失败！");
                nodeCheckMsg.append("<br/>错误信息：节点检查过程中发生异常！<br/>检查结果：<span style='color:red;font-weight:bold;'>检查失败</span>");
                nodeCheck.put("msg", nodeCheckMsg.toString());
            }

            return nodeCheck;
        }
    }

    /**
     * 改变程序的运行状态
     * @param deployId
     * @param nodeCheckMsg
     */
    private void toRightState(String deployId,StringBuffer nodeCheckMsg){

        Map<String,Object> deployIdMap=null;

        deployIdMap=new HashMap<>();
        deployIdMap.put("DEPLOY_ID",deployId);
        int effect=coreService.updateObject2New("startNodeMapper.updateStateByDeployId",deployIdMap, FrameConfigKey.DEFAULT_DATASOURCE);

        if(effect>0) {
            nodeCheckMsg.append("<span style='color:blue;'>（当前状态与检查的实际状态不符，已同步数据库）</span>");
        }else{
            nodeCheckMsg.append("<span style='color:blue;'>（当前状态与检查的实际状态不符，且同步数据库失败，请检查脚本！）</span>");
        }
    }

    /**
     * 加载配置文件的树结构
     * 传入参数：nodeId、version
     *
     * @param params
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> loadFileTree(Map<String, Object> params) throws Exception {
        List<Map<String, Object>> treeFiles = new ArrayList<>();
        Map<String, Object> shellInfo = null;

        shellInfo = coreService.queryForObject2New("startNodeMapper.queryShellInfoByNodeIds", params, FrameConfigKey.DEFAULT_DATASOURCE);

        String pwd = DesTool.dec(StringTool.object2String(shellInfo.get("SSH_PASSWD")));

        String versionPath = FileTool.exactPath(StringTool.object2String(shellInfo.get("NODE_PATH"))) + StringTool.object2String(params.get("VERSION"));


        FtpDto pubFtpDto = SessionUtil.getFtpParams();
        String ftpType = pubFtpDto.getFtpType();

        FtpDto ftpDto = new FtpDto();
        ftpDto.setFtpType(ftpType);
        ftpDto.setHostIp(StringTool.object2String(shellInfo.get("HOST_IP")));
        ftpDto.setUserName(StringTool.object2String(shellInfo.get("SSH_USER")));
        ftpDto.setPassword(pwd);
        ftpDto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);

        Trans ftpClient = FTPUtils.getFtpInstance(ftpDto);
        FTPUtils.tryLogin(ftpClient);
        Vector<FileRecord> fileList = ftpClient.getFileList(versionPath);

        Map<String, Object> currFile = new HashMap<>();
        currFile.put("fileName", StringTool.object2String(params.get("VERSION")));

        String nodeId = StringTool.object2String(params.get("NODE_ID"));
        currFile.put("nodeId", nodeId);
        treeFiles.add(currFile);

        //配置文件目录名
        String config_dir = NodeVerUtil.getCfgPathSufx();
        String temp_dir = NodeVerUtil.getTomcatTempPath();
        String contextCfg = StringTool.object2String(shellInfo.get("CONTEXT_CFG"));
        List<String> allowDir = new ArrayList<>();
        List<String> allowConfigDir = new ArrayList<>();
        Collections.addAll(allowDir,"bin","conf","webapps",config_dir,temp_dir,contextCfg);
        Collections.addAll(allowConfigDir,"bin","conf",config_dir);
        getAllChildren(ftpClient,fileList,currFile,allowDir,allowConfigDir);


        return treeFiles;
    }

    /**
     * 获得versionPath下的子配置文件
     * @param ftpClient
     * @param childVector
     * @param currFile
     * @param allowDir
     * @param allowConfigDir
     * @throws Exception
     */
    private void getAllChildren(Trans ftpClient,Vector<FileRecord> childVector,Map<String, Object> currFile,List<String> allowDir,List<String> allowConfigDir) throws Exception{
        List<Map<String,Object>> childList = new ArrayList<>();
        Map<String,Object> curChild = null;
        String filePath = null;
        String parentFileName = null;
        for(FileRecord child : childVector){

            if(child.isDirectory()){

                if(allowDir.contains(child.getFileName())){
                    curChild=new HashMap<>();
                    curChild.put("fileName",child.getFileName());

                    Vector<FileRecord> sonVector = ftpClient.getFileList(child.getFilePath() + "/" + child.getFileName());
                    getAllChildren(ftpClient, sonVector, curChild, allowDir,allowConfigDir);
                    childList.add(curChild);
                }

            }else{

                filePath = child.getFilePath();
                parentFileName = filePath.substring(filePath.lastIndexOf("/")+1);
                if(allowConfigDir.contains(parentFileName)){
                    curChild=new HashMap<>();
                    curChild.put("fileName",child.getFileName());
                    childList.add(curChild);
                }

            }


        }

        currFile.put("children",childList);
    }


    /**
     * 获得配置文件的文本内容
     *
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> getFileContent(Map<String, Object> params) throws Exception {
        Map<String, Object> result = new HashMap<>();

        log.debug("启停程序获得配置文件的文件内容，传入的参数为：" + params);

        Map<String, Object> shellInfo = coreService.queryForObject2New("startNodeMapper.queryShellInfoByNodeIds", params, FrameConfigKey.DEFAULT_DATASOURCE);
        String pwd = DesTool.dec(StringTool.object2String(shellInfo.get("SSH_PASSWD")));
        String shellCommand = "cat ${file_path}";
        String hostIp=StringTool.object2String(shellInfo.get("HOST_IP"));
        ShellUtils shellClient = new ShellUtils(hostIp, StringTool.object2String(shellInfo.get("SSH_USER")), pwd);
        String filePath = FileTool.exactPath(StringTool.object2String(shellInfo.get("NODE_PATH"))) + params.get("RELATIVE_FILE_PATH");

        if (!nodePathValidate(filePath)) {
            result.put("success", false);
            result.put("content", "文件路径非法，请检查路径！");
            return result;
        }

        List<String> allowConfigDir = new ArrayList<>();
        String config_dir = NodeVerUtil.getCfgPathSufx();
        Collections.addAll(allowConfigDir,"bin","conf",config_dir);

        String parentPath = filePath.substring(0,filePath.lastIndexOf("/"));
        String parentFileName = parentPath.substring(parentPath.lastIndexOf("/")+1);
        if(!allowConfigDir.contains(parentFileName)){
            result.put("success", true);
            result.put("hostIp",hostIp);
            result.put("filePath",filePath);
            result.put("content", "");
            return result;
        }

        shellCommand = shellCommand.replace("${file_path}", filePath);

        String fileContent = shellClient.execMsg(shellCommand);

        result.put("success", true);
        result.put("hostIp",hostIp);
        result.put("filePath",filePath);
        result.put("content", fileContent);

        return result;
    }

    /**
     * 节点的版本删除，不是删除整个节点
     *
     * @param userName
     * @param params
     * @return
     * @throws Exception
     */
    public Map<String, Object> deleteNodeVersion(String userName, List<Map<String, Object>> params) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("effectRow", -1);

        //判断选中的多个NODE是否有正在运行的，只要有一个运行的，则不进行删除
        Map<String,Object> runCount=coreService.queryForObject2New("startNodeMapper.queryStartNodeCount",params.get(params.size()-1),FrameConfigKey.DEFAULT_DATASOURCE);
        if (Integer.parseInt(StringTool.object2String(runCount.get("RUN_COUNT")))>0) {
            result.put("errorMsg", "有程序正在运行，无法删除");
            return result;
        }

        //删除多个节点版本目录、配置文件
        Map<String, Object> shellInfo = null;
        String pwd = null;
        ShellUtils shellClient = null;
        String commandModule = "rm -rf ${version_path};";
        String command = null;
        String versionPath = null;

        //日志记录的信息
        String logContentModule = "用户${user}删除节点版本，版本号${version}，节点名称:${nodeName}，节点主机:${hostip}(${userName}),节点删除目录:${path},${config_path}";
        String logContent = null;
        String version = null;
        Map<String, Object> logParam = new HashMap<>();
        Map<String,Object> configFileInfo=null;
        String configPath=null;

        params.remove(params.size()-1);
        for (Map<String, Object> nodeInfo : params) {

            shellInfo = coreService.queryForObject2New("startNodeMapper.queryShellInfoByNodeIds", nodeInfo, FrameConfigKey.DEFAULT_DATASOURCE);
            pwd = DesTool.dec(StringTool.object2String(shellInfo.get("SSH_PASSWD")));
            shellClient = new ShellUtils(StringTool.object2String(shellInfo.get("HOST_IP")), StringTool.object2String(shellInfo.get("SSH_USER")), pwd);
            version = StringTool.object2String(nodeInfo.get("VERSION"));
            versionPath = FileTool.exactPath(StringTool.object2String(shellInfo.get("NODE_PATH"))) + version;
            command = commandModule.replace("${version_path}", versionPath);
            //版本目录、配置文件的删除
            shellClient.execMsg(command);

            //启停表中的节点信息的删除
            coreService.deleteObject2New("startNodeMapper.delNodeOnStart", nodeInfo, FrameConfigKey.DEFAULT_DATASOURCE);

            //部署表中的节点信息的删除
            coreService.deleteObject2New("startNodeMapper.delNodeOnDeploy", nodeInfo, FrameConfigKey.DEFAULT_DATASOURCE);

            //后续操作：删除成功记录日志
            logContent = logContentModule.replace("${user}", userName);
            logContent = logContent.replace("${version}", version);
            logContent = logContent.replace("${nodeName}", StringTool.object2String(shellInfo.get("NODE_NAME")));
            logContent = logContent.replace("${hostip}", StringTool.object2String(shellInfo.get("HOST_IP")));
            logContent = logContent.replace("${userName}", StringTool.object2String(shellInfo.get("SSH_USER")));
            logContent = logContent.replace("${path}", versionPath);

            //获得配置文件的所在目录
            configFileInfo=coreService.queryForObject2New("startNodeMapper.queryNodeByNodeId",nodeInfo,FrameConfigKey.DEFAULT_DATASOURCE);
            configPath=StringTool.object2String(configFileInfo.get("START_CMD"));
            configPath=FileTool.exactPath(versionPath)+configPath.substring(0,configPath.lastIndexOf("/"));

            logContent = logContent.replace("${config_path}", configPath);

            logParam.put("OPERATOR_MODULE", OperatorModule.NODE_START_MANAGER.getComment());
            logParam.put("OPERATOR_NAME",OperatorName.DEL_NODE_VERSION.getComment());
            logParam.put("CREATED_USER",userName);
            logParam.put("LOG_CONTENT",logContent);

            coreService.insertObject2New("startNodeMapper.insertNodeHandleInfo",logParam,FrameConfigKey.DEFAULT_DATASOURCE);
        }

        result.put("effectRow", 1);

        return result;
    }

}

