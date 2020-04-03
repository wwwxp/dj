package com.wxp.inend.service;

import com.wxp.inend.entity.Food;

import java.util.List;

public interface FoodShowByTypeService {
    List<String> foodShowByType(String type) throws Exception;
    List<Food> getFoodByType(String type) throws Exception;
}
