package com.tydic.util;

public enum OperatorName {

    DEL_NODE(1,"DEL_NODE","删除节点"),
    DEL_NODE_VERSION(2,"DEL_NODE_VERSION","删除节点版本"),
    DEL_VERSION(3,"DEL_VERSION","删除版本"),
    DEL_NODE_TYPE(4,"DEL_NODE_TYPE","删除程序类型");

    private int code;
    private String name;
    private String comment;

    OperatorName(int code,String name,String comment){
        this.code=code;
        this.name=name;
        this.comment=comment;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }
}
