package com.wxp.inend.service;


import com.wxp.inend.entity.Admin;

import java.util.List;

public interface AdminHandleService {
    public List<Admin> selectAll();
    public List<Admin> selectByAdminNameAndPwd(String adminName,String pwd);
}
