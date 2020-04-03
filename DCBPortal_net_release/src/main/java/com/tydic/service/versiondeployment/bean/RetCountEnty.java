package com.tydic.service.versiondeployment.bean;

import java.util.concurrent.atomic.AtomicInteger;

public class RetCountEnty {

    private AtomicInteger sumCount = new AtomicInteger(0);
    private AtomicInteger succCount = new AtomicInteger(0);
    private AtomicInteger failCount = new AtomicInteger(0);

    public RetCountEnty(int sumCount) {
        setSumCount(sumCount);
    }

    public RetCountEnty() {
    }

    public void setSumCount(int sumCount) {
        this.sumCount.set(sumCount);
    }

    public int markSuc(int sucNum) {
        return succCount.getAndAdd(sucNum);
    }

    public int markSuc() {
        return markSuc(1);
    }

    public int markFail() {
        return markFail(1);
    }

    public int markFail(int falNum) {
        return failCount.getAndAdd(falNum);
    }

    public int markRet(boolean isSuc) {
        return isSuc ? markSuc() : markFail();
    }


    public int getFailCount() {
        return failCount.get();
    }

    public int getSuccCount() {
        return succCount.get();
    }

    public int getSumCount() {
        return sumCount.get();
    }

    @Override
    public String toString() {
        int sum = getSumCount();
        int suc = getSuccCount();
        int fail = getFailCount();
        float sucRet = sum == 0 ? 0 : ((float) getSuccCount() / getSumCount());
        return String.format("总数[%s],成功[%s],失败[%s],成功率[%.2f%%]", sum, suc, fail, sucRet*100);
    }
}
