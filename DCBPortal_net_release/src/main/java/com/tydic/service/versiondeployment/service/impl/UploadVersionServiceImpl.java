package com.tydic.service.versiondeployment.service.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.CommonTool;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.versiondeployment.bean.UploadCfgDto;
import com.tydic.service.versiondeployment.bean.VersionInfoEnty;
import com.tydic.service.versiondeployment.dao.VersionOptDao;
import com.tydic.service.versiondeployment.service.UploadVersionService;
import com.tydic.service.versiondeployment.util.NodeVerUtil;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.Trans;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class UploadVersionServiceImpl implements UploadVersionService {

    private static Logger log = Logger.getLogger(VersionOptServiceImpl.class);


    @Autowired
    VersionOptDao versionOptDao;

    @Override
    public boolean insertFileUpload(MultipartFile uFile, UploadCfgDto uploadCfgDto) throws Exception {
        FtpDto ftpDto = SessionUtil.getFtpParams();
        Map<String, Object> formMap = new HashMap<>();
        Trans trans = null;
        String fileName = null;
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
            //本地主机上传程序包
            fileName = uFile.getOriginalFilename();  // ocs_v0.0.1.tar.gz
            input = uFile.getInputStream();
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
            uploadCfgDto.setRemoteFullVersionFilePath(NodeVerUtil.getRemoteReleaseFullPkgStorePath(ftpDto.getFtpRootPath(),typeCode,versionNum));
//            uploadCfgDto.setRemoteCfgPath(getRemoteCfgPkgStorePath(ftpDto.getFtpRootPath(), typeCode, versionNum));//配置文件路径,暂时没有用
            String targetfilePath = FileUtil.exactPath(filePath) + fileName;//  xxx../版本/文件名.zip
            log.debug("程序部署主机保存路径:" + targetfilePath);
            //通过文件流与远程文件全路径上传
            if (BlankUtil.isBlank(filePath) || filePath.equals("/")) {
                throw new RuntimeException("路径异常请检查");
            }
            log.debug("程序部署主机删除目录:" + filePath);
            try {
                ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
                String cmd="rm -rf "+filePath;
                String execResult=cmdUtil.execMsg(cmd);
                log.debug("程序部署主机删除命令："+cmd+"，删除结果："+execResult);
            } catch (Exception e) {
                log.debug("程序部署主机删除目录" + e.getMessage());
            }
            trans.put(input, targetfilePath);
            log.debug("版本包上传成功, 远程文件路径: " + targetfilePath);
        } catch (Exception e) {
            log.debug("版本包上传异常");
            throw e;
        } finally {
            trans.close();
        }
        return true;
    }

//    @Override
//    public boolean unzipFile(ShellUtils cmdUtil, String filePath, String fileName) {
//        String result = null;
//        try {
//            // 解压上传的文件
//            String command = getShellUnzipCmdStr(filePath, fileName);
//            log.debug("unzipFile 解压上传的文件命令 --->" + command);
//            result = cmdUtil.execMsg(command);
//            if (result.toLowerCase().indexOf(Constant.ERROR) > -1 || result.toLowerCase().indexOf(Constant.FAILED) > -1 || result.contains("No such file or directory")) {
//                log.error("创建解压失败--->" + result);
//                throw new Exception("创建解压失败:" + result);
//            }
//            log.info("解压完成:" + result);
//            return true;
//        } catch (Exception e) {
//            log.error("框架版本包上传，解压失败, 失败原因：", e);
//            throw new RuntimeException("解压失败!");
//        }
//    }
//
//    @Override
//    public boolean moveConfToDir(ShellUtils cmdUtil, String oriFilePath, String targerFilePath, String fileName, boolean isDiffCfg) {
//        String result = null;
//        try {
//            if (BlankUtil.isBlank(oriFilePath) || BlankUtil.isBlank(targerFilePath) || BlankUtil.isBlank(fileName)) {
//                throw new RuntimeException("参数不能为空");
//            }
//            // 解压现在上传的文件,判断解压类型
//            String command = getShellMoveConfToDir(oriFilePath, targerFilePath, fileName);
//
//            log.debug("moveConfToDir 移动配置命令 --->" + command);
//            result = cmdUtil.execMsg(command);
//            if (result.toLowerCase().indexOf(Constant.ERROR) > -1 || result.toLowerCase().indexOf(Constant.FAILED) > -1 || result.contains("No such file or directory")) {
//                log.error("创建配置解压失败--->" + result);
//                throw new Exception("创建配置解压失败:" + result);
//            }
//            log.info("创建配置解压完成:" + result);
//            return true;
//        } catch (Exception e) {
//            log.error("框架版本包上传，创建配置解压失败, 失败原因：", e);
//            throw new RuntimeException("创建配置解压失败!");
//        }
//    }


//    /**
//     * pkg包放置的远程路径
//     *
//     * @param ftpRootPath
//     * @param versionCode
//     * @param versionNum
//     * @return
//     */
//    public static String getRemoteCfgPkgStorePath(String ftpRootPath, String versionCode, String versionNum) {
//        if (NodeVerUtil.isSomeBlank(ftpRootPath, versionCode, versionNum)) {
//            throw new IllegalArgumentException("pkg包放置的远程路径,参数异常，请检查配置");
//        }
//        return new StringBuffer()
//                .append(FileUtil.exactPath(ftpRootPath))
//                .append("node_config")
//                .append("/").append(versionCode)
//                .append("/").append(versionNum)
//                .append("/")
//                .toString();
//    }
//
//
//
//    /**
//     * 解压当前包
//     *
//     * @param filePath
//     * @param fileName
//     * @return
//     */
//    private static String getShellUnzipCmdStr(String filePath, String fileName) {
//        String unzipCmd = null;
//        if (fileName.toLowerCase().contains(".tar.gz")) {
//            unzipCmd = "tar -xzvf";
//        } else if (fileName.toLowerCase().contains(".zip")) {
//            unzipCmd = "unzip -qo";
//        } else {
//            throw new RuntimeException("未知解压格式");
//        }
//        String command = ("cd  ${filePath};${unzipCmd} ${fileName};");//chmod -R a+x *.sh;
//        command = command.replace("${filePath}", filePath);
//        command = command.replace("${unzipCmd}", unzipCmd);
//        command = command.replace("${fileName}", fileName);
//        return command;
//    }
//
//    /**
//     * 打包配置文件到指定目录并解压
//     *
//     * @param oriFilePath
//     * @param targerFilePath
//     * @param fileName
//     * @return
//     */
//    private static String getShellMoveConfToDir(String oriFilePath, String targerFilePath, String fileName) {
//        String cfgPathSufx = NodeVerUtil.getCfgPathSufx();
//        String command = ("rm -rf ${targerFilePath};mkdir -p ${targerFilePath};" +//创建目标目录
//                "cd ${oriFilePath};zip -qor ${fileName} ${cfgPathSufx} bin;" +  //进入源目录，压缩 配置目录
//                "cp ${fileName} ${targerFilePath};rm -rf ${fileName};" +//复制文件
//                "cd ${targerFilePath};unzip -qo ${fileName};chmod -R 755 *;rm -rf ${fileName};");//进入目标目录，并解压文件夹,删除压缩文件
//        command = command.replace("${oriFilePath}", oriFilePath);
//        command = command.replace("${targerFilePath}", targerFilePath);
//        command = command.replace("${fileName}", fileName);
//        command = command.replace("${cfgPathSufx}", cfgPathSufx);
//        return command;
//    }


}
