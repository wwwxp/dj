package com.wxp.inend.controller;

import com.wxp.inend.entity.HotSearchTable;
import com.wxp.inend.service.FoodHandleService;
import com.wxp.inend.service.HotSearchHandleService;
import com.wxp.inend.util.UpdateHotSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class HotSearchController extends Thread{

    @Autowired
    private static boolean runStart=true;

    @Autowired
    private FoodHandleService fhs;

    @Autowired
    private HotSearchHandleService hshs;


    private List<String> foodNames;


    @RequestMapping("/hotSearch")
    @ResponseBody
    public Map hotSearch(){

        List<HotSearchTable> list = hshs.selectAll();



        Map httpRes=new java.util.HashMap();
        httpRes.put("hotList",list);

        return httpRes;
    }


    public static void quikSort(String[][] a,int left,int right){
        if(left>=right)
            return ;



        int index=onceQuikSort(a,left,right);
        quikSort(a,left,index-1);
        quikSort(a,index+1,right);


    }

    public static int onceQuikSort(String[][] a,int left,int right){

        String[] key=a[left];

        while(left<right){

            while(left<right && Integer.parseInt(key[1]) >= Integer.parseInt(a[right][1])) right--;
            a[left]=a[right];

            while(left<right && Integer.parseInt(a[left][1]) >= Integer.parseInt(key[1])) left++;
            a[right]=a[left];


        }
        a[left]=key;

        return left;

    }

}
