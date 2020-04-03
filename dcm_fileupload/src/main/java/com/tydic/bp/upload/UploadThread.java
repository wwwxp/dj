package com.tydic.bp.upload;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.service.DFSService;
import com.tydic.bp.util.*;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisCluster;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class UploadThread implements Runnable {

    private DFSService dfsClient;
    private Vector<FileRecord> fileList = null;

    private int retryTimes = 3;
    private String pid;
    private CountDownLatch latch;
    private RedisUtil redisUtil;

    UploadThread(){
        this(null,null);
    }

    UploadThread(Vector<FileRecord> fileList,CountDownLatch latch){
        this.fileList = fileList;
        this.pid = getPid();
        this.latch = latch;

        //手动获得ioc中的dfsClient、dcaInfo
        this.redisUtil=(RedisUtil)IOCUtils.getBean("redisUtil");
        this.dfsClient=(DFSService)IOCUtils.getBean("dfsService");
    }

    @Override
    public void run() {
        try {
            put(fileList);
        }finally {
            latch.countDown();
        }
    }

    /**
     * 上传文件列表
     * @param files
     */
    public void put(Vector<FileRecord> files) {
        log.info("--------------文件列表上传开始");

        for(int i=0,length=files.size();i<length;++i){
            addFileToDfs(files.get(i));
        }

        log.info("-------------文件列表上传结束");
    }

    /**
     * 上传单个文件到dfs
     * @param file
     */
    private void addFileToDfs(FileRecord file) {
        String key = null;
        String filePath = file.getFilePath();
        int step = 0;
        FileInputStream in = null;
        ByteArrayOutputStream bos = null;
        JedisCluster jedisCluster = null;
        log.info("开始上传文件, 源文件: "+filePath);
        try {
            String redisValue = "1";
            key = getDfsKey(file);
            String curDate = DateUtil.format(new Date(), DateUtil.fullPattern);

            jedisCluster = redisUtil.getCluster();
            //key是否已存在
            if (jedisCluster.exists(key)) {
                String suffix = "_${pid}_${date}_${random}"
                                .replace("${pid}",pid)
                                .replace("${date}",curDate)
                                .replace("${random}",new Random().nextInt(9000)+1000+"");
                key = key + suffix;
                redisValue = key;
                log.info("key在redis中已存在, key: "+key+", redis的value: "+redisValue);
            }else{
                log.info("key: "+key+", redis的value: "+redisValue);
            }

            //上传原文件到dfs
            in = new FileInputStream(filePath);
            bos = new ByteArrayOutputStream();
            byte[] byteData = new byte[1024];
            int number = 0;
            if ((number = in.read(byteData)) != -1) {
                bos.write(byteData, 0, number);
            }
            byte[] data = bos.toByteArray();
            //上传到dfs
            for (int i = 0; i < retryTimes; ++i) {
                log.debug("[" + (i + 1) + "] times up to dfs, dfsFileKey:" + key);

                dfsClient.write(data, key, curDate);

                log.info("文件上传成功到dfs, 源文件:"+filePath+", 目标文件:"+key);
                break;
            }
            step++;

            //插入到redis
            jedisCluster.sadd(key,redisValue);
            log.info("插入到redis中成功, 源文件:"+filePath+", 目标文件:"+key);
            step++;

            //备份，并删除源文件
            localLateOperator(file);
            log.info("备份，并删除源文件成功");
            step++;

        }catch (Exception e){
            log.error("文件上传失败, 源文件: "+filePath,e);
            try {
                if(step >= 1){
                    dfsClient.delete(key);
                    log.info("上传失败，fastdfs的数据回滚");
                }
                if(step >= 2){
                    jedisCluster.del(key);
                    log.info("上传失败，redis的数据回滚");
                }
            }catch (Exception e2){
                log.error("fastdfs文件回滚异常/redis回滚异常, key: "+key,e);
            }

        }finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }catch (Exception e){
                log.error("流的关闭异常",e);
            }
        }
    }

    private String getPid(){
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        String pid = name.substring(0,name.indexOf("@"));
        return pid;
    }

    /**
     * 上传后的后续操作
     * @param file
     * @throws Exception
     */
    private void localLateOperator(FileRecord file) throws Exception{
        String filePath = file.getFilePath();
        String localBak = PropUtils.getProperty("upload.local.path.bak");
        if(!BlankUtil.isBlank(localBak)){
            FileTool.copyFile(filePath,localBak+file.getFileName());
            log.info("源文件备份成功, 备份路径:"+localBak+", 源文件:"+filePath);
        }
        FileTool.delete(filePath);
        log.info("源文件删除成功, 源文件:"+filePath);
    }

    /**
     * 解析出dfs的key
     * @param file
     * @return
     */
    private String getDfsKey(FileRecord file) throws Exception{
        String name = file.getFileName();
        String mountPoint = PropUtils.getProperty("dfs.mount_point");
        String uploadPath = PropUtils.getProperty("upload.remote.path");

        if(!name.matches("^([^_]+_){3}[^_]+\\.idx$")){
            throw new RuntimeException("文件名的格式不正确, 源文件:"+file.getFilePath());
        }

        String[] nameSplits = name.split("_");
        StringBuffer keyBuffer = new StringBuffer();
        keyBuffer.append(FileTool.exactPath(mountPoint));

        if(!BlankUtil.isBlank(uploadPath)){
            keyBuffer.append(FileTool.exactPath(uploadPath));
        }

        keyBuffer.append(FileTool.exactPath(nameSplits[0]))
                .append(FileTool.exactPath(nameSplits[3].substring(0,nameSplits[3].lastIndexOf(".idx"))))
                .append(FileTool.exactPath(nameSplits[2]))
                .append(FileTool.exactPath(nameSplits[1]))
                .append(name);

        return keyBuffer.toString();
    }

}
