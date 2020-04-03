package com.tydic.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			properties.setProperty("name", "jack");

			FileOutputStream fos = new FileOutputStream("aa.properties");
			properties.store(fos, "测试写入数据");
			fos.flush();
			fos.close();
			System.out.println("写入成功了...");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
