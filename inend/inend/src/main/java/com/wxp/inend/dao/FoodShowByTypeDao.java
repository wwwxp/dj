package com.wxp.inend.dao;

import com.wxp.inend.entity.Food;

import java.util.List;

public interface FoodShowByTypeDao {
    List<String> foodShowByType(String type) throws Exception;
    List<Food> getFoodByType(String type) throws Exception;
}
