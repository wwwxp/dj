package com.tydic.service.versiondeployment.bean;

import com.alibaba.fastjson.JSON;
import com.tydic.util.ftp.FileRecord;

public class PubCfg2Enty extends FileRecord {

    public transient static Integer CFG_TYPE_NODETYPE = 0;//节点（程序）类型
    public static Integer CFG_TYPE_VERSION = 1;//版本
    public static Integer CFG_TYPE_CFG_FOLDER_PATH = 2;//配置文件夹或子文件夹
    public static Integer CFG_TYPE_CFG_FILE_PATH = 3;//配置文件
    //PubCfgEnty的类型
    private int cfgType;
    private String nodeTypeId;//节点类型（程序类型）
    private String nodeId;//节点的id
    private String version;//节点版本
    
    public static String appendChildId(PubCfg2Enty parrentPubCfgEnty, String childStrId) {
        return parrentPubCfgEnty.getCurrId() + "_" + childStrId;
    }

    public int getCfgType() {
        return cfgType;
    }

    public void setCfgType(int cfgType) {
        this.cfgType = cfgType;
    }

    public String getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
