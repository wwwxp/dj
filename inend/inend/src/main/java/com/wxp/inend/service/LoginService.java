package com.wxp.inend.service;

import com.wxp.inend.entity.User;

public interface LoginService {
    User userLogin(String userName, String userPwd) throws Exception;
}
