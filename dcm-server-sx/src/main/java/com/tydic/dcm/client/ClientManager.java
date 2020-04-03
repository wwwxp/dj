package com.tydic.dcm.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

public class ClientManager extends Thread {

	private static Logger logger = Logger.getLogger(ClientManager.class);
	
	protected int servPort;
	protected ServerSocket serverSocket = null;
	protected ClientFactory clientFactory = null;
	protected Hashtable<String, Client> clientTables = new Hashtable<String, Client>();
	
	/**
	 * @param servPort 客户端连接端口
	 * @param clientFactory 创建客户端对象
	 */
	public ClientManager(int servPort, ClientFactory clientFactory) {
		this.servPort = servPort;
		this.clientFactory = clientFactory;
		this.setName("ClientManager");
	}

	
	/**
	 * 获取客户端连接
	 * @return
	 */
	public boolean open() {
		logger.debug("begin connect server socket..");
		
		boolean result = true;
		try {
			serverSocket = new ServerSocket(this.servPort);
		} catch (Exception e) {
			result = false;
			logger.error("server socket create fail.", e);
			e.printStackTrace();
		}
		logger.debug("end connect server socket, result:" + result);
		return result;
	}
	
	/**
	 * 移除Client对象
	 * @param name
	 */
	public void removeClient(String name) {
		clientTables.remove(name);
	}
	
	/**
	 * 获取客户端对象
	 * @param name
	 * @return
	 */
	public Client getClient(String name) {
		return clientTables.get(name);
	}
	
	/**
	 * 客户线程退出
	 */
	public void exit() {
		logger.debug("begin client manager exit");
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("server socket close fail.",e);
		}
		
		Enumeration<String> keys = clientTables.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			Client client = clientTables.remove(key);
			client.close();
			logger.debug("client close, client:" + client.name);
		}
		logger.debug("end client manager exit");
	}
	
	/**
	 * 线程专门用来监听客户端接入
	 */
	@Override
	public void run() {
		logger.debug("ClientManager start..");
		while(true) {
			try {
				Socket socket = serverSocket.accept();
				logger.debug("get a new socket connect, socket:" + socket.toString());
				System.out.println("get a new socket connect, socket:" + socket.toString());
				
				Client client = clientFactory.createClient(socket, this);
				if (client.initConnect()) {
					clientTables.put(client.name, client);
					client.start();
					logger.info("client thread start ok...");
				}
			} catch (IOException e) {
				logger.error("create client fail.", e);
				System.out.println("create client fail, io exception info:" + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				logger.error("create client fail.", e);
				System.out.println("create client fail, exception info:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
