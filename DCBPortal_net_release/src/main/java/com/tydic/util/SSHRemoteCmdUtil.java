package com.tydic.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class SSHRemoteCmdUtil {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(SSHRemoteCmdUtil.class);
	private Connection conn;
	private String ipAddr;
	private String charset = Charset.defaultCharset().toString();
	//private String charset = "UTF-8";
	private String userName;
	private String password;

	/**
	 * 判断IP是否为IPV4，如果为IPV4则不添加网卡，否则添加网卡操作
	 * @param ipAddr
	 * @param userName
	 * @param password
	 * @param charset
	 */
	public SSHRemoteCmdUtil(String ipAddr, String userName, String password, String charset) {
		this.ipAddr = StringTool.isIPV4Legal(ipAddr) ? ipAddr : StringUtils.trim(ipAddr) + StringUtils.trim(Constant.LOCAL_NET_CARD);
		this.userName = userName;
		this.password = password;
		if (charset != null) {
			this.charset = charset;
		}
	}
	public SSHRemoteCmdUtil(){
		
	}

	public boolean login() throws IOException {
		conn = new Connection(ipAddr);
		log.info("ipAddr: " + ipAddr);
		conn.connect(null, 5000, 0);// 连接
		return conn.authenticateWithPassword(userName, password); // 认证
	}

	public String exec(String cmds) {
		InputStream in = null;
		log.info("ipAddr:"+ipAddr+",userName:"+userName);
		String result = "";
		String error="";
		StringBuffer msg = new StringBuffer();
		try {
			if (this.login()) {
				Session session = conn.openSession(); // 打开一个会话
				log.info("cmd:" + cmds);
				session.execCommand(cmds);
				in = session.getStderr();
				error = this.processStderr(in, this.charset);
				in = session.getStdout();
				result = this.processStdout(in, this.charset);
				log.info("正常结果输出:" + result);
				log.info("错误结果输出:" + error);
				/*if(StringUtils.isNotBlank(error)){
					msg.append(Constant.ERROR);
				}*/
				session.close();
				conn.close();
			}else{
				throw new RuntimeException("远程机连接不成功");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}
		 
		return msg.append(System.getProperty("line.separator")).append(result).toString();
	}
	public String execMsg(String cmds) {
		InputStream in = null;
		log.info("ipAddr:"+ipAddr+",userName:"+userName);
		String result = "";
		String error="";
		StringBuffer msg = new StringBuffer();
		try {
			if (this.login()) {
				Session session = conn.openSession(); // 打开一个会话
				log.info("cmd:" + cmds);
				session.execCommand(cmds);
				in = session.getStderr();
				error = this.processStderr(in, this.charset);
				in = session.getStdout();
				result = this.processStdout(in, this.charset);
				log.info("正常结果输出:" + result);
				log.info("错误结果输出:" + error);
				/*if(StringUtils.isNotBlank(error)){
					msg.append(Constant.ERROR);
				}*/
				session.close();
				conn.close();
			}else{
				throw new RuntimeException("远程机连接不成功");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}
		if(StringUtils.trimToEmpty(error).length()>1){
			msg.append(Constant.ERROR).append("\n").append(result).append("\n").append(error).append(Constant.ERROR);
		}else{
			msg.append(result);
		}
		
		 
		return msg.toString();
	}
	public String execMsgGBK(String cmds) {
		InputStream in = null;
		log.info("ipAddr:"+ipAddr+",userName:"+userName);
		String result = "";
		String error="";
		StringBuffer msg = new StringBuffer();
		try {
			if (this.login()) {
				Session session = conn.openSession(); // 打开一个会话
				log.info("cmd:" + cmds);
				session.execCommand(cmds);
				in = session.getStderr();
				error = this.processStderr(in, "GBK");
				in = session.getStdout();
				result = this.processStdout(in, "GBK");
				log.info("正常结果输出:" + result);
				log.info("错误结果输出:" + error);
				/*if(StringUtils.isNotBlank(error)){
					msg.append(Constant.ERROR);
				}*/
				session.close();
				conn.close();
			}else{
				throw new RuntimeException("远程机连接不成功");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}
		if(StringUtils.trimToEmpty(error).length()>1){
			msg.append(result).append(System.getProperty("line.separator")).append(error).append("error");
		}else{
			msg.append(result);
		}
		
		 
		return msg.toString();
	}
	
	public String execStr(String cmds) {
		InputStream in = null;
		log.info("ipAddr:"+ipAddr+",userName:"+userName);
		String result = "";
		String error="";
		StringBuffer msg = new StringBuffer();
		try {
			if (this.login()) {
				Session session = conn.openSession(); // 打开一个会话
				log.info("cmd:" + cmds);
				session.execCommand(cmds);
				in = session.getStderr();
				error = this.processStderr(in, this.charset);
				in = session.getStdout();
				result = this.processStdout(in, this.charset);
				log.info("正常结果输出:" + result);
				//log.info("错误结果输出:" + error);
				if(StringUtils.isNotBlank(error)){
					msg.append(Constant.ERROR);
				}
				session.close();
				conn.close();
			}else{
				throw new RuntimeException("远程机连接不成功");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}
		 
		return msg.append(System.getProperty("line.separator")).append(result).toString();
	}

	public String processStdout(InputStream in, String charset) {
		byte[] buf = new byte[1024];
		StringBuffer sb = new StringBuffer();
		int readLength=0;
		try {
			while ((readLength=in.read(buf)) != -1) {
				if(readLength != 1024){
					sb.append(new String(buf, 0, readLength,charset));
				}else{
					
					sb.append(new String(buf,charset));
				}
				buf = new byte[1024];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	public String processStderr(InputStream in, String charset) {
		byte[] buf = new byte[1024];
		StringBuffer sb = new StringBuffer();
		int readLength=0;
		try {
			while ((readLength=in.read(buf)) != -1) {
				if(readLength != 1024){
					sb.append(new String(buf, 0, readLength,charset));
				}else{
					
					sb.append(new String(buf,charset));
				}
				buf = new byte[1024];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	public static void main(String[] args) throws Exception {
		SSHRemoteCmdUtil cmdutil = new SSHRemoteCmdUtil("192.168.161.26","bp_dcf","dic123",null);
		String cmd = "cd /public/bp/DCBPortal_Net_Test/tools;chmod a+x auto.sh;./auto.sh -s dcas -2 redis -3 192.168.161.26_01/redis.conf -4 0.0.1";
		String result = cmdutil.execMsg(cmd);
		System.out.println("SSHRemoteCmdUtil execute ---> " + result);
		
	}
	 

}
