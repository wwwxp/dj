package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.UserHandleDao;
import com.wxp.inend.entity.User;
import com.wxp.inend.mapper.UserHandleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserHandleDaoImpl implements UserHandleDao {

    @Autowired
    private UserHandleMapper uhm;

    @Override
    public List<User> selectAll() {
        return uhm.selectAll();
    }

    @Override
    public void insertOneRow(User user) {
        uhm.insertOneRow(user);
    }
}
