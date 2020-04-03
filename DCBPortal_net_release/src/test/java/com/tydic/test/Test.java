package com.tydic.test;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.tools.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;


public class Test {

    /**
     * @param args
     * @throws UnknownHostException 
     */
    public static void main(String[] args) throws UnknownHostException {
    	//String sourceFile = "env/jstorm.zip";
    	//String tt = FileTool.exactPath(sourceFile.substring(0, sourceFile.lastIndexOf("/"))) + "1.0.0" + sourceFile.substring(sourceFile.lastIndexOf("/"));
    	//System.out.println("tt--->" +tt);
//    	int s_k36_1s = 2144;
//    	int s_k33 = 2943;
//    	System.out.println(((s_k36_1s/s_k33) * 100)+"%");
//    	
//    	 DecimalFormat df = new DecimalFormat("0.00");//格式化小数    
//         String num = (df.format((float)s_k36_1s * 100 /s_k33)) + "%";//返回的是String类型    
//         System.out.println(num);
    	
    	//System.out.println("/jstorm1".equalsIgnoreCase("/jstorm1"));
//    	String envPath = "$CP/tools/env/$MV//m2db//config//$MV/dssd";
//    	envPath = envPath.replace("$MP", "aaaa").replaceAll("\\$MV", "3.0.5").replaceAll("\\/\\/", "/");
//    	System.out.println("envPath ---> " + envPath);
    	
//    	String topic = "Sta_RE_rat.WrCdrGroup_Sta_RE_rat";
//    	String topicName = topic;
//		if (!BlankUtil.isBlank(topic) && topic.indexOf(".") > -1) {
//			topicName = topic.split("\\.")[0];
//		}
//		System.out.println("topicName ---> " + topicName);
    	
    	
    	String curFile = "/X3KF/Dtl_MaAnShan/SS455520171213170470182.dat(0)";
    	
    	String realFiless = null;
    	if (curFile.indexOf("(") != -1 && curFile.indexOf(")") != -1) {
    		realFiless = curFile.substring(0, curFile.lastIndexOf("("));
    	}
    	
    	//System.out.println("dsdsdssdsd");
    	//Runtime.getRuntime().halt(1);
    	//System.out.println("dddddddddd");
    	//System.out.println("realFiless ---> " + realFiless);
    	
    	//String currentPath = System.getProperty("user.dir");
    	//System.out.println("currentPath ---> " + currentPath);
    	
    	InetAddress address = InetAddress.getLocalHost();
    	System.out.println("address ---> " + address.getHostName());

		System.out.println(String.format("aaa%sdsddsd%d", "aaa", 34));

		System.out.println("/project/ducc033/sx_bill/data/dcstand/split/6113/917/EXI_GMSC_BJxff_MSCE1_XAVMSC118090204993452.dat".replace("/project/ducc03/sx_bill/data/dcstand/split/6113/917/", ""));

		String fullStr = "/project/ducc03/sx_bill/data/dcstand/split/6113/917/EXI_GMSC_BJxff_MSCE1_XAVMSC118090204993452.dat";
		String repalceStr = "/project/ducc03/sx_bill/data/dcstand/split/6113/917/";
		String result = StringUtils.removeStart(fullStr, repalceStr);
		String result2 = StringUtils.removeEnd(fullStr, repalceStr);
		System.out.println("result --->" + result);
		System.out.println("result2 --->" + result2);


		System.out.println("1111111111111111111111111111111111111111111111111".compareTo("11111111111111111111111111111111111111111111111112"));


		String modifyTime = "20190909231152";
		if (StringUtils.indexOf(modifyTime, ".") > 0) {
			modifyTime = modifyTime.substring(0, modifyTime.indexOf("."));
		}
		long modifyTimes = DateUtil.parse(modifyTime, "yyyyMMddHHmmss").getTime();
		System.out.println("modifyTime ---> " + modifyTime);
		System.out.println("modifyTimes ---> " + modifyTimes);


//		Vector<String> aa = new Vector<String>();
//		aa.add("ABC");
//		aa.add("ABCq");
//		aa.add("ABCw");
//		aa.add("ABCe");
//		aa.add("ABCr");
//		aa.add("ABCt");
//		aa.add("ABCy");
//		System.out.println(averageAssign(aa, 3));
//
//
//		Map map = Maps.newHashMap();
//		LinkedList list = new LinkedList();
//		//ArrayList list = new ArrayList();
//		list.add(7);
//		list.add(2);
//		list.add(3);
//		list.add(4);
//		System.out.println("list --> " + list);
//		list.sort(new Comparator() {
//			@Override
//			public int compare(Object o1, Object o2) {
//
//				return o1.toString().compareTo(o2.toString());
//			}
//		});
//		System.out.println(list);

		System.out.println(JSON.parse(""));
	}

	/**
	 * 将一组数据平均分成n组
	 *
	 * @param source 要分组的数据源
	 * @param n      平均分成n组
	 * @param <T>
	 * @return
	 */
	public static <T> Vector<Vector<T>> averageAssign(Vector<T> source, int n) {
		Vector<Vector<T>> result = new Vector<Vector<T>>();
		int remainder = source.size() % n;  //(先计算出余数)
		int number = source.size() / n;  //然后是商
		int offset = 0;//偏移量
		for (int i = 0; i < n; i++) {
			List<T> value = null;
			Vector<T> vectorList = null;
			if (remainder > 0) {
				value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
				remainder--;
				offset++;
			} else {
				value = source.subList(i * number + offset, (i + 1) * number + offset);
			}
			vectorList = new Vector<T>(value.size());
			vectorList.addAll(value);
//			for (int j=0; j<value.size(); j++) {
//				vectorList.add(value.get(j));
//			}
			//Collections.copy(vectorList, value);
			result.add(vectorList);
		}
		return result;
	}
}
