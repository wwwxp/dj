package com.wxp.inend.service;

import com.wxp.inend.entity.Food;

import java.util.Map;

public interface FoodBuyService {
    Food getFoodByFoodName(String foodName) throws Exception;
    void updateSaleNumberAndMarginByFoodName(Map map) throws Exception;
}
