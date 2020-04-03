package com.tydic.web.jobtaskcfg;

import PluSoft.Utils.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.tydic.bp.QuartzConstant;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.service.QuartzService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/jobApi")
public class JobTaskApiController extends BaseController {
	private static Logger log = LoggerFactory.getLogger(JobTaskApiController.class);
	@Autowired
	CoreService coreService;

	@Autowired
	QuartzService quartzService;

	/**
	 * 查询job列表，并检查所有在执行中的job健康状态
	 */
	@RequestMapping(value = "queryJobsAndCheck", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryJobsAndCheck(HttpServletRequest request) {
		String dbKey = getDbKey(request);
        int pageSize = getPageSize(request);
        int pageIndex = getPageIndex(request);
        Map<String, Object> jobList = this.coreService.queryPageList2New("joblist.queryJobList",pageSize,pageIndex,getParamsMapByObject(request),dbKey);
        check(jobList);
        //更新之后再查一次
		jobList = this.coreService.queryPageList2New("joblist.queryJobList",pageSize,pageIndex,getParamsMapByObject(request),dbKey);

		return JSONObject.toJSONString(jobList);
	}

    /**
     * job 健康检查
     * @param jobList 分页数据对象 page 类型
     */
	private void check(Map<String, Object> jobList) {
        List<HashMap<String, String>> allJobs = (List<HashMap<String, String>>) jobList.get(FrameParamsDefKey.DATA);
        Map<String,Object> jobParamsMap = new HashMap<>();
        jobParamsMap.put("busParamsKey",QuartzConstant.BUS_PARAMS);
        JSONObject busParamObject = new JSONObject();
        jobParamsMap.put("busParamObject",busParamObject);
        List<Object> executingJobs = quartzService.queryExcutingJobs(jobParamsMap, null, null);
        List<String> toUpdateStatusIds = allJobs.stream()
                .filter((record) -> "1".equals(record.get("EXEC_STATUS"))) //数据库中为执行中状态的job id
                .map((record) -> record.get("ID"))
                .filter((id) -> !executingJobs.contains(id))                            //过滤掉服务中含有的job id
                .collect(Collectors.toList());
        updateExceptionQuitJob(toUpdateStatusIds);
    }

    /**
     * 更新异常退出的job
     */
	private void updateExceptionQuitJob(List<String> ids) {
	    for (String id : ids) {
            //执行状态：0：初始 1：执行中。2： 运行完成 3异常退出
            String execKey = "joblist.updateExecStatusById";
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("EXEC_STATUS", "3"); //更新执行状态为3
            params.put("ID", id);
            coreService.updateObject2New(execKey, params, null);
        }
    }

	@RequestMapping(value = "delJobRecord", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String deleteJobTaskRecord(HttpServletRequest request) {
        List<Map<String, String>> paramsList = getParamsList(request);
        String dbKey = getDbKey(request);
        String execKey = getExecKey(request);
        coreService.deleteBatchObject(execKey, paramsList, dbKey);
		return null;
	}

	@RequestMapping(value = "execJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateExecJobNew(HttpServletRequest request) {
		log.debug("JobTaskApiController,运行开始");
		Map<String,Object> params = this.getParamsMapByObject(request);
		try {

			Map<String,Object> jobMap = this.coreService.queryForObject2New("joblist.queryJobList",params,this.getDbKey(request));
			if(jobMap !=null && !jobMap.isEmpty()){
				String jobId = StringTool.object2String(jobMap.get("ID"));
				String jobClass = StringTool.object2String(jobMap.get("TASK_JOB_CLASS"));
				String jobParams = StringTool.object2String(jobMap.get("TASK_JOB_PARAMS"));
//				String taskType = StringTool.object2String(jobMap.get("TASK_TYPE"));

				Map<String,Object> jobParamsMap = new HashMap<String,Object>();
				jobParamsMap.put("jobName",jobId);
				jobParamsMap.put("busParamsKey",QuartzConstant.BUS_PARAMS);
				jobParamsMap.put("jobClass",jobClass);
				JSONObject busParamObject = JSONObject.parseObject(jobParams);
				busParamObject.put("TASK_NAME",params.get("TASK_NAME"));
				busParamObject.put("TASK_TYPE_DESC",params.get("TASK_TYPE_DESC"));
				busParamObject.put("jobId",params.get("ID"));// *zj* 作为业务参数传递过去的 配置的uuid
//				busParamObject.put("taskType",taskType);
				jobParamsMap.put("busParamObject",busParamObject);
				try {
					quartzService.addJobStartNow(jobParamsMap, null, null);
					Map<String,Object> jobDbMap = new HashMap<String,Object>();
					jobDbMap.put("ID",params.get("ID"));
					jobDbMap.put("EXEC_STATUS","2");
					coreService.updateObject2New("joblist.updateExecStatusById",jobDbMap,this.getDbKey(request));

				}catch(Exception e){
					log.error("调用即时任务接口报错",e);
					throw new Exception("调用即时任务接口报错,请检查定时任务（quartz）服务，是否启动！");
				}

			}else {
				throw new Exception("未查询到任务，请检查！");
			}


		} catch (Exception e) {
			log.error("JobTaskApiController, 失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return null;
	}

	@RequestMapping(value = "timerJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateTimerJob(HttpServletRequest request) {

		log.debug("JobTaskApiController,运行开始");
		Map<String,Object> params = this.getParamsMapByObject(request);
		try {

			Map<String,Object> jobMap = this.coreService.queryForObject2New("joblist.queryJobList",params,this.getDbKey(request));
			if(jobMap !=null && !jobMap.isEmpty()){
				String jobId = StringTool.object2String(jobMap.get("ID"));
				String jobClass = StringTool.object2String(jobMap.get("TASK_JOB_CLASS"));
				String jobParams = StringTool.object2String(jobMap.get("TASK_JOB_PARAMS"));
				String jobTime = StringTool.object2String(jobMap.get("CRON_EXP"));
                String startTime = StringTool.object2String(jobMap.get("CRON_START_TIME"));
                String endTime = StringTool.object2String(jobMap.get("CRON_END_TIME"));
                String taskType = StringTool.object2String(jobMap.get("TASK_TYPE"));
				Map<String,Object> jobParamsMap = new HashMap<String,Object>();
				jobParamsMap.put("jobName",jobId);
				jobParamsMap.put("busParamsKey",QuartzConstant.BUS_PARAMS);
				jobParamsMap.put("jobClass",jobClass);
                jobParamsMap.put("startTime",startTime);
                jobParamsMap.put("endTime",endTime);

				JSONObject busParamObject = JSONObject.parseObject(jobParams);
				busParamObject.put("TASK_NAME",params.get("TASK_NAME"));
				busParamObject.put("TASK_TYPE_DESC",params.get("TASK_TYPE_DESC"));
                busParamObject.put("jobId", params.get("ID"));
                busParamObject.put("taskType", taskType);
				jobParamsMap.put("busParamObject",busParamObject);
				jobParamsMap.put("time",jobTime);

				try {
					quartzService.addJob(jobParamsMap, null, null);
					Map<String,Object> jobDbMap = new HashMap<String,Object>();
					jobDbMap.put("ID",params.get("ID"));
					jobDbMap.put("EXEC_STATUS","1");
					coreService.updateObject2New("joblist.updateExecStatusById",jobDbMap,this.getDbKey(request));

				}catch(Exception e){
					log.error("调用定时任务接口报错",e);
					throw new Exception("调用定时任务接口报错,请检查定时任务（quartz）服务，是否启动！");
				}

			}else {
				throw new Exception("未查询到任务，请检查！");
			}


		} catch (Exception e) {
			log.error("JobTaskApiController, 失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return null;
	}

	@RequestMapping(value = "loopJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateLoopJob(HttpServletRequest request) {

		log.debug("JobTaskApiController,运行开始");
		Map<String,Object> params = this.getParamsMapByObject(request);
		try {

			Map<String,Object> jobMap = this.coreService.queryForObject2New("joblist.queryJobList",params,this.getDbKey(request));
			if(jobMap !=null && !jobMap.isEmpty()){
				String jobId = StringTool.object2String(jobMap.get("ID"));
				String jobClass = StringTool.object2String(jobMap.get("TASK_JOB_CLASS"));
				String jobParams = StringTool.object2String(jobMap.get("TASK_JOB_PARAMS"));
				String jobTime = StringTool.object2String(jobMap.get("CRON_EXP")); // *zj* time 是 int 类型 表间隔时间
				String startTime = StringTool.object2String(jobMap.get("CRON_START_TIME"));
				String endTime = StringTool.object2String(jobMap.get("CRON_END_TIME"));
                String taskType = StringTool.object2String(jobMap.get("TASK_TYPE"));
				Map<String,Object> jobParamsMap = new HashMap<String,Object>();
				jobParamsMap.put("jobName",jobId);
				jobParamsMap.put("jobClass",jobClass);
				jobParamsMap.put("busParamsKey",QuartzConstant.BUS_PARAMS);
				jobParamsMap.put("startTime",startTime);
				jobParamsMap.put("endTime",endTime);
				jobParamsMap.put("time",jobTime);

				JSONObject busParamObject = JSONObject.parseObject(jobParams);
				busParamObject.put("TASK_NAME",params.get("TASK_NAME"));
				busParamObject.put("TASK_TYPE_DESC",params.get("TASK_TYPE_DESC"));
                busParamObject.put("taskType", taskType);
                busParamObject.put("jobId", params.get("ID"));
                jobParamsMap.put("busParamObject",busParamObject);
				try {
					quartzService.addLoopJob(jobParamsMap, null, null);
					Map<String,Object> jobDbMap = new HashMap<String,Object>();
					jobDbMap.put("ID",params.get("ID"));
					jobDbMap.put("EXEC_STATUS","1");
					coreService.updateObject2New("joblist.updateExecStatusById",jobDbMap,this.getDbKey(request));
				}catch(Exception e){
					log.error("调用定时任务接口报错",e);
					throw new Exception("调用定时任务接口报错,请检查定时任务（quartz）服务，是否启动！");
				}

			}else {
				throw new Exception("未查询到任务，请检查！");
			}


		} catch (Exception e) {
			log.error("JobTaskApiController, 失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return null;
	}

	@RequestMapping(value = "/jobStop", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updatejobStop(HttpServletRequest request) {
		log.debug("JobTaskApiController,运行开始");
		Map<String,Object> params = this.getParamsMapByObject(request);
		try {

			Map<String,Object> jobMap = this.coreService.queryForObject2New("joblist.queryJobList",params,this.getDbKey(request));
			if(jobMap !=null && !jobMap.isEmpty()){
				String jobId = StringTool.object2String(jobMap.get("ID"));
				Map<String,Object> jobParamsMap = new HashMap<String,Object>();
				jobParamsMap.put("jobName",jobId);
				try {
					quartzService.removeJob(jobParamsMap, null, null);
					Map<String,Object> jobDbMap = new HashMap<String,Object>();
					jobDbMap.put("ID",params.get("ID"));
					jobDbMap.put("EXEC_STATUS","0");
					coreService.updateObject2New("joblist.updateExecStatusById",jobDbMap,this.getDbKey(request));

				}catch(Exception e){
					log.error("调用定时任务停止接口报错",e);
					throw new Exception("调用定时任务停止接口报错,请检查定时任务（quartz）服务，是否启动！");
				}

			}else {
				throw new Exception("未查询到任务，请检查！");
			}


		} catch (Exception e) {
			log.error("JobTaskApiController, 失败 ---> ", e);
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
			//resultData = quartzManager.getNextNFireTimes(5, param.get("cron"));
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



}
