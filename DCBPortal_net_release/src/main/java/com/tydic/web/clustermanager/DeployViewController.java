package com.tydic.web.clustermanager;

import PluSoft.Utils.JSON;
import com.tydic.bean.DeployViewDTO;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.clustermanager.DeployViewService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/deployView")
public class DeployViewController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DeployViewController.class);

	/**
	 * 部署图操作Service对象
	 */
	@Autowired
	private DeployViewService deployViewService;

	/**
	 * 获取部署图数据
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/getDeployViewData",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getDeployViewData(HttpServletRequest request) {
        log.debug("获取部署图数据开始...");
		List<DeployViewDTO> retList = null;
        try {
        	retList = deployViewService.queryDeployViewList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.error("获取部署图数据异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
		log.debug("获取部署图数据结束");
		return JSON.Encode(retList);
    }
}
