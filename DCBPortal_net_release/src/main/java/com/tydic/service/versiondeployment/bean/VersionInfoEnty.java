package com.tydic.service.versiondeployment.bean;

public class VersionInfoEnty implements Comparable<VersionInfoEnty>{
    private int preNum;
    private int midNum;
    private int sufNum;

    public final static String VERSION_INNER_FLG = ".";
    public final static Integer VERSION_INNER_LENGTH = 3;

    public VersionInfoEnty() {
        this.preNum = 1;
        this.midNum = 0;
        this.sufNum = 0;
    }

    public VersionInfoEnty(int preNum, int midNum, int sufNum) {
        if (midNum >= 10 || sufNum >= 10) {
            throw new IllegalArgumentException("IllegalArgument unknow version");
        }
        this.preNum = preNum;
        this.midNum = midNum;
        this.sufNum = sufNum;
    }

    public int toIntVal(){
        return preNum*100+midNum*10+sufNum*1;
    }

    public VersionInfoEnty(String versionString) {
        if (versionString == null || !versionString.contains(VERSION_INNER_FLG)) {
            throw new IllegalArgumentException("unknow versionString:" + versionString);
        }
        try {
            String[] versionNumStrs = versionString.trim().split('\\'+VERSION_INNER_FLG);
            if (versionNumStrs == null || versionNumStrs.length != VERSION_INNER_LENGTH) {
                throw new IllegalArgumentException("IllegalArgument unknow versionString:" + versionString);
            }
            this.preNum = Integer.valueOf(versionNumStrs[0]);
            this.midNum = Integer.valueOf(versionNumStrs[1]);
            this.sufNum = Integer.valueOf(versionNumStrs[2]);
            if (midNum >= 10 || sufNum >= 10) {
                throw new IllegalArgumentException("IllegalArgument unknow version:" + versionString);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("unknow versionString opt:" + versionString, e);
        }
    }


    public String getVersionString() {
        return new StringBuilder()
                .append(preNum).append(VERSION_INNER_FLG)
                .append(midNum).append(VERSION_INNER_FLG)
                .append(sufNum)
                .toString();
    }

    public VersionInfoEnty getNextVersion() {
        int preNum = this.preNum ;
        int midNum = this.midNum;
        int sufNum = this.sufNum+1;
        if (sufNum >= 10) {
            sufNum = sufNum % 10;
            midNum = midNum + 1;
        }
        if (midNum >= 10) {
            midNum = midNum % 10;
            preNum = preNum + 1;
        }
        return new VersionInfoEnty(preNum, midNum, sufNum);
    }

    public static void main(String[] args) {
        VersionInfoEnty versionInfoEnty = new VersionInfoEnty("11.9.9");
        System.out.println(versionInfoEnty.getVersionString());
        System.out.println(versionInfoEnty.getNextVersion().getVersionString());
    }

    public int getPreNum() {
        return preNum;
    }

    public void setPreNum(int preNum) {
        this.preNum = preNum;
    }

    public int getMidNum() {
        return midNum;
    }

    public void setMidNum(int midNum) {
        this.midNum = midNum;
    }

    public int getSufNum() {
        return sufNum;
    }

    public void setSufNum(int sufNum) {
        this.sufNum = sufNum;
    }

    @Override
    public int compareTo(VersionInfoEnty o) {
        return this.toIntVal()-o.toIntVal();
    }
    public static String getNextVersionNum(String currentVersion) {
        //版本上传每次版本号+1，当小版本号>=9,下次上传版本号大版本号累加，版本以十进制叠加
        //9.9.9 +1  10.0.0;
        return new VersionInfoEnty(currentVersion).getNextVersion().getVersionString();
    }
}
