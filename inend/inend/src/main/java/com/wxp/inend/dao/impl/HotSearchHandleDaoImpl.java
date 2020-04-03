package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.HotSearchHandleDao;
import com.wxp.inend.entity.HotSearchTable;
import com.wxp.inend.mapper.HotSearchHandleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotSearchHandleDaoImpl implements HotSearchHandleDao {

    @Autowired
    private HotSearchHandleMapper hshm;

    @Override
    public List<HotSearchTable> selectAll() {
        return hshm.selectAll();
    }
}
