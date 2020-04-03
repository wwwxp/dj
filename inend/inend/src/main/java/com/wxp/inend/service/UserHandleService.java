package com.wxp.inend.service;

import com.wxp.inend.entity.User;

import java.util.List;

public interface UserHandleService {
    public List<User> selectAll();
    public void insertOneRow(User user);
}
