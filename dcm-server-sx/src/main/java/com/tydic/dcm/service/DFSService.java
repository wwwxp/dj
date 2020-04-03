package com.tydic.dcm.service;

import com.tydic.dcm.ftran.FileRecord;

import java.io.IOException;
import java.util.List;

/**
 * 分布式文件系统配置
 */
public interface DFSService {

    /**
     * 加载配置
     */
    public void init() throws IOException;

    /**
     * 销毁
     */
    public void destory();

    /**
     * 列举目录
     *
     * @param dfsFilePath
     * @return
     * @throws Exception
     */
    public List<FileRecord> list(String dfsFilePath) throws Exception;

    /**
     * 新增文件
     *
     * @param data
     * @param dfsFilePath
     * @param dateStr
     * @throws Exception
     */
    public void write(byte[] data, String dfsFilePath,String dateStr) throws Exception;

    /**
     * 读取文件
     *
     * @param dfsFilePath
     * @return
     * @throws Exception
     */
    public byte[] read(String dfsFilePath) throws Exception;


    /**
     * 删除文件
     *
     * @param dfsFilePath
     * @return
     */
    public boolean delete(String dfsFilePath) throws Exception;


    /**
     * dfs文件重命名
     * @param sourceFile
     * @param targetFile
     * @return
     */
    public boolean rename(String sourceFile,String targetFile);

    /**
     * dfs复制文件
     * @param sourceFile
     * @param targetFile
     * @return
     */
    public void copyToLocalFile(String sourceFile, String targetFile);
}
