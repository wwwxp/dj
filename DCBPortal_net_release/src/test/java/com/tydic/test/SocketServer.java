package com.tydic.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
	public static void main(String[] args) throws IOException {
		//System.out.println("------------------------------------");
		//System.out.println("Server start......");
		//System.out.println("------------------------------------");

		ServerSocket server = new ServerSocket(8888);

		while (true) {
			Socket client = server.accept();
			OutputStream out = client.getOutputStream();
			InputStream in = client.getInputStream();

			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			byte[] temp = new byte[1024];
			int realLen = 0;
			while ((realLen = in.read(temp)) != -1) {
				byteArrayOut.write(temp, 0, realLen);
			}

			byte[] recv = byteArrayOut.toByteArray();

			// 将接收的消息,发回给客户端
			out.write(recv);
			out.flush();
			// 关闭该套接字的输入流
			client.shutdownOutput();
		}
	}
}
