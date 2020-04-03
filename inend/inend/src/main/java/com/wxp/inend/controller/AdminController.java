package com.wxp.inend.controller;

import com.wxp.inend.entity.Admin;
import com.wxp.inend.entity.Food;
import com.wxp.inend.service.AdminHandleService;
import com.wxp.inend.service.FoodHandleService;
import com.wxp.inend.util.UserImg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {
    @Autowired
    private AdminHandleService ahs;

    @Autowired
    private FoodHandleService fhs;

    @ResponseBody
    @RequestMapping("/getAdminName")
    public Map getAdminName(HttpSession session){
        String adminName=null;
        Admin admin=(Admin)session.getAttribute("admin");
        if(admin!=null)
            adminName=admin.getAdminName();

        Map res=new HashMap();
        res.put("adminName",adminName);
        return res;
    }

    @ResponseBody
    @RequestMapping("/adminLogin")
    public Map adminLogin( HttpSession session, String adminName, String adminPwd){
        int flag=0;
        String failMsg=null;
        //System.out.println("管理员的登陆信息:"+adminName+"，"+adminPwd);
        List<Admin> admins=ahs.selectByAdminNameAndPwd(adminName.trim(),adminPwd.trim());

        if(admins.size()>=1) {
            flag = 1;
            session.setAttribute("admin",admins.get(0));
        }
        Map map=new HashMap();
        map.put("flag",flag);

        return map;
    }

    @ResponseBody
    @RequestMapping("/adminUpdateGood")
    public Map adminUpdateGood(String foodName,String price,String oldPrice,String margin,String type,String img){
        Food food=new Food();
        food.setFoodName(foodName);
        food.setPrice(Float.parseFloat(price));
        food.setOldPrice(Float.parseFloat(oldPrice));
        food.setMargin(Integer.parseInt(margin));
        food.setType(type);
        food.setImg(img);
        fhs.adminUpdateGood(food);
        return null;
    }

    @ResponseBody
    @RequestMapping("/getUV")
    public Map getUV(){
        UserImg.UVDriver();
        int uv=UserImg.UVNumber;
        Map map=new HashMap();
        map.put("uv",uv);
        return map;
    }

    @ResponseBody
    @RequestMapping("/newUserImg")
    public Map newUserImg(){
        UserImg.userImgToHBaseDriver();

        return null;
    }

    @ResponseBody
    @RequestMapping("/readUserImg")
    public Map readUserImg(String userName){

        Map userImg=UserImg.readUserImg(userName);

        Map res=new HashMap();

        res.put("userImg",userImg);

        return res;
    }

    public static void main(String[] args){
        UserImg.userImgToHBaseDriver();
        System.out.println(UserImg.UVNumber);
    }
}
