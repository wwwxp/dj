package com.tydic.bp.service.impl;

import com.ctg.ctdfs.core.common.DFSContext;
import com.tydic.bp.service.DFSService;
import com.tydic.bp.util.FileRecord;
import com.tydic.bp.util.PropUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * hdfs分布式文件系统
 */
@Slf4j
public class HdfsDFSServiceImpl implements DFSService {

    //协议
    public static String PROTOCOL_PREFIX = "dfs://";

    private  Configuration configuration = new Configuration();

    private String mountPoint;
    {
        mountPoint = PropUtils.getProperty("dfs.mount_point");
    }

    @Override
    public void init() throws IOException {
        String dfsMountPoint = mountPoint;
        dfsMountPoint = StringUtils.removeEnd(dfsMountPoint, "/") + "/";

        log.info("加载hdfs分布式文件系统配置文件,默认挂载点:{}",dfsMountPoint);
        DFSContext.loadDefaultConfigs();
        Path path = new Path(dfsMountPoint);
        path.getFileSystem(configuration);
    }

    @Override
    public void destory() {
        log.info("关闭分布式文件系统");
    }

    @Override
    public List<FileRecord> list(String dfsFilePath) throws Exception {
        dfsFilePath = this.getRealPath(dfsFilePath);
        log.info("hdfs列表:{}", dfsFilePath);
        Path path = new Path(dfsFilePath);
        FileSystem fileSystem = path.getFileSystem(configuration);

        FileStatus[] fileStatuses = fileSystem.listStatus(path);
        List<FileRecord> fileRecords = new ArrayList<FileRecord>();
        for (FileStatus fileStatus : fileStatuses) {
            FileRecord fileRecord = new FileRecord();
            boolean isFile = fileStatus.isFile();
            fileRecord.setFileLength(fileStatus.getLen());
            fileRecord.setTime(new Date(fileStatus.getModificationTime()));
            fileRecord.setFileType(isFile ? FileRecord.FILE : FileRecord.DIR);


            if(isFile){
                fileRecord.setFilePath(dfsFilePath);
                fileRecord.setFileName(fileStatus.getPath().getName());

                fileRecords.add(fileRecord);
            } else {
                String subPath = dfsFilePath + "/" + fileStatus.getPath().getName() + "/";
                List<FileRecord> subFileList = list(subPath);

                fileRecords.addAll(subFileList);
            }

        }

        log.info("hdfs列表文件数:{}", CollectionUtils.size(fileRecords));
        return fileRecords;
    }

    @Override
    public void write(byte[] data, String dfsFilePath,String dateStr) throws Exception {
        dfsFilePath = this.getRealPath(dfsFilePath);
        log.info("往hdfs写入数据:{},字节数:{}", dfsFilePath, ArrayUtils.getLength(data));

        FSDataOutputStream fsDataOutputStream = null;
        try {
            Path target = new Path(dfsFilePath);
            FileSystem fileSystem = target.getFileSystem(configuration);
            boolean exists = fileSystem.exists(target);
            if (exists) {
                log.info("hdfs文件已经存在:{}", dfsFilePath);

                String newFilePath = dfsFilePath + dateStr;
                Path renamePath = new Path(newFilePath);
                boolean rename = fileSystem.rename(target, renamePath);
                log.info("重命名:{} -> {},结果:{}", dfsFilePath, newFilePath, rename);
            }

            //创建文件存储目录
            fileSystem.mkdirs(target.getParent());

            //开始创建文件
            fsDataOutputStream = fileSystem.create(target);
            fsDataOutputStream.write(data);

            fsDataOutputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("hdfs异常", e);
        } finally {
            IOUtils.closeQuietly(fsDataOutputStream);
        }
    }

    @Override
    public byte[] read(String dfsFilePath) throws Exception {
        dfsFilePath = this.getRealPath(dfsFilePath);
        log.info("从hdfs读取数据:{}", dfsFilePath);

        FSDataInputStream fsDataInputStream = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] data = null;
        try {
            Path path = new Path(dfsFilePath);
            FileSystem fileSystem = path.getFileSystem(configuration);

            fsDataInputStream = fileSystem.open(path);
            IOUtils.copy(fsDataInputStream, bos);

            data = bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("hdfs异常", e);
        } finally {
            IOUtils.closeQuietly(fsDataInputStream);
            IOUtils.closeQuietly(bos);
        }

        log.info("从hdfs读取数据完成，字节:{}", ArrayUtils.getLength(data));
        return data;
    }

    @Override
    public boolean delete(String dfsFilePath) {
        boolean result = false;

        dfsFilePath = this.getRealPath(dfsFilePath);
        log.info("从hdfs删除文件:{}", dfsFilePath);

        try {
            Path path = new Path(dfsFilePath);
            FileSystem fileSystem = path.getFileSystem(configuration);
            if(fileSystem.exists(path)){
                result = fileSystem.delete(path, false);
            }else {
                result = true;
            }
        } catch (Exception e) {
            log.error("从hdfs删除文件失败", e);
        }

        log.info("从hdfs删除文件:{}，结果：{}", dfsFilePath, result);
        return result;
    }

    @Override
    public boolean rename(String sourceFile, String targetFile) {
        boolean result = false;
        sourceFile = this.getRealPath(sourceFile);
        targetFile = this.getRealPath(targetFile);

        log.info("hdfs文件重命名:{} -> {}", sourceFile, targetFile);

        try {
            Path source = new Path(sourceFile);
            FileSystem fileSystem = source.getFileSystem(configuration);
            boolean exists = fileSystem.exists(source);
            if (exists) {
                Path target = new Path(targetFile);
                boolean rename = fileSystem.rename(source, target);
                log.info("hdfs重命名:{} -> {},结果:{}", sourceFile, targetFile, rename);
                result = rename;
            } else {
                log.info("hdfs重命名源文件不存在:{}", sourceFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("hdfs异常", e);
        }

        return result;
    }

    /**
     * 文件系统复制文件
     * @param sourceFile
     * @param targetFile
     * @return
     */
    @Override
    public void copyToLocalFile(String sourceFile, String targetFile) {
        sourceFile = this.getRealPath(sourceFile);
        targetFile = this.getRealPath(targetFile);
        log.info("hdfs文件复制到本地:{} -> {}", sourceFile, targetFile);

        try {
            Path source = new Path(sourceFile);
            FileSystem fileSystem = source.getFileSystem(configuration);
            boolean exists = fileSystem.exists(source);
            if (exists) {
                Path target = new Path(targetFile);
                fileSystem.copyToLocalFile(source, target);
                log.info("hdfs文件复制到本地成功:{} -> {}", sourceFile, targetFile);
            } else {
                log.info("hdfs重命名源文件不存在:{}", sourceFile);
                throw new RuntimeException("hdfs文件复制，源文件不存在，源文件:" + source);
            }
        } catch (Exception e) {
            throw new RuntimeException("hdfs文件复制异常", e);
        }
    }

    /**
     * 获取dfs实际路径
     *
     * @param path
     * @return
     */
    protected String getRealPath(String path) {
        if (!StringUtils.startsWith(path, PROTOCOL_PREFIX)) {
            //去除路径/
            while (StringUtils.startsWith(path, "/")) {
                path = StringUtils.removeStart(path, "/");
            }

            String dfsMountPoint = mountPoint;
            dfsMountPoint = StringUtils.removeEnd(dfsMountPoint, "/") + "/";
            return dfsMountPoint + path;
        } else {
            return path;
        }
    }


}
