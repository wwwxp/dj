package com.tydic.dcm.openapi.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线采集响应结果
 * Description:
 * User: tianjc
 * Date: 2018-10-10
 * Time: 14:17
 */
@Data
public class OfflineCollectResp {

    //结果码
    private String resultCode = null;

    //结果信息
    private String resultMsg = "";

    //采集成功文件数
    private int fileCount;

    //采集成功文件列表
    private List<FtpFile> getFile = new ArrayList<>();

    @Data
    public static class FtpFile {

        //文件id
        private int fileId;

        //文件名
        private String fileName;

        //文件大小
        private int fileSize;

        //文件记录数,获取不到所以现在填写-1
        private int fileRecords;
    }
}
