package com.wxp.inend.mapper;

import com.wxp.inend.entity.Food;

import java.util.List;

public interface FoodHandleMapper {

    public List<Food> selectAll();
    public List<String> selectFoodName();

    public void adminUpdateGood(Food food);
}
