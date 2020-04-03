//package com.tydic.util;
//
//import java.util.Map;
//
//import org.apache.log4j.Logger;
//
//
//public class RemoteCommandThread extends Thread {
//	private Logger LOG = org.apache.log4j.Logger
//	.getLogger(RemoteCommandThread.class);
//	private String userName="";
//	private String passWord="";
//	private String ip;
//	private String command;
//	public static Map<String,String> map;
//	public RemoteCommandThread(String ip,String userName,String passWord,String command) {
//		this.ip=ip;
//		this.userName=userName;
//		this.passWord=passWord;
//		this.command=command;
//	}
//	public RemoteCommandThread(){}
//	@Override
//	public void run() {
//		map=RemoteCommand.executeCommand(ip,"",userName,passWord,command);
//	}
//
//
//}
