package com.tydic.dcm.ftran;

import lombok.Data;

import java.util.Hashtable;

/**
 * 传输文件对象
 *
 * @author Yuanh
 */
@Data
public class TransItem {

    // 源文件对象
    private FileRecord sourceFile = new FileRecord();

    // 目标文件对象
    private FileRecord targetFile = new FileRecord();

    // 文件对象参数
    private Hashtable<String, Object> params = new Hashtable<String, Object>();

    // 原文件重命名名称
    private String oriFileRename;
    // 源文件备份目录
    private String oriBakPath;
    // 文件后续操作
    private String lateHandleMethod = "";
    // 源文件后续操作
    private String oriLateHandleMethod = "";
    // 文件分发sourceId
    private String sourceId;

    // 文件处理结果
    private String errorMsg = "";

    private String collDevID = "";
    //采集链路数据源
    private String collDataSource = "";
    private String sourceName = "";
    // 本地网id
    private String latnId = "";
    // 批次号
    private int batchId = 0;
    //文件记录数，格式化写入
    private int lines = 0;
    //任务类型
    private int tskType = 0;

    //文件采集结果
    private boolean transferReuslt = false;

    public TransItem(FileRecord sourceFile) {
        // 源文件对象赋值
        this.sourceFile = sourceFile;
        this.oriFileRename = sourceFile.getFileName();
        this.oriBakPath = sourceFile.getOriPathBak();
    }

    public TransItem(FileRecord sourceFile, FileRecord targetFileFile) {
        // 源文件对象赋值
        this.sourceFile = sourceFile;
        this.oriFileRename = sourceFile.getFileName();
        this.oriBakPath = sourceFile.getOriPathBak();

        this.targetFile = targetFileFile;
    }

    /**
     * 目标文件是否需要删除
     *
     * @return
     */
    public boolean needDelete() {
        return lateHandleMethod.equals(Filter.LATE_HANDLE_DELETE);
    }

    /**
     * 目标文件是否需要重命令
     *
     * @return
     */
    public boolean needRename() {
        return lateHandleMethod.equals(Filter.LATE_HANDLE_RENAME);
    }

    /**
     * 源文件是否需要重命令
     *
     * @return
     */
    public boolean oriNeedRename() {
        return oriLateHandleMethod.equals(Filter.LATE_HANDLE_RENAME);
    }

    /**
     * 源文件是否需要删除
     *
     * @return
     */
    public boolean oriNeedDelete() {
        return oriLateHandleMethod.equals(Filter.LATE_HANDLE_DELETE);
    }

}
