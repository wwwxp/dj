package com.tydic.quartz;

import com.tydic.bean.DataBaseArchiveDto;
import com.tydic.bean.DataBaseArchiveLogDto;
import com.tydic.bp.QuartzConstant;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.CommonTool;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.db.CoreBaseDao;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.util.Constant;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.quartz.*;

import java.util.*;

//@DisallowConcurrentExecution(功能同StatefulJob接口)表示在同一个job中串行,上个任务没有执行完,下个任务等待,直到上个任务执行完成才开始执行
@DisallowConcurrentExecution
public class DatabaseArchiveQuartz implements Job {

	private CoreService coreService = (CoreService) SpringContextUtil.getBean("coreService");
	
	private CoreBaseDao coreBaseDao = (CoreBaseDao) SpringContextUtil.getBean("coreBaseDao");

	private static Logger log = Logger.getLogger(DatabaseArchiveQuartz.class);

	/**
	 * 归档前目标数据清空
	 */
	public static final String ONE = "1";

	/**
	 * 归档后源表数据清空
	 */
	public static final String TWO = "2";

	/**
	 * 归档后进行表分析
	 */
	public static final String THREE = "3";

	/**
	 * 归档后进行索引分析
	 */
	public static final String FOUR = "4";

	/**
	 * 数据库表名-时间类型 tbl_[YYYYMM]
	 */
	public static final String TBL_TYPE_TIME = "TIME";

	/**
	 * 数据库表名-时间类型 tbl_[YYYYMM]
	 */
	public static final String TBL_TYPE_LATN = "LATN";

	/**
	 * 数据库表名-其他类型 即单表 无[]的正常表名 tbl
	 */
	public static final String TBL_TYPE_REAL = "REAL";
	
	/**
	 * 默认每次归档条数
	 */
	public static final int ARC_NUM_DEAFAULT = 1000;
	

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String jobId = context.getJobDetail().getKey().getName();
		log.debug("数据归档开始--->" + jobId);
		Map<String, String> busParamMap = new HashMap<>();
		DataBaseArchiveLogDto logDto = null;
		try {
			
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			Map<String, Object> jobParams = (Map<String, Object>) dataMap.get(QuartzConstant.BUS_PARAMS);
			if (BlankUtil.isBlank(jobParams) || jobParams.isEmpty()) {
				log.debug("业务参数为空，无法执行归档任务...");
				return;
			}
			String archiveId = StringTool.object2String(jobParams.get("BUS_ID"));

			// 日志批次号,日志批次号+ArchiveId 为一个任务的日志
			String batchNum = DateUtil.getCurrent("yyyyMMddHHmmssSSS");
			logDto = new DataBaseArchiveLogDto(archiveId, batchNum, "");

			// 定时任务id
			// String busId = context.getJobDetail().getKey().getName();

			busParamMap.put("ARCHIVE_ID", archiveId);

			// 表数据归档任务参数
			busParamMap = coreService.queryForObject("datasourceArchive.queryDSArchiveConfig", busParamMap,
					FrameConfigKey.DEFAULT_DATASOURCE);

			String execRuleStr = StringTool.object2String(busParamMap.get("EXEC_RULE"));

			// 源数据源id，归档表名
			String sourDsId = busParamMap.get("SOUR_DS_ID");
			String sourTableName = busParamMap.get("SOUR_TABLE_NAME").trim();

			// 目标数据源id，目标数据表
			String destDsId = busParamMap.get("DEST_DS_ID");
			String destTableName = busParamMap.get("DEST_TABLE_NAME").trim();
			
			// 归档数据条件
			String filterRule = busParamMap.get("FILTER_RULE");
			
			// 区分表名类型 时间类型tbl_[YYYYMM],本地网类型TBL_[556,5656],REAL:普通类型
			String sourTblNameType = getTblNameType(sourTableName);
			String destTblNameType = getTblNameType(destTableName);
			log.debug("数据表类型---> sourceTbl:" + sourTableName + " ,Type:" + sourTblNameType + "|destTbl:" + destTableName
					+ " ,Type:" + destTblNameType);
			
			DataBaseArchiveDto dbaDto = new DataBaseArchiveDto(archiveId, sourDsId, destDsId, filterRule, execRuleStr);
			
			//为实时日志用
			dbaDto.setJobId(jobId);
			
			//如果数据源id为空则不进行任务
			if(BlankUtil.isBlank(destDsId)||BlankUtil.isBlank(sourDsId)){
				throw new RuntimeException("数据源id不能为空");
			}
			
			//查询数据库源信息
			try{
				getDSConfig(dbaDto);
			}catch (Exception e){
				throw new RuntimeException("数据源信息查询失败");
			}
			
			if(!isDsInPool(sourDsId)){
				//数据源没被创建
				throw new RuntimeException("源数据源未被创建--->id:"+sourDsId);
			}
			if(!isDsInPool(destDsId)){
				//数据源没被创建
				throw new RuntimeException("目标数据源未被创建--->id:"+destDsId);
			}
			
			// 归档
			if (sourTblNameType.equals(destTblNameType)) {
				// 表名类型相同
				if (sourTblNameType.equals(this.TBL_TYPE_REAL)) {
					// 普通表格归档
					dbaDto.setSourTableName(sourTableName);
					dbaDto.setDestTableName(destTableName);
					
					//手动执行实时日志
					insertRealTimeLog(jobId,Constant.INT_ONE,batchNum);
					
					boolean isSuc = doArchive(dbaDto, logDto);
					
					updateRealTimeLog(jobId, isSuc);
					updateRealTimeLogEnd(jobId);
					
				} else if (sourTblNameType.equals(this.TBL_TYPE_TIME)) {
					// 时间后缀表 归档
					// 获取时间后缀-表名
					String sourTblRealName = getTimeTableNameString(sourTableName);
					String destTblRealName = getTimeTableNameString(destTableName);
					
					dbaDto.setSourTableName(sourTblRealName);
					dbaDto.setDestTableName(destTblRealName);
					
					//手动执行实时日志
					insertRealTimeLog(jobId,Constant.INT_ONE,batchNum);
					
					boolean isSuc = doArchive(dbaDto, logDto);
					
					updateRealTimeLog(jobId, isSuc);
					updateRealTimeLogEnd(jobId);

				} else if (sourTblNameType.equals(this.TBL_TYPE_LATN)) {
					// 时间本地网 归档
					// 获取本地网后缀-表名数组
					String[] sourTblRealNames = getLatnTableNameStrings(sourTableName);
					String[] destTblRealNames = getLatnTableNameStrings(destTableName);
					// 匹配策略 tbl_[556,554,465] 到 tbl_[123,125] 按顺序匹配 以 556-123
					// 554-125.
					
					//手动执行实时日志
					insertRealTimeLog(jobId,sourTblRealNames.length,batchNum);
					
					for (int i = 0; i < sourTblRealNames.length; i++) {
						if (i < destTblRealNames.length) {
							dbaDto.setSourTableName(sourTblRealNames[i]);
							dbaDto.setDestTableName(destTblRealNames[i]);
							boolean isSuc=doArchive(dbaDto, logDto);
							updateRealTimeLog(jobId, isSuc);
						}
					}
					updateRealTimeLogEnd(jobId);

				}
			} else {
				// 表名类型不同
				if (sourTblNameType.equals(this.TBL_TYPE_LATN) && destTblNameType.equals(this.TBL_TYPE_REAL)) {
					// 本地网表归档到 一张表
					String[] sourTblRealNames = getLatnTableNameStrings(sourTableName);
					String destTblRealName = destTableName;
					
					insertRealTimeLog(jobId,sourTblRealNames.length,batchNum);

					for (int i = 0; i < sourTblRealNames.length; i++) {
						dbaDto.setSourTableName(sourTblRealNames[i]);
						dbaDto.setDestTableName(destTblRealName);
						boolean isSuc=doArchive(dbaDto, logDto);
						
						updateRealTimeLog(jobId, isSuc);
					}
					updateRealTimeLogEnd(jobId);
					
				} else if (sourTblNameType.equals(this.TBL_TYPE_TIME) && destTblNameType.equals(this.TBL_TYPE_REAL)) {
					// 时间表网表归档到 一张表
					String sourTblRealName = getTimeTableNameString(sourTableName);
					String destTblRealName = destTableName;
					
					dbaDto.setSourTableName(sourTblRealName);
					dbaDto.setDestTableName(destTblRealName);
					
					insertRealTimeLog(jobId,Constant.INT_ONE,batchNum);

					boolean isSuc = doArchive(dbaDto, logDto);
					
					updateRealTimeLog(jobId, isSuc);
					
					updateRealTimeLogEnd(jobId);
					
				} else {
					throw new RuntimeException("数据库表名类型不匹配--->" + sourTableName + " to " + destTableName);
				}
			}

		} catch (Exception e) {
			addArchiveLog(logDto, e.getMessage());
			updateRealTimeLogEnd(jobId);
			log.error("数据归档异常", e);
		}
		log.debug("数据归档结束");
		updateRealTimeLogEnd(jobId);
	}

	private boolean isDsInPool(String dsId) {
        SqlSessionFactory targetSqlSessionFactory = coreBaseDao.getSqlSessionDaoSupport().getTargetSqlSessionFactorys().get(dsId);
        if(targetSqlSessionFactory == null){
        	return false;
        }
        return true;
	}

	/**
	 * 获取 数据名的类型 TIME：时间类型tbl_[YYYYMM],本地网类型TBL_[556,5656],REAL:普通类型
	 * 
	 * @param tableName
	 * @return
	 */
	private String getTblNameType(String tableName) {
		String typeStr = "";
		if (tableName.indexOf("[") > -1 && "]".equals(tableName.substring(tableName.length() - 1))) {
			// 有[ 并以 ] 为结尾的
			if (tableName.toUpperCase().indexOf("[YYYYMM") > -1) {
				// 包含字符串 [YYYYMM 为时间类型 tbl_[YYYYMM] ...
				typeStr = TBL_TYPE_TIME;
			} else {
				// 类似 TBL_[556,5656]
				typeStr = TBL_TYPE_LATN;
			}
		} else {
			// 普通表格
			typeStr = TBL_TYPE_REAL;
		}
		return typeStr;
	}

	private boolean doArchive(DataBaseArchiveDto dto, DataBaseArchiveLogDto logDto) {
		log.debug("单表数据归档开始：--->" + dto.toString());
		// 日志对象
		logDto.setSourTableName(dto.getSourTableName());
		logDto.setDestTableName(dto.getDestTableName());
		int successAmount = 0;

		Integer count = 0;
		long interTime = 0;
		String cleanResult = "";
		String tblAnalyzeResultStr = "";
		String indexAnalyzeResultStr = "";
		try {
			String[] execRules = null;
			if (!BlankUtil.isBlank(dto.getExecRuleStr().trim())) {
				execRules = dto.getExecRuleStr().trim().split(",");
			}
			if (!isTableExsis(dto.getSourTableName(), dto.getSourDsId(), dto.getSourDsType())) {
				throw new RuntimeException("源表" + dto.getSourTableName() + "不存在");
			}
			
			Map<String, Object> SourDSParamMap = new HashMap<String, Object>();
			SourDSParamMap.put("TABLE_NAME", dto.getSourTableName());

			// 查询条件 
			SourDSParamMap.put("FILTER_RULE", dto.getFilterRule());
			// 查询数据量
			try {
				count = (Integer) coreService.commonQryObject("datasourceArchive.querySourceDSAmount", SourDSParamMap,
						dto.getSourDsId());
			} catch (Exception e) {
				throw new RuntimeException("查询源表数据量失败", e);
			}
			Map<String, Object> desDsParamMap = new HashMap<String, Object>();
			desDsParamMap.put("TABLE_NAME", dto.getDestTableName());

			
			// 判断目标数据表是否存在
			if (!isTableExsis(dto.getDestTableName(), dto.getDestDsId(), dto.getDestDsType())) {
				// 目标数据源表不存在
				if (Constant.DATA_SOURCE_TYPE_MYSQL.equals(dto.getSourDsType())
						&& Constant.DATA_SOURCE_TYPE_MYSQL.equals(dto.getDestDsType())) {
					// 两个数据库都为mysql才新建表
					Map<String, Object> createTabSqlMap = coreService.queryForObject2New(
							"datasourceArchive.queryCreateTableSql", SourDSParamMap, dto.getSourDsId());
					// 创建表sql
					String sql = (String) createTabSqlMap.get("Create Table");
					// 动态创建表sql
					String createTableSql = getIfNoExCreateTbaleSql(sql, dto.getDestTableName());
					// 执行动态创建表sql
					desDsParamMap.put("EXEC_SQL", createTableSql);
					coreService.updateObject2New("datasourceArchive.createTableSQL", desDsParamMap, dto.getDestDsId());
				} else {
					// 数据源类型不同，目标数据源表也不存在，则记录日志，退出
					log.error("目标数据库表不存在");
					addArchiveLog(logDto, "目标数据库表不存在\n\n" + dto.toString());
					return false;
				}
			}

			if (isContainsKey(execRules, ONE)) {
				// 1:归档前目标数据清空
				log.debug("归档前目标数据清空");
				cleanResult = "\n归档前目标数据清空:" + cleanData(dto.getDestDsId(), dto.getDestTableName(), null);
			}
			// 获取目标数据表 字段名
			// List<HashMap<String, Object>> desColumnNameList = coreService
			// .queryForList2New("datasourceArchive.queryTableColumnNameMysql",
			// desDsParamMap, destDsId);

			int limitAmount = CommonTool.defaultInt(SystemProperty.getContextProperty("DataBaseArchivePerLimit"), ARC_NUM_DEAFAULT);
			// 总循环插入数据次数，
			int time = (int) Math.ceil((double) count / limitAmount);
			
			//初始化实时记录表
			updateRealTimeRecodeDetailLog(dto,count, successAmount);
			
			Date startTime = new Date();
			for (int i = 0; i < time; i++) {
				String querySql = "";
				if (Constant.DATA_SOURCE_TYPE_MYSQL.equals(dto.getSourDsType())) {
					String limitRange = "limit " + (limitAmount * i) + "," + limitAmount;
					// 查询源数据,param: TABLE_NAME,FILTER_RULE,LIMIT_RANGE
					SourDSParamMap.put("LIMIT_RANGE", limitRange);
					querySql = "datasourceArchive.queryDataFromSourceDSMysql";
				} else {
					// oracle分页查会有多一列 ROW_ID_TT
					SourDSParamMap.put("STAR_INDEX", i * limitAmount);
					SourDSParamMap.put("END_INDEX", (i + 1) * limitAmount);
					querySql = "datasourceArchive.queryDataFromSourceDSOracle";
				}

				List<HashMap<String, Object>> dataList = coreService.queryForList2New(querySql, SourDSParamMap,
						dto.getSourDsId());

				// 插入目标数据源表
				String dataInsertSql = getDataInsertSql(dataList, dto.getDestTableName(), dto.getDestDsType());
				desDsParamMap.put("EXEC_SQL", dataInsertSql);
				int exeCount=0;
				try{
					exeCount = coreService.insertObject2New("datasourceArchive.insertDataToDestDS", desDsParamMap,
							dto.getDestDsId());
				}catch(Exception e){
					log.error("数据归档-归档到目标数据库异常",e);
					throw new RuntimeException("数据库归档-数据归档到目标数据库异常"+e.getMessage());
				}
				successAmount += exeCount;
				log.debug("本次新增-->" + exeCount + " |" + "累加新增成功:" + successAmount);
				
				//实时记录
				updateRealTimeRecodeDetailLog(dto,count, successAmount);
				
			}
			Date endTime = new Date();
			interTime = endTime.getTime() - startTime.getTime();
			log.debug("数据统计--->总数量：" + count + " |" + "累加新增成功:" + successAmount);

			if (isContainsKey(execRules, TWO)) {
				// 2:归档后源表数据清空
				log.debug("归档后源表数据清空");
				cleanResult += "\n\n归档后源表数据清空:"
						+ cleanData(dto.getSourDsId(), dto.getSourTableName(), dto.getFilterRule());
			}
			if (isContainsKey(execRules, THREE)) {
				// 3:归档后进行表分析
				log.debug("归档后进行表分析");
				tblAnalyzeResultStr = "\n\n表分析结果：\n" +analysisTable(dto.getDestDsId(), dto.getDestTableName(), dto.getDestDsType());
			}
			if (isContainsKey(execRules, FOUR)) {
				// 4:归档后进行索引分析
				log.debug("归档后进行索引分析");
				indexAnalyzeResultStr = "\n\n索引分析结果：\n"+analysisIndex(dto.getDestDsId(), dto.getDestTableName(), dto.getDestDsType());
			}
		} catch (Exception e) {
			String msg = "\n\n耗时" + interTime/1000.00 + "s\n\n总数：" + count + "条       归档：" + successAmount + "条"
					+ dto.toString();
			msg = msg + cleanResult + tblAnalyzeResultStr + indexAnalyzeResultStr;
			addArchiveLog(logDto, "数据归档异常,异常原因：" + e.getMessage() + msg);
			log.error("单表数据归档异常", e);
			return false;
		}
		String msg = ("数据归档成功\n耗时" + interTime/1000.00 + "s\n\n总数：" + count + "条        归档：" + successAmount + "条"
				+ dto.toString());
		msg = msg + cleanResult + tblAnalyzeResultStr + indexAnalyzeResultStr;
		addArchiveLog(logDto, msg);
		log.debug("单表数据归档结束");
		return true;
	}

	/**
	 * 例子 INSERT INTO tb_bp_sys_log(log_id, log_name, login_user, params,
	 * exec_type, method, ip) SELECT
	 * '-1','test','test','test','test','test','test' FROM dual union all SELECT
	 * '-2','test2','test2','test2','test2','test2','test2' FROM dual
	 * 
	 * @param dataList
	 * @param tableName
	 * @return
	 */
	public static String getDataInsertSql(List<HashMap<String, Object>> dataList, String tableName, String dbType) {
		StringBuilder sql = new StringBuilder();
		String sqlStr = "";
		if (dataList == null) {
			return "";
		}
		sql.append("INSERT INTO ").append(tableName).append(" (");
		HashMap<String, Object> dataMapOne = dataList.get(0);
		Set<String> keySet = dataMapOne.keySet();
		// 移除oracle分页查询多出来的 row_id_tt列
		keySet.remove("ROW_ID_TT");
		for (String key : keySet) {
				sql.append(key).append(" ,");
		}
		// 去掉多余逗号
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");
		//oracle
		//INSERT INTO tableName (key,key...) select 'value','value' from dual union all  select 'value','value' from dual
		if(dbType.equals(Constant.DATA_SOURCE_TYPE_ORACLE)){
			for (HashMap<String, Object> dataMap : dataList) {
				sql.append(" SELECT ");
				for (String key : keySet) {
					Object data = dataMap.get(key);
					if (data instanceof java.util.Date) {
						sql.append(getDataTypeStr(data, dbType));
					} else if(data == null){
						//data 为 null
						sql.append("null ,");
					} else if(BlankUtil.isBlank(data)){
						//data 为 ''空串
						sql.append("'' ,");
					} else {
						String dataStr = StringTool.object2String(data);
						dataStr = replaceSpecChar(dataStr,dbType);
						sql.append("'").append(dataStr).append("' ,");
					}
				}
				sql.deleteCharAt(sql.length() - 1);
			    sql.append(" FROM dual union all");
			    sqlStr = sql.toString();
				sqlStr = sqlStr.substring(0, sqlStr.lastIndexOf("union all"));
			}
		}else{
			//oracle
			//INSERT INTO tablname (key,key...) VALUES
			sql.append(" VALUES ");
			for (HashMap<String, Object> dataMap : dataList) {
				sql.append("(");
				for (String key : keySet) {
					Object data = dataMap.get(key);
					if (data instanceof java.util.Date) {
						sql.append(getDataTypeStr(data, dbType));
					} else if(data == null){
						//data 为 null
						sql.append("null ,");
					} else if(BlankUtil.isBlank(data)){
						//data 为 ''空串
						sql.append("'' ,");
					} else {
						String dataStr = StringTool.object2String(data);
						dataStr = replaceSpecChar(dataStr,dbType);
						sql.append("'").append(dataStr).append("' ,");
					}
				}
				//去掉最后一个逗号
				sql.deleteCharAt(sql.length() - 1);
				sql.append("),");
			}
			//去掉最后一个逗号
			sql.deleteCharAt(sql.length() - 1);
			sqlStr = sql.toString();
		}
		return sqlStr;
	}

	/**
	 * 获取日期数据 sql片段
	 * 
	 * @param obj
	 *            日期数据
	 * @param dbType
	 *            数据库类型
	 * @return
	 */
	private static String getDataTypeStr(Object obj, String dbType) {
		String objStr = "";
		if (dbType.equals(Constant.DATA_SOURCE_TYPE_ORACLE)) {
			if (obj instanceof java.sql.Timestamp) {
				// 数据库数据为 timestamp
				DateTime dateTime = new DateTime(obj);
				objStr = dateTime.toString("yyyy-MM-dd HH:mm:ss.SSS");// "yyyy-MM-dd
																		// HH:mm:ss,SSS"
				objStr = " to_timestamp('" + objStr + "','yyyy-mm-dd hh24:mi:ss.ff'),";
				// } else if (obj instanceof java.util.Date) {
			} else {
				// 字段类型为 date或者datetime
				DateTime dateTime = new DateTime(obj);
				objStr = dateTime.toString(DateUtil.allPattern);// "yyyy-MM-dd
				objStr = " to_date('" + objStr + "','yyyy-mm-dd hh24:mi:ss'),";
			}
		} else if (dbType.equals(Constant.DATA_SOURCE_TYPE_MYSQL)) {
			if (obj instanceof java.sql.Timestamp) {
				// 数据库数据为 timestamp
				DateTime dateTime = new DateTime(obj);
				objStr = dateTime.toString("yyyy-MM-dd HH:mm:ss.SSS");
				// 'yyyy-MM-dd HH:mm:ss.SSS'
				objStr = " '" + objStr + "',";
				// } else if (obj instanceof java.util.Date) {
			} else {
				// 字段类型为 date或者datetime
				DateTime dateTime = new DateTime(obj);
				objStr = dateTime.toString(DateUtil.allPattern);// "yyyy-MM-dd
				// 'yyyy-MM-dd HH:mm:ss'
				objStr = " '" + objStr + "',";
			}
		} else {
			objStr = " '" + String.valueOf(objStr) + "',";
		}
		return objStr;
	}

	/**
	 * Mysql表是否存在 true:存在，false不存在
	 * 
	 * @param tableName
	 * @param dataSourceId
	 * @return
	 */
	public boolean isMysqlTblExsis(String tableName, String dataSourceId) {
		boolean isTableEx = false;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("TABLE_NAME", tableName.toUpperCase());
		// 执行 show tables like #{TABLE_NAME} 表存在的时候 返回 表名
		Object tableNameObj = coreService.commonQryObject("datasourceArchive.queryTableExisis", paramMap, dataSourceId);
		try {
			if (tableNameObj instanceof String) {
				isTableEx = !BlankUtil.isBlank(StringTool.object2String(tableNameObj).trim());
			}
		} catch (Exception e) {
			log.debug("表不存在,此异常并非操作异常", e);
		}
		return isTableEx;
	}

	/**
	 * 判断 oracle和mysql表是否存在
	 * 
	 * @param tableName
	 *            表名
	 * @param dsId
	 *            数据源id
	 * @param dsType
	 *            数据源类型
	 * @return
	 */
	public boolean isTableExsis(String tableName, String dsId, String dsType) {
		if (dsType.equals(Constant.DATA_SOURCE_TYPE_MYSQL)) {
			return isMysqlTblExsis(tableName, dsId);
		} else if (dsType.equals(Constant.DATA_SOURCE_TYPE_ORACLE)) {
			return isOracleTblExsis(tableName, dsId);
		} else {
			return false;
		}
	}

	/**
	 * Oracle表是否存在 true:存在，false不存在
	 * 
	 * @param tableName
	 * @param dataSourceId
	 * @return
	 */
	public boolean isOracleTblExsis(String tableName, String dataSourceId) {
		boolean isTableEx = false;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("TABLE_NAME", tableName.toUpperCase());
		// 表名必须为大写
		Integer tblNum = (Integer) coreService.commonQryObject("datasourceArchive.queryTblExisisOracle", paramMap,
				dataSourceId);
		if (!BlankUtil.isBlank(tblNum) && tblNum > 0) {
			// 当tblNum等于1则存在表

			isTableEx = true;
		}
		return isTableEx;
	}

	/**
	 * 获取动态创建目标表的sql
	 * 
	 * @param createTabSql
	 *            创建源表的sql CREATE `tbl` ()
	 * @param tableName
	 *            目标表的表名 CREATE TABLE IF NOT EXISTS ()
	 * @return
	 */
	private static String getIfNoExCreateTbaleSql(String createTabSql, String tableName) {

		return "CREATE TABLE IF NOT EXISTS " + "`" + tableName + "` "
				+ createTabSql.substring(createTabSql.indexOf('('));
	}

	/**
	 * 清空表数据
	 * 
	 * @param dsId
	 *            数据源id
	 * @param tableName
	 *            表名
	 * @param filterRule
	 *            过滤条件（sql条件）
	 * @return
	 */
	private String cleanData(String dsId, String tableName, String filterRule) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		if (!BlankUtil.isBlank(filterRule)) {
			paramsMap.put("FILTER_RULE",filterRule);
		}
		int count = 0;
		paramsMap.put("TABLE_NAME", tableName);
		try{
			count = coreService.deleteObject("datasourceArchive.deleteAllData", paramsMap, dsId);
		}catch(Exception e){
			throw new RuntimeException("清空表数据失败:"+tableName,e);
		}
		log.debug("删除表数据--->" + count + " 条");
		return "删除表数据--->" + count + " 条\n";
	}

	/**
	 * 表分析
	 * 
	 * @param dsId
	 * @param tableName
	 * @param dsType
	 */
	private String analysisTable(String dsId, String tableName, String dsType) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		List<HashMap<String, Object>> datalist = null;
		String resultStr = "";
		try {

			paramsMap.put("TABLE_NAME", tableName);
			String sqlKey = "";
			if (Constant.DATA_SOURCE_TYPE_MYSQL.equals(dsType)) {
				sqlKey = "datasourceArchive.queryTblAnalyzeMysql";
			} else if (Constant.DATA_SOURCE_TYPE_ORACLE.equals(dsType)) {
				sqlKey = "datasourceArchive.queryTblAnalyzeOracle";
			}
			datalist = coreService.queryForList2New(sqlKey, paramsMap, dsId);
			resultStr = !BlankUtil.isBlank(datalist) ? datalist.toString().replace('=', ':') + "\n" : "";
		} catch (Exception e) {
			throw new RuntimeException("\n\n表分析异常",e);
		}
		return resultStr;
	}

	/**
	 *
	 * @param dsId
	 * @param tableName
	 * @param dsType
	 * @return
	 */
	private String analysisIndex(String dsId, String tableName, String dsType) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("TABLE_NAME", tableName);
		String sqlKey = "";
		if (Constant.DATA_SOURCE_TYPE_MYSQL.equals(dsType)) {
			sqlKey = "datasourceArchive.queryIndexAnalyzeMysql";
		} else if (Constant.DATA_SOURCE_TYPE_ORACLE.equals(dsType)) {
			sqlKey = "datasourceArchive.queryIndexAnalyzeOracle";
		}
		List<HashMap<String, Object>> datalist = null;
		try{
			datalist = coreService.queryForList2New(sqlKey, paramsMap, dsId);
		}catch(Exception e){
			throw new RuntimeException("索引分析异常",e);
		}
		return !BlankUtil.isBlank(datalist) ? datalist.toString().replace('=', ':') + "\n" : "";
	}

	/**
	 * keys数组是否有 key
	 * 
	 * @param keys
	 *            为空返回 false
	 * @param key
	 *            当keys有key 返回true 否则返回false
	 * @return
	 */
	public static boolean isContainsKey(String[] keys, String key) {
		if (BlankUtil.isBlank(keys)) {
			// 数组为空，返回false
			return false;
		}
		for (String str : keys) {
			if (str.equals(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 类型为适配 tb_[551,552,556] 转换 为 tb_551,tb_552,tb_556 字符数组
	 * 
	 * @param tableStr
	 * @return
	 */
	private static String[] getLatnTableNameStrings(String tableStr) {
		// 得到 551,552,556字符数组
		String[] suffixs = tableStr.substring(tableStr.lastIndexOf("[") + 1, tableStr.lastIndexOf("]")).split(",");
		// 得到 tb_
		String tabPrefix = tableStr.substring(0, tableStr.lastIndexOf("["));
		String[] tableNames = new String[suffixs.length];
		for (int i = 0; i < suffixs.length; i++) {
			tableNames[i] = tabPrefix + suffixs[i];
		}
		return tableNames;
	}

	/**
	 * 类型为适配 tb_[YYYYMM]
	 * 
	 * @param tableStr
	 * @return
	 */
	public static String getTimeTableNameString(String tableStr) {
		// 得到 大写字母 字符串 YYYYMM,YYYYMM-1,YYYYMMDD,YYYYMMDD-1
		String timeMode = tableStr.substring(tableStr.lastIndexOf("[") + 1, tableStr.lastIndexOf("]")).toUpperCase();
		String timeStrSuffix = "";
		switch (timeMode) {
		case "YYYYMM":
			timeStrSuffix = DateUtil.getCurrent(DateUtil.dateMonthPattern);
			break;
		case "YYYYMM-1":
			timeStrSuffix = DateUtil.parseDate2Str(DateUtil.plusOrAddMonth(new Date(), -1), DateUtil.dateMonthPattern);
			break;
		case "YYYYMMDD":
			timeStrSuffix = DateUtil.getCurrent(DateUtil.datePattern);
			break;
		case "YYYYMMDD-1":
			timeStrSuffix = DateUtil.parseDate2Str(DateUtil.plusDays(-1), DateUtil.datePattern);
			break;
		default:
			break;
		}
		// 得到 tb_
		String tabPrefix = tableStr.substring(0, tableStr.lastIndexOf("["));
		return tabPrefix + timeStrSuffix;
	}

	/**
	 * 去掉首次出现的 where
	 * 
	 * @param oriStr
	 * @param reStr
	 * @return
	 */
	private static String removeStr(String oriStr, String reStr) {
		return oriStr.replaceFirst("[Ww][Hh][Ee][Rr][Ee]", reStr);
	}

	/**
	 * 记录日志
	 * 
	 * @param logDto
	 *            归档任务id
	 * @param msg
	 *            归档结果
	 */
	private void addArchiveLog(DataBaseArchiveLogDto logDto, String msg) {
		log.debug("写入归档日志开始");
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("ARCHIVE_ID", logDto.getArchiveId());
		paramMap.put("BATCH_NUM", logDto.getBatchNum());
		paramMap.put("ARCHIVE_RESULT", msg +"\n批次号："+logDto.getBatchNum()+ "\n==============================\n\n");
		paramMap.put("SOUR_TABLE_NAME", logDto.getSourTableName());
		paramMap.put("DEST_TABLE_NAME", logDto.getDestTableName());
		coreService.insertObject("datasourceArchive.insertArchiveLog", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
		log.debug("写入归档日志结束");
	}
	
	
	/**
	 * 归档实时日志
	 * @param jobId
	 * @param arcSum    归档表总数
	 * @param batchNum  批次号
	 */
	private void insertRealTimeLog(String jobId,int arcSum,String batchNum) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("JOB_ID", jobId);
		paramMap.put("COUNT_ARC_NUM", arcSum);
		paramMap.put("BATCH_NUM", batchNum);
		coreService.insertObject2New("datasourceArchive.insertDataArchRealTimeLog", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
	}
	/**
	 * 归档实时日志
	 * @param jobId
	 * @param isSuccess   归档一次是否成功
	 */
	private void updateRealTimeLog(String jobId,boolean isSuccess) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("JOB_ID", jobId);
		if(isSuccess){
			paramMap.put("SUCCESS_FLG", "Y");
		}
		coreService.insertObject("datasourceArchive.updateDataArchRealTimeLog", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
	}
	/**
	 * 归档实时日志
	 * @param jobId
	 */
	private void updateRealTimeLogEnd(String jobId) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("JOB_ID", jobId);
		coreService.insertObject("datasourceArchive.updateDataArchRealTimeLogEnd", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
	}
	/**
	 * 表记录实时日志
	 */
	private void updateRealTimeRecodeDetailLog(DataBaseArchiveDto dto,int recodeSum,int alreadyTransSum) {
		Map<String, Object> paramMap = new HashMap<String,Object>();
		paramMap.put("JOB_ID", dto.getJobId());
		paramMap.put("ARC_SOUR_TABLE_NOW", dto.getSourTableName());
		paramMap.put("ARC_DEST_TABLE_NOW", dto.getDestTableName());
		paramMap.put("ARC_DATA_RECODE_SUM", recodeSum);
		paramMap.put("ARC_DATA_RECODE_SUCCESS_SUM", alreadyTransSum);
		coreService.insertObject2New("datasourceArchive.updateRealTimeRecodeDetailLog", paramMap, FrameConfigKey.DEFAULT_DATASOURCE);
	}
	
	
	
	/**
	 * 查询数据库类型
	 * 
	 * @param dto
	 * @return
	 */
	private void getDSConfig(DataBaseArchiveDto dto) {
		Map<String, String> params = new HashMap<String, String>();
		// 目标数据库信息
		params.put("DS_ID", dto.getDestDsId());
		Map<String, String> resultMap = coreService.queryForObject("datasourceArchive.queryDSConfig", params,
				FrameConfigKey.DEFAULT_DATASOURCE);
		// 类型 Oracle,MySql
		dto.setDestDsType(StringTool.object2String(resultMap.get("DATASOURCE_TYPE")));
		dto.setDestDsName(StringTool.object2String(resultMap.get("DATASOURCE_NAME")));
		// 源数据信息
		params.put("DS_ID", dto.getSourDsId());
		Map<String, String> resultMap2 = coreService.queryForObject("datasourceArchive.queryDSConfig", params,
				FrameConfigKey.DEFAULT_DATASOURCE);
		// 类型 Oracle,MySql
		dto.setSourDsType(StringTool.object2String(resultMap2.get("DATASOURCE_TYPE")));
		dto.setSourDsName(StringTool.object2String(resultMap2.get("DATASOURCE_NAME")));

	}
	/**
	 * 特殊字符 mybatis 用此方法 
	 * @param str
	 * @return return "<![CDATA[ "+str+" ]]>";
	 */
	private String getCDATAString(String str){
		return "<![CDATA[ "+str+" ]]>";
	}
	/**
	 * 
	 */
	private static String replaceSpecChar(String dataStr,String dbType) {
		if(Constant.DATA_SOURCE_TYPE_MYSQL.equals(dbType)){
			dataStr = dataStr.replace("'", "\\'");
		}else if(Constant.DATA_SOURCE_TYPE_ORACLE.equals(dbType)){
			dataStr = dataStr.replace("'", "''");
			dataStr = dataStr.replace("&","'|| '&' ||'");
		}
		return dataStr;
	}
}
