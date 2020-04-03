package com.wxp.inend.service.impl;

import com.wxp.inend.dao.UserHandleDao;
import com.wxp.inend.entity.User;
import com.wxp.inend.service.UserHandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserHandleServiceImpl implements UserHandleService {

    @Autowired
    private UserHandleDao uhd;

    @Override
    public List<User> selectAll() {
        return uhd.selectAll();
    }

    @Override
    public void insertOneRow(User user) {
        uhd.insertOneRow(user);
    }
}
