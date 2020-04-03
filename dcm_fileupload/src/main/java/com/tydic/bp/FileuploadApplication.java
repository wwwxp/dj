package com.tydic.bp;

import com.tydic.bp.upload.FileUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.Resource;

@Slf4j
@SpringBootApplication
public class FileuploadApplication implements CommandLineRunner {
    @Resource
    private FileUpload fileUpload;

    @Value("${upload.refresh.time}")
    private int refreshTime;

    public static void main(String[] args) {
        SpringApplication.run(FileuploadApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        while(true) {
            try {
                log.info("开始");
                fileUpload.fileUpload();
                Thread.sleep(refreshTime * 1000);
            } catch (Exception e) {
                log.error("文件上传异常",e);
            }

        }
    }
}
