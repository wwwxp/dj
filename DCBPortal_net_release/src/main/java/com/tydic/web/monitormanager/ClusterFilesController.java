package com.tydic.web.monitormanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.alibaba.jstorm.client.ConfigExtension;
import com.alibaba.jstorm.ui.model.Response;
import com.alibaba.jstorm.ui.utils.UIUtils;
import com.alibaba.jstorm.utils.FileAttribute;
import com.alibaba.jstorm.utils.JStormUtils;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;

/**
 * @author Jark (wuchong.wc@alibaba-inc.com)
 */
@Controller
@RequestMapping("/monitorManager/log/file")
public class ClusterFilesController extends BaseController{
    private static final Logger LOG = LoggerFactory.getLogger(ClusterFilesController.class);

    private static final String PROXY_URL = "http://%s:%s/logview?%s=%s&%s=%s";
    private List<FileAttribute> files = new ArrayList<FileAttribute>();
    private List<FileAttribute> dirs = new ArrayList<FileAttribute>();
    
    /**
     * 获取目录和文件信息
     * @param request
     * @return
     */
    @RequestMapping(value = "/fileLists", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String fileLists(HttpServletRequest request) {
    	Map<String,String> paramsMap=this.getParamsMap(request);
    	Map<String,Object> returnMap=new HashMap<String,Object>();
    	String cluster_name=paramsMap.get("clusterName");
    	String name=paramsMap.get("name");
    	String host=paramsMap.get("host");
    	
    	String dir=paramsMap.get("dir");
    	String port=paramsMap.get("port");
    	
    	try {
	        cluster_name = StringEscapeUtils.escapeHtml(cluster_name);
	        dirs.clear();
	        files.clear();
	        Map conf = UIUtils.readUiConfig();
	        if (StringUtils.isBlank(dir)) {
	            dir = ".";
	        }
	        if(StringUtils.isBlank(port)){
	        	if(name.equals("supervisor")){
		    		port=String.valueOf(UIUtils.getSupervisorPort(cluster_name));
		    	}else if(name.equals("nimbus")){
		    		port=String.valueOf(UIUtils.getNimbusPort(cluster_name));
		    	}
	        }
	        String[] path = dir.split("/");
	        returnMap.put("path", path);
	        int i_port;
	        if (StringUtils.isBlank(port)) {
	            i_port = ConfigExtension.getNimbusDeamonHttpserverPort(conf);
	        } else {
	            i_port = JStormUtils.parseInt(port);
	        }
	
	        //proxy request for files info
	        String summary = requestFiles(host, i_port, dir);
	        returnMap.put("summary", summary);
	        returnMap.put("files", files);
	        returnMap.put("dirs", dirs);
	
	        // status save
	        returnMap.put("clusterName", cluster_name);
	        returnMap.put("host", host);
	        returnMap.put("port", String.valueOf(i_port));
	        returnMap.put("parent", dir);
        
		} catch (Exception e) {
			 LOG.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
    	String a=JSON.Encode(returnMap);
		return  a;
    }

    
    private String requestFiles(String host, int port, String dir){
        if (dir.contains("/..") || dir.contains("../")){
            return "File Path can't contains <code>..</code> <br/>";
        }
        Response response = UIUtils.getFiles(host, port, dir);
        String summary = null;
        if(response.getStatus() > 0){
            if(response.getStatus() == 200){
                parseString(response.getData());
            }else{
                summary = "The directory <code>" + dir + "</code> isn't exist <br/> " + response.getData();
            }
        }else{
            summary = "Failed to get files <code>" + dir + "</code> <br/>" + response.getData();
        }
        return summary;
    }


    private void parseString(String input) {
        Map<String, Map> map = (Map<String, Map>) JStormUtils
                .from_json(input);
        for (Map jobj : map.values()) {
            FileAttribute attribute = FileAttribute.fromJSONObject(jobj);
            if (attribute != null) {

                if (JStormUtils.parseBoolean(attribute.getIsDir(), false) == true) {
                    dirs.add(attribute);
                } else {
                    files.add(attribute);
                }

            }

        }
    }
}
