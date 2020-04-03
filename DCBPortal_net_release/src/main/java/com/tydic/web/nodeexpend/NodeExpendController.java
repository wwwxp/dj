package com.tydic.web.nodeexpend;

import PluSoft.Utils.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.jstorm.ui.model.ZookeeperNode;
import com.alibaba.jstorm.ui.utils.ZookeeperManager;
import com.esotericsoftware.minlog.Log;
import com.tydic.bean.ClusterNodeDto;
import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.nodeexpend.NodeExpendService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/nodeexpend")
public class NodeExpendController extends BaseController  {
	/**
	 * 节点扩展日志对象
	 */
	private static Logger log = LoggerFactory.getLogger(NodeExpendController.class);

	/**
	 * 节点收缩Service对象
	 */
	@Autowired
	private NodeExpendService nodeExpendService;

	@Autowired
	private CoreService coreService;

	/**
	 * 查询集群Topology列表
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryClusterTreeList", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryClusterTreeList(HttpServletRequest request) {
		log.debug("NodeExpendController, 查询节点收缩Topology列表...");
		try {
			List<HashMap<String, Object>> resultList = nodeExpendService.queryClusterTreeList(this.getParamsMapByObject(request) ,this.getDbKey(request));
			return JSON.Encode(resultList);
		} catch (Exception e) {
			log.error("NodeExpendController, 查询节点收缩Topology列表失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 新增阀值配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addThresholdConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String addThresholdConfig(HttpServletRequest request) {
		log.debug("NodeExpendController, 新增阀值配置...");
		try {
			Map<String, Object> retMap = nodeExpendService.addThresholdConfig(this.getParamsMapByObject(request) ,this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 新增阀值配置失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 修改阀值配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateThresholdConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateThresholdConfig(HttpServletRequest request) {
		log.debug("NodeExpendController, 修改阀值配置...");
		try {
			Map<String, Object> retMap = nodeExpendService.updateThresholdConfig(this.getParamsMapByObject(request) ,this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 修改阀值配置失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 删除阀值配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/delThresholdConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String delThresholdConfig(HttpServletRequest request) {
		log.debug("NodeExpendController, 删除阀值配置...");
		try {
			Map<String, Object> retMap = nodeExpendService.delThresholdConfig(this.getParamsMapByObject(request) ,this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 删除阀值配置失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}


	/**
	 * 新增阀值配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addTimingConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String addTimingConfig(HttpServletRequest request) {
		log.debug("NodeExpendController, 新增定时配置...");
		try {
			List<Map<String, String>> paramsList = this.getParamsList(request);
			Map<String, Object> retMap = nodeExpendService.addTimingConfig(paramsList ,this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 新增定时配置失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 新增手动配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addManualConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String addManualConfig(HttpServletRequest request) {
		log.debug("NodeExpendController, 新增手动配置...");
		try {
			List<Map<String, String>> paramsList = this.getParamsList(request);
			Map<String, Object> retMap = nodeExpendService.addManualConfig(paramsList ,this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 新增手动配置失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}


	/**
	 * 新增手动配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/execManual", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateExecManual(HttpServletRequest request) {
		log.debug("NodeExpendController, 手动执行 ..");
		try {

			Map<String, Object> retMap = nodeExpendService.addExecConfig(this.getParamsMapByObject(request),this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 新增手动执行失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 新增手动配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/execNodeexpendJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateNodeexpendJob(HttpServletRequest request) {
		log.debug("NodeExpendController, 手动执行 ..");
		try {

			Map<String, Object> retMap = nodeExpendService.updateNodeexpendJob(this.getParamsMapByObject(request),this.getDbKey(request));
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 新增手动执行失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 新增手动配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/execImmJob", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateExecImmJob(HttpServletRequest request) {
		log.debug("NodeExpendController, 定时执行 ..");
		try {

			Map<String, Object> retMap = nodeExpendService.updateExecImmJob(this.getParamsMapByObject(request),this.getDbKey(request));
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 新增手动执行失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}



	/**
	 * 修改阀值配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateTimingConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateTimingConfig(HttpServletRequest request) {
		log.debug("NodeExpendController, 修改定时配置...");
		try {
			Map<String, Object> retMap = nodeExpendService.updateTimingConfig(this.getParamsMapByObject(request) ,this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 修改定时配置失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	/**
	 * 修改手工配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateManualConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateManualConfig(HttpServletRequest request) {
		log.debug("NodeExpendController, 修改手动配置...");
		try {
			Map<String, Object> retMap = nodeExpendService.updateManualConfig(this.getParamsMapByObject(request) ,this.getDbKey(request), request);
			return JSON.Encode(retMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 修改手动配置失败， 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 修改手工配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryRule", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryRule(HttpServletRequest request) {
		try {
			String url = SystemProperty.getContextProperty("host.quota.url");
			Map<String, Object>  paramMap = this.getParamsMapByObject(request);
			//获取所有集群主机信息
			List<HashMap<String, Object>> clusterHostList = coreService.queryForList2New("expendStrategyConfig.queryClusterNodeList",paramMap , this.getDbKey(request));
			ClusterNodeDto nodeDto = new ClusterNodeDto();
			String clusterId = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_ID"));
			String clusterCode = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_CODE"));
			String clusterType = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_TYPE"));
			nodeDto.setStrategyId(StringTool.object2String(paramMap.get("STRATEGY_ID")));
			nodeDto.setClusterId(clusterId);
			nodeDto.setClusterCode(clusterCode);
			nodeDto.setClusterType(clusterType);
			String hosts = this.getTopoHost(StringTool.object2String(paramMap.get("TASK_PROGRAM_ID")), this.getDbKey(request));
			nodeDto.setDeployHostIPArray(Arrays.asList(hosts));
			String type = StringTool.object2String(paramMap.get("OPERATOR_TYPE"));

			boolean isTrigger=false;
			if("5".equals(type)){
				paramMap.put("OPERATOR_TYPE", 1);
				List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", paramMap, this.getDbKey(request));
				HttpClientUtil.getRule(url, nodeDto, StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT")));
				isTrigger = HttpClientUtil.isKTrigger(ruleList, nodeDto,"");
			}else if("6".equals(type)){
				paramMap.put("OPERATOR_TYPE", 2);
				List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", paramMap, this.getDbKey(request));
				HttpClientUtil.getRule(url, nodeDto, StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT")));
				isTrigger = HttpClientUtil.isSTrigger(ruleList, nodeDto,"");
			}
			Map<String,Object> resultMap = new HashMap<String,Object>();
			resultMap.put("isTrigger", isTrigger);
			resultMap.put("msg", nodeDto.getMsg());
			return JSON.Encode(resultMap);
		} catch (Exception e) {
			log.error("NodeExpendController, 查询主机资源信息失败， 失败原因: ", e);
			Map<String,Object> resultMap = new HashMap<String,Object>();
			resultMap.put("msg", "error");
			return JSON.Encode(resultMap);
		}
	}
	/**
	 * 修改手工配置
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/zkData", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryZkData(HttpServletRequest request) {
		Map<String, Object>  paramMap = this.getParamsMapByObject(request);
		String topologyeName = StringTool.object2String(paramMap.get("TASK_NAME"));
		try {


			Map<String, Object> resultMap = new HashMap<String,Object>();


			String clusterName = StringTool.object2String(paramMap.get("CLUSTER_CODE"));

			//服务名称

			String topologyPath = "heartbeat_report/"+topologyeName;

			List<ZookeeperNode> result;
			try {
				clusterName = StringEscapeUtils.escapeHtml(clusterName);
				result = ZookeeperManager.listZKNodes(clusterName, topologyPath);
			} catch (Exception e) {
				log.error("获取ZK服务列表失败，失败原因: ", e);
				throw e;
			}
			if (!BlankUtil.isBlank(result)) {
				int count = 0;
				for (ZookeeperNode zkNode : result) {
					String nodeName = zkNode.getName();
					String nodePath = topologyPath + "/" + nodeName;
					String nodeData = ZookeeperManager.getZKNodeData(clusterName, nodePath);
					//String nodeData = zkNode.getData();
					if (!BlankUtil.isBlank(nodeData)) {
						count += Integer.parseInt(nodeData);
					}
				}
				resultMap.put(topologyeName, count);
			}else{
				resultMap.put(topologyeName, 0);
			}
			return JSON.Encode(resultMap);
		} catch (Exception e) {
			log.error("NodeExpendController,  失败原因: ", e);
			Map<String,Object> resultMap = new HashMap<String,Object>();
			resultMap.put(topologyeName, "0");
			return JSON.Encode(resultMap);
		}
	}


	/**
	 * 业务程序读取配置文件
	 * @param taskProgramId 业务程序任务ID
	 * @param dbKey 查询数据库Key
	 */
	private String getTopoHost(String taskProgramId,String dbKey) {
		//修改Topology配置文件，新增work分配主机列表
		//根据任务ID查询配置文件
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("TASK_PROGRAM_ID", taskProgramId);
		HashMap<String, Object> taskProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryParams, dbKey);
		log.info("业务程序重新负载，业务程序信息: " + taskProgramMap.toString());

		//查询版本发布服务器信息
		FtpDto ftpDto = SessionUtil.getFtpParams();

		//业务配置文件
		String deployPath = StringTool.object2String(taskProgramMap.get("CLUSTER_DEPLOY_PATH"));
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String version = StringTool.object2String(taskProgramMap.get("VERSION"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));

		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName)
				+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;

		String hoststr="";
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			InputStream fileStream = trans.get(realFilePath);
			String buffer = FileUtil.readInputStream(fileStream);
			//配置文件JSON对象
			JSONObject fileJsonObj = com.alibaba.fastjson.JSON.parseObject(buffer.trim());
			//development节点数据
			JSONObject devJsonObj = (JSONObject) fileJsonObj.get("development");
			//config节点数据
			JSONObject configJsonObj = (JSONObject)devJsonObj.get("config");
			//topology.billing.workgroup.supervisor.hostname2rate节点数据
			JSONArray jsonArray = configJsonObj.getJSONArray("topology.billing.workgroup.supervisor.hostname2rate");

			for (Object hostObject : jsonArray) {
				JSONArray hostArray =  (JSONArray) hostObject;
				hoststr +=hostArray.get(0)+",";
			}
			char indexc = hoststr.charAt(hoststr.length()-1);
			if(indexc == ','){
				hoststr = hoststr.substring(0,hoststr.length()-1);
			}
			return hoststr;
		}catch(Exception e){
			Log.error("读取失败",e);
			throw new RuntimeException("获取配置文件失败");
		}finally {
			if (trans != null) {
				trans.close();
			}
		}


	}
	public static void main(String[] args) {
		String hosts = "1,2";
		System.out.println(Arrays.asList(hosts));

	}

}
