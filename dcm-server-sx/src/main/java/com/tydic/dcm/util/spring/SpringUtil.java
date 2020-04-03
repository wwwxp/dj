package com.tydic.dcm.util.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tydic.bp.QuartzManager;
import com.tydic.bp.core.utils.db.CoreBaseDao;
import com.tydic.bp.core.utils.tools.SpringContextUtil;

/**
 * 
 * @ClassName: SpringUtil
 * @Description: Spring对象管理类
 * @Prject: dcm-server_base
 * @author: yuanH
 * @date 2016年7月28日 下午2:37:58
 */
public class SpringUtil {

	/**
	 * 任务调度对象
	 * @return
	 */
	public static QuartzManager getQuartzManager() {
		QuartzManager quartzMgr = (QuartzManager) SpringContextUtil.getBean("quartzManager");
		return quartzMgr;
	}
	
	
	/**
	 * 获取CoreBaseDao对象，用来进行数据库操作
	 * @return
	 */
	public static CoreBaseDao getCoreBaseDao() {
		return (CoreBaseDao) SpringContextUtil.getBean("coreBaseDao");
	}
	
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext(new String[]{"conf/spring-config.xml"});
		for (int i=0; i<10; i++) {
			SpringUtil.getQuartzManager();
		}
	}
}
