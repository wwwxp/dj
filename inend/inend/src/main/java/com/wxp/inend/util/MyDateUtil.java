package com.wxp.inend.util;

import org.elasticsearch.search.DocValueFormat;
import scala.math.BigInt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;


public class MyDateUtil {

    private MyDateUtil(){}

    private static SimpleDateFormat dateFormat,dateFormat2,format;

    static{
        dateFormat=new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss",Locale.US);
        dateFormat2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format=dateFormat2;
    }

//（1）一些格式的date的格式转化
    // dd/MMM/yyyy:HH:mm:ss -> yyyy-MM-dd
    public static String getDateByDateTime(String dateTime){

        java.util.Date parse =null;
        try {
            parse = dateFormat.parse(dateTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

       return dateFormat2.format(parse).toString().split(" ")[0];
    }

    // dd/MMM/yyyy:HH:mm:ss -> HH-mm-ss
    public static String getTimeByDateTime(String dateTime){

        java.util.Date parse =null;
        try {
            parse = dateFormat.parse(dateTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateFormat2.format(parse).toString().split(" ")[1];
    }

    // dd/MMM/yyyy:HH:mm:ss -> yyyy-MM-dd HH:mm:ss
    public static String getDateTimeByDateTime(String dateTime) {

        java.util.Date parse = null;
        try {
            parse = dateFormat.parse(dateTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateFormat2.format(parse).toString();
    }

    //js格式的时间(Fri Mar 15 2019 20:48:30) -> yyyy-MM-dd HH:mm:ss
    public static String getDateTimeByJSDateTime(String jsDateTime){
        SimpleDateFormat jsDateFormat=new SimpleDateFormat("E MMM dd yyyy HH:mm:ss", Locale.US);
        Date date=null;
        try {
            date = jsDateFormat.parse(jsDateTime);
        }catch(Exception e){
            e.printStackTrace();
        }
        return dateFormat2.format(date);
    }


//（2）date的运算

    //1.两个date的加法，返回值为“date”
        // 1）一个"yyyy-MM-dd HH:mm:ss"加上"yyyy-MM-dd HH:mm:ss这样的时间间隔"
    public static String dateAddInterval(String s_date,String s_interval){




        long d=0L;
        try {

            long date=format.parse(s_date).getTime();
            long interval=format.parse(s_interval).getTime()+62170185600000L;

            //long start=format.parse("1970-01-01 08:00:00").getTime();

            d=new BigInteger(date+"").add(new BigInteger(interval+"")).longValue();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return format.format(new Date(d));
    }

    //2.两个date的减法，返回值为“秒”，形参1是后面的日期，形参2是前面的日期

    //重载1
    public static long dateMinusDate(Date bigDate, Date smallDate){

        return (bigDate.getTime()-smallDate.getTime())/1000;
    }
    //重载2
    public static long dateMinusDate(String bigDate, String smallDate){


        long res=-1;
        try {
            res = (format.parse(bigDate)).getTime() - format.parse(smallDate).getTime();
        }catch(Exception e){
            e.printStackTrace();
        }
        return res/1000;
    }

//（3）秒的转化
    //“秒转分钟”失败的异常
    public static class SecToMinException extends Exception{
        public SecToMinException(String msg){
            super(msg);
        }
    }

    //“秒”转为“分钟”
    public static float  secToMin(long sec) throws SecToMinException{
        if(sec<0 || (sec+"").matches("\\d+\\.\\d+")){
            throw new SecToMinException("你传入的秒错误，无法转化为分钟");
        }
        return new BigDecimal(sec).divide(new BigDecimal(60.0),2,BigDecimal.ROUND_HALF_UP).floatValue();
    }

    //根据生日求年龄（有误差，暂时不考虑）
    public static int getAgeByBrithDate(String brith){
        SimpleDateFormat ageFormat=new SimpleDateFormat("yyyy-MM-dd");
       float age=-1;
        try {
           Date b = ageFormat.parse(brith);
           Date now = new Date();
           long second = dateMinusDate(now, b);
           age=second/365/24/60/60;
       }catch (Exception e){
           e.printStackTrace();
       }
       return (int)age;
    }

    public static void main(String[] args) throws Exception{
        System.out.println( getAgeByBrithDate("1996-08-27"));

    }
}
