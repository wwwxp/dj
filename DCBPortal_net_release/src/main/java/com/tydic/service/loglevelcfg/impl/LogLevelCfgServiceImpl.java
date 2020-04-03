package com.tydic.service.loglevelcfg.impl;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.loglevelcfg.LogLevelCfgService;
import com.tydic.util.Constant;

@Service("logLevelCfgServiceImpl")
public class LogLevelCfgServiceImpl implements LogLevelCfgService {

	private static Logger log = Logger.getLogger(LogLevelCfgServiceImpl.class);
	@Resource
	private CoreService coreService;

	@Override
	public void update(Map<String, Object> param, String dbKey) throws Exception {
		log.info("LogLevelCfgServiceImpl:" + param);
		List<Map<String, String>> list = (List<Map<String, String>>) param.get("list");
		// List<Map<String,String>> checkData =
		// (List<Map<String,String>>)param.get("checkData");
		// StringBuffer msg = new StringBuffer();

		for (int i = 0; i < list.size(); i++) {
			Map<String, String> map = (Map<String, String>) list.get(i);
			if ("added".equals(map.get("_state"))) {
				coreService.insertObject("logLevelCfg.insertLogLevel", map, dbKey);
			} else if ("modified".equals(map.get("_state"))) {
				coreService.updateObject("logLevelCfg.updateLogLevel", map, dbKey);
			} else if ("removed".equals(map.get("_state"))) {
				coreService.deleteObject("logLevelCfg.delLogLevel", map, dbKey);
			}
		}
		/*
		 * for(int i = 0 ; i < checkData.size();i++){ Map<String,String> map =
		 * (Map<String,String>)checkData.get(i);
		 * msg.append(map.get("PRO_KEY")).append("=").append(map.get("PRO_VALUE"
		 * )).append("\n"); }
		 */
		// log.info("send message:" + msg);
		// sendMsg(msg.toString());
	}

	@Override
	public void sendMsg(Map<String, Object> param) throws Exception {
		log.info("LogLevelCfgServiceImpl:sendMsg:" + param);
		StringBuffer msg = new StringBuffer();
		List<Map<String, String>> checkData = (List<Map<String, String>>) param.get("checkData");
		for (int i = 0; i < checkData.size(); i++) {
			Map<String, String> map = (Map<String, String>) checkData.get(i);
			msg.append(map.get("PRO_KEY")).append("=").append(map.get("PRO_VALUE")).append("\n");
		}
		MulticastSocket ms = null;
		DatagramPacket dataPacket = null;
		try {
			ms = new MulticastSocket();
			ms.setTimeToLive(32);
			// 将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
			byte[] data = msg.toString().getBytes();
			// String ip = SessionUtil.getConfigValue("LOG_LEVEL_SEND_IP");
			// String port = SessionUtil.getConfigValue("LOG_LEVEL_SEND_PORT");

			param.put("ENV_NAME", "OCS_MCAST_CMD_ADDR");
			List<HashMap<String, Object>> envList = coreService.queryForList2New("environments.queryEnvList", param,
					FrameConfigKey.DEFAULT_DATASOURCE);
			if (envList == null || envList.size() < 1) {
				log.error("获取环境变量【OCS_MCAST_CMD_ADDR】失败");
				throw new RuntimeException("获取环境变量【OCS_MCAST_CMD_ADDR】失败");
			}
			String[] envAddress = String.valueOf(envList.get(0).get("ENV_VALUE")).split(":");
			InetAddress address = InetAddress.getByName(envAddress[0]);
			
			log.debug("发送主机地址: " + address.getHostAddress() + ", 主机PORT: " + envAddress[1] + ", 发送的命令为：" + msg);
			dataPacket = new DatagramPacket(data, data.length, address, Integer.parseInt(envAddress[1]));
			ms.send(dataPacket);
			ms.close();
		} catch (Exception e) {
			log.error("发送消息失败：" + e);
			throw new RuntimeException("发送消息失败：" + e.getMessage());
		}finally {
			if(ms !=null){
				ms.close();
			}
		}
	}

    /**
     * 增加日志级别信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> addLogLevel(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception {
        log.debug("增加日志级别信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        Map<String, Object> rstMap = new HashMap<String, Object>();
        try {
			coreService.insertObject2New("logLevelCfg.insertLogLevel", params, dbKey);
			rstMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
			rstMap.put(Constant.RST_STR, "日志级别信息添加成功！");
		} catch (Exception e) {
			log.error("添加日志级别配置失败， 失败原因：", e);
			throw e;
		}
        log.debug("日记级别信息添加成功...");
        return rstMap;
    }
    

    /**
     * 删除日志级别信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> delLogLevel(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("删除日志级别信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        Map<String, Object> rstMap = new HashMap<String, Object>();
        try {
			coreService.deleteObject2New("logLevelCfg.delLogLevel", params, dbKey);
			rstMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
			rstMap.put(Constant.RST_STR, "日志级别信息删除成功！");
		} catch (Exception e) {
			log.error("删除日志级别配置失败， 失败原因：", e);
			throw e;
		}
        log.debug("日记级别信息删除成功...");
        return rstMap;
    }

    /**
     * 修改日志级别信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> updateLogLevel(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception {
        log.debug("修改日志级别信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        Map<String, Object> rstMap = new HashMap<String, Object>();
        try {
			coreService.insertObject2New("logLevelCfg.updateLogLevel", params, dbKey);
			rstMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
			rstMap.put(Constant.RST_STR, "日志级别信息修改成功！");
		} catch (Exception e) {
			log.error("修改日志级别配置失败， 失败原因：", e);
			throw e;
		}
        log.debug("日记级别信息修改成功...");
        return rstMap;
    }

}
