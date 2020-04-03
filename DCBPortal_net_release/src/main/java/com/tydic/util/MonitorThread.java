//package com.tydic.util;
//
//public class MonitorThread extends Thread {
//	private long overtime = 0l;
//	public static boolean flag = false;
//
//	public MonitorThread() {
//	}
//
//	public MonitorThread(long overtime) {
//		this.overtime = overtime;
//	}
//
//	@Override
//	public void run() {
//		flag = false;
//		try {
//			System.out.println("超时：" + overtime);
//			Thread.sleep(overtime);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		if (!RemoteCommand.judge) {
//			flag = true;
//		}
//	}
//}
