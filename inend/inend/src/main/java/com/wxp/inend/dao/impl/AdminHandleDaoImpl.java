package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.AdminHandleDao;
import com.wxp.inend.entity.Admin;
import com.wxp.inend.mapper.AdminHandleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminHandleDaoImpl implements AdminHandleDao {

    @Autowired
    private AdminHandleMapper ahm;

    @Override
    public List<Admin> selectAll() {
        return ahm.selectAll();
    }

    @Override
    public List<Admin> selectByAdminNameAndPwd(String adminName,String pwd){
        return ahm.selectByAdminNameAndPwd(adminName,pwd);
    }
}
