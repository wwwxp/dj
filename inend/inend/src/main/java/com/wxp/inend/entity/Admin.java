package com.wxp.inend.entity;

public class Admin {

    private String adminName;
    private String pwd;
    private int sex;
    private String telpone;

    public Admin(){ }

    public Admin(String adminName, String pwd, int sex, String telpone) {
        this.adminName = adminName;
        this.pwd = pwd;
        this.sex = sex;
        this.telpone = telpone;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminName='" + adminName + '\'' +
                ", pwd='" + pwd + '\'' +
                ", sex=" + sex +
                ", telpone='" + telpone + '\'' +
                '}';
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getTelpone() {
        return telpone;
    }

    public void setTelpone(String telpone) {
        this.telpone = telpone;
    }
}
