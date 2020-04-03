package com.tydic.service.versiondeployment.service.impl;

import clojure.lang.Obj;
import com.alibaba.fastjson.JSON;
import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.versiondeployment.bean.PubCfgLog;
import com.tydic.service.versiondeployment.bean.UploadCfgDto;
import com.tydic.service.versiondeployment.bean.VersionInfoEnty;
import com.tydic.service.versiondeployment.dao.VersionOptDao;
import com.tydic.service.versiondeployment.service.UploadVersionService;
import com.tydic.service.versiondeployment.service.VersionOptService;
import com.tydic.service.versiondeployment.util.NodeVerUtil;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class VersionOptServiceImpl implements VersionOptService {

    private static Logger log = Logger.getLogger(VersionOptServiceImpl.class);

    @Autowired
    VersionOptDao versionOptDao;

    @Autowired
    CoreService coreService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateVersionPkg(MultipartFile uFile, HttpServletRequest request, HttpServletResponse response, Map<String, String> paramMap) throws Exception {
        log.debug("开始版本上传...");
        String dbkey = null;
        // 上传文件并解压
        try {
            Map<String, String> userMap = (Map<String, String>) request.getSession().getAttribute("userMap");
            String empeeId = com.tydic.bp.common.utils.tools.StringTool.object2String(userMap.get("EMPEE_ID"));

            String optUser = empeeId;
            String optDesc = paramMap.get("DESC");
            String full_type = paramMap.get("FULL_TYPE");
            if (!NodeConstant.FULL_TYPE.equals(full_type) && !NodeConstant.UN_FULL_TYPE.equals(full_type)) {
                throw new IllegalArgumentException("前台传参异常，FULL_TYPE");
            }

            String uploadType = StringTool.object2String(paramMap.get("uploadType"));
            UploadCfgDto uploadCfgDto = new UploadCfgDto();
            uploadCfgDto.setFullType(full_type);
            uploadCfgDto.setNodeTypeCfgId(paramMap.get("NODE_TYPE_CFG_ID"));

            String fileName = null;
            String fileRelPath = null;
            if(BusinessConstant.UPLOAD_TYPE_REMOTE.equals(uploadType)) {
                uploadCfgDto.setFileName(StringTool.object2String(paramMap.get("remoteFile")));
                fileName = StringTool.object2String(paramMap.get("remoteFile"));
                fileRelPath = StringTool.object2String(paramMap.get("fileRelPath"));
            }else{
                uploadCfgDto.setFileName(uFile.getOriginalFilename());
                fileName = uploadCfgDto.getFileName();  // ocs_v0.0.1.tar.gz
            }
            FtpDto ftpDto = SessionUtil.getFtpParams();
            uploadCfgDto.setFtpDto(ftpDto);
            if (BlankUtil.isBlank(uploadCfgDto.getNodeTypeCfgId())) {
                throw new IllegalArgumentException("前台传参异常，NODE_TYPE_CFG_ID");
            }
            //上传
            insertFileUpload(uploadType,fileName,fileRelPath,uFile, uploadCfgDto);
            //全量realse
            if (NodeConstant.RUN_WEB.equals(uploadCfgDto.getRunWeb())) {
                //web程序
                FtpDto pbFtpDto = uploadCfgDto.getFtpDto();
                try {
                    ShellUtils cmdUtil = new ShellUtils(pbFtpDto.getHostIp(), pbFtpDto.getUserName(), pbFtpDto.getPassword());
                    String cmd = null;
                    //是否全量
                    boolean isFullType = NodeConstant.FULL_TYPE.equals(StringTool.object2String(uploadCfgDto.getFullType()));
                    //是否有上一个全量realse版本
                    boolean hasLastFullPkg = !BlankUtil.isBlank(uploadCfgDto.getLastRemoteFullVersionFilePath());

                    String targetfilePath = FileUtil.exactPath(uploadCfgDto.getRemoteFilePath()) + uploadCfgDto.getFileName();

                    if (isFullType) {
                        cmd = getMkReleasePkgCmd(uploadCfgDto.getRemoteFullVersionFilePath(), uploadCfgDto.getFileName(), targetfilePath, uploadCfgDto.getContextCfg());
                    } else if (!isFullType && hasLastFullPkg) {
                        //增量包，且有最近的realse版本包存在
                        cmd = getMkReleasePkgCmdForLast(uploadCfgDto.getRemoteFullVersionFilePath(), uploadCfgDto.getContextCfg(), targetfilePath, uploadCfgDto.getLastRemoteFullVersionFilePath());
                    } else if (!isFullType && !hasLastFullPkg) {
                        //增量包，且没有全量的realse版本包存在
                        throw new RuntimeException("版本发布服务器没有最近的全量版本包");
                    }

                    String execResult = cmdUtil.execMsg(cmd);
                    log.debug("程序部署主机删除命令：" + cmd + "，删除结果：" + execResult);
                } catch (Exception e) {
                    log.debug("程序部署主机删除目录" + e.getMessage());
                }
            }

            //插入数据库
            insertNewVersionInfo(dbkey, optUser, optDesc, uploadCfgDto);

            //记录日志
            return Constant.SUCCESS;
        } catch (Exception e) {
            //异常处理

            log.error("业务版本包上传异常, 异常信息: ", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateVersionPatchPkg(MultipartFile uFile, HttpServletRequest request, HttpServletResponse response, Map<String, String> paramMap) throws Exception {
        log.debug("开始版本补丁上传...");
        String dbkey = null;

        String version = paramMap.get("DEP_VERSION");
        String nodeTypeId = paramMap.get("NODE_TYPE_CFG_ID");

        String uploadType = paramMap.get("uploadType");
        String fileName = null;
        String fileRelPath = null;
        if(BusinessConstant.UPLOAD_TYPE_REMOTE.equals(uploadType)) {
            fileName = StringTool.object2String(paramMap.get("remoteFile"));
            fileRelPath = StringTool.object2String(paramMap.get("fileRelPath"));
        }else{
            fileName = uFile.getOriginalFilename();
        }

        //参数  version  node_type_id
        String patchPkgName = fileName;

        Map<String, String> userMap = (Map<String, String>) request.getSession().getAttribute("userMap");
        String empeeId = com.tydic.bp.common.utils.tools.StringTool.object2String(userMap.get("EMPEE_ID"));


        //判断版本是否存在
        Map<String, String> paramVeMap = new HashMap<>();
        paramVeMap.put("NODE_TYPE_ID", nodeTypeId);
        paramVeMap.put("VERSION", version);
        List<HashMap<String, String>> nodeTypeCfgParamMapList = coreService.queryForList("versionOptService.queryNodeTypeVersionDetail", paramVeMap, dbkey);
        if (BlankUtil.isBlank(nodeTypeCfgParamMapList)) {
            throw new RuntimeException("版本上传记录 未找到 node_type_id:" + nodeTypeId + ",version" + version);
        }
        Map<String, String> nodeTypeCfgParamMap = nodeTypeCfgParamMapList.get(0);

        FtpDto pubFto = SessionUtil.getFtpParams();

//        A.FILE_NAME,
//        A.FILE_PATH,

        String id = StringTool.object2String(nodeTypeCfgParamMap.get("ID"));
        String desc = StringTool.object2String(paramMap.get("DESC"));

        //当前上传版本信息
        String ver_fileName = StringTool.object2String(nodeTypeCfgParamMap.get("FILE_NAME"));
        String ver_filePath = StringTool.object2String(nodeTypeCfgParamMap.get("FILE_PATH"));

        if (!NodeVerUtil.secondLevelPathValidate(ver_filePath)) {
            throw new RuntimeException("版本上传路径异常，FILE_PATH：" + ver_filePath);
        }

        String typeCode = StringTool.object2String(nodeTypeCfgParamMap.get("CODE"));
        String runWeb = StringTool.object2String(nodeTypeCfgParamMap.get("RUN_WEB"));
//        String filePath = NodeVerUtil.getRemotePkgStorePath(pubFto.getFtpRootPath(), typeCode, version);//带版本的程序路径
        //文件上传到    ${code}/${version}/bk_路径
        String patchPath = "patch_" + System.currentTimeMillis();

        String patchPkgSavePath = FileUtil.exactPath(ver_filePath) + patchPath;

        uploadPatchPkg(uploadType,fileName,fileRelPath,uFile, pubFto, patchPkgSavePath);

        if (NodeConstant.RUN_WEB.equals(runWeb)) {
            //与web的release包合并
            doWebPatchCombin(version, nodeTypeCfgParamMap, pubFto, typeCode, patchPkgSavePath, patchPkgName);
        } else {
            //java程序 合并 补丁和 zip包
            doAppPatchCombin(patchPkgName, pubFto, ver_fileName, ver_filePath, patchPkgSavePath);
        }

        Map<String, Object> param = new HashMap<>();
        param.put("NODE_TYPE_ID", nodeTypeId);
        param.put("VERSION", version);
        param.put("ID", id);
        param.put("EMPEE_ID", empeeId);
        param.put("FILE_DESC", "版本补丁["+ DateUtil.getCurrent() +"]:"+ desc + ";\n");
        coreService.updateObject2New("versionOptService.updateNodeTypeVersionListTbl", param, dbkey);

        return null;
    }


    private void doAppPatchCombin(String patchPkgName, FtpDto pubFto, String ver_fileName, String ver_filePath, String patchPkgSavePath) {
        StringBuilder cmdBlder = new StringBuilder();
        cmdBlder.append("cd ${ver_filePath} ;");

        if (patchPkgName.toLowerCase().contains(".tar.gz")) {
            cmdBlder.append("tar   -xzf ${ver_fileName}  ;");
        } else if (patchPkgName.toLowerCase().contains(".zip")) {
            cmdBlder.append("unzip -qo  ${ver_fileName}  ;");
        }
        cmdBlder.append("rm -rf ${ver_filePath}/${ver_fileName} ;");

        if (patchPkgName.toLowerCase().contains(".tar.gz")) {
            cmdBlder.append("tar -xzf ${patchPkgSavePath}/${patchPkgName} -C  ${ver_filePath}  ;");
        } else if (patchPkgName.toLowerCase().contains(".zip")) {
            cmdBlder.append("unzip -qo ${patchPkgSavePath}/${patchPkgName} -d  ${ver_filePath} ;");
        }
        cmdBlder.append("rm -rf ${patchPkgSavePath} ;");//删除补丁
        cmdBlder.append("cd ${ver_filePath} ;");

        //重新打包
        if (ver_fileName.toLowerCase().contains(".tar.gz")) {
            cmdBlder.append("tar -czf ${ver_fileName}  *  ;");
        } else if (ver_fileName.toLowerCase().contains(".zip")) {
            cmdBlder.append("zip -qor ${patchPkgName}  ./* ;");
        }
        cmdBlder.append("cd ${ver_filePath} ;");

        cmdBlder.append("ls ${ver_filePath} | egrep -v ${ver_fileName}|xargs rm -rf ;");//删除非版本包


        String cmdStr = cmdBlder.toString().replace("${ver_filePath}", ver_filePath)
                .replace("${ver_fileName}", ver_fileName)
                .replace("${patchPkgSavePath}", patchPkgSavePath)
                .replace("${patchPkgName}", patchPkgName);

        try {
            ShellUtils cmdUtil = new ShellUtils(pubFto.getHostIp(), pubFto.getUserName(), pubFto.getPassword());
            String ret = cmdUtil.execMsg(cmdStr);
            log.debug("执行命令 合并web的release版本--->" + cmdStr + "\n结果--->" + ret);
        } catch (Exception e) {
            log.error("版本补丁合并异常", e);
            throw new RuntimeException("版本补丁合并异常");
        }
    }

    /**
     * web的版本补丁合并
     *
     * @param version
     * @param nodeTypeCfgParamMap
     * @param pubFto
     * @param typeCode
     * @param patchPkgSavePath
     */
    private void doWebPatchCombin(String version, Map<String, String> nodeTypeCfgParamMap, FtpDto pubFto, String typeCode, String patchPkgSavePath, String patchPkgName) {
        //web程序  合并
        String releaseFullPkgStorePath = NodeVerUtil.getRemoteReleaseFullPkgStorePath(pubFto.getFtpRootPath(), typeCode, version);

        String contextCfg = StringTool.object2String(nodeTypeCfgParamMap.get("CONTEXT_CFG"));
        String relPkgName = contextCfg + ".zip";

        StringBuilder cmdBlder = new StringBuilder();
        //是否备份
        String relVersionPath = releaseFullPkgStorePath.substring(0,releaseFullPkgStorePath.length()-1);
        String releaseFullPkgStorePathBak = relVersionPath+"_"+(new Random().nextInt(9000)+1000) +"_bak";
        String bakCmd = "cp -r ${relVersionPath} ${relVersionPathBak}";
        bakCmd = bakCmd.replace("${relVersionPath}",releaseFullPkgStorePath)
                        .replace("${relVersionPathBak}",releaseFullPkgStorePathBak);

        //将上传的补丁包和原release包合并
        cmdBlder.append("cd ${releaseFullPkgStorePath} ;").append("unzip -qo ${relPkgName} ;")
                .append("rm -rf ${releaseFullPkgStorePath}/${relPkgName} ;");

        if (patchPkgName.toLowerCase().contains(".tar.gz")) {
            cmdBlder.append("tar -xzf ${patchPkgSavePath}/${patchPkgName} -C ${releaseFullPkgStorePath}  ;");
        } else if (patchPkgName.toLowerCase().contains(".zip")) {
            cmdBlder.append("unzip -qo ${patchPkgSavePath}/${patchPkgName} -d ${releaseFullPkgStorePath} ;");
        }

        cmdBlder.append("zip -qor ${relPkgName} ${contextCfg} ;")
                .append("rm -rf ${releaseFullPkgStorePath}/${contextCfg} ;");

        String cmdStr = cmdBlder.toString().replace("${releaseFullPkgStorePath}", releaseFullPkgStorePath)//release所在目录
                .replace("${relPkgName}", relPkgName)//release包名
                .replace("${patchPkgSavePath}", patchPkgSavePath)//补丁包上传目录
                .replace("${contextCfg}", contextCfg)//webaap上下文 目录
                .replace("${patchPkgName}", patchPkgName);//补丁包名

        ShellUtils cmdUtil = new ShellUtils(pubFto.getHostIp(), pubFto.getUserName(), pubFto.getPassword());
        String ret = null;
        try{
            ret = cmdUtil.execMsg(bakCmd);
            log.debug("执行命令 版本目录的备份--->" + bakCmd + "\n结果--->" + ret);
        }catch (Exception e){
            log.error("版本目录备份异常");
        }

        try {
            ret = cmdUtil.execMsg(cmdStr);
            log.debug("执行命令 合并web的release版本--->" + cmdStr + "\n结果--->" + ret);

            //合并成功，删除版本备份
            String delCmd = "rm -rf ${relVersionPathBak}".replace("${relVersionPathBak}",releaseFullPkgStorePathBak);
            ret = cmdUtil.execMsg(delCmd);
            log.debug("执行命令 删除备份的版本目录--->" + delCmd + "\n结果--->" + ret);
        } catch (Exception e) {
            log.error("版本补丁合并异常", e);

            //合并失败，则版本目录还原
            String restoreCmd = "rm -rf ${relVersionPath};mv ${relVersionPathBak} ${relVersionPath}";
            restoreCmd = restoreCmd.replace("${relVersionPathBak}",releaseFullPkgStorePathBak)
                                    .replaceAll("\\$\\{relVersionPath\\}",releaseFullPkgStorePath);
            ret = cmdUtil.execMsg(restoreCmd);
            log.debug("执行命令 版本目录的还原--->" + restoreCmd + "\n结果--->" + ret);

            throw new RuntimeException("版本补丁合并异常");
        }
    }

    private void uploadPatchPkg(String uploadType,String fileName,String fileRelPath,MultipartFile uFile, FtpDto pubFto, String filePath) throws Exception {
        Trans trans = null;
        InputStream input = null;
        String dbKey = null;
        try {
            if (BlankUtil.isBlank(pubFto)) {
                throw new RuntimeException("未知配置 WEB_FTP_CONFIG");
            }
            //获取部署主机信息
            pubFto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
            trans = FTPUtils.getFtpInstance(pubFto);
            FTPUtils.tryLogin(trans);
            String tagFilePath = FileUtil.exactPath(filePath) + fileName;
            if (!trans.isExistPath(filePath)) {
                trans.mkdir(filePath);
            }

            if(BusinessConstant.UPLOAD_TYPE_REMOTE.equals(uploadType)){   //远程主机上传程序包
                Map<String,String> remoteConfigMap = SessionUtil.getConfigByGroupCode("WEB_REMOTE_FILE_CFG");
                String ip = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_IP");
                String user = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_USER");
                String pwd = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PASSWD");
                String path = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PATH");
                Trans remoteTrans = FTPUtils.getFtpInstance(ip, user, pwd, pubFto.getFtpType());
                FTPUtils.tryLogin(remoteTrans);
                input = remoteTrans.get(FileTool.exactPath(path) + fileRelPath);
            }else {
                input = uFile.getInputStream();
            }

            trans.put(input, tagFilePath);
            log.debug("版本包上传成功, 远程文件路径: " + filePath);
        } catch (Exception e) {
            log.debug("版本包上传异常");
            throw e;
        } finally {
            if (trans != null) {
                trans.close();
            }
        }
    }


    public String getMkReleasePkgCmd(String curFullVersionPath, String fileName, String curUploadFileTarPath, String contentCfg) {
        StringBuilder cmdBlder = new StringBuilder();
        cmdBlder.append("rm -rf ${curFullVersionPath} ;")
                .append("mkdir -p ${curFullVersionPath}/${contentCfg} ;");
        if (fileName.toLowerCase().contains(".war")) {
            cmdBlder.append("unzip -qo ${curUploadFileTarPath} -d ${curFullVersionPath}/${contentCfg} ;");
        } else if (fileName.toLowerCase().contains(".tar.gz")) {
            cmdBlder.append("tar -xzf ${curUploadFileTarPath} -C ${curFullVersionPath}  ;");
        } else if (fileName.toLowerCase().contains(".zip")) {
            cmdBlder.append("unzip -qo ${curUploadFileTarPath} -d ${curFullVersionPath}  ;");
        }
        cmdBlder.append("cd ${curFullVersionPath} ;")
                .append("zip -qor ${contentCfg}.zip ${contentCfg} ;")
                .append("rm -rf ${curFullVersionPath}/${contentCfg} ;");

        String ret = cmdBlder.toString()
                .replace("${curFullVersionPath}", curFullVersionPath)
                .replace("${contentCfg}", contentCfg)
                .replace("${curUploadFileTarPath}", curUploadFileTarPath);
        return ret;
    }

    /**
     * @param curFullVersionPath   当前要放的全量版本路径
     * @param contentCfg           webapps下的目录
     * @param curUploadFileTarPath 当前要提交的增量文件
     * @param lastFullVersionPath  最近一个版本的增量文件
     * @return
     */
    public String getMkReleasePkgCmdForLast(String curFullVersionPath, String contentCfg, String curUploadFileTarPath, String lastFullVersionPath) {
        StringBuilder cmdBlder = new StringBuilder();
        cmdBlder.append("rm -rf ${curFullVersionPath} ;")
                .append("mkdir -p ${curFullVersionPath}/${contentCfg} ;")
                .append("unzip -qo ${lastFullVersionPath}/${contentCfg}.zip -d ${curFullVersionPath} ;")
                .append(curUploadFileTarPath.toLowerCase().contains(".tar.gz") ?
                        "tar -xzf ${curUploadFileTarPath} -C ${curFullVersionPath}" : "unzip -qo ${curUploadFileTarPath} -d ${curFullVersionPath}")
                .append("; cd ${curFullVersionPath} ;")
                .append("zip -qor ${contentCfg}.zip ${contentCfg} ;")
                .append("rm -rf ${curFullVersionPath}/${contentCfg} ;");
        String ret = cmdBlder.toString()
                .replace("${curFullVersionPath}", curFullVersionPath)
                .replace("${contentCfg}", contentCfg)
                .replace("${lastFullVersionPath}", lastFullVersionPath)
                .replace("${curUploadFileTarPath}", curUploadFileTarPath);
        return ret;

    }

    @Transactional(rollbackFor = Exception.class)
    public void insertNewVersionInfo(String dbKey, String optUser, String optDesc, UploadCfgDto uploadCfgDto) throws
            Exception {
        //更新业务类型最新版本信息
        versionOptDao.updateNodeTypeCfgVersion(uploadCfgDto.getNewVersion(), optUser, uploadCfgDto.getNodeTypeCfgId(), dbKey);

        //新增版本信息记录
        versionOptDao.insertNodeTypeVersionListTbl(uploadCfgDto.getFileName(), uploadCfgDto.getRemoteFilePath(), uploadCfgDto.getRemoteCfgPath(),
                uploadCfgDto.getNewVersion(), optUser, uploadCfgDto.getNodeTypeCfgId(), uploadCfgDto.getFullType(), optDesc, dbKey);

        if (!BlankUtil.isBlank(uploadCfgDto.getContextCfg())) {
            //第一次赋值，之后不覆盖
            versionOptDao.updateNodeTypeCfgContextCfg(uploadCfgDto.getNodeTypeCfgId(), uploadCfgDto.getContextCfg(), dbKey);
        }
    }

    /**
     * uploadCfgDto 如果是web容器 ,设置release全量包的路径
     *
     * @param uFile
     * @param uploadCfgDto
     * @return
     * @throws Exception
     */
    public boolean insertFileUpload(String uploadType,String fileName,String fileRelPath,MultipartFile uFile, UploadCfgDto uploadCfgDto) throws Exception {
        FtpDto ftpDto = SessionUtil.getFtpParams();
        Map<String, Object> formMap = new HashMap<>();
        Trans trans = null;
        InputStream input = null;
        String dbKey = null;
        try {
            if (BlankUtil.isBlank(ftpDto)) {
                throw new RuntimeException("未知配置 WEB_FTP_CONFIG");
            }
            //获取部署主机信息
            ftpDto.setTimeout(FTPUtils.TIMEOUT_DEF_MS);
            trans = FTPUtils.getFtpInstance(ftpDto);
            trans.login();

            String filePath = null;
            //上传  压缩文件的地址

            //1）从程序类型配置表获得“开始版本、当前版本、程序编码”，并获得本次版本号
            Map<String, String> nodeTypeCfgParamMap = versionOptDao.queryNoteTypeConfigById(uploadCfgDto.getNodeTypeCfgId(), dbKey);
            if (BlankUtil.isBlank(nodeTypeCfgParamMap)) {
                throw new RuntimeException("DCF_NODE_TYPE_CONFIG 未找到 id:" + uploadCfgDto.getNodeTypeCfgId());
            }

            String diff_cfg = StringTool.object2String(nodeTypeCfgParamMap.get("DIFF_CFG"));
            uploadCfgDto.setDiffCfg(StringUtils.equals(diff_cfg, Constant.NODE_TYPE_CONFIG_DIFF_CFG_TRUE) ? true : false);
            String typeCode = StringTool.object2String(nodeTypeCfgParamMap.get("CODE"));
            String versionNum = StringTool.object2String(nodeTypeCfgParamMap.get("CURR_VERSION"));
            String startVersion = StringTool.object2String(nodeTypeCfgParamMap.get("START_VERSION"));
            if (BlankUtil.isBlank(versionNum)) {
                versionNum = startVersion;//初始版本
            } else {
                versionNum = VersionInfoEnty.getNextVersionNum(versionNum);//下一版本
            }
            log.debug(String.format("节点配置编码%s 创建最新版本号%s", typeCode, versionNum));
            filePath = NodeVerUtil.getRemotePkgStorePath(ftpDto.getFtpRootPath(), typeCode, versionNum);//带版本的程序路径
            //设置资料值
            uploadCfgDto.setNewVersion(versionNum);
            uploadCfgDto.setRemoteFilePath(filePath);//文件路径
            {
                //如果是web容器，需要配置release全量包的路径
                String runWeb = nodeTypeCfgParamMap.get("RUN_WEB");
                //只有RUN_WEB为 1才有值  web程序的资源路径 如tomcat/webapps/{CONTEXT_CFG} 的 {CONTEXT_CFG}
                String contextCfg = StringTool.object2String(nodeTypeCfgParamMap.get("CONTEXT_CFG"));
                if (BlankUtil.isBlank(contextCfg)) {
                    contextCfg = NodeVerUtil.fileNameRemoveSuffix(fileName);//第一次的时候赋值
                }
                uploadCfgDto.setContextCfg(contextCfg);
                setRunWebParam(uploadCfgDto, ftpDto, dbKey, typeCode, versionNum, runWeb);
            }
//            uploadCfgDto.setRemoteCfgPath(getRemoteCfgPkgStorePath(ftpDto.getFtpRootPath(), typeCode, versionNum));//配置文件路径,暂时没有用
            String targetfilePath = null;//  xxx../版本/文件名.zip

            if(BusinessConstant.UPLOAD_TYPE_REMOTE.equals(uploadType)){   //远程主机上传程序包
                fileName = uploadCfgDto.getFileName();  // ocs_v0.0.1.tar.gz
                targetfilePath = FileUtil.exactPath(filePath) + fileName;
                Map<String,String> remoteConfigMap = SessionUtil.getConfigByGroupCode("WEB_REMOTE_FILE_CFG");
                String ip = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_IP");
                String user = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_USER");
                String pwd = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PASSWD");
                String path = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PATH");
                Trans remoteTrans = FTPUtils.getFtpInstance(ip, user, pwd, ftpDto.getFtpType());
                FTPUtils.tryLogin(remoteTrans);
                input = remoteTrans.get(FileTool.exactPath(path) + fileRelPath);
            }else {
                //本地主机上传程序包
                fileName = uFile.getOriginalFilename();  // ocs_v0.0.1.tar.gz
                targetfilePath = FileUtil.exactPath(filePath) + fileName;
                input = uFile.getInputStream();
            }

            log.debug("程序部署主机保存路径:" + targetfilePath);
            //通过文件流与远程文件全路径上传
            if (BlankUtil.isBlank(filePath) || filePath.equals("/")) {
                throw new RuntimeException("路径异常请检查");
            }
            log.debug("程序部署主机删除目录:" + filePath);
            try {
                ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
                String cmd = "rm -rf " + filePath;
                String execResult = cmdUtil.execMsg(cmd);
                log.debug("程序部署主机删除命令：" + cmd + "，删除结果：" + execResult);
            } catch (Exception e) {
                log.debug("程序部署主机删除目录" + e.getMessage());
            }
            trans.put(input, targetfilePath);
            log.debug("版本包上传成功, 远程文件路径: " + targetfilePath);
        } catch (Exception e) {
            log.debug("版本包上传异常");
            throw e;
        } finally {
            if (trans != null)
                trans.close();
        }
        return true;
    }

    private void setRunWebParam(UploadCfgDto uploadCfgDto, FtpDto ftpDto, String dbKey, String typeCode, String versionNum, String runWeb) {
        uploadCfgDto.setRunWeb(runWeb);
        if (NodeConstant.RUN_WEB.equals(runWeb)) {
            //当前release全量路径
            uploadCfgDto.setRemoteFullVersionFilePath(NodeVerUtil.getRemoteReleaseFullPkgStorePath(ftpDto.getFtpRootPath(), typeCode, versionNum));
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("NODE_TYPE_ID", uploadCfgDto.getNodeTypeCfgId());
            List<HashMap<String, String>> nodeTypeVersionList = versionOptDao.queryNodeTypeVersionListTbl(paramMap, dbKey);
            if (!BlankUtil.isBlank(nodeTypeVersionList)) {
                NodeVerUtil.sortDeployVersion2HashMap(nodeTypeVersionList);
                HashMap<String, String> depMap = nodeTypeVersionList.get(0);
                String lastVersion = StringTool.object2String(depMap.get("VERSION"));
                if (!BlankUtil.isBlank(lastVersion)) {
                    //最近release全量路径
                    uploadCfgDto.setLastRemoteFullVersionFilePath(NodeVerUtil.getRemoteReleaseFullPkgStorePath(ftpDto.getFtpRootPath(), typeCode, lastVersion));
                }
            }
        }
    }


    @Override
    public String deleteVersion(Map<String, String> paramMap, String dbKey) {
        PubCfgLog pubCfgLog = new PubCfgLog();
        String versionListId = StringTool.object2String(paramMap.get("ID"));
        String versionNum = paramMap.get("VERSION");
        String OPT_USER = paramMap.get("OPT_USER");
        if (BlankUtil.isBlank(versionListId) || BlankUtil.isBlank(versionNum)) {
            throw new RuntimeException("参数错误");
        }
        //查询节点信息


        //查询版本信息,没查询到将抛出 RuntimeException
        Map<String, String> nodeTypeVersionInfo = queryNodeTypeVersionInfo(versionListId, versionNum, dbKey);
        //查询ftp信息
        FtpDto ftpDto = SessionUtil.getFtpParams();
        if (BlankUtil.isBlank(ftpDto)) {
            throw new RuntimeException("ftp服务器配置异常");
        }
        log.debug(pubCfgLog.appendLine("删除版本信息开始--->" + JSON.toJSONString(nodeTypeVersionInfo)));
        String filePath = nodeTypeVersionInfo.get("FILE_PATH");
//        String cfgPath = nodeTypeVersionInfo.get("CFG_PATH");
        String version = nodeTypeVersionInfo.get("VERSION");
        String nodeTypeId = String.valueOf(nodeTypeVersionInfo.get("NODE_TYPE_ID"));

        Map<String, String> noteTypeConfigMap = versionOptDao.queryNoteTypeConfigById(nodeTypeId, dbKey);
        if (BlankUtil.isBlank(noteTypeConfigMap)) {
            throw new RuntimeException("程序类型信息不存在 nodeTypeId:" + nodeTypeId);
        }

        //判断是否可以删除，是否有在运行的节点
        boolean HasRunNode = isVersionNodeInRunState(nodeTypeId, version, dbKey);
        if (HasRunNode) {
            throw new RuntimeException("当前版本有正在运行的节点，无法删除！");
        }
        //检查路径是否异常 ,RuntimeException
        validPath(filePath, "版本包文件夹路径");
//        validPath(cfgPath, "版本配置文件夹路径");

        //删除版本部署节点数据
        String deleteNodeVersionRetStr = deleteNodeVersion(null, nodeTypeId, version, dbKey, OPT_USER);
        pubCfgLog.appendLine(deleteNodeVersionRetStr);

        //删除表记录
        log.debug(pubCfgLog.appendLine("删除版本信息表"));
        versionOptDao.deleteNodeTypeVersionById(versionListId, dbKey);


        //删除ftp主机的版本路径
        log.debug(pubCfgLog.appendLine("删除ftp主机的版本路径"));
        String typeCode = StringTool.object2String(noteTypeConfigMap.get("CODE"));
        //web程序用        {版本发布服务器根目录}/release/{程序编码}/{版本}/
        String realsePath = NodeVerUtil.getRemoteReleaseFullPkgStorePath(ftpDto.getFtpRootPath(), typeCode, version);

        String delRet = delectUploadHostFileAndCfgPath(ftpDto, filePath, realsePath);
        pubCfgLog.appendLine(delRet);

        //记录日志
        versionOptDao.insertNodeOptLog("节点配置", "删除节点", OPT_USER, "删除版本信息:" + JSON.toJSONString(nodeTypeVersionInfo), dbKey);
        log.debug(pubCfgLog.appendLine("记录删除日志"));

        log.debug(pubCfgLog.appendLine("删除版本信息结束"));
        return pubCfgLog.toString();
    }

    /**
     * 删除节点版本
     *
     * @param nodeId     NODE_ID
     * @param nodeTypeId
     * @param version
     * @param dbKey
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String deleteNodeVersion(String nodeId, String nodeTypeId, String version, String dbKey, String optUser) {
        PubCfgLog pubCfgLog = new PubCfgLog();
        log.debug(pubCfgLog.appendLine(String.format("删除节点版本开始 dcf_node_deploy_list.id[%s] nodeTypeId[%s] version[%s]", nodeId, nodeTypeId, version)));
        try {
            if (BlankUtil.isBlank(nodeTypeId) || BlankUtil.isBlank(version)) {
                throw new IllegalArgumentException("参数nodeTypeId,version不能为空");
            }
            boolean isNodeInRunState = isVersionNodeInRunState(nodeTypeId, version, dbKey);
            if (isNodeInRunState) {
                throw new RuntimeException("该版本程序存在运行节点,不能删除");
            }

            Map<String, String> paramMap = new HashMap<>();
            //查询部署表
            paramMap.put("NODE_ID", nodeId);//dcf_node_deploy_list.id
            paramMap.put("NODE_TYPE_ID", nodeTypeId);
            paramMap.put("VERSION", version);

            List<HashMap<String, String>> deployList = versionOptDao.queryNodeDeployVersionDetail(paramMap, dbKey);
            if (deployList != null) {
                for (Map<String, String> oneDepLoy : deployList) {
                    try {
                        log.debug(pubCfgLog.appendLine("----------start--删除单个版本节点部署信息及数据开始"));
                        deleteOneNodeVersion(oneDepLoy, dbKey, pubCfgLog, optUser);
                    } catch (Exception e) {
                        log.error(pubCfgLog.appendLine("删除单个版本节点部署信息及数据,异常:" + e.getMessage()));
                    } finally {
                        pubCfgLog.appendLine("----------");
                        pubCfgLog.appendLine("");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("删除节点版本信息异常", e);
            pubCfgLog.appendLine("异常--->" + e.getMessage());
        }
        return pubCfgLog.toString();
    }

    /**
     * a.ID,
     * a.VERSION,
     * a.NODE_TYPE_ID,
     * a.NODE_PATH
     * c.FILE_NAME,
     * c.FILE_PATH,
     * c.CFG_PATH,
     * d.HOST_IP,
     * d.HOST_NET_CARD,
     * d.SSH_PORT,
     * d.SSH_USER,
     * d.SSH_PASSWD
     *
     * @param oneDepLoyMap
     */
    public void deleteOneNodeVersion(Map<String, String> oneDepLoyMap, String dbKey, PubCfgLog pubCfgLog, String optUser) {

        String fileName = StringTool.object2String(oneDepLoyMap.get("FILE_NAME"));
        String version = StringTool.object2String(oneDepLoyMap.get("VERSION"));
        String depId = StringTool.object2String(oneDepLoyMap.get("ID"));//dcf_node_deploy_list的id字段
        String nodeId = StringTool.object2String(oneDepLoyMap.get("NODE_ID"));//
        String nodeTypeId = StringTool.object2String(oneDepLoyMap.get("NODE_TYPE_ID"));
        log.debug(pubCfgLog.appendLine("删除单个版本节点部署信息及数据--->" + JSON.toJSONString(oneDepLoyMap)));
        //删除表记录
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("VERSION", version);
        paramMap.put("ID", depId);
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        int num = versionOptDao.deleteNodeDeployListTbl(paramMap, null);
        log.debug("删除节点版本部署表记录（dcf_node_deploy_list）" + num + "条");

        //删除启停记录
        log.debug("删除启停记录(DCF_NODE_START_LIST)开始");
        paramMap.clear();
        paramMap.put("VERSION", version);
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        paramMap.put("NODE_ID", nodeId);
        Integer delNum = versionOptDao.deleteNodeStartList(paramMap, dbKey);
        log.debug("删除启停记录（DCF_NODE_START_LIST）" + delNum + "条");
        //删除部署路径
        ShellUtils shellUtils = getShellUtisByHostTbl(oneDepLoyMap);
        String nodePath = oneDepLoyMap.get("NODE_PATH");
        String filePath = null;
        //${NODE_PATH}/${版本}   目录
        filePath = FileUtil.exactPath(nodePath) + version;

        log.debug("删除版本数据,路径:" + filePath);
        delectNodeVersionFilePath(shellUtils, filePath, version, pubCfgLog);
        String logConet = getDelOneNodeVersionlogContent(optUser, version, nodeTypeId, oneDepLoyMap.get("HOST_IP"), oneDepLoyMap.get("SSH_USER"), FileUtil.exactPath(nodePath) + version);
        versionOptDao.insertNodeOptLog("节点配置", "删除节点", optUser, logConet, dbKey);
        log.debug(pubCfgLog.appendLine("删除单个版本节点部署信息及数据,成功"));
    }

    private String getDelOneNodeVersionlogContent(String user, String version, String nodeTypeId, String
            hostip, String userName, String path) {
        return String.format("用户[%s]删除版本包,版本[%s],程序类型ID[%s],节点主机:[%s]([%s])，删除节点版本目录:[%s]",
                user, version, nodeTypeId, hostip, userName, path);
    }

    /**
     * 查看是否存在节点在运行
     *
     * @param nodeTypeId
     * @param version
     * @param dbKey
     */
    private boolean isVersionNodeInRunState(String nodeTypeId, String version, String dbKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("VERSION", version);
        paramMap.put("NODE_TYPE_ID", nodeTypeId);
        List<HashMap<String, String>> nodeStartList = versionOptDao.queryNodeStartList(paramMap, dbKey);
        if (nodeStartList != null) {
            for (HashMap<String, String> nodeMap : nodeStartList) {
                String state = nodeMap.get("STATE");
                if (NodeConstant.RUNNING.equals(state)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * map参数 获取 ShellUtils
     * HOST_IP
     * SSH_USER
     * SSH_PASSWD（des加密）
     *
     * @param hostTblMap
     */
    public static ShellUtils getShellUtisByHostTbl(Map<String, String> hostTblMap) {
        String hostIp = hostTblMap.get("HOST_IP");
//        String sshPort = hostTblMap.get("SSH_PORT");
        String sshUser = hostTblMap.get("SSH_USER");
        String sshPasswd = hostTblMap.get("SSH_PASSWD");
        sshPasswd = DesTool.dec(sshPasswd);
        ShellUtils cmdUtil = new ShellUtils(hostIp, sshUser, sshPasswd);
        return cmdUtil;
    }

    private void delectNodeVersionFilePath(ShellUtils cmdUtil, String filePath, String version, PubCfgLog pubCfgLog) {
        try {
            checkPathAvailable(filePath);
            if (BlankUtil.isBlank(version)) {
                throw new RuntimeException("缺失版本信息");
            }
            log.debug(pubCfgLog.appendLine("删除节点主机的版本路径"));
            //删除ftp主机的版本包
            String commondStr = "rm -rf ${filePath}";
            commondStr = commondStr.replace("${filePath}", filePath);
            log.debug(pubCfgLog.appendLine("删除节点主机路径，命令：" + commondStr));
            String ret = cmdUtil.execMsg(commondStr);
            log.debug(pubCfgLog.appendLine("删除节点主机路径，结果：" + ret));
            if (!BlankUtil.isBlank(ret)) {
                throw new RuntimeException("shell result:" + ret);
            }
            log.debug("删除节点主机的版本路径结束--->" + ret);
        } catch (Exception e) {
            log.debug(pubCfgLog.appendLine("删除节点主机路径，异常：" + e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    private String delectUploadHostFileAndCfgPath(ShellUtils cmdUtil, String filePath, String cfgPath) {
        log.debug("删除ftp主机的版本路径");
        //删除ftp主机的版本包
        String commondStr = "rm -rf ${filePath} ${cfgPath}";
        commondStr = commondStr.replace("${filePath}", filePath);
        commondStr = commondStr.replace("${cfgPath}", cfgPath);
        String ret = cmdUtil.execMsg(commondStr);
        log.debug("删除ftp主机的版本路径结束--->" + ret);
        return new StringBuilder("执行-->").append(commondStr).append("\n结果-->").append(ret).toString();
    }

    private String delectUploadHostFileAndCfgPath(FtpDto ftpDto, String filePath, String realsePath) {
        try {
            ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
            String delet = delectUploadHostFileAndCfgPath(cmdUtil, filePath, realsePath);
            return delet;
        } catch (Exception e) {
            log.error("删除版本发布服务目录异常", e);
            return "删除版本发布服务目录异常:filePath=" + filePath + ",realsePath=" + realsePath;
        }
    }

    private void validPath(String path, String desc) {
        try {
            checkPathAvailable(path);
        } catch (Exception e) {
            throw new RuntimeException(desc + " error:" + e.getMessage());
        }
    }

    /**
     * 查询版本信息
     *
     * @param versionListId
     * @param versionNum    e* @param dbKey
     * @return
     */
    private Map<String, String> queryNodeTypeVersionInfo(String versionListId, String versionNum, String dbKey) {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("ID", versionListId);
        queryParamMap.put("VERSION", versionNum);
        Map<String, String> nodeTypeVersionInfo = versionOptDao.queryOneNodeTypeVersionListTbl(queryParamMap, dbKey);
        if (BlankUtil.isBlank(nodeTypeVersionInfo)) {
            throw new RuntimeException(String.format("版本信息未查询到 ID[%s] VERSION[%s]", versionListId, versionNum));
        }
        return nodeTypeVersionInfo;
    }

    /**
     * @param path
     */
    private void checkPathAvailable(String path) {
        if (BlankUtil.isBlank(path)) {
            throw new RuntimeException("路径不能为空,路径必须为2级以上目录，并且只能是绝对路径");
        }
        if ("/".equals(path) || path.length() <= 5) {
            throw new RuntimeException("路径必须为2级以上目录，并且只能是绝对路径");
        }
    }

    @Override
    public List<HashMap<String, String>> queryNoteTypeConfig(Map<String, String> paramMap, String dbkey) {
        return versionOptDao.queryNoteTypeConfig(paramMap, dbkey);
    }

    /**
     * 获得远程文件的目录树、所有文件列表
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> getRemoteFileTree(Map<String,Object> params,String dbKey) throws Exception{

        Map<String,String> remoteConfigMap = SessionUtil.getConfigByGroupCode("WEB_REMOTE_FILE_CFG");
        String ip = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_IP");
        String userName = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_USER");
        String pwd = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PASSWD");
        String remotePath = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PATH");
        String ftpType = SessionUtil.getConfigValue("FTP_TYPE");
        log.info("获取远程目录的信息，ip："+ip+"，用户名："+userName+"，目录路径："+remotePath);
        Trans remoteFtp = FTPUtils.getFtpInstance(ip, userName, pwd, ftpType);
        FTPUtils.tryLogin(remoteFtp);
        log.info("连接远程主机成功...");

        //目录树
        List<Map<String,Object>> dirTree = new ArrayList<>();
        Map<String,Object> firstDir = new HashMap<>();
        List<Map<String,Object>> childrenDirs = new ArrayList<>();
        String[] pathSplits = remotePath.split("/");
        firstDir.put("dirName",pathSplits[pathSplits.length-1]);
        firstDir.put("dirPath",remotePath);
        firstDir.put("children",childrenDirs);
        dirTree.add(firstDir);

        //文件列表
        String pageType = StringTool.object2String(params.get("page_type"));
        List<Map<String,Object>> allFileList = new ArrayList<>();
        Map<String,Object> firstFile = new HashMap<>();
        List<Map<String,Object>> childFileList = new ArrayList<>();
        FileRecord fileRecord = new FileRecord();
        fileRecord.setFileName(pathSplits[pathSplits.length-1]);
        fileRecord.setTargetPath(remotePath);
        fileRecord.setFileType(FileRecord.DIR);
        firstFile.put("file", fileRecord);
        firstFile.put("childFileList",childFileList);
        allFileList.add(firstFile);

        getChildrenDir(remotePath,childrenDirs,childFileList,pageType,remoteFtp);
        log.info("获得远程目录的目录树完成...");

        return new HashMap<String,Object>(){{put("dirTree",dirTree);put("fileList",allFileList);}};
    }

    public void getChildrenDir(String path,List<Map<String,Object>> childrenDirs,List<Map<String,Object>> childFileList,String pageType,Trans remoteFtp) throws Exception{
        Vector<FileRecord> fileList = remoteFtp.getFileList(path);
        if(fileList.size() == 0){
            return;
        }
        FileRecord file = null;
        Map<String,Object> childrenDir = null;
        List<Map<String,Object>> sonDirs = null;

        Map<String,Object> childFile = null;
        List<Map<String,Object>> sonFileList = null;
        for (int i = 0, length = fileList.size(); i < length; ++i) {

            file = fileList.get(i);

            childFile = new HashMap<>();
            sonFileList = new ArrayList<>();
            if (file.isDirectory()) {
                childrenDir = new HashMap<>();
                sonDirs = new ArrayList<>();
                childrenDir.put("dirName", file.getFileName());
                childrenDir.put("dirPath", file.getTargetPath());
                childrenDir.put("children", sonDirs);

                childFile.put("file", file);
                childFile.put("childFileList", sonFileList);
                getChildrenDir(file.getTargetPath(),sonDirs,sonFileList,pageType,remoteFtp);
                childrenDirs.add(childrenDir);
            }else{
//                //1-组件类型    2-业务类型，业务类型支持zip或者tar.gz文件
//                if(file.getFileName().lastIndexOf(".zip")> 0 && BusinessConstant.PARAMS_BUS_1.equals(pageType)){
//                    childFile.put("file", file);
//                } else if((file.getFileName().lastIndexOf(".tar.gz")> 0 || file.getFileName().lastIndexOf(".zip")> 0)
//                        && BusinessConstant.PARAMS_BUS_2.equals(pageType)){
                    childFile.put("file", file);
                //}
            }
            childFileList.add(childFile);
            if (CollectionUtils.isNotEmpty(childFileList)) {
                Collections.sort(childFileList, new FileSort());
            }
        }
    }

    private static class FileSort implements Comparator<Map<String,Object>> {

        @Override
        public int compare(Map<String,Object> record1, Map<String,Object> record2) {
            FileRecord e1 = (FileRecord) (record1.get("file"));
            FileRecord e2 = (FileRecord) (record1.get("file"));
            if(e1!=null && e2!=null) {
                return e1.getFileName().compareTo(e2.getFileName());
            }else{
                return 1;
            }
        }
    }
}
