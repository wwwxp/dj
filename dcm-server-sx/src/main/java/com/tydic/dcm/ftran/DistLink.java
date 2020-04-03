package com.tydic.dcm.ftran;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.constant.Constant;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.core.utils.db.DbContextHolder;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.dcfile.service.DCFileService;
import com.tydic.dcm.DcmSystem;
import com.tydic.dcm.device.LinkDataRefreshThrd;
import com.tydic.dcm.dto.DistLinkDto;
import com.tydic.dcm.enums.DistMsgTypeEnum;
import com.tydic.dcm.enums.DistTaskTypeEnum;
import com.tydic.dcm.enums.FileStoreTypeEnum;
import com.tydic.dcm.enums.LinkParameterModuleEnum;
import com.tydic.dcm.ftran.impl.FtpTran;
import com.tydic.dcm.ftran.impl.SftpTran;
import com.tydic.dcm.service.DFSService;
import com.tydic.dcm.service.impl.FastDFSService;
import com.tydic.dcm.task.DistMultTask;
import com.tydic.dcm.util.condition.Condition;
import com.tydic.dcm.util.condition.MsgFormat;
import com.tydic.dcm.util.condition.MsgItem;
import com.tydic.dcm.util.exception.DcmException;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.spring.SpringUtil;
import com.tydic.dcm.util.tools.*;
import com.tydic.dcm.warn.WarnManager;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: DistLink
 * @Description: 链路分发类
 * @Prject: dcm-server_base
 * @author: yuanhao
 * @date 2016-05-25
 */
public class DistLink implements Cloneable {

    /**
     * 获取日志ID
     */
    private static Logger logger = Logger.getLogger(DistLink.class);

    /**
     * 本地网
     */
    private static final String FMT_DIST = "999";

    /**
     * 文件传输方式(ftp/sftp)
     */
    private Trans trans;

    /**
     * 本地文件操作对象
     */
    public Local local;

    /**
     * 文件协议类型
     */
    private String protocolType;

    /**
     * 分发链路对应的本地网
     */
    private String latn_Id_Sub;

    /**
     * 分发链路属性本地网
     */
    private String latnId;

    /**
     * 分发链路ID
     */
    public String id;

    /**
     * 目标目录(目标文件路径，对于分发就是远程主机目录)
     */
    private String dstPath;

    /**
     * 目标目录2(目标文件路径2，对于分发就是远程主机目录2)
     */
    private String dstPathSd;

    /**
     * 本地备份目录(源文件备份目录,对于分发源文件目录就是当前采集程序所在主机目录)
     */
    private String localPathBak;

    /**
     * 本地文件路径(源文件路径)
     */
    private String localPath;

    /**
     * 分发文件记录数(刷新一次分发链路当前链路一次分发文件数量)
     */
    private int distRecordRows = ParamsConstant.DEFAULT_DIST_RECORD_ROWS;

    /**
     * 过滤条件对象
     */
    protected Filter filter;

    /**
     * 分发链路DTO对象
     */
    private DistLinkDto distLinkDto = null;

    /**
     * 分发开始时间
     */
    private String START_DEAL_TIME = DateUtil.format(new Date(), DateUtil.fullPattern);

    /**
     * 分发链路对象构造函数
     */
    public DistLink() {

    }

    /**
     * 分发链路构造函数
     *
     * @param devId
     */
    public DistLink(String devId) {
        //创建本地文件操作对象
        local = new Local();

        //获取分发链路信息
        this.distLinkDto = LinkDataRefreshThrd.getDistLinkAllInfo(devId);
        if (BlankUtil.isBlank(this.distLinkDto)) {
            distLinkDto = new DistLinkDto();
            getDistLink(devId);
        }
        //设置当前链路对象ID
        this.id = StringTool.object2String(distLinkDto.getDevId());
        this.latnId = distLinkDto.getLatnId();
    }

    /**
     * 根据分发链路ID获取分发链路信息
     *
     * @param devId
     * @return
     */
    private void getDistLink(String devId) {
        logger.debug("begin get distribute link info, devId: " + devId);
        try {
            // 查询链路信息
            Map<String, Object> queryParams = new HashMap<String, Object>();
            queryParams.put("devId", devId);
            Map<String, Object> distLinkMap = JdbcUtil.queryForObject("distributeMapper.queryDistLinkInfoById", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
            if (BlankUtil.isBlank(distLinkMap) || distLinkMap.isEmpty()) {
                logger.debug("get distribute link info is null, devId: " + devId);
                return;
            }

            //将查询链路属性转化为链路Dto对象
            this.id = devId;
//			this.latnId = StringTool.object2String(distLinkMap.get("LATN_ID"));
            distLinkDto = ConvertMap2Dto.convert2DistDto(distLinkMap);
            logger.info("get distribute link attributes ok, devId: " + id);

            int tskType = distLinkDto.getTskType();
            if (StringUtils.equalsIgnoreCase(distLinkDto.getFileStoreType(), FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {
                queryParams.put("MODULE", LinkParameterModuleEnum.DIST_FTP.getModule());
            } else if (DistTaskTypeEnum.DIST_MSG.getTskType().equals(tskType)) {
                queryParams.put("MODULE", LinkParameterModuleEnum.DIST_MSG.getModule());
            }

            //查询链路参数信息
            StringBuffer buffer = new StringBuffer();
            List<Map<String, Object>> distLinkParamsList = JdbcUtil.queryForList("collectMapper.queryCollLinkParamsList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
            if (!BlankUtil.isBlank(distLinkParamsList)) {
                Hashtable<String, Object> linkParams = new Hashtable<String, Object>();
                for (int i = 0; i < distLinkParamsList.size(); i++) {
                    String paramName = StringTool.object2String(distLinkParamsList.get(i).get("PARAM_NAME"));
                    String paramValue = StringTool.object2String(distLinkParamsList.get(i).get("PARAM_VALUE"));

                    //链路参数是否必填
                    String paramIsRequired = StringTool.object2String(distLinkParamsList.get(i).get("IS_REQUIRED"));
                        if (ParamsConstant.PARAMS_1.equals(paramIsRequired) && BlankUtil.isBlank(paramValue)) {
                            buffer.append(paramName);
                            buffer.append(",");
                    }

                    linkParams.put(paramName, paramValue);
                }
                distLinkDto.setLinkParams(linkParams);
            }

            //latn_ID
            this.latnId = ObjectUtils.toString(distLinkDto.getLatnId(), "-99");

            //有必填参数为空或者改链路没有配置链路参数
            if (BlankUtil.isBlank(distLinkParamsList) || !BlankUtil.isBlank(buffer.toString())) {
                String tipsMsg = "link parameter is null or missing required, "
                        + (!BlankUtil.isBlank(buffer) ? "parameter name: " + buffer.toString().substring(0, buffer.toString().length() - 1) : "");
                distLinkDto.setIsMissParams(Boolean.TRUE);
                distLinkDto.setTipsMsg(tipsMsg);
            }
            logger.info("get distribute link parameters ok, devId: " + id);
        } catch (Exception e) {
            logger.error("get distribute link attributes and parameters fail, devId: " + id, e);
            e.printStackTrace();
        }
        logger.debug("end get distribute link info, devId: " + devId);
    }

    /**
     * 分发链路参数管理
     *
     * @throws Exception
     */
    private void init() throws Exception {
        logger.debug("begin distribute link init, devId: " + id);

        String tranMode = "";
        try {
            //获取Ftp文件传输模式
            tranMode = StringTool.object2String(distLinkDto.getLinkParams().get("tran_mode"));
            //传输模式为PASV则设置为被动模式
            if (!BlankUtil.isBlank(tranMode) && ParamsConstant.LINK_FTP_TRAN_MODE_PASV.equalsIgnoreCase(tranMode.trim())) {
                tranMode = ParamsConstant.LINK_FTP_TRAN_MODE_PASV;
            } else {
                tranMode = ParamsConstant.LINK_FTP_TRAN_MODE_PORT;
            }

            //文件传输协议(FTP/SFTP)
            protocolType = StringTool.object2String(distLinkDto.getLinkParams().get("trans_protocol"));
            protocolType = BlankUtil.isBlank(protocolType) ? ParamsConstant.DEFAULT_PROTOCOL_FTP : protocolType;
        } catch (Exception e1) {

            //默认Ftp协议传输文件
            protocolType = ParamsConstant.DEFAULT_PROTOCOL_FTP;

            //默认Ftp采集模式为被动模式
            tranMode = ParamsConstant.LINK_FTP_TRAN_MODE_PORT;
        }
        logger.info("distribute link init, protocolType: " + protocolType + ", tranMode: " + tranMode + ", devid: " + id);

        //Ftp/SFTP对象参数
        String ip = StringTool.object2String(distLinkDto.getLinkParams().get("ip"));
        String userName = StringTool.object2String(distLinkDto.getLinkParams().get("username"));
        String password = StringTool.object2String(distLinkDto.getLinkParams().get("password"));
        String port = StringTool.object2String(distLinkDto.getLinkParams().get("port"));
        String timeout = StringTool.object2String(distLinkDto.getLinkParams().get("time_out"));
        if (BlankUtil.isBlank(timeout)) {
            timeout = ParamsConstant.FTP_CONN_TIMEOUT;
        }

        //FTP密码是否加密
        String ftp_password_encrypt = SystemProperty.getContextProperty("ftp_password_encrypt");
        if (StringUtils.isNotBlank(password) && StringUtils.equalsIgnoreCase(ftp_password_encrypt, "true")) {
            password = Encoder.decode(password);
        }

        //获取当前链路文件传输模式
        Boolean isPasvMode = ParamsConstant.LINK_FTP_TRAN_MODE_PASV.equalsIgnoreCase(tranMode) ? Boolean.TRUE : Boolean.FALSE;
        if (ParamsConstant.DEFAULT_PROTOCOL_FTP.equalsIgnoreCase(protocolType)) {
            trans = new FtpTran(ip, Integer.parseInt(port), userName, password, Integer.parseInt(timeout), this.id);
            trans.setPasvMode(isPasvMode);
        } else {
            trans = new SftpTran(ip, Integer.parseInt(port), userName, password, Integer.parseInt(timeout), this.id);
        }
        logger.info("distribute link init, ftp/sftp ip: " + ip + ", port: " + port + ", isPasvMode: " + isPasvMode + ", devId: " + id);

        //创建链路文件过滤条件
        filter = new Filter(distLinkDto.getLinkParams());
        logger.info("distribute link init, filter object create ok, devId: " + id);

        //获取分发文件数量,一次单条链路最多分发1000条数据
        try {
            distRecordRows = Integer.parseInt(StringTool.object2String(distLinkDto.getLinkParams().get("dist_record_rows")));
            if (distRecordRows > ParamsConstant.MAX_DIST_RECORD_ROWS) {
                distRecordRows = ParamsConstant.MAX_DIST_RECORD_ROWS;
            }
        } catch (Exception e) {
            distRecordRows = ParamsConstant.DEFAULT_DIST_RECORD_ROWS;
        }
        //当前月
        String curMonth = DateUtil.getCurrent(DateUtil.dateMonthPattern);
        //当前旬
        String tenDays = TimeTool.getTenDays();
        //当前日
        String curDay = DateUtil.getCurrent(DateUtil.datePattern);

        //生成分发目标目录
        mkDstPath(latnId, curMonth, tenDays, curDay);
        logger.info("distribute link init, create dstPath ok, devId: " + id);

        //生成分发目标目录2
        dstPathSd = StringTool.object2String(distLinkDto.getLinkParams().get("remote_path2"));
        if (!BlankUtil.isBlank(dstPathSd)) {
            mkDstPathSd(latnId, curMonth, tenDays, curDay);
            logger.info("distribute link init, create dstPathSd ok, devId: " + id);
        }

        //创建本地目录
        mkLocalPath(latnId, curMonth, tenDays, curDay);

        //创建本地备份目录
        localPathBak = StringTool.object2String(distLinkDto.getLinkParams().get("local_path_bak"));
        if (!BlankUtil.isBlank(localPathBak)) {
            mkLocalPathBak(latnId, curMonth, tenDays, curDay);
            logger.info("distribute link init, create localPathBak ok, devId: " + id);
        }
        logger.debug("end distribute link init, devId: " + id);
    }

    /**
     * 自动分发
     * 1、分发链路本地网为999时，需要将本地文件分发到多个不同主机
     * 2、分发链路本地网不为999时，直接分发到链路对应的主机
     *
     * @param
     * @throws DcmException, Exception
     */
    public void autoTransfer() throws Exception {
        logger.debug("******begin auto distribute, devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, autoTransfer executed fail, devId: " + id);
            return;
        }

        //分发链路本地网
        String latnId = distLinkDto.getLatnId();
        logger.info("auto distribute, latnId: " + latnId + ", devId: " + id);

        if (FMT_DIST.equals(latnId)) {
            //分发文件
            try {
                subAutoTransfer();
            } catch (DcmException e) {
                logger.error("auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
                //添加告警信息
                WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), e.getWarnCode(), id, e.getFileName(), e.getErrorMsg());
                //修改链路运行状态
                if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                    updateDistLinkLevel(id, e.getTipsLevel(), ParamsConstant.DIST_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
                } else {
                    updateDistLinkLevel(id, e.getTipsLevel(), null, e.getErrorCode() + ":" + e.getErrorMsg());
                }
                //throw e;
            } catch (Exception e) {
                logger.error("auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
                //添加链路告警信息
                WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), WarnManager.TRAN_WARN_DIST_FAIL, id, "", "autoTransfer fail, failure cause: " + e.getMessage());
                //修改链路告警级别
                updateDistLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, DcmException.OTHER_ERR + ":" + e.getMessage());
            }
        } else {
            latn_Id_Sub = latnId;
            //自动分发
            try {
                subAutoTransfer();
            } catch (DcmException e) {
                logger.error("auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
                //添加告警信息
                WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), e.getWarnCode(), id, e.getFileName(), e.getErrorMsg());
                //修改链路运行状态
                if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                    updateDistLinkLevel(id, e.getTipsLevel(), ParamsConstant.DIST_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
                } else {
                    updateDistLinkLevel(id, e.getTipsLevel(), null, e.getErrorCode() + ":" + e.getErrorMsg());
                }
            } catch (Exception e) {
                logger.error("auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
                //添加链路告警信息
                WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), WarnManager.TRAN_WARN_DIST_FAIL, id, "", "autoTransfer fail, failure cause: " + e.getMessage());
                //修改链路告警级别
                updateDistLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, DcmException.OTHER_ERR + ":" + e.getMessage());
            }
        }
        logger.debug("end auto distribute, devId: " + id);
    }

    /**
     * 自动分发
     * 1、对分发链路参数进行初始化设置
     * 1.1、如果初始化失败直接获取分发文件列表，将分发任务表数据移除到分发任务异常表，将分发链路状态修改为异常，抛出异常，返回
     * 1.2、初始化成功获取分发文件列表,对分发列表进行排序，遍历分发文件
     *
     * @throws Exception
     */
    private void subAutoTransfer() throws DcmException, Exception {
        long startTimes = System.currentTimeMillis();
        logger.debug("begin Sub auto distribute, startTimes: [" + DateUtil.getCurrent(DateUtil.fullPattern) + "], devId: " + id);
        Vector<TransItem> list = null;
        try {
            //分发任务类型: 1写消息队列 2普通分发
            int taskType = this.distLinkDto.getTskType();
            logger.debug("dev_id:" + id + ",taskType:" + taskType);
            if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(taskType)) {
                init();
            } else {//MQ分发需要构造一个空的filter
                this.filter = new Filter(new Hashtable<String, Object>());

                //设置一次分发文件个数
                try {
                    distRecordRows = Integer.parseInt(StringTool.object2String(distLinkDto.getLinkParams().get("dist_record_rows")));
                    if (distRecordRows > ParamsConstant.MAX_DIST_RECORD_ROWS) {
                        distRecordRows = ParamsConstant.MAX_DIST_RECORD_ROWS;
                    }
                } catch (Exception e) {
                    distRecordRows = ParamsConstant.DEFAULT_DIST_RECORD_ROWS;
                }
            }
        } catch (Exception e) {
            logger.error("auto distribute transfer init fail, devId: " + id, e);
            DcmException dcmE = new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.INIT_LINK_ERR,
                    "link initialize fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            try {
                list = getDistRecdList();
                logger.info("Sub auto distribute, get distribute file list of exception, file list size: "
                        + ArrayUtil.getSize(list) + ", devId: " + id);
            } catch (Exception e1) {
                logger.error("Sub auto distribute, get distribute file list fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.GET_FILE_LIST_ERR, "get distribute task records exception, file cause: " + e1.getMessage());
            }
            if (!BlankUtil.isBlank(list)) {
                //将分发任务表数据添加到分发异常任务表并且将分发任务表数据删除，后续需要通过前台界面“分发异常回收”才能在将对应文件分发
                for (int i = 0; i < list.size(); i++) {
                    TransItem transItem = list.get(i);
                    addExceptionTb(transItem, dcmE);
                }
            }
            throw dcmE;
        }
        logger.info("Sub auto distribute init ok, devId: " + id);

        //获取分发文件任务表
        try {
            list = getDistRecdList();
            logger.info("Sub auto distribute, get distribute file list of normal, file list size: "
                    + ArrayUtil.getSize(list) + ", devId: " + id);
        } catch (Exception e) {
            logger.error("Sub auto distribute, get distribute file list fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.GET_FILE_LIST_ERR,
                    "get dist task records exception, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }


        //条件分发告警时间,如果采集文件数量不为空，需要更新当前分发链路最新分发时间
        //贵州采集不需要分发告警
		/*if (list != null && list.size() > 0) {
			synchronized (DcmSystem.warnRefreshLinkThrd.getDistWarnHashTab()) {
				if(DcmSystem.warnRefreshLinkThrd.getDistWarnHashTab().keySet().contains(id)) {
					WarnLinkDto warnLinkDto = DcmSystem.warnRefreshLinkThrd.getDistWarnHashTab().get(id);
					warnLinkDto.setLastTime(TimeTool.getTime());
					DcmSystem.warnRefreshLinkThrd.getDistWarnHashTab().put(id, warnLinkDto);
				}
			}
		}*/

        if (!BlankUtil.isBlank(list)) {
            //分发文件进行排序
            Collections.sort(list, new SortObjectBySourceId());
            logger.info("Sub auto distribute sort ok, devId: " + id);
            //分发文件
            try {
                int threadSize = org.apache.commons.lang3.math.NumberUtils.toInt(ObjectUtils.toString(this.getDistLinkDto().getLinkParams().get("dist_thread_size"), Constant.ONE),1);
                if (!CollectionUtils.isEmpty(list) && list.size() > 100 && threadSize > 1) {
                    Vector<Vector<TransItem>> threadList =  this.averageAssign(list, threadSize);
                    CountDownLatch latch = new CountDownLatch(threadList.size());
                    for (int i=0; i<threadList.size(); i++) {
                        //创建任务线程
                        String taskName = "TASK_" + id + "$" + i;
                        DistMultTask distMultTask = new DistMultTask(taskName, id, latch, threadList.get(i));
                        Thread distThd = new Thread(distMultTask);
                        distThd.setPriority(Thread.MAX_PRIORITY);
                        distThd.start();
                    }
                    try {
                        //等待线程计数器为0，才能再次接受新的任务
                        latch.await();
                    } catch (Exception e) {
                        logger.error("mult-task count down latch fail.", e);
                    }
                    logger.info("mult-task batch transfer ok, devId: " + id);
                } else {
                    batchTransfer(list);
                    logger.info("Sub auto distribute batch transfer ok, devId: " + id);
                }
            } catch (DcmException e) {
                logger.error("auto distribute batch transfer fail, devId: " + id, e);
                throw e;
            } catch (Exception e) {
                logger.error("Sub auto distribute batch transfer fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.BATCH_DIST_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            } finally {
                if (trans != null) {
                    trans.close();
                }
            }
        }
        long endTimes = System.currentTimeMillis();
        logger.debug("end mult-task auto distribute, endTimes: [" + DateUtil.getCurrent(DateUtil.fullPattern) + "], total times: ["
                + (endTimes - startTimes) + "]ms, total file: [" + ArrayUtil.getSize(list) + "], devId: " + id);
    }

    /**
     * 多任务分发
     * @param taskName
     * @param list
     */
    public String subAutoMultTransfer(String taskName, Vector<TransItem> list) {
        try {
            long startTimes = System.currentTimeMillis();
            logger.info("start mult-task dist,taskName: " + taskName + ", dist files: "
                    + (ArrayUtil.getSize(list)) + ",startTimes: [" + startTimes + "], devId: " + id);
            try {
                //分发任务类型: 1写消息队列 2普通分发
                int taskType = this.distLinkDto.getTskType();
                logger.debug("dev_id:" + id + ",taskType:" + taskType);
                if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(taskType)) {
                    init();
                } else {//MQ分发需要构造一个空的filter
                    this.filter = new Filter(new Hashtable<String, Object>());
                }
            } catch (Exception e) {
                logger.error("auto distribute transfer init fail, devId: " + id, e);
                DcmException dcmE = new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.INIT_LINK_ERR,
                        "link initialize fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
                if (!BlankUtil.isBlank(list)) {
                    //将分发任务表数据添加到分发异常任务表并且将分发任务表数据删除，后续需要通过前台界面“分发异常回收”才能在将对应文件分发
                    for (int i = 0; i < list.size(); i++) {
                        TransItem transItem = list.get(i);
                        addExceptionTb(transItem, dcmE);
                    }
                }
                throw dcmE;
            }
            logger.info("mult-task distribute init ok, devId: " + id);

            batchTransfer(list);
            long endTimes = System.currentTimeMillis();
            logger.info("end mult-task dist,taskName: " + taskName + ", dist files: "
                    + (ArrayUtil.getSize(list)) +  ", endTimes: [" + endTimes + "], total times: ["
                    + (endTimes - startTimes) + "]ms, devId: " + id);
        } catch (DcmException e) {
            logger.error("mult-task auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
            //添加告警信息
            WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), e.getWarnCode(), id, e.getFileName(), e.getErrorMsg());
            //修改链路运行状态
            if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                updateDistLinkLevel(id, e.getTipsLevel(), ParamsConstant.DIST_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
            } else {
                updateDistLinkLevel(id, e.getTipsLevel(), null, e.getErrorCode() + ":" + e.getErrorMsg());
            }
        } catch (Exception e) {
            logger.error("mult-task auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
            //添加链路告警信息
            WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), WarnManager.TRAN_WARN_DIST_FAIL, id, "", "autoTransfer fail, failure cause: " + e.getMessage());
            //修改链路告警级别
            updateDistLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, DcmException.OTHER_ERR + ":" + e.getMessage());
        }
        return Constant.ONE;
    }

    /**
     * 将一组数据平均分成n组
     *
     * @param source 要分组的数据源
     * @param n      平均分成n组
     * @param <T>
     * @return
     */
    public static <T> Vector<Vector<T>> averageAssign(Vector<T> source, int n) {
        Vector<Vector<T>> result = new Vector<Vector<T>>();
        int remainder = source.size() % n;  //(先计算出余数)
        int number = source.size() / n;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> valueList = null;
            Vector<T> vectorList = null;
            if (remainder > 0) {
                valueList = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                valueList = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            vectorList = new Vector<T>(valueList.size());
            vectorList.addAll(valueList);
            result.add(vectorList);
        }
        return result;
    }

    /**
     * 修改链路运行状态
     *
     * @param devId     链路ID
     * @param tipsLevel 链路提示级别
     * @param runState  链路运行状态
     */
    private void updateDistLinkLevel(String devId, String tipsLevel, String runState, String linkError) {
        logger.debug("begin update distribute link tips level, tipsLevel: " + tipsLevel + ", runState: " + runState + ", linkError: " + linkError + ", devId: " + id);
        Map<String, Object> updateParams = new HashMap<String, Object>();
        updateParams.put("DEV_ID", devId);
        updateParams.put("TIPS_LEVEL", BlankUtil.isBlank(StringTool.object2String(tipsLevel)) ? ParamsConstant.PARAMS_0 : StringTool.object2String(tipsLevel));
        updateParams.put("RUN_STATE", StringTool.object2String(runState));
        updateParams.put("LINK_ERR", StringTool.object2String(linkError));
        JdbcUtil.updateObject("distributeMapper.updateDistributeLinkTipsLevel", updateParams, FrameConfigKey.DEFAULT_DATASOURCE);
        logger.debug("end update distribute link tips level, devId: " + id);
    }

    /**
     * 分发文件
     * 1、登录ftp/sftp主机, 如果登录失败直接将分发任务表数据移除到分发任务异常表并且修改分发链路运行状态, 抛出异常，返回
     * 2、遍历分发文件列表，如果分发某个文件出现异常情况，记录告警日志，后续文件继续分发
     *
     * @param list 需要分发的文件列表
     * @return
     * @throws Exception
     */
    private void batchTransfer(Vector<TransItem> list) throws DcmException {
        logger.debug("begin batch distribute file, file list size: " + ArrayUtil.getSize(list) + ", devId: " + id);
        try {
            //分发任务类型: 1写消息队列 2普通分发
            int taskType = this.distLinkDto.getTskType();
            //文件分发方式：local本地分发、remote远端分发
            String fileStoreType = this.distLinkDto.getFileStoreType();
            logger.info("tskType: " + taskType + ", fileStoreType: " + fileStoreType + ", devId: " + id);
            try {
                if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(taskType)) {
                    trans.login();
                }
            } catch (Exception e) {
                logger.error("batch distribute, login ftp/sftp host fail, devId: " + id, e);
                //修改分发任务
                DcmException dcmE = new DcmException(WarnManager.CONN_FTP_ERROR, DcmException.FTP_CONNECT_ERR, "connect ftp exception.", ParamsConstant.LINK_TIPS_LEVEL_1);
                if (!BlankUtil.isBlank(list)) {
                    for (int i = 0; i < list.size(); i++) {
                        TransItem transItem = list.get(i);
                        addExceptionTb(transItem, dcmE);
                    }
                }
                throw dcmE;
            }
            logger.info("batch distribute, login ftp/sftp host ok, devId: " + id);


            //文件批量分发过程中是否存在异常
            Boolean isExistException = Boolean.FALSE;
            //文件批量分发过程中异常描述信息
            DcmException dcmE = null;
            //文件批量分发传输
            for (int i = 0; i < list.size(); i++) {
                TransItem transItem = list.get(i);

                //更新分发开始时间
                START_DEAL_TIME = DateUtil.format(new Date(), DateUtil.fullPattern);

                try {
                    //本地普通分发
                    if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(taskType) && StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {//ftp
                        transfer(transItem);
                    } else if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(taskType) && StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_DFS.getFileStoreType())) {
                        putFileFromDFS(transItem);//从分布式文件获取文件推送到远端ftp

                        //将远程的文件复制到dstPathSd目录下
                        if (StringUtils.isNotBlank(dstPathSd)) {
                            putFileFromDFS2(transItem);
                        }

                        recordDistLog(transItem, true);//传输成功要删除分发任务
                    } else if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(taskType) && StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_FAST_DFS.getFileStoreType())) {
                        putFileFromFastDFS(transItem);//从分布式文件获取文件推送到远端ftp

                        //将远程的文件复制到dstPathSd目录下
                        if (StringUtils.isNotBlank(dstPathSd)) {
                            putFileFromFastDFS2(transItem);
                        }

                        recordDistLog(transItem, true);//传输成功要删除分发任务
                    }  else if (DistTaskTypeEnum.DIST_MSG.getTskType().equals(taskType)) {
                        String messageType = SystemProperty.getContextProperty(ParamsConstant.DIST_MESSAGE_TYPE);
                        logger.info("分发消息类型:" + messageType + ",devId:" + id);

                        if (StringUtils.equalsIgnoreCase(messageType, DistMsgTypeEnum.DIST_MSG_TABLE.getType())) {//写表
                            //写表
                            String sql_jiekou = recordDistLogWithSqlJiekou(transItem);
                        }
                    }
                } catch (DcmException e) {
                    logger.error("batch distribute, transfer file fail, devId:" + id);
                    //添加告警日志
                    WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), e.getWarnCode(), id,
                            e.getFileName(), e.getErrorCode() + ":" + e.getErrorMsg());
                    logger.debug("batch distribute, add warn log ok, devId: " + id);

                    //删除分发任务表之前要判断分发任务表记录是否存在
                    int counter = counterDistTask(transItem);
                    if (counter > 0) {
                        addExceptionTb(transItem, e);
                    }
                    isExistException = Boolean.TRUE;
                    dcmE = e;
                }
            }

            //批量分发文件过程中如果出现文件分发失败异常，将分发链路状态修改为2(异常状态)并且记录异常原因
            if (isExistException) {
                throw dcmE;
            }
            logger.info("batch distribute transfer file list ok, devId: " + id);
        } catch (DcmException e) {
            logger.error("batch distribute transfer file list fail, devId: " + id);
            throw e;
        } catch (Exception e) {
            logger.error("batch distribute transfer file list fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.BATCH_DIST_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        } finally {
            if (trans != null) {
                trans.close();
            }
        }
        logger.debug("end batch distribute file, devId: " + id);
    }

    /**
     * 判断需要删除的分发任务表记录是否大于0
     *
     * @Title: counterDistTask
     * @return: int
     * @author: tianjc
     * @date: 2017年7月19日 下午4:54:15
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private int counterDistTask(TransItem transItem) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("RECID", transItem.getSourceFile().getRecId());
        Map<String, Object> result = JdbcUtil.queryForObject("distributeMapper.countDcDistTask", params, FrameConfigKey.DEFAULT_DATASOURCE);
        int counter = NumberUtils.toInt(ObjectUtils.toString(result.get("COUNTER")));
        return counter;
    }

    /**
     * 文件分发
     * 1、将本地文件上传到远程主机
     * 1.1、目标文件处理方式先设置一个临时文件名，进行文件上传  文件上传完成后进行重命令处理,重命令失败将远程主机上传的文件删除
     * 1.2、添加分发日志记录、分发接口数据,文件重命令,上述操作进行事务管理，如果失败则将之前上传的文件删除
     * 1.3、远程备份目录文件上传
     * 2、本地文件处理
     * 2.1、判断本地文件是否需要备份，如果需要备份将本地文件拷贝到备份目录
     * 2.2、判断本地文件是否需要删除、重命令，如果需要删除或者重命令则进行本地文件操作
     *
     * @param transItem 单个文件信息
     * @throws DcmException
     * @throws IOException
     */
    private void transfer(TransItem transItem) throws DcmException, Exception {
        logger.debug("begin transfer file, devId: " + id);

        try {
            put(transItem);
            logger.info("transfer file ok, devId: " + id);
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("put file fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.PUT_FILE_ERR,
                    "put file fail, failure cause: " + e.getMessage(), transItem.getSourceFile().getFileName(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }

        try {
            //本地文件备份
            String localSourcePath = FileTool.exactPath(transItem.getSourceFile().getFilePath()) + transItem.getSourceFile().getFileName();
            if (!BlankUtil.isBlank(localPathBak)) {
                String localTargetPath = FileTool.exactPath(localPathBak) + transItem.getSourceFile().getFileName();
                FileTool.copyFile(localSourcePath, localTargetPath);
                logger.info("transfer file, local file bak ok, devId: " + id);
            }

            //删除本地文件
            if (transItem.needDelete()) {

                //1、当分发链路需要分发到多台主机时判断该文件是否为最后一个源文件，如果不是最后一个源文件则不能删除
                //2、当一条采集链路对应多条分发链路的时候，只有最后一个分发任务才能删除或者重命名源文件
                if (!multiFtp2Del(transItem)) {
                    logger.debug("end transfer file, multi ftp host to delete, fileName: " + transItem.getSourceFile().getFileName() + ", devId: " + id);
                    return;
                }

                //判断文件是否存在，只有文件存在才会进行删除
                //分发源文件可能不存在，因为是多线程同时操作，源文件可能被A链路给删除了，B链路再去获取源文件会失败
                if (FileTool.exists(localSourcePath)) {
                    logger.debug("delete source file, localSourcePath: " + localSourcePath + ", devId: " + id);
                    local.delete(localSourcePath);
                }
                logger.info("transfer file, local file delete ok, localFileName: " + localSourcePath + ", devId: " + id);
            } else if (transItem.needRename()) {
                if (!multiFtp2Del(transItem)) {
                    logger.debug("end transfer file, multi ftp host to rename, fileName: " + transItem.getSourceFile().getFileName() + ", devId: " + id);
                    return;
                }
                String localTargetFileName = FileTool.exactPath(transItem.getSourceFile().getFilePath()) + transItem.getOriFileRename();
                //判断文件是否存在，只有源文件存在才会进行重命名
                //分发源文件可能不存在，因为是多线程同时操作，源文件可能被A链路给重命名了，B链路再去获取源文件会失败
                if (FileTool.exists(localSourcePath)) {
                    logger.debug("rename source file, localSourcePath: " + localSourcePath + ", localTargetFileName:" + localTargetFileName + ", devId: " + id);
                    local.rename(localSourcePath, localTargetFileName);
                }
                logger.info("transfer file, local file rename ok, sourceName: "
                        + localSourcePath + ", targetName: " + localTargetFileName + ", devId: " + id);
            }
        } catch (Exception e) {
            logger.error("transfer file fail, devId: " + id, e);
            throw new DcmException(WarnManager.COLL_LATE_HANDLE_FAIL, DcmException.LATE_OPERATOR_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }
        logger.debug("end transfer file, devId: " + id);
    }

    /**
     * 当分发链路latn_id=999，判断该文件是否可删除或者重命名
     *
     * @param transItem
     * @return
     */
    private Boolean multiFtp2Del(TransItem transItem) {
        logger.debug("begin to determine whether the file can be deleted or renamed, fileName: " + transItem.getSourceFile().getFileName()
                + ", fileLength: " + transItem.getSourceFile().getFileLength()
                + ", filePath: " + transItem.getSourceFile().getFilePath()
                + ", devId: " + id);
        Boolean isCanDel = Boolean.TRUE;
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
        queryParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
        queryParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
        List<Map<String, Object>> rstList = JdbcUtil.queryForList("distributeMapper.queryDistTaskForCanDel", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
        if (!BlankUtil.isBlank(rstList)) {
            isCanDel = Boolean.FALSE;
        }
        logger.debug("end to determine whether the file can be deleted or renamed, result: " + isCanDel + ", devId: " + id);
        return isCanDel;
    }

    /**
     * 判断当前目标重命名规则中是否有序列
     *
     * @return
     */
    private Boolean hasSequenceForRename() {
        Boolean hasSequence = Boolean.FALSE;
        Vector<Object> dstNameItem = filter.dstNameRule.getItemList();
        if (dstNameItem != null) {
            for (int i = 0; i < dstNameItem.size(); i++) {
                Object item = dstNameItem.get(i);
                if (item instanceof String) {

                } else if (item instanceof MsgItem) {
                    MsgItem conditionItem = (MsgItem) item;
                    if (ParamsConstant.PARAMS_DST_RULE_SEQUENCE.equals(conditionItem.getName())) {
                        hasSequence = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        return hasSequence;
    }

    /**
     * 将本地文件分发
     *
     * @param transItem
     * @throws Exception
     */
    private void put(TransItem transItem) throws DcmException, Exception {
        logger.debug("begin put local file, file Name:" + transItem.getSourceFile().getFileName()
                + ", start time:" + DateUtil.getCurrent(DateUtil.readPattern) + ", devId: " + id);

        //临时文件后缀
        String tmpLocation = ObjectUtils.toString(this.getDistLinkDto().getLinkParams().get("tmp_location"), ParamsConstant.TMP_LOCATION_SUFFIX);
        String tmp = "." + DcmSystem.random(10000) + ".tmp";
        if (StringUtils.equalsIgnoreCase(tmpLocation, ParamsConstant.TMP_LOCATION_PREFIX)) {
            tmp = "tmp." + DcmSystem.random(10000) + ".";
        }

        //判断目标文件重命名是否包含序列,如果包含序列获取最新序列
        Boolean hasSequence = Boolean.FALSE;
        if (hasSequenceForRename()) {
            hasSequence = Boolean.TRUE;
            transItem.getTargetFile().setFileName(filter.getDstFileName(transItem.getSourceFile(), Sequence.getLastSequence(this.id)));
        }
        logger.debug("hasSequence: " + hasSequence + ", devId: " + id);

        //需要分发的本地文件(源文件)
        String localPath = "";
        //分发到远程主机文件(目标临时文件)
        String remotePath = "";
        try {
            //分发目标路径
            transItem.getTargetFile().setFilePath(dstPath);

            //判断远程目录是否存在,如果不存在则创建
            if (!trans.isExistPath(dstPath)) {
                Boolean isCreateOk = trans.mkdir(FileTool.exactPath(dstPath));
                if (!isCreateOk) {
                    transItem.setErrorMsg("mkdir fail, Please check the permissions.");
                    throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.DIR_CREATE_ERR,
                            "remote mkdir fail, Please check the permissions, remote path <" + dstPath + ">",
                            ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
                }
                logger.debug("put local file, created dst directories ok, directory: " + dstPath + ", devId: " + id);
            }

            String targetFileName = null;

            String localTempFileHidden = ObjectUtils.toString(distLinkDto.getLinkParams().get("local_temp_file_hidden"));
            if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                targetFileName = "." + transItem.getTargetFile().getFileName() + tmp;
            }else{
                targetFileName = transItem.getTargetFile().getFileName() + tmp;
            }
            try {

                //文件上传本地文件、目标文件
                localPath = FileTool.exactPath(transItem.getSourceFile().getFilePath()) + transItem.getSourceFile().getFileName();

                //根据临时文件前缀获取文件名称
                if (StringUtils.equalsIgnoreCase(tmpLocation, ParamsConstant.TMP_LOCATION_PREFIX)) {
                    if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                        targetFileName = "." + tmp + transItem.getTargetFile().getFileName();
                    }else{
                        targetFileName = tmp + transItem.getTargetFile().getFileName();
                    }
                    remotePath = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + targetFileName;
                } else {
                    remotePath = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + targetFileName;
                }

                //文件上传
                long startTimes = System.currentTimeMillis();
                logger.debug("start upload file, local file:" + localPath + ", remote file:" + remotePath
                        + ", startTimes: [" + startTimes + "], devId: " + id);
                trans.put(localPath, remotePath);
                long endTimes = System.currentTimeMillis();
                logger.debug("end upload file, endTimes:[" + endTimes + "], cost times:[" + (endTimes - startTimes) + "] ms, devId: " + id);
            } catch (Exception e) {
                logger.error("put local file fail, devId: " + id, e);
                transItem.setErrorMsg("file transfer fail.");
                throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.PUT_FILE_ERR,
                        "file upload fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
            }

            //记录分发日志
            recordDistLog(transItem, tmp, targetFileName);
            logger.debug("put local file, record dist log ok, devId: " + id);
        } catch (DcmException e) {
            //文件上传失败,回滚序列
            if (hasSequence) {
                Sequence.rollBackSequence(this.id);
                logger.debug("file put fail, rollback sequence, devId:" + id);
            }
            throw e;
        } catch (Exception e) {
            //文件上传失败,回滚序列
            if (hasSequence) {
                Sequence.rollBackSequence(this.id);
                logger.debug("file put fail, rollback sequence, devId:" + id);
            }
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }

        //将本地文件分发到远程备份目录
        try {
            if (!BlankUtil.isBlank(dstPathSd)) {
                logger.debug("put local file, start operator bak file, dstPathSd: " + dstPathSd + ", devId: " + id);
                //判断远程目录是会否存在,如果不存在则创建
                if (!trans.isExistPath(dstPathSd)) {
                    boolean dirRet = trans.mkdir(dstPathSd);
                    logger.debug("create dir, result: " + dirRet + ", devId: " + id);
                }
                //远程备份目录、备份临时文件名称
                String local_temp_file_hidden = ObjectUtils.toString(distLinkDto.getLinkParams().get("local_temp_file_hidden"));
                if(ParamsConstant.PARAMS_1.equals(local_temp_file_hidden)){
                    remotePath = FileTool.exactPath(dstPathSd) + "." + transItem.getSourceFile().getFileName() + tmp;
                }else {
                    remotePath = FileTool.exactPath(dstPathSd) + transItem.getSourceFile().getFileName() + tmp;
                }
                //将本地文件上传到远程备份目录
                trans.put(localPath, remotePath);
                logger.debug("put bak temp file success, devId: " + id);

                //远程文件重命令
                boolean renameRet = trans.rename(remotePath, FileTool.exactPath(dstPathSd) + transItem.getSourceFile().getFileName());
                logger.debug("put bak file, rename result: " + renameRet + ", devId: " + id);
            }
        } catch (Exception e) {
            logger.error("put bak file failed, devId: " + id, e);
            boolean delRet = trans.delete(remotePath);
            logger.debug("put local file, delete bak file, result: " + delRet + ", devId: " + id);
            throw new DcmException(WarnManager.COLL_LATE_HANDLE_FAIL, DcmException.LATE_OPERATOR_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }
    }

    /**
     * 从分布式文件系统写入文件到远程ftp
     *
     * @Title: putFileFromDFS
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: void
     * @author: tianjc
     * @date: 2017年6月7日 上午9:43:52
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private void putFileFromDFS(TransItem transItem) throws DcmException {
        logger.debug("begin put file from dfs, fileName:" + transItem.getSourceFile().getFileName()
                + ", start time:" + (DateUtil.getCurrent(DateUtil.readPattern)) + ", devId: " + id);
        //判断目标文件重命名是否包含序列,如果包含序列获取最新序列
        Boolean hasSequence = Boolean.FALSE;
        if (hasSequenceForRename()) {
            hasSequence = Boolean.TRUE;
            transItem.getTargetFile().setFileName(filter.getDstFileName(transItem.getSourceFile(), Sequence.getLastSequence(this.id)));
        }

        //文件上传
        long startTimes = System.currentTimeMillis();
        try {
            //分发目标路径
            transItem.getTargetFile().setFilePath(dstPath);

            //判断远程目录是否存在,如果不存在则创建
            if (!trans.isExistPath(dstPath)) {
                Boolean isCreateOk = trans.mkdir(FileTool.exactPath(dstPath));
                if (!isCreateOk) {
                    transItem.setErrorMsg("mkdir fail, Please check the permissions.");
                    throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.DIR_CREATE_ERR,
                            "remote mkdir fail, Please check the permissions, remote path <" + dstPath + ">",
                            ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
                }
                logger.debug("put local file, created dst directories ok, directory: " + dstPath + ", devId: " + id);
            }

            //从分布式文件系统读取数据
            Exception error = null;
            byte[] data = null;
            int retryTimes = ParamsConstant.FTP_GET_PUT_TRY_COUNT;

            String dfsFileKey = transItem.getSourceName();
            DFSService dfsService = (DFSService) SpringContextUtil.getBean("dfsService");
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times get file from fastdfs,dfsFileKey:" + dfsFileKey + ",devId:" + this.id);

                //从分布式文件系统获取数据
                try {
                    data = dfsService.read(dfsFileKey);

                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times get file from fastdfs fail,dfsFileKey:" + dfsFileKey + ",devId:" + this.id);
                    error = e;
                }
            }

            //获取文件流失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }

            //输出文件大小
            logger.debug("fileSize:" + ArrayUtils.getLength(data) + ", fileKey: " + dfsFileKey + ", devId: " + this.id);


            //临时文件后缀
            String fileName = transItem.getTargetFile().getFileName();
            String tmpLocation = ObjectUtils.toString(this.getDistLinkDto().getLinkParams().get("tmp_location"), ParamsConstant.TMP_LOCATION_SUFFIX);
            String tmp = "." + DcmSystem.random(10000) + ".tmp";

            String tmpFileName = null;
            String localTempFileHidden = ObjectUtils.toString(distLinkDto.getLinkParams().get("local_temp_file_hidden"));
            if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                tmpFileName = "." + fileName + tmp;
            }else{
                tmpFileName = fileName + tmp;
            }

            if (StringUtils.equalsIgnoreCase(tmpLocation, ParamsConstant.TMP_LOCATION_PREFIX)) {
                tmp = "tmp." + DcmSystem.random(10000) + ".";

                if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                    tmpFileName = "." + tmp + fileName;
                }else {
                    tmpFileName = tmp + fileName;
                }
            }

            //上传目标文件(临时文件)
            String remotePath = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + tmpFileName;
            //上传目标文件(最终文件名称)
            String remotePathFinal = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + fileName;
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times put file to ftp,remotePath:" + remotePath + ",devId:" + this.id);

                //将数据上传到ftp
                ByteArrayInputStream bis = null;
                try {
                    bis = new ByteArrayInputStream(data);
                    boolean result = trans.putFileStream(bis, remotePath);
                    if (result) {
                        boolean renameRst = trans.rename(remotePath, remotePathFinal);
                        logger.info("rename file, sourceFile: " + remotePath + ", finalFile: " + remotePathFinal + ", rename result: " + renameRst + ", devId: " + this.id);

                        error = null;
                        break;
                    } else {
                        throw new RuntimeException("put file to ftp result:" + result);
                    }
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times put file to ftp fail,remotePath:" + remotePath + ",devId:" + this.id);

                    boolean delOk = trans.delete(remotePath);
                    logger.info("delete tmp file, filePath: " + remotePath + ", devId: " + this.id + ", delete result: " + delOk);
                    error = e;
                } finally {
                    IOUtils.closeQuietly(bis);
                }
            }

            //上传到ftp失败
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }
            logger.debug("put file to ftp success, devId: " + id);
        } catch (DcmException e) {
            //文件上传失败,回滚序列
            if (hasSequence) {
                Sequence.rollBackSequence(this.id);
                logger.debug("put file fail, rollback sequence, devId:" + id);
            }

            throw e;
        } catch (Exception e) {
            //文件上传失败,回滚序列
            if (hasSequence) {
                Sequence.rollBackSequence(this.id);
                logger.debug("file put fail, rollback sequence, devId:" + id);
            }
            logger.error("", e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }

        long endTimes = System.currentTimeMillis();
        logger.debug("end put file, cost times:[" + (endTimes - startTimes) + "]ms,devId: " + id);
    }

    /**
     * 从分布式文件系统写入文件到远程ftp
     *
     * @Title: putFileFromDFS
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: void
     * @author: tianjc
     * @date: 2017年6月7日 上午9:43:52
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private void putFileFromFastDFS(TransItem transItem) throws DcmException {
        logger.debug("begin put file from fastdfs, fileName:" + transItem.getSourceFile().getFileName()
                + ", start time:" + (DateUtil.getCurrent(DateUtil.allPattern)) + ", devId: " + id);
        //判断目标文件重命名是否包含序列,如果包含序列获取最新序列
        Boolean hasSequence = Boolean.FALSE;
        if (hasSequenceForRename()) {
            hasSequence = Boolean.TRUE;
            transItem.getTargetFile().setFileName(filter.getDstFileName(transItem.getSourceFile(), Sequence.getLastSequence(this.id)));
        }

        //文件上传
        long startTimes = System.currentTimeMillis();
        try {
            //分发目标路径
            transItem.getTargetFile().setFilePath(dstPath);

            //判断远程目录是否存在,如果不存在则创建
            if (!trans.isExistPath(dstPath)) {
                Boolean isCreateOk = trans.mkdir(FileTool.exactPath(dstPath));
                if (!isCreateOk) {
                    transItem.setErrorMsg("mkdir fail, Please check the permissions.");
                    throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.DIR_CREATE_ERR,
                            "remote mkdir fail, Please check the permissions, remote path <" + dstPath + ">",
                            ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
                }
                logger.debug("put local file, created dst directories ok, directory: " + dstPath + ", devId: " + id);
            }

            //从分布式文件系统读取数据
            Exception error = null;
            byte[] data = null;
            int retryTimes = ParamsConstant.FTP_GET_PUT_TRY_COUNT;

            String redisKey = transItem.getSourceName();
            DCFileService fileService = (DCFileService) SpringContextUtil.getBean("dcFileService");
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times get file from fastdfs,redisKey:" + redisKey + ",devId:" + this.id);

                //从分布式文件系统获取数据
                try {
                    data = fileService.readFile(redisKey);

                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times get file from fastdfs fail,redisKey:" + redisKey + ",devId:" + this.id);
                    logger.error("get file from fastdfs fail", e);
                    error = e;
                }
            }

            //获取文件流失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }

            //输出文件大小
            logger.debug("fileSize:" + ArrayUtils.getLength(data));

            //临时文件后缀
            String fileName = transItem.getTargetFile().getFileName();
            String tmpLocation = ObjectUtils.toString(this.getDistLinkDto().getLinkParams().get("tmp_location"), ParamsConstant.TMP_LOCATION_SUFFIX);
            String tmp = "." + DcmSystem.random(10000) + ".tmp";

            String tmpFileName = null;
            String localTempFileHidden = ObjectUtils.toString(distLinkDto.getLinkParams().get("local_temp_file_hidden"));
            if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                tmpFileName = "." + fileName + tmp;
            }else{
                tmpFileName = fileName + tmp;
            }

            if (StringUtils.equalsIgnoreCase(tmpLocation, ParamsConstant.TMP_LOCATION_PREFIX)) {
                tmp = "tmp." + DcmSystem.random(10000) + ".";

                if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                    tmpFileName = "." + tmp + fileName;
                }else {
                    tmpFileName = tmp + fileName;
                }
            }

            //上传目标文件(临时文件)
            String remotePath = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + tmpFileName;
            //上传目标文件(最终文件名称)
            String remotePathFinal = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + fileName;
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times put file to ftp,remotePath:" + remotePath + ",devId:" + this.id);

                //将数据上传到ftp
                ByteArrayInputStream bis = null;
                try {
                    bis = new ByteArrayInputStream(data);
                    boolean result = trans.putFileStream(bis, remotePath);
                    if (result) {
                        boolean renameRst = trans.rename(remotePath, remotePathFinal);
                        logger.info("rename file, sourceFile: " + remotePath + ", finalFile: " + remotePathFinal + ", rename result: " + renameRst + ", devId: " + this.id);
                        error = null;
                        break;
                    } else {
                        throw new RuntimeException("put file to ftp result:" + result);
                    }
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times put file to ftp fail,remotePath:" + remotePath + ",devId:" + this.id);
                    boolean delOk = trans.delete(remotePath);
                    logger.info("delete tmp file, filePath: " + remotePath + ", devId: " + this.id + ", delete result: " + delOk);
                    error = e;
                } finally {
                    com.alibaba.fastjson.util.IOUtils.close(bis);
                }
            }

            //上传到ftp失败
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }

            logger.debug("put file to ftp success, devId: " + id);
        } catch (DcmException e) {
            //文件上传失败,回滚序列
            if (hasSequence) {
                Sequence.rollBackSequence(this.id);
                logger.debug("put file fail, rollback sequence, devId:" + id);
            }

            throw e;
        } catch (Exception e) {
            //文件上传失败,回滚序列
            if (hasSequence) {
                Sequence.rollBackSequence(this.id);
                logger.debug("file put fail, rollback sequence, devId:" + id);
            }

            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }

        long endTimes = System.currentTimeMillis();
        logger.debug("end put file, cost times:[" + (endTimes - startTimes) + "]ms,devId: " + id);
    }

    /**
     * 从分布式文件系统写入文件到远程ftp
     *
     * @Title: putFileFromDFS
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: void
     * @author: tianjc
     * @date: 2017年6月7日 上午9:43:52
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private void putFileFromFastDFS2(TransItem transItem) throws DcmException {
        logger.debug("begin put file from fastdfs, fileName:" + transItem.getSourceFile().getFileName()
                + ", start time:" + (DateUtil.getCurrent(DateUtil.allPattern)) + ", devId: " + id);
        //文件上传
        long startTimes = System.currentTimeMillis();
        try {
            //分发目标路径
            transItem.getTargetFile().setFilePath(dstPathSd);

            //判断远程目录是否存在,如果不存在则创建
            if (!trans.isExistPath(dstPathSd)) {
                Boolean isCreateOk = trans.mkdir(FileTool.exactPath(dstPathSd));
                if (!isCreateOk) {
                    transItem.setErrorMsg("mkdir fail, Please check the permissions.");
                    throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.DIR_CREATE_ERR,
                            "remote mkdir fail, Please check the permissions, remote path <" + dstPathSd + ">",
                            ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
                }
                logger.debug("put local file, created dst directories ok, directory: " + dstPathSd + ", devId: " + id);
            }

            //从分布式文件系统读取数据
            Exception error = null;
            byte[] data = null;
            int retryTimes = ParamsConstant.FTP_GET_PUT_TRY_COUNT;

            String redisKey = transItem.getSourceName();
            DCFileService fileService = (DCFileService) SpringContextUtil.getBean("dcFileService");
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times get file from fastdfs,redisKey:" + redisKey + ",devId:" + this.id);

                //从分布式文件系统获取数据
                try {
                    data = fileService.readFile(redisKey);

                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times get file from fastdfs fail,redisKey:" + redisKey + ",devId:" + this.id);
                    logger.error("get file from fastdfs fail", e);
                    error = e;
                }
            }

            //获取文件流失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }

            //输出文件大小
            logger.debug("fileSize:" + ArrayUtils.getLength(data));

            //临时文件后缀
            String fileName = transItem.getTargetFile().getFileName();
            String tmpLocation = ObjectUtils.toString(this.getDistLinkDto().getLinkParams().get("tmp_location"), ParamsConstant.TMP_LOCATION_SUFFIX);
            String tmp = "." + DcmSystem.random(10000) + ".tmp";
            String tmpFileName = null;
            String localTempFileHidden = ObjectUtils.toString(distLinkDto.getLinkParams().get("local_temp_file_hidden"));
            if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                tmpFileName = "." + fileName + tmp;
            }else{
                tmpFileName = fileName + tmp;
            }

            if (StringUtils.equalsIgnoreCase(tmpLocation, ParamsConstant.TMP_LOCATION_PREFIX)) {
                tmp = "tmp." + DcmSystem.random(10000) + ".";

                if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                    tmpFileName = "." + tmp + fileName;
                }else {
                    tmpFileName = tmp + fileName;
                }
            }

            //上传目标文件(临时文件)
            String remotePath = FileTool.exactPath(dstPathSd) + tmpFileName;
            //上传目标文件(最终文件名称)
            String remotePathFinal = FileTool.exactPath(dstPathSd) + fileName;
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times put file to ftp,remotePath:" + remotePath + ",devId:" + this.id);

                //将数据上传到ftp
                ByteArrayInputStream bis = null;
                try {
                    bis = new ByteArrayInputStream(data);
                    boolean result = trans.putFileStream(bis, remotePath);
                    if (result) {
                        boolean renameRst = trans.rename(remotePath, remotePathFinal);
                        logger.info("rename file, sourceFile: " + remotePath + ", finalFile: " + remotePathFinal + ", rename result: " + renameRst + ", devId: " + this.id);
                        error = null;
                        break;
                    } else {
                        throw new RuntimeException("put file to ftp result:" + result);
                    }
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times put file to ftp fail,remotePath:" + remotePath + ",devId:" + this.id);
                    boolean delOk = trans.delete(remotePath);
                    logger.info("delete tmp file, filePath: " + remotePath + ", devId: " + this.id + ", delete result: " + delOk);
                    error = e;
                } finally {
                    com.alibaba.fastjson.util.IOUtils.close(bis);
                }
            }

            //上传到ftp失败
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }

            logger.debug("put file to ftp success, devId: " + id);
        }  catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }
        long endTimes = System.currentTimeMillis();
        logger.debug("end put file, cost times:[" + (endTimes - startTimes) + "]ms,devId: " + id);
    }

    /**
     * 从分布式文件系统写入文件到远程ftp
     *
     * @Title: putFileFromDFS
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: void
     * @author: tianjc
     * @date: 2017年6月7日 上午9:43:52
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private void putFileFromDFS2(TransItem transItem) throws DcmException {
        logger.debug("begin put file from dfs, fileName:" + transItem.getSourceFile().getFileName()
                + ", start time:" + (DateUtil.getCurrent(DateUtil.allPattern)) + ", devId: " + id);
        //文件上传
        long startTimes = System.currentTimeMillis();
        try {
            //判断远程目录是否存在,如果不存在则创建
            if (!trans.isExistPath(dstPathSd)) {
                Boolean isCreateOk = trans.mkdir(FileTool.exactPath(dstPathSd));
                if (!isCreateOk) {
                    transItem.setErrorMsg("mkdir fail, Please check the permissions.");
                    throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.DIR_CREATE_ERR,
                            "remote mkdir fail, Please check the permissions, remote path <" + dstPathSd + ">",
                            ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
                }
                logger.debug("put local file, created dst2 directories ok, directory: " + dstPathSd + ", devId: " + id);
            }

            //从分布式文件系统读取数据
            Exception error = null;
            byte[] data = null;
            int retryTimes = ParamsConstant.FTP_GET_PUT_TRY_COUNT;
            String dfsFileKey = transItem.getSourceName();
            DFSService dfsService = (DFSService) SpringContextUtil.getBean("dfsService");
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times get file from fastdfs,dfsFileKey:" + dfsFileKey + ",devId:" + this.id);
                //从分布式文件系统获取数据
                try {
                    data = dfsService.read(dfsFileKey);
                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times get file from fastdfs fail,dfsFileKey:" + dfsFileKey + ",devId:" + this.id);
                    error = e;
                }
            }

            //获取文件流失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }

            //输出文件大小
            logger.debug("fileSize:" + ArrayUtils.getLength(data) + ", fileKey: " + dfsFileKey + ", devId: " + this.id);

            //临时文件后缀
            String fileName = transItem.getTargetFile().getFileName();
            String tmpLocation = ObjectUtils.toString(this.getDistLinkDto().getLinkParams().get("tmp_location"), ParamsConstant.TMP_LOCATION_SUFFIX);
            String tmp = "." + DcmSystem.random(10000) + ".tmp";
            String tmpFileName = null;

            String localTempFileHidden = ObjectUtils.toString(distLinkDto.getLinkParams().get("local_temp_file_hidden"));
            if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                tmpFileName = "." + fileName + tmp;
            }else{
                tmpFileName = fileName + tmp;
            }

            if (StringUtils.equalsIgnoreCase(tmpLocation, ParamsConstant.TMP_LOCATION_PREFIX)) {
                tmp = "tmp." + DcmSystem.random(10000) + ".";

                if(ParamsConstant.PARAMS_1.equals(localTempFileHidden)){
                    tmpFileName = "." + tmp + fileName;
                }else {
                    tmpFileName = tmp + fileName;
                }
            }

            //上传目标文件(临时文件名称)
            String remotePath = FileTool.exactPath(dstPathSd) + tmpFileName;
            //上传目标文件(最终文件名称)
            String remotePathFinal = FileTool.exactPath(dstPathSd) + fileName;
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times put file to ftp, remotePath:" + remotePath + ",devId:" + this.id);

                //将数据上传到ftp
                ByteArrayInputStream bis = null;
                try {
                    bis = new ByteArrayInputStream(data);
                    boolean result = trans.putFileStream(bis, remotePath);
                    if (result) {
                        boolean renameRst = trans.rename(remotePath, remotePathFinal);
                        logger.info("rename file, sourceFile: " + remotePath + ", finalFile: " + remotePathFinal + ", rename result: " + renameRst + ", devId: " + this.id);
                        error = null;
                        break;
                    } else {
                        throw new RuntimeException("put file to ftp result:" + result + ", remotePath: " + remotePath + ", devId: " + id);
                    }
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times put file to ftp fail,remotePath:" + remotePath + ",devId:" + this.id);

                    boolean delOk = trans.delete(remotePath);
                    logger.info("delete tmp file, filePath: " + remotePath + ", devId: " + this.id + ", delete result: " + delOk);

                    error = e;
                } finally {
                    IOUtils.closeQuietly(bis);
                }
            }
            //上传到ftp失败
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }
            logger.debug("put file to remote_path2 success, devId: " + id);
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }

        long endTimes = System.currentTimeMillis();
        logger.debug("end put file, cost times:[" + (endTimes - startTimes) + "]ms,devId: " + id);
    }

    /**
     * 记录分发日志
     *
     * @param transItem
     * @param tmp
     * @param targetFileName 分发目标临时文件名称
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void recordDistLog(TransItem transItem, String tmp, String targetFileName) throws Exception {
        logger.debug("begin add distribute log, source fileName: " + transItem.getSourceFile().getFileName() + ", devId: " + id);

        SqlSession sqlSession = SpringUtil.getCoreBaseDao().getSqlSession();
        DbContextHolder.setDbType(FrameConfigKey.DEFAULT_DATASOURCE);

        //sql接口是否要写入其他数据库
        String datasource = ObjectUtils.toString(this.distLinkDto.getLinkParams().get("datasource"));
        datasource = StringUtils.defaultIfBlank(datasource, FrameConfigKey.DEFAULT_DATASOURCE);

        //获取新的source_id
        Map<String, Object> seqParams = new HashMap<String, Object>();
        seqParams.put("sequenceName", "SEQ_SOURCE_ID");
        Map<String, Object> sourceMap = JdbcUtil.queryForObject("collectMapper.querySequenceByName", seqParams, datasource);
        String newSourceId = null;
        if (!BlankUtil.isBlank(sourceMap) && !sourceMap.isEmpty()) {
            newSourceId = sourceMap.get("ID").toString();
        }

        int step = 0;
        try {
            //添加分发日志表数据
            Map<String, Object> logParams = new HashMap<String, Object>();
            logParams.put("DEV_ID", id);
            logParams.put("METHOD", transItem.getParams().get(ParamsConstant.LINK_EXEC_METHOD));
            logParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
            logParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
            logParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
            logParams.put("ORI_FILE_TIME", transItem.getSourceFile().time(DateUtil.allPattern));
            logParams.put("DST_PATH", transItem.getTargetFile().getFilePath());
            logParams.put("DST_FILE_NAME", transItem.getTargetFile().getFileName());
            logParams.put("DST_FILE_LENGTH", transItem.getTargetFile().getFileLength());
            logParams.put("DST_FILE_TIME", DateUtil.getCurrent(DateUtil.allPattern));
            logParams.put("AFTER_ACTION", transItem.getLateHandleMethod());
            logParams.put("ORI_FILE_RENAME", transItem.getOriFileRename());
            logParams.put("DEAL_TIME", DateUtil.getCurrent(DateUtil.dateFormatPattern));
            logParams.put("MONTHNO", TimeTool.getMonth());
            //拆分后的新source_id
            logParams.put("SOURCE_ID", newSourceId);
            //源文件source_id，来自dc_dist_task
            logParams.put("ORG_SOURCE_ID", StringUtils.defaultIfEmpty(transItem.getSourceFile().getSourceId(), Constant.ZERO));
            int addCnt = JdbcUtil.insertObject("distributeMapper.addDcDistLog", logParams, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.info("add distribute log data ok, exec result: " + addCnt + ", devId: " + id);
            ++step;

            //添加分发SqlJiekou
            String sqlJieKou = StringTool.object2String(distLinkDto.getLinkParams().get("SQL_jiekou"));
            if (!BlankUtil.isBlank(sqlJieKou)) {
                int parentSourceId = getParentSourceId(transItem);
                MsgFormat msgRule = new MsgFormat(sqlJieKou);
                Hashtable<String, String> ruleParams = (Hashtable<String, String>) distLinkDto.getLinkParams().clone();
                Hashtable<String, String> newRuleParams = new Hashtable<String, String>();
                newRuleParams.put("source_id", "'" + transItem.getTargetFile().getSourceId() + "'");
                newRuleParams.put("addr_id", "'" + distLinkDto.getAddrId() + "'");
                newRuleParams.put("link_id", "'" + id + "'");
                newRuleParams.put("file_name", "'" + transItem.getTargetFile().getFileName() + "'");
                newRuleParams.put("file_path", "'" + transItem.getTargetFile().getFilePath() + "'");
                newRuleParams.put("file_length", "'" + transItem.getTargetFile().getFileLength() + "'");
                newRuleParams.put("parent_source_id", "'" + parentSourceId + "'");
                //当前文件时间
                Date fileTime = transItem.getTargetFile().getTime();
                String fileTimeStr = "";
                if (fileTime != null) {
                    fileTimeStr = DateUtil.format(fileTime, DateUtil.allPattern);
                }
                String sourceName = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + transItem.getTargetFile().getFileName();
                newRuleParams.put("source_name", "'" + sourceName + "'");

                newRuleParams.put("file_time", "'" + fileTimeStr + "'");
                newRuleParams.put("JK_oper_list_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_oper_list_id")) + "'");
                newRuleParams.put("JK_switch_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_switch_id")) + "'");
                newRuleParams.put("JK_exchange_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_exchange_id")) + "'");
                newRuleParams.put("JK_collect_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_collect_id")) + "'");
                if (FMT_DIST.equals(latnId)) {
                    newRuleParams.put("JK_latn_id", "'" + latn_Id_Sub + "'");
                } else {
                    newRuleParams.put("JK_latn_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_latn_id")) + "'");
                }
                if (!CollectionUtils.isEmpty(ruleParams)) {
                    Set<String> keys = ruleParams.keySet();
                    for (String key : keys) {
                        String value = ruleParams.get(key);
                        if (StringUtils.isNotBlank(value)) {
                            newRuleParams.put(key, "'" + ObjectUtils.toString(value) + "'");
                        }
                    }
                }
                String jiekouSql = msgRule.format(newRuleParams);
                logger.debug("target sql_jiekou：" + sqlJieKou);

                //遍历执行添加SQL
                Map<String, Object> execParams = new HashMap<String, Object>();
                String[] execSqls = jiekouSql.split("&");
                for (int i = 0; i < execSqls.length; i++) {
                    String execSql = execSqls[i];
                    if (StringUtils.isNotBlank(execSql)) {
                        execParams.put("EXEC_SQL", execSql);
                        int addSqlCnt = JdbcUtil.insertObject("collectMapper.addSqlToExecute", execParams, datasource);
                        logger.debug("add sql_jiekou, effect count: " + addSqlCnt + ", devId: " + id);
                        //sqlSession.insert("collectMapper.addSqlToExecute", execParams);
                    }
                }
                logger.debug("add sql jiekou success, devId: " + id);
                ++step;
            }

            //远程主机临时文件
            String remoteSourceFile = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + targetFileName;
            //远程主机最终文件
            String remoteTargetFile = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + transItem.getTargetFile().getFileName();
            //远程主机文件重命令
            boolean renameRet = trans.rename(remoteSourceFile, remoteTargetFile);
            logger.info("remote file rename ok, result: " + renameRet+ ", devId: " + id);

            //删除分发任务表数据
            Map<String, Object> delParams = new HashMap<String, Object>();
            delParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
            delParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
            delParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
            delParams.put("SOURCE_ID", transItem.getSourceFile().getSourceId());
            delParams.put("RECID", transItem.getSourceFile().getRecId());
            delParams.put("DEV_ID", id);
            int delCnt = JdbcUtil.deleteObject("distributeMapper.delDcDistTask", delParams, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.info("add distribute log, delete dc_dist_task data ok, exec result: " + delCnt + ", devId: " + id);
            ++step;
        } catch (Exception e) {
            //操作失败，回滚数据
            logger.warn("put lateHandle failed, data rollback, devId: " + id);
            removeInvalidLog(newSourceId, step, datasource);
            //记录日志失败，将远程主机临时文件删除
            String sourceFile = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + targetFileName;
            boolean delRet = trans.delete(sourceFile);
            logger.warn("put lateHandle failed, delete temp file, result: " + delRet + ", devId: " + id);

            logger.error("record distribute log fail.", e);
            e.printStackTrace();
            throw e;
        }
        logger.debug("end add distribute log, devId: " + id);
    }

    /**
     * 记录分发日志及删除分发任务表记录,采集格式化跟分发的SOURCE_FILES表不同
     *
     * @param transItem
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public String recordDistLogWithSqlJiekou(TransItem transItem) throws Exception {
        String sqlJieKou = null;
        logger.debug("begin add distribute log, source fileName: " + transItem.getSourceFile().getFileName() + ", devId: " + id);

        String sourceId = transItem.getSourceFile().getSourceId();
        //分发
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("SOURCE_ID", sourceId);
        Map<String, Object> msg = JdbcUtil.queryForObject("collectMapper.querySourceFileRecord", params, transItem.getCollDataSource());

        //sql接口是否要写入其他数据库，获取批价库的数据源，生成的序列也是要批价库的SEQ
        String datasource = ObjectUtils.toString(this.distLinkDto.getLinkParams().get("datasource"));
        datasource = StringUtils.defaultIfBlank(datasource, FrameConfigKey.DEFAULT_DATASOURCE);

        //使用新的序列
        Map<String, Object> seqParams = new HashMap<String, Object>();
        seqParams.put("sequenceName", "SEQ_SOURCE_ID");
        Map<String, Object> sourceMap = JdbcUtil.queryForObject("collectMapper.querySequenceByName", seqParams, datasource);
        String newSourceId = null;
        if (!BlankUtil.isBlank(sourceMap) && !sourceMap.isEmpty()) {
            newSourceId = sourceMap.get("ID").toString();
        }

        int step = 0;
        try {
            //添加分发日志表数据
            Map<String, Object> logParams = new HashMap<String, Object>();
            logParams.put("DEV_ID", id);
            logParams.put("METHOD", transItem.getParams().get(ParamsConstant.LINK_EXEC_METHOD));
            logParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
            logParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
            logParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
            logParams.put("ORI_FILE_TIME", transItem.getSourceFile().time(DateUtil.allPattern));

            logParams.put("DST_PATH", transItem.getSourceFile().getFilePath());
            logParams.put("DST_FILE_NAME", transItem.getSourceFile().getFileName());
            logParams.put("DST_FILE_LENGTH", transItem.getSourceFile().getFileLength());
            logParams.put("DST_FILE_TIME", DateUtil.getCurrent(DateUtil.allPattern));
            logParams.put("AFTER_ACTION", transItem.getLateHandleMethod());
            logParams.put("ORI_FILE_RENAME", transItem.getOriFileRename());
            logParams.put("DEAL_TIME", DateUtil.getCurrent(DateUtil.dateFormatPattern));
            logParams.put("MONTHNO", TimeTool.getMonth());
            logParams.put("START_DEAL_TIME", this.START_DEAL_TIME);
            logParams.put("SOURCE_ID", newSourceId);
            logParams.put("ORG_SOURCE_ID", sourceId);
            JdbcUtil.insertObject("distributeMapper.addDcDistLog", logParams, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.info("add distribute log data ok, devId: " + id);
            ++step;

            //接口语句
            sqlJieKou = StringTool.object2String(this.distLinkDto.getLinkParams().get("SQL_jiekou"));
            if (!BlankUtil.isBlank(sqlJieKou)) {
                if (MapUtils.isEmpty(msg)) {
                    throw new RuntimeException("dist msg is empty, can't add SQL_jiekou, Please check collect link SQL_jiekou.");
                }
                int parentSourceId = getParentSourceId(transItem);

                MsgFormat msgRule = new MsgFormat(sqlJieKou);
                Hashtable<String, String> ruleparams = (Hashtable<String, String>) distLinkDto.getLinkParams().clone();
                Hashtable<String, String> newRuleParams = new Hashtable<String, String>();
                newRuleParams.put("source_id", "'" + newSourceId + "'");
                newRuleParams.put("source_name", "'" + ObjectUtils.toString(msg.get("SOURCE_NAME")) + "'");
                newRuleParams.put("addr_id", "'" + distLinkDto.getAddrId() + "'");
                newRuleParams.put("link_id", "'" + id + "'");
                newRuleParams.put("file_name", "'" + transItem.getSourceFile().getFileName() + "'");
                newRuleParams.put("file_path", "'" + transItem.getSourceFile().getFilePath() + "'");
                newRuleParams.put("file_length", "'" + transItem.getSourceFile().getFileLength() + "'");
                newRuleParams.put("parent_source_id", "'" + parentSourceId + "'");

                //当前文件时间
                Date fileTime = transItem.getSourceFile().getTime();
                String fileTimeStr = "";
                if (fileTime != null) {
                    fileTimeStr = DateUtil.format(fileTime, DateUtil.allPattern);
                }
                newRuleParams.put("file_time", "'" + fileTimeStr + "'");

                newRuleParams.put("JK_oper_type", "'" + ObjectUtils.toString(msg.get("OPER_TYPE")) + "'");
                newRuleParams.put("JK_oper_list_id", "'" + ObjectUtils.toString(msg.get("OPER_LIST_ID")) + "'");
                newRuleParams.put("JK_proc_list", "'" + ObjectUtils.toString(msg.get("PROC_LIST")) + "'");
                newRuleParams.put("JK_switch_id", "'" + ObjectUtils.toString(msg.get("SWITCH_ID")) + "'");
                newRuleParams.put("JK_exchange_id", "'" + ObjectUtils.toString(msg.get("EXCHANGE_ID")) + "'");
                newRuleParams.put("JK_collect_id", "'" + ObjectUtils.toString(msg.get("COLLECT_ID")) + "'");
                newRuleParams.put("JK_latn_id", "'" + ObjectUtils.toString(msg.get("LATN_ID")) + "'");

                newRuleParams.put("lines", "'" + ObjectUtils.toString(transItem.getLines()) + "'");
                newRuleParams.put("lines_id", "'" + ObjectUtils.toString(transItem.getLines()) + "'");
                newRuleParams.put("batch_id", "'" + ObjectUtils.toString(transItem.getBatchId()) + "'");

                if (!CollectionUtils.isEmpty(ruleparams)) {
                    Set<String> keys = ruleparams.keySet();
                    for (String key : keys) {
                        String value = ruleparams.get(key);
                        if (StringUtils.isNotBlank(value)) {
                            newRuleParams.put(key, "'" + ObjectUtils.toString(value) + "'");
                        }
                    }
                }

                sqlJieKou = msgRule.format(newRuleParams);
                logger.debug("target sql_jiekou：" + sqlJieKou);

                //遍历执行添加SQL
                Map<String, Object> execParams = new HashMap<String, Object>();
                String[] execSqls = sqlJieKou.split("&");
                for (int i = 0; i < execSqls.length; i++) {
                    String execSql = execSqls[i];
                    if (StringUtils.isNotBlank(execSql)) {
                        execParams.put("EXEC_SQL", execSql);
                        JdbcUtil.insertObject("collectMapper.addSqlToExecute", execParams, datasource);
                    }
                }
                logger.debug("add sql jiekou success, devId: " + id);
                ++step;
            }

            //删除分发任务表数据
            Map<String, Object> delParams = new HashMap<String, Object>();
            delParams.put("SOURCE_ID", transItem.getSourceFile().getSourceId());
            delParams.put("RECID", transItem.getSourceFile().getRecId());
            delParams.put("DEV_ID", id);
            JdbcUtil.deleteObject("distributeMapper.delDcDistTask", delParams, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.info("add distribute log, delete dc_dist_task data ok, devId: " + id);
            ++step;
        } catch (Exception e) {
            //记录日志失败，将远程主机临时文件删除
            logger.error("record distribute log fail.", e);
            removeInvalidLog(newSourceId, step,datasource);

            throw new DcmException(WarnManager.COLL_LATE_HANDLE_FAIL, DcmException.LATE_OPERATOR_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }

        logger.debug("end add distribute log, devId: " + id);
        return sqlJieKou;
    }

    /**
     * 根据sourceId删除记录
     *  @param sourceId
     * @param step
     * @param datasource
     */
    private void removeInvalidLog(String sourceId, int step, String datasource) {
        logger.info("记录分发日志失败，删除记录,source_id:" + sourceId);

        int size = 0;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("DEV_ID", id);
        paramMap.put("SOURCE_ID", sourceId);

        //删除分发日志记录
        if (step >= 0) {
            try {
                size = JdbcUtil.deleteObject("distributeMapper.delInvalidDistLog", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("删除dc_dist_log记录,source_id:" + sourceId + ",size:" + size);
            } catch (Exception e) {
                logger.error("删除dc_dist_log记录失败,source_id:" + sourceId, e);
            }
        }

        //删除source_files
        if (step >= 1) {
            try {
                size = JdbcUtil.deleteObject("distributeMapper.delInvalidSourceFiles", paramMap, datasource);
                logger.info("删除source_files记录,source_id:" + sourceId + ",size:" + size);
            } catch (Exception e) {
                logger.error("删除source_files记录失败,source_id:" + sourceId, e);
            }
        }
    }

    /**
     * 记录分发日志及删除分发任务表记录
     *
     * @param dataSource 数据源
     * @param jiekouSql
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void recordSqlJiekouWithDatasouce(String dataSource, String jiekouSql) throws Exception {
        logger.debug("开始写入sql接口表:" + dataSource);
        //遍历执行添加SQL
        Map<String, Object> execParams = new HashMap<String, Object>();
        String[] execSqls = jiekouSql.split("&");
        for (int i = 0; i < execSqls.length; i++) {
            String execSql = execSqls[i];
            if (StringUtils.isNotBlank(execSql)) {
                execParams.put("EXEC_SQL", execSql);
                JdbcUtil.insertObject("collectMapper.addSqlToExecute", execParams, dataSource);
            }
        }
        logger.debug("写入sql接口表完成:" + dataSource);
    }

    /**
     * 记录分发日志及删除分发任务表记录
     *
     * @param transItem
     * @param removeDistTask
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void recordDistLog(TransItem transItem, boolean removeDistTask) throws Exception {
        logger.debug("begin add distribute log, source fileName: " + transItem.getSourceFile().getFileName() + ", devId: " + id);
        SqlSession sqlSession = SpringUtil.getCoreBaseDao().getSqlSession();
        DbContextHolder.setDbType(FrameConfigKey.DEFAULT_DATASOURCE);

        //事务回滚步骤
        int step = 0;
        //分发任务表临时数据保存在内存
        Map<String, Object> distTskTempData = null;

        //sql接口是否要写入其他数据库
        String datasource = ObjectUtils.toString(this.distLinkDto.getLinkParams().get("datasource"));
        datasource = StringUtils.defaultIfBlank(datasource, FrameConfigKey.DEFAULT_DATASOURCE);

        try {
            //step 1: 删除分发任务表数据
            if (removeDistTask) {

                Map<String, Object> param = new HashMap<String, Object>();
                param.put("DEV_ID", id);
                param.put("RECID", transItem.getSourceFile().getRecId());
                param.put("SOURCE_ID", transItem.getSourceFile().getSourceId());
                distTskTempData = JdbcUtil.queryForObject("distributeMapper.queryDcDistTaskById", param, FrameConfigKey.DEFAULT_DATASOURCE);

                //删除分发任务表数据
                Map<String, Object> delParams = new HashMap<String, Object>();
                delParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
                delParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
                delParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
                delParams.put("SOURCE_ID", transItem.getSourceFile().getSourceId());
                delParams.put("RECID", transItem.getSourceFile().getRecId());
                delParams.put("DEV_ID", id);
                sqlSession.delete("distributeMapper.delDcDistTask", delParams);
                logger.info("add distribute log, delete dc_dist_task data ok, devId: " + id);
                step++; //1
            }

            //step 2: 添加分发日志表数据

            //添加分发日志表数据
            Map<String, Object> logParams = new HashMap<String, Object>();
            logParams.put("DEV_ID", id);
            logParams.put("METHOD", transItem.getParams().get(ParamsConstant.LINK_EXEC_METHOD));
            logParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
            logParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
            logParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
            logParams.put("ORI_FILE_TIME", transItem.getSourceFile().time(DateUtil.allPattern));

            logParams.put("DST_PATH", transItem.getTargetFile().getFilePath());
            logParams.put("DST_FILE_NAME", transItem.getTargetFile().getFileName());
            logParams.put("DST_FILE_LENGTH", transItem.getTargetFile().getFileLength());
            logParams.put("DST_FILE_TIME", DateUtil.getCurrent(DateUtil.allPattern));
            //logParams.put("AFTER_ACTION", transItem.getLateHandleMethod());
            logParams.put("AFTER_ACTION", "");
            logParams.put("ORI_FILE_RENAME", transItem.getOriFileRename());
            logParams.put("DEAL_TIME", DateUtil.getCurrent(DateUtil.dateFormatPattern));
            logParams.put("MONTHNO", TimeTool.getMonth());
            logParams.put("START_DEAL_TIME", this.START_DEAL_TIME);

            String sourceId = transItem.getSourceFile().getSourceId();
            if (BlankUtil.isBlank(sourceId) || ParamsConstant.PARAMS_0.equals(sourceId)) {

                //重分发日志表获取SourceID信息
                String oriFilePath = transItem.getSourceFile().getFilePath();
                String oriFileName = transItem.getSourceFile().getFileName();
                String oriFileLength = String.valueOf(transItem.getSourceFile().getFileLength());
                long dcDistLogSourceId = this.getSourceIdFromDcDistLog(sqlSession, oriFilePath, oriFileName, oriFileLength);
                sourceId = String.valueOf(dcDistLogSourceId);

                if (ParamsConstant.PARAMS_0.equals(sourceId)) {
                    //重分发任务日常表获取SourceID信息
                    long dcDistTaskSourceId = this.getSourceIdFromDcDistTaskAbn(sqlSession, oriFilePath, oriFileName, oriFileLength);
                    sourceId = String.valueOf(dcDistTaskSourceId);
                    logParams.put("SOURCE_ID", sourceId);
                    logParams.put("ORG_SOURCE_ID", ParamsConstant.PARAMS_0);
                } else {
                    Map<String, Object> querySeqParams = new HashMap<String, Object>();
                    querySeqParams.put("sequenceName", "SEQ_SOURCE_ID");
                    Map<String, Object> seqMap = JdbcUtil.queryForObject("collectMapper.querySequenceByName", querySeqParams, FrameConfigKey.DEFAULT_DATASOURCE);
                    String recId = StringTool.object2String(seqMap.get("ID"));

                    logParams.put("SOURCE_ID", recId);
                    logParams.put("ORG_SOURCE_ID", sourceId);
                }
            } else {
                logParams.put("SOURCE_ID", transItem.getSourceFile().getSourceId());
                logParams.put("ORG_SOURCE_ID", ParamsConstant.PARAMS_0);
            }
            sqlSession.insert("distributeMapper.addDcDistLog", logParams);
            logger.info("add distribute log data ok, devId: " + id);
            step++; //2

            //添加分发SqlJiekou
            String sqlJieKou = StringTool.object2String(distLinkDto.getLinkParams().get("SQL_jiekou"));
            if (!BlankUtil.isBlank(sqlJieKou)) {
                int parentSourceId = getParentSourceId(transItem);

                MsgFormat msgRule = new MsgFormat(sqlJieKou);
                Hashtable<String, String> ruleParams = (Hashtable<String, String>) distLinkDto.getLinkParams().clone();
                ruleParams.put("source_id", "'" + transItem.getTargetFile().getSourceId() + "'");
                ruleParams.put("addr_id", "'" + distLinkDto.getAddrId() + "'");
                ruleParams.put("link_id", "'" + id + "'");
                ruleParams.put("file_name", "'" + transItem.getTargetFile().getFileName() + "'");
                ruleParams.put("file_path", "'" + transItem.getTargetFile().getFilePath() + "'");
                ruleParams.put("parent_source_id", "'" + parentSourceId + "'");

                String sourceName = FileTool.exactPath(transItem.getTargetFile().getFilePath()) + transItem.getTargetFile().getFileName();
                ruleParams.put("source_name", "'" + sourceName + "'");

                ruleParams.put("file_length", "'" + transItem.getTargetFile().getFileLength() + "'");
                ruleParams.put("JK_oper_list_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_oper_list_id")) + "'");
                ruleParams.put("JK_switch_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_switch_id")) + "'");
                ruleParams.put("JK_exchange_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_exchange_id")) + "'");
                ruleParams.put("JK_collect_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_collect_id")) + "'");
                if (FMT_DIST.equals(latnId)) {
                    ruleParams.put("JK_latn_id", "'" + latn_Id_Sub + "'");
                } else {
                    ruleParams.put("JK_latn_id", "'" + StringTool.object2String(distLinkDto.getLinkParams().get("JK_latn_id")) + "'");
                }
                String jiekouSql = msgRule.format(ruleParams);

                //遍历执行添加SQL
                Map<String, Object> execParams = new HashMap<String, Object>();
                String[] execSqls = jiekouSql.split("&");
                for (int i = 0; i < execSqls.length; i++) {
                    String execSql = execSqls[i];
                    execParams.put("EXEC_SQL", execSql);
                    sqlSession.insert("collectMapper.addSqlToExecute", execParams);
                }
                logger.debug("add sql jiekou success, devId: " + id);
                step++; //3
            }

            logger.info("remote file rename ok, devId: " + id);
        } catch (Exception e) {
            recordDistLogRollBack(transItem.getSourceFile().getSourceId(), step, distTskTempData, datasource);
            //记录日志失败，将远程主机临时文件删除
            logger.error("record distribute log fail.", e);
            throw new DcmException(WarnManager.COLL_LATE_HANDLE_FAIL, DcmException.LATE_OPERATOR_ERR,
                    e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, transItem.getSourceFile().getFileName());
        }
        logger.debug("end add distribute log, devId: " + id);
    }

    /**
     * 通过分发任务表的SOURCE_ID、RECID查询source_files表的parent_source_id
     * @param transItem
     * @return
     */
    private int getParentSourceId(TransItem transItem){
        String dataSource = transItem.getCollDataSource();
        //查询parentSourceId
        Map<String,Object> queryParam = new HashMap<String,Object>(){{put("SOURCE_ID",transItem.getSourceFile().getSourceId());}};
        Map<String,Object> parentSourceIdMap = JdbcUtil.queryForObject("distributeMapper.queryParentSourceId",queryParam,dataSource);

        int parentSourceId = -1;
        if(!BlankUtil.isBlank(parentSourceIdMap) && !BlankUtil.isBlank(parentSourceIdMap.get("PARENT_SOURCE_ID"))){
            parentSourceId = Integer.parseInt(StringTool.object2String(parentSourceIdMap.get("PARENT_SOURCE_ID")));
        }
        return parentSourceId;
    }

    //回滚：recordDistLog()
    private void recordDistLogRollBack(String sourceId, int step, Map<String, Object> distTskTempData, String datasource) {
        Map<String, Object> logParams = new HashMap<String, Object>();
        logParams.put("DEV_ID", id);
        logParams.put("SOURCE_ID", sourceId);
        int size;
        //手工事务回滚
        //删除分发任务表失败，数据回滚
        try {
            if (step >= 1) {
                size = JdbcUtil.insertObject("distributeMapper.addDcDistTaskForRollBack", distTskTempData, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("回滚DC_DIST_TASK分发任务表数据记录,source_id:" + sourceId + ",size:" + size);
            }
        } catch (Exception e) {
            logger.error("回滚DC_DIST_TASK记录失败,source_id:" + sourceId);
        }
        try {
            //添加分发日志表数据，数据回滚
            if (step >= 2) {
                size = JdbcUtil.deleteObject("distributeMapper.delInvalidDistLog", logParams, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("删除dc_coll_log记录,source_id:" + sourceId + ",size:" + size);
            }
        } catch (Exception e) {
            logger.error("删除dc_coll_log记录失败,source_id:" + sourceId);
        }
        try {
            if (step >= 3) {
                size = JdbcUtil.deleteObject("distributeMapper.delInvalidSourceFiles", logParams, datasource);
                logger.info("删除source_files记录,source_id:" + sourceId + ",size:" + size);
            }
        } catch (Exception e) {
            logger.error("删除source_files记录失败,source_id:" + sourceId);
        }
    }

    /**
     * 添加分发任务异常表(需要先将分发任务表数据删除，需要事务控制)
     *
     * @param transItem
     * @throws Exception
     */
    public void addExceptionTb(TransItem transItem, DcmException dcmE) throws DcmException {
        logger.debug("begin add distribute exception operator, devId: " + id);
        int step = 0;
        try {
            //添加分发异常任务表
            String errCode = "";
            String errMsg = "";
            if (dcmE != null) {
                errCode = dcmE.getErrorCode();
                errMsg = dcmE.getErrorMsg();
            }

            //获取一个新的序列号
            Map<String, Object> seqParams = new HashMap<String, Object>();
            seqParams.put("sequenceName", "SEQ_DIST_TASK_ABN");
            Map<String, Object> recidMap = JdbcUtil.queryForObject("collectMapper.querySequenceByName", seqParams, FrameConfigKey.DEFAULT_DATASOURCE);
            long newRecID = 0L;
            if (MapUtils.isNotEmpty(recidMap)) {
                newRecID = Long.parseLong(recidMap.get("ID").toString());
            }

            //添加分发异常表
            Map<String, Object> addParams = new HashMap<String, Object>();
            addParams.put("RECID", transItem.getSourceFile().getRecId());
            addParams.put("SOURCE_ID", transItem.getSourceFile().getSourceId());
            addParams.put("DIST_DEV_ID", this.id);
            addParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
            addParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
            addParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
            addParams.put("ORI_FILE_TIME", DateUtil.format(transItem.getSourceFile().getTime(), DateUtil.allPattern));
            addParams.put("LATN_ID", transItem.getLatnId());
            addParams.put("COLL_DEV_ID", transItem.getCollDevID());
            addParams.put("BATCH_ID", transItem.getBatchId());
            addParams.put("LINES", transItem.getLines());
            addParams.put("STATUS", ParamsConstant.PARAMS_0);
            addParams.put("TSK_TYPE", transItem.getTskType());
            addParams.put("EXCEPTIONDESC", StringUtils.substring(errCode + " : " + errMsg, 0, 200));
            JdbcUtil.insertObject("distributeMapper.addDcDistTaskAbn", addParams, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.info("add distribute exception operator, add dc_dist_task_abn data ok, devId: " + id);
            step++;

            //添加分发异常任务历史表，用来记录那些分发文件出现了异常记录
            //判断历史数据是否存在，如果存在则修改，否则添加
            int updCnt = JdbcUtil.updateObject("distributeMapper.updateDcDistTaskAbnHistory", addParams, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.debug("distribute exception operator, update dc_dist_task_abn_history data ok, updCnt: " + updCnt + ", devId: " + id);
            if (updCnt == 0) {
                JdbcUtil.insertObject("distributeMapper.addDcDistTaskAbnHistory", addParams, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("add distribute exception operator, add dc_dist_task_abn_history data ok, devId: " + id);
            }
            step++;

            //删除分发任务表数据
            Map<String, Object> delParams = new HashMap<String, Object>();
            delParams.put("SOURCE_ID", transItem.getSourceFile().getSourceId());
            delParams.put("RECID", transItem.getSourceFile().getRecId());
            delParams.put("DEV_ID", id);
            JdbcUtil.deleteObject("distributeMapper.delDcDistTask", delParams, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.info("add distribute exception operator, delete dc_dist_task data ok, devId: " + id);
            step++;
        } catch (Exception e) {
            logger.error("add distribute exception operator fail, devId: " + id, e);
            removeInvalidExceptionTask(transItem.getSourceFile().getSourceId(), transItem.getSourceFile().getRecId(), transItem.getSourceFile().getFileName(), step);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.DIST_EXCEPTION_ERR, "operate db err " + e.getMessage());
        }
        logger.debug("end add distribute exception operator, devId: " + id);
    }

    /**
     * 回滚分发异常添加异常表数据失败情况
     * @param sourceId
     * @param recId
     * @param oriFileName
     * @param step
     */
    private void removeInvalidExceptionTask(String sourceId, String recId, String oriFileName, int step) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("SOURCE_ID", sourceId);
        params.put("REC_ID", sourceId);
        params.put("DEV_ID", id);
        params.put("ORI_FILE_NAME", oriFileName);

        //删除分发任务处理异常表DC_DIST_TASK_ABN
        if (step >= 1) {
            try {
                int delCnt = JdbcUtil.deleteObject("distributeMapper.delDcDistTaskAbn", params, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("delete dc_dist_task_abn records, source_id:" + sourceId + ",recId: " + recId + ", exec result: " + delCnt + ", devId: " + id);
            } catch (Exception e) {
                logger.error("delete dc_dist_task_abn fail, source_id:" + sourceId + ",recId: " + recId);
                logger.error("", e);
            }
        }

        //删除分发任务异常表以及历史异常表数据
        if(step >= 2) {
            try {
                int delCnt = JdbcUtil.deleteObject("distributeMapper.delDcDistTaskAbnHistory", params, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("delete dc_dist_task_abn_history records, source_id:" + sourceId + ",recId: " + recId + ", exec result: " + delCnt + ", devId: " + id);
            } catch (Exception e) {
                logger.error("delete dc_dist_task_abn_history fail, source_id:" + sourceId + ",recId: " + recId);
                logger.error("", e);
            }
        }
    }

    /**
     * 查询分发有效文件列表
     *
     * @return
     */
    private List<Map<String, Object>> getDistTaskList() {
        logger.debug("begin get distribute task list, latnId: " + latnId + ", devId: " + id);

        List<Map<String, Object>> rstList = null;
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            if (FMT_DIST.equals(latnId)) {
                //修改需要分发记录状态
                params.put("LATN_ID", latn_Id_Sub);
                params.put("LINK_ID", id);
                params.put("DIST_DEV_ID", id);
                params.put("DIST_RECORD_ROW", distRecordRows);
            } else {
                params.put("LINK_ID", id);
                params.put("DIST_DEV_ID", id);
                params.put("DIST_RECORD_ROW", distRecordRows);
            }

            //查询分发文件列表
            rstList = JdbcUtil.queryForList("distributeMapper.queryDistFileListWithLatn", params, FrameConfigKey.DEFAULT_DATASOURCE);
            logger.debug("get distribute task list, query task list ok, latn_Id_Sub: " + latn_Id_Sub
                    + ", list size: " + ArrayUtil.getSize(rstList) + ", devId: " + id);
        } catch (BeansException e) {
            logger.error("get distribute task list fail, devId: " + id);
            throw e;
        }
        logger.debug("end get distribute task list, list size: " + ArrayUtil.getSize(rstList) + ", devId: " + id);
        return rstList;
    }

    /**
     * 获取手动分发文件列表sequence
     *
     * @return
     * @throws Exception
     */
    private Vector<TransItem> getDistRecdList() throws Exception {
        logger.debug("begin get distribute records list, devId: " + id);

        List<Map<String, Object>> distFileTaskList = getDistTaskList();
        logger.debug("get distribute records list ok, list size: "
                + ArrayUtil.getSize(distFileTaskList) + ", devId: " + id);

        //返回文件列表对象
        Vector<TransItem> rstList = new Vector<TransItem>();


        //发送消息队列的任务数
        if (!BlankUtil.isBlank(distFileTaskList)) {

            //获取采集链路时执行接口sql的数据源
            Map<String,Object> queryDatasource = new HashMap<String,Object>(){{put("COLL_DEV_ID",distFileTaskList.get(0).get("COLL_DEV_ID"));}};
            Map<String,Object> datasourceMap = JdbcUtil.queryForObject("distributeMapper.queryDatasourceByCollDevId",queryDatasource,FrameConfigKey.DEFAULT_DATASOURCE);
            String dataSource = FrameConfigKey.DEFAULT_DATASOURCE;
            if(MapUtils.isNotEmpty(datasourceMap)) {
                dataSource = ObjectUtils.toString(datasourceMap.get("DATASOURCE"));
                if (BlankUtil.isBlank(dataSource)) {
                    dataSource = FrameConfigKey.DEFAULT_DATASOURCE;
                }
            }

            for (int i = 0; i < distFileTaskList.size(); i++) {
                Map<String, Object> taskMap = distFileTaskList.get(i);

                //文件名，格式化中文件名包含全路径，所以需要根据目录将文件名单独提取出来
                String oriFileName = StringTool.object2String(taskMap.get("ORI_FILE_NAME"));
                String oriPath = StringTool.object2String(taskMap.get("ORI_PATH"));
                String fileName = StringUtils.removeStart(oriFileName, oriPath);

                //源文件对象
                FileRecord sourceFile = new FileRecord();
                sourceFile.setFileType(FileRecord.FILE);
                sourceFile.setFileName(fileName);
                sourceFile.setFilePath(oriPath);
                sourceFile.setFileLength(Long.parseLong(StringTool.object2String(taskMap.get("ORI_FILE_LENGTH"))));
                sourceFile.setSourceId(StringTool.object2String(taskMap.get("SOURCE_ID")));
                sourceFile.setRecId(StringTool.object2String(taskMap.get("RECID")));
                sourceFile.setTime(DateUtil.parse(StringTool.object2String(taskMap.get("ORI_FILE_TIME")), DateUtil.allPattern));
                if (!filter.check(sourceFile)) {
                    logger.debug("get distribute records list, distribute file had filter, fileName:" + sourceFile.getFileName() + ", devId: " + id);
                    continue;
                }

                //目标文件对象
                FileRecord targetFile = new FileRecord();
                targetFile.setFilePath(dstPath);
                targetFile.setFileType(sourceFile.getFileType());

                //判断目标文件重命名是否包含序列,如果包含序列则在后续put方法中获取包含序列的新命名
                if (hasSequenceForRename() == false) {
                    //目标文件重命名文件名称
                    targetFile.setFileName(this.filter.getDstFileName(sourceFile));
                } else {
                    targetFile.setFileName(fileName);
                }

                targetFile.setFileLength(sourceFile.getFileLength());
                targetFile.setTime(sourceFile.getTime());
                targetFile.setSourceId(sourceFile.getSourceId());

                //分发文件对象(包含源文件、目标文件、以及后续操作信息)
                TransItem transItem = new TransItem(sourceFile, targetFile);
                transItem.setLatnId(StringTool.object2String(taskMap.get("LATN_ID")));
                transItem.setBatchId(NumberUtils.toInt(StringTool.object2String(taskMap.get("BATCH_ID"))));
                transItem.setLines(NumberUtils.toInt(StringTool.object2String(taskMap.get("LINES"))));
                //任务类型
                transItem.setTskType(NumberUtils.toInt(StringTool.object2String(taskMap.get("TSK_TYPE"))));

                    transItem.setCollDevID(StringTool.object2String(taskMap.get("COLL_DEV_ID")));
                String fileStoreType = this.distLinkDto.getFileStoreType();
                if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_DFS.getFileStoreType())
                        || StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_FAST_DFS.getFileStoreType())) {
                    String sourceName = StringTool.object2String(taskMap.get("SOURCE_NAME"));
                    if (StringUtils.isBlank(sourceName)) {
                        //sourceName = "/" + transItem.getCollDevID() + transItem.getSourceFile().getFilePath() + transItem.getSourceFile().getFileName();
                        sourceName = FileTool.exactPath(transItem.getSourceFile().getFilePath()) + fileName;
                    }
                    logger.debug("SOURCE_NAME:" + sourceName + ",DIST_DEV_ID:" + id);
                    transItem.setSourceName(sourceName);
                }

                //设置目标文件分发后的处理方式(rename/delete)
                transItem.setLateHandleMethod(this.filter.lateHandleMethod);
                //目标文件是否需要重命令，如果需要重命令则重命令后的文件名称
                if (transItem.needRename()) {
                    transItem.setOriFileRename(this.filter.getOriFileName(transItem.getSourceFile()));
                }
                //获取分发任务中采集链路ID，如果分发异常用来写入分发异常表
                transItem.getParams().put("COLL_DEV_ID", StringTool.object2String(taskMap.get("COLL_DEV_ID")));
                //文件通过什么方式分发,后续用来记录日志
                transItem.getParams().put(ParamsConstant.LINK_EXEC_METHOD, ParamsConstant.TASK_TYPE_AUTO_DIST);

                //获取采集链路时数据源，用来执行接口sql
                transItem.setCollDataSource(dataSource);

                rstList.add(transItem);
            }
        }
        logger.debug("end get distribute records list, final list size:" + ArrayUtil.getSize(rstList) + ", devId: " + id);

        return rstList;
    }

    /**
     * 生成分发目标目录2
     *
     * @param latnId
     * @param curMonth
     * @param tenDays
     * @param curDay
     */
    private void mkDstPathSd(String latnId, String curMonth, String tenDays, String curDay) throws Exception {
        logger.debug("begin mkDstPathSd, latnId: " + latnId + ", curMonth: " + curMonth
                + ", tenDays: " + tenDays + ", curDay: " + curDay + ", devId: " + id);
        //目标目录
        dstPathSd = FileTool.exactPath(dstPathSd);
        if (dstPathSd.contains("#latn_id")) {
            dstPathSd = dstPathSd.replaceAll("#latn_id", latnId);
        }
        if (dstPathSd.contains("#month")) {
            dstPathSd = dstPathSd.replaceAll("#month", curMonth);
        }
        dstPathSd = FileTool.exactPath(dstPathSd);

        String distDstDirFlag = StringTool.object2String(distLinkDto.getLinkParams().get("dist_dst_dir_flag"));
        if (ParamsConstant.DIR_FLAG_MONTH.equals(distDstDirFlag)) {
            dstPathSd += curMonth;
        } else if (ParamsConstant.DIR_FLAG_TENDAYS.equals(distDstDirFlag)) {
            dstPathSd += tenDays;
        } else if (ParamsConstant.DIR_FLAG_DAY.equals(distDstDirFlag)) {
            dstPathSd += curDay;
        }
        dstPathSd = FileTool.exactPath(dstPathSd);
        logger.debug("end mkDstPathSd, final dstPathSd: " + dstPathSd + ", devId: " + id);
    }

    /**
     * 生成分发目标目录
     *
     * @param latnId
     * @param curMonth
     * @param tenDays
     * @param curDay
     */
    private void mkDstPath(String latnId, String curMonth, String tenDays, String curDay) throws Exception {
        logger.debug("begin mkDstPath, latnId: " + latnId + ", curMonth: " + curMonth
                + ", tenDays: " + tenDays + ", curDay: " + curDay + ", devId: " + id);

        //远程目标目录
        dstPath = StringTool.object2String(distLinkDto.getLinkParams().get("remote_path"));
        dstPath = FileTool.exactPath(dstPath);
        if (dstPath.contains("#latn_id")) {
            dstPath = dstPath.replaceAll("#latn_id", latn_Id_Sub);
        }
        if (dstPath.contains("#month")) {
            dstPath = dstPath.replaceAll("#month", curMonth);
        }
        dstPath = FileTool.exactPath(dstPath);

        String distDstDirFlag = StringTool.object2String(distLinkDto.getLinkParams().get("dist_dst_dir_flag"));
        if (ParamsConstant.DIR_FLAG_MONTH.equals(distDstDirFlag)) {
            dstPath += curMonth;
        } else if (ParamsConstant.DIR_FLAG_TENDAYS.equals(distDstDirFlag)) {
            dstPath += tenDays;
        } else if (ParamsConstant.DIR_FLAG_DAY.equals(distDstDirFlag)) {
            dstPath += curDay;
        }
        dstPath = FileTool.exactPath(dstPath);
        logger.debug("end mkDstPath, final dstPath: " + dstPath + ", devId: " + id);
    }

    /**
     * 生成分发本地目录
     *
     * @param latnId
     * @param curMonth
     * @param tenDays
     * @param curDay
     */
    private void mkLocalPath(String latnId, String curMonth, String tenDays, String curDay) throws Exception {
        logger.debug("begin mkLocalPath, latnId: " + latnId + ", curMonth: " + curMonth
                + ", tenDays: " + tenDays + ", curDay: " + curDay + ", devId: " + id);

        //本地目录
        localPath = StringTool.object2String(distLinkDto.getLinkParams().get("local_path"));

        localPath = FileTool.exactPath(localPath);
        if (localPath.contains("#latn_id")) {
            localPath = localPath.replaceAll("#latn_id", latnId);
        }
        if (localPath.contains("#month")) {
            localPath = localPath.replaceAll("#month", curMonth);
        }
        localPath = FileTool.exactPath(localPath);
        logger.debug("mkdirs localPath: " + localPath + ", devId: " + id);

        //本地目录不存在时创建
            if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(this.getDistLinkDto().getTskType())
                && StringUtils.equalsIgnoreCase(this.getDistLinkDto().getFileStoreType(), FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {
            //判断本地目录是否存在，如果不存在创建
            File localPathFile = new File(localPath);
            boolean mkResult = localPathFile.mkdirs();
            if (!localPathFile.exists()) {
                throw new Exception("mkdirs fail,local_path: " + dstPath + ", mkResult: " + mkResult + ", devId: " + id);
            }
            logger.debug("end mkLocalPath, final localPath:" + localPathFile + ", devId: " + id);
        }
    }

    /**
     * 生成分发本地备份目录
     *
     * @param latnId
     * @param curMonth
     * @param tenDays
     * @param curDay
     */
    private void mkLocalPathBak(String latnId, String curMonth, String tenDays, String curDay) throws Exception {
        logger.debug("begin mkLocalPathBak, latnId: " + latnId + ", curMonth: " + curMonth
                + ", tenDays: " + tenDays + ", curDay: " + curDay + ", devId: " + id);
        //本地目录
        localPathBak = FileTool.exactPath(localPathBak);
        if (localPathBak.contains("#latn_id")) {
            localPathBak = localPathBak.replaceAll("#latn_id", latnId);
        }
        if (localPathBak.contains("#month")) {
            localPathBak = localPathBak.replaceAll("#month", curMonth);
        }
        localPathBak = FileTool.exactPath(localPathBak);

        String localPathBakFlag = StringTool.object2String(distLinkDto.getLinkParams().get("local_path_bak_flag"));
        if (ParamsConstant.DIR_FLAG_MONTH.equals(localPathBakFlag)) {
            localPathBak += curMonth;
        } else if (ParamsConstant.DIR_FLAG_TENDAYS.equals(localPathBakFlag)) {
            localPathBak += tenDays;
        } else if (ParamsConstant.DIR_FLAG_DAY.equals(localPathBakFlag)) {
            localPathBak += curDay;
        }
        localPathBak = FileTool.exactPath(localPathBak);
        logger.debug("mkdirs localPathBak: " + localPathBak + ", devId: " + id);

        if (DistTaskTypeEnum.DIST_FTP.getTskType().equals(this.getDistLinkDto().getTskType())
                && StringUtils.equalsIgnoreCase(this.getDistLinkDto().getFileStoreType(), FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {
            //判断本地目录是否存在，如果不存在创建
            File localPathBakFile = new File(localPathBak);
            boolean mkResult = localPathBakFile.mkdirs();
            if (!localPathBakFile.exists()) {
                throw new Exception("mkdirs fail,localPathBak: " + dstPath + ", mkResult: " + mkResult + ", devId: " + id);
            }
            logger.debug("end mkLocalPathBak, final localPathBak:" + localPathBak + ", devId: " + id);
        }
    }

    /**
     * 修改分发链路对应的Ftp主机(拆分文件用来进行文件多个主机分发)
     *
     * @param latnMap
     */
    private void initLatnInfo(Map<String, Object> latnMap) {
        logger.debug("begin get latn host info, latnMap: " + latnMap + ", devId: " + id);
        //当前本地网ID
        String locId = StringTool.object2String(latnMap.get("LOC_ID"));
        latn_Id_Sub = locId;

        //当前本地网对应ftp/sftp信息
        String newIp = StringTool.object2String(latnMap.get("IP"));
        String newPort = StringTool.object2String(latnMap.get("PORT"));
        String newUserName = StringTool.object2String(latnMap.get("USERNAME"));
        String newPassword = StringTool.object2String(latnMap.get("PASSWORD"));
        distLinkDto.getLinkParams().put("ip", newIp);
        distLinkDto.getLinkParams().put("port", newPort);
        distLinkDto.getLinkParams().put("username", newUserName);
        distLinkDto.getLinkParams().put("password", newPassword);
        logger.debug("end get latn host info, ip: " + newIp + ", port: "
                + newPort + ", username: " + newUserName + ", password: " + newPassword);
    }

    /**
     * 检查参数是否完整，如果不完整则链路不运行
     *
     * @return
     */
    private Boolean checkParams() {
        logger.debug("begin check that the link parameters are correct and complete, miss parameters :"
                + distLinkDto.getIsMissParams() + ", devId: " + id);
        Boolean isMiss = distLinkDto.getIsMissParams();
        if (isMiss) {
            logger.error("link parameters are incorrect or incomplete, cause: " + distLinkDto.getTipsMsg() + ", devId: " + id);
            //添加告警信息
            WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), WarnManager.TRAN_WARN_DFPARAM_NNOR,
                    this.id, "", distLinkDto.getTipsMsg());
            logger.debug("check that the link parameters are correct and complete, add tran warn data OK!");

            //修改链路运行状态为停止状态，链路级别设置为高级别告警
            String tipsMsg = DcmException.INIT_LINK_ERR + ":" + distLinkDto.getTipsMsg();
            updateDistLinkLevel(this.id, ParamsConstant.LINK_TIPS_LEVEL_2, ParamsConstant.COLL_LINK_RUN_STATE_ERR, tipsMsg);
            logger.debug("check that the link parameters are correct and complete, update link TipsLevel OK!");
        }
        logger.debug("end check that the link parameters are correct and complete, devId: " + id);
        return isMiss;
    }

    /**
     * 手动分发
     *
     * @param file_list
     * @param enableFilter
     * @throws Exception
     */
    public void handTransfer(String file_list, boolean enableFilter) throws Exception {
        logger.debug("begin hand transfer, file_list: " + file_list + ", enableFilter: " + enableFilter + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, handTransfer executed fail, devId: " + id);
            return;
        }

        try {
            try {
                init();
                logger.info("hand transfer init ok, devId: " + id);
            } catch (Exception e) {
                logger.error("hand transfer init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.INIT_LINK_ERR,
                        "link initialize fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //获取手动分发文件列表
            Vector<TransItem> rsList = createHandList(file_list, enableFilter);
            logger.info("hand transfer, get hand transfer file list ok, file list size: "
                    + ArrayUtil.getSize(rsList) + ", devId: " + id);

            //手动分发
            if (!BlankUtil.isBlank(rsList)) {
                handTransfer(rsList);
            }

            //分发正常结束,将链路级别修改为正常
            //不修改原有运行状态
//			updateDistLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_0, null, null);
            logger.debug("end hand transfer, devId: " + id);
        } catch (DcmException e) {
            logger.error("hand distribute fail, latnId: " + latnId + ", devId: " + id, e);
            //添加告警信息
            WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), e.getWarnCode(), id, e.getFileName(), e.getErrorMsg());
            //修改链路运行状态
            if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                updateDistLinkLevel(id, e.getTipsLevel(), ParamsConstant.DIST_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
            }

        } catch (Exception e) {
            logger.error("hand distribute fail, latnId: " + latnId + ", devId: " + id, e);
            //添加链路告警信息
            WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), WarnManager.TRAN_WARN_DIST_FAIL, id, "", "handTransfer fail, failure cause: " + e.getMessage());
            //修改链路告警级别
            updateDistLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, DcmException.OTHER_ERR + ":" + e.getMessage());
        }
    }

    /**
     * 获取手动采集文件列表数据
     *
     * @return
     */
    private Vector<TransItem> createHandList(String file_list, boolean enableFilter) {
        logger.debug("begin get hand distribute file list, enableFilter: " + enableFilter + ", devId: " + id);

        //返回手动分发文件列表
        Vector<TransItem> rstList = new Vector<TransItem>();

        if (!BlankUtil.isBlank(file_list)) {
            Vector<String> list = StringTool.tokenString(file_list, "|");
            logger.debug("get hand distribute file list, file list size: " + ArrayUtil.getSize(list) + ", devId: " + id);
            for (int i = 0; i < list.size(); i++) {
                String lineStr = list.get(i);

                //对客户端文件列表参数进行分割
                Vector<String> singleFile = StringTool.tokenString(lineStr, "\t");

                //文件名，格式化中文件名包含全路径，所以需要根据目录将文件名单独提取出来
                String oriFileName = StringTool.object2String(singleFile.get(1));
                String oriPath = StringTool.object2String(singleFile.get(0));
                String fileName = StringUtils.removeStart(oriFileName, oriPath);

                //源文件信息
                FileRecord sourceFile = new FileRecord();
                sourceFile.setFilePath(oriPath);
                sourceFile.setFileType(FileRecord.FILE);
                sourceFile.setFileName(fileName);
                sourceFile.setTime(DateUtil.parse(StringTool.object2String(singleFile.get(2)), DateUtil.allPattern));
                sourceFile.setFileLength(Long.parseLong(StringTool.object2String(singleFile.get(3))));
                //重分发任务表获取SourceID和RECID
                Map<String, Object> retMap = getSourceIdFromDcDistTask(sourceFile.getFilePath(), oriFileName, StringTool.object2String(singleFile.get(3)));
                sourceFile.setSourceId(StringTool.object2String(retMap.get("SOURCE_ID")));
                sourceFile.setRecId(StringTool.object2String(retMap.get("RECID")));

                //目标文件信息
                FileRecord targetFile = new FileRecord();
                targetFile.setFilePath(dstPath);
                targetFile.setFileType(sourceFile.getFileType());
                targetFile.setFileName(sourceFile.getFileName());
                targetFile.setFileLength(sourceFile.getFileLength());
                targetFile.setTime(sourceFile.getTime());
                targetFile.setSourceId(sourceFile.getSourceId());

                //文件项信息
                TransItem transItem = new TransItem(sourceFile, targetFile);
                String sourceName = FileTool.exactPath(sourceFile.getFilePath()) + sourceFile.getFileName();
                logger.info("handle dist file, sourceName: " + sourceName + ", devId: " + id);
                transItem.setSourceName(sourceName);

                //是否使用规则(文件重命名、删除等规则)
                if (enableFilter) {
                    //判断目标文件重命名是否包含序列,如果包含序列则在后续put方法中获取包含序列的新命名
                    if (hasSequenceForRename() == false) {
                        //目标文件重命名文件名称
                        transItem.getTargetFile().setFileName(filter.getDstFileName(sourceFile));
                    } else {
                        transItem.getTargetFile().setFileName(StringTool.object2String(singleFile.get(1)));
                    }

                    //文件分发后续操作
                    transItem.setLateHandleMethod(filter.lateHandleMethod);

                    //源文件是否需要重命名，如果需要重命名获取源文件重命名文件名称
                    if (transItem.needRename()) {
                        transItem.setOriFileRename(filter.getOriFileName(sourceFile));
                    }
                }
                transItem.getParams().put(ParamsConstant.LINK_EXEC_METHOD, ParamsConstant.TASK_TYPE_HAND_DIST);
                rstList.add(transItem);
            }
        }
        logger.debug("end get hand distribute file list, final hand file list size: " + ArrayUtil.getSize(rstList) + ", devId: " + id);
        return rstList;
    }

    /**
     * 获取SourceId
     *
     * @param sqlSession
     * @param filePath
     * @param fileName
     * @param fileLength
     * @return
     */
    private long getSourceIdFromDcDistLog(SqlSession sqlSession, String filePath, String fileName, String fileLength) {
        logger.debug("begin get sourceID from dc_dist_log, filePath:" + filePath + ", fileName: "
                + fileName + ", fileLength: " + fileLength + ", devId: " + id);
        String sourceId = "";
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("ORI_PATH", filePath);
        queryParams.put("ORI_FILE_NAME", fileName);
        queryParams.put("ORI_FILE_LENGTH", fileLength);
        queryParams.put("DEV_ID", id);
        List<Map<String, Object>> logList = JdbcUtil.queryForList("distributeMapper.querySourceIdFromDcDistLog", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
        if (!BlankUtil.isBlank(logList)) {
            Map<String, Object> logMap = logList.get(0);
            sourceId = StringTool.object2String(logMap.get("SOURCE_ID"));
        }
        sourceId = BlankUtil.isBlank(sourceId) ? ParamsConstant.PARAMS_0 : sourceId;
        logger.debug("end get sourceID from dc_dist_log, sourceID: " + sourceId + ", devId: " + id);
        return Long.parseLong(sourceId);
    }

    /**
     * 重分发任务表获取SourceID,RECID
     *
     * @param filePath
     * @param fileName
     * @param fileLength
     * @return
     */
    private Map<String, Object> getSourceIdFromDcDistTask(String filePath, String fileName, String fileLength) {
        logger.debug("begin get sourceID from dc_dist_task, filePath:" + filePath + ", fileName: "
                + fileName + ", fileLength: " + fileLength + ", devId: " + id);
        Map<String, Object> retMap = new HashMap<String, Object>();

        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("ORI_PATH", filePath);
        queryParams.put("ORI_FILE_NAME", fileName);
        queryParams.put("ORI_FILE_LENGTH", fileLength);
        queryParams.put("DEV_ID", id);
        List<Map<String, Object>> logList = JdbcUtil.queryForList("distributeMapper.querySourceIdFromDcDistTask", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
        String sourceId = "";
        String recId = "";
        if (!BlankUtil.isBlank(logList)) {
            Map<String, Object> logMap = logList.get(0);
            sourceId = StringTool.object2String(logMap.get("SOURCE_ID"));
            recId = StringTool.object2String(logMap.get("RECID"));
        }
        sourceId = BlankUtil.isBlank(sourceId) ? ParamsConstant.PARAMS_0 : sourceId;
        retMap.put("SOURCE_ID", sourceId);
        retMap.put("RECID", recId);
        logger.debug("end get sourceID from dc_dist_task, sourceID: " + sourceId + ", recId: " + recId + ", devId: " + id);
        return retMap;
    }

    /**
     * 重分发异常表获取SourceID
     *
     * @param filePath
     * @param fileName
     * @param fileLength
     * @return
     */
    private long getSourceIdFromDcDistTaskAbn(SqlSession sqlSession, String filePath, String fileName, String fileLength) {
        logger.debug("begin get sourceID from dc_dist_task_abn, filePath:" + filePath + ", fileName: "
                + fileName + ", fileLength: " + fileLength + ", devId: " + id);
        String sourceId = "";
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("ORI_PATH", filePath);
        queryParams.put("ORI_FILE_NAME", fileName);
        queryParams.put("ORI_FILE_LENGTH", fileLength);
        queryParams.put("DEV_ID", id);
        List<Map<String, Object>> logList = JdbcUtil.queryForList("distributeMapper.querySourceIdFromDcDistTaskAbn", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
        if (!BlankUtil.isBlank(logList)) {
            Map<String, Object> logMap = logList.get(0);
            sourceId = StringTool.object2String(logMap.get("SOURCE_ID"));
        }
        sourceId = BlankUtil.isBlank(sourceId) ? ParamsConstant.PARAMS_0 : sourceId;
        logger.debug("end get sourceID from dc_dist_task_abn, sourceID: " + sourceId + ", devId: " + id);
        return Long.parseLong(sourceId);
    }

    /**
     * 手动采集文件
     *
     * @param rsList
     * @throws Exception
     */
    private void handTransfer(Vector<TransItem> rsList) throws DcmException {
        logger.debug("begin hand transfer, file list size:" + ArrayUtil.getSize(rsList) + ", devId: " + id);
        //手动分发文件
        try {
            batchTransfer(rsList);
            logger.info("hand transfer, batch transfer ok, devId: " + id);
        } catch (DcmException e) {
            throw e;
        }
        logger.debug("end hand transfer, devId: " + id);
    }


    /**
     * 立即分发
     *
     * @throws Exception
     */
    public void realTransfer() throws DcmException, Exception {
        logger.debug("begin real transfer, latnId: " + distLinkDto.getLatnId() + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, realTransfer executed fail, devId: " + id);
            return;
        }

        //分发链路本地网
        String latnId = distLinkDto.getLatnId();
        if (FMT_DIST.equals(latnId)) {
            //所有本地网对应的Ftp信息
            List<Map<String, Object>> latnList = DcmSystem.distRefreshLinkThrd.latnList;
            logger.debug("real transfer, latnList size: " + ArrayUtil.getSize(latnList) + ", devId: " + id);
            if (!BlankUtil.isBlank(latnList)) {
                for (int i = 0; i < latnList.size(); i++) {
                    Map<String, Object> latnMap = latnList.get(i);
                    String locId = StringTool.object2String(latnMap.get("LOC_ID"));
                    if (FMT_DIST.equals(locId)) {
                        continue;
                    }

                    //切换Ftp信息
                    initLatnInfo(latnMap);

                    //立即分发
                    try {
                        subRealTransfer();
                    } catch (DcmException e) {
                        logger.error("auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
                        //添加告警信息
                        WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), e.getWarnCode(), id, e.getFileName(), e.getErrorMsg());
                        //修改链路运行状态
                        if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                            updateDistLinkLevel(id, e.getTipsLevel(), ParamsConstant.DIST_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
                        }
                        //throw e;
                    } catch (Exception e) {
                        logger.error("auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
                        //添加链路告警信息
                        WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), WarnManager.TRAN_WARN_DIST_FAIL, id, "", "autoTransfer fail, failure cause: " + e.getMessage());
                        //修改链路告警级别
                        updateDistLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, DcmException.OTHER_ERR + ":" + e.getMessage());
                        //throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR, e.getMessage());
                    }
                }
            }
        } else {
            latn_Id_Sub = latnId;
            //立即分发
            try {
                subRealTransfer();
            } catch (DcmException e) {
                logger.error("real distribute fail, latnId: " + latnId + ", devId: " + id, e);
                //添加告警信息
                WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), e.getWarnCode(), id, e.getFileName(), e.getErrorMsg());
                //修改链路运行状态
                if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                    updateDistLinkLevel(id, e.getTipsLevel(), ParamsConstant.DIST_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
                } else {
                    updateDistLinkLevel(id, e.getTipsLevel(), null, e.getErrorCode() + ":" + e.getErrorMsg());
                }
                //throw e;
            } catch (Exception e) {
                logger.error("auto distribute fail, latnId: " + latnId + ", devId: " + id, e);
                //添加链路告警信息
                WarnManager.tranWarn(StringTool.object2String(distLinkDto.getAddrId()), WarnManager.TRAN_WARN_DIST_FAIL, id, "", "autoTransfer fail, failure cause: " + e.getMessage());
                //修改链路告警级别
                updateDistLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, DcmException.OTHER_ERR + ":" + e.getMessage());
                //throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR, e.getMessage());
            }
        }
        logger.debug("end real transfer, devId: " + id);
    }

    /**
     * 立即分发
     * 1、对分发链路参数进行初始化设置
     * 1.1、如果初始化失败直接获取分发文件列表，将分发任务表数据移除到分发任务异常表，将分发链路状态修改为异常，抛出异常，返回
     * 1.2、初始化成功获取分发文件列表,对分发列表进行排序，遍历分发文件
     *
     * @throws Exception
     */
    private void subRealTransfer() throws DcmException, Exception {
        logger.debug("begin Sub real transfer, devId: " + id);

        Vector<TransItem> list = null;
        try {
            init();
        } catch (Exception e) {
            logger.error("real distribute transfer init fail, devId: " + id, e);
            DcmException dcmE = new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.INIT_LINK_ERR,
                    "link initialize fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            try {
                list = getDistRecdList();
                logger.debug("Sub real transfer, get distribute file list for exception ok, list size: " + ArrayUtil.getSize(list) + ", devId:" + id);
            } catch (Exception e1) {
                logger.error("Sub real transfer, get distribute file list for exception fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.GET_FILE_LIST_ERR, "get distribute task records exception, failure cause: " + e1.getMessage());
            }
            if (!BlankUtil.isBlank(list)) {
                for (int i = 0; i < list.size(); i++) {
                    TransItem transItem = list.get(i);
                    addExceptionTb(transItem, dcmE);
                }
            }
            throw dcmE;
        }
        logger.info("Sub real transfer init ok, devId: " + id);

        //获取分发文件任务表
        try {
            list = getDistRecdList();
            logger.info("Sub real transfer, get distribute file list for normal ok, list size: " + ArrayUtil.getSize(list) + ", devId: " + id);
        } catch (Exception e) {
            logger.error("Sub real transfer, get distribute file list for normal fail, devId: " + id);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.GET_FILE_LIST_ERR, "get distribute task records exception, failure cause: " + e.getMessage());
        }

        //分发文件
        try {
            if (!BlankUtil.isBlank(list)) {
                //分发文件进行排序
                Collections.sort(list, new SortObjectBySourceId());
                logger.info("Sub real transfer, sort ok, order by SourceID asc, devId: " + id);

                batchTransfer(list);
                logger.info("Sub real transfer, batch transfer ok, devId: " + id);

            }
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Sub real distribute batch transfer fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.BATCH_DIST_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }

        logger.debug("end Sub real transfer, devId: " + id);
    }

    /**
     * 手动分发,获取源文件列表
     *
     * @param cdt          过滤条件
     * @param isColed_flag 是否上传文件
     * @param beginTime    开始时间
     * @param endTime      结束时间
     * @return
     * @throws Exception
     */
    public Vector<Object> handListOri(Condition cdt, String isColed_flag, String beginTime, String endTime) throws Exception {
        logger.debug("begin get hand source file list, cdt: " + ((cdt == null) ? "" : cdt.toString()) + ", isColed_flag: "
                + isColed_flag + ", beginTime: " + beginTime + ", endTime: " + endTime + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, handListOri executed fail, devId: " + id);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR, distLinkDto.getTipsMsg());
        }

        //本地目录文件
        Vector<Object> rsList = new Vector<Object>();
        try {
            //初始化分发链路
            try {
                init();
                logger.info("get hand source file list, distribute link init ok, devId: " + id);
            } catch (Exception e) {
                logger.error("get hand source file list, distribute link init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.INIT_LINK_ERR,
                        "Failed to initialize link parameters, LINK ID: " + id + ", failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            String fileStoreType = this.distLinkDto.getFileStoreType();
            Vector<FileRecord> fileList = new Vector<FileRecord>();
            if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_DFS.getFileStoreType())) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("DIST_DEV_ID", id);
                //List<Map<String, Object>> pathList = JdbcUtil.queryForList("distributeMapper.queryLinkPathList", param, FrameConfigKey.DEFAULT_DATASOURCE);
                List<Map<String, Object>> pathList = JdbcUtil.queryForList("distributeMapper.queryLinkLocalPathList", param, FrameConfigKey.DEFAULT_DATASOURCE);
                if (!CollectionUtils.isEmpty(pathList)) {
                    DFSService dfsService = (DFSService) SpringContextUtil.getBean("dfsService");
                    //dfs挂载点
                    String dfsMountPoint = SystemProperty.getContextProperty(ParamsConstant.DFS_MOUNT_POINT);
                    dfsMountPoint = StringUtils.removeEnd(dfsMountPoint, "/");

                    for (int i = 0; i < pathList.size(); ++i) {
                        //String collDevID = ObjectUtils.toString(pathList.get(i).get("LINK_ID"));
                        //String path = dfsMountPoint + "/" + id + FileTool.exactPath(ObjectUtils.toString(pathList.get(i).get("ORI_PATH")));
                        //String path = dfsMountPoint + FileTool.exactPath(ObjectUtils.toString(pathList.get(i).get("ORI_PATH")));
                        //对应list方法中添加了挂载点
                        String path = FileTool.exactPath(ObjectUtils.toString(pathList.get(i).get("ORI_PATH")));
                        List<FileRecord> tempList = dfsService.list(path);
                        if (!CollectionUtils.isEmpty(tempList)) {
                            //将文件系统加载点目录去掉
                            for (int j=0; j<tempList.size(); j++) {
                                String filePath = tempList.get(j).getFilePath();
                                filePath = StringUtils.replace(filePath, dfsMountPoint, "");
                                tempList.get(j).setFilePath(filePath);
                            }
                            fileList.addAll(tempList);
                        }
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_FAST_DFS.getFileStoreType())) {
                Vector<FileRecord> tempList = FastDFSService.list(null, this.localPath);
                if (!CollectionUtils.isEmpty(tempList)) {
                    fileList.addAll(tempList);
                }
            } else {
                fileList = getList(getSubPathFlag());
            }

            logger.info("get hand source file list, file list size: " + ArrayUtil.getSize(fileList) + ", devId: " + id);

            //组装前台查询时间过滤条件
            if (!BlankUtil.isBlank(beginTime)) {
                beginTime = beginTime + " 00:00:00";
            }
            if (!BlankUtil.isBlank(endTime)) {
                endTime = endTime + " 23:59:59";
            }

            //获取分发日志表已经分发过的数据(如果前台根据条件进行了过滤则在查询日志表也进行时间过滤，减少list数量，提高效率)
            Vector<FileRecord> distedList = getDistListed(beginTime, endTime);
            logger.info("get hand source file list, had distribute file list size: " + ArrayUtil.getSize(distedList) + ", devId: " + id);

            Iterator<FileRecord> iter = fileList.iterator();
            while (iter.hasNext()) {
                //源文件
                FileRecord record = iter.next();
                //根据前台查询条件校验源文件
                if (filter.check(record, cdt)) {
                    //判断文件是否已经被分发过
                    boolean isDistributed = checkFileHasDist(distedList, record);
                    //1、文件已经分发过的，前台是否上传条件为true
                    //2、文件没有分发过的，前台是否上传条件为false
                    //3、前台是否上传条件为all
                    if ((isDistributed && ParamsConstant.LINK_FILE_UPLOAD_TRUE.equalsIgnoreCase(isColed_flag))
                            || (!isDistributed && ParamsConstant.LINK_FILE_UPLOAD_FALSE.equalsIgnoreCase(isColed_flag))
                            || (ParamsConstant.LINK_FILE_UPLOAD_ALL.equalsIgnoreCase(isColed_flag))) {
                        //过滤前台查询时间条件
                        if (!BlankUtil.isBlank(beginTime) && !BlankUtil.isBlank(endTime)) {
                            if (record.getTime().after(DateUtil.parse(beginTime, DateUtil.allPattern))
                                    && record.getTime().before(DateUtil.parse(endTime, DateUtil.allPattern))) {
                                String line = "";
                                if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_DFS.getFileStoreType())) {
                                    line = "/" + record.getOriPathBak();
                                }
                                line += record.getFilePath() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + record.getFileName() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + DateUtil.format(record.getTime(), DateUtil.allPattern) + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + record.getFileLength() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + isDistributed;

                                rsList.add(line);
                            }
                        } else {
                            String line = "";
                            //if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_DFS.getFileStoreType())) {
                            //    line = "/" + record.getOriPathBak();
                            //}
                            line += record.getFilePath() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + record.getFileName() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + DateUtil.format(record.getTime(), DateUtil.allPattern) + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + record.getFileLength() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + isDistributed;
                            rsList.add(line);
                        }
                    }
                }
            }
            logger.debug("end begin get hand source file list, final file list size: " + ArrayUtil.getSize(rsList) + ", devId: " + id);
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("hand source file list fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR, "Failed to list the local file list, failure cause: " + e.getMessage());
        }
        return rsList;
    }

    /**
     * 手动分发,获取分发目标目录文件列表
     *
     * @param cdt 目标文件过滤参数
     * @return
     * @throws Exception
     */
    public Vector<Object> handListDst(Condition cdt) throws DcmException, Exception {
        logger.debug("begin get hand target file list, cdt:" + ((cdt == null) ? "" : cdt.toString()) + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, handListDst executed fail, devId: " + id);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR, distLinkDto.getTipsMsg());
        }

        Vector<Object> rstList = new Vector<Object>();
        try {

            try {
                init();
                logger.info("get hand target file list init ok, devId: " + id);
            } catch (Exception e) {
                logger.error("get hand target file list init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.INIT_LINK_ERR,
                        "Failed to initialize link parameters, LINK ID: " + id + ", failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //远程登录Ftp/SFtp获取远程目录文件
            try {
                trans.login();
                logger.info("get hand target file list, login ftp/sftp host ok, devId: " + id);
            } catch (Exception e) {
                logger.error("connect ftp exception, devId: " + id, e);
                throw new DcmException(WarnManager.CONN_FTP_ERROR, DcmException.FTP_CONNECT_ERR, "connect ftp exception.", ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //获取远程目录文件列表
            Vector<FileRecord> list = trans.getFileList(FileTool.exactPath(dstPath), null);
            logger.info("get hand target file list, dstPath: " + dstPath + ", file list size: " + ArrayUtil.getSize(list) + ", devId: " + id);

            if (!BlankUtil.isBlank(list)) {
                Iterator<FileRecord> iter = list.iterator();
                while (iter.hasNext()) {
                    FileRecord record = iter.next();
                    //对远程目录文件进行过滤条件校验
                    if (filter.check(record, cdt)) {
                        String line = record.getFilePath() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                + record.getFileName() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                + DateUtil.format(record.getTime(), DateUtil.allPattern) + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                + record.getFileLength();
                        rstList.add(line);
                    }
                }
            }
            logger.debug("end get hand target file list, final file list size: " + ArrayUtil.getSize(rstList) + ", devId: " + id);
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("get hand target file list fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.OTHER_ERR, "Failed to list the remote file list, failure cause: " + e.getMessage());
        } finally {
            trans.close();
        }
        return rstList;
    }

    /**
     * 判断文件是否已经被分发过
     *
     * @param distedList 当前链路已经分发过的文件列表
     * @param fileRecord 当前文件信息
     * @return 是否已经分发
     */
    private boolean checkFileHasDist(Vector<FileRecord> distedList, FileRecord fileRecord) {
        logger.debug("begin check file had distribute, distedList size: " + ArrayUtil.getSize(distedList)
                + ", fileRecord:" + fileRecord.toString() + ", devId: " + id);
        boolean isDist = false;
        if (!BlankUtil.isBlank(distedList)) {
            for (int i = 0; i < distedList.size(); i++) {
                FileRecord record = distedList.get(i);
                if (record.getFileName().equals(fileRecord.getFileName())
                        && record.getFilePath().equals(fileRecord.getFilePath())) {
                    isDist = true;
                    break;
                }
            }
        }
        logger.debug("end check file had distribute, isDist: " + isDist);
        return isDist;
    }

    /**
     * 获取已经分发的文件列表
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    private Vector<FileRecord> getDistListed(String beginTime, String endTime) {
        logger.debug("get had distribute file list, beginTime: " + beginTime + ", endTime: " + endTime + ", devId: " + id);
        Vector<FileRecord> rstList = new Vector<FileRecord>();

        //查询分发日志中已经分发过的文件列表
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("DEV_ID", this.id);
        queryParams.put("beginTime", beginTime);
        queryParams.put("endTime", endTime);
        List<Map<String, Object>> logList = JdbcUtil.queryForList("distributeMapper.queryDistLogById", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
        if (!BlankUtil.isBlank(logList)) {
            for (int i = 0; i < logList.size(); i++) {
                Map<String, Object> logMap = logList.get(i);
                String oriPath = StringTool.object2String(logMap.get("ORI_PATH"));
                String oriFileName = StringTool.object2String(logMap.get("ORI_FILE_NAME"));
                FileRecord fileRecord = new FileRecord();
                fileRecord.setFilePath(oriPath);
                fileRecord.setFileName(oriFileName);
                rstList.add(fileRecord);
            }
        }
        logger.debug("end had distribute file list, file list size:" + ArrayUtil.getSize(rstList) + ", devId: " + id);
        return rstList;
    }

    /**
     * 获取本地文件列表
     *
     * @param flag 是否获取子目录文件列表
     * @return
     * @throws IOException
     */
    private Vector<FileRecord> getList(boolean flag) throws DcmException {
        logger.debug("begin get list, flag: " + flag + ", devId: " + id);

        Vector<FileRecord> rstList = new Vector<FileRecord>();
        //是否包含子目录文件
        try {
            if (flag) {
                //本地目录切换
                local.cd(localPath);
                logger.info("get list, change local directory ok, devId: " + id);

                //获取本地目录文件列表
                Vector<FileRecord> localList = local.list();
                logger.info("get list, get local directory file list ok, list size: " + ArrayUtil.getSize(localList) + ",devId: " + id);

                Iterator<FileRecord> iter = localList.iterator();
                while (iter.hasNext()) {
                    FileRecord fileRecord = iter.next();

                    //当前文件类型为目录则获取子目录文件列表，添加到rstList对象
                    if (fileRecord.isDirectory()) {
                        String newDirPath = FileTool.exactPath(FileTool.exactPath(fileRecord.getFilePath()) + fileRecord.getFileName());
                        logger.info("get list, change new directory, directory name: " + newDirPath + ", devId: " + id);

                        //获取子目录文件
                        getAllFileWithChildren(rstList, newDirPath);
                    } else {
                        rstList.add(fileRecord);
                    }
                }
            } else {
                //获取当前目录文件,不包含子目录文件
                rstList = getList();
            }
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            logger.error("get file list fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.GET_FILE_LIST_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }
        logger.debug("end get list, final file list size: " + ArrayUtil.getSize(rstList) + ", devId: " + id);
        return rstList;
    }

    /**
     * 递归获取目录下所有的文件列表
     *
     * @param rstList
     * @param newDirPath
     */
    private void getAllFileWithChildren(Vector<FileRecord> rstList, String newDirPath) throws DcmException {
        logger.debug("begin get list with chilren directory, rstList size: " + ArrayUtil.getSize(rstList)
                + ", newDirPath:" + newDirPath + ", devId: " + id);
        try {
            //本地目录切换
            local.cd(newDirPath);

            //获取本地目录文件列表
            Vector<FileRecord> newRstList = local.list();
            logger.debug("get list with chilren directory, get directory file list size: " + ArrayUtil.getSize(newRstList) + ", devId: " + id);

            //遍历文件目录，如果包含子目录遍历子目录获取文件
            Iterator<FileRecord> iter = newRstList.iterator();
            while (iter.hasNext()) {
                FileRecord fileRecord = iter.next();
                if (fileRecord.isDirectory()) {
                    newDirPath = FileTool.exactPath(FileTool.exactPath(newDirPath) + fileRecord.getFileName());

                    //递归获取子目录文件，添加到rstList对象
                    getAllFileWithChildren(rstList, newDirPath);
                } else {
                    rstList.add(fileRecord);
                }
            }
        } catch (Exception e) {
            logger.error("get list with chilren directory fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.GET_FILE_LIST_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }
        logger.debug("end get all local file list, devId: " + id);
    }

    /**
     * 获取源目录文件列表
     *
     * @return
     * @throws IOException
     */
    private Vector<FileRecord> getList() throws DcmException {
        logger.debug("begin get file list, devId: " + id);
        Vector<FileRecord> rstList = new Vector<FileRecord>();
        try {
            //切换到本地目录
            local.cd(localPath);
            //获取本地目录文件列表
            rstList = local.list();
            logger.debug("get local file list ok, file list size: " + ArrayUtil.getSize(rstList) + ", devId: " + id);

            //遍历当前目录文件列表，对于目录直接删除
            Iterator<FileRecord> iter = rstList.iterator();
            while (iter.hasNext()) {
                FileRecord fileRecord = iter.next();
                if (fileRecord.isDirectory()) {
                    iter.remove();
                }
            }
            logger.debug("end get file list, rstList:" + ArrayUtil.getSize(rstList) + ", devId: " + id);
        } catch (Exception e) {
            logger.error("get local file list fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_DIST_FAIL, DcmException.GET_FILE_LIST_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }
        return rstList;
    }

    /**
     * 是否获取子目录文件列表
     *
     * @return
     */
    private boolean getSubPathFlag() {
        logger.debug("begin get Sub directory flag, devId: " + id);

        boolean isGetSubPath = Boolean.FALSE;
        String subPathFlag = StringTool.object2String(distLinkDto.getLinkParams().get("sub_path_flag"));
        if (!BlankUtil.isBlank(subPathFlag)) {
            isGetSubPath = ParamsConstant.PARAMS_1.equals(subPathFlag) ? Boolean.TRUE : Boolean.FALSE;
        }
        logger.debug("end get Sub directory flag, flag:" + isGetSubPath + ", devId: " + id);
        return isGetSubPath;
    }

    public String getLatn_Id_Sub() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLatnId() {
        return this.latnId;
    }

    public void setLatn_Id_Sub(String locId) {
        this.latn_Id_Sub = locId;
    }

    public DistLinkDto getDistLinkDto() {
        return this.distLinkDto;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DistLink link = (DistLink) super.clone();
        link.distLinkDto = (DistLinkDto) link.distLinkDto.clone();
        return link;
    }
}

/**
 * 分发文件排序，根据SoureID
 *
 * @author Yuanh
 */
class SortObjectBySourceId implements Comparator<Object>, Serializable {
    private static final long serialVersionUID = 816173929686849893L;

    public int compare(Object obj1, Object obj2) {
        if (obj1 instanceof TransItem && obj2 instanceof TransItem) {
            TransItem sourceTransItem = (TransItem) obj1;
            TransItem targetTransItem = (TransItem) obj2;
            if (sourceTransItem != null
                    && targetTransItem != null
                    && !BlankUtil.isBlank(targetTransItem.getSourceFile().getSourceId())
                    && !BlankUtil.isBlank(sourceTransItem.getSourceFile().getSourceId())) {
                return (int) (Long.parseLong(sourceTransItem.getSourceFile().getSourceId()) - Long.parseLong(targetTransItem.getSourceFile().getSourceId()));
            }
        }
        return 0;
    }
}
