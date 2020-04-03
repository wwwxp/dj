package com.tydic.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class SocketClient {
	public static void main(String[] args) throws IOException {
		//System.out.println("------------------------------------");
		//System.out.println("Client start......");
		//System.out.println("------------------------------------");
		byte[] msg = new String("connect successfully!!!").getBytes();

		InetAddress inetAddr = InetAddress.getLocalHost();
		Socket client = new Socket(inetAddr, 8888);
		OutputStream out = client.getOutputStream();
		InputStream in = client.getInputStream();

		out.write(msg);
		out.flush();
		// 关闭该套接字的输出流
		client.shutdownOutput();

		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		byte[] temp = new byte[1024];
		int realLen = 0;
		while ((realLen = in.read(temp)) != -1) {
			byteArrayOut.write(temp, 0, realLen);
		}

		byte[] recv = byteArrayOut.toByteArray();

		System.out.println("Client receive msg:" + new String(recv));

		/*
		 * 切记：在这里关闭输入流，并不会使服务端的输入流到达流末尾返回-1，仅仅是释放资源而已
		 */
		in.close();
		out.close();

	}
}
