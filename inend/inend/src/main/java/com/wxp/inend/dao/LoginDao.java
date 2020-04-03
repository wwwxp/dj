package com.wxp.inend.dao;

import com.wxp.inend.entity.User;

public interface LoginDao {
    User userLogin(String userName, String userPwd) throws Exception;
}
