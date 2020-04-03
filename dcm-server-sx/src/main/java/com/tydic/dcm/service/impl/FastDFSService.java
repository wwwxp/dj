package com.tydic.dcm.service.impl;

import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.dcfile.service.DCFileService;
import com.tydic.dcm.ftran.FileRecord;
import com.tydic.dcm.service.DFSService;
import com.tydic.dcm.util.exception.DcmException;
import com.tydic.dcm.util.tools.FileTool;
import com.tydic.dcm.warn.WarnManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: tianjc
 * Date: 2018-08-15
 * Time: 15:50
 */
public class FastDFSService {

    /**
     * log4j日志对象
     */
    private static Logger logger = Logger.getLogger(DFSService.class);

    /**
     * 列举分布式文件系统列表
     * @Title: handListDst
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @return: Vector<Object>
     * @author: tianjc
     * @date: 2017年7月18日 下午6:37:57
     * @editAuthor:
     * @editDate:
     * @editReason:
     */
    public static Vector<FileRecord> list(String devId,String dstPath) throws DcmException{
        logger.debug("begin get dfs dist file list, devId: " + devId + ",remotePath:"+ dstPath);

        //遍历目标文件列表，进行文件参数过滤校验
        Vector<FileRecord> rsList = new Vector<FileRecord>();;
        try {
            String remotePath = FileTool.exactPath(dstPath);
            DCFileService fileService = (DCFileService) SpringContextUtil.getBean("dcFileService");
            if(fileService == null){
                throw new RuntimeException("Not found fileService");
            }

            if(!fileService.isExistDir(remotePath)){
                logger.warn("指定目录不存在，采集文件后会自动添加");
                return null;
            }
            //分布式文件系统的文件列表
            List<String> remoteList = fileService.readDir(remotePath);
            if(CollectionUtils.isEmpty(remoteList)){
                logger.debug("FastDFS分布式文件系统目录为空,remotePath:" + remotePath);
                return null;
            } else {
                for(int i=0;i<remoteList.size();++i){
                    String fileName = remoteList.get(i);

                    String fileKey = FileTool.exactPath(remotePath) + fileName;
                    //重redis中获取文件详细信息
                    List<String> fileList = fileService.getRedisUtil().getCluster().lrange(fileKey, 0, -1);

                    //获取文件系统文件信息
                    FileRecord fileRecord = new FileRecord();
                    fileRecord.setFileName(fileName);
                    fileRecord.setFilePath(FileTool.exactPath(dstPath));
                    fileRecord.setFileType(FileRecord.FILE);
                    if (CollectionUtils.isNotEmpty(fileList)) {
                        //文件大小
                        String fileLength = fileList.get(3);
                        //文件创建时间
                        String fileCrtDate = fileList.get(4) + "000";
                        //文件修改时间
                        String fileUpdDate = fileList.get(5) + "000";
                        fileRecord.setFileLength(Long.valueOf(fileLength));
                        fileRecord.setTime(new Date(Long.valueOf(fileUpdDate)));
                    } else {
                        fileRecord.setFileLength(-1);
                        fileRecord.setTime(new Date());
                    }
                    rsList.add(fileRecord);
                }
            }
        } catch (Exception e) {
            logger.error("list dfs dst file list fail, devId: " + devId, e);
            throw new DcmException(WarnManager.TRAN_WARN_COLL_FAIL, DcmException.OTHER_ERR, "Failed to list the dfs local file list, failure cause: " + e.getMessage());
        }
        return rsList;
    }
}
