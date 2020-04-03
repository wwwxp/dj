package com.tydic.job;

import com.alibaba.jstorm.ui.model.ZookeeperNode;
import com.alibaba.jstorm.ui.utils.ZookeeperManager;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.monitormanager.clustersummary.impl.ClusterSummaryServiceImpl;
import com.tydic.util.Constant;
import com.tydic.util.PropertiesUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.SftpTran;
import com.tydic.util.ftp.Trans;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@Component("TaskConfigInit")
public class TaskConfigInit {

    private static Logger log = Logger.getLogger(TaskConfigInit.class);

    @Autowired
    private CoreService coreService;

    /**
     * ZK服务组节点列表
     */
    private static final String ZK_SERVICE_REGIST_PATH = "localservice/regist";

    private static final String FILE_SWITCH_CLOSED = "1";

    // 组件名称
    private static final String COMPONENT_TYPE_NIMBUS = "nimbus";
    private static final String COMPONENT_TYPE_JSTORM = "jstorm";


//    //配置(秒，分，时，日，月，周)
//    @Scheduled(cron = "*/5 * * * * ?")
    public void queryChartsListNew()throws Exception{

        Map<String,Object> clusterMap = new HashMap<String,Object>();
        clusterMap.put("CLUSTER_TYPE","zookeeper");

        //集群信息
        List<HashMap<String, Object>> clusterList = coreService.queryForList2New("serviceType.queryServiceTypeList",clusterMap, FrameConfigKey.DEFAULT_DATASOURCE);
        if (BlankUtil.isBlank(clusterList)){
            return;
        }

        //返回服务对象
        List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();

        Map<String,Object> param = new HashMap<String,Object>();
        for (int h = 0;h<clusterList.size();h++){
            param.clear();
            String CLUSTER_ID = clusterList.get(h).get("CLUSTER_ID").toString();
            param.put("CLUSTER_ID", CLUSTER_ID);
            String clusterName = StringTool.object2String(clusterList.get(h).get("CLUSTER_NAME"));


            //查询当前集群所有服务列表
            List<Map<String, Object>> serviceList = this.queryZkServiceList(param, FrameConfigKey.DEFAULT_DATASOURCE);
            if (BlankUtil.isBlank(serviceList)) {
                continue;
            }

            //统计每个服务节点数据
            for (int i=0; i<serviceList.size(); i++) {
                Map<String, Object> rstMap = new HashMap<String, Object>();

                String serviceName = StringTool.object2String(serviceList.get(i).get("SERVICE_NAME"));
                //服务名称
                rstMap.put("SERVICE_NAME", serviceName);
                //统计每个服务名称对应的详细数据
                Map<String, Object> queryMap = new HashMap<String, Object>();
                queryMap.put("CLUSTER_ID", CLUSTER_ID);
                queryMap.put("SERVICE_NAME", serviceName);
                List<Map<String, Object>> serviceDataList = this.queryZkServiceDataList(queryMap, FrameConfigKey.DEFAULT_DATASOURCE);
                log.debug("服务积压情况，图表展示， 服务对应节点信息: " + serviceDataList + ", 服务名称: " + serviceName);

                int totalExecQueneSize = 0;

                HashSet<String> hostIpList = new HashSet<String>();
                if (!BlankUtil.isBlank(serviceDataList)) {
                    for (int j=0; j<serviceDataList.size();j++) {

                        //执行队列大小(一级缓存积压)
                        String execQueneSizeStr = StringTool.object2String(serviceDataList.get(j).get("EXEC_QUENE_SIZE"));
                        int execQueneSize = BlankUtil.isBlank(execQueneSizeStr) ? 0 : Integer.parseInt(execQueneSizeStr);

                        //二级缓存积压
                        String fileQueueSizeStr = StringTool.object2String(serviceDataList.get(j).get("FILE_QUEUE_SIZE"));
                        int fileQueueSize = BlankUtil.isBlank(fileQueueSizeStr) ? 0 : Integer.parseInt(fileQueueSizeStr);

                        totalExecQueneSize += (execQueneSize + fileQueueSize);

                        //获取主机
                        String hostIp = StringTool.object2String(serviceDataList.get(j).get("HOST_IP"));
                        hostIpList.add(hostIp);
                    }
                }

                //服务关联主机数量
                rstMap.put("HOST_SIZE", hostIpList.size());
                //服务执行队列总大小
                rstMap.put("TOTAL_EXEC_QUENE_SIZE", totalExecQueneSize);
                //服务相信信息
                rstMap.put("SERVICE_DATA", serviceDataList);
                //集群名称
                rstMap.put("CLUSTER_NAME", clusterName);
                coreService.insertObject2New("taskService.insertTaskService",rstMap,FrameConfigKey.DEFAULT_DATASOURCE);
            }
        }
    }

    /**
     * 查询ZK集群服务列表
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> queryZkServiceList(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("ZK集群服务列表， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        //根据ZK集群ID查询关联Jstorm集群编码
        List<HashMap<String, Object>> jstormList = coreService.queryForList2New("busRelationClusterList.queryClusterListByZkClusterId", params, dbKey);
        log.debug("zookeeper集群关联的Jstorm集群信息为: " + jstormList);

        List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
        if (!BlankUtil.isBlank(jstormList)) {
            String clusterName = StringTool.object2String(jstormList.get(0).get("CLUSTER_CODE"));
            List<ZookeeperNode> result;
            try {
                clusterName = StringEscapeUtils.escapeHtml(clusterName);
                result = ZookeeperManager.listZKNodes(clusterName, ZK_SERVICE_REGIST_PATH);
            } catch (Exception e) {
                log.error("ZK服务列表失败，失败原因: ", e);
                throw e;
            }
            if (!BlankUtil.isBlank(result)) {
                for (ZookeeperNode zkNode : result) {
                    Map<String, Object> nodeMap = new HashMap<String, Object>();
                    nodeMap.put("SERVICE_NAME", zkNode.getName());
                    serviceList.add(nodeMap);
                }
            }
            log.debug("ZK集群服务列表获取结束，服务列表:" + serviceList);
        }

        return serviceList;
    }

    /**
     * 查询ZK服务节点信息
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> queryZkServiceDataList(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("ZK服务节点数据，参数: " + params.toString() + ", dbKey: " + dbKey);
        //根据ZK集群ID查询关联Jstorm集群编码
        List<HashMap<String, Object>> jstormList = coreService.queryForList2New("busRelationClusterList.queryClusterListByZkClusterId", params, dbKey);
        log.debug("zookeeper集群关联的Jstorm集群信息为: " + jstormList);

        List<Map<String, Object>> serviceDataList = new ArrayList<Map<String, Object>>();
        if (!BlankUtil.isBlank(jstormList)) {
            String clusterName = StringTool.object2String(jstormList.get(0).get("CLUSTER_CODE"));

            //服务名称
            String serviceName = StringTool.object2String(params.get("SERVICE_NAME"));
            String servicePath = ZK_SERVICE_REGIST_PATH + "/" + serviceName;

            List<ZookeeperNode> result;
            try {
                clusterName = StringEscapeUtils.escapeHtml(clusterName);
                result = ZookeeperManager.listZKNodes(clusterName, servicePath);
            } catch (Exception e) {
                log.error("ZK服务列表失败原因: ", e);
                throw e;
            }
            if (!BlankUtil.isBlank(result)) {
                for (ZookeeperNode zkNode : result) {
                    String nodeName = zkNode.getName();
                    String nodePath = servicePath + "/" + nodeName;
                    String nodeData = ZookeeperManager.getZKNodeData(clusterName, nodePath);
                    if (!BlankUtil.isBlank(nodeData)) {
                        String [] nodeList = nodeData.split(";");
                        for (String node : nodeList) {
                            //任务名称，任务Id, c进程号， pending大小， 执行队列大小，filequeue大小，发送消息总量(向c进程)
                            String [] nodeStr = node.split(",");

                            Map<String, Object> taskNodeMap = new HashMap<String, Object>();
                            taskNodeMap.put("TASK_NAME", nodeStr[0]);
                            taskNodeMap.put("TASK_ID", nodeStr[1]);
                            taskNodeMap.put("C_PRO_ID", nodeStr[2]);
                            taskNodeMap.put("PENDING_SIZE", nodeStr[3]);
                            taskNodeMap.put("EXEC_QUENE_SIZE", nodeStr[4]);
                            taskNodeMap.put("FILE_QUEUE_SIZE", nodeStr[5]);
                            taskNodeMap.put("MSG_COUNT", nodeStr[6]);
                            taskNodeMap.put("HOST_IP", nodeName);
                            serviceDataList.add(taskNodeMap);
                        }
                    }
                }

            }
        }
        log.debug("ZK服务积压量获取结束， 总记录数: " + serviceDataList.size());
        return serviceDataList;
    }

//    @PostConstruct
    public void initYaml(){
        // 获取Jstorm配置文件
        String filePath = null;
        try {
            filePath = URLDecoder.decode(ClusterSummaryServiceImpl.class.getResource("/").getPath(), "UTF-8") + "/storm.yaml";

            // 是否自定义配置文件路径
            String fileSwitch = Constant.FILE_SWITCH;
            if (FILE_SWITCH_CLOSED.equals(fileSwitch)) {
                filePath = Constant.STORAM_YAML_PATH;
            }
            //同步配置文件
            initStormYaml(filePath);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步Yaml配置文件
     */
    public void initStormYaml(String filePath, String ... args) {
        try {
            synchronized (ClusterSummaryServiceImpl.class) {
                log.debug("解析Nimbus Yaml配置文件开始, 本地配置文件路径: " + filePath);
                Map<String, Object> yamlMap = new HashMap<String, Object>();
                List<Map<String, Object>> yamlList = new ArrayList<Map<String, Object>>();

                // 获取ZK集群信息
                Map<String, Object> queryMap = new HashMap<String, Object>();
                queryMap.put("DEPLOY_FILE_TYPE", COMPONENT_TYPE_NIMBUS);
                queryMap.put("DEPLOY_TYPE", COMPONENT_TYPE_JSTORM);
                List<HashMap<String, Object>> nimbusList = coreService.queryForList2New("clusterConfig.queryNimbusClusterList",
                        queryMap, FrameConfigKey.DEFAULT_DATASOURCE);
                if (!BlankUtil.isBlank(nimbusList)) {
                    for (int i = 0; i < nimbusList.size(); i++) {
                        String hostIp = StringTool.object2String(nimbusList.get(i).get("HOST_IP"));
                        String sshPort = StringTool.object2String(nimbusList.get(i).get("SSH_PORT"));
                        String sshUser = StringTool.object2String(nimbusList.get(i).get("SSH_USER"));
                        String sshPwd = StringTool.object2String(nimbusList.get(i).get("SSH_PASSWD"));
                        String yamlPath = StringTool.object2String(nimbusList.get(i).get("FILE_PATH"));
                        if (!BlankUtil.isBlank(sshPwd)) {
                            sshPwd = DesTool.dec(sshPwd);
                        } else {
                            log.debug("请配置主机密码, 主机: " + hostIp);
                            continue;
                        }


                        try{
                            //登录Nimbus所在主机解析Yaml配置文件
                            Trans trans = new SftpTran(hostIp, Integer.parseInt(sshPort), sshUser, sshPwd, 8000);
                            trans.login();
                            InputStream in = trans.get(yamlPath);
                            Yaml yaml = new Yaml();
                            Map<String, Object> remoteYamlMap = (Map<String, Object>) yaml.load(in);
                            in.close();
                            trans.completePendingCommand();
                            Object zkServers = remoteYamlMap.get("storm.zookeeper.servers");
                            Object zkRoot = remoteYamlMap.get("storm.zookeeper.root");
                            Object zkPort = remoteYamlMap.get("storm.zookeeper.port");
                            log.debug("获取远程Nimbus配置文件信息: zkServers: "
                                    + zkServers.toString() + ", zkRoot: " + zkRoot
                                    + ", zkPort: " + zkPort);

                            //判断该Nimbus是否为备机(主备配置文件一直), 直接跳过
                            if (!BlankUtil.isBlank(yamlList)) {
                                boolean isExists = false;
                                for (int j = 0; j < yamlList.size(); j++) {
                                    String oldZkRoot = StringTool.object2String(yamlList.get(j).get("zkRoot")).trim();
                                    log.debug("ZK根目录： " + zkRoot + "， 上一个ZK根目录： " + oldZkRoot);
                                    String newZkRoot = StringTool.object2String(zkRoot).trim();
                                    ;
                                    if (oldZkRoot.equalsIgnoreCase(newZkRoot)) {
                                        isExists = true;
                                        break;
                                    }
                                }
                                if (isExists) {
                                    continue;
                                }
                            }

                            Map<String, Object> tempMap = new HashMap<String, Object>();
                            tempMap.put("name", StringTool.object2String(zkRoot).replaceAll("/", ""));
                            tempMap.put("zkRoot", zkRoot);
                            tempMap.put("zkPort", zkPort);
                            tempMap.put("zkServers", zkServers);

                            yamlList.add(tempMap);
                        }catch (Exception e){
                            continue;
                        }
                    }
                    yamlMap.put("ui.clusters", yamlList);
                    log.debug("生成的内容：" + yamlMap);
                    PropertiesUtils.updateYamlFile(filePath, yamlMap);
                }
                log.debug("Yaml配置文件同步完成...");
            }
        } catch (Exception e) {
            log.error("解析Yaml配置文件失败", e);
        }
    }

}
