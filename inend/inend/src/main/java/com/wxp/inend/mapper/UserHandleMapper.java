package com.wxp.inend.mapper;

import com.wxp.inend.entity.User;

import java.util.List;

public interface UserHandleMapper {
    public List<User> selectAll();
    public void insertOneRow(User user);
}
