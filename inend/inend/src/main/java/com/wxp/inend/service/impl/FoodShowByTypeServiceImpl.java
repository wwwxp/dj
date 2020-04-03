package com.wxp.inend.service.impl;

import com.wxp.inend.dao.FoodShowByTypeDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.service.FoodShowByTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FoodShowByTypeServiceImpl implements FoodShowByTypeService {

    @Autowired
    private FoodShowByTypeDao f;

    @Override
    public List<String> foodShowByType(String type) throws Exception {
        return f.foodShowByType(type);
    }

    @Override
    public List<Food> getFoodByType(String type) throws Exception {
        return f.getFoodByType(type);
    }
}
