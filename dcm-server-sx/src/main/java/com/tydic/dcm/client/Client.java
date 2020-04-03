package com.tydic.dcm.client;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.tydic.dcm.util.tools.ArrayUtil;

/**
 * 
 * @author Yuanh
 *
 */
public abstract class Client extends Thread {
	/**
	 * 日志对象
	 */
	private static Logger logger = Logger.getLogger(Client.class);
	
	/**
	 * 客户端命令前缀、执行结果返回给客户端前缀
	 */
	private static final String CLIENT_CMD_HEAD = "@@@@####";
	
	/**
	 * 客户端线程名称
	 */
	public String name;
	
	/**
	 * Socket对象
	 */
	protected Socket socket;
	
	/**
	 * 客户管理对象
	 */
	protected ClientManager clientManager;
	
	/**
	 * 接收的客户端命令字节
	 */
	protected byte [] cmdBytes;
	
	/**
	 * 抽象方法，具体实现在子类中，用来执行真正的业务逻辑
	 * @param cmd
	 * @return
	 * @throws java.lang.Exception
	 */
	public abstract CommandResult execute(Command cmd) throws java.lang.Exception;
	
	/**
	 * 初始化连接,目前为空实现
	 * @return
	 * @throws java.lang.Exception
	 */
	public abstract boolean initConnect() throws java.lang.Exception;
	
	/**
	 * 子类关闭连接
	 * @throws java.lang.Exception
	 */
	public abstract void CleanConnect();
	
	
	/**
	 * 客户端对象
	 * 
	 * @param socket
	 * @param clientManager
	 */
	public Client(Socket socket, ClientManager clientManager) {
		this.socket = socket;
		this.clientManager = clientManager;
		this.name = socket.getInetAddress().getHostName() + "@" + socket.getPort();
		cmdBytes = new byte[Command.CMD_MAX_LENGTH + 1];
		setName("Client");
	}
	
	/**
	 * 接收并且解析客户端发送的命令,通过下面的解析得出客户端发送的命令
	 * 1-8个字节:@@@@####
	 * 9-16个字节:后续Cmd命令的长度
	 * 17个字节及之后:Cmd正式命令
	 * @return
	 */
	protected Command recvCommand() {
		try {
			//用来判断接收的命令是否为前台客户端发送命令，通过接收到前8位字节进行区分
			byte [] headCmd = new byte[8];
			socket.getInputStream().read(headCmd);
			String headCmdStr = new String(headCmd);
			if (!CLIENT_CMD_HEAD.equals(headCmdStr)) {
				return null;
			}
			logger.info("get client command, cmd head: " + headCmdStr);
			
			//判断接收到的客户端命令总长度
			socket.getInputStream().read(headCmd);
			String cmdLenStr = new String(headCmd);
			int cmdLength = Integer.parseInt(cmdLenStr.trim());
			if (cmdLength > Command.CMD_MAX_LENGTH) {
				return null;
			}
			logger.info("get client command, cmd length: " + cmdLenStr);
			
			//获取命令,并且将命令解析为Command对象
			int pos = 0;
			while(pos < cmdLength) {
				int len = socket.getInputStream().read(cmdBytes, pos, cmdLength - pos);
				pos += len;
			}
			Command command = new Command(cmdBytes, 0, cmdLength);
			logger.info("get client command, command ID:" + command.id 
					+ ", type:" + command.type 
					+ ", params size:" + ArrayUtil.getSize(command.getParamsTable()));
			return command;
		} catch (IOException e) {
			logger.error("get client socket command fail.",  e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("get command fail.",e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 返回命令执行结果
	 * 
	 * @param cmdResult
	 * @return
	 */
	protected boolean sendCommandResult(CommandResult cmdResult) {
		logger.debug("begin send command to client, params:" + cmdResult.toString());
		boolean result = true;
		try {
			//获取命令执行响应Body
			byte [] cmdRst = cmdResult.getBytes();
			//获取命令执行响应Head
			String rstCmdStr = CLIENT_CMD_HEAD + String.valueOf(100000000 + cmdRst.length).substring(1);
			socket.getOutputStream().write(rstCmdStr.getBytes());
			socket.getOutputStream().write(cmdRst);
			socket.getOutputStream().flush();
		} catch (IOException e) {
			result = false;
			logger.error("send command execute result fail.", e);
		}
		return result;
	}
	
	/**
	 * 客户端命令执行
	 * 
	 * @param cmd
	 * @return
	 */
	protected CommandResult iexecute(Command cmd) {
		try {
			return execute(cmd);
		} catch (Exception e) {
			logger.error("execute client command fail.", e);
			e.printStackTrace();
			return new CommandResult(cmd, false, e.getMessage());
		}
	}

	/**
	 * 1、获取客户端命令并且解析
	 * 2、执行客户命令对应的业务处理逻辑
	 * 3、返回命令执行结果
	 */
	@Override
	public void run() {
		try {
			//获取客户端发送的命令，并且解析为Command对象
			Command cmd = recvCommand();
			logger.debug("get client command. cmd:" + cmd == null ? "null" : cmd.toString());
			System.out.println("get client command. cmd:" + cmd == null ? "null" : cmd.toString());
			
			//命令解析错误直接返回
			if (null == cmd) {
				return;
			}
			
			//执行命令对应的业务处理方法
			CommandResult rst = iexecute(cmd);
			if (rst.getRstRecordSize() < 20) {
				logger.debug("execute client command, result:" + rst.toString());
			} else {
				logger.debug("execute client command, result size:" + rst.toString2());
			}
			
			//将命令执行结果返回给客户端
			sendCommandResult(rst);
			logger.info("send client command execute result ok!");
		} catch (Exception e) {
			logger.error("client thread run fail.", e);
			e.printStackTrace();
		} finally {
			close();
			clientManager.removeClient(this.name);
			logger.debug("client execute final. client:" + this.name);
		}
	}
	
	/**
	 * 关闭连接
	 */
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			logger.error("socket close fail.",e);
			//e.printStackTrace();
		}
		CleanConnect();
	}
}
