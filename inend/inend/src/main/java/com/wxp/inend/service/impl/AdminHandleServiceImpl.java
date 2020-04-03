package com.wxp.inend.service.impl;

import com.wxp.inend.dao.AdminHandleDao;
import com.wxp.inend.entity.Admin;
import com.wxp.inend.service.AdminHandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminHandleServiceImpl implements AdminHandleService {

    @Autowired
    private AdminHandleDao ahd;

    @Override
    public List<Admin> selectAll() {
        return ahd.selectAll();
    }
    public List<Admin> selectByAdminNameAndPwd(String adminName,String pwd){
        return ahd.selectByAdminNameAndPwd(adminName,pwd);
    }
}
