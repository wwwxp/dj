package com.tydic.util;

public class NodeConstant {

    //shell脚本执行成功/失败标志位
    public static final String SUCCESS = "<<<0000>>>";
    public static final String FAIL = "<<<0001>>>";

    //节点处于运行状态
    public static final String RUNNING="1";
    public static final String STOP="0";

    // '是否运行在容器中，容器为tomcat、weblogic，0:单独运行  1：运行在容器中',
    public static final String RUN_WEB = "1";
    public static final String NOT_RUN_WEB = "0";

    // 版本包，增量或者全量
    public static final String FULL_TYPE = "1";
    public static final String UN_FULL_TYPE = "0";
}
