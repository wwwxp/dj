package com.tydic.util;

public enum OperatorModule {

    NODE_CONFIG(1,"NODE_CONFIG","节点配置"),
    VERSION_UPLOAD(2,"VERSION_UPLOAD","节点配置"),
    VERSION_START_STOP_MANAGEMENT(3,"VERSION_START_STOP_MANAGEMENT","版本启停管理"),
    NODE_TYPE_CONFIG(4,"NODE_TYPE_CONFIG","程序类型配置"),
    NODE_START_MANAGER(5,"NODE_START_MANAGER","版本启停管理");

    private int code;
    private String name;
    private String comment;

    OperatorModule(int code,String name,String comment){
        this.code=code;
        this.name=name;
        this.comment=comment;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
