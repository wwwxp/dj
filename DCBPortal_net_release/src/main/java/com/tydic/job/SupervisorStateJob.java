package com.tydic.job;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.util.Constant;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.security.krb5.internal.crypto.Des;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("supervisorStateJob")
public class SupervisorStateJob {
	private static Logger log = Logger.getLogger(SupervisorStateJob.class);

	@Autowired
	private CoreService coreService;

	//@Scheduled(cron = "0 */2 * * * ?")
	//s@Scheduled(cron = "*/5 * * * * ?")
	public void queryChartsListNew() throws Exception {
		try {


			List<Map<String, Object>> taskList = coreService.queryForList3New("instConfig.querySupervisorInitTask", null, FrameConfigKey.DEFAULT_DATASOURCE);
			if (CollectionUtils.isEmpty(taskList)) {
				return;
			}
			for (int i = 0; i < taskList.size(); i++) {
				Map<String, Object> map = taskList.get(i);
				String username = StringTool.object2String(map.get("SSH_USER"));
				String passwd = DesTool.dec(StringTool.object2String(map.get("SSH_PASSWD")));
				String ip = StringTool.object2String(map.get("HOST_IP"));
				ShellUtils cmdUtil = new ShellUtils(ip, username, passwd);
				String path = map.get("CLUSTER_DEPLOY_PATH") + "/" + Constant.Tools + Constant.ENV + map.get("VERSION") + "/" + Constant.JSTORM;
				String cmd = "ps ux|grep supervisor.log|grep " + path + "|grep -v grep|awk '{print $2}'";
				String result = cmdUtil.execMsg(cmd);
				Pattern p = Pattern.compile("[^0-9]");
				Matcher m = p.matcher(result);
				if (StringUtils.isEmpty(m.replaceAll(""))) {
					if (!StringUtils.equals(StringTool.object2String(map.get("STATUS")), "0")) {
						map.put("STATUS", 0);
						coreService.updateObject2New("instConfig.updateDcfDeployInstConfig", map, FrameConfigKey.DEFAULT_DATASOURCE);
					}
				} else {
					if (!StringUtils.equals(StringTool.object2String(map.get("STATUS")), "1")) {
						map.put("STATUS", 1);
						coreService.updateObject2New("instConfig.updateDcfDeployInstConfig", map, FrameConfigKey.DEFAULT_DATASOURCE);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
