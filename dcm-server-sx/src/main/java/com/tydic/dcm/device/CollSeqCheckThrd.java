package com.tydic.dcm.device;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.dcm.dto.CollLinkDto;
import com.tydic.dcm.dto.WarnSeqDto;
import com.tydic.dcm.ftran.FileRecord;
import com.tydic.dcm.ftran.TransItem;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.tools.ArrayUtil;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.StringTool;
import com.tydic.dcm.warn.WarnManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

/**
 * 采集文件序列连续性校验
 *
 * @author Yuanh
 */
@Slf4j
public class CollSeqCheckThrd extends Thread {

    /**
     * 采集序列连续性队列
     */
    public static Hashtable<String, WarnSeqDto> collSeqFileHashTab = new Hashtable<String, WarnSeqDto>();

    /**
     * 采集文件列表队列信息
     */
    private List<Hashtable<String, WarnSeqDto>> seqCheckList = new ArrayList<Hashtable<String, WarnSeqDto>>();


    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        log.debug("start check file continuity...");
        while (true) {
            try {
                if (CollSeqCheckThrd.collSeqFileHashTab.isEmpty()) {
                    Thread.sleep(3000);
                } else {
                    synchronized (CollSeqCheckThrd.collSeqFileHashTab) {
                        //将采集链路采集文件列表等信息clone到另一个队列
                        Hashtable<String, WarnSeqDto> collSeqList = (Hashtable<String, WarnSeqDto>) CollSeqCheckThrd.collSeqFileHashTab.clone();
                        seqCheckList.add(collSeqList);
                        //清除队列
                        CollSeqCheckThrd.collSeqFileHashTab.clear();
                    }
                    //校验当前文件列表序列是否连续
                    checkFileSeqContinuity();
                }
            } catch (Exception e) {
                log.error("check file continuity fail.", e);
            }
        }
    }

    /**
     * 校验文件序列完整性
     */
    private void checkFileSeqContinuity() {
        log.debug("begin check file sequence continuity, seqCheckList size: " + ArrayUtil.getSize(seqCheckList));
        try {
            for (int i = 0; i < seqCheckList.size(); i++) {
                //获取单次采集文件列表信息
                Hashtable<String, WarnSeqDto> singleCollFileList = seqCheckList.get(i);

                Iterator<String> iter = singleCollFileList.keySet().iterator();
                while (iter.hasNext()) {
                    String devId = iter.next();
                    WarnSeqDto warnSeqDto = singleCollFileList.get(devId);

                    //根据链路ID获取采集链路信息
                    CollLinkDto collLinkDto = LinkDataRefreshThrd.getCollLinkAllInfo(devId);
                    if (collLinkDto == null) {
                        collLinkDto = warnSeqDto.getCollLinkDto();
                    }

                    //序列校验规则,如果校验规则为空则不需要校验链路采集文件连续性
                    String seqCheckRule = StringTool.object2String(collLinkDto.getLinkParams().get("sequence_check_rule"));

                    log.debug("current seqCheckRule:" + seqCheckRule + "DEV_ID:" + devId);
                    if (BlankUtil.isBlank(seqCheckRule)) {
                        continue;
                    }

                    String[] seqCheckRules = null;
                    if (seqCheckRule.indexOf(",") != -1) {
                        //分割校验规则,如果校验参数形式如: 5,  这样设置序列长度为99
                        seqCheckRules = seqCheckRule.split(",");

                        if (seqCheckRules.length == 1) {
                            seqCheckRules = new String[]{seqCheckRules[0], ParamsConstant.PARAMS_99};
                        }
                    } else {
                        seqCheckRules = new String[]{seqCheckRule, ParamsConstant.PARAMS_99};
                    }

                    if (!BlankUtil.isBlank(seqCheckRules) && BlankUtil.isBlank(seqCheckRules[1])) {
                        seqCheckRules[1] = ParamsConstant.PARAMS_99;
                    }

                    //检查规则是否有效
                    boolean isBadRule = false;
                    for (String rule : seqCheckRules) {
                        if (!NumberUtils.isNumber(rule)) {
                            log.debug("seqCheckRule:" + seqCheckRule + "DEV_ID:" + devId);
                            isBadRule = true;
                            break;
                        }
                    }
                    if (isBadRule) {
                        continue;
                    }

                    //采集文件列表,如果采集文件列表为空不需要校验文件连续性,直接返回
                    Vector<TransItem> fileList = warnSeqDto.getFileList();
                    if (fileList == null || fileList.size() == 0) {
                        continue;
                    }
                    //当前采集链路本地采集列表序列集合
                    List<Long> sequenceList = new ArrayList<Long>();
                    //根据文件序列将文件相关信息保存
                    Map<Long, TransItem> fileMap = new HashMap<Long, TransItem>();

                    //字符串起始位置，序列长度，终止位置
                    int startIndex = Integer.parseInt(seqCheckRules[0]);
                    int length = Integer.parseInt(seqCheckRules[1]);
                    int endPos = startIndex + length;

                    //获取本次采集文件列表所有序列   7,2 重第七位开始，长度为2位
                    for (int j = 0; j < fileList.size(); j++) {
                        //文件名称
                        String fileName = fileList.get(j).getSourceFile().getFileName();

                        String fileSeq = StringUtils.substring(fileName, startIndex - 1, endPos - 1);

                        //截取文件名称序列
                        if (!NumberUtils.isNumber(fileSeq)) {
                            log.warn("非数字序列:{},check rule:{},fileName:{}", fileSeq, ArrayUtils.toString(seqCheckRules), fileName);
                            continue;
                        }

                        //将文件名称序列添加到集合，用来对比
                        sequenceList.add(Long.parseLong(fileSeq));
                        fileMap.put(Long.parseLong(fileSeq), fileList.get(j));
                    }

                    //根据文件序列进行升序排列
                    Collections.sort(sequenceList);

                    //判断当前采集一批文件第一个文件是否和上一次最后一个文件序列连续
                    Long firstSeq = sequenceList.get(0);
                    //查询文件序列列表数据
                    Map<String, Object> queryParams = new HashMap<String, Object>();
                    queryParams.put("DEV_ID", devId);
                    queryParams.put("FIRST_SEQ", firstSeq);
                    queryParams.put("SUBSTR_START_POS", seqCheckRules[0]);
                    queryParams.put("SUBSTR_LEN", seqCheckRules[1]);
                    List<Map<String, Object>> fileSeqList = JdbcUtil.queryForList("collectMapper.queryFileSequenceList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);

                    Long lastSeq = null;
                    if (!BlankUtil.isBlank(fileSeqList)) {
                        String linkLastSeq = StringTool.object2String(fileSeqList.get(0).get("LAST_SEQ"));
                        lastSeq = NumberUtils.toLong(linkLastSeq);
                    }


                    //判断当前采集一批文件内部是否连续
                    for (int j = 0; j < sequenceList.size(); j++) {
                        Long nextSeq = sequenceList.get(j);

                        //判断本次采集文件是否连续
                        if (lastSeq != null && (lastSeq + 1) != nextSeq) {

                            //排除采集失败重新采集的文件，从数据库查询历史数据
                            queryParams.put("FIRST_SEQ", nextSeq);
                            fileSeqList = JdbcUtil.queryForList("collectMapper.queryFileSequenceList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
                            if (CollectionUtils.isNotEmpty(fileSeqList)) {
                                String linkLastSeq = StringTool.object2String(fileSeqList.get(0).get("LAST_SEQ"));
                                lastSeq = NumberUtils.toLong(linkLastSeq);

                                //从数据库查询到上一个序列
                                if ((lastSeq + 1) == nextSeq) {
                                    lastSeq = nextSeq;
                                    continue;
                                }
                            }

                            //文件在lastSeq位置不连续，需要添加告警日志
                            TransItem item = fileMap.get(nextSeq);
                            String fileName = item.getSourceFile().getFileName();
                            String content = "采集链路[" + devId + "]文件名[" + fileName + "]不连续";
                            WarnManager.tranWarn(String.valueOf(warnSeqDto.getCollLinkDto().getAddrId()), WarnManager.TRAN_WARN_CFSEQ_NNOR, devId, fileName, content);
                        }

                        lastSeq = nextSeq;
                    }
                }
            }
            seqCheckList.clear();
        } catch (Exception e) {
            log.error("add check file sequence continuity fail.", e);
        }
        log.debug("end check file sequence continuity");
    }

    public static void main(String[] args) {
        //加载配置文件
        new ClassPathXmlApplicationContext(new String[]{"conf/spring-config.xml"});

        CollSeqCheckThrd collSeqCheckThrd = new CollSeqCheckThrd();
        collSeqCheckThrd.start();

        String devId = "4";
        Vector<TransItem> fileList = new Vector<TransItem>();
        for (int i = 0; i < 3; ++i) {
            FileRecord fileRecord = new FileRecord();
            fileRecord.setFileName(ObjectUtils.toString(2 * i + 1));

            fileList.add(new TransItem(fileRecord));
        }

        WarnSeqDto warnSeqDto = new WarnSeqDto();
        warnSeqDto.setDevId(devId);
        warnSeqDto.setCollLinkDto(new CollLinkDto());
        warnSeqDto.setFileList(fileList);
        collSeqCheckThrd.collSeqFileHashTab.put(devId, warnSeqDto);

        try {
            Thread.sleep(60 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
