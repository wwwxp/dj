package com.tydic.dcm.client;

import java.net.Socket;

import org.apache.log4j.Logger;

import com.tydic.dcm.device.DeviceControl;

public class DcmClient extends Client {
	private static Logger logger = Logger.getLogger(DcmClient.class);
	
	// ----------------------command---------------------
	public static final int MODULE_MON = 8;
	public static final int CMD_MON_VIEW = MODULE_MON * 100 + 1;
	public static final int CMD_MON_CTRL = MODULE_MON * 100 + 2;

	public DcmClient(Socket socket, ClientManager manager) {
		super(socket, manager);
		setName("DcmClient");
	}

	/**
	 * 执行客户端发送的命令
	 * 
	 * @param cmd 客户端命令对象
	 * @return CommandResult
	 */
	@Override
	public CommandResult execute(Command cmd) throws Exception {
		logger.debug("begin execute client command, cmd:" + cmd.toString());
		
		CommandResult rst = dcmExecute(cmd);
		if (rst.getRstRecordSize() < 20) {
			logger.debug("execute client command result:" + rst.toString());
		} else {
			logger.debug("execute client command result:" + rst.toString2());
		}
		logger.debug("end execute client command");
		return rst;
	}
	
	/**
	 * 客户端命令处理逻辑
	 * @param cmd
	 * @return
	 */
	private CommandResult dcmExecute(Command cmd) {
		int module = cmd.type / 100;
		if (module == MODULE_MON) {
			return monModule(cmd);
		} else {
			return new CommandResult(cmd, false, "not found module, module:" + module);
		}
	}
	
	private CommandResult monModule(Command cmd) {
		if (cmd.type == CMD_MON_CTRL) {
			return DeviceControl.control(cmd);
		} else {
			return new CommandResult(cmd, false, "cmd.type error. current cmd.type:" + cmd.type);
		}
	}

	@Override
	public void CleanConnect() {
		
	}

	/**
	 * 初始化Client连接，目前为空实现
	 */
	@Override
	public boolean initConnect() throws Exception {
		return true;
	}
}
