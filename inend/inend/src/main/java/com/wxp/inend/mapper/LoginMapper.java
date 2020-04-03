package com.wxp.inend.mapper;

import com.wxp.inend.entity.User;
import org.apache.ibatis.annotations.Param;

public interface LoginMapper {
    User userLogin(@Param(value="userName") String userName,@Param(value="userPwd") String userPwd) throws Exception;

}


