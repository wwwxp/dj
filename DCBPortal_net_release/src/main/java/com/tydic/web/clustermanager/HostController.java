package com.tydic.web.clustermanager;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.ssh.model.HostSystem;
import com.tydic.bp.ssh.web.SSHCommander;
import com.tydic.service.clustermanager.HostService;
import com.tydic.util.SSHRemoteCmdUtil;
import com.tydic.util.StringTool;

@Controller
@RequestMapping("/host")
public class HostController extends BaseController {
    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(HostController.class);

    /**
     * 主机操作Service
     */
    @Autowired
    private HostService hostService;

    /**
     * 核心Service
     */
    @Autowired
    private CoreService coreService;

    /**
     * 删除主机
     * @param request
     * @return
     */
    @RequestMapping(value="/delete",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String deleteHost(HttpServletRequest request) {
        log.debug("删除主机信息开始...");
        String result = null;
        try {
            hostService.deleteHost(getParamsList(request), getDbKey(request));
        } catch (Exception e) {
            log.error("删除主机信息失败， 失败原因:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("删除主机信息结束...");
        return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, result));
    }

    /**
     * 新增对象
     * @param request
     * @return
     */
    @RequestMapping(value="/insertHost",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String insertHost(HttpServletRequest request) {
        log.debug("新增主机信息开始...");
        try {
            hostService.insertHost(this.getParamsMap(request), getDbKey(request));
        } catch (Exception e) {
            log.error("新增主机信息失败， 失败信息: ",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("新增主机信息结束...");
        return null;
    }
    /**
     * 修改对象
     * @param request
     * @return
     */
    @RequestMapping(value="/updateHost",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateHost(HttpServletRequest request) {
        log.debug("修改主机信息开始...");
        try {
            hostService.updateHost(this.getParamsMap(request), getDbKey(request));
        } catch (Exception e) {
            log.error("修改主机信息异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("修改主机信息结束...");
        return null;
    }

    /**
     * 批量修改主机密码
     * @param request
     * @return
     */
    @RequestMapping(value="/updatePasswdBatch",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updatePasswdBatch(HttpServletRequest request) {
        log.debug("批量修改主机密码开始...");
        try {
            Map<String, Object> retMap = hostService.updatePasswdBatch(this.getParamsMapByObject(request), getDbKey(request));
            log.debug("批量修改主机密码结束,返回结果：" + retMap.toString());
            return JSON.Encode(retMap);
        } catch (Exception e) {
            log.error("批量修改主机密码异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


    /**
     * 查询对象
     * @param request
     * @return
     */
    @RequestMapping(value="/getHostInfo",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getHostInfo(HttpServletRequest request) {
        log.debug("查询主机信息开始...");
        try {
            Map<String,String> result = hostService.queryHostInfo(this.getParamsMap(request), getDbKey(request));
            log.debug("查询主机信息结束");
            return JSON.Encode(result);
        } catch (Exception e) {
            log.error("查询主机信息异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * sftp登录测试
     * @param request
     * @return
     */
    @RequestMapping(value="/loginTest",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String multiLoginTest(HttpServletRequest request) {
        log.debug("主机SSH测试开始...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            // 获取请求参数
            Map<String, Object> map = this.getParamsMapByObject(request);
            String userName = StringTool.object2String(map.get("SSH_USER"));
            String password = (DesTool.dec(StringTool.object2String(map.get("SSH_PASSWD"))));
            String host = StringTool.object2String(map.get("HOST_IP"));
            log.debug("主机SSH测试， 主机信息: IP:" + host + ", userName: " + userName + ", password: " + password);

            // 登录
            SSHRemoteCmdUtil cmdUtil = new SSHRemoteCmdUtil(host, userName, password, null);
            Boolean flag = cmdUtil.login();
            if(flag){
                resultMap.put("flag", "1");
            }
        } catch (Exception e) {
            log.error("主机SSH测试失败， 失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "ssh连接测试失败！"));
        }
        log.debug("主机SSH测试结束...");
        return JSON.Encode(resultMap);
    }

    /**
     * 终端操作
     * 如果通过代理打开终端，需要在代理中添加下面配置:
     * proxy_set_header        Upgrade $http_upgrade;
     * proxy_set_header        Connection "upgrade";
     *
     * @param request
     * @return
     */
    @RequestMapping(method=RequestMethod.POST, value = "/terminal")
    public String insertCompute(HttpServletRequest request, @RequestParam("termialHost") String host) {
        log.debug("跳转到终端测试, 当前主机列表 ---> " + host);
        List<HostSystem> hosts = new ArrayList<HostSystem>();

        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("HOST_IDS", host);
        List<HashMap<String, Object>> hostList = coreService.queryForList2New("host.queryHostForTermial", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
        if (!BlankUtil.isBlank(hostList)) {
            for (int i=0; i<hostList.size(); i++) {
                HostSystem hostSystem = new HostSystem();
                String hostIp = StringTool.object2String(hostList.get(i).get("HOST_IP"));
                String hostUserName = StringTool.object2String(hostList.get(i).get("SSH_USER"));
                String hostPassword = StringTool.object2String(hostList.get(i).get("SSH_PASSWD"));
                hostSystem.setHost(hostIp);
                hostSystem.setUser(hostUserName);
                hostSystem.setPassword(DesTool.dec(hostPassword));
                hosts.add(hostSystem);
            }
        }
        log.debug("主机终端连接， 主机信息: " + hosts.toString());
        String cmd = request.getParameter("termialCmd");
        String forward = null;
        if(StringUtils.isNotBlank(cmd)){
            forward = SSHCommander.openSSHTermOnSystem(request, hosts,cmd);
        }else{
            forward = SSHCommander.openSSHTermOnSystem(request, hosts);
        }

        log.debug("终端跳转页面 ---> " + forward);
        return forward;
    }

    /**
     * excel主机模板信息导入
     * @param uFile
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/importFromExcel", produces = {"application/json;charset=UTF-8"})
    public String exportHostInfoToExcel(@RequestParam MultipartFile uFile, HttpServletRequest request,HttpServletResponse response) throws Exception {
        log.debug("批量导入主机模板信息 ---> " + request);
        Map<String, Object> queryParams = new HashMap<String, Object>();
        PrintWriter out =response.getWriter();
        try {
            //获取页面传递的其他信息
            Map formMap = this.getParams("FILE_NAME,uploadType,FILE_TYPE", request);
            Map<String,Object> list = hostService.queryHostInfoForExcel(uFile,request, this.getParamsMap(request), getDbKey(request));
            log.debug("批量导入主机信息完成！");
            out.println(JSON.Encode(list));

        } catch (Exception e) {
            log.error("查询主机信息异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }finally {
            out.flush();
            out.close();
        }
        return null ;
    }

    /**
     * 导出模板实现方法
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/downloadExcel")
    @ResponseBody
    public String downloadExcelTemplate(HttpServletRequest request, HttpServletResponse response) {
        log.debug("下载excel模板 ---> " + request);
        String templatePath = request.getServletContext().getRealPath("/") + "WEB-INF/excelfile/" +"template.xlsx";
        File file = new File(templatePath);
        String fileName = file.getName();
        try {
            InputStream input = new BufferedInputStream(new FileInputStream(templatePath));
            byte[] buffers = new byte[input.available()];
            response.addHeader("Content-Disposition", "attachment;filename=" + new String((fileName).getBytes("GBK"), "ISO-8859-1"));
            response.setContentType("application/vnd.ms-excel");
            FileInputStream inStream = new FileInputStream(templatePath);
            while (inStream.read(buffers) > 0) {
                response.getOutputStream().write(buffers);
            }
            response.getOutputStream().close();
            inStream.close();
        } catch (FileNotFoundException e) {
            log.error("导出模板异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        } catch (IOException e) {
            log.error("导出模板异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("下载excel模板结束！");
        return null;
    }

}
