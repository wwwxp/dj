package com.wxp.inend.controller;

import com.wxp.inend.entity.BuyFood;
import com.wxp.inend.entity.Food;
import com.wxp.inend.entity.User;
import com.wxp.inend.service.FoodBuyService;
import com.wxp.inend.util.ESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FoodBuyController {

    @Autowired
    private FoodBuyService fbs;
    private int pageSize=4;
    private int pageCount;
    private int rowCount;

    @RequestMapping("/submitCar")
    @ResponseBody
    Map submitCar(HttpSession session) throws Exception{

        List<BuyFood> car=(List<BuyFood>)session.getAttribute("car");

        //把数据库中的“salnumber”增加（salenumber=salenumber+number），remain减少（remain=remian-number）
        Map map=new HashMap();
        BuyFood bf=null;
        for(int i=0;i<car.size();++i){
            bf=car.get(i);
            map.put("number",bf.getNumber());
            map.put("foodName",bf.getFoodName());
            ESUtil.updateNumber(bf.getFoodName(),bf.getNumber());       //es数据库更新
            fbs.updateSaleNumberAndMarginByFoodName(map);

        }

        car.clear();
        session.removeAttribute("car");

        return null;
    }

    @RequestMapping("/deleteFoodInCar")
    @ResponseBody
    Map deleteFoodInCar(String foodName,HttpSession session) throws Exception{

        List<BuyFood> car=(List<BuyFood>)session.getAttribute("car");

        for(int i=0;i<car.size();++i){
            if(car.get(i).getFoodName().equals(foodName)){
                car.remove(i);
                break;
            }
        }

        return null;
    }


    @RequestMapping("/getFoodByFoodName")
    @ResponseBody
    Map getFoodByFoodName(String foodName) throws Exception{
            Food food=fbs.getFoodByFoodName(foodName);
            Map map=new HashMap();
            map.put("food",food);
            return map;
    }

    @RequestMapping("/foodCarShow")
    @ResponseBody
    Map foodCarShow(int pageNow,HttpSession session){

        List<BuyFood> car=(List<BuyFood>)session.getAttribute("car");

        User user = (User)session.getAttribute("user");

      //  System.out.println("user:"+user);

        Map httpRes=new HashMap();

        List<BuyFood> httpCar=null;

        int empty=0;
        if(car!=null && car.size()>0 ){

            empty=1;

            //把获取的“购物车”进行分页
            rowCount=car.size();

            if(rowCount%pageSize==0)
                pageCount=rowCount/pageSize;
            else
                pageCount=rowCount/pageSize+1;

            if(pageNow<1)
                pageNow=1;
            else if(pageNow>pageCount)
                pageNow=pageCount;

            int fromIndex=(pageNow-1)*pageSize;
            int toIndex=fromIndex+pageSize;

            httpCar=car.subList(fromIndex,toIndex>car.size()?car.size():toIndex);
            //System.out.println("car.size()="+car.size()+",car:"+car);
            //System.out.println("httpCar.size()="+httpCar.size()+",httpCar:"+httpCar);

        }

        int noLogin=0;
        if(user!=null){
            noLogin=1;
        }



        httpRes.put("empty",empty);
        httpRes.put("car",httpCar);
        httpRes.put("noLogin",noLogin);
        httpRes.put("pageCount",pageCount);

        return httpRes;
    }

    @RequestMapping("/addFoodToCar")
    @ResponseBody
    Map addFoodToCar(String foodName, String price, String number, HttpSession session) throws Exception{

        //第一个条件为“用户已登陆”，第二个条件为“库存足够”

        //获得“库存余量”
        Food food = fbs.getFoodByFoodName(foodName);
        int margin= food.getMargin();
        int i_number = Integer.parseInt(number.trim());

        //获得登陆用户信息
        User user=(User)session.getAttribute("user");

        int noLogin=0;
        int noMargin=0;

        if(user!=null && i_number <= margin) {      //“有用户登陆、且余量够多”才可以添加到购物车
            noLogin=1;
            noMargin=1;
            //接收传过来的“一种商品”

            float f_price = Float.parseFloat(price);

            BuyFood bf = new BuyFood(foodName, f_price, i_number, f_price * i_number, food.getType());

            //获取购物车、并装入购物车
            List<BuyFood> car = (List<BuyFood>) session.getAttribute("car");
            if (car == null) {
                List<BuyFood> car2 = new ArrayList<BuyFood>();
                car2.add(bf);
                session.setAttribute("car", car2);
            } else {
                boolean flag = false;
                for (int i = 0; i < car.size(); ++i) {

                    if (car.get(i).getFoodName().equals(foodName)) {
                        BuyFood bf2 = car.get(i);
                        bf2.setNumber(bf2.getNumber() + i_number);
                        flag = true;
                        break;
                    }
                }
                if (!flag)
                    car.add(bf);

            }
        }

        Map httpRes=new HashMap();

        if(user!=null)
            noLogin=1;

        if(i_number <= margin) {   //库存充足
            noMargin=1;
        }

        httpRes.put("margin",margin);
        httpRes.put("noMargin",noMargin);
        httpRes.put("noLogin",noLogin);

        return httpRes;
    }



}
