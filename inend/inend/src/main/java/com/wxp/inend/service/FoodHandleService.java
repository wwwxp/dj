package com.wxp.inend.service;

import com.wxp.inend.entity.Food;

import java.util.List;

public interface FoodHandleService {
    public List<Food> selectAll();
    public List<String> selectFoodName();
    public void adminUpdateGood(Food food);
}
