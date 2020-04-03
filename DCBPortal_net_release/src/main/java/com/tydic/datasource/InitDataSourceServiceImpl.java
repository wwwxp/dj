package com.tydic.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.db.SqlSessionDaoSupport;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.StringTool;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.datasource]    
  * @ClassName:    [InitDataSourceServiceImpl]     
  * @Description:  [动态创建数据源]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-8-8 上午11:44:24]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-8-8 上午11:44:24]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
public class InitDataSourceServiceImpl {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(InitDataSourceServiceImpl.class);
	
	/**
	 * 缓存数据库连接池对象,用来关闭动态数据库
	 */
	private static Map<String, DruidDataSource> factoryMap = new HashMap<String, DruidDataSource>();
	
	/**
	 * 获取系统所有数据源句柄对象
	 */
	@Autowired(required=true)
	private SqlSessionDaoSupport sqlSessionDaoSupport;
	
	/**
	 * 动态数据源读取配置文件对象
	 */
	private Resource configLocation;

	/**
	 * 动态数据源读取XML配置对象
	 */
	private Resource[] mapperLocations;
	
	/**
	 * 核心Service对象
	 */
	@Autowired
	private CoreService coreService;
	
	/**
	 * 用来定义数据库连接变量
	 */
	public static final String DATA_SOURCE_TYPE = "datasource";
	private static final String DATA_CFG_CODE_DRIVER_CLZ = "driverClassName";
	private static final String DATA_CFG_CODE_URL = "url";
	private static final String DATA_CFG_CODE_USERNAME = "username";
	private static final String DATA_CFG_CODE_PASSWD = "password";
	
	/**
	 * 初始化创建数据源
	 */
	public void init() {
		initDataSource();
	}
	
	/**
	 * 关闭是将缓存的数据源信息清空
	 */
	public void close() {
		log.debug("关闭动态数据源创建,清空动态数据源对象, 当前存在的动态数据源对象有: " + factoryMap.toString());
		if (factoryMap != null && !factoryMap.isEmpty()) {
			Map<Object, SqlSessionFactory> allSqlSession = this.getSqlSessionDaoSupport().getTargetSqlSessionFactorys();
			Iterator<String> keys = factoryMap.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				DruidDataSource currDataSource = factoryMap.get(key);
				if (currDataSource != null) {
					currDataSource.close();
				}
				allSqlSession.remove(key);
			}
		}
	}
	
	/**
	 * 项目启动后将组件对应的数据源都创建
	 */
	private void initDataSource() {
		//获取所有组件数据源
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", Constant.DCA);
		List<HashMap<String, Object>> dsList = coreService.queryForList2New("componentsConfig.queryComponentDsList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
		log.debug("初始化组件数据源开始...");
		//根据组件ID分组获取组件数据源
		if (!BlankUtil.isBlank(dsList)) {			
			//获取所有的集群
			List<String> clusterList = new ArrayList<String>();
			for(int i=0; i<dsList.size(); i++) {
				String clusterId = StringTool.object2String(dsList.get(i).get("CLUSTER_ID"));
				if (!clusterList.contains(clusterId)) {
					clusterList.add(clusterId);
				}
			}
			log.debug("初始化组件数据源， 可创建数据源组件个数为: " + clusterList.size());
			
			for (int i=0; i<clusterList.size(); i++) {
				String clusterId = clusterList.get(i);
				String clusterCode = null;
				
				List<Map<String, Object>> clusterEleList = new ArrayList<Map<String, Object>>();
				for (int j=0; j<dsList.size(); j++) {
					Map<String, Object> childClusterMap = dsList.get(j);
					String childClusterId = StringTool.object2String(childClusterMap.get("CLUSTER_ID"));
					String childClusterCode = StringTool.object2String(childClusterMap.get("CLUSTER_CODE"));
					
					if (clusterId.equals(childClusterId)) {
						//判断是否为密码类型，如果为密码类型需要解密
						if (Constant.NEED_DES_PASSWD) {
							String cfgType = StringTool.object2String(childClusterMap.get("CFG_TYPE"));
							//String cfgCode = StringTool.object2String(childClusterMap.get("CFG_CODE"));
							String cfgValue = StringTool.object2String(childClusterMap.get("CFG_VALUE"));
							String isPasswd = StringTool.object2String(childClusterMap.get("CFG_IS_PASSWD"));
							if (DATA_SOURCE_TYPE.equalsIgnoreCase(cfgType) 
									&& BusinessConstant.PARAMS_BUS_1.equals(isPasswd)) {
								cfgValue = DesTool.dec(cfgValue);
								childClusterMap.put("CFG_VALUE", cfgValue);
							} 
						}
						
						clusterEleList.add(childClusterMap);
						clusterCode = childClusterCode;
					}
				}
				
				if (!BlankUtil.isBlank(clusterCode)) {
					try {
						addExtDataSource(clusterCode, clusterEleList);
						log.debug("动态添加数据源成功， 数据源Key: " + clusterCode);
					} catch (Exception e) {
						log.error("添加动态数据源失败, 集群编码 + " + clusterCode + "， 失败原因: " + e);
					}
					log.debug("集群编码为【" + clusterCode + "】初始化创建数据源成功...");
				}
			}
		}
	}
	
	/**
	 * 新增数据源
	 * @param dbKey 数据源Key
	 * @param dsList 数据源配置信息
	 * @throws Exception 异常信息
	 */
	public void addExtDataSource(String dbKey, List<Map<String, Object>> dsList) throws Exception {
		log.debug("动态新增数据源， 数据源Key: " +  dbKey + ", 数据源参数: " + dsList.toString());
		//创建数据源
		DruidDataSource dataSource = createExtDataSource(dbKey, dsList);
		log.debug("动态创建数据源成功， 数据源地址: " + dataSource);
				
		Map<Object, SqlSessionFactory> allSqlSession = this.getSqlSessionDaoSupport().getTargetSqlSessionFactorys();
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setConfigLocation(configLocation);
		factoryBean.setMapperLocations(mapperLocations);
		factoryBean.setDataSource(dataSource);
		
		try {
			if (!allSqlSession.containsKey(dbKey)) {
				allSqlSession.put(dbKey, factoryBean.getObject());
				log.debug("新增数据源，将数据源添加到sqlSessionFactory， 数据源Key: " + dbKey + ", 数据源信息: " + dataSource);
				
				//将数据源缓存
				factoryMap.put(dbKey, dataSource);
				log.debug("新增数据源成功， 数据源Key: " + dbKey);
			} else {
				log.debug("数据源存在，先将数据源删掉,数据源Key " + dbKey);
				
				//将数据源对象关闭
				DruidDataSource currDataSource = factoryMap.get(dbKey);
				if (currDataSource != null) {
					currDataSource.close();
				}
				log.debug("新增数据源， 先将已经存在数据源关闭， 数据源Key: " + dbKey + ", 数据源信息: " + currDataSource);
				
				//将动态数据源移除掉
				allSqlSession.remove(dbKey);
				log.debug("新增数据源， 将已经存在的数据源从targetSqlSessionFactorys对象移除， 数据源Key: " + dbKey);
				
				//将新增数据源添加到sqlSessionFactorys对象中
				allSqlSession.put(dbKey, factoryBean.getObject());
				log.debug("新增数据源， 将数据源信息添加到targetSqlSessionFactorys对象， 数据源Key：" + dbKey + ", 数据源信息: " + dataSource);
				
				//将数据源添加到更新到缓存对象
				factoryMap.put(dbKey, dataSource);
				log.debug("新增数据源， 缓存数据源对象，数据源Key: " + dbKey + ", 数据源信息: " + dataSource);
			}
			log.debug("新增数据源后， 当前的数据源对象有: " + allSqlSession.toString());
		} catch (Exception e) {
			log.error("添加动态数据源失败, 数据源Key: " + dbKey + "， 失败原因: ", e);
			throw e;
		}
	}
	
	/**
	 * 修改数据源
	 * @param dbKey  数据源Key
	 * @param dsList 数据源配置数据
	 * @throws Exception 
	 */
	public void updateExtDataSource(String dbKey, List<Map<String, Object>> dsList) throws Exception {
		log.debug("动态修改数据源， 数据源Key: " +  dbKey + ", 数据源参数: " + dsList.toString());
		//创建数据源
		DruidDataSource dataSource = createExtDataSource(dbKey, dsList);
		log.debug("动态创建数据源成功， 数据源地址: " + dataSource);
		
		Map<Object, SqlSessionFactory> allSqlSession = this.getSqlSessionDaoSupport().getTargetSqlSessionFactorys();
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setConfigLocation(configLocation);
		factoryBean.setMapperLocations(mapperLocations);
		factoryBean.setDataSource(dataSource);
		
		try {
			if (allSqlSession.containsKey(dbKey)) {
				log.debug("数据源存在，先将数据源删掉,数据源Key " + dbKey);
				
				//将数据源对象关闭
				DruidDataSource currDataSource = factoryMap.get(dbKey);
				if (currDataSource != null) {
					currDataSource.close();
				}
				log.debug("修改数据源， 先将数据源关闭， 数据源Key: " + dbKey + ", 数据源信息: " + currDataSource);
				
				allSqlSession.remove(dbKey);
				log.debug("修改数据源，将数据源从sqlsessionFactorys移除， 数据源Key: " + dbKey);
				
				allSqlSession.put(dbKey, factoryBean.getObject());
				log.debug("修改数据源， 将修改后的数据源信息更新， 数据源Key: " + dbKey);
				
				factoryMap.put(dbKey, dataSource);
				log.debug("修改数据源，将修改后的数据源对象添加到缓存， 数据源Key: " + dbKey + ", 数据源信息: " + dataSource);
				
			} else {
				allSqlSession.put(dbKey, factoryBean.getObject());
				log.debug("修改数据源，数据源不存在， 新增数据源， 数据源Key: " + dbKey);
				
				//将数据源缓存
				factoryMap.put(dbKey, dataSource);
				log.debug("修改数据源，数据源缓存成功， 数据源Key: " + dbKey + ", 数据源对象: " + dataSource);
			}
			log.debug("修改数据源后，当前的数据源对象有: " + allSqlSession.toString());
		} catch (Exception e) {
			log.error("添加动态数据源失败, 数据源Key: " + dbKey + "， 失败原因: ", e);
			throw e;
		}
	}
	
	/**
	 * 删除数据源
	 * @param dbKey  数据源Key
	 * @throws Exception
	 */
	public void delExtDataSource(String dbKey) throws Exception {
		log.debug("动态删除数据源， 数据源Key: " + dbKey);
		
		Map<Object, SqlSessionFactory> allSqlSession = this.getSqlSessionDaoSupport().getTargetSqlSessionFactorys();
		try {
			if (allSqlSession.containsKey(dbKey)) {
				
				//将数据源对象关闭
				DruidDataSource currDataSource = factoryMap.get(dbKey);
				if (currDataSource != null) {
					currDataSource.close();
				}
				log.debug("移除数据源， 先将数据源对象关闭， 数据源Key: " + dbKey + ", 数据源信息: " + currDataSource);
				
				allSqlSession.remove(dbKey);
				log.debug("移除数据源，将数据源从targetSqlSessionFactorys对象中移除， 数据源Key: " + dbKey);
				log.debug("移除数据源后， 当前的数据源对象有: " + allSqlSession.toString());
			}
		} catch (Exception e) {
			log.error("数据源删除失败， 数据源Key: " + dbKey + "， 失败原因: ", e);
			throw e;
		}
	}
	
	/**
	 * 动态创建数据源
	 * @param dbKey 数据库Key
	 * @param dsList 集群参数
	 * @return 数据源对象
	 */
	private DruidDataSource createExtDataSource(String dbKey, List<Map<String, Object>> dsList) {
		log.debug("动态创建数据源，数据源Key: " + dbKey + ", 数据源信息: " + dsList.toString());
		Properties dsProp = new Properties();
		
		for (int i=0; i<dsList.size(); i++) {
			Map<String, Object> dsMap = dsList.get(i);
			
			//获取数据源URL
			String cfgType = StringTool.object2String(dsMap.get("CFG_TYPE"));
			String cfgCode = StringTool.object2String(dsMap.get("CFG_CODE"));
			String cfgValue = StringTool.object2String(dsMap.get("CFG_VALUE"));
			
			if (DATA_SOURCE_TYPE.equalsIgnoreCase(cfgType) && DATA_CFG_CODE_DRIVER_CLZ.equalsIgnoreCase(cfgCode)) {
				dsProp.setProperty("driverClassName", cfgValue);
			} else if (DATA_SOURCE_TYPE.equalsIgnoreCase(cfgType) && DATA_CFG_CODE_URL.equalsIgnoreCase(cfgCode)) {
				dsProp.setProperty("url", cfgValue);
			} else if (DATA_SOURCE_TYPE.equalsIgnoreCase(cfgType) && DATA_CFG_CODE_USERNAME.equalsIgnoreCase(cfgCode)) {
				dsProp.setProperty("username", cfgValue);
			} else if (DATA_SOURCE_TYPE.equalsIgnoreCase(cfgType) && DATA_CFG_CODE_PASSWD.equalsIgnoreCase(cfgCode)) {
				dsProp.setProperty("password", cfgValue);
			} else if (DATA_SOURCE_TYPE.equalsIgnoreCase(cfgType)){
				dsProp.setProperty(cfgCode, cfgValue);
			}
		}
		
		DruidDataSource dataSource = null;
		try {
			dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(dsProp);
		} catch (Exception e) {
			log.error("动态创建数据源失败，数据源Key: " + dbKey + ", 失败原因: ", e);
			if (dataSource != null) {
				dataSource.close();
			}
		}
		return dataSource;
	}

	public SqlSessionDaoSupport getSqlSessionDaoSupport() {
		return sqlSessionDaoSupport;
	}

	public void setSqlSessionDaoSupport(SqlSessionDaoSupport sqlSessionDaoSupport) {
		this.sqlSessionDaoSupport = sqlSessionDaoSupport;
	}

	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	public void setMapperLocations(Resource[] mapperLocations) {
		this.mapperLocations = mapperLocations;
	}
}


