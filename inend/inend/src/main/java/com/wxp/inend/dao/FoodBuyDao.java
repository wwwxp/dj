package com.wxp.inend.dao;

import com.wxp.inend.entity.Food;

import java.util.Map;

public interface FoodBuyDao {
    Food getFoodByFoodName(String foodName) throws Exception;
    void updateSaleNumberAndMarginByFoodName(Map map) throws Exception;
}
