package com.tydic.bp.upload;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.util.FileRecord;
import com.tydic.bp.util.FileTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
public class FileUpload {
    @Value("${upload.local.path}")
    private List<String> localPath = null;
    @Value("${upload.thread.size}")
    private int threadSize = 5;

    public void fileUpload(){
        //获取文件列表
        Vector<FileRecord> fileList = getFileList();
        log.info("文件列表："+fileList);

        if(fileList!=null && fileList.size()>100){
            //平均分配工作量
            Vector<Vector<FileRecord>> vectors = averageAssign(fileList, threadSize);
            CountDownLatch latch = new CountDownLatch(vectors.size());
            for(int i=0,length=vectors.size();i<length;++i){
                Thread worker = new Thread(new UploadThread(vectors.get(i),latch));
                worker.start();
            }
            try {
                latch.await();
            }catch (Exception e){
                log.error("mult-task count down latch fail.", e);
            }
        }else{
            new UploadThread().put(fileList);
        }
    }

    /**
     * 获得文件列表
     * @return
     */
    private Vector<FileRecord> getFileList(){
        log.info("开始获取文件列表..., 路径："+localPath);

        if(BlankUtil.isBlank(localPath)){
            throw new RuntimeException("路径不能为空");
        }

        Vector<FileRecord> fileList = new Vector<>();

        for(int i=0,length=localPath.size();i<length;++i){
            try {

                File localDir = new File(localPath.get(i));
                if (localDir.exists() && localDir.isDirectory()) {
                    Vector<FileRecord> onceList = FileTool.getSubList(localPath.get(i), new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            if (file.getName().endsWith(".tmp")) {
                                return false;
                            }
                            return true;
                        }
                    });
                    fileList.addAll(onceList);
                } else {
                    throw new RuntimeException("目录不存在，或者路径错误, filePath: "+localPath.get(i));
                }

            }catch (Exception e){
                log.error("文件获取异常",e);
            }
        }
        log.info("获得文件列表完成（不含.tmp后缀文件），文件列表长度：" + fileList.size());

        return fileList;
    }

    /**
     * 为每个线程平均分配工作量
     * @param fileList
     * @param threadSize
     * @return
     */
    private Vector<Vector<FileRecord>> averageAssign(Vector<FileRecord> fileList,int threadSize){
        int listLength = fileList.size();

        int remain = listLength % threadSize;
        int partValue = listLength / threadSize;
        int[] parts = new int[threadSize];
        Arrays.fill(parts,partValue);

        if(remain!=0){
            int i = 0;
            while(remain != 0){
                i = i % threadSize;
                parts[i]++;
                remain--;
                i++;
            }
        }

        Vector<Vector<FileRecord>> rstList = new Vector<>();
        int startIndex = 0;
        int endIndex = 0;
        List<FileRecord> subList = null;
        Vector<FileRecord> subVector = null;
        for(int i=0,length=parts.length;i<length;++i){
            endIndex = startIndex+parts[i];
            subList = fileList.subList(startIndex,endIndex);
            startIndex = endIndex;

            subVector = new Vector<>(subList.size());
            subVector.addAll(subList);
            rstList.add(subVector);
        }
        return rstList;
    }
}
