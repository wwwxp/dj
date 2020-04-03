package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.FoodHandleDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.mapper.FoodHandleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FoodHandleDaoImp implements FoodHandleDao {

    @Autowired
    private FoodHandleMapper fhm;

    public void adminUpdateGood(Food food){
        fhm.adminUpdateGood(food);
    }

    @Override
    public List<Food> selectAll() {
        return fhm.selectAll();
    }

    @Override
    public List<String> selectFoodName() {
        return fhm.selectFoodName();
    }
}
