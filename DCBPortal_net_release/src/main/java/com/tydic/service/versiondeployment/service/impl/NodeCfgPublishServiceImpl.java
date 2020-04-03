package com.tydic.service.versiondeployment.service.impl;

import com.alibaba.jstorm.utils.TimeUtils;
import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.versiondeployment.bean.PubCfg2Enty;
import com.tydic.service.versiondeployment.bean.PubCfgLog;
import com.tydic.service.versiondeployment.bean.RetCountEnty;
import com.tydic.service.versiondeployment.bean.VersionInfoEnty;
import com.tydic.service.versiondeployment.dao.VersionOptDao;
import com.tydic.service.versiondeployment.service.NodeCfgPublishService;
import com.tydic.service.versiondeployment.util.NodeVerUtil;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;

@Service
public class NodeCfgPublishServiceImpl implements NodeCfgPublishService {

    private static Logger log = Logger.getLogger(NodeCfgPublishServiceImpl.class);


    @Resource
    CoreService coreService;

    @Resource
    VersionOptDao versionOptDao;

    /**
     * 同步文件夹到其它节点上
     * @param params
     * @param dbKey
     * @return
     */
    public Map<String, String> synConfig(Map<String, Object> params, String dbKey) {
        StringBuffer transInfo = new StringBuffer();
        Trans localFtp = null;
        String localCfgTmpFile = null;
        long startTime = 0L;
        try {
            if (MapUtils.isEmpty(params)) {
                throw new IllegalArgumentException("参数不能为空");
            }
            //1）把文件夹打包成zip文件临时文件
            //获得所有nodeId的路径信息
            List<Map<String, Object>> allNodePathInfo = coreService.queryForList3New("versionOptService.queryNodePathInfoById", params, dbKey);
            String localNodeId = StringTool.object2String(params.get("NODE_ID"));
            Map<String, Object> localPathInfo = findNodePathInfo(localNodeId, allNodePathInfo);
            String localIp = StringTool.object2String(localPathInfo.get("HOST_IP"));
            String localUser = StringTool.object2String(localPathInfo.get("SSH_USER"));
            String localPwd = StringTool.object2String(localPathInfo.get("SSH_PASSWD"));
            String localPath = StringTool.object2String(localPathInfo.get("NODE_PATH"));
            localPwd = DesTool.dec(localPwd);
            String localCfgParentPath = (String) params.get("cfgParentPath");
            String localCfgPath = (String) params.get("cfgPath");
            transInfo.append("当前主机：" + localIp + ",用户名：" + localUser + "，文件路径：" + localCfgPath);
            transInfo.append("\n开始时间：" + DateUtil.getCurrent(DateUtil.allPattern));
            startTime = System.currentTimeMillis();

            List<String> nodeIds = (List) params.get("NODE_IDS");
            nodeIds.remove(localNodeId);
            if(nodeIds.size() == 0){
                throw new RuntimeException("没有需要同步的其它文件夹");
            }

            ShellUtils localClient = new ShellUtils(localIp, localUser, localPwd);
            String tmpFlag = "_" + startTime + (new Random().nextInt(9000) + 1000) + "_tmp.zip";
            localCfgTmpFile = localCfgPath + tmpFlag;
            String cfgName = localCfgPath.replace(FileTool.exactPath(localCfgParentPath),"");
            if(cfgName.endsWith("/")){
                cfgName=cfgName.substring(0,cfgName.length()-1);
            }
            String tmpCfgZipName = cfgName+tmpFlag;

            String zipCmd = "cd ${cfgParentPath};zip -qr ${tmpCfgZipName} ${cfgName}"
                    .replace("${cfgParentPath}",localCfgParentPath)
                    .replace("${tmpCfgZipName}", tmpCfgZipName)
                    .replace("${cfgName}", cfgName);
            String result = localClient.execMsg(zipCmd);
            log.info("压缩当前文件夹，执行的命令：" + zipCmd + "，执行结果：" + result);
            transInfo.append("\n压缩当前文件夹，执行的命令：" + zipCmd);
            //获取压缩文件的流
            FtpDto ftpDto = SessionUtil.getFtpParams();
            ftpDto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
            localFtp = FTPUtils.getFtpInstance(localIp, localUser, localPwd, ftpDto.getFtpType(), ftpDto.getTimeout());
            transInfo.append("\n开始同步到以下节点：");
            transInfo.append("\n======================================");

            //2）创建多线程，分别传输所有的zip文件
            List<Future<String>> futures = new ArrayList<>();
            String localCfgPathPart = localCfgPath.replace(FileTool.exactPath(localPath), "");
            String localCfgParentPart = localCfgParentPath.replace(FileTool.exactPath(localPath), "");

            Map<String,String> localInfo = new HashMap<>();
            localInfo.put("localIp",localIp);
            localInfo.put("localUser",localUser);
            localInfo.put("localPwd",localPwd);
            localInfo.put("localCfgPathPart",localCfgPathPart);
            localInfo.put("localCfgParentPart",localCfgParentPart);
            localInfo.put("tmpFlag",tmpFlag);
            localInfo.put("localCfgTmpFile",localCfgTmpFile);
            for (String nodeId : nodeIds) {
                futures.add(NodeVerUtil.executorService.submit(new TransferZip(nodeId,localInfo , ftpDto, allNodePathInfo)));
            }

            //传输的后续操作
            for (Future<String> future : futures) {
                transInfo.append(future.get(6, TimeUnit.MINUTES));
            }
            transInfo.deleteCharAt(transInfo.length() - 1);
            transInfo.append("\n======================================");
        } catch (Exception e) {
            transInfo.append("\n同步异常：" + e);
            log.error("同步异常：" + e);
            e.printStackTrace();
        } finally {
            //删除临时压缩文件
            try {
                if (localFtp != null) {
                    FTPUtils.tryLogin(localFtp);
                    if (localFtp.isExistPath(localCfgTmpFile)) {
                        localFtp.delete(localCfgTmpFile);
                        transInfo.append("\n当前文件的压缩文件删除成功");
                    }
                    localFtp.close();
                }
            } catch (Exception e) {
                transInfo.append("\n压缩文件删除失败");
                e.printStackTrace();
            }

        }
        DecimalFormat numFormat = new DecimalFormat("0.#");
        transInfo.append("\n同步文件夹到其它节点结束");
        transInfo.append("\n结束时间：" + DateUtil.getCurrent(DateUtil.allPattern) + "，总耗时：[" + numFormat.format((System.currentTimeMillis() - startTime) / 1000.0) + "]秒");
        return new HashMap<String, String>() {{
            put("transInfo", transInfo.toString());
        }};
    }

    /**
     * 传输本地压缩包的线程类
     */
    private class TransferZip implements Callable<String>{
        private String nodeId;
        private String localCfgPathPart;
        private String localCfgParentPart;
        private String tmpFlag;
        private Trans localFtp;
        private String localCfgTmpFile;
        private FtpDto ftpDto;
        private List<Map<String,Object>> allNodePathInfo;
        TransferZip(String nodeId,Map<String,String> localInfo,FtpDto ftpDto,List<Map<String,Object>> allNodePathInfo){
                this.nodeId = nodeId;
                this.localCfgPathPart = localInfo.get("localCfgPathPart");
                this.localCfgParentPart = localInfo.get("localCfgParentPart");
                this.tmpFlag = localInfo.get("tmpFlag");
                this.ftpDto = ftpDto;
                this.allNodePathInfo = allNodePathInfo;
                this.localFtp = FTPUtils.getFtpInstance(localInfo.get("localIp"), localInfo.get("localUser"), localInfo.get("localPwd"), ftpDto.getFtpType(), ftpDto.getTimeout());
                this.localCfgTmpFile = localInfo.get("localCfgTmpFile");
        }

        @Override
        public String call(){
            StringBuffer transInfo = new StringBuffer();
            String tmpZipFile = null;
            Trans remotePathFtp = null;
            boolean success = true;
            long startTime = 0L;
            try {
                Map<String,Object> pathInfo = findNodePathInfo(nodeId,allNodePathInfo);
                String ip = StringTool.object2String(pathInfo.get("HOST_IP"));
                String sshUser = StringTool.object2String(pathInfo.get("SSH_USER"));
                String pwd = StringTool.object2String(pathInfo.get("SSH_PASSWD"));
                String nodePath = StringTool.object2String(pathInfo.get("NODE_PATH"));
                pwd = DesTool.dec(pwd);
                String cfgPath = FileTool.exactPath(nodePath) + localCfgPathPart;
                String cfgParentPath = FileTool.exactPath(nodePath) + localCfgParentPart;
                tmpZipFile = cfgPath + tmpFlag;

                transInfo.append("\n目标节点："+pathInfo.get("NODE_NAME")+"，目标主机："+ip+"，目标用户名："+sshUser+"，目标文件路径："+cfgPath);
                log.info("目标主机："+ip+"，目标用户名："+sshUser+"，目标文件路径："+cfgPath);
                transInfo.append("\n开始时间：" + DateUtil.getCurrent(DateUtil.allPattern));
                startTime = System.currentTimeMillis();
                FTPUtils.tryLogin(localFtp);
                InputStream localZipStream = localFtp.get(localCfgTmpFile);
                transInfo.append("\n当前主机连接成功...");
                log.info("当前主机连接成功...");

                remotePathFtp = FTPUtils.getFtpInstance(ip, sshUser, pwd, ftpDto.getFtpType(), ftpDto.getTimeout());
                FTPUtils.tryLogin(remotePathFtp);
                transInfo.append("\n目标主机连接成功...");
                log.info("目标主机连接成功...");

                if(remotePathFtp.isExistPath(cfgParentPath)){
                    remotePathFtp.put(localZipStream,tmpZipFile);
                    transInfo.append("\n文件传输成功...");
                    log.info("文件传输成功...");

                    //解压
                    ShellUtils remoteShellUtils = new ShellUtils(ip, sshUser, pwd);
                    String unzipCfgCmd = "unzip -qo ${cfgZipFile} -d ${cfgParentPath}"
                            .replace("${cfgZipFile}",tmpZipFile)
                            .replace("${cfgParentPath}", cfgParentPath);
                    String result = remoteShellUtils.execMsg(unzipCfgCmd);
                    transInfo.append("\n解压文件，解压命令："+unzipCfgCmd);
                    log.info("解压文件，解压命令："+unzipCfgCmd+"，解压结果："+result);
                }else{
                    throw new RuntimeException("目标文件不存在，文件路径："+cfgParentPath);
                }

            }catch (Exception e){
                success = false;
                log.error("同步文件夹异常");
                transInfo.append("\n同步文件夹异常："+e);
                e.printStackTrace();
            }finally {
                if (remotePathFtp != null && remotePathFtp.isExistPath(tmpZipFile)) {
                    try {
                        remotePathFtp.delete(tmpZipFile);
                        transInfo.append("\n压缩文件删除成功");
                        remotePathFtp.close();
                    } catch (Exception e) {
                        success = false;
                        transInfo.append("\n压缩文件删除失败");
                    }
                }
            }
            DecimalFormat numFormat = new DecimalFormat("0.#");
            transInfo.append("\n结束时间：" + DateUtil.getCurrent(DateUtil.allPattern) + "，耗时：[" + numFormat.format((System.currentTimeMillis() - startTime) / 1000.0) + "]秒");
            transInfo.append("\n同步结果："+(success?"[成功]\n":"[失败]\n"));
            return transInfo.toString();
        }
    }

    /**
     * 根据nodeId获取对应的节点路径信息
     * @param nodeId
     * @param table
     * @return
     */
    public Map<String,Object> findNodePathInfo(String nodeId,List<Map<String,Object>> table){

        for(Map<String,Object> row:table){
            if(StringTool.object2String(row.get("NODE_ID")).equals(nodeId)){
                return row;
            }
        }
        return null;
    }

    @Override
    public List<HashMap<String, String>> queryVersionType(String dbKey) {
        //程序类型
        List<HashMap<String, String>> list = coreService.queryForList("versionOptService.queryNodeTypeCfg", new HashMap<>(), dbKey);
        return list;
    }

    @Override
    public List<HashMap<String, String>> queryDeployVersion(String nodeTypeId, String dbKey) {
        if (BlankUtil.isBlank(nodeTypeId)) {
            log.debug("节点类型不能为空");
            return new ArrayList<>();
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        List<HashMap<String, String>> list = coreService.queryForList("versionOptService.queryDeployVersionByNodeTypeId", paramMap, dbKey);

        //boolean isRunweb = versionOptDao.isNodeTypeRunWeb(nodeTypeId, dbKey);
        NodeVerUtil.sortDeployVersion2HashMap(list);//版本降序
        return list;
    }



    /**
     * 使用节点类型，版本，查询 节点信息
     *
     * @param nodeTypeId (必传)
     * @param version    （必传）
     * @param dbKey
     * @return
     */
    @Override
    public List<HashMap<String, Object>> queryNodeDeployInfo(String nodeTypeId, String version, String dbKey) {
        if (BlankUtil.isBlank(nodeTypeId) || BlankUtil.isBlank(version)) {
            return new ArrayList<>();
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("VERSION", version);
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        List<HashMap<String, Object>> mapList = coreService.queryForList2New("versionOptService.queryNodeDeployInfoForUpdateCfg", paramMap, dbKey);
        return mapList;
    }


    /**
     * 查询节点部署文件目录结构
     * 程序目录结构
     * 参数 节点类型 + 版本
     * 节点->版本->bin或congfig目录(配置目录)->遍历文件目录
     * 1        2            3                  4
     *
     * @param nodeTypeId 必须
     * @param nodeId     可选
     * @param version    必须
     * @param dbKey
     * @return
     */
    @Override
    public List<PubCfg2Enty> queryNodeDeployCfgFileDir(String nodeTypeId, String nodeId, String version, String dbKey) {

        PubCfgLog pubCfgLog = new PubCfgLog();
        if (BlankUtil.isBlank(nodeTypeId)) {
            throw new RuntimeException("节点类型不能为空");
        }
        if (BlankUtil.isBlank(version)) {
            throw new RuntimeException("节点版本不能为空");
        }
        List<PubCfg2Enty> pubCfg2EntyList = new ArrayList<>();

        Map<String, String> typeMap = new HashMap<>();
        PubCfg2Enty nodeTypePubCfg = queryNodeTypeCfgForEnty(nodeTypeId, dbKey, typeMap);
        if (nodeTypePubCfg == null) {
            throw new RuntimeException("节点类型不存在");
        }
        //查询配置文件路径放置在 pubCfg2EntyList
        if (NodeConstant.RUN_WEB.equals(typeMap.get("RUN_WEB"))) {
            //运行在web容器上
            runOnWeb(nodeTypeId, nodeId, version, dbKey, pubCfgLog, pubCfg2EntyList, nodeTypePubCfg);
        } else {
            //独立运行
            runByOwn(nodeTypeId, nodeId, version, dbKey, pubCfgLog, pubCfg2EntyList, nodeTypePubCfg);
        }
        formatCfgEntyList(pubCfg2EntyList);
        return pubCfg2EntyList;
    }
    public static final String BlANK_LIST = "exe|jar|war|tar.gz|tar";
    public void formatCfgEntyList(List<PubCfg2Enty> pubCfg2Enties){
        if(pubCfg2Enties==null){
            return;
        }
        Iterator<PubCfg2Enty> iterator = pubCfg2Enties.iterator();
        while (iterator.hasNext()){
            PubCfg2Enty pubCfg2Enty = iterator.next();
            String fileName = StringTool.object2String(pubCfg2Enty.getFileName());
            String[] blankList = BlANK_LIST.split("\\|");
            for (String blkStr:blankList){
                if(fileName.toLowerCase().endsWith("."+blkStr)){
                    //黑名单移除
                    iterator.remove();
                    break;
                }
            }

        }
    }

    /**
     * 获取web上的配置文件目录，目前只支持tomcat
     *
     * @param nodeTypeId
     * @param nodeId
     * @param version
     * @param dbKey
     * @param pubCfgLog
     * @param pubCfg2EntyList
     * @param nodeTypePubCfg
     */
    private void runOnWeb(String nodeTypeId, String nodeId, String version, String dbKey, PubCfgLog pubCfgLog, List<PubCfg2Enty> pubCfg2EntyList, PubCfg2Enty nodeTypePubCfg) {
        //查询所有节点    第一层
        List<HashMap<String, String>> nodeDepVerInfoList = queryNodeDepVersionDetail(nodeTypeId, nodeId, version, dbKey);
        List<Future<List<PubCfg2Enty>>> futureList = new ArrayList<>();
        for (Map<String, String> verMap : nodeDepVerInfoList) {
            String node_version = StringTool.object2String(verMap.get("VERSION"));
            String file_desc = StringTool.object2String(verMap.get("NODE_DESC"));
//            String fileName = StringTool.object2String(verMap.get("FILE_NAME"));
            String nodePath = StringTool.object2String(verMap.get("NODE_PATH"));//节点主机的部署路径
            String nodeName = StringTool.object2String(verMap.get("NODE_NAME"));//节点主机的部署路径
            String hostIp = StringTool.object2String(verMap.get("HOST_IP"));//节点主机的部署路径
            String thisNodeId = StringTool.object2String(verMap.get("NODE_ID"));//dcf_node_config.ID
            PubCfg2Enty nodeInfoEnty = new PubCfg2Enty();

            nodePath = FileUtil.exactPath(FileUtil.exactPath(nodePath)+version)+NodeVerUtil.getTomcatTempPath();

            nodeInfoEnty.setCurrId(PubCfg2Enty.appendChildId(nodeTypePubCfg, thisNodeId));
            nodeInfoEnty.setParentId(nodeTypePubCfg.getCurrId());
            nodeInfoEnty.setFileName(nodeName + "[" + hostIp + "]["+version+"](web)");
            nodeInfoEnty.setNodeTypeId(nodeTypeId);
            nodeInfoEnty.setNodeId(thisNodeId);
            nodeInfoEnty.setDesc(file_desc);
            nodeInfoEnty.setVersion(node_version);
            nodeInfoEnty.setCfgType(PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH);
            nodeInfoEnty.setParentId(nodeTypeId);
            nodeInfoEnty.setFilePath(nodePath);
            nodeInfoEnty.setTargetPath(nodePath);
            pubCfg2EntyList.add(nodeInfoEnty);
            Callable<List<PubCfg2Enty>> callable = new WebFilePubCfgEntiesCallabl(verMap, nodeInfoEnty, pubCfgLog);
            Future<List<PubCfg2Enty>> future = NodeVerUtil.executorService.submit(callable);
            futureList.add(future);
            //同步获取
//            List<PubCfg2Enty> webFilePubCfgEnties = getWebFilePubCfgEnties(verMap, nodeInfoEnty, pubCfgLog);
//            pubCfg2EntyList.addAll(webFilePubCfgEnties);
        }
        asyncGetFutureEnty(pubCfg2EntyList, futureList);

    }


    private List<HashMap<String, String>> queryNodeDepVersionDetail(String nodeTypeId, String nodeId, String version, String dbKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        paramMap.put("NODE_ID", nodeId);
        paramMap.put("VERSION", version);

        //查询节点配置表，查询节点部署表，查询 主机配置表
        List<HashMap<String, String>> nodeDepVerInfoList = versionOptDao.queryNodeDeployVersionDetail(paramMap, dbKey);
        log.debug(String.format("查询数据库中节点类型信息 NODE_TYPE_ID[%s] VERSION[%s]", nodeTypeId, nodeId, version));
        return nodeDepVerInfoList;
    }

    /**
     * @param nodeTypeId      必须
     * @param nodeId          可选
     * @param version         必须
     * @param dbKey
     * @param pubCfgLog
     * @param pubCfg2EntyList
     * @param nodeTypePubCfg
     */
    private void runByOwn(String nodeTypeId, String nodeId, String version, String dbKey, PubCfgLog pubCfgLog, List<PubCfg2Enty> pubCfg2EntyList, PubCfg2Enty nodeTypePubCfg) {
        List<HashMap<String, String>> nodeDepVerInfoList = queryNodeDepVersionDetail(nodeTypeId, nodeId, version, dbKey);
        List<Future<List<PubCfg2Enty>>> futureList = new ArrayList<>();
        for (Map<String, String> verMap : nodeDepVerInfoList) {
            String node_version = StringTool.object2String(verMap.get("VERSION"));
            String file_desc = StringTool.object2String(verMap.get("NODE_DESC"));
            String nodePath = StringTool.object2String(verMap.get("NODE_PATH"));//节点主机的部署路径
            String nodeName = StringTool.object2String(verMap.get("NODE_NAME"));//节点主机的部署路径
            String hostIp = StringTool.object2String(verMap.get("HOST_IP"));//节点主机的部署路径
            String thisNodeId = StringTool.object2String(verMap.get("NODE_ID"));//dcf_node_config.ID
            nodePath = BlankUtil.isBlank(node_version) ? nodePath : FileUtil.exactPath(nodePath) + node_version;
            PubCfg2Enty verPubCfg2Enty = new PubCfg2Enty();//第二层级 节点->版本

            verPubCfg2Enty.setCurrId(PubCfg2Enty.appendChildId(nodeTypePubCfg, thisNodeId));
            verPubCfg2Enty.setParentId(nodeTypePubCfg.getCurrId());
            verPubCfg2Enty.setFileName(nodeName + "[" + hostIp + "]");
            verPubCfg2Enty.setNodeTypeId(nodeTypeId);
            verPubCfg2Enty.setNodeId(thisNodeId);
            verPubCfg2Enty.setDesc(file_desc);
            verPubCfg2Enty.setVersion(node_version);
            verPubCfg2Enty.setCfgType(PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH);
            verPubCfg2Enty.setParentId(nodeTypeId);
            verPubCfg2Enty.setFilePath(nodePath);
            verPubCfg2Enty.setTargetPath(nodePath);
            PubCfg2Enty nodeInfoEnty = new PubCfg2Enty(); //第一层级 节点
            try {
                BeanUtils.copyProperties(nodeInfoEnty, verPubCfg2Enty);
            } catch (Exception e) {
                log.error("复制配置异常", e);
            }
            verPubCfg2Enty.setParentId(nodeInfoEnty.getCurrId());
            verPubCfg2Enty.setCurrId(verPubCfg2Enty.getCurrId() + "_" + node_version);
            verPubCfg2Enty.setFileName(node_version);

            pubCfg2EntyList.add(verPubCfg2Enty);
            pubCfg2EntyList.add(nodeInfoEnty);
            //异步获取目录信息  第三层级  节点->版本->bin或congfig目录(配置目录)
            Callable<List<PubCfg2Enty>> callable = new FilePubCfgEntiesCallabl(verMap, verPubCfg2Enty, pubCfgLog);
            Future<List<PubCfg2Enty>> future = NodeVerUtil.executorService.submit(callable);
            futureList.add(future);
            //同步获取
//            List<PubCfg2Enty> filePubCfgEntyList = getFilePubCfgEnties(verMap, verPubCfg2Enty, pubCfgLog);
//            pubCfg2EntyList.addAll(filePubCfgEntyList);
        }
        asyncGetFutureEnty(pubCfg2EntyList, futureList);
    }

    private void asyncGetFutureEnty(final List<PubCfg2Enty> pubCfg2EntyList, final List<Future<List<PubCfg2Enty>>> futureList) {
        for (Future<List<PubCfg2Enty>> future : futureList) {
            try {
                List<PubCfg2Enty> pubCfg2Enties = future.get(3, TimeUnit.MINUTES);//同步阻塞获取
                pubCfg2EntyList.addAll(pubCfg2Enties);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    class FilePubCfgEntiesCallabl implements Callable<List<PubCfg2Enty>> {
        private Map<String, String> verMap;
        private PubCfg2Enty priCfgEnty;
        private PubCfgLog pubCfgLog;

        public FilePubCfgEntiesCallabl(Map<String, String> verMap, PubCfg2Enty priCfgEnty, PubCfgLog pubCfgLog) {
            this.verMap = verMap;
            this.priCfgEnty = priCfgEnty;
            this.pubCfgLog = pubCfgLog;
        }

        @Override
        public List<PubCfg2Enty> call() throws Exception {
            return getFilePubCfgEnties(verMap, priCfgEnty, pubCfgLog);
        }
    }

    class WebFilePubCfgEntiesCallabl implements Callable<List<PubCfg2Enty>> {
        private Map<String, String> verMap;
        private PubCfg2Enty priCfgEnty;
        private PubCfgLog pubCfgLog;

        public WebFilePubCfgEntiesCallabl(Map<String, String> verMap, PubCfg2Enty priCfgEnty, PubCfgLog pubCfgLog) {
            this.verMap = verMap;
            this.priCfgEnty = priCfgEnty;
            this.pubCfgLog = pubCfgLog;
        }

        @Override
        public List<PubCfg2Enty> call() throws Exception {
            return getWebFilePubCfgEnties(verMap, priCfgEnty, pubCfgLog);
        }
    }


    private List<PubCfg2Enty> getWebFilePubCfgEnties(Map<String, String> verMap, PubCfg2Enty priCfgEnty, PubCfgLog pubCfgLog) {

        //得到节点路径
        //在节点路径下取3种       第二层

        List<PubCfg2Enty> fileEntyList = new ArrayList<>();
        String hostIp = StringTool.object2String(verMap.get("HOST_IP"));
        String user = StringTool.object2String(verMap.get("SSH_USER"));
        String pwd = DesTool.dec(StringTool.object2String(verMap.get("SSH_PASSWD")));

        FtpDto pubFtpDto = SessionUtil.getFtpParams();
        String ftpType = pubFtpDto.getFtpType();
        log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]节点部署服务器：文件上传开始 nodePath[%s]", hostIp, user, priCfgEnty.getTargetPath())));
        FtpDto ftpDtoOne = new FtpDto();
        ftpDtoOne.setFtpType(ftpType);
        ftpDtoOne.setHostIp(hostIp);
        ftpDtoOne.setUserName(user);
        ftpDtoOne.setPassword(pwd);
        ftpDtoOne.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
        Trans trans = null;
        try {         //登录部署主机
            trans = FTPUtils.getFtpInstance(ftpDtoOne);
            trans.login();
            String filePath = priCfgEnty.getTargetPath();
            if (!BlankUtil.isBlank(filePath) && trans.isExistPath(filePath)) {
                //1)拿到bin  config文件夹信息
                //{节点路径}/bin
                //{节点路径}/conf
                //{节点路径}/webapps/{war包程序名前缀}/config
                Vector<FileRecord> cfgFileRecordVector = trans.getFileList(priCfgEnty.getTargetPath());//
                if (!BlankUtil.isBlank(cfgFileRecordVector)) {
                    for (FileRecord cfgFileRecord : cfgFileRecordVector) {
                        if ("bin".equals(cfgFileRecord.getFileName())
                                || "conf".equals(cfgFileRecord.getFileName())) {
                            //取到配置文件夹的记录
                            log.debug("获取到配置文件夹记录:" + cfgFileRecord.getTargetPath());
                            PubCfg2Enty cfgEnty = new PubCfg2Enty();
                            BeanUtils.copyProperties(cfgEnty, cfgFileRecord);
                            cfgEnty.setParentId(priCfgEnty.getCurrId());
                            cfgEnty.setFileName("tomcat/" + cfgEnty.getFileName());
                            cfgEnty.setVersion(priCfgEnty.getVersion());
                            cfgEnty.setNodeTypeId(priCfgEnty.getNodeTypeId());
                            cfgEnty.setNodeId(priCfgEnty.getNodeId());
                            cfgEnty.setCurrId(UUID.randomUUID().toString().replaceAll("-", ""));
                            cfgEnty.setParentId(priCfgEnty.getCurrId());
                            //目录或文件
                            cfgEnty.setCfgType(cfgFileRecord.isDirectory() ? PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH : PubCfg2Enty.CFG_TYPE_CFG_FILE_PATH);
                            fileEntyList.add(cfgEnty);
                        }
                    }
                }

                {//获取 {节点路径}/webapps/{appContentDir}/config
                    String context_cfg = StringTool.object2String(verMap.get("CONTEXT_CFG"));//test-app.tar.gz 或者 test-app.zip 或者 test-app.war 的前缀 test-app
                    String appContentDir = context_cfg;
                    String appConfPath = FileUtil.exactPath(priCfgEnty.getTargetPath()) + "webapps/" + appContentDir;
                    if (trans.isExistPath(appConfPath)) {
                        Vector<FileRecord> wbappFileList = trans.getFileList(appConfPath);
                        if (wbappFileList != null) {
                            for (FileRecord cfgFileRecord : wbappFileList) {
                                if (cfgFileRecord.isDirectory() && NodeVerUtil.getCfgPathSufx().equals(cfgFileRecord.getFileName())) {
                                    //取到配置文件夹的记录
                                    log.debug("获取到配置文件夹记录:" + cfgFileRecord.getTargetPath());
                                    PubCfg2Enty cfgEnty = new PubCfg2Enty();
                                    BeanUtils.copyProperties(cfgEnty, cfgFileRecord);
                                    cfgEnty.setFileName(appContentDir + "/" + cfgEnty.getFileName());
                                    cfgEnty.setParentId(priCfgEnty.getCurrId());
                                    cfgEnty.setVersion(priCfgEnty.getVersion());
                                    cfgEnty.setNodeTypeId(priCfgEnty.getNodeTypeId());
                                    cfgEnty.setNodeId(priCfgEnty.getNodeId());
                                    cfgEnty.setCurrId(UUID.randomUUID().toString().replaceAll("-", ""));
                                    cfgEnty.setParentId(priCfgEnty.getCurrId());
                                    //目录或文件
                                    cfgEnty.setCfgType(cfgFileRecord.isDirectory() ? PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH : PubCfg2Enty.CFG_TYPE_CFG_FILE_PATH);
                                    fileEntyList.add(cfgEnty);
                                    break;
                                }
                            }
                        }
                    }

                }


                List<FileRecord> chelfileList = new ArrayList<>();//bin 和 config配置目录遍历文件结构
                List<PubCfg2Enty> chelPubCfg2Enty = new ArrayList<>();
                for (PubCfg2Enty cfg2Enty : fileEntyList) {
                    if (!cfg2Enty.isDirectory()) {
                        continue;
                    }
                    trans.getAllFileList(chelfileList, cfg2Enty.getTargetPath(), cfg2Enty.getCurrId());
                    for (FileRecord cheFileRcd : chelfileList) {
                        PubCfg2Enty cfgEnty = new PubCfg2Enty();
                        BeanUtils.copyProperties(cfgEnty, cheFileRcd);
                        cfgEnty.setVersion(cfg2Enty.getVersion());
                        cfgEnty.setNodeTypeId(cfg2Enty.getNodeTypeId());
                        cfgEnty.setNodeId(cfg2Enty.getNodeId());
                        //目录或文件
                        cfgEnty.setCfgType(cheFileRcd.isDirectory() ? PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH : PubCfg2Enty.CFG_TYPE_CFG_FILE_PATH);
                        chelPubCfg2Enty.add(cfgEnty);
                    }
                    chelfileList.clear();
                }
                fileEntyList.addAll(chelPubCfg2Enty);
            } else {
                log.debug("目录不存在" + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            trans.close();
        }
        return fileEntyList;
    }


    /**
     * 获取部署主机配置文件内容
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map 返回对象
     */
    @Override
    public Map<String, String> getFileContent(Map<String, String> params, String dbKey) {
        String nodeId = params.get("nodeId");
        String targetPath = params.get("targetPath");
        String version = params.get("version");
        String nodeTypeId = params.get("nodeTypeId");

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        paramMap.put("VERSION", version);
        paramMap.put("NODE_ID", nodeId);
        List<HashMap<String, String>> nodeDepVerInfoList = versionOptDao.queryNodeDeployVersionDetail(paramMap, dbKey);
        if (BlankUtil.isBlank(nodeDepVerInfoList)) {
            throw new RuntimeException("文件不存在");
        }
        HashMap<String, String> ndMap = nodeDepVerInfoList.get(0);

        String hostIp = StringTool.object2String(ndMap.get("HOST_IP"));
        String user = StringTool.object2String(ndMap.get("SSH_USER"));
        String pwd = DesTool.dec(StringTool.object2String(ndMap.get("SSH_PASSWD")));
        FtpDto cfgHostFtpDto = SessionUtil.getFtpParams();
        String ftpType = cfgHostFtpDto.getFtpType();
        FtpDto ftpDto = new FtpDto();
        ftpDto.setFtpType(ftpType);
        ftpDto.setHostIp(hostIp);
        ftpDto.setUserName(user);
        ftpDto.setPassword(pwd);


        log.debug("获取配置文件内容， 参数: " + params + ", dbKey: " + dbKey);
        // 获取所有参数信息
        String webRootPath = params.get("webRootPath");

        Map<String, String> cont = new HashMap<String, String>();

        //文件名称
        String fileName = params.get("fileName");
        //文件路径
        String filePath = params.get("filePath");
        // 目录拼接
        String path = targetPath;
        log.debug("配置文件路径: " + path);

        // 文件内容
        String fileContent = "";
        String localPath = webRootPath + Constant.TMP + UUID.randomUUID().toString() + "/";
        Trans trans = null;
        try {
            trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType(), FTPUtils.TIMEOUT_DEF_MS);
            trans.login();
            if (trans.isExistPath(path)) {
                cont.put("REAL_DEPLOY_PATH", ftpDto.getHostIp() + " -> " + path);
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
            throw new RuntimeException(e.getMessage());
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


    @Override
    public String updateCfgAndPublish(PubCfg2Enty pubCfg2Enty, boolean isPublishAll, String fileContent, String dbkey) {
        if (pubCfg2Enty == null || BlankUtil.isBlank(isPublishAll) || BlankUtil.isBlank(fileContent)) {
            throw new RuntimeException("参数异常");
        }
        PubCfgLog pubCfgLog = new PubCfgLog();
        String nodeId = pubCfg2Enty.getNodeId();
        String version = pubCfg2Enty.getVersion();
        String nodeTypeId = pubCfg2Enty.getNodeTypeId();
        if (BlankUtil.isBlank(nodeId) || BlankUtil.isBlank(version) || BlankUtil.isBlank(nodeTypeId)) {
            throw new RuntimeException("节点参数异常");
        }
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("ID", nodeTypeId);
        log.debug(String.format("查询节点类型信息 nodeTypeId[%s]", nodeTypeId));
        List<HashMap<String, String>> noteTypeConfigList = versionOptDao.queryNoteTypeConfig(queryParam, dbkey);
        if (BlankUtil.isBlank(noteTypeConfigList)) {
            log.debug("节点类型未找到id " + nodeTypeId);
            return null;
        }
        Map<String, String> nodeTypeMap = noteTypeConfigList.get(0);
        String runWeb = StringTool.object2String(nodeTypeMap.get("RUN_WEB"));// '是否运行在容器中，容器为tomcat、weblogic，0:单独运行  1：运行在容器中',
        String cfgFileVersionTargetPath = null;
        try {
            if (NodeConstant.RUN_WEB.equals(runWeb)) {
                //web程序路径
                cfgFileVersionTargetPath = getWebPath(pubCfg2Enty.getTargetPath(), nodeId, version, nodeTypeId, dbkey); //获取到    {节点目录}/.... 后面的绝对目录
            } else {
                //获取文件相对目录   {版本}/config/myconf.yml
                cfgFileVersionTargetPath = getVersionPath(pubCfg2Enty.getTargetPath(), version);//获取到版本路径
            }
            if (BlankUtil.isBlank(cfgFileVersionTargetPath)) {
                throw new RuntimeException("获取相对路径异常");
            }
        } catch (Exception e) {
            log.error("获取相对路径异常", e);
            throw e;
        }
        log.debug(pubCfgLog.appendLine("配置文件发布开始:"));
        //查询主机信息
        log.debug(pubCfgLog.appendLine("配置文件发布:是否同步所有主机配置—>" + isPublishAll));
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        if (!isPublishAll) {
            paramMap.put("NODE_ID", nodeId);
        }
        paramMap.put("VERSION", version);

        FtpDto pubFtpDto = SessionUtil.getFtpParams();
        String ftpType = pubFtpDto.getFtpType();

        List<HashMap<String, String>> nodeDepVerInfoList = versionOptDao.queryNodeDeployVersionDetail(paramMap, dbkey);
        if (nodeDepVerInfoList != null) {
            RetCountEnty retCountEnty = new RetCountEnty(nodeDepVerInfoList.size());
            for (Map<String, String> nodeInfoMap : nodeDepVerInfoList) {
                String retStr = publishFileToHost(nodeInfoMap, ftpType, cfgFileVersionTargetPath, fileContent, retCountEnty);
                pubCfgLog.appendLine(retStr);
            }
            pubCfgLog.getStringBuffer().insert(0, "同步配置结果：" + retCountEnty.toString() + "\n");
        }
        return pubCfgLog.toString();
    }

    private String getVersionPath(String tarpath, String version) {
        return tarpath.substring(tarpath.indexOf(version));
    }

    /**
     * @param tarpath
     * @param nodeId     必须
     * @param version    必须
     * @param nodeTypeId 必须
     * @param dbkey
     * @return
     */
    private String getWebPath(String tarpath, String nodeId, String version, String nodeTypeId, String dbkey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        paramMap.put("NODE_ID", nodeId);
        paramMap.put("VERSION", version);

        List<HashMap<String, String>> nodeDepVerInfoList = versionOptDao.queryNodeDeployVersionDetail(paramMap, dbkey);
        HashMap<String, String> nodeInfoMap = nodeDepVerInfoList.get(0);
        String nodePath = StringTool.object2String(nodeInfoMap.get("NODE_PATH"));
        String webPath = tarpath.substring(tarpath.indexOf(nodePath) + nodePath.length());
        log.debug("获取web项目的相对路径:" + webPath);
        return webPath;
    }

    private String publishFileToHost(Map<String, String> nodeInfoMap, String ftpType, String fileVersionPath, String fileContent, RetCountEnty retCountEnty) {
        boolean isSuc = false;
        PubCfgLog pubCfgLog = new PubCfgLog();
        pubCfgLog.appendLine("_______________start_____________________________");
        String hostIp = StringTool.object2String(nodeInfoMap.get("HOST_IP"));
        String user = StringTool.object2String(nodeInfoMap.get("SSH_USER"));
        String pwd = DesTool.dec(StringTool.object2String(nodeInfoMap.get("SSH_PASSWD")));
        String nodePath = StringTool.object2String(nodeInfoMap.get("NODE_PATH"));
        nodePath = FileUtil.exactPath(nodePath);//节点路径加上 tomcat模板路径
        log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]服务器,配置修改：文件上传开始 nodePath[%s] file[%s]", hostIp, user, nodePath, fileVersionPath)));

        if (BlankUtil.isBlank(nodePath)) {
            log.debug(pubCfgLog.appendLine("NODE_PATH参数异常"));
            return pubCfgLog.toString();
        }
        FtpDto ftpDtoOne = new FtpDto();
        ftpDtoOne.setFtpType(ftpType);
        ftpDtoOne.setHostIp(hostIp);
        ftpDtoOne.setUserName(user);
        ftpDtoOne.setPassword(pwd);
        ftpDtoOne.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
        Trans trans = null;
        try {         //登录部署主机
            trans = FTPUtils.getFtpInstance(ftpDtoOne);
            trans.login();
            log.debug(pubCfgLog.appendLine("登录部署主机"));
            String filePath = FileUtil.exactPath(nodePath) + (fileVersionPath);
            if (!BlankUtil.isBlank(filePath) && trans.isExistPath(filePath)) {
                log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]服务器,配置修改：文件上传开始 文件路径[%s]", hostIp, user, filePath)));
                trans.put(new StringInputStream(fileContent), filePath);
                log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]服务器,配置修改：文件上传完成 文件路径[%s]", hostIp, user, filePath)));
                isSuc = true;
            } else {
                log.debug(pubCfgLog.appendLine("配置修改：文件不存在"));
            }
        } catch (Exception e) {
            log.debug(pubCfgLog.appendLine("配置修改异常:" + e.getMessage()), e);
        } finally {
            trans.close();
        }
        retCountEnty.markRet(isSuc);
        log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]服务器,配置修改,结束。结果: [%s]", hostIp, user, (isSuc ? "成功" : "失败"))));
        pubCfgLog.appendLine("_______________end_____________________________");
        pubCfgLog.appendLine("");
        return pubCfgLog.toString();
    }



    private List<PubCfg2Enty> getFilePubCfgEnties(Map<String, String> verMap, PubCfg2Enty priCfgEnty, PubCfgLog pubCfgLog) {
        List<PubCfg2Enty> fileEntyList = new ArrayList<>();
        String hostIp = StringTool.object2String(verMap.get("HOST_IP"));
        String user = StringTool.object2String(verMap.get("SSH_USER"));
        String pwd = DesTool.dec(StringTool.object2String(verMap.get("SSH_PASSWD")));

        FtpDto pubFtpDto = SessionUtil.getFtpParams();
        String ftpType = pubFtpDto.getFtpType();
        log.debug(pubCfgLog.appendLine(String.format("HOST_IP[%s]SSH_USER[%s]节点部署服务器：文件上传开始 nodePath[%s]", hostIp, user, priCfgEnty.getTargetPath())));
        FtpDto ftpDtoOne = new FtpDto();
        ftpDtoOne.setFtpType(ftpType);
        ftpDtoOne.setHostIp(hostIp);
        ftpDtoOne.setUserName(user);
        ftpDtoOne.setPassword(pwd);
        ftpDtoOne.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
        Trans trans = null;
        try {         //登录部署主机
            trans = FTPUtils.getFtpInstance(ftpDtoOne);
            trans.login();
            String filePath = priCfgEnty.getTargetPath();
            if (!BlankUtil.isBlank(filePath) && trans.isExistPath(filePath)) {
                //1)拿到bin  config文件夹信息
                Vector<FileRecord> cfgFileRecordVector = trans.getFileList(priCfgEnty.getTargetPath());//    NODE_PATH/[VERSION]
                if (!BlankUtil.isBlank(cfgFileRecordVector)) {
                    for (FileRecord cfgFileRecord : cfgFileRecordVector) {
                        if ("bin".equals(cfgFileRecord.getFileName())
                                || StringUtils.equals(NodeVerUtil.getCfgPathSufx(), cfgFileRecord.getFileName())) {
                            //取到配置文件夹的记录
                            log.debug("获取到配置文件夹记录:" + cfgFileRecord.getTargetPath());
                            PubCfg2Enty cfgEnty = new PubCfg2Enty();
                            BeanUtils.copyProperties(cfgEnty, cfgFileRecord);
                            cfgEnty.setParentId(priCfgEnty.getCurrId());
                            cfgEnty.setVersion(priCfgEnty.getVersion());
                            cfgEnty.setNodeTypeId(priCfgEnty.getNodeTypeId());
                            cfgEnty.setNodeId(priCfgEnty.getNodeId());
                            cfgEnty.setCurrId(UUID.randomUUID().toString().replaceAll("-", ""));
                            cfgEnty.setParentId(priCfgEnty.getCurrId());
                            //目录或文件
                            cfgEnty.setCfgType(cfgFileRecord.isDirectory() ? PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH : PubCfg2Enty.CFG_TYPE_CFG_FILE_PATH);
                            fileEntyList.add(cfgEnty);
                        }
                    }
                }

                List<FileRecord> chelfileList = new ArrayList<>();//bin 和 config配置目录遍历文件结构
                List<PubCfg2Enty> chelPubCfg2Enty = new ArrayList<>();
                for (PubCfg2Enty cfg2Enty : fileEntyList) {
                    if (!cfg2Enty.isDirectory()) {
                        continue;
                    }
                    trans.getAllFileList(chelfileList, cfg2Enty.getTargetPath(), cfg2Enty.getCurrId());
                    for (FileRecord cheFileRcd : chelfileList) {
                        PubCfg2Enty cfgEnty = new PubCfg2Enty();
                        BeanUtils.copyProperties(cfgEnty, cheFileRcd);
                        cfgEnty.setVersion(cfg2Enty.getVersion());
                        cfgEnty.setNodeTypeId(cfg2Enty.getNodeTypeId());
                        cfgEnty.setNodeId(cfg2Enty.getNodeId());
                        //目录或文件
                        cfgEnty.setCfgType(cheFileRcd.isDirectory() ? PubCfg2Enty.CFG_TYPE_CFG_FOLDER_PATH : PubCfg2Enty.CFG_TYPE_CFG_FILE_PATH);
                        chelPubCfg2Enty.add(cfgEnty);
                    }
                    chelfileList.clear();
                }
                fileEntyList.addAll(chelPubCfg2Enty);
            } else {
                log.debug("目录不存在" + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            trans.close();
        }
        return fileEntyList;
    }

    /**
     * 节点类型  组成Bean对象
     *
     * @param nodeTypeId 必传
     * @param dbKey
     * @return
     */
    private PubCfg2Enty queryNodeTypeCfgForEnty(String nodeTypeId, String dbKey, Map<String, String> typeMap) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("ID", nodeTypeId);
        log.debug(String.format("查询节点类型信息 nodeTypeId[%s]", nodeTypeId));
        List<HashMap<String, String>> noteTypeConfigList = versionOptDao.queryNoteTypeConfig(queryParam, dbKey);
        if (BlankUtil.isBlank(noteTypeConfigList)) {
            log.debug("节点类型未找到id " + nodeTypeId);
            return null;
        }
        Map<String, String> nodeTypeMap = noteTypeConfigList.get(0);
        String node_type_config_id = StringTool.object2String(nodeTypeMap.get("ID"));
        String node_type_name = StringTool.object2String(nodeTypeMap.get("NAME"));
        String node_type_code = StringTool.object2String(nodeTypeMap.get("CODE"));
        String node_type_desc = StringTool.object2String(nodeTypeMap.get("DESC"));
        String runWeb = StringTool.object2String(nodeTypeMap.get("RUN_WEB"));// '是否运行在容器中，容器为tomcat、weblogic，0:单独运行  1：运行在容器中',
        typeMap.put("RUN_WEB", runWeb);

        PubCfg2Enty pubCfgEnty = new PubCfg2Enty();
        pubCfgEnty.setCurrId("TPY" + nodeTypeId);
        pubCfgEnty.setFileName(node_type_name + "(" + node_type_code + ")");
        pubCfgEnty.setNodeTypeId(node_type_config_id);
        pubCfgEnty.setDesc(node_type_desc);
        pubCfgEnty.setCfgType(PubCfg2Enty.CFG_TYPE_NODETYPE);
        pubCfgEnty.setParentId("0");
        return pubCfgEnty;
    }

}
