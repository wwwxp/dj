package com.tydic.dcm.util.jdbc;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tydic.bp.common.utils.tools.CommonTool;
import com.tydic.bp.core.utils.db.DbContextHolder;
import com.tydic.dcm.util.spring.SpringUtil;

/**
 * 
 * @ClassName: JdbcUtil
 * @Description: 进行JDBC数据库操作帮助类
 * @Prject: dcm-server_base
 * @author: yuanH
 * @date 2016年7月28日 下午2:37:58
 */
public class JdbcUtil {

	/**
     * log4j日志对象
     */
    private static Logger log = Logger.getLogger(JdbcUtil.class);
	
	/**
	 * 查询List集合
	 * @param execKey
	 * @param params
	 * @param dbKey
	 * @return
	 */
	public static List<Map<String, Object>> queryForList(String execKey,Map<String, Object> params, String dbKey) {
		DbContextHolder.clearDbType();
        log.debug("jdbcUtil, 查询多条记录, 执行sql语句Key值 ---> " + execKey + ", 数据源Key ---> " + dbKey);
        log.debug("jdbcUtil, 查询多条记录, 查询参数 ---> "+CommonTool.printParams(params));
        DbContextHolder.setDbType(dbKey);
		List<Map<String, Object>> resultData = SpringUtil.getCoreBaseDao().getSqlSession().selectList(execKey, params);
		return resultData;
	}
	
    /**
     * 查询单行记录
     * @param execKey
     * @param params
     * @param dbKey
     * @return
     */
    public static Map<String, Object> queryForObject(String execKey,Map<String, Object> params, String dbKey) {
        DbContextHolder.clearDbType();
        log.debug("jdbcUtil, 查询单条记录, 执行sql语句Key值 ---> "+execKey + ", 数据源Key ---> " + dbKey);
        log.debug("jdbcUtil, 查询单条记录, 查询参数 ---> "+CommonTool.printParams(params));
        DbContextHolder.setDbType(dbKey);
        Map<String, Object> resultdata = SpringUtil.getCoreBaseDao().getSqlSession().selectOne(execKey,params);
        return resultdata;
    }
	
    /**
     * jdbcUtil，插入对象
     * @param execKey
     * @param dbKey
     * @return
     */
    public static int insertObject(String execKey, Map<String, Object> params, String dbKey) {
        DbContextHolder.clearDbType();
        log.debug("jdbcUtil, 添加数据, 执行sql语句Key值 ---> " + execKey + ", 数据源Key ---> " + dbKey);
        log.debug("jdbcUtil, 添加数据, 执行参数 ---> " +CommonTool.printParams(params));
        DbContextHolder.setDbType(dbKey);
        int affectedRow = SpringUtil.getCoreBaseDao().getSqlSession().insert(execKey, params);
        return affectedRow;
    }
	
    /**
     * jdbcUtil，更新对象
     * @param execKey
     * @param dbKey
     * @return
     */
    public static int updateObject(String execKey, Map<String, Object> params,String dbKey) {
        DbContextHolder.clearDbType();
        log.debug("jdbcUtil, 修改数据, 执行sql语句Key值 ---> " + execKey + ", 数据源Key ---> " + dbKey);
        log.debug("jdbcUtil, 修改数据, 执行参数 ---> "+CommonTool.printParams(params));
        DbContextHolder.setDbType(dbKey);
        int affectedRow = SpringUtil.getCoreBaseDao().getSqlSession().update(execKey, params);
        return affectedRow;
    }
    
    /**
     * jdbcUtil，删除对象
     * @param execKey
     * @param dbKey
     * @return
     */
    public static int deleteObject(String execKey, Map<String, Object> params,String dbKey) {
        DbContextHolder.clearDbType();
        log.debug("jdbcUtil, 修改数据, 执行sql语句Key值 ---> " + execKey + ", 数据源Key ---> " + dbKey);
        log.debug("jdbcUtil, 修改数据, 执行参数 ---> "+CommonTool.printParams(params));
        DbContextHolder.setDbType(dbKey);
        int affectedRow = SpringUtil.getCoreBaseDao().getSqlSession().delete(execKey, params);
        return affectedRow;
    }
}
