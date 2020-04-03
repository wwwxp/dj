package com.tydic.service.versiondeployment.service;

import com.tydic.service.versiondeployment.bean.UploadCfgDto;
import com.tydic.util.ShellUtils;
import org.springframework.web.multipart.MultipartFile;

public interface UploadVersionService {

    /**
     * 版本上传
     * sfp/ftp  上传到具体主机
     * @param uFile
     * @param uploadCfgDto
     * @return
     */
     public boolean insertFileUpload(MultipartFile uFile, UploadCfgDto uploadCfgDto) throws Exception;

}
