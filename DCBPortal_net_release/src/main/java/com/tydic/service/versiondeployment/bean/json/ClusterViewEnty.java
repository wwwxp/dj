/**
 * Copyright 2019 bejson.com
 */
package com.tydic.service.versiondeployment.bean.json;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ClusterViewEnty implements Serializable {

    public static final String C_TYPE_CLUSTER = "CLUS";
    public static final String C_TYPE_NODE_TYPE = "NODE_TYPE";
    public static final String C_TYPE_NODE = "NODE";

    public ClusterViewEnty(String name, String value) {
        this.name = name;
        this.value = value;
    }
    public ClusterViewEnty() {
    }

    private String name;
    private String c_id;
    private String c_type;
    private String value;
    private List<ClusterViewEnty> children;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setChildren(List<ClusterViewEnty> children) {
        this.children = children;
    }

    public List<ClusterViewEnty> getChildren() {
        return children;
    }

    public String getC_id() {
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public String getC_type() {
        return c_type;
    }

    public void setC_type(String c_type) {
        this.c_type = c_type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}