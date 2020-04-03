package com.tydic.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Auther: Yuanh
 * Date: 2018-07-16 15:20
 * Description:
 */
public class DeployViewDTO {
    //集群ID
    private String clusterId;
    //集群名称
    private String clusterName;
    //集群类型
    private String clusterType;
    //1：表示组件， 3：表示业务
    private String clusterFlag;
    //集群部署主机数量
    private Integer hostCount;
    //主机列表
    private String hostList;
    //集群实例个数
    private Integer instCount;

    //子程序对象列表
    private List<DeploySubProgramDTO> subProgramList;

    public class DeploySubProgramDTO {
        //子程序名称
        private String name;
        //子程序实例对象个数
        private Integer instCount;
        //实例对象列表
        private List<HashMap<String, Object>> instList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getInstCount() {
            return instCount;
        }

        public void setInstCount(Integer instCount) {
            this.instCount = instCount;
        }
        public List<HashMap<String, Object>> getInstList() {
            return instList;
        }

        public void setInstList(List<HashMap<String, Object>> instList) {
            this.instList = instList;
        }

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    ", instCount=" + instCount +
                    ", instList=" + instList +
                    '}';
        }
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public Integer getHostCount() {
        return hostCount;
    }

    public void setHostCount(Integer hostCount) {
        this.hostCount = hostCount;
    }

    public Integer getInstCount() {
        return instCount;
    }

    public void setInstCount(Integer instCount) {
        this.instCount = instCount;
    }

    public String getClusterFlag() {
        return clusterFlag;
    }

    public void setClusterFlag(String clusterFlag) {
        this.clusterFlag = clusterFlag;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<DeploySubProgramDTO> getSubProgramList() {
        return subProgramList == null ? new ArrayList<DeploySubProgramDTO>() : subProgramList;
    }

    public void setSubProgramList(List<DeploySubProgramDTO> subProgramList) {
        this.subProgramList = subProgramList;
    }

    public String getHostList() {
        return hostList;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    @Override
    public String toString() {
        return " {" +
                "clusterId='" + clusterId + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", clusterType='" + clusterType + '\'' +
                ", clusterFlag='" + clusterFlag + '\'' +
                ", hostCount=" + hostCount +
                ", hostList='" + hostList + '\'' +
                ", instCount=" + instCount +
                ", subProgramList=" + subProgramList +
                '}';
    }

    public static  void main(String [] args) {

        List<DeployViewDTO> list = new ArrayList<DeployViewDTO>();
        DeployViewDTO dto = new DeployViewDTO();
        dto.setClusterFlag("aa");
        dto.setInstCount(1);
        dto.setClusterType("other");

        List<DeploySubProgramDTO> subList = new ArrayList<DeploySubProgramDTO>();
        DeploySubProgramDTO aa = dto.new DeploySubProgramDTO();
        aa.setInstCount(1);
        aa.setName("ceshi");
        subList.add(aa);
        dto.setSubProgramList(subList);
        list.add(dto);
        System.out.println("dto.---> " + list.toString());
    }
}
