package com.tydic.dcm.client;

import java.net.Socket;

public class DcmClientFactory implements ClientFactory {

	public DcmClientFactory() {

	}

	@Override
	public Client createClient(Socket socket, ClientManager clientManager) {
		return new DcmClient(socket, clientManager);
	}
}
