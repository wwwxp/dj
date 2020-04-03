//package com.tydic.util;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.log4j.Logger;
//
//import ch.ethz.ssh2.Connection;
//import ch.ethz.ssh2.ConnectionInfo;
//import ch.ethz.ssh2.Session;
//import ch.ethz.ssh2.StreamGobbler;
//
//import com.esotericsoftware.minlog.Log;
//
///**
// * @author luoyongfei
// * add 2014-04-10
// */
//public class RemoteCommand {
//	private static Connection conn;
//	private static Logger LOG = org.apache.log4j.Logger.getLogger(RemoteCommand.class);
//	public  static boolean judge=false;
//	/*private static ThreadPoolExecutor executor;
//	static{//初始化线程池
//		executor = new ThreadPoolExecutor(5, 50, 300,
//				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(3),
//				new ThreadPoolExecutor.CallerRunsPolicy());
//	}*/
//	/**
//	 * 建立连接
//	 * @param ip
//	 * @param port
//	 * @param userName
//	 * @param password
//	 * @return
//	 */
//	private static boolean createConn(String ip, String port, String userName,
//			String password) {
//		conn = new Connection(ip);
//		try {
//			ConnectionInfo info = conn.connect();
//			return conn.authenticateWithPassword(userName, password);
//		} catch (IOException e) {
//			return false;
//		}
//	}
//
//	/**
//	 * 执行命令 ~!区分行
//	 * @param ip
//	 * @param port
//	 * @param userName
//	 * @param password
//	 * @param command
//	 * @param date
//	 * @return -1连接失败 -2读取消息超时
//	 */
//
//	public  static Map<String, String> executeCommand(String ip, String port,
//			String userName, String password, String command) {
//		LOG.info("执行命令："+command);
//		judge=false;
//		boolean flag = createConn(ip, port, userName, password);
//		Map<String, String> map = new HashMap<String, String>();
//		if (flag == false) {
//			map.put("fail","-1");
//			return map;
//		}
//		try {
//			Session session = conn.openSession();
//			session.execCommand(command);
//			InputStream stderrInput = new StreamGobbler(session.getStderr());
//			InputStream stdoutInput = new StreamGobbler(session.getStdout());
//			BufferedReader  stdoutBuff= new BufferedReader(
//					new InputStreamReader(stdoutInput));
//			BufferedReader stderrBuff = new BufferedReader(new InputStreamReader(stderrInput));
//			String stderrInfo = "";
//			StringBuffer stderrString = new StringBuffer();
//			while ((stderrInfo = stderrBuff.readLine()) != null) {//错误流
//				stderrString.append(stderrInfo + "\n");
//			}
//			String stdoutInfo = "";
//			StringBuffer stdoutString = new StringBuffer();
//			while ((stdoutInfo = stdoutBuff.readLine()) != null) {//标准流
//				stdoutString.append(stdoutInfo + "\n");
//			}
//			Log.info(stdoutString.toString());
//			stdoutBuff.close();
//			stderrBuff.close();
//			session.close();
//			conn.close();
//			//LOG.warn("#RemoteCommand executeCommand -> return stderrString:" + stderrBuff.toString()+" stdoutString:"+stdoutString.toString());
//			judge=true;
//			if (!stderrString.toString().trim().equals("")){
//				map.put("error",stderrString.toString());
//				stderrString=null;
//				return map;
//			}else{
//				map.put("standard",stdoutString.toString());
//				stdoutString=null;
//				return map;
//			}
//		} catch (IOException e) {
//			return map;
//		}
//	}
//	public static Map<String, String> executeCommand(String ip, String port,
//			String userName, String password, String command,long overtime) {
//		/*executor.execute(new RemoteCommandThread(ip,userName,password,command));
//		executor.execute(new MonitorThread(overtime));*/
//		System.out.println("执行命令("+ip+":"+port+")@"+userName+"："+command);
//		Thread th=new Thread(new RemoteCommandThread(ip,userName,password,command));
//		Thread th1=new Thread(new MonitorThread(overtime));
//		th.start();
//		th1.start();
//		while(true){
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				LOG.warn("#RemoteCommand executeCommand ->  Thread:" + e);
//			}
//			if(RemoteCommandThread.map!=null){
//				th.stop();
//				th1.stop();
//				return RemoteCommandThread.map;
//			}
//			System.out.println(RemoteCommandThread.map+"==============="+MonitorThread.flag);
//			if(MonitorThread.flag){
//				th.stop();
//				th1.stop();
//				Map<String, String> map=new HashMap<String, String>();
//				map.put("overtime", "-2");
//				return map;
//			}
//		}
//	}
//}
