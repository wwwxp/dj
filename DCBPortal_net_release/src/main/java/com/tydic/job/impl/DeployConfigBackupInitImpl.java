package com.tydic.job.impl;

import com.tydic.bean.FtpDto;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.job.DeployConfigBackupInit;
import com.tydic.util.SessionUtil;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;


/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_V10]   
  * @Package:      [com.tydic.job.impl]    
  * @ClassName:    [DeployConfigBackupInitImpl]     
  * @Description:  [版本发布服务器配置文件备份]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-11-7 上午9:17:03]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-11-7 上午9:17:03]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service("backupService")
public class DeployConfigBackupInitImpl implements DeployConfigBackupInit {

	private static Logger logger = Logger.getLogger(DeployConfigBackupInitImpl.class);
	
	//默认远程主机端口
	private static final String DEFAULT_REMOTE_PORT = "22";
	
	//本机默认备份目录
	//private static final String DEFAULT_LOCAL_BACKUP_PATH = "backup";
	
	//文件分隔符
	private static final String DEFAULT_FILE_SEPARTOR = "/";
	
	/**
	 * 备份版本发布服务器配置文件
	 */
	@Override
	public void backupConfig() {
		try {
			//获取版本发布服务器信息
			FtpDto ftpDto = SessionUtil.getFtpParams();
			
			//备份远程主机信息
			String backupRemoteIp = SystemProperty.getContextProperty("backup.remote.ip");
			String backupRemotePort = SystemProperty.getContextProperty("backup.remote.port");
			if (BlankUtil.isBlank(backupRemotePort)) {
				backupRemotePort = DEFAULT_REMOTE_PORT;
			}
			String backupRemoteUser = SystemProperty.getContextProperty("backup.remote.username");
			String backupRemotePasswd = SystemProperty.getContextProperty("backup.remote.password");
			String backupRemotePath = SystemProperty.getContextProperty("backup.remote.targetpath");
			//需要吧目录最后/去掉，不然上传的文件不会创建对应的目录，而是将目录中的文件上传
			if (!BlankUtil.isBlank(backupRemotePath) && backupRemotePath.endsWith(DEFAULT_FILE_SEPARTOR)) {
				backupRemotePath = backupRemotePath.substring(0, backupRemotePath.length() -1);
			}
			
			//获取需要备份的配置文件目录
			String backupFilePath = SystemProperty.getContextProperty("backup.sourcepath");
			//默认备份整个目录
			if (BlankUtil.isBlank(backupFilePath)) {
				backupFilePath = ftpDto.getFtpRootPath();
			} else {
				backupFilePath = backupFilePath.replaceAll(",", " ");
			}
			logger.debug("配置文件备份操作， 版本发布服务器IP： " + ftpDto.getHostIp() + "，用户名：" + ftpDto.getUserName());
			
			//本地主机备份目录,该目录不能是版本部署目录的子目录
			try {
				String backupLocalPath = SystemProperty.getContextProperty("backup.local.targetpath");
				////public/bp/AH_DCBPortal
				logger.debug("本地目录备份， 版本发布服务器备份目录：" + backupLocalPath);
				
				///public/bp/AH_DCBPortal/conf
				logger.debug("本地目录备份， 版本发布服务器需要备份目录：" + backupFilePath);
				if (!BlankUtil.isBlank(backupLocalPath) && !StringTool.object2String(backupLocalPath).equals(StringTool.object2String(ftpDto.getFtpRootPath()))
						&& !backupLocalPath.startsWith(backupFilePath) && !backupFilePath.startsWith(backupLocalPath)) {
					if (backupLocalPath.endsWith(DEFAULT_FILE_SEPARTOR)) {
						backupLocalPath = backupLocalPath.substring(0, backupLocalPath.length() -1);
					}
					String localCommand = "";
					localCommand += " mkdir -p " + backupLocalPath + ";";
					localCommand += " cp -r " + backupFilePath + " " + backupLocalPath;
					logger.debug("版本发布服务器配置文件备份，备份到版本发布服务器，备份目录：" + backupLocalPath + "\n备份命令: " + localCommand);
					
					ShellUtils shellUtils = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
					String execRst = shellUtils.execMsg(localCommand);
					logger.debug("版本发布服务器配置文件本地备份，当前备份时间：" + (DateUtil.getCurrent(DateUtil.allPattern)) + " 备份命令执行结果: " + execRst);
				}
			} catch (Exception e1) {
				logger.error("本地备份失败， 失败原因：", e1);
			}
			
			try {
				if (!BlankUtil.isBlank(backupRemoteIp) && !BlankUtil.isBlank(backupRemoteUser) 
						&& !BlankUtil.isBlank(backupRemotePasswd) && !BlankUtil.isBlank(backupRemotePath)) {
					logger.debug("备份远程主机IP：" + backupRemoteIp + "， 备份远程目录: " + backupRemotePath 
							+ "，备份开始时间：" + (DateUtil.getCurrent(DateUtil.allPattern)));
					
					String command = "expect -c '";
						//expect默认timeout为30S,大文件传输需要设置为-1表示无穷大
						command += "set timeout -1;";
						//下面两行都支持远程目录备份，后面一个效率更高，增量备份
						//command += "spawn scp -r -P " + backupRemotePort + " " + backupFilePath + " " + backupRemoteUser + "@" + backupRemoteIp + ":" + backupRemotePath + ";";
						command += "spawn rsync -avz --port=" + backupRemotePort + " " + backupFilePath + " " + backupRemoteUser + "@" + backupRemoteIp + ":" + backupRemotePath + ";";
						command += "expect ";
						command += "*assword* { send \"" + backupRemotePasswd + "\r\" };";
						command += "expect eof; '" ;
					logger.debug("版本发布服务器配置文件备份，备份到远程主机：" + backupRemoteIp + ", \n备份命令：" + command);
					ShellUtils shellUtils = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
					String execRst = shellUtils.execMsg(command);
					logger.debug("执行远程主机备份命令结束，当前备份时间：" + (DateUtil.getCurrent(DateUtil.allPattern)) + "，执行命令返回结果：" + execRst);
				}
			} catch (Exception e) {
				logger.warn("远程备份失败， 失败原因：", e);
			} 
		} catch (Exception e) {
			logger.warn("文件备份失败， 失败信息：", e);
		}
	}
}
