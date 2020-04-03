package com.wxp.inend.controller;

import com.wxp.inend.entity.User;
import com.wxp.inend.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private LoginService ls;

    @RequestMapping("/userCancel")
    @ResponseBody
    Map userCancel(HttpSession session){
        session.removeAttribute("user");
        return null;
    }

    @RequestMapping("/userOnFresh")
    @ResponseBody
    Map userOnFresh(HttpSession session){
        User user=(User)session.getAttribute("user");
      //  System.out.println("user:"+user);
        int flag=0;
        if(user!=null)
            flag=1;
        Map httpResponse=new HashMap();
        httpResponse.put("flag",flag);
        httpResponse.put("user",user);

        return httpResponse;
    }

    @RequestMapping("/userLogin")
    @ResponseBody
    Map userLogin(String userName, String userPwd, HttpServletRequest request) throws Exception{

      //  System.out.println("userLogin已执行,userName:"+userName+",userPwd"+userPwd);
        User user=ls.userLogin(userName,userPwd);

      //  System.out.println("返回用户信息成功："+user);

        HttpSession session=request.getSession();

        int flag=0;

        if(user!=null) {
            flag = 1;
            session.setAttribute("user",user);
        }

        Map map=new HashMap();
        map.put("flag",flag);
        map.put("user",user);

        return map;
    }
}
