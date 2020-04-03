package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.FoodPageShowDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.mapper.FoodPageShowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FoodPageShowDaoImpl implements FoodPageShowDao {

    @Autowired
    private FoodPageShowMapper f;

    @Override
    public List<Food> foodPageShow(Map map) throws Exception{
        return f.foodPageShow(map);
    }
}
