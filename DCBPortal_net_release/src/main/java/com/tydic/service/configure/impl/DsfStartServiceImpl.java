package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.DsfStartService;
import com.tydic.service.configure.InstConfigService;
import com.tydic.util.Constant;
import com.tydic.util.ShellUtils;
import com.tydic.util.SingletonThreadPool;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.log.LoggerUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Service
public class DsfStartServiceImpl implements DsfStartService {

    /**
     * 日志对象
     */
    private static Logger logger = Logger.getLogger(DsfStartServiceImpl.class);

    @Autowired
    private CoreService coreService;

    /**
     * 获取启停日志文件service对象
     */
    @Autowired
    private InstConfigService instConfigService;

    /**
     * 启动dsf
     * @param param
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> startDsf(List<Map<String, String>> param, String dbKey) throws Exception {
        logger.info("启动Dsf,参数--->"+param+", dbKey --->"+dbKey);
        //主机处理结果保存对象
        List<Map<String, Object>> resultMsgList = new ArrayList<Map<String, Object>>();
        //返回对象
        Map<String, Object> rstMap = new HashMap<String, Object>();

        if(!BlankUtil.isBlank(param)){

            //获取当前集群部署根目录
            Map<String, String> querySingleMap = param.get(0);
            Map<String, Object> queryClusterMap = new HashMap<String, Object>();
            queryClusterMap.put("CLUSTER_ID", querySingleMap.get("CLUSTER_ID"));
            queryClusterMap.put("CLUSTER_TYPE", querySingleMap.get("CLUSTER_TYPE"));
            Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
            if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
                throw new RuntimeException("集群信息查询失败, 请检查！");
            }
            //组件部署根目录
            final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
            //集群ID
            final String clusterId = querySingleMap.get("CLUSTER_ID");
            //集群类型
            final String clusterType = querySingleMap.get("CLUSTER_TYPE");
            //集群编码
            final String clusterCode = querySingleMap.get("CLUSTER_CODE");
            //操作用户ID
            final String empeeId = StringTool.object2String(querySingleMap.get("EMPEE_ID"));
            //获取启动组件执行线程
            ExecutorService pool = SingletonThreadPool.getExecutorService();

            try{
                final List<Map<String,String>> resultList = new ArrayList<>();

                Long startTimes = System.currentTimeMillis();
                for (int i = 0;i<param.size();i++){
                    final Map<String ,String> hostMap = (Map<String, String>) param.get(i);
                    pool.execute(new Runnable() {
                        public void run() {
                            Map<String, String> resultMap = new HashMap<>();
                            Long startProTimes = System.currentTimeMillis();
                            Logger threadLogger = null;
                            Map tempMap = null;
                            try {
                                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                                //程序启停版本
                                final String version = StringTool.object2String(hostMap.get("VERSION"));
                                //启停组件小类型
                                final String deployType = StringTool.object2String(hostMap.get("DEPLOY_TYPE"));
                                //获取启停命令
                                // chmod a+x auto.sh;./auto.sh -s ${clusterType} -2 ${tracker} -3 ${配置文件} -4 ${version}
                                final String autoFilePath = StringTool.object2String(hostMap.get("autoFile"));

                                //Dsf启动命令
                                //chmod a+x auto.sh;mkdir -p exec_temp/7943521668806;cp auto.sh exec_temp/7943521668806/auto.sh;cd exec_temp/7943521668806;./auto.sh -s dsf -2 dsf -3 /home/bp_dcf/DCBP_DFS/ -4 1.0.0;rm -rf ../7943521668806;
                                final String deployFile = MessageFormat.format(autoFilePath, Constant.DSF, Constant.DSF, appRootPath, version, StringTool.object2String(System.nanoTime()));
                                logger.debug("Dsf 启停脚本命令 ---> " + deployFile);

                                //配置文件真实路径
                                String completFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.DSF_DIR;

                                //主机ID
                                String hostId = hostMap.get("HOST_ID");

                                //线程日志文件目录
                                String logPath = instConfigService.getLogPath(clusterType);
                                logger.info("start dsf logPath--->" + logPath);

                                //线程日志文件
                                Map<String, Object> logNameMap = instConfigService.getLogName(clusterId, clusterType, hostId, version, clusterType, completFilePath);
                                String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
                                String logStrName = ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
                                logger.info("start dsf logShaName--->" + logShaName + ", logStrName--->" + logStrName);

                                String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
                                threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "M2dbStartServiceImpl-Start-" + Thread.currentThread().getName());
                                threadLogger.info("start dsf, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
                                threadLogger.info("clusterId: " + clusterId + ", hostId: " + hostId + ", version: " + version + ", deployType: " + clusterType + ", deployFileType: " + clusterType + ", startAutoFilePath: " + completFilePath);
                                threadLogger.info("logName source --->" + logStrName);

                                //获取启停主机信息
                                Map<String, Object> queryMap = new HashMap<String, Object>();
                                queryMap.put("HOST_ID", hostId);
                                threadLogger.debug("query host, params: " + queryMap);
                                tempMap = coreService.queryForObject2New("host.queryHostList", queryMap, dbKey);
                                threadLogger.debug("query host, result: " + tempMap);

                                //最终SSH2执行的命令
                                String command = "";
                                //获取dsf配置文件目录
                                command += "cd " + appRootPath + Constant.Tools + ";" + deployFile;
                                String sshIp = String.valueOf(tempMap.get("HOST_IP"));
                                String sshUser = String.valueOf(tempMap.get("SSH_USER"));
                                String sshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
                                ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);

                                logger.debug("执行启动Dsf命令，命令: " + command);
                                threadLogger.debug("start dsf, sshIp: " + sshIp +", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
                                threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
                                String result = cmdUtil.execMsg(command);
                                threadLogger.info("result: " + result);
                                logger.debug("执行启动Dsf命令，结果: " + result);

                                resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
                                resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

                                if (result.indexOf(Constant.SUCCESS) < 0) {
                                    threadLogger.error("start dsf failed, cause by: " + result);
                                    resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
                                } else {
                                    threadLogger.info("start dsf success!!");
                                    //该参数是用来添加到启动记录表
                                    resultMap.put("DEPLOY_FILE_TYPE", clusterType);
                                    resultMap.put("INST_PATH", Constant.T_DEFAULT);
                                    resultMap.put("FILE_PATH", completFilePath);
                                    resultMap.put("DEPLOY_TYPE", clusterType);
                                    resultMap.put("VERSION", version);
                                    resultMap.put("STATUS", Constant.STATE_ACTIVE);
                                    //配置文件软连接路径
                                    String softLinkPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DSF_DIR + Constant.CONF;
                                    resultMap.put("SOFT_LINK_PATH", softLinkPath);
                                    resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
                                }
                                //将脚本输出信息中的空行和错误标志给替换
                                result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
                                resultMap.put(Constant.RST_STR, result);
                                resultList.add(resultMap);

                                threadLogger.info("ret result--->" + resultMap.toString());
                                Long endProTimes = System.currentTimeMillis();
                                Long totalProTimes = (endProTimes - startProTimes)/1000;
                                threadLogger.info("start dsf final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
                            } catch (Exception e) {
                                logger.error("DSF程序启动失败，失败原因: ", e);
                                resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
                                resultMap.put(Constant.RST_STR, "DSF failed to start, cause: " + e.getMessage());
                                resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
                                resultList.add(resultMap);
                                if (threadLogger != null) {
                                    threadLogger.error("start dsf failed, cause by: ", e);
                                    threadLogger.info("ret result --->" + resultMap.toString());
                                    Long endProTimes = System.currentTimeMillis();
                                    Long totalProTimes = (endProTimes - startProTimes)/1000;
                                    threadLogger.info("start dsf final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
                                }
                            }
                        }
                    });
                }

                //轮序等待所有线程执行完成
                while(resultList.size() < param.size()){
                    logger.debug("本次总启动DSF进程数:" + param.size() + ", 已经启动完成DSF进程数:" + resultList.size());
                    SingletonThreadPool.getExecutorService();
                    Thread.sleep(2000);
                }
                Long endTimes = System.currentTimeMillis();
                Long totalTimes = (endTimes - startTimes)/1000;
                logger.info("DSF进程启动完成，本次启动DSF进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

                for(int i = 0 ; i < resultList.size();i++){
                    Map<String, Object> singleMap = new HashMap<String, Object>();
                    Map<String,String> resultMap = resultList.get(i);
                    if(Constant.RST_CODE_SUCCESS.equalsIgnoreCase(resultMap.get(Constant.RST_CODE))){
                        singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 启动成功\n" + resultMap.get(Constant.RST_STR));

                        //将配置信息添加到数据库保存
                        Map<String, String> fileMap = new HashMap<>();
                        fileMap.put("HOST_ID", resultMap.get("HOST_ID"));
                        fileMap.put("DEPLOY_FILE_TYPE", resultMap.get("DEPLOY_FILE_TYPE"));
                        fileMap.put("FILE_PATH", resultMap.get("FILE_PATH"));
                        fileMap.put("DEPLOY_TYPE", resultMap.get("DEPLOY_TYPE"));
                        fileMap.put("INST_PATH", resultMap.get("INST_PATH"));
                        fileMap.put("VERSION", resultMap.get("VERSION"));
                        fileMap.put("SOFT_LINK_PATH", resultMap.get("SOFT_LINK_PATH"));
                        fileMap.put("CONFIG_PATH", resultMap.get("CONFIG_PATH"));
                        fileMap.put("STATUS", resultMap.get("STATUS"));
                        fileMap.put("PM2_NAME", resultMap.get("PM2_NAME"));
                        fileMap.put("IS_MONITOR", resultMap.get("IS_MONITOR"));
                        fileMap.put("CLUSTER_ID", clusterId);
                        fileMap.put("PORT", resultMap.get("PORT"));
                        //存在匹配记录，更新
                        List<HashMap<String, String>> instList = coreService.queryForList("instConfig.queryConfigInfoByConditions", fileMap, dbKey);
                        if(!BlankUtil.isBlank(instList) && !instList.isEmpty()){
                            coreService.updateObject("instConfig.updateConfigInfoByConditions", fileMap, dbKey);
                            logger.debug("启动实例修改成功, 参数: " + fileMap.toString());
                        } else {//不存在匹配记录，添加
                            if (Constant.DCAM.equals(resultMap.get("DEPLOY_FILE_TYPE"))
                                    || Constant.DCAS.equals(resultMap.get("DEPLOY_FILE_TYPE"))) {
                                coreService.insertObject("instConfig.addDcfDeployInstConfigByDcasAndDcam", fileMap, dbKey);
                            } else {
                                coreService.insertObject("instConfig.addDcfDeployInstConfig", fileMap, dbKey);
                            }
                            logger.debug("启动实例添加成功, 参数: " + fileMap.toString());
                        }

                    } else {
                        singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 启动失败\n" + resultMap.get(Constant.RST_STR));
                    }
                    singleMap.put(Constant.RST_CODE, resultMap.get(Constant.RST_CODE));
                    resultMsgList.add(singleMap);
                }

                //返回结果信息
                StringBuffer rstBuffer = new StringBuffer();
                String rstCode = Constant.RST_CODE_SUCCESS;
                for (int i=0; i<resultList.size(); i++) {
                    Map<String, Object> hostRstMap = resultMsgList.get(i);
                    rstBuffer.append(hostRstMap.get(Constant.RST_STR)).append(Constant.LINE_FLAG);
                    if (Constant.RST_CODE_FAILED.equalsIgnoreCase(StringTool.object2String(hostRstMap.get(Constant.RST_CODE)))) {
                        rstCode = Constant.RST_CODE_FAILED;
                    }
                }
                rstMap.put(Constant.RST_STR, rstBuffer.toString());
                rstMap.put(Constant.RST_CODE, rstCode);

            }catch (Exception e){
                logger.info("启动Dsf失败，失败原因：",e);
            }
        }
        return rstMap;
    }


    /**
     * 停止dsf
     * @param param
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> stopDsf(List<Map<String, String>> param, String dbKey) throws Exception {
        logger.debug("停止Dsf， 参数 ---> " + param + ", dbKey ---> " + dbKey);
        //主机处理结果保存对象
        List<Map<String, Object>> resultMsgList = new ArrayList<Map<String, Object>>();
        //返回对象
        Map<String, Object> rstMap = new HashMap<String, Object>();

        if(!BlankUtil.isBlank(param)){
            //获取当前集群部署根目录
            Map<String, String> querySingleMap = param.get(0);
            Map<String, Object> queryClusterMap = new HashMap<String, Object>();
            queryClusterMap.put("CLUSTER_ID", querySingleMap.get("CLUSTER_ID"));
            queryClusterMap.put("CLUSTER_TYPE", querySingleMap.get("CLUSTER_TYPE"));
            Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
            if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
                throw new RuntimeException("集群信息查询失败, 请检查！");
            }
            //组件部署根目录
            final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
            //集群ID
            //final String clusterId = querySingleMap.get("CLUSTER_ID");
            //集群类型
            final String clusterType = querySingleMap.get("CLUSTER_TYPE");
            //用户ID
            final String empeeId = querySingleMap.get("EMPEE_ID");
            //获取启动组件执行线程
            ExecutorService pool = SingletonThreadPool.getExecutorService();
            try {
                final List<Map<String,String>> resultList = new ArrayList<>();
                Long startTimes = System.currentTimeMillis();
                for(int i=0;i<param.size();i++){
                    final Map<String ,String> hostMap =(Map<String, String>) param.get(i);
                    //通过线程池异步调用停止脚本
                    pool.execute(new Runnable() {
                        public void run() {
                            Map<String, String> resultMap = new HashMap<>();
                            Long startProTimes = System.currentTimeMillis();
                            Logger threadLogger = null;
                            Map tempMap = null;
                            try {
                                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                                //停止程序执行Shell命令
                                String executeCmd = hostMap.get("autoFile");
                                //停止程序小类型
                                String deployFileType = clusterType;
                                //配置文件
                                String instName = hostMap.get("INST_PATH");
                                //停止程序版本
                                String version = hostMap.get("VERSION");
                                //格式化后的执行命令
                                String autoFile = MessageFormat.format(executeCmd, Constant.DSF, deployFileType, instName, version);

                                //组件程序启停实例ID
                                String instId = hostMap.get("INST_ID");
                                //日志文件路径
                                String logPath = instConfigService.getLogPath(clusterType);
                                logger.info("stop dsf logPath--->" + logPath);

                                Map<String, Object> logNameMap = instConfigService.getLogName(instId, dbKey);
                                //加密后的日志文件名称
                                String logShaName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
                                //日志文件名称构成字串
                                String logStrName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
                                logger.info("stop dsf logShaName--->" + logShaName + ", logStrName--->" + logStrName);

                                String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
                                threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "DsfStartServiceImpl-Stop-" + Thread.currentThread().getName());
                                threadLogger.info("stop dsf, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
                                threadLogger.info("logName source: " + logStrName);

                                //根据主机ID查询主机信息
                                threadLogger.debug("query host, params: " + hostMap);
                                tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);
                                threadLogger.debug("query host, result: " + tempMap);

                                //停止dsf执行命令
                                String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
                                //远程SSH主机信息
                                String sshIp = StringTool.object2String(tempMap.get("HOST_IP"));
                                String sshUser = StringTool.object2String(tempMap.get("SSH_USER"));
                                String sshPasswd = DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD")));
                                ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);
                                logger.debug("执行停止Dsf命令，命令: " + command);
                                threadLogger.info("stop dsf, sshIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
                                threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
                                String result = cmdUtil.execMsg(command);
                                threadLogger.info("result: " + result);
                                logger.debug("执行停止Dsf命令，结果: " + result);

                                resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
                                resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

                                //用来修改记录状态
                                resultMap.put("INST_ID", String.valueOf(hostMap.get("INST_ID")));
                                if (result.indexOf(Constant.SUCCESS) < 0) {
                                    resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
                                    threadLogger.error("stop dsf failed, cause by: " + result);
                                } else {
                                    threadLogger.info("stop dsf success!!");
                                    resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
                                }
                                //将脚本输出信息中的空行和错误标志给替换
                                result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
                                resultMap.put(Constant.RST_STR, result);
                                resultList.add(resultMap);

                                threadLogger.info("ret result: " + resultMap.toString());
                                long endProTimes = System.currentTimeMillis();
                                long totalProTimes = (endProTimes - startProTimes)/1000;
                                threadLogger.debug("stop dsf final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
                            } catch (Exception e) {
                                logger.error("DSF程序停止失败，失败原因: ", e);
                                resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
                                resultMap.put(Constant.RST_STR, "DSF failed to stop, cause: " + e.getMessage());
                                resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
                                resultList.add(resultMap);
                                if (threadLogger != null) {
                                    threadLogger.error("stop dsf failed, cause by: ", e);
                                    threadLogger.info("ret result: " + resultMap.toString());
                                    long endProTimes = System.currentTimeMillis();
                                    long totalProTimes = (endProTimes - startProTimes)/1000;
                                    threadLogger.debug("stop dsf final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
                                }
                            }
                        }
                    });
                }

                //轮询等待所有的线程执行完成
                while(resultList.size() < param.size()){
                    logger.debug("本次总停止DSF进程数:" + param.size() + ", 已经停止完成DSF进程数:" + resultList.size());
                    SingletonThreadPool.getExecutorService();
                    Thread.sleep(2000);
                }
                Long endTimes = System.currentTimeMillis();
                Long totalTimes = (endTimes - startTimes)/1000;
                logger.info("DSF进程停止完成，本次停止DSF进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

                for(int i = 0 ; i < resultList.size();i++){
                    Map<String, Object> singleMap = new HashMap<String, Object>();
                    Map<String,String> resultMap = resultList.get(i);
                    if(Constant.RST_CODE_SUCCESS.equalsIgnoreCase(resultMap.get(Constant.RST_CODE))){
                        //成功信息添加到List返回到前台
                        singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 停止成功\n" + resultMap.get(Constant.RST_STR));

                        //停止成功，修改实例状态
                        Map<String, String> updateParams = new HashMap<String, String>();
                        updateParams.put("INST_ID", resultMap.get("INST_ID"));
                        updateParams.put("STATUS", Constant.STATE_NOT_ACTIVE);
                        coreService.updateObject("instConfig.updateDcfDeployInstConfig", updateParams, dbKey);
                    }else{
                        singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 停止失败\n" + resultMap.get(Constant.RST_STR));
                    }
                    singleMap.put(Constant.RST_CODE, resultMap.get(Constant.RST_CODE));
                    resultMsgList.add(singleMap);
                }

                //返回结果信息
                StringBuffer rstBuffer = new StringBuffer();
                String rstCode = Constant.RST_CODE_SUCCESS;
                for (int i=0; i<resultList.size(); i++) {
                    Map<String, Object> hostRstMap = resultMsgList.get(i);
                    rstBuffer.append(hostRstMap.get(Constant.RST_STR)).append(Constant.LINE_FLAG);
                    if (Constant.RST_CODE_FAILED.equalsIgnoreCase(StringTool.object2String(hostRstMap.get(Constant.RST_CODE)))) {
                        rstCode = Constant.RST_CODE_FAILED;
                    }
                }
                rstMap.put(Constant.RST_STR, rstBuffer.toString());
                rstMap.put(Constant.RST_CODE, rstCode);
            } catch(Exception e){
                logger.error("停止Zookeeper失败， 失败原因: ", e);
            }
        }
        return rstMap;
    }
}
