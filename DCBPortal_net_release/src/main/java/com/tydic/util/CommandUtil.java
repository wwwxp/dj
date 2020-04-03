//package com.tydic.util;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Logger;
//
//public class CommandUtil {
//	/**
//	 * 日志对象
//	 */
//	private static Logger log = Logger.getLogger(CommandUtil.class);
//
//	public static String exec(String cmd) throws Exception {
//
//		log.info("cmd:" + cmd);
//		//程序部署在windows环境使用下面命令
//		//String[] cmdArray = new String[] { "/bin/sh", "-c", cmd };
//		//程序部署在windows环境使用下面命令
//		String[] cmdArray = new String[] { "cmd.exe", "/c", cmd };
//		Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
//		Process p = run.exec(cmdArray);// 启动另一个进程来执行命令
//		p.waitFor();
//		String result = processStdout(p.getInputStream(), "UTF-8");
//		log.debug("命令执后的输出的结果：" + result);
//		if (result.indexOf(Constant.ERROR) >= 0 || result.indexOf(Constant.FAILED) >= 0
//		   || result.indexOf(Constant.EXCEPTION) >= 0) {
//			throw new Exception(result);
//		}
//		/*
//		 * //检查命令是否执行失败。 if (p.waitFor() != 0) { if (p.exitValue() ==
//		 * 1)//p.exitValue()==0表示正常结束，1：非正常结束 throw new Exception("命令执行失败!"); }
//		 * in.close();
//		 */
//
//		return result;
//	}
//
//
//	public static boolean exec(String[] cmd) throws Exception {
//
//		log.info("cmd:" + cmd.toString());
//		Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
//		Process p = run.exec(cmd);// 启动另一个进程来执行命令
//		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//		BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
//		String lineStr;
//
//		while ((lineStr = inBr.readLine()) != null) {
//			System.out.println(lineStr);
//		}
//
//		// 检查命令是否执行失败。
//		if (p.waitFor() != 0) {
//			if (p.exitValue() == 1) // p.exitValue()==0表示正常结束，1：非正常结束
//				throw new Exception("命令执行失败!");
//		}
//		inBr.close();
//		in.close();
//
//		return true;
//	}
//
//	public static String processStdout(InputStream in, String charset) {
//		byte[] buf = new byte[1024];
//		StringBuffer sb = new StringBuffer();
//		try {
//			while (in.read(buf) != -1) {
//				sb.append(new String(buf, charset));
//			}
//		} catch (IOException e) {
//			log.error("错误--->"+e);
//		}
//		return sb.toString();
//	}
//	public static String spilt(String src,String regex){
//		if(StringUtils.isNotBlank(src)){
//			String [] str = src.split(regex);
//			if(str!= null && str.length>0){
//				return str[0];
//			}else{
//				return "";
//			}
//		}
//		return "";
//	}
//
//	public static void main(String[] args) throws Exception {
//         System.out.println(CommandUtil.spilt("/sdf/sf/sdf/bin/sdfd/","bin"));
//		// CommandUtil.exec({"ping www.baidu.com"});
//	}
//
//}
