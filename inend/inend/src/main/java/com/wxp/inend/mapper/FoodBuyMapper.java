package com.wxp.inend.mapper;

import com.wxp.inend.entity.Food;

import java.util.Map;

public interface FoodBuyMapper {

    Food getFoodByFoodName(String foodName) throws Exception;

    void updateSaleNumberAndMarginByFoodName(Map map) throws Exception;
}
