package com.tydic.service.versiondeployment.service;

import com.tydic.util.ftp.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface VersionOptService {

    public List<HashMap<String, String>> queryNoteTypeConfig(Map<String, String> paramMap, String dbkey);

    public String updateVersionPkg(MultipartFile uFile, HttpServletRequest request, HttpServletResponse response, Map<String, String> paramMap) throws Exception;

    /**
     * 版本补丁
     * @param uFile
     * @param request
     * @param response
     * @param paramMap
     * @return
     * @throws Exception
     */
    public String updateVersionPatchPkg(MultipartFile uFile, HttpServletRequest request, HttpServletResponse response, Map<String, String> paramMap) throws Exception;



    public String deleteVersion(Map<String, String> paramMap, String dbKey) ;

    public Map<String,Object> getRemoteFileTree(Map<String,Object> params,String dbKey) throws Exception;
}
