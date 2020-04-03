package com.wxp.inend.entity;

public class User {
    private String userName;
    private String pwd;
    private String name;
    private Integer age;
    private String telphone;
    private String province;
    private String city;
    private String home;

    public User(){}

    public User(String userName, String pwd, String name, Integer age, String telphone, String province, String city, String home) {
        this.userName = userName;
        this.pwd = pwd;
        this.name = name;
        this.age = age;
        this.telphone = telphone;
        this.province = province;
        this.city = city;
        this.home = home;
    }

    @Override
    public String toString() {


        return "User{" +
                "userName='" + userName + '\'' +
                ", pwd='" + pwd + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", telphone='" + telphone + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", home='" + home + '\'' +
                '}';
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }
}
