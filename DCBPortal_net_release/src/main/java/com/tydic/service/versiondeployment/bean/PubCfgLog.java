package com.tydic.service.versiondeployment.bean;

public class PubCfgLog {
    //过程日志展示类
    private StringBuffer stringBuffer;

    public PubCfgLog() {
        stringBuffer = new StringBuffer();
    }

    public String appendLine(String line) {
        stringBuffer.append(line).append("\n");
        return line;
    }

    public StringBuffer getStringBuffer() {
        return stringBuffer;
    }

    @Override
    public String toString() {
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        PubCfgLog pubCfgLog = new PubCfgLog();
        pubCfgLog.appendLine("aaa");
        StringBuffer stringBuffer = pubCfgLog.getStringBuffer();
        stringBuffer = null;
        System.out.println(pubCfgLog.toString());
    }
}