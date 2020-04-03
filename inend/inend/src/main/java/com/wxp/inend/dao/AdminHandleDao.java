package com.wxp.inend.dao;


import com.wxp.inend.entity.Admin;

import java.util.List;

public interface AdminHandleDao {
    public List<Admin> selectAll();
    public List<Admin> selectByAdminNameAndPwd(String adminName,String pwd);
}
