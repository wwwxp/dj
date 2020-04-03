package com.wxp.inend.dao;

import com.wxp.inend.entity.User;

import java.util.List;

public interface UserHandleDao {
    public List<User> selectAll();
    public void insertOneRow(User user);
}
