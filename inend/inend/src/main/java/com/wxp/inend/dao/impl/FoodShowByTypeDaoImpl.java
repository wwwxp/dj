package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.FoodShowByTypeDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.mapper.FoodShowByTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FoodShowByTypeDaoImpl implements FoodShowByTypeDao {

    @Autowired
    private FoodShowByTypeMapper f;

    @Override
    public List<String> foodShowByType(String type) throws Exception {
        return f.foodShowByType(type);
    }

    @Override
    public List<Food> getFoodByType(String type) throws Exception {
        return f.getFoodByType(type);
    }
}
