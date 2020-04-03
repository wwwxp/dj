package com.wxp.inend.service.impl;

import com.wxp.inend.dao.FoodPageShowDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.service.FoodPageShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FoodPageShowServiceImpl implements FoodPageShowService {

    @Autowired
    private FoodPageShowDao p;

    @Override
    public List<Food> foodPageShow(Map map) throws Exception {
        return p.foodPageShow(map);
    }
}
