package com.tydic.dcm.client;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.dcm.util.tools.StringTool;

/**
 * 客户端命令对象
 * @author Yuanh
 *
 */
public class Command {
	/**
	 * 日志对象
	 */
	private static Logger logger = Logger.getLogger(Command.class);
	
	/**
	 * 命令转换编码
	 */
	public static final String ENCODE = "GBK";
	
	/**
	 * 客户端命令分割符
	 */
	protected static final String SP = "\n";
	
	/**
	 * 命令
	 */
	public static final int CMD_MAX_LENGTH = 1024 * 1024;
	
	/**
	 * 客户端发送命令ID
	 */
	protected int id;
	
	/**
	 * 客户端发送命令类型
	 */
	protected int type;
	
	/**
	 * 客户端发送命令具体参数列表
	 */
	protected Hashtable<String, String> paramsTable;
	
	public Command() {
		
	}
	
	/**
	 * 解析客户端命令
	 * @param bytes
	 * @param spos
	 * @param epos
	 * @throws Exception
	 */
	public Command(byte [] bytes, int spos, int epos) throws Exception {
		logger.debug("begin create client command...");
		paramsTable = new Hashtable<String, String>();
		
		String cmdStr = new String(bytes, spos, epos, ENCODE);
		logger.info("command body: " + cmdStr);
		
		Vector<String> cmdList = StringTool.tokenStringChar(cmdStr, SP);
		if (cmdList == null || cmdList.size() < 2) {
			logger.debug("client command exception, please check it.");
			throw new RuntimeException("client command exception, command:" + cmdStr);
		} else {
			//获取命令ID
			id = Integer.parseInt(cmdList.get(0));
			//获取命令Type
			type = Integer.parseInt(cmdList.get(1));
			//获取命令业务参数
			for (int i=2; i<cmdList.size(); i++) {
				String singleCmdStr = cmdList.get(i);
				int index = singleCmdStr.indexOf("=");
				if (!BlankUtil.isBlank(singleCmdStr) && index != -1) {
					String cmdKey = singleCmdStr.substring(0, index);
					String cmdVal = singleCmdStr.substring(index + 1);
					paramsTable.put(cmdKey, cmdVal);
				} else {
					logger.debug("client command exception, current command item: " + singleCmdStr);
					throw new RuntimeException("client command exception, current command item:" + singleCmdStr);
				}
			}
		}
		logger.debug("end create client command ok.");
	}
	
	/**
	 * 获取命令参数对应的值
	 * @param cmdKey
	 * @return
	 */
	public String getParam(String cmdKey) {
		return paramsTable.get(cmdKey);
	}
	
	/**
	 * 获取命令参数列表
	 * @return
	 */
	public Hashtable<String, String> getParamsTable() {
		return this.paramsTable;
	}
	
	/**
	 * 当前命令对象字符串形式
	 */
	public String toString() {
		return "" + id + "|" + type + "|" + paramsTable.toString();
	}
}
