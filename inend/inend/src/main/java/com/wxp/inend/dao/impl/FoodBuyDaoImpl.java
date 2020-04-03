package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.FoodBuyDao;
import com.wxp.inend.entity.Food;
import com.wxp.inend.mapper.FoodBuyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FoodBuyDaoImpl implements FoodBuyDao {

    @Autowired
    private FoodBuyMapper fbm;

    @Override
    public Food getFoodByFoodName(String foodName) throws Exception {
        return fbm.getFoodByFoodName(foodName);
    }

    @Override
    public void updateSaleNumberAndMarginByFoodName(Map map) throws Exception {
            fbm.updateSaleNumberAndMarginByFoodName(map);
    }
}
