package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.LoginDao;
import com.wxp.inend.entity.User;
import com.wxp.inend.mapper.LoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginDaoImpl implements LoginDao {

    @Autowired
    private LoginMapper lm;

    @Override
    public User userLogin(String userName, String userPwd) throws Exception {
        return lm.userLogin(userName,userPwd);
    }
}
