package com.wxp.inend.controller;

import com.wxp.inend.entity.Food;
import com.wxp.inend.service.FoodShowByTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FoodShowByTypeController {

    @Autowired
    private FoodShowByTypeService f;

    @RequestMapping("/foodShowByType")
    @ResponseBody
    Map foodShowByType(String type) throws Exception{
        List<String> foodsOfType = f.foodShowByType(type);
       // System.out.println("根据类别获得foodName成功："+foodsOfType);

        Map httpRes = new HashMap();
        httpRes.put("partFoods",foodsOfType);
        return httpRes;
    }

    @RequestMapping("/foodShowOfTypeInMid")
    @ResponseBody
    Map foodShowOfTypeInMid(String type) throws Exception{
        List<Food> foods=f.getFoodByType(type);

       // System.out.println("“"+type+"”类食品获得食品成功："+foods);

        Map httpRes = new HashMap();
        httpRes.put("partFoods",foods);

        return httpRes;
    }
}
