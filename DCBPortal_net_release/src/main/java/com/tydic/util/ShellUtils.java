package com.tydic.util;

import com.jcraft.jsch.*;
import com.tydic.bp.common.utils.tools.BlankUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: tom
 * Date: 16-12-20
 * Time: 下午7:31
 * To change this template use File | Settings | File Templates.
 */
public class ShellUtils {
	//记录日志
	private static Logger logger = Logger.getLogger(ShellUtils.class);
	//远程主机连接对象
    private JSch jsch;
    //远程主机连接Session对象
    private Session session;
    //执行命令远程主机返回成功输出流信息
    private String succes;

	//执行命令远程主机返回失败输出流信息
	private String error;
	
	//远程主机IP
	private String hostId;
	//登录远程主机用户名
	private String userName;
	//登录远程主机密码
	private String password;
	
	//编码格式
	//private final static String _ENCODING_GBK = "GBK";
	private final static String ENCODING_UTF8 = "UTF-8";
	//初始环境变量命令
	private final static String INIT_ENV_VAR="if [ -e /etc/profile ]; then source /etc/profile; fi;if [ -e ~/.bashrc ]; then source ~/.bashrc; fi;if [ -e ~/.bash_profile ]; then source ~/.bash_profile; fi;";

	public ShellUtils() {
		
	}

	/**
	 * Shell命令执行帮助类
	 * 
	 * @param hostId
	 * @param userName
	 * @param password
	 */
    public ShellUtils(String hostId, String userName, String password) {
    	this.hostId = StringTool.isIPV4Legal(hostId) ? StringUtils.trim(hostId) : StringUtils.trim(hostId) + StringUtils.trim(Constant.LOCAL_NET_CARD);
    	this.userName = userName;
    	this.password = password;
    }
    
    /**
     * 连接远程主机Shell
     * 
     * @throws JSchException
     */
    public void connect() throws JSchException {
    	jsch = new JSch();
        session = jsch.getSession(this.userName, this.hostId, 22);
        session.setPassword(this.password);

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
    }
    
    /**
     * 连接到指定的IP
     *
     * @throws JSchException
     */
    public void connect(String user, String passwd, String host) throws JSchException {
        jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(passwd);

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
    }
    
    /**
     * 执行相关的命令
     * 
     * @param command 执行的命令
     * @param user    登录用户
     * @param passwd  登录密码
     * @param host    登录主机
     * @throws JSchException
     */
    public String execMsg(String command, String user, String passwd, String host) {
    	//连接远程主机
        try {
			connect(user, passwd, host);
		} catch (JSchException e) {
			logger.error("远程主机连接失败, 失败原因 --->", e);
			throw new RuntimeException("远程主机连接失败,当前主机:" + host + ", 登录用户名: " + user);
		}
        logger.debug("远程主机连接成功, 用户名 ---> " + user + ", 密码 ---> " + passwd + ", 主机 ---> " + host + ",\n 执行命令 ---> " + command);
        BufferedReader reader = null;
        Channel channel = null;
        StringBuffer msg = new StringBuffer();
        try {
            if (command != null) {
                channel = session.openChannel("exec");
                session.setTimeout(10000);
                ((ChannelExec) channel).setCommand(INIT_ENV_VAR+command);

                channel.setInputStream(null);
                ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
                ((ChannelExec) channel).setErrStream(errorStream);

                channel.connect(10000);
                InputStream in = channel.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in, ENCODING_UTF8));
                String buf = "";
                String tmpStr ="";
                while ((tmpStr = reader.readLine()) != null) {
                    buf=buf+tmpStr+"\n";
                }
                succes = buf;
                //错误信息
				error = errorStream.toString();
				
				if(StringUtils.trimToEmpty(error).length()>1){
					msg.append(Constant.FLAG_ERROR).append(succes).append("\n").append(error).append(Constant.FLAG_ERROR);
				}else{
					msg.append(succes);
				}
            }
        } catch (IOException e) {
        	logger.error("执行Shell命令失败，失败信息 --->", e);
            throw new RuntimeException(e);
        } catch (JSchException e) {
        	logger.error("执行Shell命令失败，失败信息 --->", e);
            throw new RuntimeException(e);
        } finally {
            try {
            	if (reader != null) {
            		reader.close();
            	}
            	if (channel != null) {
            		channel.disconnect();
            	}
            	if (session != null) {
            		session.disconnect();
            	}
            } catch (IOException e) {
            	logger.error("关闭读取流失败，失败信息 --->", e);
                e.printStackTrace();
            }
        }
        return msg.toString();
    }
    
    /**
     * 执行相关的命令
     * @param command 执行命令
     * 
     * @throws JSchException
     */
    public String execMsg(String command) {
    	//连接远程主机
    	try {
			connect(this.userName, this.password, this.hostId);
		} catch (JSchException e) {
			logger.error("远程主机连接失败, 失败原因 --->", e);
			throw new RuntimeException("远程主机连接失败,当前主机:" + this.hostId + ", 登录用户名: " + this.userName);
		}
        logger.debug("远程主机连接成功, 用户名 ---> " + this.userName + ", 密码 ---> " + this.password 
        		+ ", \n主机 ---> " + this.hostId + ", 执行命令 ---> " + command);
        
        BufferedReader reader = null;
        Channel channel = null;
        StringBuffer msg = new StringBuffer();
        try {
            if (!BlankUtil.isBlank(command)) {
                channel = session.openChannel("exec");
                session.setTimeout(10000);
                ((ChannelExec) channel).setCommand(INIT_ENV_VAR+command);

                channel.setInputStream(null);
                ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
                ((ChannelExec) channel).setErrStream(errorStream);

                channel.connect(10000);
                InputStream in = channel.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in, ENCODING_UTF8));
                String buf = "";
                String tmpStr ="";
                while ((tmpStr = reader.readLine()) != null) {
                    buf=buf+tmpStr+"\n";
                }
                succes = buf;
                //错误信息
				error = errorStream.toString();
				
				if(StringUtils.trimToEmpty(error).length()>1){
					msg.append(Constant.FLAG_ERROR).append(succes).append("\n").append(error).append(Constant.FLAG_ERROR);
					//msg.append(Constant.ERROR).append("\n").append(succes).append("\n").append(error).append(Constant.ERROR);
				}else{
					msg.append(succes);
				}
            }
        } catch (IOException e) {
        	logger.error("执行Shell命令失败，失败信息 --->", e);
            throw new RuntimeException(e);
        } catch (JSchException e) {
        	logger.error("执行Shell命令失败，失败信息 --->", e);
            throw new RuntimeException(e);
        } finally {
        	try {
            	if (reader != null) {
            		reader.close();
            	}
            	if (channel != null) {
            		channel.disconnect();
            	}
            	if (session != null) {
            		session.disconnect();
            	}
            } catch (IOException e) {
            	logger.error("关闭读取流失败，失败信息 --->", e);
                e.printStackTrace();
            }
        }
        return msg.toString();
    }

    public String getSucces() {
		return succes;
	}

	public String getError() {
		return error;
	}
	
    public static void main(String[] args){
        try {
        	System.out.println("执行命令.....");
            ShellUtils shellUtils = new ShellUtils("192.168.161.25", "bp_dcf", "dic123");
            //String result = shellUtils.execMsg("cd /public/bp/DCBPortal_test/business/V0.0.0.1/bin/; ./start_billingtop.sh topologyBILL-V0.0.0.1 start \"TOPOLOGY_CFG=/public/bp/DCBPortal_test/business/V0.0.0.1/cfg|DBDRIVER_PATH=/public/bp/DCBPortal_test/business/V0.0.0.1/lib|DBDRIVER_CONSTR=sh_pre/sh_pre2015@ORA9411g|LOGADDR=192.168.161.143:9006|OCS_HOME=/public/bp/DCBPortal_test/business/V0.0.0.1/|CONFIGFILE=/public/bp/DCBPortal_test/business/V0.0.0.1/cfg/dcstorm.conf|FMT_CONFIG=/public/bp/DCBPortal_test/business/V0.0.0.1/cfg|JSTORM_HOME=/public/bp/DCBPortal_test/tools/env/0.0.2/jstorm\"");
            String result = shellUtils.execMsg("ln -s -f /public/bp/fastDFS1/data/ /public/bp/fastDFS1/data/M00");
            System.out.println("result ----> " + result);
            //System.out.println("success ---> " + shellUtils.getSucces());
            //System.out.println("error ---> " + shellUtils.getError());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
