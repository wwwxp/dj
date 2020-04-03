package com.wxp.inend.controller;

import com.wxp.inend.dao.RowCountDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.service.FoodPageShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FoodPageShowController {

    @Autowired
    private FoodPageShowService fs;

    @Autowired
    private RowCountDao rowCountDao;

    private Integer pageSize=6;
    private Integer pageNow;
    private Integer rowCount;
    private Integer pageCount;

    @RequestMapping("/foodPageShow")
    @ResponseBody
    Map foodPageShow(HttpServletRequest request, Integer pageNow2, String handle) throws Exception{

        //获得rowCount
        rowCount=rowCountDao.foodRowCount();

       // System.out.println("rowCount获得成功："+rowCount);

        //求得pageCount
        if(rowCount%pageSize==0)
            pageCount=rowCount/pageSize;
        else
            pageCount=rowCount/pageSize+1;

        //获得pageNow
        HttpSession session=request.getSession();
        pageNow = (Integer) session.getAttribute("pageNow");

        if(pageNow==null && !handle.equals("point")) {
            pageNow = 1;
            session.setAttribute("pageNow",1);
        }

        //判断“翻页、指定页数”
        if(handle.equals("up"))
            pageNow+=1;
        else if(handle.equals("down"))
            pageNow-=1;
        else if(handle.equals("point"))
            pageNow=pageNow2;

        //防止pageNow不合法
        if(pageNow<1)
            pageNow=1;
        else if(pageNow>pageCount)
            pageNow=pageCount;

        session.setAttribute("pageNow",pageNow);
        //System.out.println("pageNow="+pageNow);

        //查询数据库
        Map pageMsg=new HashMap();
        pageMsg.put("fromIndex",pageSize*(pageNow-1));
        pageMsg.put("pageSize",pageSize);

        List<Food> foods=fs.foodPageShow(pageMsg);

       // System.out.println("food表获得成功："+foods);

        Map httpRes=new HashMap();
        httpRes.put("foods",foods);
        httpRes.put("pageCount",pageCount);
        httpRes.put("pageNow",pageNow);
        return httpRes;
    }
}
