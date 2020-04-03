package com.tydic.dcm.ftran;

import com.alibaba.fastjson.util.IOUtils;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.dcfile.exception.DCFileException;
import com.tydic.dcfile.service.DCFileService;
import com.tydic.dcm.DcmSystem;
import com.tydic.dcm.device.CollSeqCheckThrd;
import com.tydic.dcm.device.LinkDataRefreshThrd;
import com.tydic.dcm.dto.CollLinkDto;
import com.tydic.dcm.dto.WarnLinkDto;
import com.tydic.dcm.dto.WarnSeqDto;
import com.tydic.dcm.enums.FileStoreTypeEnum;
import com.tydic.dcm.enums.LinkParameterModuleEnum;
import com.tydic.dcm.ftran.impl.FtpTran;
import com.tydic.dcm.ftran.impl.SftpTran;
import com.tydic.dcm.openapi.response.OfflineCollectResp;
import com.tydic.dcm.service.DFSService;
import com.tydic.dcm.service.impl.FastDFSService;
import com.tydic.dcm.util.condition.Condition;
import com.tydic.dcm.util.condition.MsgFormat;
import com.tydic.dcm.util.exception.DcmException;
import com.tydic.dcm.util.jdbc.DcaUtil;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.tools.*;
import com.tydic.dcm.warn.WarnManager;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: CollLink
 * @Description: 链路采集类，包括自动采集、手动采集、获取远程目录文件、本地目录文件等
 * @Prject: dcm-server_base
 * @author: yuanhao
 * @date 2016-05-25
 */
public class CollLink {

    /**
     * 采集链路日志对象
     */
    private static Logger logger = Logger.getLogger(CollLink.class);
    /**
     * 本地文件操作对象
     */
    public Local local;
    /**
     * 链路ID
     */
    public String id;
    /**
     * 任务类型(主要用来区分自动采集、立即采集)
     */
    public String taskType;
    /**
     * 采集文件最小大小
     */
    int minFileSize = -1;
    /**
     * 链路参数过滤条件对象
     */
    private Filter filter;
    /**
     * 采集链路Dto对象
     */
    private CollLinkDto collLinkDto;
    /**
     * 文件传输方式(ftp/sftp)
     */
    private Trans trans;
    /**
     * 采集链路目标目录(对于采集就是本地目录)
     */
    private String dstPath;
    /**
     * 采集链路目标目录2(对于采集就是本地目录2)
     */
    private String dstPathSd;
    /**
     * 采集链路目标备份目录(对于采集就是本地备份目录)
     */
    private String dstPathBak;
    /**
     * 采集链路源目录备份目录(对于采集就是远程主机备份目录)
     */
    private String oriPathBak;
    /**
     * 链路类型
     */
    private String subType;
    /**
     * 格式化Sql
     */
    private String sqlJiekou;
    /**
     * 采集开始时间
     */
    private String START_DEAL_TIME = DateUtil.format(new Date(), DateUtil.fullPattern);

    /**
     * list命令过滤条件
     */
    protected String list_cdt = null;

    public CollLink() {
        super();
    }

    /**
     * 采集链路构造函数
     *
     * @param devId 链路ID
     */
    public CollLink(String devId) {
        //本地文件操作对象
        local = new Local();

        //根据链路ID获取采集链路信息
        this.collLinkDto = LinkDataRefreshThrd.getCollLinkAllInfo(devId);
        if (BlankUtil.isBlank(this.collLinkDto)) {
            collLinkDto = new CollLinkDto();
            getCollLink(devId);
        }
        //设置当前链路对象ID
        this.id = StringTool.object2String(collLinkDto.getDevId());
    }

    /**
     * 获取链路信息
     *
     * @param devId
     * @return
     */
    private void getCollLink(String devId) {
        logger.debug("begin get collect link info, devId: " + devId);

        try {
            // 查询链路信息
            Map<String, Object> queryParams = new HashMap<String, Object>();
            queryParams.put("devId", devId);
            Map<String, Object> collLinkMap = JdbcUtil.queryForObject("collectMapper.queryCollLinkInfoById", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
            if (BlankUtil.isBlank(collLinkMap) || collLinkMap.isEmpty()) {
                logger.debug("get collect link info is null, devId: " + devId);
                return;
            }

            //将查询链路属性转化为链路Dto对象
            this.id = devId;
            collLinkDto = ConvertMap2Dto.convert2CollDto(collLinkMap);
            if (StringUtils.equalsIgnoreCase(collLinkDto.getFileStoreType(), FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {
                queryParams.put("MODULE", LinkParameterModuleEnum.COLL_LOCAL.getModule());
            } else {
                queryParams.put("MODULE", LinkParameterModuleEnum.COLL_DFS.getModule());
            }

            logger.info("get collect link attributes ok, devId: " + id);

            //查询链路参数信息
            StringBuffer buffer = new StringBuffer();
            List<Map<String, Object>> collLinkParamsList = JdbcUtil.queryForList("collectMapper.queryCollLinkParamsList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
            if (!BlankUtil.isBlank(collLinkParamsList)) {
                Hashtable<String, Object> linkParams = new Hashtable<String, Object>();
                for (int i = 0; i < collLinkParamsList.size(); i++) {
                    String paramName = StringTool.object2String(collLinkParamsList.get(i).get("PARAM_NAME"));
                    String paramValue = StringTool.object2String(collLinkParamsList.get(i).get("PARAM_VALUE"));

                    //链路参数是否必填
                    String paramIsRequired = StringTool.object2String(collLinkParamsList.get(i).get("IS_REQUIRED"));
                    if (ParamsConstant.PARAMS_1.equals(paramIsRequired) && BlankUtil.isBlank(paramValue)) {
                        buffer.append(paramName);
                        buffer.append(",");
                    }
                    linkParams.put(paramName, paramValue);
                }
                collLinkDto.setLinkParams(linkParams);
            }

            //有必填参数为空或者改链路没有配置链路参数
            if (BlankUtil.isBlank(collLinkParamsList) || !BlankUtil.isBlank(buffer.toString())) {
                String tipsMsg = "link parameter is null or missing required, " + (!BlankUtil.isBlank(buffer) ? "parameter name: " + buffer.toString().substring(0, buffer.toString().length() - 1) : "");
                collLinkDto.setIsMissParams(Boolean.TRUE);
                collLinkDto.setTipsMsg(tipsMsg);
            }
            logger.info("get collect link parameters ok, devId: " + id);
        } catch (Exception e) {
            logger.error("get collect link attributes and parameters fail, devId: " + id, e);
        }
        logger.debug("end get collect link info, devId: " + id);
    }

    /**
     * 手动采集
     *
     * @param file_list    采集列表
     * @param enableFilter 是否启动过滤条件
     * @throws DcmException
     */
    public void handTransfer(String file_list, boolean enableFilter) throws DcmException {
        logger.debug("begin hand transfer, enableFilter: " + enableFilter + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, handTransfer executed fail, devId: " + id);
            return;
        }

        try {
            //根据链路参数进行路径等设置
            try {
                init();
                logger.info("hand transfer initialize ok, devId: " + id);
            } catch (Exception e) {
                logger.error("hand transfer init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.INIT_LINK_ERR,
                        "link initialize fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //获取手动采集文件列表
            Vector<TransItem> rsList = createHandList(file_list, enableFilter);
            logger.debug("create hand list ok, final hand list size: " + ArrayUtil.getSize(rsList) + ", devId: " + id);

            //手动采集
            if (!BlankUtil.isBlank(rsList)) {
                handTransfer(rsList);
            }
            logger.debug("end hand transfer. devId: " + id);
        } catch (DcmException e) {
            logger.error("handTransfer fail, error Code: " + e.getErrorCode() + ", error Cause: " + e.getErrorMsg()
                    + ", error level: " + e.getTipsLevel() + ", devId: " + id, e);

            //添加告警信息
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), e.getWarnCode(), id, e.getFileName(), e.getErrorMsg());
            //修改链路运行状态
            if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                updateCollLinkLevel(id, e.getTipsLevel(), ParamsConstant.COLL_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
            }
            throw e;
        } catch (Exception e) {
            logger.error("handTransfer fail, devId: " + id, e);
            //添加链路告警信息
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), WarnManager.TRAN_WARN_COLL_FAIL, id, "", "handTransfer exception, exception cause:: " + e.getMessage());
            //修改链路告警级别
            updateCollLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, e.getMessage());

            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        } finally {
            this.closeDCAConnection(id);
        }
    }

    /**
     * 手动采集程序
     *
     * @param rsList
     * @throws DcmException
     */
    private void handTransfer(Vector<TransItem> rsList) throws DcmException, Exception {
        logger.debug("begin hand transfer, rsList size: " + ArrayUtil.getSize(rsList) + ", devId: " + id);
        try {
            //文件采集
            batchTransfer(rsList);
            logger.info("hand batch transfer ok, devId: " + id);
        } catch (DcmException e) {
            logger.error("hand batch transfer fail, devId: " + id, e);
            throw e;
        } catch (Exception e) {
            logger.error("hand batch transfer fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }
        logger.debug("end hand transfer, devId: " + id);
    }

    /**
     * 检查参数是否完整，如果不完整则链路不运行
     *
     * @return
     */
    private Boolean checkParams() {
        logger.debug("begin check that the link parameters are correct and complete, miss parameters :"
                + collLinkDto.getIsMissParams() + ", devId: " + id);
        Boolean isMiss = collLinkDto.getIsMissParams();
        if (isMiss) {
            logger.error("link parameters are incorrect or incomplete, cause: " + collLinkDto.getTipsMsg() + ", devId: " + id);

            //添加告警信息
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), WarnManager.TRAN_WARN_CFPARAM_NNOR,
                    this.id, "", collLinkDto.getTipsMsg());
            logger.debug("check that the link parameters are correct and complete, add tran warn data OK!");

            //修改链路运行状态为停止状态，链路级别设置为高级别告警
            String tipsMsg = DcmException.INIT_LINK_ERR + ":" + collLinkDto.getTipsMsg();
            updateCollLinkLevel(this.id, ParamsConstant.LINK_TIPS_LEVEL_2, ParamsConstant.COLL_LINK_RUN_STATE_ERR, tipsMsg);
            logger.debug("check that the link parameters are correct and complete, update link TipsLevel OK!");
        }
        logger.debug("end check that the link parameters are correct and complete, devId: " + id);
        return isMiss;
    }

    /**
     * 查询Ftp主机文件列表
     *
     * @param cdt
     * @return
     */
    public Vector<Object> handListDst(Condition cdt) throws DcmException, Exception {
        logger.debug("begin get hand dist file list, cdt:" + cdt + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, handListDst executed fail, devId: " + id);
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, collLinkDto.getTipsMsg(), ParamsConstant.LINK_TIPS_LEVEL_2);
        }

        //遍历目标文件列表，进行文件参数过滤校验
        Vector<Object> rsList = new Vector<Object>();
        try {
            try {
                //根据链路参数进行源路径、目标路径初始化等设置
                init();
            } catch (Exception e) {
                logger.error("hand list dst file list init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.INIT_LINK_ERR,
                        "Failed to initialize link parameters, LINK ID: " + id + ", failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }
            logger.info("hand list dst file list init ok, devId: " + id);

            String fileStoreType = this.collLinkDto.getFileStoreType();
            logger.info("get handListDst, fileStoreType: " + fileStoreType + ", devId: " + id);
            Vector<FileRecord> fileList = new Vector<FileRecord>();
            if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_DFS.getFileStoreType())) {
                logger.debug("get handListDst file list from dfs:" + dstPath + ",devId: " + id);

                DFSService dfsService = (DFSService) SpringContextUtil.getBean("dfsService");
                //dfs挂载点
                String dfsMountPoint = SystemProperty.getContextProperty(ParamsConstant.DFS_MOUNT_POINT);
                dfsMountPoint = StringUtils.removeEnd(dfsMountPoint, "/");
                //String path = dfsMountPoint + "/" + id + FileTool.exactPath(ObjectUtils.toString(dstPath));
                String path = dfsMountPoint + FileTool.exactPath(ObjectUtils.toString(dstPath));
                List<FileRecord> tempList = dfsService.list(path);
                if (!CollectionUtils.isEmpty(tempList)) {
                    fileList.addAll(tempList);
                }

            } else if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_FAST_DFS.getFileStoreType())) {
                logger.debug("get handListDst file list from fastdfs, remotePath:" + dstPath + ",devId: " + id);
                String path = FileTool.exactPath(ObjectUtils.toString(dstPath));
                Vector<FileRecord> tempList = FastDFSService.list(id, path);
                if (!CollectionUtils.isEmpty(tempList)) {
                    fileList.addAll(tempList);
                }
            } else {
                logger.debug("获取本地目录文件列表:" + dstPath + ",dev_id:" + id);
                //本地切换目录
                local.cd(dstPath);
                logger.info("hand list dst file list change dir ok, devId: " + id);
                //获取目标目录文件列表(对于采集就是本地文件列表)
                fileList = local.list();
            }

            logger.info("hand list dst file list, file list size: " + ArrayUtil.getSize(fileList) + ", devId: " + id);

            Iterator<FileRecord> iter = fileList.iterator();
            while (iter.hasNext()) {
                FileRecord record = iter.next();
                //根据前台参数进行文件过滤校验
                if (filter.check(record, cdt)) {
                    String line = "";
                    line += record.getFilePath() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                            + record.getFileName() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                            + DateUtil.format(record.getTime(), DateUtil.allPattern) + ParamsConstant.DEFAULT_TAB_SEPARATOR
                            + record.getFileLength();
                    rsList.add(line);
                }
            }

            logger.debug("end get hand dst file list, rsList: " + ArrayUtil.getSize(rsList) + ", devId: " + id);
        } catch (DcmException e) {
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), e.getWarnCode(), id, "", "Failed to list the local file list, failure cause:" + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("hand list dst file list fail, devId: " + id, e);
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), WarnManager.TRAN_WARN_COLL_FAIL, id, "", "Failed to list the local file list, failure cause:" + e.getMessage());

            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, "Failed to list the local file list, failure cause: " + e.getMessage());
        } finally {
            this.closeDCAConnection(id);
        }
        return rsList;
    }

    /**
     * 获取手动采集远程目录文件列表
     *
     * @param cdt          过滤参数
     * @param isColed_flag 是否已经上传
     * @param beginTime    开始时间
     * @param endTime      结束时间
     * @return
     * @throws Exception
     */
    public Vector<Object> handListOri(Condition cdt, String isColed_flag, String beginTime, String endTime) throws Exception {
        logger.debug("begin hand list ori file list, cdt:" + (cdt == null ? "null" : cdt.toString())
                + ", isColed_flag: " + isColed_flag + ", beginTime: " + beginTime + ", endTime: " + endTime + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, handListDst executed fail, devId: " + id);
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, collLinkDto.getTipsMsg());
        }

        //远程目录文件
        Vector<Object> rsList = new Vector<Object>();
        try {
            try {
                //根据链路参数进行路径等设置
                init();
                logger.info("hand list ori file list init ok, devId: " + id);
            } catch (Exception e) {
                logger.error("hand list ori file list init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.INIT_LINK_ERR,
                        "Failed to initialize link parameters, LINK ID: " + id + ", failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //获取源目录文件列表(对于采集源目录就是远程目录文件列表)
            Vector<FileRecord> remoteFileList = getList(getSubPathFlag());
            logger.debug("hand list ori file list, file list size: " + ArrayUtil.getSize(remoteFileList) + ", devId: " + id);

            //获取远程目录未采集的文件列表
            List<FileRecord> unCollectedFileList = getUnCollectedFileList(remoteFileList);
            logger.debug("hand list ori uncollected file list, file list size: "
                    + ArrayUtil.getSize(unCollectedFileList) + ", devId: " + id);

            //查询时间过滤
            if (!BlankUtil.isBlank(beginTime)) {
                beginTime = beginTime.trim() + "00:00:00";
            }
            if (!BlankUtil.isBlank(endTime)) {
                endTime = endTime.trim() + "23:59:59";
            }

            //远程目录文件列表
            Iterator<FileRecord> iter = remoteFileList.iterator();
            String checkCondition = StringTool.object2String(collLinkDto.getLinkParams().get("check_condition"));
            while (iter.hasNext()) {
                FileRecord record = iter.next();

                //根据前台查询条件校验源文件
                if (filter.check(record, cdt)) {
                    //判断文件是否被采集过(true:采集过的，false:未采集过)
                    record.setCheckCondition(checkCondition);
                    //自定义类使用Collection的contains方法需要重写equals跟hash方法
                    boolean isCollected = !unCollectedFileList.contains(record);

                    //1、文件已经采集过的，前台是否上传条件为true
                    //2、文件没有采集过的，前台是否上传条件为false
                    //3、前台是否上传条件为all
                    if ((isCollected && ParamsConstant.LINK_FILE_UPLOAD_TRUE.equalsIgnoreCase(isColed_flag))
                            || (!isCollected && ParamsConstant.LINK_FILE_UPLOAD_FALSE.equalsIgnoreCase(isColed_flag))
                            || (ParamsConstant.LINK_FILE_UPLOAD_ALL.equalsIgnoreCase(isColed_flag))) {

                        //过滤前台查询时间条件
                        if (!BlankUtil.isBlank(beginTime) && !BlankUtil.isBlank(endTime)) {
                            if (record.getTime().after(DateUtil.parse(beginTime, DateUtil.allPattern))
                                    && record.getTime().before(DateUtil.parse(endTime, DateUtil.allPattern))) {
                                String line = record.getFilePath() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + record.getFileName() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + DateUtil.format(record.getTime(), DateUtil.allPattern) + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + record.getFileLength() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                        + isCollected;
                                rsList.add(line);
                            }
                        } else {
                            String line = record.getFilePath() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + record.getFileName() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + DateUtil.format(record.getTime(), DateUtil.allPattern) + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + record.getFileLength() + ParamsConstant.DEFAULT_TAB_SEPARATOR
                                    + isCollected;
                            rsList.add(line);
                        }
                    }
                }
            }
            logger.debug("end hand list ori file list, result size:" + ArrayUtil.getSize(rsList) + ", devId: " + id);
        } catch (DcmException e) {
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), e.getWarnCode(), id, "", "Failed to list the remote file list, failure cause:" + e.getMessage());

            throw e;
        } catch (Exception e) {
            logger.error("hand list ori file list fail, devId: " + id, e);

            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), WarnManager.TRAN_WARN_COLL_FAIL, id, "", "Failed to list the remote file list, failure cause:" + e.getMessage());
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, "Failed to list the remote file list, failure cause: " + e.getMessage());
        } finally {
            this.closeDCAConnection(id);
        }
        return rsList;
    }

    /**
     * 手动采集,源文件列表获取当前未被采集的文件列表
     *
     * @param remoteFileList
     * @return
     */
    private List<FileRecord> getUnCollectedFileList(Vector<FileRecord> remoteFileList) {
        logger.debug("begin get uncollected file list, file list size: "
                + ArrayUtil.getSize(remoteFileList) + ", devId: " + id);
        //有效文件列表
        List<FileRecord> fileList = new ArrayList<FileRecord>();

        if (!BlankUtil.isBlank(remoteFileList)) {
            try {
                //文件名比对去重服务
                String checkCondition = StringTool.object2String(collLinkDto.getLinkParams().get("check_condition"));

                //文件是否进行剔重操作，默认进行剔重
                fileList = remoteFileList;
                Boolean isFileFilterSwitch = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_FILE_FILTER_SWITCH, Boolean.TRUE);
                logger.info("current file filter switch :" + isFileFilterSwitch);
                if (isFileFilterSwitch) {
                    fileList = FileComparison.getEffectFileList(this.id, remoteFileList, checkCondition);
                } else {
                    logger.warn("current file not filter with SQL or Redis!!");
                }

                logger.info("get uncollected file list ok, file list size: "
                        + ArrayUtil.getSize(fileList) + ", devId: " + id);

                if (!BlankUtil.isBlank(fileList)) {
                    Iterator<FileRecord> iterator = fileList.iterator();
                    while (iterator.hasNext()) {
                        FileRecord fileRecord = iterator.next();

                        if (BlankUtil.isBlank(fileRecord.getFileLength())) {
                            fileRecord.setFileLength(Long.parseLong(ParamsConstant.PARAMS_0));
                        }
                        //不让文件大小小于设置的最小文件大小
                        if (fileRecord.getFileLength() <= minFileSize) {
                            iterator.remove();
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("get uncollected file list fail, devId: " + id, e);
            }
        }
        logger.debug("end get uncollected file list, file list size: " + ArrayUtil.getSize(fileList) + ", devId: " + id);
        return fileList;
    }

    /**
     * 获取手动采集文件列表
     *
     * @param file_list    手动采集文件列表
     * @param enableFilter 是否启动过滤条件
     */
    private Vector<TransItem> createHandList(String file_list, boolean enableFilter) {
        logger.debug("begin get hand collect file list, file_list: " + file_list + ", enableFilter: "
                + enableFilter + ", devId: " + id);

        Vector<TransItem> rstList = new Vector<TransItem>();

        //对手动采集文件列表进行分割,分割结果是每个文件信息成一个字串
        Vector<String> list = StringTool.tokenStringChar(file_list, "|");
        for (int i = 0; i < list.size(); i++) {
            String lineStr = list.get(i);

            //对单个文件信息进行分割，获取文件属性信息
            //lineStr组成信息:  文件路径 + "\t" + 文件名称+ "\t" + 文件创建时间  + "\t" + 文件长度
            Vector<String> singleFileList = StringTool.tokenStringChar(lineStr, ParamsConstant.DEFAULT_TAB_SEPARATOR);

            //源文件信息
            FileRecord sourceFile = new FileRecord();
            sourceFile.setFilePath(singleFileList.get(0));     //文件目录
            sourceFile.setFileName(singleFileList.get(1));     //文件名称
            sourceFile.setTime(DateUtil.parse(singleFileList.get(2), DateUtil.allPattern));   //文件时间
            sourceFile.setFileLength(Long.parseLong(singleFileList.get(3)));   //文件长度

            String oriBakPath = this.oriPathBak;
            if (StringUtils.isBlank(oriBakPath)) {
                oriBakPath = singleFileList.get(0);
            } else {
                String remotePath = ObjectUtils.toString(collLinkDto.getLinkParams().get("remote_path"));
                String oriFilePath = singleFileList.get(0);
                oriBakPath = StringUtils.replace(oriFilePath, remotePath, oriBakPath);
            }
            //文件备份目录
            sourceFile.setOriPathBak(oriBakPath);

            //目标文件信息
            FileRecord targetFile = new FileRecord();
            targetFile.setFilePath(dstPath);
            targetFile.setFileName(sourceFile.getFileName());
            targetFile.setTime(sourceFile.getTime());
            targetFile.setFileLength(sourceFile.getFileLength());

            //文件信息(包含源文件)
            TransItem transItem = new TransItem(sourceFile, targetFile);

            //是否使用规则(文件重命名、删除等规则)
            if (enableFilter) {
                //目标文件重命名文件名称
                transItem.getTargetFile().setFileName(filter.getDstFileName(sourceFile));
                //文件采集后续操作
                transItem.setLateHandleMethod(filter.lateHandleMethod);
                //源文件重命名文件名称
                if (transItem.needRename()) {
                    transItem.getTargetFile().setFileName(filter.getDstFileName(transItem.getSourceFile()));
                }

                //设置采集源文件后续操作方法
                transItem.setOriLateHandleMethod(filter.oriLatehandleMethod);
                if (transItem.oriNeedRename()) {
                    transItem.setOriFileRename(filter.getOriFileName(sourceFile));
                }
            }
            //设置采集方法，后续记录日志使用
            //transItem.getParams().put(ParamsConstant.LINK_EXEC_METHOD, ParamsConstant.TASK_TYPE_HAND_COLL);
            rstList.add(transItem);
        }
        logger.debug("end get hand collect file list, rstList size:" + ArrayUtil.getSize(rstList) + ", devId: " + id);
        return rstList;
    }

    /**
     * 自动采集
     */
    public void autoTransfer() throws DcmException, Exception {
        long startTimes = System.currentTimeMillis();
        logger.debug("************** begin auto transfer, startTimes:" + startTimes + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, autoTransfer executed fail, devId: " + id);
            return;
        }

        Vector<TransItem> list = null;
        try {
            //根据链路参数进行路径等设置
            try {
                init();
                logger.info("auto transfer initialize ok, devId: " + id);
            } catch (Exception e) {
                logger.error("auto transfer init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.INIT_LINK_ERR,
                        "link initialize fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //获取远程目录有效文件列表
            list = getAvaiableList();
            logger.info("auto transfer get avaiable file list, size: " + ArrayUtil.getSize(list) + ", devId: " + id);

            //添加采集告警时间,如果采集文件数量不为空，需要更新当前采集链路最新分发时间
            if (!BlankUtil.isBlank(list)) {
                //更新链路告警最新时间
                addWarnRefreshLink();

                //文件采集
                batchTransfer(list);
            }

            logger.info("auto transfer batch ok, devId: " + id);
        } catch (DcmException e) {
            logger.error("autoTransfer fail, error Code: " + e.getErrorCode() + ", error Cause: " + e.getErrorMsg() + ", error level: " + e.getTipsLevel() + ", devId: " + id, e);
            //修改链路运行状态
            if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                updateCollLinkLevel(id, e.getTipsLevel(), ParamsConstant.COLL_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
            } else if (StringUtils.equals(this.taskType, ParamsConstant.TASK_TYPE_AUTO_COLL)) {//自动采集才会修改成轻微异常
                updateCollLinkLevel(id, e.getTipsLevel(), null, e.getErrorCode() + ":" + e.getErrorMsg());
            }

            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), e.getWarnCode(), id, "", "autoTransfer error, failure cause: " + e.getErrorMsg());

            //更新链路告警最新时间
            addWarnRefreshLink();
        } catch (Exception e) {
            logger.error("autoTransfer fail, devId: " + id, e);
            //添加链路告警信息
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), WarnManager.TRAN_WARN_COLL_FAIL, id, "", "autoTransfer error, failure cause: " + e.getMessage());
            //修改链路告警级别
            if (StringUtils.equals(this.taskType, ParamsConstant.TASK_TYPE_AUTO_COLL)) {//自动采集才会修改成轻微异常
                updateCollLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, e.getMessage());
            }

            //更新链路告警最新时间
            addWarnRefreshLink();
        } finally {
            this.closeDCAConnection(id);
        }
        long endTimes = System.currentTimeMillis();
        logger.debug("end auto transfer, endTimes: [" + endTimes + "], cost total times: ["
                + (endTimes - startTimes) + "] millisecond, collect total file number: " + ArrayUtil.getSize(list) + ", devId: " + id);
    }

    /**
     * 添加告警信息，当FTP连不上时不需要提示长时间无文件生成告警
     */
    private void addWarnRefreshLink() {
        //更新链路告警最新时间
        synchronized (DcmSystem.warnRefreshLinkThrd.getCollWarnHashTab()) {
            if (DcmSystem.warnRefreshLinkThrd.getCollWarnHashTab().keySet().contains(id)) {
                WarnLinkDto warnLinkDto = DcmSystem.warnRefreshLinkThrd.getCollWarnHashTab().get(id);
                warnLinkDto.setLastTime(TimeTool.getTime());
                DcmSystem.warnRefreshLinkThrd.getCollWarnHashTab().put(id, warnLinkDto);
                logger.debug("auto transfer success modify warn refresh time, devId: " + id);
            }
        }
    }

    /**
     * 自动采集文件传输
     *
     * @param list
     */
    private Vector<TransItem> batchTransfer(Vector<TransItem> list) throws DcmException, Exception {
        logger.info("begin batch transfer, file size: " + ArrayUtil.getSize(list) + ", devId: " + id);

        try {
            try {
                trans.login();
            } catch (Exception e) {
                logger.error("connect ftp exception, devId: " + id, e);
                throw new DcmException(WarnManager.CONN_FTP_ERROR, DcmException.FTP_CONNECT_ERR, "connect ftp exception.", ParamsConstant.LINK_TIPS_LEVEL_1);
            }
            logger.info("batch transfer login remote host ok, devId: " + id);


            //文件批量采集传输过程中是否存在异常
            DcmException error = null;

            //遍历采集有效文件列表
            Vector<TransItem> tempList = new Vector<TransItem>();
            for (int i = 0; i < list.size(); i++) {
                TransItem item = list.get(i);
                try {
                    //更新采集开始时间
                    START_DEAL_TIME = DateUtil.format(new Date(), DateUtil.fullPattern);

                    transfer(item);

                    //将采集文件对象添加到临时队列，用来进行文件连续性校验
                    tempList.add(item);

                    //采集成功
                    item.setTransferReuslt(true);
                } catch (DcmException e) {
                    WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), WarnManager.TRAN_WARN_COLL_FAIL, id,
                            item.getSourceFile().getFileName(), e.getErrorCode() + ":" + e.getErrorMsg());

                    error = e;

                    logger.error("current file collect fail, devId: " + id + ", fileName: " + item.getSourceFile().getFileName());
                }
            }

            //添加文件连续性校验,采集成功的文件列表加入检验列表
            synchronized (CollSeqCheckThrd.collSeqFileHashTab) {
                WarnSeqDto seqDto = new WarnSeqDto(this.id, tempList, this.collLinkDto);
                CollSeqCheckThrd.collSeqFileHashTab.put(this.id, seqDto);
            }

            //批量采集文件过程中如果出现文件采集失败异常，采集链路记录异常原因
            if (!BlankUtil.isBlank(error)) {
                DcmException dcmE = new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, error.getErrorCode(), error.getErrorMsg(), ParamsConstant.LINK_TIPS_LEVEL_1);
                updateCollLinkLevel(id, dcmE.getTipsLevel(), null, dcmE.getErrorCode() + ":" + dcmE.getErrorMsg());
            }

            logger.info("batch transfer all ok, devId: " + id);
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, "batchTransfer file exception, exception cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        } finally {
            trans.close();
            this.closeDCAConnection(id);
        }
        logger.info("end batch transfer ok, devId: " + id);

        return list;
    }

    /**
     * 关闭DCA连接
     *
     * @param devId
     */
    private void closeDCAConnection(String devId) {
        //将DCA连接关闭
        String processLinkId = devId + "_" + Thread.currentThread().getId();
        logger.info("close DCA connection, devId:" + processLinkId);
        DcaUtil.close(processLinkId);
    }

    /**
     * 文件采集
     *
     * @param item
     * @throws DcmException
     */
    private void transfer(TransItem item) throws DcmException {
        logger.debug("begin transfer one file, devId: " + id);
        try {

            Map<String, Object> seqParams = new HashMap<String, Object>();
            seqParams.put("sequenceName", "SOURCE_ID");

            long sourceId = getSourceId();

            String dfsFileKey = this.getDfsFileKey(item.getTargetFile());
            String fileStoreType = this.collLinkDto.getFileStoreType();
            logger.debug("dfsFileKey --->" + dfsFileKey + ", fileStoreType --> " + fileStoreType);

            //采集2路径是否为空，如果不为空则需要写入文件系统
            int step = 0;
            String dsfFileKey2 = null;
            if (StringUtils.isNotBlank(dstPathSd)) {
                dsfFileKey2 = this.getDfsFileKey2(item.getTargetFile());
            }
            logger.debug("dsfFileKey2 --->" + dsfFileKey2);

            //采集备份路径是否为空，如果不为空则需要写入文件系统
            String dsfFileKeyBak = null;
            if (StringUtils.isNotBlank(dstPathBak)) {
                dsfFileKeyBak = this.getDfsFileKeyBak(item.getTargetFile());
            }
            logger.debug("dsfFileKeyBak --->" + dsfFileKeyBak);

            //获取远程文件
            try {
                //采集到本地目录
                if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {
                    get(item);
                } else if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_FAST_DFS.getFileStoreType())) {
                    //采集到FastDfs文件系统，文件系统支持重命名规则，文件名存储结构:/path/file_name
                    addFileToFastDFS(dfsFileKey, item);
                    step = 1;
                    logger.info("get fastDfsFileKey OK, fastDfsFileKey: " + dfsFileKey + ", devId: " + id);

                    //采集落文件系统path2
                    if (StringUtils.isNotBlank(dsfFileKey2)) {
                        addFileToFastDFS(dsfFileKey2, item);
                        step = 2;
                        logger.info("get fastDfsFileKey2 OK, fastDfsFileKey2: " + dsfFileKey2 + ", devId: " + id);
                    }

                    //采集落文件系统Bak目录
                    if (StringUtils.isNotBlank(dsfFileKeyBak)) {
                        addFileToFastDFS(dsfFileKeyBak, item);
                        step = 3;
                        logger.info("get fastDfsFileKeyBak OK, fastDfsFileKeyBak: " + dsfFileKeyBak + ", devId: " + id);
                    }
                } else {
                    //采集到分布式文件系统
                    //对分布式文件系统支持重命名规则,文件名存储结构/DEV_ID/path/file_name
                    addFileToDFS(dfsFileKey, item);
                    step = 1;
                    logger.info("get dfsFileKey OK, dfsFileKey: " + dfsFileKey + ", devId: " + id);

                    //采集落文件系统path2
                    if (StringUtils.isNotBlank(dsfFileKey2)) {
                        addFileToDFS(dsfFileKey2, item);
                        step = 2;
                        logger.info("get dsfFileKey2 OK, dsfFileKey2: " + dsfFileKey2 + ", devId: " + id);
                    }

                    //采集落文件系统Bak目录
                    if (StringUtils.isNotBlank(dsfFileKeyBak)) {
                        addFileToDFS(dsfFileKeyBak, item);
                        step = 3;
                        logger.info("get dsfFileKeyBak OK, dsfFileKeyBak: " + dsfFileKeyBak + ", devId: " + id);
                    }
                }
            } catch (DcmException e) {
                //处理过程失败要从分布式文件系统删除文件
                removeFileFromDFSChain(step, fileStoreType, dfsFileKey, dsfFileKey2, dsfFileKeyBak);
                throw e;
            } catch (Exception e) {
                //处理过程失败要从分布式文件系统删除文件
                removeFileFromDFSChain(step, fileStoreType, dfsFileKey, dsfFileKey2, dsfFileKeyBak);

                logger.error(e.getMessage(), e);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.GET_FILE_ERR,
                        "ftp get file exception!", ParamsConstant.LINK_TIPS_LEVEL_1, item.getSourceFile().getFileName());
            }
            logger.info("get remote file ok, devId: " + id);

            try {
                //设置目标文件长度
                item.getTargetFile().setFileLength(item.getSourceFile().getFileLength());

                //设置目标文件时间
                item.getTargetFile().setTime(DateUtil.getCurrentDate());

                //把下面的recordLog(item)方法里面的代码抽成postProcesssing(item);方法中的三个方法:
                //后续处理:1.记录日志,2.添加分发任务DC_DIST_TASK,3.添加SQL_jiekou,4.Redis对比去重需要将采集文件信息添加到Redis中
                addLateOperator(sourceId, item);
                logger.info("collect transfer file late handle ok, devId: " + id);
            } catch (Exception e) {
                logger.error("collect transfer file late handle fail, devId: " + id, e);

                this.removeFileFromDFSChain(3, fileStoreType, dfsFileKey, dsfFileKey2, dsfFileKeyBak);

                //后续处理是在事务中处理的，如果添加日志失败，而下次会再次采集，所以这里不需要删除已经采集的目标文件
                throw new DcmException(WarnManager.COLL_LATE_HANDLE_FAIL, DcmException.RECORD_LOG_ERR,
                        "collect late operate exception, exception cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, item.getSourceFile().getFileName());
            }

            try {
                //FTP源文件需要重命名
                if (item.oriNeedRename()) {
                    String remotePath = FileTool.exactPath(item.getSourceFile().getOriPathBak());
                    if (!trans.isExistPath(remotePath)) {
                        trans.mkdir(remotePath);
                    }

                    //重命名失败要记录告警表
                    boolean renameResult = trans.rename(FileTool.exactPath(item.getSourceFile().getFilePath()) + item.getSourceFile().getFileName(), remotePath + item.getOriFileRename());
                    if (!renameResult) {
                        WarnManager.tranWarn(String.valueOf(collLinkDto.getAddrId()), WarnManager.COLL_LATE_HANDLE_FAIL, this.id, item.getSourceFile().getFileName(), "ftp网元文件后续操作重命名失败");
                    }
                } else if (item.oriNeedDelete()) {
                    boolean deleteResult = trans.delete(FileTool.exactPath(item.getSourceFile().getFilePath()) + item.getSourceFile().getFileName());
                    if (!deleteResult) {
                        WarnManager.tranWarn(String.valueOf(collLinkDto.getAddrId()), WarnManager.COLL_LATE_HANDLE_FAIL, this.id, item.getSourceFile().getFileName(), "ftp网元文件后续操作删除失败");
                    }
                }
                logger.debug("collect transfer file delete or rename ok, devId: " + id);
            } catch (Exception e) {
                item.setErrorMsg("delete or rename file exception");
                logger.error("delete or rename file exception, devId: " + id, e);
                throw new DcmException(WarnManager.COLL_LATE_HANDLE_FAIL, DcmException.LATE_OPERATOR_ERR,
                        "delete or rename file exception, exception cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, item.getSourceFile().getFileName());
            }
            logger.info("transfer OK, devId: " + id);
        } catch (DcmException e) {
            throw e;
        } catch (Exception e) {
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR,
                    "transfer exception, exception cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, item.getSourceFile().getFileName());
        }
        logger.info("end transfer one file, devId: " + id);
    }

    /**
     * 将文件重文件系统删除
     *
     * @param step          当前文件存储位置
     * @param fileStoreType 文件存储方式
     * @param dfsFileKey
     * @param dsfFileKey2
     */
    private void removeFileFromDFSChain(int step, String fileStoreType, String dfsFileKey, String dsfFileKey2, String dsfFileKeyBak) {
        logger.debug("remove file from dfschain, step: " + step + ", fileStoreType: " + fileStoreType + ", dfsFileKey: "
                + dfsFileKey + ", dsfFileKey2: " + dsfFileKey2 + ", dsfFileKeyBak: " + dsfFileKeyBak + ", devId: " + id);
        //处理过程失败要从分布式文件系统删除文件
        if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_DFS.getFileStoreType())) {//后续操作失败要删除文件
            if (step == 1) {  //回退dfsFileKey
                removeFileFromDFS(dfsFileKey);
                renameOriFileFromDFS(dfsFileKey);
                logger.info("addFileToDFS exception, delete file from dfs, dfsFileKey:" + dfsFileKey + ", devId: " + id + ", step: " + step);
            } else if (step == 2) {   //回退dfsFileKey && dsfFileKey2
                removeFileFromDFS(dfsFileKey);
                renameOriFileFromDFS(dfsFileKey);
                logger.info("addFileToDFS exception, delete file from dfs, dfsFileKey:" + dfsFileKey + ", devId: " + id);
                //采集落文件系统path2
                if (StringUtils.isNotBlank(dsfFileKey2)) {
                    removeFileFromDFS(dsfFileKey2);
                    renameOriFileFromDFS(dsfFileKey2);
                    logger.info("addFileToDFS exception, delete file from dfs, dsfFileKey2:" + dsfFileKey2 + ", devId: " + id);
                }
            } else if (step == 3) {
                removeFileFromDFS(dfsFileKey);
                renameOriFileFromDFS(dfsFileKey);
                logger.info("delete file from dfs, dfsFileKey:" + dfsFileKey + ", devId: " + id);

                //采集落文件系统path2
                if (StringUtils.isNotBlank(dsfFileKey2)) {
                    removeFileFromDFS(dsfFileKey2);
                    renameOriFileFromDFS(dsfFileKey2);
                    logger.info("delete file from dfs, dsfFileKey2:" + dsfFileKey2 + ", devId: " + id);
                }

                //采集落文件系统Bak目录
                if (StringUtils.isNotBlank(dsfFileKeyBak)) {
                    removeFileFromDFS(dsfFileKeyBak);
                    renameOriFileFromDFS(dsfFileKeyBak);
                    logger.info("delete file from dfs, dsfFileKeyBak:" + dsfFileKeyBak + ", devId: " + id);
                }
            }
        } else if (StringUtils.equalsIgnoreCase(fileStoreType, FileStoreTypeEnum.FILE_STORE_FAST_DFS.getFileStoreType())) {//后续操作失败要删除文件
            if (step == 1) {  //回退dfsFileKey
                removeFileFromFastDFS(dfsFileKey);
                logger.info("addFileToFastDFS exception, delete file from fastDfs, dfsFileKey:" + dfsFileKey + ", devId: " + id + ", step: " + step);
            } else if (step == 2) {   //回退dfsFileKey && dsfFileKey2
                removeFileFromFastDFS(dfsFileKey);
                logger.info("addFileToFastDFS exception, delete file from fastDfs, dfsFileKey:" + dfsFileKey + ", devId: " + id);
                //采集落文件系统path2
                if (StringUtils.isNotBlank(dsfFileKey2)) {
                    removeFileFromFastDFS(dsfFileKey2);
                    logger.info("addFileToFastDFS exception, delete file from fastDfs, dsfFileKey2:" + dsfFileKey2 + ", devId: " + id);
                }
            } else if (step == 3) {
                removeFileFromFastDFS(dfsFileKey);
                logger.info("addFileToFastDFS exception, delete file from fastDfs, dfsFileKey:" + dfsFileKey + ", devId: " + id);

                //采集落文件系统path2
                if (StringUtils.isNotBlank(dsfFileKey2)) {
                    removeFileFromFastDFS(dsfFileKey2);
                    logger.info("addFileToFastDFS exception, delete file from fastDfs, dsfFileKey2:" + dsfFileKey2 + ", devId: " + id);
                }
                //采集落文件系统Bak目录
                if (StringUtils.isNotBlank(dsfFileKeyBak)) {
                    removeFileFromFastDFS(dsfFileKeyBak);
                    logger.info("addFileToFastDFS exception, delete file from fastDfs, dsfFileKeyBak:" + dsfFileKeyBak + ", devId: " + id);
                }
            }
        }
    }


    /**
     * 重FastDFS文件系统删除文件
     *
     * @param fastDfsFileKey
     */
    private void removeFileFromFastDFS(String fastDfsFileKey) {
        try {
            DCFileService fileService = (DCFileService) SpringContextUtil.getBean("dcFileService");
            if (fileService.isExistFile(fastDfsFileKey)) {
                fileService.deleteFile(fastDfsFileKey);
                logger.info("remove file from fastdfs success, fastDfsFileKey: " + fastDfsFileKey + ", devId: " + id);
            }
        } catch (Exception e) {
            logger.error("remove file from fastdfs fail,fastDfsFileKey: " + fastDfsFileKey + ", devId: " + id, e);
        }
    }

    /**
     * 从分布式文件系统删除文件
     *
     * @Title: removeFileFromDFS
     * @return: void
     * @author: tianjc
     * @date: 2017年2月17日 上午11:35:38
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private void removeFileFromDFS(String dfsFileKey) {
        try {
            DFSService dfsService = (DFSService) SpringContextUtil.getBean("dfsService");
            dfsService.delete(dfsFileKey);
        } catch (Exception e) {
            logger.error("remove file from fastdfs fail,dfsFileKey:" + dfsFileKey, e);
        }
    }

    /**
     * 删除文件需要恢复原始文件
     *
     * @param dfsFileKey
     */
    public void renameOriFileFromDFS(String dfsFileKey) {
        try {
            DFSService dfsService = (DFSService) SpringContextUtil.getBean("dfsService");
            dfsService.rename(dfsFileKey + START_DEAL_TIME, dfsFileKey);
        } catch (Exception e) {
            logger.error("remove file from fastdfs fail,dfsFileKey:" + dfsFileKey, e);
        }
    }

    /**
     * 后续处理
     * 1.记录日志
     * 2.添加分发任务DC_DIST_TASK
     * 3.添加SQL_jiekou
     *
     * @param sourceId
     * @param transItem
     * @throws Exception
     */
    private void addLateOperator(long sourceId, TransItem transItem) throws Exception {
        logger.debug("begin add collect late handle operator, devId: " + id);

        String dataSource = null;
        int step = 0;
        try {
            logger.info("collect late handle, get collect sourceId, sourceId: " + sourceId + ", devId: " + id);

            //第一步:添加到Redis中
            String comparisonService = PropertiesUtil.getValueByKey(ParamsConstant.COLL_FILTER_TYPE, ParamsConstant.COLL_FILTER_TYPE_SQL);
            if (comparisonService.equalsIgnoreCase(ParamsConstant.COLL_FILTER_TYPE_DCA)) {// 添加到Redis中
                FileComparison.addDistTaskToRedis(this.id, transItem.getSourceFile());
                logger.info("collect late handle, add redis ok, devId: " + id);
            }

            //第二步:记录日志
            addRecordLog(transItem, sourceId);
            logger.info("collect late handle, add collect log table ok, devId: " + id);
            step++;

            //第三步：添加分发任务:SQL->DC_DIST_TASK
            addDistTask(transItem, sourceId);
            logger.info("collect late handle, add dist task ok, devId: " + id);
            step++;

            //第四步:添加SQL_jiekou
            dataSource = addSqlJieKou(transItem, sourceId);
            logger.info("collect late handle, add sql_jiekou ok, devId: " + id);
            step++;
            logger.debug("add collect late handle operator success, devId: " + id);
        } catch (Exception e) {
            logger.error("collect late handle operator fail, devId: " + id, e);

            removeInvalidLog(sourceId, dataSource ,step);
            //事务回滚
            throw e;
        }
        logger.debug("end add collect late handle operator, devId: " + id);
    }

    /**
     * 根据sourceId删除记录
     *
     * @param sourceId
     * @param step
     */
    private void removeInvalidLog(long sourceId, String dataSource ,int step) {
        logger.info("记录采集日志失败，删除记录,source_id:" + sourceId);

        int size = 0;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("DEV_ID", id);
        paramMap.put("SOURCE_ID", sourceId);

        //删除采集日志记录
        if (step >= 0) {
            try {
                size = JdbcUtil.deleteObject("collectMapper.delInvalidCollLog", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("删除dc_coll_log记录,source_id:" + sourceId + ",size:" + size);
            } catch (Exception e) {
                logger.error("删除dc_coll_log记录失败,source_id:" + sourceId);
            }
        }

        //删除分发任务
        if (step >= 1) {
            try {
                size = JdbcUtil.deleteObject("collectMapper.delInvalidDistTask", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
                logger.info("删除dc_dist_task记录,source_id:" + sourceId + ",size:" + size);
            } catch (Exception e) {
                logger.error("删除dc_dist_task记录失败,source_id:" + sourceId);
            }
        }


        //删除source_files
        if (step >= 2) {
            try {
                size = JdbcUtil.deleteObject("collectMapper.delInvalidSourceFiles", paramMap,dataSource);
                logger.info("删除source_files记录,source_id:" + sourceId + ",size:" + size);
            } catch (Exception e) {
                logger.error("删除source_files记录失败,source_id:" + sourceId);
            }

            try {
                //删除task_manager表记录
                size = JdbcUtil.deleteObject("collectMapper.delInvalidTaskManager", paramMap, dataSource);
                logger.info("删除task_manager记录,source_id:" + sourceId + ",size:" + size);
            } catch (Exception e) {
                logger.error("删除task_manager记录失败,source_id:" + sourceId);
            }
        }
    }

    /**
     * 添加SQL_jiekou
     *
     * @param transItem
     * @param sourceId
     */
    @SuppressWarnings("unchecked")
    private String addSqlJieKou(TransItem transItem, long sourceId) {
        logger.debug("begin add sql_jiekou, sourceId: " + sourceId + ", fmtFlag: " + collLinkDto.getFmtFlag() + ", devId: " + id);

        //格式化分发
        if (ParamsConstant.NOFORMATTER.equals(collLinkDto.getFmtFlag())) {
            return null;
        }
        //数据源
        String dataSource = FrameConfigKey.DEFAULT_DATASOURCE;
        //添加SQL_jiekou
        if (!BlankUtil.isBlank(sqlJiekou)) {
            MsgFormat msgRule = new MsgFormat(sqlJiekou);

            //获取采集链路参数副本
            Hashtable<String, String> ruleParams = (Hashtable<String, String>) collLinkDto.getLinkParams().clone();

            //遍历链路参数并且为链路参数加上引号
            Iterator<String> iter = ruleParams.keySet().iterator();
            while (iter.hasNext()) {
                String params_key = iter.next();
                String params_value = ruleParams.get(params_key);
                ruleParams.put(params_key, "'" + params_value + "'");
            }

            String dfsFileKey = this.getDfsFileKey(transItem.getTargetFile());

            ruleParams.put("source_name", "'" + dfsFileKey + "'");
            ruleParams.put("source_id", "'" + sourceId + "'");
            ruleParams.put("JK_oper_type", "'" + StringTool.object2String(collLinkDto.getLinkParams().get("JK_oper_type")) + "'");
            ruleParams.put("JK_oper_list_id", "'" + StringTool.object2String(collLinkDto.getLinkParams().get("JK_oper_list_id")) + "'");
            ruleParams.put("JK_proc_list", "'" + StringTool.object2String(collLinkDto.getLinkParams().get("JK_proc_list")) + "'");
            ruleParams.put("JK_switch_id", "'" + StringTool.object2String(collLinkDto.getLinkParams().get("JK_switch_id")) + "'");
            ruleParams.put("JK_exchange_id", "'" + StringTool.object2String(collLinkDto.getLinkParams().get("JK_exchange_id")) + "'");
            ruleParams.put("JK_collect_id", "'" + StringTool.object2String(collLinkDto.getLinkParams().get("JK_collect_id")) + "'");
            ruleParams.put("JK_latn_id", "'" + StringTool.object2String(collLinkDto.getLinkParams().get("JK_latn_id")) + "'");
            ruleParams.put("file_name", "'" + transItem.getTargetFile().getFileName() + "'");
            ruleParams.put("file_path", "'" + transItem.getTargetFile().getFilePath() + "'");
            ruleParams.put("file_length", "'" + transItem.getTargetFile().getFileLength() + "'");

            //当前文件时间
            Date fileTime = transItem.getSourceFile().getTime();
            String fileTimeStr = "";
            if (fileTime != null) {
                fileTimeStr = DateUtil.format(fileTime, DateUtil.allPattern);
            }
            ruleParams.put("file_time", "'" + fileTimeStr + "'");
            ruleParams.put("link_id", "'" + id + "'");

            //离线采集接口采集之后要等待格式化触发任务
            if (StringUtils.equals(this.taskType, ParamsConstant.TASK_TYPE_OFFLINE_COLL)) {
                ruleParams.put("task_state", "7");
            } else {
                ruleParams.put("task_state", "0");
            }

            String jiekouSql = msgRule.format(ruleParams);

            dataSource = ObjectUtils.toString(collLinkDto.getLinkParams().get("datasource"));
            if (BlankUtil.isBlank(dataSource)) {
                dataSource = FrameConfigKey.DEFAULT_DATASOURCE;
            }
            logger.info("add sql_jiekou, jiekouSql:" + jiekouSql + ", devId: " + id + ", datasource: " + dataSource);

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
            logger.debug("add sql jiekou success, devId: " + id);
        }
        logger.debug("end add sql_jiekou, devId: " + id);
        return dataSource;
    }

    /**
     * 添加分发任务DC_DIST_TASK
     *
     * @param transItem
     * @param sourceId
     */
    private void addDistTask(TransItem transItem, long sourceId) {
        logger.debug("begin add dist task data, sourceId: " + sourceId + ", distribute Id: "
                + collLinkDto.getRltDistId() + ", fmt flag: " + collLinkDto.getFmtFlag() + ", devId: " + id);

        //添加分发任务DC_DIST_TASK
        if (!BlankUtil.isBlank(collLinkDto.getRltDistId())) {
            //不需要格式化文件，直接添加DC_dist_task表数据
            if (ParamsConstant.FORMATTER.equals(collLinkDto.getFmtFlag())) {
                String rltDistIds = collLinkDto.getRltDistId();
                String[] distIds = rltDistIds.trim().split("#");
                for (int i = 0; i < distIds.length; i++) {
                    if (!BlankUtil.isBlank(distIds[i].trim())) {
                        String distId = distIds[i].trim();
                        Map<String, Object> distParams = new HashMap<String, Object>();
                        distParams.put("SOURCE_ID", sourceId);
                        distParams.put("DIST_DEV_ID", Long.parseLong(distId));
                        distParams.put("ORI_PATH", transItem.getTargetFile().getFilePath());
                        distParams.put("ORI_FILE_NAME", transItem.getTargetFile().getFileName());
                        distParams.put("ORI_FILE_LENGTH", transItem.getTargetFile().getFileLength());
                        distParams.put("ORI_FILE_TIME", transItem.getTargetFile().time(DateUtil.allPattern));
                        distParams.put("LATN_ID", this.collLinkDto.linkParams.get("JK_latn_id"));
                        distParams.put("COLL_DEV_ID", id);
                        distParams.put("STATUS", ParamsConstant.PARAMS_0);
                        distParams.put("CREATE_TIME", DateUtil.getCurrent(DateUtil.allPattern));
                        JdbcUtil.insertObject("collectMapper.addDcDistTask", distParams, FrameConfigKey.DEFAULT_DATASOURCE);
                    }
                }
                logger.debug("add not format dist task data success, devId: " + id);
            } else if (ParamsConstant.FORMATTER_AND_DIST.equals(collLinkDto.getFmtFlag())) {   //文件需要格式化
                /**
                 * 采集链路FMT_FLAG=2
                 * 		1.分发链路PARENT_FLAG=0直接添加DC_DIST_TASK
                 * 		2.分发链路PARENT_FLAG=1调用SQL_jiekou,格式化后的子文件只会往PARENT_FLAG=1发送
                 * 		这样就能满足同时分发原始话单文件和格式化子文件的功能了
                 */
                String rltDistIds = collLinkDto.getRltDistId();
                String queryDistIds = rltDistIds.trim().replaceAll("#", ",");
                queryDistIds = StringUtils.removeStart(queryDistIds, ",");
                queryDistIds = StringUtils.removeEnd(queryDistIds, ",");

                Map<String, Object> queryLinkParams = new HashMap<String, Object>();
                queryLinkParams.put("DIST_IDS", queryDistIds);
                List<Map<String, Object>> linkParamsList = JdbcUtil.queryForList("collectMapper.queryDistLinkList", queryLinkParams, FrameConfigKey.DEFAULT_DATASOURCE);
                if (!BlankUtil.isBlank(linkParamsList)) {
                    for (int i = 0; i < linkParamsList.size(); i++) {
                        Map<String, Object> linkMap = linkParamsList.get(i);
                        String parentFlag = StringTool.object2String(linkMap.get("PARENT_FLAG"));
                        if (!BlankUtil.isBlank(parentFlag) && ParamsConstant.PARAMS_0.equals(parentFlag.trim())) {
                            String distId = StringTool.object2String(linkMap.get("DEV_ID"));

                            Map<String, Object> distParams = new HashMap<String, Object>();
                            distParams.put("SOURCE_ID", sourceId);
                            distParams.put("DIST_DEV_ID", Long.parseLong(distId));
                            distParams.put("ORI_PATH", transItem.getTargetFile().getFilePath());
                            distParams.put("ORI_FILE_NAME", transItem.getTargetFile().getFileName());
                            distParams.put("ORI_FILE_LENGTH", transItem.getTargetFile().getFileLength());
                            distParams.put("ORI_FILE_TIME", transItem.getTargetFile().time(DateUtil.allPattern));
                            distParams.put("LATN_ID", this.collLinkDto.linkParams.get("JK_latn_id"));
                            distParams.put("COLL_DEV_ID", id);
                            distParams.put("STATUS", ParamsConstant.PARAMS_0);
                            distParams.put("CREATE_TIME", DateUtil.getCurrent(DateUtil.allPattern));
                            JdbcUtil.insertObject("collectMapper.addDcDistTask", distParams, FrameConfigKey.DEFAULT_DATASOURCE);
                        }
                    }
                    logger.debug("add original dist task data success, devId: " + id);
                }
            }
        }
        logger.debug("end add dist task data,  devId: " + id);
    }

    /**
     * 记录日志
     *
     * @param transItem
     * @param sourceId
     */
    private void addRecordLog(TransItem transItem, long sourceId) {
        logger.debug("begin add collect log, sourceId: " + sourceId + ", devId: " + id);

        //获取当前文件采集途径(auto/hand)
        String method = this.taskType;

        //添加采集日志DC_COLL_LOG
        Map<String, Object> logParams = new HashMap<String, Object>();
        logParams.put("SOURCE_ID", sourceId);
        logParams.put("DEV_ID", id);
        logParams.put("TRANSFER_METHOD", method);
        logParams.put("ORI_PATH", transItem.getSourceFile().getFilePath());
        logParams.put("ORI_FILE_NAME", transItem.getSourceFile().getFileName());
        logParams.put("ORI_FILE_LENGTH", transItem.getSourceFile().getFileLength());
        logParams.put("ORI_FILE_TIME", transItem.getSourceFile().time(DateUtil.allPattern));
        logParams.put("DST_PATH", transItem.getTargetFile().getFilePath());
        logParams.put("DST_FILE_NAME", transItem.getTargetFile().getFileName());
        logParams.put("DST_FILE_LENGTH", transItem.getTargetFile().getFileLength());
        logParams.put("DST_FILE_TIME", transItem.getTargetFile().time(DateUtil.allPattern));
        logParams.put("AFTER_ACTION", transItem.getOriLateHandleMethod());
        /**
         * 这个没有记录，如果使用这种rename规则文件应该保存在/ORI_FILE_BAK_PATH/ORI_FILE_RENAME
         * ORI_FILE_BAK_PATH为空保存原始路径
         */
        logParams.put("ORI_FILE_BAK_PATH", transItem.getOriBakPath());
        logParams.put("ORI_FILE_RENAME", transItem.getOriFileRename());
        logParams.put("DEAL_TIME", DateUtil.getCurrent(DateUtil.dateFormatPattern));
        logParams.put("MONTHNO", TimeTool.getMonth());
        logParams.put("START_DEAL_TIME", START_DEAL_TIME);
        JdbcUtil.insertObject("collectMapper.addDcCollLog", logParams, FrameConfigKey.DEFAULT_DATASOURCE);
        logger.debug("end add collect log, devId: " + id);
    }

    /**
     * 修改链路运行状态
     *
     * @param devId     链路ID
     * @param tipsLevel 链路提示级别
     * @param runState  链路运行状态
     */
    private void updateCollLinkLevel(String devId, String tipsLevel, String runState, String linkError) {
        logger.debug("begin update collect link tips level, tipsLevel: " + tipsLevel + ", runState: " + runState + ", linkError: " + linkError + ", devId: " + id);
        Map<String, Object> updateParams = new HashMap<String, Object>();
        updateParams.put("DEV_ID", devId);
        updateParams.put("TIPS_LEVEL", BlankUtil.isBlank(StringTool.object2String(tipsLevel)) ? ParamsConstant.PARAMS_0 : StringTool.object2String(tipsLevel));
        updateParams.put("RUN_STATE", StringTool.object2String(runState));
        updateParams.put("LINK_ERR", StringTool.object2String(linkError));
        JdbcUtil.updateObject("collectMapper.updateCollLinkTipsLevel", updateParams, FrameConfigKey.DEFAULT_DATASOURCE);
        logger.debug("end update collect link tips level, devId: " + id);
    }

    /**
     * 获取SourceId
     *
     * @return
     */
    private long getSourceId() {
        logger.debug("begin get source id, devId: " + id);
        long sourceId = 0l;
        Map<String, Object> seqParams = new HashMap<String, Object>();
        seqParams.put("sequenceName", "SEQ_SOURCE_ID");
        Map<String, Object> sourceMap = JdbcUtil.queryForObject("collectMapper.querySequenceByName", seqParams, FrameConfigKey.DEFAULT_DATASOURCE);

        if (!BlankUtil.isBlank(sourceMap) && !sourceMap.isEmpty()) {
            sourceId = Long.parseLong(sourceMap.get("ID").toString());
        }
        logger.debug("end get source id, sourceId:" + sourceId + ", devId: " + id);
        return sourceId;
    }

    /**
     * 将远程文件采集到本地
     *
     * @param item
     * @throws Exception
     */
    private void get(TransItem item) throws DcmException, Exception {
        logger.info("begin get remote file, file Name:" + item.getSourceFile().getFileName()
                + ", start time:" + (DateUtil.getCurrent(DateUtil.allPattern)) + ", devId: " + id);
        String tmp = "." + DcmSystem.random(10000) + ".tmp";

        //远程目录+文件名
        String remotePath = "";
        //本地目录+文件名
        String localPath = "";
        //将远程目录文件下载到本地，本地文件名称保存为临时名称
        try {
            String localTempFileHidden = ObjectUtils.toString(collLinkDto.getLinkParams().get("local_temp_file_hidden"));
            remotePath = item.getSourceFile().getFilePath() + item.getSourceFile().getFileName();

            if (ParamsConstant.PARAMS_1.equals(localTempFileHidden)) {
                localPath = dstPath + "." + item.getTargetFile().getFileName() + tmp;
            } else {
                localPath = dstPath + item.getTargetFile().getFileName() + tmp;
            }

            logger.debug("start download file, remote file:" + remotePath + ", local file:" + localPath
                    + ", startTime:" + DateUtil.getCurrent(DateUtil.partPattern) + ", devId: " + id);
            long beginTimes = System.currentTimeMillis();
            trans.get(remotePath, localPath);
            long endTimes = System.currentTimeMillis();
            logger.debug("end download file, endTime:" + DateUtil.getCurrent(DateUtil.partPattern)
                    + ", total times:[ " + (endTimes - beginTimes) + " ]ms, devId: " + id);
        } catch (Exception e) {
            logger.error("download file fail, devId: " + id, e);
            //文件采集失败，删除本地临时文件
            if (FileTool.exists(localPath)) {
                local.delete(localPath);
                logger.debug("download file fail, delete local file ok, devId: " + id);
            }
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.GET_FILE_ERR,
                    "file download fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, item.getSourceFile().getFileName());
        }

        try {
            //文件采集成功，修改本地临时文件名称
            local.rename(localPath, dstPath + item.getTargetFile().getFileName());
            logger.info("rename local file ok, devId: " + id);
        } catch (Exception e) {
            logger.error("rename local file fail, devId: " + id, e);
            //文件采集失败，删除本地临时文件
            if (FileTool.exists(localPath)) {
                //本地临时文件重命名失败，删除本地临时文件
                local.delete(localPath);
                logger.debug("rename local file fail, delete local file ok, devId: " + id);
            }
            throw new DcmException(WarnManager.COLL_LATE_HANDLE_FAIL, DcmException.LATE_OPERATOR_ERR,
                    "rename file exception, exception cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1, item.getSourceFile().getFileName());
        }

        //本地备份目录文件下载(本地文件拷贝)
        if (!BlankUtil.isBlank(dstPathBak)) {
            //源文件名称(包含绝对路径)
            String sourceFile = dstPath + item.getTargetFile().getFileName();
            //目标文件名称(包含绝对路径)
            String targetBakFile = FileTool.exactPath(dstPathBak) + item.getTargetFile().getFileName();
            String targetFile = targetBakFile + tmp;
            //文件备份，直接进行本地文件拷贝
            FileTool.copyFile(sourceFile, targetFile);
            logger.debug("local file bak copy ok, devId: " + id);

            local.rename(targetFile, targetBakFile);
            logger.debug("local file bak ok, devId: " + id);
        }

        //本地备份目录2(本地文件拷贝)
        if (!BlankUtil.isBlank(dstPathSd)) {
            //源文件名称(包含绝对路径)
            String sourceFile = dstPath + item.getTargetFile().getFileName();
            //目标文件名称(包含绝对路径)
            String targetSecondFile = FileTool.exactPath(dstPathSd) + item.getTargetFile().getFileName();
            String targetFile = targetSecondFile + tmp;
            //文件备份，直接进行本地文件拷贝
            FileTool.copyFile(sourceFile, targetFile);
            logger.debug("local file second copy ok, devId: " + id);

            local.rename(targetFile, targetSecondFile);
            logger.debug("local file second bak ok, devId: " + id);
        }
        logger.info("end get remote file, file Name:" + item.getSourceFile().getFileName()
                + ", end time:" + (DateUtil.getCurrent(DateUtil.allPattern)) + ", devId: " + id);
    }

    /**
     * 两次文件对比，用来判断文件是否完整生成
     *
     * @param list
     * @return
     * @throws Exception
     */
    private Vector<FileRecord> getSecondAvaiableList(Vector<FileRecord> list) throws Exception {
        //根据文件路径、文件名称确认是否同一个文件，如果为同一个文件判断两个文件大小是否一样，如果一样在说明该文件生成完整，否则文件没有生成完整，不予采集
        int creatingCount;
        try {
            //第二次获取远程目录采集文件,用来排除正在生成的话单文件(正在生成的话单文件大小会变化)
            Thread.sleep(1000);
            //再次获取远程主机文件列表
            Vector<FileRecord> secondList = getList(getSubPathFlag());
            logger.debug("get second collect file list, file list size: " + ArrayUtil.getSize(secondList) + ", devId: " + id);

            //进行文件对比
            creatingCount = 0;
            for (int i = 0; i < list.size(); i++) {
                //第一次文件列表信息
                String fileName = list.get(i).getFileName();
                String filePath = list.get(i).getFilePath();
                Long fileLength = list.get(i).getFileLength();

                for (int j = 0; j < secondList.size(); j++) {
                    //第二次获取的文件列表信息
                    String secondFileName = secondList.get(j).getFileName();
                    String secondFilePath = secondList.get(j).getFilePath();
                    Long secondFileLength = secondList.get(j).getFileLength();
                    //判断是否为同一个文件(根据文件名称、文件路径就能唯一确定一个文件)
                    if (fileName.equals(secondFileName) && filePath.equals(secondFilePath)) {
                        //两个文件大小不一致，说明文件正在生成过程中，不予采集
                        if (fileLength.compareTo(secondFileLength) != 0) {
                            list.remove(i);
                            i--;
                            creatingCount++;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("get second avaiable list fail, devId: " + id, e);
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.GET_FILE_LIST_ERR,
                    "get comparison file list exception, exception cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
        }
        logger.info("get collect file list, creatingCount: " + creatingCount + ", devId: " + id);
        return list;
    }

    /**
     * 将文件采集到分布式文件系统
     *
     * @Title: addFileToDFS
     * @return: void
     * @author: tianjc
     * @date: 2017年6月6日 下午4:33:09
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    public void addFileToDFS(String dfsFileKey, TransItem item) throws DcmException, Exception {
        logger.info("start coll file to dfs, dfsFileKey:" + dfsFileKey + ", devId: " + id);

        String sourceFileName = item.getSourceFile().getFileName();
        String remotePath = FileTool.exactPath(item.getSourceFile().getFilePath()) + sourceFileName;

        //从FTP获取字节流写入分布式文件系统
        ByteArrayOutputStream bos = null;
        try {
            //对ftp文件流进行重试
            Exception error = null;
            int retryTimes = ParamsConstant.FTP_GET_PUT_TRY_COUNT;
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times get ftp stream, remotePath:" + remotePath + ",devId:" + this.id);
                if (i != 0) {
                    trans.reconnect();
                }

                try {
                    bos = trans.getFileStream(remotePath);

                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times get ftp stream fail,remotePath:" + remotePath + ",devId:" + this.id);
                    error = e;
                }
            }

            //重试获取文件流失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }


            //对分布式文件系统上传进行重试
            byte[] data = bos.toByteArray();
            DFSService dfsService = (DFSService) SpringContextUtil.getBean("dfsService");
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times up to dfs, dfsFileKey:" + dfsFileKey + ",devId:" + id);

                try {
                    dfsService.write(data, dfsFileKey, this.START_DEAL_TIME);

                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times up to dfs fail,dfsFileKey:" + dfsFileKey + ",devId:" + id);
                    error = e;

                    //处理过程失败要从分布式文件系统删除文件
                    removeFileFromDFS(dfsFileKey);
                    renameOriFileFromDFS(dfsFileKey);
                }
            }

            //重试上传失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }
        } catch (IOException e) {
            throw new RuntimeException("up to dfs fail,dfsFileKey:" + dfsFileKey, e);
        } catch (Exception e) {
            throw new RuntimeException("coll fail,dfsFileKey:" + dfsFileKey, e);
        } finally {
            IOUtils.close(bos);
        }
    }

    /**
     * 将文件采集到FastDFS文件系统
     *
     * @Title: addFileToDFS
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: void
     * @author: tianjc
     * @date: 2017年6月6日 下午4:33:09
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    public void addFileToFastDFS(String fastDfsFileKey, TransItem item) throws Exception {
        logger.info("start coll file to fastdfs, fastDfsFileKey:" + fastDfsFileKey + ", fileName: " + item.getSourceName() + ", devId: " + id);

        String sourceFileName = item.getSourceFile().getFileName();
        final String remotePath = FileTool.exactPath(item.getSourceFile().getFilePath()) + sourceFileName;

        //从FTP获取字节流写入分布式文件系统
        ByteArrayOutputStream bos = null;
        try {
            //对ftp文件流进行重试
            Exception error = null;
            int retryTimes = ParamsConstant.FTP_GET_PUT_TRY_COUNT;
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times get ftp stream,remotePath:" + remotePath + ",devId:" + this.id);
                if (i != 0) {
                    trans.reconnect();
                }
                try {
                    bos = trans.getFileStream(remotePath);
                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times get ftp stream fail,remotePath:" + remotePath + ",devId:" + this.id);
                    error = e;
                }
            }

            //重试获取文件流失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }

            //对分布式文件系统上传进行重试
            byte[] data = bos.toByteArray();
            DCFileService fileService = (DCFileService) SpringContextUtil.getBean("dcFileService");
            for (int i = 0; i < retryTimes; ++i) {
                logger.debug("[" + (i + 1) + "] times up to fastdfs, fastDfsFileKey:" + fastDfsFileKey + ",devId:" + id);

                try {
                    //上传之前先要判断路径是否存在
                    String dfsPath = StringUtils.substring(fastDfsFileKey, 0, fastDfsFileKey.lastIndexOf("/") + 1);
                    if (!fileService.isExistDir(dfsPath)) {
                        fileService.makeDir(dfsPath);
                    }

                    //开始上传文件
                    fileService.updateFile(fastDfsFileKey, data);

                    error = null;
                    break;
                } catch (Exception e) {
                    logger.warn("[" + (i + 1) + "] times up to fastdfs fail, fastDfsFileKey:" + fastDfsFileKey + ",devId:" + id);
                    logger.error("upload file error", e);
                    error = e;

                    //处理过程失败要从分布式文件系统删除文件
                    removeFileFromFastDFS(fastDfsFileKey);
                }
            }

            //重试上传失败，将异常往外抛
            if (!BlankUtil.isBlank(error)) {
                throw error;
            }
        } catch (DCFileException e) {
            throw new RuntimeException("up to fastdfs fail, fastDfsFileKey:" + fastDfsFileKey, e);
        } catch (Exception e) {
            throw new RuntimeException("coll fail, fastDfsFileKey:" + fastDfsFileKey, e);
        } finally {
            IOUtils.close(bos);
        }
    }

    /**
     * 获取自动采集有效文件列表
     *
     * @return
     * @throws Exception
     */
    private Vector<TransItem> getAvaiableList() throws DcmException, Exception {
        logger.debug("begin to get available list, devId: " + id);

        //获取远程目录采集文件(获取全部文件，对全部文件进行采集条件过滤，排序，删除不采集数量)
        Vector<FileRecord> list = getList(getSubPathFlag());
        logger.info("get first collect file list, list size: " + ArrayUtil.getSize(list) + ", devId: " + id);

        //判断文件是否需要开启两次采集文件大小对比，主要是用来过滤远程主机未完整生成的文件
        Boolean isFileComparSwitch = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_FILE_COMPARISON_SWITCH, Boolean.TRUE);
        logger.debug("get file comparison switch, isFileComparSwitch: " + isFileComparSwitch + ", devId: " + id);
        if (isFileComparSwitch) {
            list = getSecondAvaiableList(list);
            logger.debug("get list of files for comparison, file list size: " + ArrayUtil.getSize(list) + ", devId: " + id);
        }

        //对远程目录文件进行去重处理
        Vector<TransItem> effectList = getEffectFileList(list);
        logger.info("get effect collect file list, file list size: " + ArrayUtil.getSize(effectList) + ", devId: " + id);

        //对有效文件列表进行排序处理
        String sortKey = StringTool.object2String(collLinkDto.getLinkParams().get("sort_key"));
        logger.info("get sort key, sort key:" + sortKey + ", devId: " + id);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(effectList) && !BlankUtil.isBlank(sortKey)) {
            effectList = getSortEffectList(effectList, sortKey);
        }
        logger.debug("end to get available list, final file list size: " + ArrayUtil.getSize(effectList) + ", devId: " + id);
        return effectList;
    }


    /**
     * 对采集文件进行排序处理
     *
     * @param list    采集文件列表
     * @param sortKey 排序规则(file_time/file_name)
     * @return
     */
    private Vector<TransItem> getSortEffectList(Vector<TransItem> list, String sortKey) {
        logger.debug("begin get sort file list, list size:" + ArrayUtil.getSize(list) + ", sortKey:" + sortKey + ", devId: " + id);

        String field = sortKey;
        String sortType = "ASC";
        if (StringUtils.indexOf(sortKey, "#") > 0) {
            field = StringUtils.defaultString(sortKey.split("#")[0], "file_name");
            sortType = StringUtils.defaultString(sortKey.split("#")[1], "ASC");
        }
        logger.debug("sort rule, field: " + field + ", sortType: " + sortType + ", devId: " + id);
        Collections.sort(list, new Comparator<TransItem>() {
            private String field;
            private String sortType;

            public Comparator setComparator(String field, String sortType) {
                this.field = field;
                this.sortType = sortType;
                return this;
            }

            @Override
            public int compare(TransItem o1, TransItem o2) {
                if (StringUtils.equalsIgnoreCase(sortType, "ASC") && StringUtils.equalsIgnoreCase(field, "file_time")) {
                    return o1.getSourceFile().getTime().compareTo(o2.getSourceFile().getTime());
                } else if (StringUtils.equalsIgnoreCase(sortType, "DESC") && StringUtils.equalsIgnoreCase(field, "file_time")) {
                    return o2.getSourceFile().getTime().compareTo(o1.getSourceFile().getTime());
                } else if (StringUtils.equalsIgnoreCase(sortType, "ASC") && StringUtils.equalsIgnoreCase(field, "file_name")) {
                    return ObjectUtils.toString(o1.getSourceFile().getFileName(), "").compareTo(ObjectUtils.toString(o2.getSourceFile().getFileName(), ""));
                } else {
                    return ObjectUtils.toString(o2.getSourceFile().getFileName()).compareTo(ObjectUtils.toString(o1.getSourceFile().getFileName()));
                }
            }
        }.setComparator(field, sortType));
        logger.debug("end get sort file list, list size:" + ArrayUtil.getSize(list) + ", devId: " + id);
        return list;
    }

    /**
     * 对被剔重的文件进行判断，在源主机中是否存在，如果存在，判断是否开启了删除/重命名规则，如果已开启，则该文件采集时删除/重命令失败，需要在剔重后进行再次删除
     *
     * @param list     源文件主机列表
     * @param fileList 剔重后的采集文件列表
     */
    private void addOperatorOriFileList(Vector<FileRecord> list, List<FileRecord> fileList) {
        logger.info("check remote files and log data are synchronize, total list: " + ArrayUtil.getSize(list)
                + ", uncollect list: " + ArrayUtil.getSize(fileList) + ", devId: " + id);

        //判断源文件处理方式
        String oriLateHandleMethod = ObjectUtils.toString(collLinkDto.getLinkParams().get("ori_late_handle_method"));
        //获取已采集过的文件列表
        List<FileRecord> collectedList = (List<FileRecord>) ListUtils.removeAll(list, fileList);
        logger.info("oriLateHandleMethod: " + oriLateHandleMethod + ", collected list: " + ArrayUtil.getSize(collectedList) + ", devId: " + id);

        //源文件删除操作
        if (StringUtils.equals(oriLateHandleMethod, Filter.LATE_HANDLE_DELETE)) {
            try {
                //登录远程ftp/sftp
                trans.login();

                for (FileRecord fileRecord : collectedList) {
                    String oriFilePath = null;
                    try {
                        String filePath = StringUtils.removeEnd(fileRecord.getFilePath(), File.separator) + File.separator;
                        String fileName = fileRecord.getFileName();
                        oriFilePath = filePath + fileName;
                        boolean delOK = trans.delete(oriFilePath);
                        if (delOK) {
                            logger.warn("delete remote file success, filePath; " + oriFilePath + ", devId: " + id);
                        } else {
                            logger.warn("delete remote file fail, filePath: " + oriFilePath + ", devId: " + id);
                            WarnManager.tranWarn(String.valueOf(collLinkDto.getAddrId()), WarnManager.COLL_LATE_HANDLE_FAIL, this.id, fileRecord.getFileName(), "ftp网元文件操作删除失败");
                        }
                    } catch (Exception e) {
                        logger.error("delete remote file exception, filePath: " + oriFilePath + ", devId: " + id);
                        logger.error("", e);
                    }
                }
            } catch (Exception e) {
                logger.error("connect ftp exception, check source file has been delete, devId: " + id);
                logger.error("", e);
            } finally {
                if (trans != null) {
                    trans.close();
                }
            }
        } else if (StringUtils.equals(oriLateHandleMethod, Filter.LATE_HANDLE_RENAME)) {  //对源文件进行备份操作

            //检查备份目录是否存在，如果不存在则不能进行备份处理
            if (StringUtils.isBlank(oriPathBak)) {
                logger.warn("please check backup directory exist, paramName: remote_path_bak, devId: " + id);
                return;
            }

            try {
                //登录远程ftp/sftp
                trans.login();

                for (FileRecord fileRecord : collectedList) {
                    String oriFilePath = null;
                    String bakFilePath = null;
                    try {
                        //获取源文件备份目录
                        String oriBakPath = this.oriPathBak;
                        //获取源文件备份名称
                        String oriBakName = filter.getOriFileName(fileRecord);

                        if (!trans.isExistPath(oriBakPath)) {
                            trans.mkdir(oriBakPath);
                        }

                        //获取源文件
                        String filePath = StringUtils.removeEnd(fileRecord.getFilePath(), File.separator) + File.separator;
                        String fileName = fileRecord.getFileName();
                        oriFilePath = filePath + fileName;

                        //获取备份文件
                        String bakOriFilePath = StringUtils.removeEnd(oriBakPath, File.separator) + File.separator;
                        bakFilePath = bakOriFilePath + oriBakName;

                        //重命名失败要记录告警表
                        boolean renameOK = trans.rename(oriFilePath, bakFilePath);
                        if (renameOK) {
                            logger.warn("rename remote file success, oriFilePath: " + oriFilePath + ", bakFilePath: " + bakFilePath + ", devId: " + id);
                        } else {
                            logger.warn("rename remote file fail, oriFilePath: " + oriFilePath + ", bakFilePath: " + bakFilePath + ", devId: " + id);
                            WarnManager.tranWarn(String.valueOf(collLinkDto.getAddrId()), WarnManager.COLL_LATE_HANDLE_FAIL, this.id, fileRecord.getFileName(), "ftp网元文件操作重命名失败");
                        }
                    } catch (Exception e) {
                        logger.error("rename remote file exception, oriFilePath: " + oriFilePath + ", bakFilePath: " + bakFilePath + ", devId: " + id);
                        logger.error("", e);
                    }
                }
            } catch (Exception e) {
                logger.error("connect ftp exception, check source file has been rename, devId: " + id);
                logger.error("", e);
            } finally {
                if (trans != null) {
                    trans.close();
                }
            }
        }
        logger.debug("check remote files and log data are synchronize, devId: " + id);
    }

    /**
     * 获取有效的文件列表(注意事务管理)
     * TEMP_REMOTE_FILE_LIST表示一个临时表，创建语句为:create global temporary table TEMP_REMOTE_FILE_LIST
     *
     * @param list 文件列表
     * @return
     */
    private Vector<TransItem> getEffectFileList(Vector<FileRecord> list) throws DcmException {
        logger.debug("begin get collect effect file list, list size: " + ArrayUtil.getSize(list) + ", devId: " + id);
        //有效文件列表
        Vector<TransItem> retList = new Vector<TransItem>();

        if (!BlankUtil.isBlank(list)) {
            try {
                //文件名比对去重服务
                String checkCondition = StringTool.object2String(collLinkDto.getLinkParams().get("check_condition"));

                List<FileRecord> fileList = list;
                //文件是否进行剔重操作，默认进行剔重
                Boolean isFileFilterSwitch = PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_FILE_FILTER_SWITCH, Boolean.TRUE);
                logger.info("current file filter switch: " + isFileFilterSwitch + ", devId: " + id);
                if (isFileFilterSwitch) {
                    fileList = FileComparison.getEffectFileList(this.id, list, checkCondition);

                    //对已采集过的文件去远程主机判断是否已删除，如果未删除，将远程主机文件删除， SX:20190906提出
                    if (list.size() != fileList.size()) {
                        addOperatorOriFileList(list, fileList);
                    } else {
                        logger.info("all files need to be collecte, devId: " + id);
                    }
                } else {
                    logger.warn("current file not filter with SQL or Redis!!");
                }

                logger.debug("get collect effect file list, list size: " + ArrayUtil.getSize(fileList) + ", devId: " + id);
                if (!BlankUtil.isBlank(fileList)) {
                    Iterator<FileRecord> iterator = fileList.iterator();
                    while (iterator.hasNext()) {
                        FileRecord fileRecord = iterator.next();

                        if (BlankUtil.isBlank(fileRecord.getFileLength())) {
                            fileRecord.setFileLength(Long.parseLong(ParamsConstant.PARAMS_0));
                            logger.warn("current file length is null, fileName: " + fileRecord.getFileName() + ", devId: " + id);
                        }
                        //不让文件大小小于设置的最小文件大小
                        if (fileRecord.getFileLength() <= minFileSize) {
                            logger.debug("current file size lt min file length, fileName: " + fileRecord.getFileName() + ", devId: " + id);
                            iterator.remove();
                            continue;
                        }

                        //陕西采集现场过滤源目录为.tmp文件
                        if (StringUtils.endsWith(fileRecord.getFileName(), ".tmp")) {
                            logger.warn("current file is temporary and does not need to be collected, fileName: " + fileRecord.getFileName() + ", devId: " + id);
                            iterator.remove();
                            continue;
                        }

                        //文件类型
                        fileRecord.setFileType(FileRecord.FILE);

                        String oriBakPath = this.oriPathBak;
                        if (StringUtils.isBlank(oriBakPath)) {
                            oriBakPath = fileRecord.getFilePath();
                        } else {
                            String remotePath = ObjectUtils.toString(collLinkDto.getLinkParams().get("remote_path"));
                            String oriFilePath = fileRecord.getFilePath();
                            oriBakPath = StringUtils.replace(oriFilePath, remotePath, oriBakPath);
                        }
                        //文件备份目录
                        fileRecord.setOriPathBak(oriBakPath);

                        //包含源文件、目标文件信息对象
                        TransItem transItem = new TransItem(fileRecord);
                        //目标文件路径
                        transItem.getTargetFile().setFilePath(dstPath);
                        //目标文件名称
                        transItem.getTargetFile().setFileName(transItem.getSourceFile().getFileName());

                        //目标文件后续操作
                        transItem.setLateHandleMethod(filter.lateHandleMethod);
                        //“本地”文件是否需要重命令，如果需要重命名设置重命名文件名称
                        if (transItem.needRename()) {
                            transItem.getTargetFile().setFileName(filter.getDstFileName(transItem.getSourceFile()));
                        }

                        //FTP源文件后续操作
                        transItem.setOriLateHandleMethod(filter.oriLatehandleMethod);
                        if (transItem.oriNeedRename()) {
                            transItem.setOriFileRename(filter.getOriFileName(transItem.getSourceFile()));
                        }

                        //当前采集类型,后面记录采集操作日志用
                        //transItem.getParams().put(ParamsConstant.LINK_EXEC_METHOD, ParamsConstant.TASK_TYPE_AUTO_COLL);
                        retList.add(transItem);
                    }
                }
            } catch (Exception e) {
                logger.error("get effect file list fail, devId: " + id, e);
                //处理失败，回滚事务
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.GET_FILE_LIST_ERR,
                        "get effect file list exception, exception cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }
        }
        logger.debug("end get collect effect file list, final file list size:" + ArrayUtil.getSize(retList) + ", devId: " + id);
        return retList;
    }

    /**
     * 获取远程目录文件列表
     *
     * @param flag 是否需要采集子目录文件
     * @return
     */
    private Vector<FileRecord> getList(Boolean flag) throws DcmException, Exception {
        logger.debug("begin to get collect file list, flag: " + flag + ", devId: " + id);

        //采集文件列表
        Vector<FileRecord> rstList = new Vector<FileRecord>();

        //获取采集源文件目录
        String remotePath = StringTool.object2String(collLinkDto.getLinkParams().get("remote_path"));

        //1、需要采集子目录文件列表并且链路类型为Normal
        //2、采集当前目录下的文件列表(子目录文件不采集)
        if (flag && ParamsConstant.ST_NOR_LINK.equalsIgnoreCase(subType)) {
            rstList = getListCycle(remotePath);
        } else {
            rstList = getList(remotePath);
        }
        logger.debug("end to get collect file list, final file list size:" + ArrayUtil.getSize(rstList) + ", devId: " + id);
        return rstList;
    }


    /**
     * 获取最外层文件列表
     *
     * @param remotePath
     * @return
     */
    private Vector<FileRecord> getListCycle(String remotePath) throws DcmException, Exception {
        logger.debug("begin get collect file list by cycle, remotePath:" + remotePath + ", devId: " + id);
        Vector<FileRecord> rstRecords = new Vector<FileRecord>();
        try {
            try {
                trans.login();
            } catch (Exception e) {
                logger.error("connect ftp exception, devId: " + id, e);
                //记录异常日志
                throw new DcmException(WarnManager.CONN_FTP_ERROR, DcmException.FTP_CONNECT_ERR, "connect ftp exception.", ParamsConstant.LINK_TIPS_LEVEL_1);
            }
            logger.info("get collect file list by cycle, login ftp host ok, devId: " + id);

            //判断远程目录是否存在,如果不存在直接抛出异常
            boolean isExistPath = trans.isExistPath(remotePath);
            if (!isExistPath) {
                logger.debug("get collect file list by cycle, remote path not exists, devId: " + id);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.DIR_NOT_EXISTS_ERR, "remote path <" + remotePath + "> is not exists!", ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //获取采集文件列表
            Vector<FileRecord> fileRecords = trans.getFileList(remotePath, list_cdt);
            logger.info("get collect file list by cycle, get remote file list, list size: "
                    + ArrayUtil.getSize(fileRecords) + ", remote path:" + remotePath + ", devId: " + id);

            //trans_condition条件过滤,过滤没通过的文件直接移除
            if (!BlankUtil.isBlank(fileRecords)) {
                Iterator<FileRecord> iter = fileRecords.iterator();
                while (iter.hasNext()) {
                    FileRecord fileRecord = iter.next();
                    if (!filter.check(fileRecord) && !fileRecord.isDirectory()) {
                        logger.debug("get collect file list by cycle, filter file name:" + fileRecord.getFileName() + ", devId: " + id);
                        iter.remove();
                    }
                }
            }
            logger.info("get collect file list by cycle, get check after file list, list size:"
                    + ArrayUtil.getSize(fileRecords) + ", devId: " + id);

            //对目录进行过滤删除
            Iterator<FileRecord> iter = fileRecords.iterator();
            while (iter.hasNext()) {
                FileRecord fileRecord = iter.next();
                if (!fileRecord.isFile()) {
                    String subRemotePath = FileTool.exactPath(FileTool.exactPath(fileRecord.getFilePath()) + fileRecord.getFileName());
                    getAllFileRecords(rstRecords, subRemotePath);
                } else {
                    //rstRecords.add(fileRecord);
                }
            }
            logger.info("get collect file list by cycle, get all children file list, list size: "
                    + ArrayUtil.getSize(rstRecords) + ", devId: " + id);

            //不采集文件个数
            String unCollNum = StringTool.object2String(collLinkDto.getLinkParams().get("uncoll_num"));
            //不需要采集的文件
            int unCollCount = 0;
            try {
                unCollCount = Integer.parseInt(unCollNum);
            } catch (NumberFormatException e) {
                unCollCount = 0;
            }

            //对过滤后的文件进行升序排序
            Comparator<FileRecord> comparator = new FileRecordComparator();
            Collections.sort(fileRecords, comparator);
            logger.info("get collect file list by cycle, unCollCount: " + unCollCount + ", devId: " + id);

            //对最外存文件列表不采集个数进行过滤
            if (!BlankUtil.isBlank(fileRecords)) {
                //将外层目录中包含的目录移除
                for (int i = 0; i < fileRecords.size(); i++) {
                    if (fileRecords.get(i).isDirectory()) {
                        fileRecords.remove(fileRecords.get(i));
                        i--;
                    }
                }

                //移除不采集文件个数
                unCollCount = fileRecords.size() < unCollCount ? fileRecords.size() : unCollCount;
                for (int i = 0; i < unCollCount; i++) {
                    fileRecords.remove(fileRecords.size() - 1);
                }
            }
            logger.info("get collect file list by cycle, get uncollect filter after file size: " + ArrayUtil.getSize(fileRecords) + ", devId: " + id);

            //将过滤后的最外层文件添加到最终采集文件列表中,注意只添加文件类型，目录类型不需要添加
            if (!BlankUtil.isBlank(fileRecords)) {
                rstRecords.addAll(fileRecords);
            }

            logger.info("get collect file list by cycle, final file list size: "
                    + (rstRecords == null ? 0 : rstRecords.size()) + ", devId: " + id);
        } catch (DcmException e) {
            logger.error("get collect file list by cycle fail, devId: " + id + ", error code:" + e.getErrorCode() + ", exception info:" + e.getErrorMsg());
            throw e;
        } catch (Exception e) {
            logger.error("get collect file list by cycle fail, devId: " + id, e);
            throw e;
        } finally {
            trans.close();
        }
        logger.debug("end get collect file list by cycle, final file list size:" + ArrayUtil.getSize(rstRecords) + ", devId: " + id);
        return rstRecords;
    }

    /**
     * 遍历获取目录下所有的文件(包括子目录下的文件)
     *
     * @param rstRecords
     * @param subRemotePath
     * @throws Exception
     * @throws DcmException
     */
    private void getAllFileRecords(Vector<FileRecord> rstRecords, String subRemotePath) throws Exception, DcmException {
        logger.debug("begin get all collect file list, rstRecords size:" + ArrayUtil.getSize(rstRecords)
                + ", subRemotePath:" + subRemotePath + ", devId: " + id);

        //判断远程目录是否存在,如果不存在直接抛出异常
        boolean isExistPath = trans.isExistPath(subRemotePath);
        if (!isExistPath) {
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.DIR_NOT_EXISTS_ERR, "remote path <" + subRemotePath + "> is not exists", ParamsConstant.LINK_TIPS_LEVEL_1);
        }

        //获取采集文件列表
        Vector<FileRecord> fileRecords = trans.getFileList(subRemotePath, list_cdt);
        logger.info("get current path file list, file list size: "
                + ArrayUtil.getSize(fileRecords) + ", path " + subRemotePath + ", devId: " + id);

        //trans_condition条件过滤,过滤没通过的文件直接移除
        if (!BlankUtil.isBlank(fileRecords)) {
            Iterator<FileRecord> iter = fileRecords.iterator();
            while (iter.hasNext()) {
                FileRecord fileRecord = iter.next();
                if (!filter.check(fileRecord) && !fileRecord.isDirectory()) {
                    iter.remove();
                }
            }
        }

        //递归获取文件列表
        Iterator<FileRecord> iter = fileRecords.iterator();
        while (iter.hasNext()) {
            FileRecord fileRecord = iter.next();
            if (!fileRecord.isFile()) {
                subRemotePath = FileTool.exactPath(FileTool.exactPath(fileRecord.getFilePath()) + fileRecord.getFileName());
                getAllFileRecords(rstRecords, subRemotePath);
            } else {
                rstRecords.add(fileRecord);
            }
        }
        logger.debug("end get all collect file list, devId: " + id);
    }

    /**
     * 获取有效文件列表
     *
     * @return
     */
    private Vector<FileRecord> getList(String remotePath) throws DcmException, Exception {
        logger.debug("begin get collect file list, remotePath:" + remotePath + ", devId: " + id);
        Vector<FileRecord> fileRecords = null;
        if (ParamsConstant.ST_NOR_LINK.equalsIgnoreCase(subType)) {
            try {
                try {
                    //登录远程ftp/sftp
                    trans.login();
                } catch (Exception e) {
                    logger.error("connect ftp exception, devId: " + id, e);
                    throw new DcmException(WarnManager.CONN_FTP_ERROR, DcmException.FTP_CONNECT_ERR, "connect ftp exception, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
                }
                logger.info("get collect file list, login ftp host ok, devId: " + id);

                //判断远程目录是否存在,如果不存在直接抛出异常
                boolean isExistPath = trans.isExistPath(remotePath);
                if (!isExistPath) {
                    logger.debug("get file list, current remote path not exist, remotePath:" + remotePath + ", devId: " + id);
                    throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.DIR_NOT_EXISTS_ERR, "remote path <" + remotePath + "> is not exists!", ParamsConstant.LINK_TIPS_LEVEL_1);
                }

                //获取采集文件列表(远程全部)
                fileRecords = trans.getFileList(remotePath, list_cdt);
                logger.info("get collect file list, file list size: " + ArrayUtil.getSize(fileRecords)
                        + ", remotePath: " + remotePath + ", devId: " + id);


                //trans_condition条件过滤,过滤没通过的文件直接移除
                if (!BlankUtil.isBlank(fileRecords)) {
                    Iterator<FileRecord> iter = fileRecords.iterator();
                    while (iter.hasNext()) {
                        FileRecord fileRecord = iter.next();
                        if ((!filter.check(fileRecord) && !fileRecord.isDirectory()) || !fileRecord.isFile()) {
                            iter.remove();
                        }
                    }
                }
                logger.info("get collect file list, check after file list size: " + ArrayUtil.getSize(fileRecords) + ", devId: " + id);

                //不需要采集的文件
                String unCollNum = StringTool.object2String(collLinkDto.getLinkParams().get("uncoll_num"));
                int unCollCount = 0;
                try {
                    unCollCount = Integer.parseInt(unCollNum);
                } catch (NumberFormatException e) {
                    unCollCount = 0;
                }
                logger.info("get collect file list, uncollCount:" + unCollCount + ", devId: " + id);

                //对采集文件进行文件名称降序排列
                Comparator<FileRecord> comparator = new FileRecordComparator();
                Collections.sort(fileRecords, comparator);

                //不采集文件数量进行删除
                if (!BlankUtil.isBlank(fileRecords)) {
                    unCollCount = fileRecords.size() < unCollCount ? fileRecords.size() : unCollCount;
                    for (int i = 0; i < unCollCount; i++) {
                        fileRecords.remove(fileRecords.size() - 1);
                    }
                }
            } catch (DcmException e) {
                throw e;
            } catch (Exception e) {
                logger.error("get collect file list fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.GET_FILE_LIST_ERR,
                        "get file list fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            } finally {
                trans.close();
            }
        }
        logger.debug("end get collect file list, final file list size:" + ArrayUtil.getSize(fileRecords) + ", devId: " + id);
        return fileRecords;
    }

    /**
     * 是否子目录文件列表
     *
     * @return
     */
    private boolean getSubPathFlag() {
        logger.debug("begin get Sub directory flag, devId: " + id);
        boolean isGetSubPath = Boolean.FALSE;
        //子目录标记
        String subPathFlag = StringTool.object2String(collLinkDto.getLinkParams().get("sub_path_flag"));
        if (!BlankUtil.isBlank(subPathFlag)) {
            isGetSubPath = ParamsConstant.PARAMS_1.equals(subPathFlag) ? Boolean.TRUE : Boolean.FALSE;
        }
        logger.debug("end get Sub directory flag, flag:" + isGetSubPath + ", devId: " + id);
        return isGetSubPath;
    }

    /**
     * 设置链路参数
     *
     * @throws java.lang.Exception
     */
    private void init() throws Exception {
        logger.debug("begin init collect link, devId: " + id);

        //文件传输协议(FTP/SFTP)
        String protocolType = "";
        //ftp是否为主动模式
        String tranMode = "";
        try {
            //获取Ftp文件传输模式
            tranMode = StringTool.object2String(collLinkDto.getLinkParams().get("tran_mode"));
            //传输模式为PASV则设置为被动模式
            if (!BlankUtil.isBlank(tranMode) && ParamsConstant.LINK_FTP_TRAN_MODE_PASV.equalsIgnoreCase(tranMode.trim())) {
                tranMode = ParamsConstant.LINK_FTP_TRAN_MODE_PASV;
            } else {
                tranMode = ParamsConstant.LINK_FTP_TRAN_MODE_PORT;
            }

            //根据链路参数获取协议
            protocolType = StringTool.object2String(collLinkDto.getLinkParams().get("trans_protocol"));
            //如果链路参数为空，则为默认的协议(ftp)
            protocolType = BlankUtil.isBlank(protocolType) ? ParamsConstant.DEFAULT_PROTOCOL_FTP : protocolType;
        } catch (Exception e1) {
            //设置为默认的协议(ftp)
            protocolType = ParamsConstant.DEFAULT_PROTOCOL_FTP;

            //默认Ftp采集模式为被动模式
            tranMode = ParamsConstant.LINK_FTP_TRAN_MODE_PORT;
        }

        //Ftp/SFTP对象参数
        String ip = StringTool.object2String(collLinkDto.getLinkParams().get("ip"));
        String userName = StringTool.object2String(collLinkDto.getLinkParams().get("username"));
        String password = StringTool.object2String(collLinkDto.getLinkParams().get("password"));
        String port = StringTool.object2String(collLinkDto.getLinkParams().get("port"));
        String timeout = StringTool.object2String(collLinkDto.getLinkParams().get("time_out"));
        //如果链路参数中timeout为空,则设置为默认的(60000)
        if (BlankUtil.isBlank(timeout)) {
            timeout = ParamsConstant.FTP_CONN_TIMEOUT;
        }

        //FTP密码是否加密
        String ftp_password_encrypt = SystemProperty.getContextProperty("ftp_password_encrypt");
        if (StringUtils.isNotBlank(password) && StringUtils.equalsIgnoreCase(ftp_password_encrypt, "true")) {
            password = Encoder.decode(password);
        }

        //根据类型进行构造对应的协议对象
        if (ParamsConstant.DEFAULT_PROTOCOL_FTP.equalsIgnoreCase(protocolType)) {
            trans = new FtpTran(ip, Integer.parseInt(port), userName, password, Integer.parseInt(timeout), this.id);

            //获取当前链路文件传输模式
            Boolean isPasvMode = ParamsConstant.LINK_FTP_TRAN_MODE_PASV.equals(tranMode) ? Boolean.TRUE : Boolean.FALSE;
            trans.setPasvMode(isPasvMode);
        } else {
            trans = new SftpTran(ip, Integer.parseInt(port), userName, password, Integer.parseInt(timeout), this.id);
        }
        logger.info("init collect link, host protocol:" + protocolType + ", ip:" + ip + ", port:" + port + ", devId: " + id);

        //初始化文件过滤条件
        list_cdt = StringTool.object2String(collLinkDto.getLinkParams().get("list_cdt"));

        try {
            //采集文件最小大小
            String minFileSizeStr = StringTool.object2String(collLinkDto.getLinkParams().get("min_file_size"));
            if (!BlankUtil.isBlank(minFileSizeStr)) {
                minFileSize = Integer.parseInt(minFileSizeStr);
            }
        } catch (Exception e) {
            minFileSize = -1;
        }

        //计算采集目录
        parseRemotePath();

        //创建链路文件过滤对象
        filter = new Filter(collLinkDto.getLinkParams());
        //本地网标识
        String latnId = StringTool.object2String(collLinkDto.getLinkParams().get("JK_latn_id"));
        //当前月
        String curMonth = DateUtil.getCurrent(DateUtil.dateMonthPattern);
        //当前旬
        String tenDays = TimeTool.getTenDays();
        //当前日
        String curDay = DateUtil.getCurrent(DateUtil.datePattern);

        //创建采集目录
        mkDstPath(latnId, curMonth, tenDays, curDay);
        logger.info("init collect link, mkdst path ok, devId: " + id);

        //创建采集目录2
        dstPathSd = StringTool.object2String(collLinkDto.getLinkParams().get("local_path2"));
        logger.info("init collect link, local_path2: " + dstPathSd + ", devId: " + id);
        if (!BlankUtil.isBlank(dstPathSd)) {
            mkDstPathSd(latnId, curMonth, tenDays, curDay);
            logger.info("init collect link, mkdst path2 ok, devId: " + id);
        }

        //创建采集目标备份目录
        dstPathBak = StringTool.object2String(collLinkDto.getLinkParams().get("local_path_bak"));
        logger.info("init collect link, local_path_bak: " + dstPathBak + ", devId: " + id);
        if (!BlankUtil.isBlank(dstPathBak)) {
            mkDstPathBak(latnId, curMonth, tenDays, curDay);
            logger.info("init collect link, mkdst path bak ok, devId: " + id);
        }

        //设置源目录
        subType = collLinkDto.getSubType();
        if (ParamsConstant.ST_NOR_LINK.equalsIgnoreCase(subType)) {
            //设置采集源目录
            String remotePath = StringTool.object2String(collLinkDto.getLinkParams().get("remote_path"));
            collLinkDto.getLinkParams().put("remote_path", FileTool.exactPath(remotePath));

            String remotePathBak = StringTool.object2String(collLinkDto.getLinkParams().get("remote_path_bak"));
            if (!BlankUtil.isBlank(remotePathBak)) {
                if (remotePathBak.contains("#latn_id")) {
                    remotePathBak = remotePathBak.replaceAll("#latn_id", latnId);
                }
                if (remotePathBak.contains("#month")) {
                    remotePathBak = remotePathBak.replaceAll("#month", curMonth);
                }
                oriPathBak = FileTool.exactPath(remotePathBak);
            }
            logger.info("init collect link, remote file exact path ok, devId: " + id);
        } else if (ParamsConstant.ST_DBA_LINK.equalsIgnoreCase(subType)) {
            //获取sql_jiekou
        }
        //接口语句
        sqlJiekou = StringTool.object2String(collLinkDto.getLinkParams().get("SQL_jiekou"));

        //判断分发链路格式是否正确,正确的分发链路格式应该 100009#100010#100011
        String rltDistId = collLinkDto.getRltDistId();
        if (!BlankUtil.isBlank(rltDistId)) {
            Pattern pattern = Pattern.compile(new String("^[0-9#]*$"));
            Matcher match = pattern.matcher(rltDistId);
            if (!match.matches()) {
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.INIT_LINK_ERR, "dist link separator can only #!", ParamsConstant.LINK_TIPS_LEVEL_1);
            }
        }

        //初始化dca连接
        String comparisonService = PropertiesUtil.getValueByKey(ParamsConstant.COLL_FILTER_TYPE, ParamsConstant.COLL_FILTER_TYPE_SQL);
        if (comparisonService.equalsIgnoreCase(ParamsConstant.COLL_FILTER_TYPE_DCA)) {// 添加到Redis中
            String finalDevId = id + "_" + Thread.currentThread().getId();
            logger.info("init DCA connection, DevId: " + finalDevId);
            DcaUtil.init(finalDevId);
        }
        logger.debug("end init collect link, devId: " + id);
    }

    /**
     * 生成采集目标目录
     *
     * @param latnId
     * @param curMonth
     * @param tenDays
     * @param curDay
     */
    private void mkDstPath(String latnId, String curMonth, String tenDays, String curDay) throws Exception {
        logger.debug("begin mkDstPath, latnId: " + latnId + ", curMonth: " + curMonth
                + ", tenDays: " + tenDays + ", curDay: " + curDay + ", devId: " + id);
        //本地目录
        dstPath = StringTool.object2String(collLinkDto.getLinkParams().get("local_path"));
        //为目录添加分割符
        dstPath = FileTool.exactPath(dstPath);
        if (dstPath.contains("#latn_id")) {
            dstPath = dstPath.replaceAll("#latn_id", latnId);
        }
        if (dstPath.contains("#month")) {
            dstPath = dstPath.replaceAll("#month", curMonth);
        }
        dstPath = FileTool.exactPath(dstPath);
        //分月存放标识
        String collDstDirFlag = StringTool.object2String(collLinkDto.getLinkParams().get("coll_dst_dir_flag"));
        if (ParamsConstant.DIR_FLAG_MONTH.equals(collDstDirFlag)) {
            dstPath += curMonth;
        } else if (ParamsConstant.DIR_FLAG_TENDAYS.equals(collDstDirFlag)) {
            dstPath += tenDays;
        } else if (ParamsConstant.DIR_FLAG_DAY.equals(collDstDirFlag)) {
            dstPath += curDay;
        }
        dstPath = FileTool.exactPath(dstPath);

        if (StringUtils.equalsIgnoreCase(this.collLinkDto.getFileStoreType(), FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {//采集到本地目录
            //判断目录是否存在，如果不存在创建
            File dstFilePath = new File(dstPath);
            logger.debug("mkdirs, local_path: " + dstPath + ", devId: " + id);

            boolean mkResult = dstFilePath.mkdirs();
            if (!dstFilePath.exists()) {
                throw new java.lang.Exception("mkdirs fail,local_path: " + dstPath + ", mkResult: " + mkResult + ", devId: " + id);
            }
        }
        logger.debug("end mkDstPath, final local_path:" + dstPath + ", devId: " + id);
    }

    /**
     * 生成采集目标目录
     *
     * @param latnId
     * @param curMonth
     * @param tenDays
     * @param curDay
     */
    private void mkDstPathSd(String latnId, String curMonth, String tenDays, String curDay) throws Exception {
        logger.debug("begin method mkDstPathSd, latnId:" + latnId + ", curMonth:" + curMonth
                + ", tenDays:" + tenDays + ", curDay:" + curDay + ", devId: " + id);
        //本地目录
        dstPathSd = StringTool.object2String(collLinkDto.getLinkParams().get("local_path2"));
        dstPathSd = FileTool.exactPath(dstPathSd);
        if (dstPathSd.contains("#latn_id")) {
            dstPathSd = dstPathSd.replaceAll("#latn_id", latnId);
        }
        if (dstPathSd.contains("#month")) {
            dstPathSd = dstPathSd.replaceAll("#month", curMonth);
        }
        dstPathSd = FileTool.exactPath(dstPathSd);

        String collDstDirFlag = StringTool.object2String(collLinkDto.getLinkParams().get("coll_dst_dir_flag"));
        if (ParamsConstant.DIR_FLAG_MONTH.equals(collDstDirFlag)) {
            dstPathSd += curMonth;
        } else if (ParamsConstant.DIR_FLAG_TENDAYS.equals(collDstDirFlag)) {
            dstPathSd += tenDays;
        } else if (ParamsConstant.DIR_FLAG_DAY.equals(collDstDirFlag)) {
            dstPathSd += curDay;
        }
        dstPathSd = FileTool.exactPath(dstPathSd);
        //判断目录是否存在，如果不存在创建
        File dstSdFilePath = new File(dstPathSd);
        logger.debug("mkdirs local_path2: " + dstPathSd + ", devId: " + id);

        if (StringUtils.equalsIgnoreCase(this.collLinkDto.getFileStoreType(), FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {//采集到本地目录
            boolean mkResult = dstSdFilePath.mkdirs();
            if (!dstSdFilePath.exists()) {
                throw new java.lang.Exception("mkdirs fail,local_path2: " + dstSdFilePath + ",mkResult: " + mkResult + ",devId: " + id);
            }
        }
        logger.debug("end method mkDstPathSd, final local_path2:" + dstPathSd + ", devId: " + id);
    }

    /**
     * 采集到本地备份目录
     *
     * @param latnId
     * @param curMonth
     * @param tenDays
     * @param curDay
     */
    private void mkDstPathBak(String latnId, String curMonth, String tenDays, String curDay) throws Exception {
        logger.debug("begin method mkDstPathBak, latnId:" + latnId + ", curMonth:" + curMonth
                + ", tenDays:" + tenDays + ", curDay:" + curDay + ", devId: " + id);
        //本地目录备份目录
        dstPathBak = StringTool.object2String(collLinkDto.getLinkParams().get("local_path_bak"));
        dstPathBak = FileTool.exactPath(dstPathBak);
        if (dstPathBak.contains("#latn_id")) {
            dstPathBak = dstPathBak.replaceAll("#latn_id", latnId);
        }
        if (dstPathBak.contains("#month")) {
            dstPathBak = dstPathBak.replaceAll("#month", curMonth);
        }
        dstPathBak = FileTool.exactPath(dstPathBak);

        //本地备份目录子目录分月存放规则
        String collDstDirFlag = StringTool.object2String(collLinkDto.getLinkParams().get("coll_dst_dir_bak_flag"));
        if (ParamsConstant.DIR_FLAG_MONTH.equals(collDstDirFlag)) {
            dstPathBak += curMonth;
        } else if (ParamsConstant.DIR_FLAG_TENDAYS.equals(collDstDirFlag)) {
            dstPathBak += tenDays;
        } else if (ParamsConstant.DIR_FLAG_DAY.equals(collDstDirFlag)) {
            dstPathBak += curDay;
        }
        dstPathBak = FileTool.exactPath(dstPathBak);
        //判断目录是否存在，如果不存在创建
        File dstBakFilePath = new File(dstPathBak);
        logger.debug("mkdirs local_path_bak: " + dstPathBak + ", devId: " + id);

        if (StringUtils.equalsIgnoreCase(this.collLinkDto.getFileStoreType(), FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType())) {//采集到本地目录
            boolean mkResult = dstBakFilePath.mkdirs();
            if (!dstBakFilePath.exists()) {
                throw new java.lang.Exception("mkdirs fail,local_path_bak: " + dstBakFilePath + ",mkResult: " + mkResult + ",devId: " + id);
            }
        }
        logger.debug("end method mkDstPathBak, final local_path_bak:" + dstBakFilePath + ", devId: " + id);
    }

    /**
     * 链路属性、参数
     *
     * @return
     */
    public CollLinkDto getCollLinkDto() {
        return collLinkDto;
    }

    /**
     * 解析采集目录规则,例如路径中带#month,替换成本月
     *
     * @Title: parseRemotePath
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: String
     * @author: tianjc
     * @date: 2017年12月15日 下午6:43:54
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    private void parseRemotePath() {
        if (!CollectionUtils.isEmpty(collLinkDto.getLinkParams())) {
            //获取采集源文件目录
            String remotePath = StringTool.object2String(collLinkDto.getLinkParams().get("remote_path"));
            logger.debug("original remote_path:" + remotePath);
            if (remotePath.contains("#latn_id")) {
                String latnId = ObjectUtils.toString(collLinkDto.getLinkParams().get("JK_latn_id"));
                remotePath = remotePath.replaceAll("#latn_id", latnId);
            }
            if (remotePath.contains("#month")) {
                DateTime now = DateTime.now();
                remotePath = remotePath.replaceAll("#month", now.toString("yyyyMM"));
            }
            collLinkDto.getLinkParams().put("remote_path", remotePath);
            logger.debug("parse remote_path:" + remotePath);
        }
    }

    /**
     * 获取写入文件系统Key
     *
     * @param fileRecord
     * @return
     */
    private String getDfsFileKey(FileRecord fileRecord) {
        //String dfsFileKey = "/" + id + FileTool.exactPath(fileRecord.getFilePath()) + fileRecord.getFileName();
        String dfsFileKey = FileTool.exactPath(fileRecord.getFilePath()) + fileRecord.getFileName();
        logger.debug("文件key:" + dfsFileKey);

        return dfsFileKey;
    }

    /**
     * 获取写入文件系统Key2
     *
     * @param fileRecord
     * @return
     */
    private String getDfsFileKey2(FileRecord fileRecord) {
        //String dfsFileKey = "/" + id + FileTool.exactPath(dstPathSd) + fileRecord.getFileName();
        String dfsFileKey = FileTool.exactPath(dstPathSd) + fileRecord.getFileName();
        logger.debug("文件key2:" + dfsFileKey);
        return dfsFileKey;
    }

    /**
     * 获取写入文件系统备份Key
     *
     * @param fileRecord
     * @return
     */
    private String getDfsFileKeyBak(FileRecord fileRecord) {
        //String dfsFileKey = "/" + id + FileTool.exactPath(dstPathBak) + fileRecord.getFileName();
        String dfsFileKey = FileTool.exactPath(dstPathBak) + fileRecord.getFileName();
        logger.debug("文件keybak:" + dfsFileKey);
        return dfsFileKey;
    }

    /**
     * 离线采集：通过openapi调用，先获取文件列表然后进行采集
     */
    public OfflineCollectResp offlineCollect() {
        long startTimes = System.currentTimeMillis();
        logger.debug("************** begin offlineCollect transfer, startTimes:" + startTimes + ", devId: " + id);

        //判断链路是否缺失参数，如果参数缺失则不能采集
        if (checkParams()) {
            logger.debug("link missing parameter, offlineTransfer executed fail, devId: " + id);
            return null;
        }

        Exception exception = null;
        OfflineCollectResp resp = new OfflineCollectResp();
        Vector<TransItem> list = null;
        try {
            //根据链路参数进行路径等设置
            try {
                init();
                logger.info("offline transfer initialize ok, devId: " + id);
            } catch (Exception e) {
                logger.error("offline transfer init fail, devId: " + id, e);
                throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.INIT_LINK_ERR,
                        "link initialize fail, failure cause: " + e.getMessage(), ParamsConstant.LINK_TIPS_LEVEL_1);
            }

            //获取远程目录有效文件列表
            list = getAvaiableList();
            logger.info("offline transfer get avaiable file list, size: " + ArrayUtil.getSize(list) + ", devId: " + id);

            //添加采集告警时间,如果采集文件数量不为空，需要更新当前采集链路最新分发时间
            if (!BlankUtil.isBlank(list)) {
                //更新链路告警最新时间
                addWarnRefreshLink();

                //文件采集
                list = batchTransfer(list);
            }

            logger.info("offline transfer batch ok, devId: " + id);
        } catch (DcmException e) {
            exception = e;
            logger.error("offlineTransfer fail, error Code: " + e.getErrorCode() + ", error Cause: " + e.getErrorMsg()
                    + ", error level: " + e.getTipsLevel() + ", devId: " + id, e);
            //修改链路运行状态
            if (ParamsConstant.LINK_TIPS_LEVEL_2.equals(e.getTipsLevel())) {
                updateCollLinkLevel(id, e.getTipsLevel(), ParamsConstant.COLL_LINK_RUN_STATE_ERR, e.getErrorCode() + ":" + e.getErrorMsg());
            } else if (StringUtils.equals(this.taskType, ParamsConstant.TASK_TYPE_AUTO_COLL)) {//自动采集才会修改成轻微异常
                updateCollLinkLevel(id, e.getTipsLevel(), null, e.getErrorCode() + ":" + e.getErrorMsg());
            }
        } catch (Exception e) {
            exception = e;
            logger.error("offlineTransfer fail, devId: " + id, e);
            //添加链路告警信息
            WarnManager.tranWarn(StringTool.object2String(collLinkDto.getAddrId()), WarnManager.TRAN_WARN_COLL_FAIL, id, "", "autoTransfer error, failure cause: " + e.getMessage());
            //修改链路告警级别
            if (StringUtils.equals(this.taskType, ParamsConstant.TASK_TYPE_AUTO_COLL)) {//自动采集才会修改成轻微异常
                updateCollLinkLevel(id, ParamsConstant.LINK_TIPS_LEVEL_1, null, e.getMessage());
            }
        } finally {
            this.closeDCAConnection(id);
            //返回错误信息
            if (null != exception) {
                resp.setResultCode("500");
                resp.setResultMsg(exception.getMessage());
            } else {
                //拼装返回结果
                resp.setResultCode("0");
                resp.setResultMsg("Success");
                if (!CollectionUtils.isEmpty(list)) {
                    int index = 0;
                    for (TransItem item : list) {
                        //是否传输成功
                        if (item.isTransferReuslt()) {
                            OfflineCollectResp.FtpFile ftpFile = new OfflineCollectResp.FtpFile();
                            ftpFile.setFileId(index);
                            ftpFile.setFileName(item.getSourceFile().getFileName());
                            ftpFile.setFileSize(new Long(item.getSourceFile().getFileLength()).intValue());
                            ftpFile.setFileRecords(-1);

                            ++index;
                            resp.getGetFile().add(ftpFile);
                        }
                    }
                    resp.setFileCount(index);
                }
            }
        }
        long endTimes = System.currentTimeMillis();
        logger.debug("end offline transfer, endTimes: {" + endTimes + "}, this batch cost total times: {"
                + (endTimes - startTimes) + "} millisecond, collect total file number: " + ArrayUtil.getSize(list) + ", devId: " + id);


        return resp;
    }

}

/**
 * 采集文件排序,根据文件名称升序排列
 *
 * @author Yuanh
 */
class FileRecordComparator implements Comparator<FileRecord> {
    public int compare(FileRecord o1, FileRecord o2) {
        FileRecord e1 = (FileRecord) o1;
        FileRecord e2 = (FileRecord) o2;
        if (e1.getFileName().compareTo(e2.getFileName()) > 0) {
            return 1;
        } else if (e1.getFileName().compareTo(e2.getFileName()) < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
