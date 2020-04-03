package com.tydic.test;

import java.util.HashMap;
import java.util.Map;

import PluSoft.Utils.JSON;

public class JsonTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> linkParams = new HashMap<>();
		linkParams.put("IP", "192.168.161.89");
		linkParams.put("REMOTE_PATH", "/public/bp/zte");
		linkParams.put("port", "22");

		Map<String, Object> boltParams = new HashMap<String, Object>();
		boltParams.put("DEV_ID", 101488);
		boltParams.put("DEV_NAME", "ZTE-GZ-001");
		boltParams.put("DEV_PARAMS", linkParams);

		String rst = JSON.Encode(boltParams);
		System.out.println("获取的参数为:" + rst);
		
		Map<String, Object> decodeMap = (Map<String, Object>) JSON.Decode(rst);
		System.out.println(decodeMap.get("DEV_ID"));
		Map<String, String> decodeLinkParams = (Map<String, String>) decodeMap.get("DEV_PARAMS");
		System.out.println(decodeLinkParams.get("IP"));
	}

}
