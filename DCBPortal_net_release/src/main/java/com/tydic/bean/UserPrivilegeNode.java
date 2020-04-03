package com.tydic.bean;

import java.util.ArrayList;
import java.util.List;

public class UserPrivilegeNode {
        //节点ID
        private String id;
        //节点父ID,用来构建节点树
        private String parentId;

        //业务节点ID
        private String busId;
        //节点名称
        private String nodeName;
        //节点类型
        private String nodeType;
        //节点级别
        private String nodeLevel;

        //集群ID
        private String clusterId;

        //子节点
        private List<UserPrivilegeNode> childrens = new ArrayList<UserPrivilegeNode>();

        public UserPrivilegeNode(){

        }

        public UserPrivilegeNode(String id, String parentId, String busId, String clusterId, String nodeName, String nodeType, String nodeLevel) {
            this.id = id;
            this.parentId = parentId;
            this.busId = busId;
            this.clusterId = clusterId;
            this.nodeName = nodeName;
            this.nodeType = nodeType;
            this.nodeLevel = nodeLevel;
        }

        public List<UserPrivilegeNode> getChildrens() {
            return childrens;
        }

        public void setChildrens(List<UserPrivilegeNode> childrens) {
            this.childrens = childrens;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getBusId() {
            return busId;
        }

        public void setBusId(String busId) {
            this.busId = busId;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getNodeLevel() {
            return nodeLevel;
        }

        public void setNodeLevel(String nodeLevel) {
            this.nodeLevel = nodeLevel;
        }

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }
}