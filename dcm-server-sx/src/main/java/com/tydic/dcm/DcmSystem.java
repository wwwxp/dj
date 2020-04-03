package com.tydic.dcm;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.dcm.client.DcmClientFactory;
import com.tydic.dcm.client.DcmClientManager;
import com.tydic.dcm.device.*;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.PropertiesUtil;
import com.tydic.dcm.util.tools.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


/**
 * @author Yuanh
 * <p>
 * DCM采集程序总入口
 */
@Slf4j
public class DcmSystem {

    /**
     * 随机数对象
     */
    public static Random random;

    /**
     * 系统日志对象
     */
    private static Logger logger = Logger.getLogger(DcmSystem.class);

    /**
     * 定时刷新采集任务
     */
    public static CollRefreshLinkThrd collRefreshLinkThrd = null;

    /**
     * 定时刷新分发任务
     */
    public static DistRefreshLinkThrd distRefreshLinkThrd = null;

    /**
     * 定时刷新告警任务
     */
    public static WarnRefreshLinkThrd warnRefreshLinkThrd = null;

    /**
     * 定时刷新链路参数
     */
    public static LinkDataRefreshThrd linkReloadDataThrd = null;

    /**
     * 分发异常回收线程处理
     */
    public static DistTaskAbnCallbackThrd abnCallbackThrd = null;

    /**
     * 文件连续性校验线程
     */
    public static CollSeqCheckThrd collSeqCheckThrd = null;

    /**
     * 任务调度状态监控
     */
    public static QuartzStatusMonitorThrd quartzStatusMonitorThrd = null;

    /**
     * 客户端管理对象
     */
    public static DcmClientManager clientManager = null;

    /**
     * 启动函数
     *
     * @throws Exception
     */
    public static void start() throws Exception {
        random = new Random(TimeTool.getTime());

        //加载配置,Spring配置文件
        loadConfig();

        //自动添加当前采集程序信息到DC_HOST_DEP_INFO表
//		recordHostInstance();

        //加载链路数据
        loadLinkData();

        //初始化告警任务
        initWarnTask();

        //初始化告警文件连续性
        initWarnSeqContinuity();

        //初始化采集任务
        initCollTask();

        //校验分发异常重复数据
        checkDistDuplicateData();

        //初始化分发任务
        initDistTask();

        //分发异常回收
        initDistTaskAbnCallBack();

        //初始化客户端
        initClient();

        //任务调度状态监控
        initQuartzStatusMonitor();

        System.out.println("Dcm start OK!");
    }

    /**
     * 往主机实例表添加记录
     *
     * @Title: recordHostInstance
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: void
     * @author: tianjc
     * @date: 2017年7月26日 上午10:41:53
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private static void recordHostInstance() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();

            String hostIp = addr.getHostAddress();
            String hostName = addr.getHostName();
            String servPort = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_SERVER_PORT);
            String groupId = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_GROUP_ID);

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("IP", hostIp);
            param.put("PORT", servPort);
            param.put("GROUP_ID", groupId);
            List<Map<String, Object>> hostList = JdbcUtil.queryForList("collectMapper.queryHostList", param, FrameConfigKey.DEFAULT_DATASOURCE);

            if (CollectionUtils.isEmpty(hostList)) {
                logger.debug("add record to DC_HOST_DEP_INFO");
                param.put("HOST_NAME", hostName + ":" + servPort);

                //按照groupID查询，如果groupID已经存在就只能插入主机ip了
                hostList = JdbcUtil.queryForList("collectMapper.queryHostGroupList", param, FrameConfigKey.DEFAULT_DATASOURCE);
                if (!CollectionUtils.isEmpty(hostList)) {
                    param.put("GROUP_ID", "");
                }

                JdbcUtil.insertObject("collectMapper.addHost", param, FrameConfigKey.DEFAULT_DATASOURCE);
            }

        } catch (UnknownHostException e) {
            logger.error("get host info fail", e);
        }
    }

    /**
     * 获取随机数
     *
     * @param rand
     * @return
     */
    public static int random(int rand) {
        return random.nextInt(rand);
    }

    /**
     * 采用云管理平台启动采集程序,将配置文件复制到本地
     *
     * @param args
     * @Title: initConfigFile
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: void
     * @author: tianjc
     * @date: 2017年6月20日 上午9:44:20
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private static void initConfigFile(String[] args) {
        try {
            String[] defaultConfigFiles = new String[]{"config.properties", "log4j.properties", "fastdfs_client.properties"};

            String configPath = args[0];
            String[] strArray = configPath.split(",");

            String path = "";
            if (ArrayUtils.getLength(strArray) > 0) {
                path = StringUtils.substring(strArray[0], 0, strArray[0].lastIndexOf("/"));
                logger.info("programe path:" + path);
            }

            List<String> fileList = new ArrayList<String>();
            for (String defaultConfigFile : defaultConfigFiles) {
                boolean isContained = false;
                for (String fileName : strArray) {
                    if (StringUtils.endsWith(fileName, defaultConfigFile)) {
                        isContained = true;
                        fileList.add(fileName);
                        break;
                    }
                }

                if (!isContained) {
                    fileList.add("../cfg/" + defaultConfigFile);
                }
            }

            String currentPath = System.getProperty("user.dir");
            logger.info("programe work dir:" + currentPath);

            strArray = fileList.toArray(strArray);
            for (String fileName : strArray) {
                if (StringUtils.isNotBlank(fileName)) {
                    String sourceFile = fileName;
                    if (StringUtils.indexOf(fileName, "/") == -1) {
                        sourceFile = path + File.separator + fileName;
                    }

                    String targetFile = currentPath + File.separator + StringUtils.substring(fileName, fileName.lastIndexOf("/") + 1);
                    //相同文件就不需要拷贝了
                    if (StringUtils.equals(sourceFile, targetFile)) {
                        break;
                    }

                    FileInputStream fis = null;
                    FileOutputStream fos = null;
                    try {
                        File inputFile = new File(sourceFile);
                        File outputFile = new File(targetFile);

                        if (inputFile.exists() == false) {
                            throw new FileNotFoundException("config file not found:" + sourceFile);
                        }
                        if (outputFile.exists() == false) {
                            outputFile.createNewFile();
                        }

                        logger.info("sourceFile:" + sourceFile + " ---> targetFile：" + targetFile);

                        fis = new FileInputStream(inputFile);
                        fos = new FileOutputStream(outputFile);
                        byte[] buffer = new byte[512];

                        for (int len = 0; (len = fis.read(buffer)) != -1; ) {
                            fos.write(buffer, 0, len);
                            fos.flush();
                        }
                    } catch (Exception e) {
                        logger.error("read config file error!" + sourceFile, e);
                    } finally {
                        if (fis != null) {
                            fis.close();
                        }

                        if (fos != null) {
                            fos.close();
                        }
                    }

                }
            }
        } catch (Exception e) {
            logger.error("参数有误,请检查参数!", e);
            System.exit(-1);
        }
    }

    /**
     * 加载配置文件
     */
    public static void loadConfig() throws Exception {
        logger.info("start load config...");

        //加载配置文件
        new ClassPathXmlApplicationContext(new String[]{"conf/spring-config.xml"});

        logger.info("load config success");
        System.out.println("Load Config OK!");
    }

    /**
     * 加载采集任务
     */
    public static void initCollTask() {
        Boolean isActive = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_COLL_INIT_ACTIVE, Boolean.TRUE);
        logger.debug("init collect task, isActive:" + isActive);
        if (isActive) {
            //刷新采集间隔时间,默认间隔时间为5秒
            String heart = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_COLL_HEART, ParamsConstant.PARAMS_5);
            Long refreshInterval = Long.parseLong(heart) * 1000;

            //采集实例当前GroupID
            String groupId = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_GROUP_ID);

            //创建采集线程对象
            collRefreshLinkThrd = new CollRefreshLinkThrd(refreshInterval, groupId);
            //设置线程优先级
            collRefreshLinkThrd.setPriority(Thread.MAX_PRIORITY);
            //启动定时刷新采集线程
            collRefreshLinkThrd.start();
            logger.info("init collect task success, refreshInterval: " + heart + " seconds, groupId:" + groupId);
            System.out.println("init collect task OK, refreshInterval: " + heart + " seconds, groupId:" + groupId);
        }
        logger.debug("init collect task ok!");
    }

    /**
     * 分发异常回收
     */
    public static void initDistTaskAbnCallBack() {
        Boolean isActive = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_DIST_TASK_ABN_CALLBACK_ACTIVE, Boolean.FALSE);
        logger.info("init distribute task abn callback, isActive: " + isActive);
        System.out.println("init distribute task abn callback, isActive: " + isActive);
        if (isActive) {
            //采集实例当前GroupID
            String groupId = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_GROUP_ID);
            abnCallbackThrd = new DistTaskAbnCallbackThrd(groupId);
            abnCallbackThrd.start();
            logger.info("distribute task abn callback start success, groupId: " + groupId);
            System.out.println("distribute task abn callback start success, groupId: " + groupId);
        }
    }

    /**
    * @Description: 校验分发异常数据
    * @return void
    * @author yuanhao
    * @date 2019-11-19 9:07
    */
    public static void checkDistDuplicateData() {
        //分发实例当前GroupID
        String groupId = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_GROUP_ID);
        logger.info("check distribute duplication data start, groupId: " + groupId);
        DistTaskAbnCheck.checkDuplicateData(groupId);
        logger.info("check  distribute duplication data end!");
    }

    /**
     * 初始化分发任务
     */
    public static void initDistTask() {
        Boolean isActive = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_DIST_INIT_ACTIVE, Boolean.TRUE);
        logger.debug("init distribute task, isActive:" + isActive);
        if (isActive) {

            //刷新分发间隔时间,默认间隔时间为5秒
            String heart = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_DIST_HEART, ParamsConstant.PARAMS_5);
            Long refreshInterval = Long.parseLong(heart) * 1000;

            String distThrdSize = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_DIST_THREAD_SIZE, ParamsConstant.PARAMS_5);

            //分发实例当前GroupID
            String groupId = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_GROUP_ID);

            distRefreshLinkThrd = new DistRefreshLinkThrd(refreshInterval, groupId, Integer.parseInt(distThrdSize));
            //加载所有的本地网信息
            distRefreshLinkThrd.loadLatnList();
            //设置线程优先级
            distRefreshLinkThrd.setPriority(Thread.MAX_PRIORITY);
            //启动定时刷新分发线程
            distRefreshLinkThrd.start();

            logger.info("init distribute task success, refreshInterval: " + heart + " seconds, groupId: " + groupId);
            System.out.println("init distribute task OK, refreshInterval: " + heart + " seconds, groupId: " + groupId);
        }
        logger.debug("init distribute task ok!");
    }

    /**
     * 初始化告警任务
     */
    public static void initWarnTask() {
        logger.debug("init warn task for check file collect time");

        //刷新分发间隔时间,默认间隔时间为5秒
        String heart = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_WARN_HEART, ParamsConstant.PARAMS_5);
        Long refreshInterval = Long.parseLong(heart) * 1000;

        //自动采集是否开启
        Boolean isCollActive = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_COLL_INIT_ACTIVE, Boolean.TRUE);

        //自动分发是否开启
        Boolean isDistActive = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_DIST_INIT_ACTIVE, Boolean.TRUE);
        //告警刷新
        warnRefreshLinkThrd = new WarnRefreshLinkThrd(refreshInterval, isCollActive, isDistActive);
        warnRefreshLinkThrd.start();

        System.out.println("warn task OK, refresh interval: " + heart + " seconds");
        logger.debug("init warn task for check file collect time ok!");
    }

    /**
     * 间隔刷新采集、分发链路参数属性
     */
    public static void loadLinkData() {
        logger.debug("init load link parameters and attributes");

        //刷新分发间隔时间,默认间隔时间为5秒
        String linkRefreshInterval = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_LINK_REFRESH_INTERVAL, ParamsConstant.PARAMS_5);
        Long refreshInterval = Long.parseLong(linkRefreshInterval) * 1000;

        //自动采集是否开启
        Boolean isCollActive = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_COLL_INIT_ACTIVE, Boolean.TRUE);

        //自动分发是否开启
        Boolean isDistActive = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_DIST_INIT_ACTIVE, Boolean.TRUE);

        //当前采集GroupID
        String groupId = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_GROUP_ID);
        //告警刷新
        linkReloadDataThrd = new LinkDataRefreshThrd(refreshInterval, groupId, isCollActive, isDistActive);
        linkReloadDataThrd.init();
        linkReloadDataThrd.start();

        System.out.println("init load link parameters and attribute OK, refresh interval: " + linkRefreshInterval + " seconds");
        logger.debug("init load link parameters and attributes OK!");
    }

    /**
     * 文件连续性校验
     */
    public static void initWarnSeqContinuity() {
        logger.debug("init warn task for check file continuity");
        collSeqCheckThrd = new CollSeqCheckThrd();
        collSeqCheckThrd.start();
        System.out.println("check file continuity task ok!");
        logger.debug("init warn task for check file continuity ok!");
    }

    /**
     * 任务调度状态监控
     */
    public static void initQuartzStatusMonitor() {
        logger.debug("init quartz status monitor");
        String monitorHeart = PropertiesUtil.getValueByKey("monitor_heart", ParamsConstant.PARAMS_5);
        if (Integer.parseInt(monitorHeart) > 0) {
            quartzStatusMonitorThrd = new QuartzStatusMonitorThrd(Integer.parseInt(monitorHeart));
            quartzStatusMonitorThrd.start();
            System.out.println("init quartz status monitor ok, resfreshTimes: " + monitorHeart + " seconds");
        }
        logger.debug("init quartz status monitor OK!");
    }

    /**
     * 初始化客户端
     */
    public static void initClient() {
        logger.debug("begin init client");
        String servPort = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_SERVER_PORT);
        logger.debug("init client, server port:" + servPort);

        clientManager = new DcmClientManager(Integer.parseInt(servPort), new DcmClientFactory());
        if (clientManager.open()) {
            clientManager.start();
            logger.debug("init client success");
            System.out.println("init client OK, Server Port: " + servPort);
        } else {
            logger.debug("***init client fail***");
            System.out.println("init client fail. Please check param: " + ParamsConstant.PARAMS_SERVER_PORT + " value");
        }
    }

    /**
     * DCM采集程序退出
     */
    public static void exit() {
        if (collRefreshLinkThrd != null) {
            collRefreshLinkThrd.exit();
            System.out.println("SystemExit to collRefreshLinkThrd !");
        }
        if (warnRefreshLinkThrd != null) {
            warnRefreshLinkThrd.exit();
            System.out.println("SystemExit to warnRefreshLinkThrd");
        }
        if (clientManager != null) {
            clientManager.exit();
            System.out.println("SystemExit to clientManager!");
        }
        Runtime.getRuntime().exit(0);
    }

    public static void main(String[] args) {
        try {
            log.info("开始启动DcmServer");
            System.out.println("===== DIC-DCM-SX Version 1.0.0  (2018/10/30) =====");
            start();
        } catch (Exception e) {
            logger.error("dcm start fail.", e);
            e.printStackTrace();
            exit();
        } catch (Error er) {
            logger.error("dcm start error.", er);
            er.printStackTrace();
            exit();
        } catch (Throwable the) {
            logger.error("dcm start error.", the);
            the.printStackTrace();
            exit();
        }
    }
}
