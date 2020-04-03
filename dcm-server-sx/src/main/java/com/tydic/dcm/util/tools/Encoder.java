package com.tydic.dcm.util.tools;

import java.util.Scanner;

/**
 * 
 * @ClassName: Encoder
 * @Description: 链路密码加解密
 * @Prject: dcm-server_base
 * @author: yuanH
 * @date 2016年7月28日 下午2:37:58
 */
public class Encoder {

	private Encoder() {
	}

	/**
	 * Encode解密
	 * 
	 * @param decodeStr 需要界面的字串
	 * @return
	 */
	public static String decode(String decodeStr) {
		StringBuffer buf = new StringBuffer();
		int length = decodeStr.length();
		if ((length % 2) != 0)
			throw new IllegalArgumentException("Decode String length illegal!");

		for (int i = 0; i < length; i = i + 2) {
			String temp = decodeStr.substring(i, i + 2);
			byte value = Integer.valueOf(temp, 16).byteValue();
			char ch = (char) ((~value) - 1);
			buf.append(ch);
		}
		return buf.toString();
	}

	/**
	 * encode加密
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			byte b = (byte) ~((byte) ch + 1);
			String s = Integer.toHexString(b);
			buf.append(s.substring(s.length() - 2));
		}
		return buf.toString();
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
        System.out.println("请选择你的操作:(1:加密, 2:解密)");
        int type = scanner.nextInt();
        if (type == 1) {
        	System.out.println("请输入要加密的明文:");
        	String encryptValue = encode(scanner.next());
        	System.out.println("加密后的结果为:" + encryptValue);
        } else if (type == 2) {
        	System.out.println("请输入要解密的密文:");
        	String encryptValue = decode(scanner.next());
        	System.out.println("解密后的结果为:" + encryptValue);
        } else {
        	System.out.println("请按照上面的提示进行操作，请选择1或者2");
        }
	}
}
