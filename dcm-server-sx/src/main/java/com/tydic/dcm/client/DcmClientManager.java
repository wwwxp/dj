package com.tydic.dcm.client;


public class DcmClientManager extends ClientManager {

	public DcmClientManager(int servPort, ClientFactory factory) {
		super(servPort, factory);
		this.setName("DcmClientManager");
	}
}
