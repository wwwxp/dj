package com.tydic.service.nodemanager.impl;

import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodemanager.NodeManagerService;

import com.tydic.service.versiondeployment.dao.VersionOptDao;
import com.tydic.service.versiondeployment.util.NodeVerUtil;
import com.tydic.util.*;
import com.tydic.util.ftp.FileTool;
import org.apache.commons.collections.map.HashedMap;
import org.apache.xmlbeans.impl.regex.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author 王贤朋
 *
 */
@Service
public class NodeManagerServiceImpl implements NodeManagerService {

    /**
     * 核心Service对象
     */
    @Resource
    private CoreService coreService;

    private static Logger log = LoggerFactory.getLogger(NodeManagerServiceImpl.class);

    /*
    * 判断NODE_TYPE的数据类型是否正确
    * */

    public boolean nodeTypeRight(String nodeType){
        Pattern nodeTypeReg=Pattern.compile("^[\\w-]+$");

        Matcher matcher = nodeTypeReg.matcher(nodeType);

        return matcher.matches();
    }

    /**
     * 单个的添加节点配置
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> insertNode(Map<String,Object> params, String dbKey) throws Exception {

        Map<String,Object> result=new HashMap<String,Object>();

        String nodeType=StringTool.object2String(params.get("NODE_TYPE"));
        String nodePath=StringTool.object2String(params.get("NODE_PATH"));

        boolean isRight=nodeTypeRight(nodeType);

        log.debug("判断业务类型字段是否合法，结果为："+isRight);
        if(!isRight){
            result.put("effectRow",-1);
            result.put("errorMsg","节点类型不能含有特殊字符");
            return result;
        };

        boolean checkNodePath=nodePathValidate(nodePath);
        log.debug("判断节点路径字段是否合法，结果为："+checkNodePath);

        if(!checkNodePath){
            result.put("effectRow",-1);
            result.put("errorMsg","节点路径必须为二级目录及以上的合法的绝对路径！");
            return result;
        };

        List<Map<String,Object>> rows=coreService.queryForList3New("nodeManagerMapper.queryNodeByHostIdAndNodePath",params,dbKey);

        log.debug("判断节点信息是否重复，查询结果为："+rows);
        if(rows!=null && rows.size()>0){
            result.put("effectRow",-1);
            result.put("errorMsg","节点信息重复，节点主机和节点路径为唯一性条件，请重新输入！");
            return result;
        }

        //判断是否为web程序
        if(isRunWeb(nodeType,dbKey)){
            String temp_dir= NodeVerUtil.getTomcatTempPath();
            params.put("START_CMD",temp_dir+"/bin/start.sh");
            params.put("STOP_CMD",temp_dir+"/bin/stop.sh");
            params.put("CHECK_CMD",temp_dir+"/bin/check.sh");
        }else{
            params.put("START_CMD","bin/startup.sh");
            params.put("STOP_CMD","bin/stop.sh");
            params.put("CHECK_CMD","bin/check.sh");
        }

        int rowNumber=coreService.insertObject2New("nodeManagerMapper.insertNode",params,dbKey);
        result.put("effectRow",rowNumber);

        log.debug("节点插入成功");

        return result;
    }

    private boolean isRunWeb(String nodeTypeId,String dbKey) throws Exception{
        Map<String,Object> params=new HashMap<>();
        params.put("NODE_TYPE_ID",nodeTypeId);

        Map<String,Object> runWebMap = coreService.queryForObject2New("nodeManagerMapper.queryNodeTypeInfoById",params,dbKey);

        return StringTool.object2String(runWebMap.get("RUN_WEB")).equals(NodeConstant.RUN_WEB);
    }

    /**
     * 批量新增节点配置信息
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> insertBatchNode(Map<String,Object> params,String dbKey) throws Exception{
        Map<String,Object> result=new HashMap<String,Object>();

        //对主机IP、节点类型进行随意组合，并判断是否存在，并添加不存在的Node
        String[] hostIpIds=StringTool.object2String(params.get("HOST_IP_USER")).split(",");
        String[] nodeTypeIds=StringTool.object2String(params.get("NODE_TYPE")).split(",");

        params.put("HOST_IP_USER",Arrays.asList(hostIpIds));
        params.put("NODE_TYPE",Arrays.asList(nodeTypeIds));
        //用于判断节点是否重复
        String nodePath=StringTool.object2String(params.get("NODE_PATH"));

        boolean checkNodePath=nodePathValidate(nodePath);
        log.debug("判断节点路径字段是否合法，结果为："+checkNodePath);

        if(!checkNodePath){
            result.put("effectRow",0);
            result.put("errorMsg","节点路径必须为二级目录及以上的合法的绝对路径！");
            return result;
        };

        //查询出对应IP地址，从而可以通过HOST_ID，获得HOST_IP、SSH_USER
        List<Map<String,Object>> hostInfo=coreService.queryForList3New("nodeManagerMapper.queryHostInfoById",params,dbKey);

        //查询出节点类型信息，从而可以获得Code，来对NodeName进行解析(eg.把$hostip$替换为HOST_IP)
        List<Map<String,Object>> nodeTypeInfo=coreService.queryForList3New("nodeManagerMapper.queryNodeTypeByNodeTypeId",params,dbKey);

        //查询对节点信息表中，所有已经存在的对应的主机ID、节点路径（用来判断节点是否重复）
        List<Map<String,Object>> hostIdNodePath=coreService.queryForList3New("nodeManagerMapper.queryNodeHostIdAndNodePath",params,dbKey);

        //获得节点名称
        String nodeName=StringTool.object2String(params.get("NODE_NAME"));

        Map<String,Object> hostRow=null;
        int effectRow=0;
        int count=hostIpIds.length*nodeTypeIds.length;
        boolean isExists=true;
        String newNodeName=null;
        String hostIp=null;

        String temp_dir= NodeVerUtil.getTomcatTempPath();

        //批量进行新增，不合法的不会进行新增
        for(String hostIpId:hostIpIds){
            for(String nodeTypeId:nodeTypeIds){

                //对nodeTypeId进行校验
                boolean isRight=nodeTypeRight(nodeTypeId);

                log.debug("判断业务类型字段是否合法，结果为："+isRight);

                if(!isRight){
                    continue;
                }else{
                    result.put("errorMsg","业务类型字段都不合法！");
                }

                params.put("HOST_IP",hostIpId);
                params.put("NODE_TYPE",nodeTypeId);

                //通过"HOST_ID"获得"HOST信息（ssh_user、host_ip）"，主要用来展示插入结果
                hostRow=queryHostInfoById(hostIpId,hostInfo);

                //判断是否已存在，存在则不会进行插入
                hostIp=StringTool.object2String(hostRow.get("HOST_IP"));
                isExists=existsHostIpAndNodePath(hostIp,nodePath,hostIdNodePath);

                 if(!isExists){       //如果节点信息不存在，则可以进行插入

                     newNodeName=nodeName;

                     if(newNodeName.contains("$hostip$")){
                         newNodeName=newNodeName.replace("$hostip$",hostIp);
                     }
                     if(newNodeName.contains("$user$")){
                         newNodeName=newNodeName.replace("$user$",StringTool.object2String(hostRow.get("SSH_USER")));
                     }
                     if(newNodeName.contains("$program$")){

                         newNodeName=newNodeName.replace("$program$",queryCodeByNodeTypeId(nodeTypeId,nodeTypeInfo));
                     }
                     log.debug("NODE_NMAE解析结束，NODE_NAME："+newNodeName);

                     params.put("NODE_NAME",newNodeName);

                     effectRow++;

                     //判断是否为web程序
                     if(isRunWeb(nodeTypeId,dbKey)){

                         params.put("START_CMD",temp_dir+"/bin/start.sh");
                         params.put("STOP_CMD",temp_dir+"/bin/stop.sh");
                         params.put("CHECK_CMD",temp_dir+"/bin/check.sh");
                     }else{
                         params.put("START_CMD","bin/startup.sh");
                         params.put("STOP_CMD","bin/stop.sh");
                         params.put("CHECK_CMD","bin/check.sh");
                     }

                     coreService.insertObject2New("nodeManagerMapper.insertNode",params,dbKey);
                 }else{
                     result.put("errorMsg","节点信息全部重复，节点主机和节点路径为唯一性条件，请重新输入！");
                 }

            }
        }
        result.put("effectRow",effectRow);
        result.put("failCount",count-effectRow);

        log.debug("批量添加结束，添加的成功的个数为："+effectRow);

        return result;
    }

    /**
     * 在主机信息内存表中，通过id查找host信息
     * @param hostId
     * @param hostInfo
     * @return
     */
    public Map<String,Object> queryHostInfoById(String hostId,List<Map<String,Object>> hostInfo){

        if(hostId==null || hostInfo==null){
            return null;
        }

        for(Map<String,Object> row:hostInfo){
            if(StringTool.object2String(row.get("NODE_HOST_ID")).equals(hostId)){
                return row;
            }
        }

        return null;
    }

    /**
     * 在节点类型信息内存表中，通过ID查询Code
     * @param nodeTypeId
     * @param nodeTypeInfo
     * @return
     */
    public String queryCodeByNodeTypeId(String nodeTypeId,List<Map<String,Object>> nodeTypeInfo){

        if(nodeTypeId==null || nodeTypeInfo==null){
            return null;
        }
        for(Map<String,Object> row:nodeTypeInfo){
            if(StringTool.object2String(row.get("NODE_TYPE_ID")).equals(nodeTypeId)){
                return StringTool.object2String(row.get("CODE"));
            }
        }
        return null;
    }

    /**
     * 在节点信息内存表中（只含NODE_HOST_ID、NODE_PATH两个字段），判断传入的hostId、nodePath是否已经存在
     * @param hostIp
     * @param nodePath
     * @param hostIdNodePaths
     * @return
     */
    public boolean existsHostIpAndNodePath(String hostIp,String nodePath,List<Map<String,Object>> hostIdNodePaths){

        nodePath=FileTool.exactPath(nodePath);

        String existsNodePath=null;
        for(Map<String,Object> hostIdNodePath:hostIdNodePaths){

            existsNodePath=FileTool.exactPath(StringTool.object2String(hostIdNodePath.get("NODE_PATH")));

            if(hostIp.equals(hostIdNodePath.get("HOST_IP")) && nodePath.equals(existsNodePath)){
                return true;
            }
        }

        return false;
    }

    /**
     * 修改节点配置信息
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> updateNode(Map<String,Object> params, String dbKey) throws Exception {

        Map<String,Object> result=new HashMap<String,Object>();

        String nodeTypeId=StringTool.object2String(params.get("NODE_TYPE"));

        //判断该节点程序是否已部署
        log.debug("判断该节点程序是否已部署，业务类型ID为："+nodeTypeId+"，NODE_ID为："+StringTool.object2String(params.get("ID")));
        List<Map<String,Object>> nodeRows=coreService.queryForList3New("nodeManagerMapper.queryNodeByNodeTypeId",params,dbKey);

        if(nodeRows!=null && nodeRows.size()>0){
            result.put("effectRow",-1);
            result.put("errorMsg","该节点程序已部署，无法进行修改！");
            return result;
        }


        boolean isRight=nodeTypeRight(nodeTypeId);

        log.debug("判断业务类型字段是否合法，业务类型ID为："+nodeTypeId);
        if(!isRight){
            result.put("effectRow",-1);
            result.put("errorMsg","节点类型不能含有特殊字符");
            return result;
        };

        boolean checkNodePath=nodePathValidate(StringTool.object2String(params.get("NODE_PATH")));
        log.debug("判断节点路径字段是否合法，结果为："+checkNodePath);

        if(!checkNodePath){
            result.put("effectRow",-1);
            result.put("errorMsg","节点路径必须为二级目录及以上的合法的绝对路径！");
            return result;
        };



        List<Map<String,Object>> rows=coreService.queryForList3New("nodeManagerMapper.queryNodeByHostIdAndNodePath",params,dbKey);

        log.debug("判断节点信息是否重复，查询结果为："+rows);
        if(rows!=null && rows.size()>0){
            result.put("effectRow",-1);
            result.put("errorMsg","节点信息重复，节点主机和节点路径为唯一性条件，请重新输入！");
            return result;
        }

        int rowNumber=coreService.insertObject2New("nodeManagerMapper.updateNode",params,dbKey);
        result.put("effectRow",rowNumber);

        log.debug("节点修改成功");

        return result;
    }


    /**
     * 判断被删除的节点是否已经部署
     * @param nodeId
     * @param nodeTypeId
     * @param nodeTypeTable
     * @return
     */
    public boolean isDeploy(String nodeId,String nodeTypeId,List<Map<String,Object>> nodeTypeTable){

        for (Map<String, Object> nodeTypeRow : nodeTypeTable) {
            if (StringTool.object2String(nodeTypeRow.get("NODE_ID")).equals(nodeId) && StringTool.object2String(nodeTypeRow.get("NODE_TYPE_ID")).equals(nodeTypeId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断被删除的节点是否正在运行
     * @param nodeId
     * @param nodeTypeId
     * @param nodeStartTable
     * @return
     */
    public boolean isRun(String nodeId,String nodeTypeId,List<Map<String,Object>> nodeStartTable){

        for (Map<String, Object> nodeTypeRow : nodeStartTable) {
            if (StringTool.object2String(nodeTypeRow.get("NODE_ID")).equals(nodeId) && StringTool.object2String(nodeTypeRow.get("NODE_TYPE_ID")).equals(nodeTypeId)) {
                if (StringTool.object2String(nodeTypeRow.get("STATE")).equals("1")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断被删除节点的部署、运行
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> deployRunState(List<Map<String,Object>> params, String dbKey) throws Exception{
        Map<String,Object> result=new HashMap<>();

        List<String> nodeIds=new ArrayList<>();

        //获得所有的NODE_ID，用作查询参数
        for(Map<String,Object> row:params){
            nodeIds.add(StringTool.object2String(row.get("ID")));
        }

        Map<String,Object> nodeIdsParam=new HashMap<String,Object>(){
            {put("NODE_IDS",nodeIds);}
        };

        //获得部署表数据
        List<Map<String,Object>> nodeTypeTable = coreService.queryForList3New("nodeManagerMapper.queryNodeTypeTable", nodeIdsParam, dbKey);

        List<Map<String,Object>> nodeStartTable = coreService.queryForList3New("nodeManagerMapper.queryNodeStartTable", nodeIdsParam, dbKey);

        String hostIp = null;
        String nodePath=null;
        String nodeId=null;
        String nodeTypeId=null;

        boolean isDeploy=false;
        boolean isRun=false;

        //判断是否节点是否部署
        for(Map<String,Object> nodeRow:params){

            hostIp = StringTool.object2String(nodeRow.get("HOST_IP"));
            nodePath = StringTool.object2String(nodeRow.get("NODE_PATH"));

            nodeId=StringTool.object2String(nodeRow.get("ID"));
            nodeTypeId=StringTool.object2String(nodeRow.get("NODE_TYPE_ID"));

            //判断是否部署
            isDeploy=isDeploy(nodeId,nodeTypeId,nodeTypeTable);

            //判断是否为运行状态
            isRun=isRun(nodeId,nodeTypeId,nodeStartTable);

            if(isDeploy && isRun){
                result.put("deploy", true);
                result.put("running", true);
                result.put("msg", hostIp + "-" + nodePath + "节点程序已部署且正在运行，无法进行删除！");
                break;

            }else if(isDeploy && !isRun){
                result.put("deploy", true);
                result.put("running", false);
                result.put("msg", "选中的节点中，有节点程序已部署未运行，是否确认删除！");
                break;
            }
        }


        return result;
    }

    /**
     * linux路径为二级及以上的目录返回true
     * @param nodePath
     * @return
     */
    private boolean nodePathValidate(String nodePath){

        //linux的二级目录及以上的匹配
        Pattern reg = Pattern.compile("^/[^/]+/[^/]+(/[^/]+)*/?$");

        Matcher matcher = reg.matcher(nodePath);

        return matcher.matches();
    }

    private Map<String,Object> queryLoginInfoOnHost(String hostId,List<Map<String,Object>> hostTable){

        for(Map<String,Object> row:hostTable){
            if(StringTool.object2String(row.get("HOST_ID")).equals(hostId)){
                return row;
            }

        }
        return null;
    }

    /**
     * 删除节点配置信息
     * @param userName
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> deleteNode(String userName,List<Map<String,Object>> params,String dbKey) throws Exception{

        Map<String,Object> result=new HashMap<>();

        //记录插入到日志所需的参数
        Map<String,Object> logParam=new HashMap<>();
        String logContentModule="用户${user}删除节点${nodeName}，节点主机:${hostip}(${userName})，删除节点目录:${path}";
        String logContent=null;
        String nodePath=null;
        String hostIp=null;
        Map<String,Object> loginInfo=null;
        String shellCommandModule="rm -rf ${NODE_PATH}";
        String shellCommand=null;
        String execRes=null;
        int effectCount=0;

        //获得传入的所有的HOST_ID,从而构成查询参数
        List<String> hostIds=new ArrayList<>();
        for(Map<String,Object> row:params){

            hostIds.add(StringTool.object2String(row.get("NODE_HOST_ID")));
        }

        Map<String,Object> hostIdsParam=new HashMap<>();
        hostIdsParam.put("HOST_IDS",hostIds);
        List<Map<String,Object>> hostInfo=coreService.queryForList3New("nodeManagerMapper.queryLoginInfoByIds",hostIdsParam,dbKey);

        String pwd=null;

        for(Map<String,Object> rowParams:params){

            nodePath=StringTool.object2String(rowParams.get("NODE_PATH"));
            hostIp=StringTool.object2String(rowParams.get("HOST_IP"));

            loginInfo=queryLoginInfoOnHost(StringTool.object2String(rowParams.get("NODE_HOST_ID")),hostInfo);

            //节点对应程序、配置文件的删除
            pwd=DesTool.dec(StringTool.object2String(loginInfo.get("SSH_PASSWD")));
            ShellUtils shellClient=new ShellUtils(StringTool.object2String(loginInfo.get("HOST_IP")),StringTool.object2String(loginInfo.get("SSH_USER")),pwd);

            if(!nodePathValidate(nodePath)){
                result.put("effectRow",-1);
                result.put("msg","删除失败，路径必须为2级以上的合法的绝对路径");
                return result;
            }

            shellCommand=shellCommandModule.replace("${NODE_PATH}",nodePath);
            execRes=shellClient.execMsg(shellCommand);
            log.debug("配置文件的删除完成");

           //DCF_NODE_CONFIG表中数据的删除
            effectCount+=coreService.deleteObject2New("nodeManagerMapper.delNode",rowParams,dbKey);

            //关联表的删除
            effectCount+=coreService.deleteObject2New("nodeManagerMapper.delNodeDeployList",rowParams,dbKey);
            effectCount+=coreService.deleteObject2New("nodeManagerMapper.delNodeStartList",rowParams,dbKey);
            effectCount+= coreService.deleteObject2New("nodeManagerMapper.delNodeClusterEleConfig",rowParams,dbKey);
            log.debug("4个表的删除成功,总共删除的记录数："+effectCount);

            //删除成功，则记录日志到dcf_node_operator_log表中
            logContent=logContentModule.replace("${user}",userName);
            logContent=logContent.replace("${nodeName}",StringTool.object2String(rowParams.get("NODE_NAME")));
            logContent=logContent.replace("${hostip}",StringTool.object2String(rowParams.get("HOST_IP")));
            logContent=logContent.replace("${userName}",StringTool.object2String(rowParams.get("SSH_USER")));
            logContent=logContent.replace("${path}",nodePath);

            logParam.put("OPERATOR_MODULE",OperatorModule.NODE_CONFIG.getComment());
            logParam.put("OPERATOR_NAME",OperatorName.DEL_NODE.getComment());
            logParam.put("CREATED_USER",userName);
            logParam.put("LOG_CONTENT",logContent);

            coreService.insertObject2New("nodeManagerMapper.insertNodeHandleInfo",logParam,dbKey);
            log.debug("记录删除操作到日志表中成功");
        }

        result.put("effectRow",effectCount);

        return result;

    }
}
