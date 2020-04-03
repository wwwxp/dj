package com.tydic.service.versiondeployment.util;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.CommonTool;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.versiondeployment.bean.VersionInfoEnty;
import com.tydic.util.FileUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeVerUtil {

    private static Logger logger = LoggerFactory.getLogger(NodeVerUtil.class);


    public final static ExecutorService executorService = new ThreadPoolExecutor(10, 50,
            10L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024));


    /**
     * 获取配置文件夹的目录，默认为config
     * @return
     */
    public static String getCfgPathSufx() {
        return  CommonTool.defaultStr(SystemProperty.getContextProperty("node.manager.conf_dir"), "config");
    }


    /**
     * 获取配置文件夹的目录，默认为config
     * @return
     */
    public static String getTomcatTempPath() {
        return  CommonTool.defaultStr(SystemProperty.getContextProperty("node.manager.tomcat_temp_first_path"), "tomcat-temp");
    }


    /**
     *  获取 web程序的   包名前缀， tomcat-temp/webapps/${webAppContentPath}  中的 ${webAppContentPath} 即程序路径
     * @param fileName
     * @return
     */
    public static String fileNameRemoveSuffix(String fileName){
        String lowFname = fileName.toLowerCase();

        if (lowFname.endsWith(".tar.gz")) {

            fileName = fileName.substring(0,fileName.lastIndexOf("."));
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        } else if (lowFname.endsWith(".zip")) {

            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        } else if (lowFname.endsWith(".war")) {

            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        } else {

            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        }
        return fileName;
    }
    //版本包上传路径 后缀
    public static String getRemoteDeployDir(){
        String deploy_dir = SystemProperty.getContextProperty("node.manager.deploy_dir");
        deploy_dir = BlankUtil.isBlank(deploy_dir) ? "node_data" : deploy_dir;
        return deploy_dir;
    }
    /**
     * 需要查数据库
     * pkg包放置的远程路径
     * {版本发布服务器路径}/{程序编码}/{版本}/上传的包
     * @param nodeTypeCode
     * @param versionNum
     * @return
     */
    public static String getRemotePkgStorePath(String nodeTypeCode, String versionNum) {
        FtpDto ftpDto = SessionUtil.getFtpParams();
       return getRemotePkgStorePath(ftpDto.getFtpRootPath(),  nodeTypeCode, versionNum);
    }

        /**
         * pkg包放置的远程路径
         * {版本发布服务器根目录}/{node.manager.deploy_dir}/{程序编码}/{版本}/    放置    上传的包
         * @param ftpRootPath
         * @param nodeTypeCode
         * @param versionNum
         * @return
         */
    public static String getRemotePkgStorePath(String ftpRootPath, String nodeTypeCode, String versionNum) {

        if (isSomeBlank(ftpRootPath, nodeTypeCode, versionNum)) {
            throw new IllegalArgumentException("pkg包放置的远程路径,参数异常，请检查配置");
        }
        return new StringBuffer()
                .append(FileUtil.exactPath(ftpRootPath))
                .append(getRemoteDeployDir())
                .append("/").append(nodeTypeCode)
                .append("/").append(versionNum)
                .append("/")
                .toString();
    }

    /**
     *  暂时只有web用
     * release目录，即web全量叠加包的路径
     *  {版本发布服务器根目录}/release/{程序编码}/{版本}/    放置    全量的程序包
     * @param ftpRootPath
     * @param nodeTypeCode
     * @param versionNum
     * @return
     */
    public static String getRemoteReleaseFullPkgStorePath(String ftpRootPath, String nodeTypeCode, String versionNum) {
        if (isSomeBlank(ftpRootPath, nodeTypeCode, versionNum)) {
            throw new IllegalArgumentException("pkg包放置的远程路径,参数异常，请检查配置");
        }
        return new StringBuffer()
                .append(FileUtil.exactPath(ftpRootPath))
                .append("release")
                .append("/").append(nodeTypeCode)
                .append("/").append(versionNum)
                .append("/")
                .toString();
    }

    /**
     *      * 需要查数据库
     *  暂时只有web用
     * release目录，即web全量叠加包的路径
     *  {版本发布服务器路径}/release/{程序编码}/{版本}/全量的程序包
     * @param nodeTypeCode
     * @param versionNum
     * @return
     */
    public static String getRemoteReleaseFullPkgStorePath(String nodeTypeCode, String versionNum) {
        FtpDto ftpDto = SessionUtil.getFtpParams();
        return  getRemoteReleaseFullPkgStorePath(ftpDto.getFtpRootPath(),nodeTypeCode,versionNum);
    }


    /**
     * 至少一个为 null或者为""  返回true
     * @param sss
     * @return
     */
    public static boolean isSomeBlank(String... sss) {
        if (sss == null) {
            return true;
        }
        for (String s : sss) {
            if (BlankUtil.isBlank(s)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 对List<Map<String,Object>>按version进行降序排序
     * @param list
     */
    public static void sortDeployVersion(List<Map<String, Object>> list) {
        if (BlankUtil.isBlank(list)) {
            return;
        }
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> depMap1, Map<String, Object> depMap2) {
                try {
                    String version1 = StringUtils.defaultString(StringTool.object2String(depMap1.get("VERSION")), "1.0.0");
                    String version2 = StringUtils.defaultString(StringTool.object2String(depMap2.get("VERSION")), "1.0.0");
                    VersionInfoEnty versionInfoEnty1 = new VersionInfoEnty(version1);
                    VersionInfoEnty versionInfoEnty2 = new VersionInfoEnty(version2);
                    return versionInfoEnty1.compareTo(versionInfoEnty2) * -1;//降序
                } catch (Exception e) {
                    logger.error("排序异常", e);
                }
                return 0;
            }
        });
    }

    /**
     * 版本降序
     * @param list
     */
    public static void sortDeployVersion2HashMap(List<HashMap<String, String>> list) {
        if (BlankUtil.isBlank(list)) {
            return;
        }
        Collections.sort(list, (depMap1, depMap2) -> {
            try {
                String version1 = StringUtils.defaultString(depMap1.get("VERSION"), "1.0.0");
                String version2 = StringUtils.defaultString(depMap2.get("VERSION"), "1.0.0");
                VersionInfoEnty versionInfoEnty1 = new VersionInfoEnty(version1);
                VersionInfoEnty versionInfoEnty2 = new VersionInfoEnty(version2);
                return versionInfoEnty1.compareTo(versionInfoEnty2) * -1;//降序
            } catch (Exception e) {
                logger.error("排序异常", e);
            }
            return 0;
        });
    }

    /**
     * linux路径为二级及以上的目录返回true
     *
     * @param path
     * @return
     */
    public static boolean secondLevelPathValidate(String path) {

        //linux的二级目录及以上的匹配
        Pattern reg = Pattern.compile("^/[^/]+/[^/]+(/[^/]+)*/?$");

        Matcher matcher = reg.matcher(path);

        return matcher.matches();
    }

    public static void main(String[] args) {
    }
}
