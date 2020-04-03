package com.tydic.dcm.ftran;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.core.utils.db.DbContextHolder;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.dcm.util.jdbc.DcaUtil;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.spring.SpringUtil;
import com.tydic.dcm.util.tools.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 文件去重比对服务
 *
 * @ClassName: FileComparison
 * @Prject: dcm-server_base
 * @author: tianjc
 * @date 2016年7月26日 上午8:46:02
 */
public class FileComparison {

    private static Logger logger = Logger.getLogger(FileComparison.class);

    // 批量添加临时表一次性提交记录数(如果批量添加数据比较大不能一次性批量插入)
    private static final int db_commit_count = 500;

    // 文件对比剃重时间（单位：月）
    private static String coll_comparison_time = PropertiesUtil.getValueByKey(ParamsConstant.COLL_COMPARISON_TIME, ParamsConstant.PARAMS_1);

    // Redis中key存活时间(单位:天)，默认90天
    private static int redis_expire_time = NumberUtils.toInt(PropertiesUtil.getValueByKey(ParamsConstant.DCA_EXPIRE_TIME), 90) * 24 * 3600;

    /**
     * @param devID
     * @param list
     * @param checkCondition
     * @return
     */
    public static List<FileRecord> getEffectFileList(String devID, Vector<FileRecord> list, String checkCondition) {
        logger.debug("begin get effect file list, file list size: " + ArrayUtil.getSize(list) + ", checkCondition: "
                + checkCondition + ", devId: " + devID);
        String comparisonService = PropertiesUtil.getValueByKey(ParamsConstant.COLL_FILTER_TYPE,
                ParamsConstant.COLL_FILTER_TYPE_SQL);
        logger.debug("get effect file list, filter comparison type: " + comparisonService + ", devId: " + devID);

        List<FileRecord> uncollectList = new ArrayList<>();
        if (comparisonService.equalsIgnoreCase(ParamsConstant.COLL_FILTER_TYPE_SQL)) {// 通过SQL日志比对去重

            String sessionId = UUID.randomUUID().toString();
            try {

                if (CollectionUtils.isNotEmpty(list)) {
                    //已经采集过的文件
                    List<FileRecord> collectedList = getEffectFileListBySQL(sessionId, devID, list, checkCondition);

                    //排除采集过的文件
                    if (CollectionUtils.isNotEmpty(collectedList)) {
                        uncollectList = (List<FileRecord>) ListUtils.removeAll(list, collectedList);
                    } else {
                        //全部都是沒有采集过的文件，避免拷贝
                        uncollectList = list;
                    }
                }

            } catch (Exception e) {
                logger.error("采集sql比对去重发生异常,sessionId:" + sessionId, e);
                throw e;
            } finally {
                //删除临时表记录
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("SESSION_ID", sessionId);
                JdbcUtil.deleteObject("collectMapper.truncateTempTable", params, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.debug("delete temp files, devId: " + devID);
            }

        } else if (comparisonService.equalsIgnoreCase(ParamsConstant.COLL_FILTER_TYPE_DCA)) {// 通过Redis比对去重
            uncollectList = getEffectFileListByRedis(devID, list, checkCondition);
        }
        logger.debug(
                "end get effect file list, final file list size: " + ArrayUtil.getSize(uncollectList) + ", devId: " + devID);
        return uncollectList;
    }

    /**
     * @Title: getEffectFileListBySQL
     * @return: List<FileRecord>
     * @author: tianjc
     * @date: 2016年7月26日 上午10:38:05
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    public static List<FileRecord> getEffectFileListBySQL(String sessionId, String devID, Vector<FileRecord> list,
                                                          String checkCondition) {
        logger.debug("begin get effect file list by SQL, file list size: " + ArrayUtil.getSize(list)
                + ", checkCondition: " + checkCondition + ", devId: " + devID);
        // 获取数据库连接对象
        SqlSession sqlSession = SpringUtil.getCoreBaseDao().getSqlSession();
        // 设置上下文数据源
        DbContextHolder.setDbType(FrameConfigKey.DEFAULT_DATASOURCE);

        // 将文件数据添加到临时表
        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        Iterator<FileRecord> iter = list.iterator();
        while (iter.hasNext()) {
            FileRecord fileRecord = iter.next();
            Map<String, Object> fileMap = new HashMap<String, Object>();
            fileMap.put("DEV_ID", devID);
            fileMap.put("FILE_PATH", fileRecord.getFilePath());
            fileMap.put("FILE_LENGTH", fileRecord.getFileLength());
            fileMap.put("FILE_NAME", fileRecord.getFileName());
            fileMap.put("FILE_TIME", fileRecord.time(null));
            fileMap.put("SESSION_ID", sessionId);
            tempList.add(fileMap);
        }

        // 参数
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("SESSION_ID", sessionId);
        params.put("COLL_COMPARISON_TIME", coll_comparison_time);

        if (tempList.size() == 0) {// 文件列表为空
            return new ArrayList<FileRecord>();
        } else if (tempList.size() < db_commit_count) {// 批量添加数据，当数据量比较大的时候批量添加会失败，这里分批添加
            sqlSession.insert("collectMapper.addTempTableData", tempList);
        } else {
            while (true) {// 一直循环处理，当数组里的数据全部取完，则才会退出
                List<Map<String, Object>> onceAddList = new ArrayList<Map<String, Object>>();
                boolean isCommit = false;
                if (tempList.size() < 1) {
                    break;
                }
                for (int i = 0; (i < db_commit_count && params.size() > 0); i++) {
                    if (tempList.size() == 0) {// 遍历完成
                        break;
                    }

                    onceAddList.add(tempList.get(0));// 每次取第一个
                    tempList.remove(0);// 插入之后立刻删掉，必免重复
                    isCommit = true;// 如果有数据，则需要插入数据库的标识
                }
                if (isCommit) {
                    sqlSession.insert("collectMapper.addTempTableData", onceAddList);
                }
            }
        }
        logger.debug("get effect file list by SQL, add temporary table ok, tempList size: "
                + ArrayUtil.getSize(tempList) + ", devId: " + devID);

        // 已经采集过的文件进行排重处理,通过临时表和采集日志文件对比获取文件列表(临时表,不存在于日志表中的记录 )
        List<Map<String, Object>> tempRemoteLists = new ArrayList<Map<String, Object>>();
        // 默认使用文件path+name进行采集文件排重处理
        if (BlankUtil.isBlank(checkCondition)
                || ParamsConstant.LINK_NOT_COLL_KEY_PATH_NAME.equalsIgnoreCase(checkCondition)) {
            tempRemoteLists = JdbcUtil.queryForList("collectMapper.queryListByPathName", params, FrameConfigKey.DEFAULT_DATASOURCE);
        }
        // 文件path+name+time进行文件排重处理
        else if (ParamsConstant.LINK_NOT_COLL_KEY_PATH_NAME_TIME.equalsIgnoreCase(checkCondition)) {
            tempRemoteLists = JdbcUtil.queryForList("collectMapper.queryListByPathNameTime", params, FrameConfigKey.DEFAULT_DATASOURCE);
        }
        // 文件name进行文件排重处理
        else if (ParamsConstant.LINK_NOT_COLL_KEY_NAME.equalsIgnoreCase(checkCondition)) {
            tempRemoteLists = JdbcUtil.queryForList("collectMapper.queryListByName", params, FrameConfigKey.DEFAULT_DATASOURCE);
        }
        logger.debug("get effect file list by SQL, after filter file list size: " + ArrayUtil.getSize(tempRemoteLists)
                + ", devId: " + devID);


        List<FileRecord> fileList = new ArrayList<FileRecord>();
        for (Map<String, Object> map : tempRemoteLists) {
            FileRecord fileRecord = new FileRecord();
            fileRecord.setFileType(FileRecord.FILE);
            fileRecord.setFileName(StringTool.object2String(map.get("ORI_FILE_NAME")));
            fileRecord.setFilePath(StringTool.object2String(map.get("ORI_PATH")));
            fileRecord.setFileLength(Long.parseLong(StringTool.object2String(map.get("ORI_FILE_LENGTH"))));
            fileRecord.setTime(DateUtil.parse(StringTool.object2String(map.get("ORI_FILE_TIME")), DateUtil.allPattern));
            fileList.add(fileRecord);
        }
        logger.debug("end get effect file list by SQL, final file list size: " + ArrayUtil.getSize(fileList)
                + ", devId: " + devID);
        return fileList;
    }

    /**
     * dca剃重
     *
     * @param devID
     * @param list
     * @param checkCondition
     * @return
     */
    public static List<FileRecord> getEffectFileListByRedis(String devID, Vector<FileRecord> list,
                                                            String checkCondition) {
        logger.debug("begin get effect file list by Redis, file list size: " + ArrayUtil.getSize(list)
                + ", checkCondition: " + checkCondition + ", devId: " + devID);
        // 链路文件信息
        List<FileRecord> fileList = new ArrayList<FileRecord>();

        int searchSize = list.size();
        long start = System.currentTimeMillis();
        try {
            //域
            String keyField = SystemProperty.getContextProperty(ParamsConstant.DCA_KEY_FIELD);
            //是否将所有的Key转化为Hash进行处理
            boolean filterHash = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_FILE_FILTER_HASH, Boolean.FALSE);
            logger.info("store dca key，key filter with hashcode: " + filterHash);

            String finalDevId = devID + "_" + Thread.currentThread().getId();
            for (int i = 0; i < searchSize; ++i) {
                FileRecord fileRecord = list.get(i);

                String key = null;

                // 默认使用文件path+name进行采集文件排重处理
                if (BlankUtil.isBlank(checkCondition)
                        || ParamsConstant.LINK_NOT_COLL_KEY_PATH_NAME.equalsIgnoreCase(checkCondition)) {
                    key = keyField + devID + "#" + fileRecord.getFileName() + "#" + fileRecord.getFilePath();
                    logger.debug("path+name string key: " + key + ", devId: " + devID);
                    if (filterHash) {
                        key = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName() + "#" + fileRecord.getFilePath());
                        logger.debug("path+name SHA256 key: " + key + ", devId: " + devID);
                    }
                }
                // 文件path+name+time进行文件排重处理
                else if (ParamsConstant.LINK_NOT_COLL_KEY_PATH_NAME_TIME.equalsIgnoreCase(checkCondition)) {
                    String dateStr = DateUtil.format(fileRecord.getTime(), DateUtil.fullPattern);
                    key = keyField + devID + "#" + fileRecord.getFileName() + "#" + fileRecord.getFilePath() + "#" + dateStr;
                    logger.debug("path+name+time string key: " + key + ", devId: " + devID);
                    if (filterHash) {
                        key = keyField + devID + "#" + SHATool.getSHA256StrJava((fileRecord.getFileName() + "#" + fileRecord.getFilePath() + "#" + dateStr));
                        logger.debug("path+name+time SHA256 key: " + key + ", devId: " + devID);
                    }
                }
                // 文件name进行文件排重处理
                else if (ParamsConstant.LINK_NOT_COLL_KEY_NAME.equalsIgnoreCase(checkCondition)) {
                    key = keyField + devID + "#" + fileRecord.getFileName();
                    logger.debug("name string key: " + key + ", devId: " + devID);
                    if (filterHash) {
                        key = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName());
                        logger.debug("name SHA256 key: " + key + ", devId: " + devID);
                    }
                }

                //判断是否在dca中，不存在可以进行采集：1已采集，0未采集，其余异常
                boolean exists = DcaUtil.exists(finalDevId, key);
                if (!exists) {
                    fileList.add(list.get(i));
                }
            }
        } catch (Exception e) {
            logger.error("dca comparision fail, devId: " + devID, e);
            throw e;
        }

        long end = System.currentTimeMillis();
        long interval = end - start;
        long avg = 0;
        long rate = 0;
        if (interval > 0) {
            avg = list.size() / interval;
        } else {
            logger.debug("time interval is 0,can not cal time used and avg rate");
        }
        if (avg > 0) {
            rate = Math.round(1000.0 / avg);
        }
        logger.debug("filter file size:" + (list.size() - fileList.size()) + ",unCollect file size:" + fileList.size() + ",total used(ms):" + interval + ",avg(record/s):" + rate);
        logger.debug("end get effect file list by Redis, devId: " + devID);
        return fileList;
    }

    /**
     * 往dca添加记录
     * @param devID
     * @param fileRecord
     */
    public static void addDistTaskToRedis(String devID, FileRecord fileRecord) {
        logger.debug("begin add dca data, fileRecord : " + fileRecord + ", devId: " + devID);
        String dateStr = DateUtil.format(fileRecord.getTime(), DateUtil.fullPattern);
        //域
        String keyField = SystemProperty.getContextProperty(ParamsConstant.DCA_KEY_FIELD);

        String keyFileName = keyField + devID + "#" + fileRecord.getFileName();
        String keyFilePath = keyField + devID + "#" + fileRecord.getFileName() + "#" + fileRecord.getFilePath();
        String keyFileTime = keyField + devID + "#" + fileRecord.getFileName() + "#" + fileRecord.getFilePath() + "#" + dateStr;
        logger.debug("add dca data, key: " + keyFileTime + ", devId: " + devID);

        //是否将所有的Key转化为Hash进行处理
        boolean filterHash = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_FILE_FILTER_HASH, Boolean.FALSE);
        logger.info("store dca key，key filter with hashcode: " + filterHash);
        if (filterHash) {
            keyFileName = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName());
            keyFilePath = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName() + "#" + fileRecord.getFilePath());
            keyFileTime = keyField + devID + "#" + SHATool.getSHA256StrJava(fileRecord.getFileName() + "#" + fileRecord.getFilePath() + "#" + dateStr);
            logger.debug("SHA256 name key: " + keyFileName);
            logger.debug("SHA256 path key: " + keyFilePath);
            logger.debug("SHA256 time key: " + keyFileTime);
        }

        String finalDevId = devID + "_" + Thread.currentThread().getId();
        try {
            boolean added = DcaUtil.setex(finalDevId, keyFileName, redis_expire_time, "1");
            if (!added) {
                throw new RuntimeException("往dca添加key失败,key:" + keyFileName);
            }

            added = DcaUtil.setex(finalDevId, keyFilePath, redis_expire_time, "1");
            if (!added) {
                throw new RuntimeException("往dca添加key失败,key:" + keyFilePath);
            }

            added = DcaUtil.setex(finalDevId, keyFileTime, redis_expire_time, "1");
            if (!added) {
                throw new RuntimeException("往dca添加key失败,key:" + keyFileTime);
            }
        } catch (Exception e) {
            logger.error("dca add key fail, key: " + keyFileTime + ", devId: " + devID, e);
            DcaUtil.delKey(finalDevId, keyFileName, keyFilePath, keyFileTime);
            throw new RuntimeException("dca broken");
        }
        logger.debug("end add dca data, devId: " + devID);
    }

}
