package com.wxp.inend.service.impl;

import com.wxp.inend.dao.FoodHandleDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.service.FoodHandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FoodHandleServiceImp implements FoodHandleService {

    @Autowired
    private FoodHandleDao fhd;

    public void adminUpdateGood(Food food){
        fhd.adminUpdateGood(food);
    }

    @Override
    public List<Food> selectAll() {
        return fhd.selectAll();
    }

    @Override
    public List<String> selectFoodName() {
        return fhd.selectFoodName();
    }
}
