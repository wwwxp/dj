package com.tydic.dcm.task;

import com.tydic.dcm.ftran.DistLink;
import com.tydic.dcm.ftran.TransItem;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * Auther: Yuanh
 * Date: 2019-05-24 15:10
 * Description:
 */
public class DistMultTask implements Runnable {

    //任务名称
    private String taskName;

    //分发链路ID
    private String devId;

    //计数器
    private CountDownLatch latch;

    //本地分发文件列表
    private Vector<TransItem> fileList;

    /**
     * 分发文件任务
     * @param taskName
     * @param devId
     * @param fileList
     */
    public DistMultTask(String taskName, String devId, CountDownLatch latch, Vector<TransItem> fileList) {
        this.taskName = taskName;
        this.devId = devId;
        this.latch = latch;
        this.fileList = fileList;
    }

    @Override
    public void run() {
        try {
            //获取分发链路列表
            DistLink link = new DistLink(devId);
            link.subAutoMultTransfer(taskName, fileList);
        } finally {
            latch.countDown();
        }
    }
}
