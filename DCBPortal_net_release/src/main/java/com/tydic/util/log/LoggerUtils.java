package com.tydic.util.log;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Auther: Yuanh
 * Date: 2019-12-05 09:44
 * Description:
 */
public class LoggerUtils {

    //日志输出目录默认路径
    public static final String DEFAULT_LOG_PATH = "../logs/task/";

    //写入日志文件字符集编码
    public static final String LOG_ENCODING_GBK = "GBK";

    //日志输出字符集编码
    public static final String DEFAULT_LOG_ENCODING = "UTF-8";

    private static final String DEFAULT_LOG_PATTERN = "[%d{yyyy-MM-dd HH:mm:ss.SSS}][%5p][%C] %m%n";

    public static Logger getThreadLogger(String logPath, String logFileName) {
        return getThreadLogger(logPath, logFileName, null, DEFAULT_LOG_PATTERN, Level.DEBUG, false);
    }

    public static Logger getThreadLogger(String logPath, String logFileName, String threadName) {
        return getThreadLogger(logPath, logFileName, threadName, DEFAULT_LOG_PATTERN);
    }

    public static Logger getThreadLogger(String logPath, String logFileName, String threadName, String logPattern) {
        return getThreadLogger(logPath, logFileName, threadName, logPattern, Level.DEBUG);
    }

    /**
    * @Description: TODO
     * @param logPath 日志文件输出路径
     * @param logFileName 日志文件名称
     * @param threadName 日志线程名称
     * @param logPattern 日志输出格式
     * @param level 日志输出级别
    * @return org.apache.log4j.Logger
    * @author yuanhao
    * @date 2019-12-05 10:03
    */
    public static Logger getThreadLogger(String logPath, String logFileName, String threadName, String logPattern, Level level) {
        return getThreadLogger(logPath, logFileName, threadName, logPattern, level, false);
    }

    /**
    * @Description: 创建线程输出日志对象
    * @param logPath 日志文件输出路径
    * @param logFileName 日志文件名称
    * @param threadName 日志线程名称
    * @param logPattern 日志输出格式
    * @param level 日志输出级别
    * @param appendFlag 是否追加日志文件
    * @return org.apache.log4j.Logger
    * @author yuanhao
    * @date 2019-12-05 10:00
    */
    public static Logger getThreadLogger(String logPath, String logFileName, String threadName, String logPattern, Level level, boolean appendFlag) {
        threadName = StringUtils.isBlank(threadName) ? Thread.currentThread().getName() : threadName;
        level = (level == null) ? Level.DEBUG : level;
        logPath = StringUtils.isBlank(logPath) ? DEFAULT_LOG_PATH : StringUtils.removeEnd(logPath,"/") + "/";
        File file = new File(logPath);
        if (!file.exists()) {
            boolean filePathCT = file.mkdirs();
            System.out.println("创建日志文件目录:" + logPath + ", 结果: " + filePathCT);
        }
        logFileName = StringUtils.isBlank(logFileName) ? Thread.currentThread().getId() + ".log" : logFileName;
        logPattern = StringUtils.isBlank(logPattern) ? DEFAULT_LOG_PATTERN : logPattern;

        Logger logger = Logger.getLogger(threadName);
        //初始化日志文件
        logger.removeAllAppenders();
        //设置是否继承父Root日志，false默认不继承
        logger.setAdditivity(false);
        //设置日志文件输出级别
        logger.setLevel(level);
        //创建日志appender对象，用来输出日志
        FileAppender fileAppender = new FileAppender();
        PatternLayout layout = new PatternLayout();
        //log日志输出格式
        String conversionPattern = logPattern;
        layout.setConversionPattern(conversionPattern);
        fileAppender.setLayout(layout);
        //log日志输出路径以及日志文件名称
        fileAppender.setFile(logPath + logFileName);
        //log日志输出默认字符集编码
        fileAppender.setEncoding(DEFAULT_LOG_ENCODING);
        //是否追加日志文件，false：不追加，直接覆盖， true:追加日志文件
        fileAppender.setAppend(appendFlag);
        //适配当前配置
        fileAppender.activateOptions();
        //将当前appender添加到日志对象中
        logger.addAppender(fileAppender);
        return logger;
    }

    public static void main(String[] args) throws  Exception {
        File file = new File(".");
        if (file.exists()) {
            boolean ctOk = file.mkdirs();
            System.out.println("目录创建结果: " + ctOk);
        }
        System.out.println("current Path---> " + file.getPath());

        List<String> threadList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("创建日志文件中都是。。。。");
                    Logger logger = LoggerUtils.getThreadLogger("F:/data/task", Thread.currentThread().getName() + ".log");
                    logger.info("添加数据中国了打发点发发发" + Thread.currentThread().getName());

                    String ddd = "dssssssssss";
                    logger.debug("sdfaffffffffffffffffffff:" + ddd);

                    logger.warn("dssddssdsds");

                    logger.error("dsssssssss");
                    System.out.println("线程执行完成: " + Thread.currentThread().getName());
                }
            });
            threadList.add(thread.getName());
            thread.start();
        }

        Thread.sleep(5000);
        System.out.println("线程执行完成了.....");

        for (int i=0; i<threadList.size(); i++) {
            File aa = new File("F:/data/task/" + threadList.get(i) + ".log");
            if (aa.exists()) {
                boolean delOk = aa.delete();
                System.out.println("文件删除成功...." + delOk + "\t" + aa.getName());

                boolean renameOk = aa.renameTo(new File("F:/data/task/" + threadList.get(i) + "_BAK.log"));
                System.out.println("重命名结果: " + renameOk);
            }
        }

        Thread.currentThread().join();

    }
}
