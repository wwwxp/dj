package com.wxp.inend.service;

import com.wxp.inend.entity.Food;

import java.util.List;
import java.util.Map;

public interface FoodPageShowService {
    List<Food> foodPageShow(Map map) throws Exception;
}
