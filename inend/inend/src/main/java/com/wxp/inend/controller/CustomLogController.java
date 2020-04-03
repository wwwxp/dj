package com.wxp.inend.controller;

import com.wxp.inend.entity.User;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Controller
public class CustomLogController {
    static File targetFile;
    static BufferedWriter writer;
    static String userName;
    static User user;
    static {
        try {
            InputStream in = CustomLogController.class.getClassLoader().getResourceAsStream("windowsOrLinux.properties");
            Properties p=new Properties();
            p.load(in);
             targetFile= new File(p.getProperty("customLog.outputfile.name"));

            writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile,true)));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/userActionMsg")
    @ResponseBody
    public Map userActionMsg(HttpSession session, HttpServletRequest request, String ip, String refer, String date, String sourceURL,String value, String linkType,String appName, String osName, String osLanguage, String agent){

        user=(User)session.getAttribute("user");

        userName=user==null?"-":user.getUserName();

        int port=request.getRemotePort();

        try {
            writer.write(userName+" "+ip+" "+port+" "+refer+" "+date+" "+sourceURL+" "+value+" "+linkType+" "+appName+" "+osName+" "+osLanguage+" "+agent+"\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

}
