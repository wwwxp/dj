package com.tydic.service.nodemanager.impl;

import clojure.lang.Obj;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.nodemanager.NodeTypeManagerService;
import com.tydic.util.OperatorModule;
import com.tydic.util.OperatorName;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Wxp
 *
 */
@Service
public class NodeTypeManagerServiceImpl implements NodeTypeManagerService {

    /**
     * 核心Service对象
     */
    @Resource
    private CoreService coreService;

    private static Logger log = LoggerFactory.getLogger(NodeTypeManagerServiceImpl.class);

    public boolean checkCode(String code){

        Pattern pattern=Pattern.compile("^[\\w-]+$");
        Matcher matcher = pattern.matcher(code);

        return matcher.matches();
    }

    public boolean checkVersion(String version){
        Pattern pattern=Pattern.compile("^[1-9][0-9]*\\.[0-9]\\.[0-9]$");
        Matcher matcher = pattern.matcher(version);

        return matcher.matches();
    }

    /**
     * 程序类型的新增
     * @param userName
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> insertNodeType(String userName,Map<String, Object> params, String dbKey) throws Exception {

        Map<String,Object> result=new HashMap<>();

        String nodeTypeName=StringTool.object2String(params.get("NAME"));
        String code=StringTool.object2String(params.get("CODE"));

        String startVersion=StringTool.object2String(params.get("START_VERSION"));

        log.debug("对程序名称、程序编码进行校验，程序名称为："+nodeTypeName+"，程序编码为："+code);
        if (BlankUtil.isBlank(nodeTypeName) || BlankUtil.isBlank(code)) {

            result.put("errorMsg", "程序名称或程序编码不能为空！");
            return result;
        } else {
            if (!checkCode(code)) {
                result.put("errorMsg", "程序编码只能由字母、数字、-、下划线组成！");
                return result;
            }
        }

        log.debug("判断程序编码是否已经存在");
        Map<String,Object> codeCount=coreService.queryForObject2New("nodeTypeManagerMapper.queryCodeCount",params,dbKey);

        if(Integer.parseInt(StringTool.object2String(codeCount.get("CODE_COUNT")))>=1){

            result.put("errorMsg","程序编码已存在，请重新输入！");
            return result;
        }

        if(!checkVersion(startVersion)){
            result.put("errorMsg", "开始版本号的格式不正确，请重新输入！");
            return result;
        }

        log.debug("正在进行程序类型的插入");
        params.put("USER_NAME",userName);
        int effectRow=coreService.insertObject2New("nodeTypeManagerMapper.nodeTypeInsert",params,dbKey);

        result.put("effectRow",effectRow);

        return result;
    }

    /**
     * 更新程序类型配置
     * @param userName
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> updateNodeType(String userName,Map<String,Object> params,String dbKey) throws Exception{
        Map<String,Object> result=new HashMap<>();
        result.put("effectRow",-1);

        //首先判断要修改的程序类型是否已经部署，已经部署则不进行修改
        Map<String,Object> nodeTypeCount=coreService.queryForObject2New("nodeTypeManagerMapper.queryDeployTableByNodeTypeId",params,dbKey);

        if(Integer.parseInt(StringTool.object2String(nodeTypeCount.get("NODE_TYPE_COUNT")))>0){
            result.put("errorMsg","该程序类型已经部署，不能进行修改！");
            return result;
        }

        String startVersion=StringTool.object2String(params.get("START_VERSION"));
        String nodeTypeName=StringTool.object2String(params.get("NAME"));
        String code=StringTool.object2String(params.get("CODE"));

        log.debug("对程序名称、程序编码进行校验，程序名称为："+nodeTypeName+"，程序编码为："+code);
        if (BlankUtil.isBlank(nodeTypeName) || BlankUtil.isBlank(code)) {

            result.put("errorMsg", "程序名称或程序编码不能为空！");
            return result;
        } else {
            if (!checkCode(code)) {
                result.put("errorMsg", "程序编码只能由字母、数字、-、下划线组成！");
                return result;
            }
        }

        log.debug("判断程序编码是否已经存在");
        Map<String,Object> codeCount=coreService.queryForObject2New("nodeTypeManagerMapper.queryCodeCount",params,dbKey);

        if(Integer.parseInt(StringTool.object2String(codeCount.get("CODE_COUNT")))>=1){

            result.put("errorMsg","程序编码已存在，请重新输入！");
            return result;
        }

        if(!checkVersion(startVersion)){
            result.put("errorMsg", "开始版本号的格式不正确，请重新输入！");
            return result;
        }

        log.debug("正在进行程序类型的更新");
        params.put("USER_NAME",userName);

        int effectRow=coreService.updateObject2New("nodeTypeManagerMapper.nodeTypeUpdate",params,dbKey);
        result.put("effectRow",effectRow);

        return result;
    }

    /**
     * 判断要被删除的多个程序类型配置中，是否有正在运行的
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> beingUsed(List<Map<String,Object>> params, String dbKey) throws Exception{
        Map<String,Object> result=new HashMap<>();
        result.put("using",false);

        Map<String,Object> runCount=null;

        for(Map<String,Object> nodeTypeParam:params){

            runCount=coreService.queryForObject2New("nodeTypeManagerMapper.queryRunningNumberByNodeTypeId",nodeTypeParam,dbKey);

            if(Integer.parseInt(StringTool.object2String(runCount.get("NODE_TYPE_RUN_COUNT")))>0){

                log.debug("选中的程序类型中，其程序正在运行，请先停止程序！");
                result.put("using",true);
                return result;
            }


        }


        return result;
    }

    /**
     * 从含有“NODE_TYPE_ID”字段的内存表中，获得该内存表中的一行记录
     * @param nodeTypeId
     * @param table
     * @return
     */
    public List<Map<String,Object>> getRowsByNodeTypeId(String nodeTypeId,List<Map<String,Object>> table){

        List<Map<String,Object>> queryResult=new ArrayList<>();

        for(Map<String,Object> row:table){

            if(StringTool.object2String(row.get("NODE_TYPE_ID")).equals(nodeTypeId)){

                queryResult.add(row);
            }
        }
        return queryResult;
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

    private Map<String,String> getVersionPublishInfo(String dbKey){
        Map<String,Object> param=new HashMap<>();
        param.put("FTP_IP","FTP_IP");
        param.put("FTP_USERNAME","FTP_USERNAME");
        param.put("FTP_PASSWD","FTP_PASSWD");
        param.put("FTP_ROOT_PATH","FTP_ROOT_PATH");

        List<Map<String,Object>> publishList=coreService.queryForList3New("nodeTypeManagerMapper.queryVersionPublishInfo",param,dbKey);

        Map<String,String> publishInfo = new HashMap<>();
        for(Map<String,Object> row:publishList){
            publishInfo.put(StringTool.object2String(row.get("CONFIG_NAME")),StringTool.object2String(row.get("CONFIG_VALUE")));
        }

        return publishInfo;
    }

    /**
     * 删除程序类型
     * @param userName
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String,Object> deleteNodeType(String userName,List<Map<String,Object>> params,String dbKey) throws Exception{
        Map<String,Object> result=new HashMap<>();
        result.put("effectRow", -1);

        String logContentModule="用户${user}删除程序${typeName}（${typeCode}）；删除版本发布服务器目录:${versionPath}；删除节点名称:${nodeName}，节点主机${hostIp}(${userName})；节点目录:${path}";
        String nodePath=null;
        String versionPath=null;
        String version=null;
        String hostIp=null;
        String sshUser=null;
        String nodeTypeId=null;
        String logContent=null;
        Map<String,Object> logParam=new HashMap<>();

        //获得传进来的所有NODE_TYPE_ID,并构成查询参数
        List<String> nodeTypeIds=new ArrayList<>();
        for(Map<String,Object> row:params){
            nodeTypeIds.add(StringTool.object2String(row.get("ID")));
        }
        Map<String,Object> nodeTypeIdsParam=new HashMap<>();
        nodeTypeIdsParam.put("NODE_TYPE_IDS",nodeTypeIds);

        //通过NODE_TYPE_ID，获取
        //      1）dcf_node_config表的“NODE_NAME、NODE_HOST_ID、NODE_PATH”字段
        //      2）dcf_host表的“HOST_IP、SSH_USER、SSH_PASSWD”字段
        List<Map<String,Object>> nodeHostTable=coreService.queryForList3New("nodeTypeManagerMapper.queryDelInfoOfNodeHost",nodeTypeIdsParam,dbKey);

        //获取dcf_node_type_version_list表的“FILE_PATH、VERSION”字段
        List<Map<String,Object>> nodeVersionTable=coreService.queryForList3New("nodeTypeManagerMapper.queryDelInfoOfVersion",nodeTypeIdsParam,dbKey);

        List<Map<String,Object>> versionInfo=null;

        //一个NODE_TYPE对应的多台主机，要对多台主机都进行删除
        List<Map<String,Object>> nodeHostInfo=null;

        ShellUtils shellClient=null;
        String pwd=null;
        String shellCommandModule="rm -rf ${PATH}";
        String shellCommand=null;
        String execRes=null;
        String code=null;

        //获得版本发布服务器信息，并创建其客户端、删除目录的命令
        Map<String,String> publishInfo=getVersionPublishInfo(dbKey);
        ShellUtils pubClient=new ShellUtils(publishInfo.get("FTP_IP"),
                                            publishInfo.get("FTP_USERNAME"),
                                            publishInfo.get("FTP_PASSWD"));
        //部署的版本包目录
        String deploy_dir = SystemProperty.getContextProperty("node.manager.deploy_dir");
        deploy_dir = BlankUtil.isBlank(deploy_dir) ? "node_data" : deploy_dir;

        //删除版本包的命令
        String delDataCommandModule="cd "+FileTool.exactPath(publishInfo.get("FTP_ROOT_PATH"))+deploy_dir+";rm -rf ${node_type_code};";

        //删除release目录的命令
        String delConfigCommandModule="cd "+FileTool.exactPath(publishInfo.get("FTP_ROOT_PATH"))+"release;rm -rf ${node_type_code};";
        String delDataCommand=null;
        String delReleaseCommand=null;

        for (Map<String, Object> row : params) {

            nodeTypeId=StringTool.object2String(row.get("ID"));

            //获得程序类型对应的Node信息、host信息
            nodeHostInfo=getRowsByNodeTypeId(nodeTypeId,nodeHostTable);
            //版本发布服务器的删除(一个NODE_TYPE可能对应多个版本，所以删除版本目录的父目录即可删除多个版本)
            versionInfo=getRowsByNodeTypeId(nodeTypeId,nodeVersionTable);

            //对应的主机都要进行删除
            if(nodeHostInfo.size()>0) {
                log.debug("删除版本目录发布服务器、部署节点程序、配置文件，并且记录日志表");
                for (Map<String, Object> nodeHostRow : nodeHostInfo) {
                    //删除版本目录发布服务器
                    code=StringTool.object2String(row.get("CODE"));
                    delDataCommand=delDataCommandModule.replace("${node_type_code}",code);
                    delReleaseCommand=delConfigCommandModule.replace("${node_type_code}",code);

                    execRes=pubClient.execMsg(delDataCommand);
                    log.debug("程序版本类型的编码为："+code+"，版本目录的删除命令为："+delDataCommand+"，删除结果为："+execRes);
                    execRes=pubClient.execMsg(delReleaseCommand);
                    log.debug("配置目录的删除命令为："+delReleaseCommand+"，删除结果为："+execRes);

                    log.debug("版本目录的删除完成");
                    //创建对应主机的客户端
                    hostIp = StringTool.object2String(nodeHostRow.get("HOST_IP"));
                    sshUser = StringTool.object2String(nodeHostRow.get("SSH_USER"));
                    pwd = DesTool.dec(StringTool.object2String(nodeHostRow.get("SSH_PASSWD")));
                    shellClient = new ShellUtils(hostIp, sshUser, pwd);

                    //一个程序类型对应多个版本目录，删除版本目录的父目录，即可删除多个版本目录
                    if (versionInfo.size() > 0) {
                        versionPath = StringTool.object2String(versionInfo.get(0).get("FILE_PATH"));
                        version = StringTool.object2String(versionInfo.get(0).get("VERSION"));
                        versionPath = versionPath.replace(version, "");

                        while (versionPath.lastIndexOf("/") == versionPath.length() - 1) {
                            versionPath = versionPath.substring(0, versionPath.length() - 1);
                        }

                        if (!nodePathValidate(versionPath)) {
                            result.put("msg", "要删除的版本目录的路径必须为二级以上的合法目录");
                            return result;
                        }
                        //把该目录进行删除
                        shellCommand = shellCommandModule.replace("${PATH}", versionPath);
                        execRes = shellClient.execMsg(shellCommand);
                        log.debug("节点目录的删除完成");

                    }

                    //部署节点程序、配置文件的删除(一个NODE_TYPE可能对应NODE)
                    nodePath = StringTool.object2String(nodeHostRow.get("NODE_PATH"));

                    if (!nodePathValidate(nodePath)) {
                        result.put("msg", "要删除的版本目录的路径必须为二级以上的合法目录");
                        return result;
                    }

                    shellCommand = shellCommandModule.replace("${PATH}", nodePath);
                    execRes = shellClient.execMsg(shellCommand);
                    log.debug("部署节点程序的删除完成，删除结果为：" + execRes);

                    //记录删除操作日志到dcf_node_operator_log表
                    logContent = logContentModule.replace("${user}", userName);
                    logContent = logContent.replace("${typeName}", StringTool.object2String(nodeHostRow.get("NODE_TYPE_NAME")));
                    logContent = logContent.replace("${typeCode}", StringTool.object2String(nodeHostRow.get("NODE_TYPE_CODE")));
                    logContent = logContent.replace("${versionPath}", versionPath==null?"":versionPath);
                    logContent = logContent.replace("${nodeName}", StringTool.object2String(nodeHostRow.get("NODE_NAME")));
                    logContent = logContent.replace("${hostIp}", hostIp);
                    logContent = logContent.replace("${userName}", sshUser);
                    logContent = logContent.replace("${path}", StringTool.object2String(nodeHostRow.get("NODE_PATH")));

                    logParam.put("OPERATOR_MODULE", OperatorModule.NODE_TYPE_CONFIG.getComment());
                    logParam.put("OPERATOR_NAME", OperatorName.DEL_NODE_TYPE.getComment());
                    logParam.put("CREATED_USER", userName);
                    logParam.put("LOG_CONTENT", logContent);

                    coreService.insertObject2New("nodeTypeManagerMapper.insertNodeHandleInfo", logParam, dbKey);
                    log.debug("记录删除操作到日志表中成功");

                }
            }
        }


        log.debug("删除程序类型...");
        coreService.deleteObject2New("nodeTypeManagerMapper.delNodeType", params, dbKey);

        log.debug("删除Node...");
        coreService.deleteObject2New("nodeTypeManagerMapper.delNodeTypeOnNodeTable", params, dbKey);

        log.debug("删除版本信息...");
        coreService.deleteObject2New("nodeTypeManagerMapper.delNodeTypeOnVersionTable", params, dbKey);

        log.debug("删除部署信息...");
        coreService.deleteObject2New("nodeTypeManagerMapper.delNodeTypeOnDeployTable", params, dbKey);

        log.debug("删除启停信息...");
        coreService.deleteObject2New("nodeTypeManagerMapper.delNodeTypeOnStartTable", params, dbKey);

        result.put("effectRow", 1);
        return result;
    }

}
