package com.wxp.inend.mapper;


import com.wxp.inend.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminHandleMapper {
    public List<Admin> selectAll();

    public List<Admin> selectByAdminNameAndPwd(@Param(value="adminName") String adminName,@Param(value="pwd") String pwd);
}
