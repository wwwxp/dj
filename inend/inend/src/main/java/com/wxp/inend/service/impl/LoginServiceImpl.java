package com.wxp.inend.service.impl;

import com.wxp.inend.dao.LoginDao;
import com.wxp.inend.entity.User;
import com.wxp.inend.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginDao ld;

    @Override
    public User userLogin(String userName, String userPwd) throws Exception {
        return ld.userLogin(userName,userPwd);
    }
}
