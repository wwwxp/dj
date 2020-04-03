package com.wxp.inend.service.impl;

import com.wxp.inend.dao.FoodBuyDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.service.FoodBuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FoodBuyServiceImlp implements FoodBuyService {

    @Autowired
    private FoodBuyDao fbd;

    @Override
    public Food getFoodByFoodName(String foodName) throws Exception {
        return fbd.getFoodByFoodName(foodName);
    }

    @Override
    public void updateSaleNumberAndMarginByFoodName(Map map) throws Exception {
        fbd.updateSaleNumberAndMarginByFoodName(map);
    }
}
