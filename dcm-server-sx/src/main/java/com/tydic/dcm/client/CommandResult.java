package com.tydic.dcm.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.dcm.util.tools.ArrayUtil;

/**
 * 返回客户端命令对象
 * @author Yuanh
 *
 */
public class CommandResult {
	/**
	 * 日志对象
	 */
	private static Logger logger = Logger.getLogger(CommandResult.class);
	
	/**
	 * 返回命令间隔符
	 */
	protected static char CH = '\n';
	
	/**
	 * 返回命令最大长度
	 */
	protected static int MAX_LENGTH = (1024 * 1024);

	/**
	 * 命令ID
	 */
	protected int id;
	
	/**
	 * 命令执行结果
	 */
	protected boolean rst;
	
	/**
	 * 命令执行返回对象列表
	 */
	protected Vector<Object> result;
	
	/**
	 * 命令直接结果对象构造函数
	 * 
	 * @param cmd 客户端解析后的命令对象，主要是用来匹配命令ID
	 */
	public CommandResult(Command cmd) {
		this.id = cmd.id;
		this.rst = false;
		result = new Vector<Object>();
	}

	public CommandResult(Command cmd, boolean rst) {
		this.id = cmd.id;
		this.rst = rst;
		result = new Vector<Object>();
	}
	
	public CommandResult(Command cmd, boolean rst, String msg) {
		this.id = cmd.id;
		this.rst = rst;
		result = new Vector<Object>();
		result.add(msg);
	}
	
	public CommandResult(Command cmd, Vector<Object> msgList) {
		this.id = cmd.id;
		this.rst = true;
		result = new Vector<Object>();
		result.addAll(msgList);
	}
	
	/**
	 * 设置命令执行结果
	 * @param rst
	 */
	public void setExecuteResult(boolean rst) {
		this.rst = rst;
	}
	
	/**
	 * 添加参数命令执行结果对象
	 * @param msg
	 */
	public void add(String msg) {
		if(this.result == null) {
			this.result = new Vector<Object>();
		}
		this.result.add(msg);
	}
	
	/**
	 * 获取命令执行结果返回值参数大小
	 * @return
	 */
	public int getRstRecordSize() {
		return ArrayUtil.getSize(this.result);
	}
	
	/**
	 * 将命令直接结果解析为字节
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String rstStr = "" + id + CH + rst ;
		bos.write(rstStr.getBytes());
		for (int i=0; i<result.size(); i++) {
			Object item = result.get(i);
			if (!BlankUtil.isBlank(item)) {
				String line = "";
				if (item instanceof Vector) {
					Vector<String> record = (Vector<String>) item;
					for (int j=0; j<record.size(); j++) {
						if (j > 0) {
							line += "\t";
						}
						line += record.get(j).trim();
					}
					rstStr = CH + line;
				} else {
					rstStr = CH + item.toString().trim();
				}
			} else {
				rstStr = CH + "null";
			}
			bos.write(rstStr.getBytes());
		}
		logger.debug("execute command result:" + rstStr);
		return bos.toByteArray();
	}

	public String toString() {
		return "" + id + "|" + rst + "|" + result.toString();
	}

	public String toString2() {
		return "" + id + "|" + rst + "|size " + result.size();
	}
}
