package com.tydic.dcm.client;

import java.net.Socket;

public interface ClientFactory {

	public Client createClient(Socket socket, ClientManager clientManager);
}
