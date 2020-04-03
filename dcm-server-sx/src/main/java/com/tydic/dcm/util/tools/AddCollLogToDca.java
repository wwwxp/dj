package com.tydic.dcm.util.tools;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.dcm.ftran.FileRecord;
import com.tydic.dcm.util.jdbc.DcaUtil;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

public class AddCollLogToDca {
    static {
        new ClassPathXmlApplicationContext(new String[]{"conf/spring-config.xml"});
    }

    private static String ALL_GROUP_FLAG = "all";
    private static int ONCE_MOVE_NUMBER = 10000;

    // Redis中key存活时间(单位:天)，默认90天
    private static int redis_expire_time = NumberUtils.toInt(PropertiesUtil.getValueByKey(ParamsConstant.DCA_EXPIRE_TIME), 90) * 24 * 3600;
    private static Logger logger = Logger.getLogger(AddCollLogToDca.class);

    public static void main(String[] args) throws Exception{
        //获得天数
        int days = 0;
        if(args.length == 0){
            days = Integer.parseInt(PropertiesUtil.getValueByKey(ParamsConstant.DCA_EXPIRE_TIME));
        }else{
            if(!args[0].matches("^\\-?\\d+$")){
                throw new IllegalArgumentException("parameter error: days can only be integer");
            }
            days = Integer.parseInt(args[0]);
            if(days<0){
                days = Integer.parseInt(PropertiesUtil.getValueByKey(ParamsConstant.DCA_EXPIRE_TIME));
            }
        }
        String startTime = DateUtil.getCurrent(DateUtil.allPattern);
        long startTimestamp = System.currentTimeMillis();
        String startDate = getStartDateByDays(days);
        String nowDate = new SimpleDateFormat("yyyy-MM-ss").format(new Date());

        logger.info("start time:"+startTime);
        logger.info("begin dc_coll_log to DCA...");
        logger.info("time range of logging："+startDate+" ~ "+nowDate);

        boolean useCurrentGroup = true;
        if(args.length >= 2 && args[1].equals(ALL_GROUP_FLAG)){
            useCurrentGroup = false;
        }

        Map<String,Object> param = new HashMap<>();
        param.put("START_DATE",startDate);
        param.put("ONCE_MOVE_NUMBER",ONCE_MOVE_NUMBER);

        //采集当前组时，则获得groupId对应的链路Id
        String groupInfo = null;
        if(useCurrentGroup){
            String groupId = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_GROUP_ID);
            Map<String,Object> queryLinkId = new HashMap<String,Object>(){{ put("GROUP_ID",groupId); }};
            List<Map<String,Object>> allLinkId = JdbcUtil.queryForList("collectMapper.queryLinkIdByGroupId", queryLinkId, FrameConfigKey.DEFAULT_DATASOURCE);
            List<String> linkIds = new ArrayList<>();
            for(Map<String,Object> linkId : allLinkId){
                linkIds.add(StringTool.object2String(linkId.get("DEV_ID")));
            }
            param.put("LINK_IDS",linkIds);
            groupInfo = "group_id：" + groupId + ", link_ids：" + linkIds;

        }else {
            groupInfo = "group_id: ALL，link_ids: ALL";
        }
        logger.info(groupInfo);

        List<Map<String, Object>> onceCollLogList = null;
        int startMoveNum = 0;
        double threadNum = 0;
        int logNum = 0;
        long[] count = new long[3];
        //线程池
        ExecutorService executorService = new ThreadPoolExecutor(6, 20, 10L, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(1024));

        while (true) {
            //获得移动一次的日志数据
            param.put("START_MOVE_NUMBER",startMoveNum);
            onceCollLogList = JdbcUtil.queryForList("collectMapper.queryNeedMoveCollLog", param, FrameConfigKey.DEFAULT_DATASOURCE);

            logNum += onceCollLogList.size();
            if(onceCollLogList.size() == 0){
                break;
            }

            threadNum = Math.ceil(onceCollLogList.size() / 2000.0);
            List<List<Map<String,Object>>> lists=averageAssign(onceCollLogList,(int)threadNum);

            List<Future<long[]>> futureList = new ArrayList<>();
            for(int i=0,length=lists.size();i<length;++i){
                LogAddTask task = new LogAddTask(lists.get(i));
                Future<long[]> future = executorService.submit(task);
                futureList.add(future);
            }

            futureListHandle(futureList,count);

            startMoveNum = startMoveNum + ONCE_MOVE_NUMBER;
        }
        long endTime = (System.currentTimeMillis()-startTimestamp)/1000/60;
        logger.info("add log table data record to DCA，total log records："+logNum);
        logger.info("number of all keys："+logNum*3+"，number of keys written successfully："+count[0]+"，number of keys failed to write："+count[1]+"，number of existing keys："+count[2]);
        logger.info("start time:" + startTime);
        logger.info("time range of logging：" + startDate + " ~ " + nowDate);
        logger.info(groupInfo);
        logger.info("end time:"+DateUtil.getCurrent(DateUtil.allPattern)+"，["+endTime+"] minutes");
        System.exit(0);
    }

    /**
     * 对线程的返回值进行统计
     * @param futureList
     * @param count
     */
    private static void futureListHandle(List<Future<long[]>> futureList,long[] count){
        for (Future<long[]> future:futureList){
            long[] onceCount = null;
            try {
                onceCount = future.get();
            }catch (Exception e){
                e.printStackTrace();
            }
            count[0] += onceCount[0];
            count[1] += onceCount[1];
            count[2] += onceCount[2];
        }
    }

    /**
     * 线程
     */
    private static class LogAddTask implements Callable<long[]> {
        private List<Map<String,Object>> logList = null;

        LogAddTask(List<Map<String,Object>> logList){
            this.logList = logList;
        }

        @Override
        public long[] call() {
            long[] count = new long[3];

            try {
                FileRecord fileRecord = null;
                String linkId = null;
                long[] onceCount = null;
                for (int i = 0, length = logList.size(); i < length; ++i) {
                    fileRecord = new FileRecord();
                    fileRecord.setFileName(StringTool.object2String(logList.get(i).get("ORI_FILE_NAME")));
                    fileRecord.setFilePath(StringTool.object2String(logList.get(i).get("ORI_PATH")));
                    fileRecord.setTime(DateUtil.parse(StringTool.object2String(logList.get(i).get("ORI_FILE_TIME")),DateUtil.allPattern));
                    linkId = StringTool.object2String(logList.get(i).get("LINK_ID"));
                    onceCount = addDistTaskToRedis(linkId,fileRecord);
                    count[0] += onceCount[0];
                    count[1] += onceCount[1];
                    count[2] += onceCount[2];
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return count;
        }

    }

    /**
     *
     * @param days
     * @return
     * @throws Exception
     */
    private static String getStartDateByDays(int days) throws Exception{
        LocalDate nowDate = LocalDate.now();
        LocalDate localDate = nowDate.minusDays(days);
        return localDate.toString();
    }

    /**
     * 将一组数据平均分成n组
     *
     * @param source 要分组的数据源
     * @param n      平均分成n组
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();
        int remainder = source.size() % n;  //(先计算出余数)
        int number = source.size() / n;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> valueList = null;
            List<T> vectorList = null;
            if (remainder > 0) {
                valueList = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                valueList = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            vectorList = new ArrayList<T>(valueList.size());
            vectorList.addAll(valueList);
            result.add(vectorList);
        }
        return result;
    }

    /**
     * 往dca添加记录
     * @param devID
     * @param fileRecord
     */
    public static long[] addDistTaskToRedis(String devID, FileRecord fileRecord) {
        logger.info("begin add a log to DCA, file info : " + fileRecord + ", devId: " + devID);
        String dateStr = DateUtil.format(fileRecord.getTime(), DateUtil.fullPattern);
        //域
        String keyField = SystemProperty.getContextProperty(ParamsConstant.DCA_KEY_FIELD);

        String keyFileName = keyField + devID + "#" + fileRecord.getFileName();
        String keyFilePath = keyField + devID + "#" + fileRecord.getFileName() + "#" + fileRecord.getFilePath();
        String keyFileTime = keyField + devID + "#" + fileRecord.getFileName() + "#" + fileRecord.getFilePath() + "#" + dateStr;
        logger.info("DCA data, key: " + keyFileTime + ", devId: " + devID);

        //是否将所有的Key转化为Hash进行处理
        boolean filterHash = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_FILE_FILTER_HASH, Boolean.FALSE);
        logger.info("start adding a log data to DCA...，key filter with hashcode: " + filterHash);
        if (filterHash) {
            keyFileName = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName());
            keyFilePath = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName() + "#" + fileRecord.getFilePath());
            keyFileTime = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName() + "#" + fileRecord.getFilePath() + "#" + dateStr);
            logger.debug("SHA256 name key: " + keyFileName);
            logger.debug("SHA256 path key: " + keyFilePath);
            logger.debug("SHA256 time key: " + keyFileTime);
        }

        String finalDevId = devID + "_" + Thread.currentThread().getId();
        int successCount = 0;
        int failCount = 0;
        int repeatCount = 0;
        try {
            boolean added = false;
            if (!DcaUtil.exists(finalDevId, keyFileName)) {
                added = DcaUtil.setex(finalDevId, keyFileName, redis_expire_time, "1");
                if (!added) {
                    throw new RuntimeException("failed to add key,key:" + keyFileName);
                }else{
                    successCount ++;
                    logger.info("key added successfully，key:"+keyFileName);
                }
            }else{
                repeatCount ++;
                logger.info("key already exists,key:" + keyFileName);
            }

            if (!DcaUtil.exists(finalDevId, keyFilePath)) {
                added = DcaUtil.setex(finalDevId, keyFilePath, redis_expire_time, "1");
                if (!added) {
                    throw new RuntimeException("failed to add key,key:" + keyFilePath);
                }else{
                    successCount ++;
                    logger.info("key added successfully，key:"+keyFilePath);
                }
            }else{
                repeatCount ++;
                logger.info("key already exists,key:" + keyFilePath);
            }
            if (!DcaUtil.exists(finalDevId, keyFileTime)) {
                added = DcaUtil.setex(finalDevId, keyFileTime, redis_expire_time, "1");
                if (!added) {
                    throw new RuntimeException("failed to add key,key:" + keyFileTime);
                }else{
                    successCount ++;
                    logger.info("key added successfully，key:"+keyFileTime);
                }
            }else{
                repeatCount ++;
                logger.info("key already exists,key:" + keyFileTime);
            }
        } catch (Exception e) {
            logger.info("add DCA exception, key: " + keyFileTime + ", devId: " + devID, e);
            DcaUtil.delKey(finalDevId, keyFileName, keyFilePath, keyFileTime);
            failCount +=3;
            successCount = 0;
            repeatCount = 0;
            logger.info("delete the data added to DCA, key: " + keyFileTime + ", devId: " + devID);
        }
        logger.info("end of add a log data to DCA..., devId: " + devID);

        return new long[]{successCount,failCount,repeatCount};
    }

}
