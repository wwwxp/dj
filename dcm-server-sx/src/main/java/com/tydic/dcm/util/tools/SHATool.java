package com.tydic.dcm.util.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Auther: Yuanh
 * Date: 2019-04-02 16:48
 * Description:
 */
public class SHATool {
    /**
     * 利用java原生的摘要实现SHA256加密
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256StrJava(String str){
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }
    /**
     * 将byte转为16进制
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i=0;i<bytes.length;i++){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString().toUpperCase();
    }

    public static void main(String[] args) {
        System.out.println(SHATool.getSHA256StrJava("AAAAAA"));
        System.out.println(SHATool.getSHA256StrJava("BBBBBB"));
        System.out.println(SHATool.getSHA256StrJava("中国的但是多所 #$@*@*^*)(@^#*)(@#GLJGLJLKJDSHSHISYHASJDvvvvvvvvvvvvvvvvvv433333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333"));
        System.out.println(SHATool.getSHA256StrJava("中国的但是多所 #$@*@*^*)(@^#*)(@#GLJGLJLKJDSHSHISYHASJDvvvvvvvvvvvvvvvvvv433333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333"));
        System.out.println(SHATool.getSHA256StrJava("AAAAAA"));
        System.out.println(SHATool.getSHA256StrJava("1"));
        System.out.println(SHATool.getSHA256StrJava("2"));

    }
}
