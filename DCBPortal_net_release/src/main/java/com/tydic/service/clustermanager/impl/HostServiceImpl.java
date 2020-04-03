package com.tydic.service.clustermanager.impl;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.clustermanager.HostService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.StringTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple to Introduction
 *
 * @ProjectName: [DCBPortal_net_release]
 * @Package: [com.tydic.service.clustermanager.impl]
 * @ClassName: [HostServiceImpl]
 * @Description: [主机管理实现操作类]
 * @Author: [Yuanh]
 * @CreateDate: [2017-6-13 下午5:17:02]
 * @UpdateUser: [Yuanh]
 * @UpdateDate: [2017-6-13 下午5:17:02]
 * @UpdateRemark: [说明本次修改内容]
 * @Version: [v1.0]
 */
@Service
@SuppressWarnings("all")
public class HostServiceImpl implements HostService {
    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(HostServiceImpl.class);

    @Resource
    public CoreService coreService;

    /**
     * 删除主机信息
     *
     * @param paramsList 业务参数
     * @param dbKey      数据库Key
     * @return void
     */
    @Override
    public void deleteHost(List<Map<String, String>> paramsList, String dbKey) throws Exception {
        log.debug("删除主机信息， 业务参数: " + paramsList.toString() + ", dbKey: " + dbKey);
        try {
            coreService.deleteObject("host.delHost", paramsList, dbKey);
            log.debug("添加主机信息成功...");
            coreService.deleteObject("deployHome.deleteDeployHostByHostId", paramsList, dbKey);
            log.debug("删除主机部署信息成功...");
        } catch (Exception e) {
            log.error("添加主机信息失败， 失败原因: ", e);
            throw new RuntimeException("删除主机失败 :" + e.getMessage());
        }
    }

    /**
     * 添加主机信息
     *
     * @param param 业务参数
     * @param dbKey 数据库Key
     * @return void
     */
    @Override
    public void insertHost(Map<String, String> param, String dbKey) throws Exception {
        log.debug("添加主机信息， 业务参数: " + param.toString() + ", dbKey: " + dbKey);
        param.put("FLAG", "insert");
        validate(param, dbKey);
        log.debug("添加主机信息， 参数后台校验通过...");

        param.put("SSH_PASSWD", DesTool.enc(param.get("SSH_PASSWD")));

        //对网卡进行自动添加%
        String hostNetCard = param.get("HOST_NET_CARD");
        if (StringUtils.isNotBlank(hostNetCard)) {
            if (!StringUtils.startsWith(hostNetCard, "%")) {
                hostNetCard = "%" + hostNetCard;
            }
            param.put("HOST_NET_CARD", hostNetCard);
        }
        coreService.insertObject("host.insertHost", param, dbKey);

    }

    /**
     * 修改主机信息
     *
     * @param param 业务参数
     * @param dbKey 数据库Key
     * @return void
     */
    @Override
    public void updateHost(Map<String, String> param, String dbKey) throws Exception {
        log.debug("修改主机信息， 业务参数: " + param.toString() + ", dbKey: " + dbKey);
        param.put("FLAG", "update");
        validate(param, dbKey);
        log.debug("修改主机信息， 参数后台校验通过...");

        param.put("SSH_PASSWD", DesTool.enc(param.get("SSH_PASSWD")));
        //对网卡进行自动添加%
        String hostNetCard = param.get("HOST_NET_CARD");
        if (StringUtils.isNotBlank(hostNetCard)) {
            if (!StringUtils.startsWith(hostNetCard, "%")) {
                hostNetCard = "%" + hostNetCard;
            }
            param.put("HOST_NET_CARD", hostNetCard);
        }
        coreService.updateObject("host.updateHost", param, dbKey);
    }

    /**
     * 批量修改主机密码
     *
     * @param param 业务参数
     * @param dbKey 数据库Key
     * @return void
     */
    @Override
    public Map<String, Object> updatePasswdBatch(Map<String, Object> param, String dbKey) throws Exception {
        log.debug("批量修改主机密码， 业务参数: " + param.toString() + ", dbKey: " + dbKey);
        param.put("SSH_PASSWD", DesTool.enc(StringTool.object2String(param.get("SSH_PASSWD"))));
        List<Map<String, Object>> hostList = (List<Map<String,Object>>) param.get("DATA");
        StringBuffer hostIdBuffer  = new StringBuffer();

        Map<String, Object> retMap = new HashMap<String, Object>(){{
            put("RST_CODE", "1");
        }};
        String hostIdStr = "";
        if (CollectionUtils.isNotEmpty(hostList)) {
            for (int i=0; i<hostList.size(); i++) {
                hostIdBuffer.append("'").append(hostList.get(i).get("HOST_ID")).append("'").append(",");
            }
            if (StringUtils.isNotBlank(hostIdBuffer)) {
                hostIdStr = hostIdBuffer.toString().substring(0, hostIdBuffer.toString().length() - 1);
                log.info("批量修改密码，主机ID:" + hostIdStr);
                param.put("HOST_ID", hostIdStr);
            }
        }
        if (StringUtils.isBlank(hostIdStr)) {
            throw new Exception("请勾选需要批量修改密码的主机列表！");
        }
        coreService.updateObject2New("host.updateHostPasswdBatch", param, dbKey);
        log.debug("批量修改主机密码成功...");
        return retMap;
    }

    /**
     * 查询主机信息
     *
     * @param param 业务参数
     * @param dbKey 数据库Key
     * @return Map
     */
    @Override
    public Map<String, String> queryHostInfo(Map<String, String> param, String dbKey) throws Exception {
        log.debug("查询主机信息， 业务参数: " + param.toString() + ", dbKey: " + dbKey);

        Map<String, String> result = coreService.queryForObject("host.queryHostList", param, dbKey);
        if (!BlankUtil.isBlank(result) && !result.isEmpty()) {
            result.put("SSH_PASSWD", DesTool.dec(result.get("SSH_PASSWD")));
        }
        log.debug("查询主机信息， 查询结果: " + result == null ? "" : result.toString());
        return result;
    }

    /**
     * 批量导入excel信息
     *
     * @param param
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> queryHostInfoForExcel(@RequestParam("file") MultipartFile uFile, HttpServletRequest request, Map<String, String> param, String dbKey) throws Exception {
        log.debug("查询主机信息， 业务参数: " + param.toString() + ", dbKey: " + dbKey);

        Map<String, Object> queryParams = new HashMap<String, Object>();
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, String>> insertList = new ArrayList();
        List<Map<String, String>> errorList = new ArrayList();

        File file = null;
        String importSavePath = null;
        try {
            ExcelManage excelManage = new ExcelManage();
            String currTime = DateUtil.getCurrent(DateUtil.datePattern) + System.nanoTime();
            importSavePath = request.getServletContext().getRealPath("/") + "WEB-INF/excelfile/" + currTime + "/";
            file = new File(importSavePath);
            if (!file.exists()) {
                boolean createOk = file.mkdirs();
                log.info("目录创建成功，目录: " + importSavePath);
            }
            String webContextPath = importSavePath + "/" + uFile.getOriginalFilename();
            uFile.transferTo(new File(webContextPath));//预存文件到指定目录

            List<Map<String, Object>> list = excelManage.readFromExcel(webContextPath, "Sheet1");
            log.debug("Excel文件中共有记录 ：" + (list == null ? 0 : list.size()));

            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                Map<String, String> obj = (Map<String, String>) iterator.next();
                if (StringUtils.isBlank(obj.get("HOST_IP")) || StringUtils.isBlank(obj.get("HOST_NAME"))) {
                    log.warn("当前记录是空记录行,无法导入，过滤!");
                    continue;
                }

                //校验主机是否在dcf_hosts中存在，如果存在不需要添加
                Map rows = coreService.queryForObject("host.queryMuitlCode", obj, dbKey);
                int sum = ((Long) rows.get("SUM")).intValue();
                if (sum > 0) {
                    obj.put("FAIL_MSG", "该主机信息已经存在!");
                    log.debug("当前主机在表中已经存在,不能添加， 主机信息: " + obj.toString());
                    errorList.add(obj);
                    continue;
                }
                //判断excel文件中是否存在相同记录数据，如果存在相同记录数据，则过滤不添加
                boolean isExists = false;
                for (int i = 0; i < insertList.size(); i++) {
                    String hostIp = insertList.get(i).get("HOST_IP");
                    String hostName = insertList.get(i).get("HOST_NAME");
                    String sshUser = insertList.get(i).get("SSH_USER");
                    if (hostIp.equals(obj.get("HOST_IP")) && sshUser.equals(obj.get("SSH_USER"))) {
                        obj.put("FAIL_MSG", "该主机信息已经存在!");
                        log.debug("当前主机导入重复,不能添加， 主机信息: " + insertList.get(i).toString());
                        errorList.add(obj);
                        isExists = true;
                        break;
                    }
                }

                if (!isExists) {
                    if (validateExcelDate(obj) > 0) {
                        insertList.add(obj);
                    } else {
                        log.debug("当前主机数据格式校验不通过， 主机信息： " + obj.toString());
                        obj.put("FAIL_MSG", "数据格式存在问题!");
                        errorList.add(obj);
                    }
                }
            }

            //正确导入的数据
            if (!BlankUtil.isBlank(insertList)) {
                for (int i = 0; i < insertList.size(); i++) {
                    Map<String, String> addParam = insertList.get(i);
                    //对网卡进行自动添加%
                    String hostNetCard = addParam.get("HOST_NET_CARD");
                    if (StringUtils.isNotBlank(hostNetCard)) {
                        if (!StringUtils.startsWith(hostNetCard, "%")) {
                            hostNetCard = "%" + hostNetCard;
                        }
                        addParam.put("HOST_NET_CARD", hostNetCard);
                    }
                }
                coreService.insertBatchObject("host.insertHostList", insertList, dbKey);
                log.debug("导入主机信息成功，成功记录数： " + insertList.size());
                result.put("retCode", BusinessConstant.PARAMS_BUS_1);
            }
        } catch (Exception e) {
            log.error("文件导入失败， 失败原因: ", e);
            result.put("retCode", BusinessConstant.PARAMS_BUS_0);
        } finally {
            //将文件删除
            if (file != null && StringUtils.isNotBlank(importSavePath) && file.exists()) {
                file.delete();
                log.debug("文件导入临时目录删除， 删除目录: " + importSavePath);
            }
        }
        
        result.put("SUCESS_RESULT", insertList);
        result.put("ERROR_RESULT", errorList);
        log.debug("批量导入主机信息结束， 其中成功导入：" + insertList.size() + " 条， 失败：" + errorList.size() + " 条");
        return result;
    }

    /**
     * 数据库验证是否重复
     *
     * @param param
     * @param dbKey
     * @throws Exception
     */
    private void validate(Map<String, String> param, String dbKey) throws Exception {
        log.debug("校验主机信息， 业务参数: " + param.toString() + ", dbKey: " + dbKey);

        Map rows = coreService.queryForObject("host.queryMuitlCode", param, dbKey);
        int sum = ((Long) rows.get("SUM")).intValue();
        if (sum > 0) {
            throw new Exception("主机名、主机IP、SSH用户同时存在相同记录，请重新输入！");
        }
    }


    /**
     * 校验excel中的数据
     *
     * @param map
     * @return
     */
    private int validateExcelDate(Map<String, String> map) {
        int result = 1;
        String hostIp = StringTool.object2String(map.get("HOST_IP"));
        String hostName = StringTool.object2String(map.get("HOST_NAME"));
        String sshPort = StringTool.object2String(map.get("SSH_PORT"));
        String sshUser = StringTool.object2String(map.get("SSH_USER"));
        String sshPasswd = StringTool.object2String(map.get("SSH_PASSWD"));
        String coreCount = StringTool.object2String(map.get("CORE_COUNT"));
        String memSize = StringTool.object2String(map.get("MEM_SIZE"));
        String storeSize = StringTool.object2String(map.get("STORE_SIZE"));
        //!validateIp(hostIp) ||
        if(!validateSshPort(sshPort) || !validateHostNameAndSshUser(hostName) ||!validateHostNameAndSshUser(sshUser)||
                !validateSshpasswd(sshPasswd) || !validateCoreCountAndMemAndStore(coreCount)){
            result = -1;
        }
        log.debug("导入主机数据格式校驗， 返回值: " + result + "， 主机信息： " + map.toString());
        return result;
    }

    /**
     * 校验主机IP是否合法
     * @param str
     * @return
     */
    public boolean validateIp(String str){
        if(BlankUtil.isBlank(str) || str.length() < 7 || str.length() > 15){
            return false;
        }
        String regex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern p = Pattern.compile(regex);
        Matcher mat = p.matcher(str);
        return mat.find();
    }

    /**
     * 校验hostName和sshUser
     * @param str
     * @return
     */
    public boolean validateHostNameAndSshUser(String str){
        //需要校验空格
        if (BlankUtil.isBlank(str)) {
            return false;
        }
        return true;//默认返回为真
    }

	/**
	 * 校验sshPort
	 * 
	 * @param str
	 * @return
	 */
	public boolean validateSshPort(String str) {
		boolean flag = true;
		if (BlankUtil.isBlank(str)) {
			flag = false;
		} else {
			try {
				// int num = Integer.valueOf(str);
				double num = Double.valueOf(str);
				if (num < 0) {
					flag = false;
				} else {
					flag = true;// 非负，且在范围之内
				}
			} catch (Exception e) {
				flag = false;
				log.debug("批量模板信息导入，参数校验失败", e);
			}

		}
		return flag;
	}

    /**
     * 密码校验，密码需要加密吧？
     * @param str
     * @return
     */
    public boolean validateSshpasswd(String str) {
        if (BlankUtil.isBlank(str)) {
            return false;
        }
        return true;
    }

    /**
     * 校验coreCount和memStore
     * @param str
     * @return
     */
    public boolean validateCoreCountAndMemAndStore(String str){
        boolean flag = true;
        if (BlankUtil.isBlank(str)) {
            flag = false;
        }else{
            try{
                //int num = Integer.valueOf(str);
                double num = Double.valueOf(str);
                if(num < 0){
                    flag = false;
                }else{
                    flag = true;//非负，且在范围之内
                }
            }catch (Exception e){
                flag = false;
                log.error("批量模板信息导入，参数校验失败", e);
            }
        }
        return flag;
    }

}
