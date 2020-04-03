package com.wxp.inend.mapper;

import com.wxp.inend.entity.Food;

import java.util.List;

public interface FoodShowByTypeMapper {
    List<String> foodShowByType(String type) throws Exception;
    List<Food> getFoodByType(String type) throws Exception;

}
