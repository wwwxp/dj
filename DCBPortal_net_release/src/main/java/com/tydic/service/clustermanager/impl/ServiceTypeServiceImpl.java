package com.tydic.service.clustermanager.impl;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.tydic.bean.FtpDto;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.tools.RSAUtils;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.datasource.InitDataSourceServiceImpl;
import com.tydic.service.clustermanager.ServiceTypeService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.clustermanager.impl]    
  * @ClassName:    [ServiceTypeServiceImpl]     
  * @Description:  [组件集群配置管理]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-15 下午4:10:46]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-15 下午4:10:46]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
@SuppressWarnings("all")
public class ServiceTypeServiceImpl implements ServiceTypeService {
    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(ServiceTypeServiceImpl.class);
    
    //组件参数操作类型
    private static final String DO_CFG_ADD = "add";
    private static final String DO_CFG_EDIT = "edit";
    
    @Resource
    public CoreService coreService;

    /**
     * 增加集群信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> insertServiceType(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception {
        log.debug("增加集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        //集群编码
        String clusterCode = StringTool.object2String(params.get("CLUSTER_CODE"));
        //部署路径
        String clusterDeployPath = StringTool.object2String(params.get("CLUSTER_DEPLOY_PATH")).trim();
        
        //判断当前集群编码是否被使用
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("CLUSTER_CODE", clusterCode);
        List<HashMap<String, Object>> clusterCodeList = coreService.queryForList2New("serviceType.queryPersonalConfByCode", queryMap, dbKey);
        if (!BlankUtil.isBlank(clusterCodeList)) {
        	throw new RuntimeException("集群编码已经存在, 请重新输入！");
        }
        log.debug("校验集群编码成功，集群编码：" + clusterCode);
        
        //判断集群部署路径是否已经存在，如果存在不能新增
        Map<String, Object> queryPathMap = new HashMap<String, Object>();
        queryPathMap.put("CLUSTER_DEPLOY_PATH", clusterDeployPath);
        if (!clusterDeployPath.endsWith("/")) {
        	queryPathMap.put("CLUSTER_DEPLOY_PATH_NEW", clusterDeployPath + "/");
        } else {
        	queryPathMap.put("CLUSTER_DEPLOY_PATH_NEW", clusterDeployPath.substring(0, clusterDeployPath.length() - 1));
        }
        List<HashMap<String, Object>> clusterPathList = coreService.queryForList2New("serviceType.queryServiceTypeByPath", queryPathMap, dbKey);
        if (!BlankUtil.isBlank(clusterPathList)) {
        	//throw new RuntimeException("集群部署路径已经存在, 请重新输入！");
        }
        log.debug("校验集群部署路径成功， 集群编码：" + clusterCode);
        
        //集群类型
        String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
        //添加集群信息
        Map<String, String> addParams = new HashMap<String, String>();
        addParams.put("CLUSTER_CODE", clusterCode);
        addParams.put("CLUSTER_NAME", StringTool.object2String(params.get("CLUSTER_NAME")));
        addParams.put("TARGET_SH_PATH", FileTool.exactPath(Constant.ENV));
        addParams.put("SOURCE_SH_FILE", FileTool.exactPath(Constant.ENV) + clusterType + ".zip");
        addParams.put("TYPE", BusinessConstant.PARAMS_BUS_1);
        addParams.put("STATUS", BusinessConstant.PARAMS_BUS_1);
        addParams.put("SEQ", StringTool.object2String(params.get("SEQ")));
        addParams.put("M2DB_INSTANCE", StringTool.object2String(params.get("M2DB_INSTANCE")));
        addParams.put("CLUSTER_TYPE", clusterType);
        addParams.put("CLUSTER_DEPLOY_PATH", clusterDeployPath);
        coreService.insertObject("serviceType.addServiceType", addParams, dbKey);
        log.debug("添加集群信息成功， 新增集群编码: " + clusterCode);
        
        //添加集群参数
        List paramsList = (List) params.get("PARAMS_LIST");
        if (!BlankUtil.isBlank(paramsList)) {
        	String clusterId = addParams.get("clusterId");
        	for (int i=0; i<paramsList.size(); i++) {
        		Map paramMap = (Map) paramsList.get(i);
        		paramMap.put("CLUSTER_ID", clusterId);
        		
        		//是否密码类型,密码类型会进行加密处理
        		String cfgValue = StringTool.object2String(paramMap.get("CFG_VALUE"));
        		String isPasswd = StringTool.object2String(paramMap.get("CFG_IS_PASSWD"));
        		if (BusinessConstant.PARAMS_BUS_1.equals(isPasswd)) {
        			cfgValue = this.decrypt(request, cfgValue);
        			//对前台解密后的密码进行DesTool加密保存
            		if (!BlankUtil.isBlank(cfgValue) && Constant.NEED_DES_PASSWD) {
            			cfgValue = DesTool.enc(cfgValue);
            		}
            		paramMap.put("CFG_VALUE", cfgValue);
        		}
        	}
        	int addCount = coreService.insertBatchObject("componentsConfig.addBatchConfigList", paramsList, dbKey);
        	log.debug("添加集群参数成功， 当前集群类型: " + clusterType + ", 添加记录行: " + addCount);
        
        	if (Constant.DCA.equalsIgnoreCase(clusterType)) {
        		
        		//判断是否需要加密，如果加了在创建数据源时需要Des解密
        		if (Constant.NEED_DES_PASSWD) {
	        		for (int i=0; i<paramsList.size(); i++) {
	            		Map paramMap = (Map) paramsList.get(i);
	            		//是否密码类型,密码类型会进行加密处理
	            		String cfgValue = StringTool.object2String(paramMap.get("CFG_VALUE"));
	            		String isPasswd = StringTool.object2String(paramMap.get("CFG_IS_PASSWD"));
	            		if (BusinessConstant.PARAMS_BUS_1.equals(isPasswd)) {
	            			//对前台解密后的密码进行DesTool加密保存
	                		if (!BlankUtil.isBlank(cfgValue)) {
	                			cfgValue = DesTool.dec(cfgValue);
	                		}
	                		paramMap.put("CFG_VALUE", cfgValue);
	            		}
	            	}
        		}
            	InitDataSourceServiceImpl serviceImpl = (InitDataSourceServiceImpl) SpringContextUtil.getBean("initDataSource");
            	serviceImpl.addExtDataSource(clusterCode, paramsList);
        	}
        }
        return null;
    }
    

    /**
     * 删除集群信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> deleteServiceType(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("删除集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

        //判断该集群是否已经被部署
        String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
        //集群编码
        String clusterCode = StringTool.object2String(params.get("CLUSTER_CODE"));
        //集群类型
        String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
        //判断业务集群是否已经部署
        List<HashMap<String, Object>> deployHostList = coreService.queryForList2New("deployHome.queryHostByDeploy", params, dbKey);
        if (!BlankUtil.isBlank(deployHostList)) {
        	throw new RuntimeException("该集群已经部署主机, 不能删除！");
        }
        log.debug("校验主机是否部署成功...");
        //获取ftp主机配置
		FtpDto ftpDto = SessionUtil.getFtpParams();
        //删除集群配置文件
		if (!BlankUtil.isBlank(ftpDto)) {

			String delPath = ftpDto.getFtpRootPath() + Constant.CONF + Constant.PLAT_CONF
					+ Constant.RELEASE_DIR + FileTool.exactPath(clusterType) + clusterCode;
			log.debug("删除集群信息，版本发布服务器配置文件路径: " + delPath);
			
			ShellUtils shellUtils = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
			String rstStr =  shellUtils.execMsg("rm -rf " + delPath);
			log.debug("删除版本发布服务器主机配置文件列表，返回结果: " + rstStr);
		} else {
			log.debug("获取版本发布服务器主机信息失败， 集群配置文件未删除， 集群编码: " + clusterCode);
		}
        
        Map<String, String> delParams = new HashMap<String, String>();
        delParams.put("CLUSTER_ID", clusterId);
        coreService.deleteObject("serviceType.delServiceType", delParams, dbKey);
        log.debug("删除集群成功, 集群ID: " + clusterId);
        
        int delCount = coreService.deleteObject("componentsConfig.delComponentsConfigList", delParams, dbKey);
        log.debug("删除集群配置信息成功, 集群ID: " + clusterId + ", 删除记录数: " + delCount);
        
        //DCA动态删除数据源
        if (Constant.DCA.equalsIgnoreCase(clusterType)) {
        	InitDataSourceServiceImpl serviceImpl = (InitDataSourceServiceImpl) SpringContextUtil.getBean("initDataSource");
        	serviceImpl.delExtDataSource(clusterCode);
        }
        
        return null;
    }

    /**
     * 修改集群信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> updateServiceType(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception {
        log.debug("修改集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        //集群ID
        String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
        //集群类型
        String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
        //集群编码
        String clusterCode = StringTool.object2String(params.get("CLUSTER_CODE"));
        
        //查询集群编码是否已经被使用
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("CLUSTER_CODE", clusterCode);
        queryMap.put("CLUSTER_ID", params.get("CLUSTER_ID"));
        List<HashMap<String, Object>> list = coreService.queryForList2New("serviceType.queryServiceTypeForUpdate", queryMap, dbKey);
        if (!BlankUtil.isBlank(list)) {
        	throw new RuntimeException("修改后的集群编码已经被使用，请重新输入！");
        }
        
        //集群名称
        String clusterName = StringTool.object2String(params.get("CLUSTER_NAME"));
        //添加集群信息
        Map<String, String> updateParams = new HashMap<String, String>();
        updateParams.put("CLUSTER_ID", clusterId);
        updateParams.put("CLUSTER_NAME", clusterName);
        updateParams.put("CLUSTER_CODE", clusterCode);
        updateParams.put("SEQ", StringTool.object2String(params.get("SEQ")));
        updateParams.put("M2DB_INSTANCE", StringTool.object2String(params.get("M2DB_INSTANCE")));
        updateParams.put("CLUSTER_DEPLOY_PATH", StringTool.object2String(params.get("CLUSTER_DEPLOY_PATH")));
        coreService.updateObject("serviceType.updateServiceType", updateParams, dbKey);
        log.debug("集群信息修改成功, 集群名称: " + clusterName);
        
        //根据集群ID删除集群参数
        Map<String, String> delParams = new HashMap<String, String>();
        delParams.put("CLUSTER_ID", clusterId);
        coreService.deleteObject("componentsConfig.delComponentsConfigList", delParams, dbKey);
        log.debug("删除集群参数成功， 集群ID: " + clusterId);
        
        //添加集群参数
        List paramsList = (List) params.get("PARAMS_LIST");
        if (!BlankUtil.isBlank(paramsList)) {
        	for (int i=0; i<paramsList.size(); i++) {
        		Map paramMap = (Map) paramsList.get(i);
        		paramMap.put("CLUSTER_ID", clusterId);
        		//是否密码类型,密码类型会进行加密处理
        		String cfgValue = StringTool.object2String(paramMap.get("CFG_VALUE"));
        		String isPasswd = StringTool.object2String(paramMap.get("CFG_IS_PASSWD"));
        		if (BusinessConstant.PARAMS_BUS_1.equals(isPasswd)) {
        			cfgValue = this.decrypt(request, cfgValue);
        			//对前台解密后的密码进行DesTool加密保存
            		if (!BlankUtil.isBlank(cfgValue) && Constant.NEED_DES_PASSWD) {
            			cfgValue = DesTool.enc(cfgValue);
            		}
            		paramMap.put("CFG_VALUE", cfgValue);
        		}
        	}
        	int addCount = coreService.insertBatchObject("componentsConfig.addBatchConfigList", paramsList, dbKey);
        	log.debug("添加集群参数成功， 当前集群类型: " + clusterType + ", 添加记录行: " + addCount);
        
        	//更新组件数据源
        	if (Constant.DCA.equalsIgnoreCase(clusterType)) {
        		//判断是否需要加密，如果加了在创建数据源时需要Des解密
        		if (Constant.NEED_DES_PASSWD) {
	        		for (int i=0; i<paramsList.size(); i++) {
	            		Map paramMap = (Map) paramsList.get(i);
	            		//是否密码类型,密码类型会进行加密处理
	            		String cfgValue = StringTool.object2String(paramMap.get("CFG_VALUE"));
	            		String isPasswd = StringTool.object2String(paramMap.get("CFG_IS_PASSWD"));
	            		if (BusinessConstant.PARAMS_BUS_1.equals(isPasswd)) {
	            			//对前台解密后的密码进行DesTool加密保存
	                		if (!BlankUtil.isBlank(cfgValue)) {
	                			cfgValue = DesTool.dec(cfgValue);
	                		}
	                		paramMap.put("CFG_VALUE", cfgValue);
	            		}
	            	}
        		}
        		InitDataSourceServiceImpl serviceImpl = (InitDataSourceServiceImpl) SpringContextUtil.getBean("initDataSource");
            	serviceImpl.updateExtDataSource(clusterCode, paramsList);
        	}
        }
        return null;
    }


    /**
     * 查询集群参数信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
	@Override
	public List<HashMap<String, Object>> queryComponentsParams(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查询集群参数信息， 业务参数: " + params + ", dbKey: " + dbKey);
		//操作类型，如果是新增则根据集群类型查询模板表，否则查询配置表
		String doCfg = StringTool.object2String(params.get("DO_CFG"));
		
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", params.get("CLUSTER_TYPE"));
		List<HashMap<String, Object>> rstList = coreService.queryForList2New("componentsConfig.queryComponentsConfigTemplateList", queryParams, dbKey);
		
		if (DO_CFG_EDIT.equals(doCfg)) {
			queryParams.put("CLUSTER_ID", params.get("CLUSTER_ID"));
			List<HashMap<String, Object>> cfgList = coreService.queryForList2New("componentsConfig.queryComponentConfigList", queryParams, dbKey);
			
			//判断参数是否存在，如果不存在读取默认模板中的参数， 取两个集合并值作为返回值
			if (!BlankUtil.isBlank(cfgList) && !BlankUtil.isBlank(rstList)) {
				int rstLen = rstList.size();
				int cfgLen = cfgList.size();
				for (int i = 0; i<rstLen; i++) {
					String cfgType = StringTool.object2String(rstList.get(i).get("CFG_TYPE"));
					String cfgCode = StringTool.object2String(rstList.get(i).get("CFG_CODE"));
					
					boolean isExists = false;
					for (int j=0; j<cfgLen; j++) {
						String currCfgType = StringTool.object2String(cfgList.get(j).get("CFG_TYPE"));
						String currCfgCode = StringTool.object2String(cfgList.get(j).get("CFG_CODE"));
						if (cfgType.equals(currCfgType) && cfgCode.equals(currCfgCode)) {
							isExists = true;
							break;
						}
					}
					if (!isExists) {
						cfgList.add(rstList.get(i));
					}
				}
			}
			rstList = cfgList;
		}
		
		rstList = rstList == null ? new ArrayList<HashMap<String, Object>>() : rstList;
		//对密码类型配置项进行界面处理
		if (Constant.NEED_DES_PASSWD) {
			for(int i=0; i<rstList.size(); i++) {
				String isPasswd = StringTool.object2String(rstList.get(i).get("CFG_IS_PASSWD"));
				if (BusinessConstant.PARAMS_BUS_1.equals(isPasswd)) {
					String cfgValue = StringTool.object2String(rstList.get(i).get("CFG_VALUE"));
					if (!BlankUtil.isBlank(cfgValue)) {
						rstList.get(i).put("CFG_VALUE", DesTool.dec(cfgValue));
					}
				}
			}
		}
				
		log.debug("查询集群参数信息成功， 返回参数信息: " + (rstList.toString()));
		return rstList;
	}

    /**
     * RSA解密
     * @param request
     * @param ciphertext
     * @return
     */
    public String decrypt(HttpServletRequest request,String ciphertext){
     	 ServletContext cxt=request.getSession().getServletContext();
    	 Map<String,BigInteger> RSAMap=(Map<String,BigInteger>)cxt.getAttribute("RSAMap");
    	 BigInteger modulus = RSAMap.get("modulus");
    	 BigInteger private_exponent = RSAMap.get("private_exponent");
    	 RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus.toString(), private_exponent.toString());
    	 String text=null;
		try {
			text = RSAUtils.decryptByPrivateKey(ciphertext, priKey);
		} catch (Exception e) {
			 log.error(ciphertext+"，解密失败！："+e);
			 throw new RuntimeException(ciphertext+"，解密失败！"+e.getMessage());
			 
		}
    	 
        log.debug("密文："+ciphertext+",解密成功");
        return text;
    }
}
