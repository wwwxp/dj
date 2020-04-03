package com.wxp.inend.dao;

import com.wxp.inend.entity.Food;

import java.util.List;

public interface FoodHandleDao {

    public List<Food> selectAll();
    public List<String> selectFoodName();
    public void adminUpdateGood(Food food);
}
