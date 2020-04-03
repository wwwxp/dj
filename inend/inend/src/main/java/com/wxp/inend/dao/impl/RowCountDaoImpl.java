package com.wxp.inend.dao.impl;

import com.wxp.inend.dao.RowCountDao;
import com.wxp.inend.mapper.RowCountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RowCountDaoImpl implements RowCountDao {

    @Autowired
    private RowCountMapper r;

    @Override
    public Integer foodRowCount() throws Exception {
        return r.foodRowCount();
    }
}
