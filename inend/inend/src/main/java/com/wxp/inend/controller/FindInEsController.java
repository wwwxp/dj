package com.wxp.inend.controller;

import com.wxp.inend.entity.FoodAllField;
import com.wxp.inend.service.FoodHandleService;
import com.wxp.inend.util.ESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class FindInEsController {


    @RequestMapping("/findInEs")
    @ResponseBody
    public Map findInRs(String value) throws Exception{

        List<FoodAllField> foods = new ArrayList<>(ESUtil.findFoodByNameTypeComment(value));

        Map map=new HashMap();
        map.put("esFoods",foods);

        //System.out.println(foods);

        return map;
    }

}
