package com.tydic.web.jobtaskcfg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.annotations.JsonAdapter;
import com.tydic.bp.QuartzConstant;
import com.tydic.bp.QuartzManager;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.util.Constant;

import PluSoft.Utils.JSON;
import oracle.sql.CLOB;

@Controller
@RequestMapping(value = "/jobTaskCfg")
public class JobTaskCfgController extends BaseController {
	private static Logger log = LoggerFactory.getLogger(JobTaskCfgController.class);
	@Autowired
	CoreService coreService;

	@Autowired
	QuartzManager quartzManager;
	
	/**
	 * 立即执行归档任务
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "doJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateExecJob(HttpServletRequest request) {
		Map<String, Object> dsParamMap = this.getParamsMapByObject(request);
		log.debug("JobTaskCfgController,运行开始" + dsParamMap.toString());
		try {
			String archiveId = StringTool.object2String(dsParamMap.get("ARCHIVE_ID"));
			dsParamMap.put("ARCHIVE_ID", archiveId);
			Map<String, Object> jobMap = coreService.queryForObject2New("datasourceArchive.queryDSArchiveConfig",
					dsParamMap, this.getDbKey(request));
			if (BlankUtil.isBlank(jobMap)) {
				return JSON.Encode(ResponseObj.getResultMap(ResponseObj.WARN, "定时任务不存在"));

			}
			String busType = "2";
			String jobClass = "";
			if (Constant.BUS_TYPE_ONE_EXPAND_STRATEGY.equals(busType)) {
				jobClass = Constant.JOB_CLASS_EXPAND_STRATEGY;
			} else if (Constant.BUS_TYPE_TWO_DB_ARCH.equals(busType)) {
				jobClass = Constant.JOB_CLASS_DB_ARCH;
			} else {
				jobMap.put(QuartzConstant.JOB_CLASS, "");
			}
			Map<String, Object> busParamsMap = new HashMap<String, Object>();
			busParamsMap.put("BUS_ID", archiveId);
			jobMap.put(QuartzConstant.BUS_PARAMS, busParamsMap);
			String uuid = UUID.randomUUID().toString().replaceAll("-", "");
			quartzManager.addJobStartNow(uuid, jobClass, "@busParams@", busParamsMap);
			log.debug("JobTaskCfgController,添加立即执行任务成功");
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, uuid));
		} catch (Exception e) {
			log.error("JobTaskCfgController, 执行任务失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		
	}


	@RequestMapping(value = "execJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateExecJobNew(HttpServletRequest request) {
		log.debug("JobTaskCfgController,运行开始");
		try {

			List<Map<String, String>> paramList = this.getParamsList(request);
			if (paramList != null && !paramList.isEmpty()) {
				for (int i = 0; i < paramList.size(); i++) {
					Map<String, String> jobMap = coreService.queryForObject("jobtaskcfg.queryTaskList",
							paramList.get(i), this.getDbKey(request));
					quartzManager.addJobStartNow(jobMap.get("TASK_NAME"), jobMap.get("TASK_JOB_CLASS"), "data",
							jobMap.get("TASK_JOB_PARAMS"));
				}
			} else {
				throw new Exception("参数有问题，请检查");
			}

		} catch (Exception e) {
			log.error("LogLevelCfgController, 失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return null;
	}

	@RequestMapping(value = "timerJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateTimerJob(HttpServletRequest request) {
		log.debug("JobTaskCfgController,运行开始");
		try {

			List<Map<String, String>> paramList = this.getParamsList(request);
			if (paramList != null && !paramList.isEmpty()) {
				for (int i = 0; i < paramList.size(); i++) {
					Map<String, String> jobMap = coreService.queryForObject("jobtaskcfg.queryTaskList",
							paramList.get(i), this.getDbKey(request));
					// quartzManager.addJobStartNow(jobMap.get("TASK_NAME"),
					// jobMap.get("TASK_JOB_CLASS"),
					// "data",jobMap.get("TASK_JOB_PARAMS"));
					paramList.get(i).put("STATUS", "1");
					int type = Integer.parseInt(String.valueOf((Object) jobMap.get("TASK_TYPE")));
					if (2 == type) {
						quartzManager.addJob(jobMap.get("TASK_NAME"), jobMap.get("TASK_JOB_CLASS"),
								jobMap.get("TIME_MODE"), null, null, jobMap.get("START_TIME"), jobMap.get("END_TIME"));
					} else if (3 == type) {
						quartzManager.addJob(jobMap.get("TASK_NAME"), jobMap.get("TASK_JOB_CLASS"),
								jobMap.get("TIME_MODE"), null, null, jobMap.get("START_TIME"));
					}

				}
				coreService.updateObject("jobtaskcfg.updateTaskStatusById", paramList, this.getDbKey(request));
			} else {
				throw new Exception("参数有问题，请检查");
			}

		} catch (Exception e) {
			log.error("LogLevelCfgController, 失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return null;
	}

	@RequestMapping(value = "/jobStop", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updatejobStop(HttpServletRequest request) {
		log.debug("JobTaskCfgController,运行开始");
		try {

			List<Map<String, String>> paramList = this.getParamsList(request);
			if (paramList != null && !paramList.isEmpty()) {
				for (int i = 0; i < paramList.size(); i++) {
					Map<String, String> jobMap = coreService.queryForObject("jobtaskcfg.queryTaskList",
							paramList.get(i), this.getDbKey(request));
					// quartzManager.addJobStartNow(jobMap.get("TASK_NAME"),
					// jobMap.get("TASK_JOB_CLASS"),
					// "data",jobMap.get("TASK_JOB_PARAMS"));
					paramList.get(i).put("STATUS", "0");
					quartzManager.removeJob(jobMap.get("TASK_NAME"), QuartzConstant.JOB_GROUP_NAME);

				}
				coreService.updateObject("jobtaskcfg.updateTaskStatusById", paramList, this.getDbKey(request));
			} else {
				throw new Exception("参数有问题，请检查");
			}

		} catch (Exception e) {
			log.error("LogLevelCfgController, 失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return null;
	}
	
	

	@RequestMapping(value = "queryCronFiveFireTime", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryCronFiveFireTime(HttpServletRequest request) {
		log.debug("JobTaskCfgController,运行开始");
		List<String> resultData = new ArrayList<String>();
		try {

			Map<String, String> param = this.getParamsMap(request);
			log.debug("queryCronFiveFireTime参数为:" + param);
			resultData = quartzManager.getNextNFireTimes(5, param.get("cron"));
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < resultData.size(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("FIRE_TIME", resultData.get(i));
				list.add(map);
			}
			return JSON.Encode(list);
		} catch (Exception e) {
			log.error("LogLevelCfgController, 失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	@RequestMapping(value = "queryCmdList", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryCmdList(HttpServletRequest request) {
		log.debug("JobTaskCfgController,查询命令列表开始");
		List<String> resultData = new ArrayList<String>();
		String empId = null;
		Map empeeMap = null;
		try {
			if (request.getSession() != null && request.getSession().getAttribute("userMap") != null) {
				empeeMap = (Map) request.getSession().getAttribute("userMap");
				empId = String.valueOf(empeeMap.get("EMPEE_ID"));
			} else {
				return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "用户不存在"));
			}

			Map<String, String> param = this.getParamsMap(request);
			param.put("LOGIN_EMPEE_ID", empId);

			if (empId.equals("1")) {// 如果超级用户登录，给一个超级标识，查询权限用
				param.put("LOGIN_SUPER_ADMIN", "1");
			}
			log.debug("queryCronFiveFireTime参数为:" + param);
			List<HashMap<String, String>> list = coreService.queryForList("jobtaskcfg.queryCmdList", param,
					FrameConfigKey.DEFAULT_DATASOURCE);
			log.debug("JobTaskCfgController,查询命令列表结束");
			return JSON.Encode(list);
		} catch (Exception e) {
			log.error("JobTaskCfgController, 查询命令列表异常---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	/**
	 * 查询归档任务日志
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "queryArchiveLogList", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryArchiveLog(HttpServletRequest request) {
		log.debug("JobTaskCfgController,查询最新归档日志开始");
		Map<String, Object> params = this.getParamsMapByObject(request);
		try {
			if(BlankUtil.isBlank(params)){
				return JSON.Encode(new ArrayList<Map<String, String>>());
			}
			List<HashMap<String, Object>> list = coreService.queryForList2New("datasourceArchive.queryArchiveLogLastTime", params,
					FrameConfigKey.DEFAULT_DATASOURCE);
			log.debug("JobTaskCfgController,查询最新归档日志结束");
			return JSON.Encode(list);
		} catch (Exception e) {
			log.error("JobTaskCfgController,查询最新归档日志异常---> ", e);
			return JSON.Encode(new ArrayList<Map<String, String>>());
		}
	}
	
}
