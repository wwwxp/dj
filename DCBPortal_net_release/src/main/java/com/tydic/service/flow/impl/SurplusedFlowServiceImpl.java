package com.tydic.service.flow.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.flow.SurplusedFlowService;
import com.tydic.util.SessionUtil;

@Service("surplusedFlowImpl")
public class SurplusedFlowServiceImpl implements SurplusedFlowService {
 
    private static Logger log = Logger.getLogger(SurplusedFlowServiceImpl.class);
    @Autowired
    private CoreService coreService; 
  @Override
	public Map flowTransferQuery(Map<String, String> param, String dbKey)
			throws Exception {
	    Map resultMap = new HashMap(); 
	    resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
        resultMap.put(FrameParamsDefKey.PAGE_SIZE, 10);
        resultMap.put(FrameParamsDefKey.TOTAL, 0);
        resultMap.put(FrameParamsDefKey.DATA,  new ArrayList());
		String servno = param.get("SERVNO");
		JSONObject json = new JSONObject();
		json.put("SERVNO", servno);
		
		JSONArray array = getRemoteData(json);
		if(array == null) return resultMap;
		
		String[] groupid = new String [array.size()];
		String[] h_baseresourceid = new String [array.size()];
		for(int i=0;i<array.size();i++){
			JSONObject obj = (JSONObject)array.get(i);
			groupid[i]=obj.getString("AOFR_ID");
			h_baseresourceid[i]=obj.getString("BASERESOURCEID");
		}
		
		Map<String,String> flowParams = new HashMap<String,String>();
	

//		flowParams.put("GROUPID", StringUtils.join(groupid, ","));
		flowParams.put("GROUPID", StringUtils.join(groupid, ","));
		List<HashMap<String, String>> groupInfo = coreService.queryForList("monitorMapper.queryGroupInfo", flowParams, FrameConfigKey.ANOTHER_DATASOURCE);

		
		for(int i=0;  i<groupid.length;i++ ){
			for(HashMap<String, String> group:groupInfo ){
				BigDecimal offer_id =(BigDecimal)(Object)group.get("OFFER_ID");
				if(groupid[i].equals(offer_id.toString())){
					JSONObject obj = (JSONObject)array.get(i);
					obj.put("GROUPID_NAME", group.get("OFFER_NAME"));
					break;
				}
			}
			
		}
		
		
//		flowParams.put("H_BASERESOURCEID", StringUtils.join(h_baseresourceid, ","));
		flowParams.put("H_BASERESOURCEID", StringUtils.join(h_baseresourceid, ","));
		List<HashMap<String, String>> baseResourceInfo = coreService.queryForList("monitorMapper.queryBaseResourceInfo", flowParams, FrameConfigKey.ANOTHER_DATASOURCE);
		for(int i=0; i<h_baseresourceid.length;i++ ){
			for(HashMap<String, String> baseResource:baseResourceInfo ){
				BigDecimal ratable_resource_id =(BigDecimal)(Object)baseResource.get("RATABLE_RESOURCE_ID");
				if(h_baseresourceid[i].equals(ratable_resource_id.toString())){
					JSONObject obj = (JSONObject)array.get(i);
					obj.put("BASERESOURCEID_NAME", baseResource.get("RATABLE_RESOURCE_NAME"));
					break;
				}
			}
			
		}
		
		
		resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
        resultMap.put(FrameParamsDefKey.PAGE_SIZE, array.size());
        resultMap.put(FrameParamsDefKey.TOTAL, array.size());
        resultMap.put(FrameParamsDefKey.DATA,  array);
		return resultMap;
	}
  
   private JSONArray getRemoteData(JSONObject json){
		JSONArray array=null;
        Socket socket = null;  
        try {  
            //创建一个流套接字并将其连接到指定主机上的指定端口号  
        	String address = SessionUtil.getConfigValue("FLOW_TRANSFER_SERVER_IP");
        	int port = Integer.parseInt(SessionUtil.getConfigValue("FLOW_TRANSFER_SERVER_PORT"));
        	 log.debug("开始与【"+address+":"+port+"】建立连接...");   
            socket = new Socket(address, port);    
                
            //读取服务器端数据    
            InputStream input = socket.getInputStream();    
            //向服务器端发送数据    
            OutputStream out = socket.getOutputStream();    
            String req=json.toJSONString();
            long  writelength=req.getBytes().length;
            log.debug("请求:"+writelength+req);    
            out.write(req.getBytes());   
            out.flush();  
            byte[] headerBytes = new byte[8];
			int readSize = 0;
			int leftSize = 8;
			readSize = input.read(headerBytes, 0, leftSize);
			leftSize = leftSize - readSize;
			while (leftSize != 0) {
				readSize = input.read(headerBytes, 12 - leftSize, leftSize);
				leftSize = leftSize - readSize;
			}
			String head=new String(headerBytes);
			int readLength=(int)Long.parseLong(head.trim());
			
			//判断是否有查询结果
			if(readLength<1) return null;
			
			readSize=0;
			leftSize=readLength;
			
            byte[] readBytes =new byte[readLength];
            readSize = input.read(readBytes, 0, leftSize);
            leftSize = leftSize - readSize;
            while (leftSize != 0) {
				readSize = input.read(headerBytes, readLength - leftSize, leftSize);
				leftSize = leftSize - readSize;
			}
            
            String returnMsg=new String(readBytes);
            array=JSONArray.parseArray(returnMsg);
            log.debug("返回结果: " + array.toJSONString());
            out.close();  
            input.close();  
            return array;
        } catch (Exception e) {  
        	log.error("客户端异常:" + e.getMessage());   
        	return array;
        } finally {  
            if (socket != null) {  
                try {  
                    socket.close();  
                } catch (IOException e) {  
                    socket = null;   
                    log.error("客户端 finally 异常:" + e.getMessage());   
                }  
            }  
        }  
    
		
	}
	
	public static void main(String[] args) throws Exception {
		  
		 System.out.println("客户端启动...");    
	        if (true) {    
//	        	Thread.sleep(4000);
	            Socket socket = null;  
	            try {  
	                //创建一个流套接字并将其连接到指定主机上的指定端口号  
	                socket = new Socket("192.168.161.94", 9000);    
	                    
	                //读取服务器端数据    
	                InputStream input = socket.getInputStream();    
	                //向服务器端发送数据    
	                OutputStream out = socket.getOutputStream();    
//	                System.out.print("请输入: \t");    
//	                String str = new BufferedReader(new InputStreamReader(System.in)).readLine(); 
	                String req="{\"SERVNO\":\"13412345678\"}";
	                long  writelength=req.getBytes().length;
	                System.out.print("输入: \t"+writelength+req);    
	                out.write(req.getBytes());   
	                out.flush();  
	                byte[] headerBytes = new byte[8];
					int readSize = 0;
					int leftSize = 8;
					readSize = input.read(headerBytes, 0, leftSize);
					leftSize = leftSize - readSize;
					while (leftSize != 0) {
						readSize = input.read(headerBytes, 12 - leftSize, leftSize);
						leftSize = leftSize - readSize;
					}
					String head=new String(headerBytes);
					int readLength=(int)Long.parseLong(head.trim());
					
					readSize=0;
					leftSize=readLength;
					
	                byte[] readBytes =new byte[readLength];
	                readSize = input.read(readBytes, 0, leftSize);
	                leftSize = leftSize - readSize;
	                while (leftSize != 0) {
						readSize = input.read(headerBytes, readLength - leftSize, leftSize);
						leftSize = leftSize - readSize;
					}
	                
	                String returnMsg=new String(readBytes);
	                JSONArray array=JSONArray.parseArray(returnMsg);
	              System.out.println("服务器端返回过来的是: " + array.toJSONString());
	                	
	                 
	                  
	                out.close();  
	                input.close();  
	            } catch (Exception e) {  
	            	
	                System.out.println("客户端异常:" + e.getMessage());   
	            } finally {  
	                if (socket != null) {  
	                    try {  
	                        socket.close();  
	                    } catch (IOException e) {  
	                        socket = null;   
	                        System.out.println("客户端 finally 异常:" + e.getMessage());   
	                    }  
	                }  
	            }  
	        }    
		 }

	
	
	
	
}
