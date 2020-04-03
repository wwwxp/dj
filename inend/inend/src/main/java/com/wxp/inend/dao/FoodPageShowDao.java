package com.wxp.inend.dao;

import com.wxp.inend.entity.Food;

import java.util.List;
import java.util.Map;

public interface FoodPageShowDao {

    List<Food> foodPageShow(Map map) throws Exception;
}
