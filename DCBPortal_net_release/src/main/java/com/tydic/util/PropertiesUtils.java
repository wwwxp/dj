package com.tydic.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.tydic.service.monitormanager.clustersummary.impl.ClusterSummaryServiceImpl;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;




public class PropertiesUtils {

	/**
	 *  读取配置文件
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getPropertiesFile(String filePath) throws Exception {
		Properties prop = new Properties();
		InputStream in = new FileInputStream(new File(filePath));
		 List<Map<String,Object>> list= new ArrayList<>();
		try {
			prop.load(in);
			
			 Iterator<Entry<Object, Object>> it = prop.entrySet().iterator();  
		        while (it.hasNext()) {  
		            Entry<Object, Object> entry = it.next();  
		            Map<String,Object> item= new HashMap<>();
		            Object key = entry.getKey();  
		            Object value = entry.getValue();  
		            item.put("key", key);
		            item.put("value", value);
		            list.add(item);
		        }  
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
	/**
	 *  读取配置文件
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static Properties getPropertiesFileByP(String filePath) throws Exception {
		Properties prop = new Properties();
		InputStream in = new FileInputStream(new File(filePath));
		try{
			prop.load(in);
			in.close();
		} catch (IOException e) {
		 
		}
		return prop;
	}

	/**
	 *  读取配置文件
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static Properties getPropertiesFileByInputStream(InputStream in) throws Exception {
		Properties prop = new Properties();

		try{
			prop.load(in);
			in.close();
		} catch (IOException e) {

		}
		return prop;
	}
	/**
	 * 更新配置文件,并生成到指定的目录
	 * @param filePath
	 * @param param
	 * @param remark
	 * @throws Exception
	 */
	public static void updatePropertiesFile(String filePath,String destFile, Map<String, String> param,String remark) throws Exception {
		Properties properties = new Properties();
		InputStream in = new FileInputStream(new File(filePath));
		properties.load(in);
		if (param != null && param.size() > 0) {
			for (Map.Entry entry : param.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				properties.setProperty(key, value);
			}
		}
		File file = new File(destFile);
		if(!file.exists()){
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(destFile);
		properties.store(fos, remark);
		fos.close();
		in.close();
	}
	/**
	 * 得很yaml文件 的所有属性，并以String 类型输出
	 * @param filepath 目标文件
	 * @return
	 * @throws Exception
	 */
	
	public static List<Map<String,Object>> getYamlFile(String filepath) throws Exception{    

		Yaml yaml = new Yaml();
        Map ret;
        try {
            ret = (Map) yaml.load(new FileReader(filepath));
        } catch (Exception e) {
            ret = null;
        }
        List<Map<String,Object>> list= new ArrayList<>();
		 Set confKey= ret.keySet();
		 Iterator iterator=confKey.iterator();
		 while(iterator.hasNext()){
			 Map<String,Object> item= new HashMap<>();
			 String key=(String)iterator.next();
			 item.put("key", key);
			/* Object obj = ret.get(key);
			 if(obj instanceof ArrayList){
				 StringBuffer tempStr = new StringBuffer();
				 List temp = (ArrayList)obj;
				 Iterator iter = temp.iterator();  
			        while(iter.hasNext())  
			        {  
			        	tempStr.append(iter.next()).append(" ");
			        } 
			        item.put("value", tempStr.toString());
			 }else{
				 item.put("value", ret.get(key));
			 }*/
			 item.put("value", ret.get(key));
			 
			 list.add(item);
		 }
		 System.out.println(list);
        return list;
	}
	/**
	 * 得很yaml文件 的所有属性，并以Map 类型输出
	 * @param filepath 目标文件
	 * @return
	 * @throws Exception
	 */
	
	public static Map getYamlFileByMap(String filepath) throws Exception{    

		Yaml yaml = new Yaml();
        Map ret;
        try {
            ret = (Map) yaml.load(new FileReader(filepath));
        } catch (Exception e) {
            ret = new HashMap();
        }
        return ret;
	}
	/**
	 * 获取文件，然后更新yaml文件，并放到指定的目录 ，原文件不更新
	 * @param filePath 原文件
	 * @param destFile 产生的新文件
	 * @param param
	 * @throws Exception
	 */
	public static void updateYamlFile(String filePath,String destFile, Map<String, Object> param) throws Exception{
		Yaml yaml = new Yaml();
        Map<String,Object> ret = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filePath);
            ret = (Map<String,Object>) yaml.load(fileReader);
            
        } catch (Exception e) {
            ret = null;
        }finally {
        	fileReader.close();
        	if(ret == null){
        		ret = new HashMap<String, Object>();
        	}
		}
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
         yaml = new Yaml(options);
       
         if (param != null && param.size() > 0) {
 			for (Map.Entry<String,Object> entry : param.entrySet()) {
 				String key = entry.getKey().toString();
 				Object value = entry.getValue();
 				ret.put(key, value);
 				
 			}
 		}
        String data = yaml.dump(ret);
        File file = new File(destFile);
		if(!file.exists()){
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
        FileUtils.writeStringToFile(file, data,false);
        
        FileReader fr=new FileReader(file);
        BufferedReader br=new BufferedReader(fr);
        String line="";
        StringBuffer sb = new StringBuffer();
        while ((line=br.readLine())!=null) {
           sb.append(" ").append(line).append(System.getProperty("line.separator"));
        }
        br.close();
        fr.close();
        FileUtils.writeStringToFile(file, sb.toString(),false);
        //System.out.println(data);
	}
	
	/**
	 * 获取文件，然后更新原yaml文件，并放到指定的目录  
	 * @param filePath 原文件 
	 * @param param
	 * @throws Exception
	 */
	public static void updateYamlFile(String filePath,Map<String, Object> param) throws Exception{
		Yaml yaml = new Yaml();
		Map ret;
		try {
			ret = (Map) yaml.load(new FileReader(filePath));
		} catch (Exception e) {
			e.printStackTrace();
			ret = null;
		}
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);

		if (param != null && param.size() > 0) {
			for (Map.Entry entry : param.entrySet()) {
				String key = entry.getKey().toString();
				Object value = entry.getValue();
				ret.put(key, value);
			}
		}
		String data = yaml.dump(ret);
		File file = new File(filePath);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileUtils.writeStringToFile(file, data, false);
	}

	public static void main(String[] args) throws Exception {
		Map<String, Object> param = new HashMap<>();
		List<String> list =new ArrayList();
		list.add("192.168.111.111");
		list.add("192.168.111.112");
		list.add("192.168.111.113");
		param.put("storm.zookeeper.servers",list);
		updateYamlFile("F:\\SourceCode\\1_Develop\\38_BP\\深圳研发\\公共管理\\BP_MAVEN\\bp_frame\\net_test\\DCBPortal_net_release\\target\\DCBPortal_net_release\\WEB-INF\\classes\\storm.yaml",param);

		for (int i=0; i<5; i++) {
			new Thread(new Runnable() {

				private int index;
				public Runnable add(int i) {
					this.index = i;
					return this;
				}

				@Override
				public void run() {
					Map<String, Object> param = new HashMap<>();
					List<String> list =new ArrayList();
					list.add("192.168.111." + index);
					list.add("192.168.112." + index);
					list.add("192.168.113." + index);
					param.put("storm.zookeeper.servers",list);
					System.out.println("开始 ---> " + index);
					try {
						String filePath = "F:\\SourceCode\\1_Develop\\38_BP\\深圳研发\\公共管理\\BP_MAVEN\\bp_frame\\net_test\\DCBPortal_net_release\\target\\DCBPortal_net_release\\WEB-INF\\classes\\storm.yaml";
						ClusterSummaryServiceImpl summaryService = new ClusterSummaryServiceImpl();
						summaryService.initStormYaml(filePath, String.valueOf(index));
						//PropertiesUtils.updateYamlFile("F:\\SourceCode\\1_Develop\\38_BP\\深圳研发\\公共管理\\BP_MAVEN\\bp_frame\\net_test\\DCBPortal_net_release\\target\\DCBPortal_net_release\\WEB-INF\\classes\\storm.yaml",param);
						//System.out.println("结束 ---> " + index);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}.add(i)).start();
		}

	}



}
