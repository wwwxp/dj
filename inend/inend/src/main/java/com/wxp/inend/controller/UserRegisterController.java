package com.wxp.inend.controller;

import com.wxp.inend.entity.User;
import com.wxp.inend.service.UserHandleService;
import com.wxp.inend.util.MyDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class UserRegisterController {

    @Autowired
    private UserHandleService uhs;

    @RequestMapping("/register")
    public ModelAndView userRegister(HttpSession session,String userName,String pwd,String user,String brith,String telphone,String province,String city,String home){
        int age=MyDateUtil.getAgeByBrithDate(brith);

        User u=new User(userName,pwd,user,age,telphone,province,city,home);
        uhs.insertOneRow(u);
        if(!u.getUserName().equals(""))
            session.setAttribute("user",u);
        ModelAndView view=new ModelAndView();
        view.setViewName("redirect:index.html");
        return view;
    }

}
