//package com.tydic.util;
//
///* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
///**
// * This program enables you to connect to sshd server and get the shell prompt.
// *   $ CLASSPATH=.:../build javac Shell.java
// *   $ CLASSPATH=.:../build java Shell
// * You will be asked username, hostname and passwd.
// * If everything works fine, you will get the shell prompt. Output may
// * be ugly because of lacks of terminal-emulation, but you can issue commands.
// *
// */
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Properties;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.jcraft.jsch.Channel;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.Session;
//
//public class Shell{
//	private static final Log log = LogFactory.getLog(Shell.class);
//	/**
//
//	 * 利用JSch包实现远程主机SHELL命令执行（执行完后关闭连接）
//
//	 * @param ip 主机IP
//	 * @param user 主机登陆用户名
//	 * @param psw  主机登陆密码
//	 * @param port 主机ssh2登陆端口，如果取默认值，传-1
//     * @param command 发送命令
//	 */
//	public static String sshShell(String ip, String user, String password ,int port ,String command) throws Exception{
//	    Session session = null;
//	    Channel channel = null;
//	    String resultStr=null;
//	    String encoding="UTF-8";
//	    JSch jsch = new JSch();
//
//	    if(port <=0){
//	        //连接服务器，采用默认端口
//	        session = jsch.getSession(user, ip);
//	    }else{
//	        //采用指定的端口连接服务器
//	        session = jsch.getSession(user, ip ,port);
//	    }
//
//	    //如果服务器连接不上，则抛出异常
//	    if (session == null) {
//	        throw new Exception("session is null");
//	    }
//	    //设置第一次登陆的时候提示，可选值：(ask | yes | no)
//	    Properties config = new Properties();
//		config.put("StrictHostKeyChecking", "no");
//		session.setConfig(config);
//	    //设置登陆主机的密码
//	    session.setPassword(password);//设置密码
//
//	    //设置登陆超时时间
//	    session.connect(30000);
//
//	    try {
//	        //创建sftp通信通道
//	        channel = (Channel) session.openChannel("shell");
//	        channel.connect(1000);
//
//	        //获取输入流和输出流
//	        InputStream input = channel.getInputStream();
//	        OutputStream out = channel.getOutputStream();
//
//	        //发送需要执行的SHELL命令，需要用\n结尾，表示回车
//	        String shellCommand = command+"\n";
//	        out.write(shellCommand.getBytes());
//	        out.flush();
//
//
//	        Thread.sleep(100);
//			 //获取命令执行的结果
//	        if (input.available() > 0) {
//	            byte[] data = new byte[input.available()];
//	            int nLen = input.read(data);
//
//	            if (nLen < 0) {
//	                throw new Exception("network error.");
//	            }
//
//	            //转换输出结果并打印出来
//	            resultStr = new String(data, 0, nLen,encoding);
//	            log.debug("shellCommand return:\n"+resultStr);
//	        }else{
//	        	Thread.sleep(500);
//	        	if(input.available()>0){
//	        		byte[] data = new byte[input.available()];
//		            int nLen = input.read(data);
//		            if (nLen < 0) {
//		                throw new Exception("network error.");
//		            }
//
//		            //转换输出结果并打印出来
//		            resultStr = new String(data, 0, nLen,encoding);
//	        	}
//
//	        }
//	        input.close();
//	        out.close();
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	    } finally {
//	        session.disconnect();
//	        channel.disconnect();
//	    }
//
//	    return resultStr;
//	}
//	/**
//	 * 返回Channel，由使用者控制连接的所有细节（包括流的处理和关闭）
//     *@param ip 主机IP
//	 * @param user 主机登陆用户名
//	 * @param password  主机登陆密码
//	 * @param port 主机ssh2登陆端口，如果取默认值，传-1
//	 * @return  Channel
//	 * @throws Exception
//	 */
//	public static Channel  getShellChannel(String ip, String user, String password ,int port ) throws Exception{
//	    Session session = null;
//	    Channel channel = null;
//	    JSch jsch = new JSch();
//
//	    if(port <=0){
//	        //连接服务器，采用默认端口
//	        session = jsch.getSession(user, ip);
//	    }else{
//	        //采用指定的端口连接服务器
//	        session = jsch.getSession(user, ip ,port);
//	    }
//
//	    //如果服务器连接不上，则抛出异常
//	    if (session == null) {
//	        throw new Exception("session is null");
//	    }
//	    //设置第一次登陆的时候提示，可选值：(ask | yes | no)
//	    java.util.Properties config = new java.util.Properties();
//		config.put("StrictHostKeyChecking", "no");
//		session.setConfig(config);
//	    //设置登陆主机的密码
//	    session.setPassword(password);//设置密码
//
//	    //设置登陆超时时间
//	    session.connect(30000);
//
//	    try {
//	        //创建sftp通信通道
//	        channel = (Channel) session.openChannel("shell");
//	        channel.connect(1000);
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	    }
//
//	    return channel;
//	}
//
//	public static void main(String[] args){
//		try {
//			String result=Shell.sshShell("192.168.161.166", "storm", "storm01", 22,"ll");
//			System.out.println(result);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}