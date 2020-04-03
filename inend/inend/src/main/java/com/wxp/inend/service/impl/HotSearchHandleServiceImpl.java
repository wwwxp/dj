package com.wxp.inend.service.impl;

import com.wxp.inend.dao.HotSearchHandleDao;
import com.wxp.inend.entity.HotSearchTable;
import com.wxp.inend.service.HotSearchHandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotSearchHandleServiceImpl implements HotSearchHandleService {

    @Autowired
    private HotSearchHandleDao hshd;

    @Override
    public List<HotSearchTable> selectAll() {
        return hshd.selectAll();
    }
}
