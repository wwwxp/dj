package com.tydic.service.versiondeployment.bean;

import com.tydic.bean.FtpDto;

public class UploadCfgDto {
    /**
     * 业务节点配置id
     */
    private String  nodeTypeCfgId;
    /**
     * ftp及ssl访问消息配置
     */
    private FtpDto ftpDto;

    private String fileName;

    /**
     * 类型表的context_cfg字段，描述
     * web程序的资源路径 如tomcat/webapps/{CONTEXT_CFG} 的 {CONTEXT_CFG}
     */
    private String contextCfg;

    /**
     * 远程存放路径
     */
    private String remoteFilePath;

    /**
     * 远程存放路径  release
     * 全量包（web用）
     */
    private String remoteFullVersionFilePath;

    /**
     * 远程存放路径 release
     * 全量包（web用） 最近的一个版本路径
     */
    private String lastRemoteFullVersionFilePath;


    /**
     * 是否区分ip
     * 区分ip的情况下，将只在版本发布服务器上的配置文件目录下，创建版本目录
     * 不区分ip的情况，将版本包的配置文件打包复制在配置文件目录上，创建版本目录并解压配置文件
     * diff_cfg 1 true 区分 0 fasle 不区分
     */
    private boolean isDiffCfg;


    private String runWeb;


    /**
     * 远程存放配置的路径
     */
    private String remoteCfgPath;


    /**
     * FULL_TYPE    0增量 1全量
     */
    private String fullType;

    /**
     * 上传后的版本号
     */
    private String newVersion;

    public UploadCfgDto(){

    }

    public String getRunWeb() {
        return runWeb;
    }

    public void setRunWeb(String runWeb) {
        this.runWeb = runWeb;
    }


    public void setLastRemoteFullVersionFilePath(String lastRemoteFullVersionFilePath) {
        this.lastRemoteFullVersionFilePath = lastRemoteFullVersionFilePath;
    }

    public void setContextCfg(String contextCfg) {
        this.contextCfg = contextCfg;
    }

    public String getContextCfg() {
        return contextCfg;
    }

    public String getLastRemoteFullVersionFilePath() {
        return lastRemoteFullVersionFilePath;
    }

    public String getRemoteFullVersionFilePath() {
        return remoteFullVersionFilePath;
    }

    public void setRemoteFullVersionFilePath(String remoteFullVersionFilePath) {
        this.remoteFullVersionFilePath = remoteFullVersionFilePath;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public String getFullType() {
        return fullType;
    }

    public String getNodeTypeCfgId() {
        return nodeTypeCfgId;
    }

    public void setNodeTypeCfgId(String nodeTypeCfgId) {
        this.nodeTypeCfgId = nodeTypeCfgId;
    }

    public FtpDto getFtpDto() {
        return ftpDto;
    }

    public void setFtpDto(FtpDto ftpDto) {
        this.ftpDto = ftpDto;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public String getRemoteCfgPath() {
        return remoteCfgPath;
    }

    public void setRemoteCfgPath(String remoteCfgPath) {
        this.remoteCfgPath = remoteCfgPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public void setDiffCfg(boolean diffCfg) {
        isDiffCfg = diffCfg;
    }

    public boolean isDiffCfg() {
        return isDiffCfg;
    }
}
