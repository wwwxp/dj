package com.wxp;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;
import java.util.Properties;

public class ConnectionPool {
    private static LinkedList<Connection> pool;
    private static final int minConNum=5;
    private static final int maxConNum=20;
    private static int num=0;

    private static String driver;
    private static String url;
    private static String username;
    private static String password;
    static{
        try {

            //解析properties
            InputStream in= ConnectionPool.class.getClassLoader().getResourceAsStream("windowsOrLinux.properties");
            Properties p=new Properties();
            p.load(in);

            driver=p.getProperty("mysql.driver");
            url=p.getProperty("mysql.url");
            username=p.getProperty("mysql.username");
            password=p.getProperty("mysql.password");


            Class.forName(driver);


            //pool的初始化
            pool=new LinkedList<>();

            for(int i=1;i<=minConNum;++i){

                pool.push( DriverManager.getConnection(url,username,password));
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception{
        if(num<minConNum){
            num++;
            return pool.poll();
        }else if(num<=maxConNum-1){
            num++;
            return DriverManager.getConnection(url,username,password);

        }else
            throw new Exception("连接池已被使用完");

    }

    public static void close(Connection con) throws Exception{
        if(con==null)
            return;
        pool.push(con);
        num--;

    }


}

