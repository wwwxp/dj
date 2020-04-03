package com.tydic.job.impl;

import com.tydic.bean.ClusterNodeQuotaDto;
import com.tydic.util.BusinessConstant;

import java.text.NumberFormat;
import java.util.*;

public class CreateData {

	public static void addTempData(List<ClusterNodeQuotaDto> list, String hostIp) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		//nf.setMinimumFractionDigits(2);
		//nf.setRoundingMode(RoundingMode.HALF_UP);
		for (int i = 0; i < 30; i++) {
			Double randNum = Math.random() * Math.random() * 100;
			randNum = Double.parseDouble(String.format("%.0f", randNum));
			
			Double randBus = Math.random() * Math.random() * 100000;
			randBus = Double.parseDouble(String.format("%.0f", randBus));
			
			ClusterNodeQuotaDto dto = new ClusterNodeQuotaDto(i + 1 + "", hostIp, Double.parseDouble(String.format("%.0f", randNum)), Double.parseDouble(String.format("%.0f", randNum)), Double.parseDouble(String.format("%.0f", randNum)), Double.parseDouble(String.format("%.0f", randNum)), randNum, randBus);
			System.out.println("dto ---> " + dto);
			list.add(dto);
		}
	}
	
	public static void addTempData(List<ClusterNodeQuotaDto> list, String hostIp, int count) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		//nf.setMinimumFractionDigits(2);
		//nf.setRoundingMode(RoundingMode.HALF_UP);
		for (int i = 0; i < count; i++) {
			Double randNum = Math.random() * Math.random() * 100;
			randNum = Double.parseDouble(String.format("%.0f", Math.random() * Math.random() * 100));
			
			Double randBus = Math.random() * Math.random() * 100000;
			randBus = Double.parseDouble(String.format("%.0f", randBus));
			
			ClusterNodeQuotaDto dto = new ClusterNodeQuotaDto(i + 1 + "", hostIp, Double.parseDouble(String.format("%.0f", randNum)), 
					Double.parseDouble(String.format("%.0f", Math.random() * Math.random() * 100)), 
					Double.parseDouble(String.format("%.0f", Math.random() * Math.random() * 100)), 
					Double.parseDouble(String.format("%.0f", Math.random() * Math.random() * 100)), 
					Double.parseDouble(String.format("%.0f", Math.random() * Math.random() * 100)), randBus);
			System.out.println("dto ---> " + dto);
			list.add(dto);
		}
	}
	
	/**
	 * 获取连续批次中最小值
	 * @param hostQuotaList
	 * @param quotaType
	 */
	public static List<HashMap<String, Object>> getMaxQuotaValue(List<ClusterNodeQuotaDto> hostQuotaList, final String quotaType) {
		for (ClusterNodeQuotaDto clusterNodeQuotaDto : hostQuotaList) {
			System.out.println("排序前数据--->" + clusterNodeQuotaDto);
		}
		Collections.sort(hostQuotaList, new Comparator<ClusterNodeQuotaDto>() {
			@Override
			public int compare(ClusterNodeQuotaDto quotaDto1, ClusterNodeQuotaDto quotaDto2) {
				if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) {
					if(quotaDto1.getCpuRateSum() <= quotaDto2.getCpuRateSum()){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) {
					if(quotaDto1.getMemoryRateSum() <= quotaDto2.getMemoryRateSum()){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) {
					if(quotaDto1.getDiskTotalRate() <= quotaDto2.getDiskTotalRate()){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_4.equals(quotaType)) {
					if((quotaDto1.getNetworkInRateSum() + quotaDto1.getNetworkOutRateSum()) <= (quotaDto2.getNetworkInRateSum() + quotaDto2.getNetworkOutRateSum())){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_5.equals(quotaType)) {
					if(quotaDto1.getBussSum() <= quotaDto2.getBussSum()){
						return -1;
					}
					return 0;
				}
				return 0;
			}
			
		});
		
		for (ClusterNodeQuotaDto clusterNodeQuotaDto : hostQuotaList) {
			System.out.println("排序后数据---*" + clusterNodeQuotaDto);
		}
		
		HashSet<String> hostSet = new HashSet<String>();
		for (ClusterNodeQuotaDto nodeDto : hostQuotaList) {
			hostSet.add(nodeDto.getHostIp());
		}
		
		List<HashMap<String, Object>> retList = new ArrayList<HashMap<String, Object>>();
		for (String hostIp : hostSet) {
			HashMap<String, Object> hostMap = new HashMap<String, Object>();
			List<ClusterNodeQuotaDto> hostList = new ArrayList<ClusterNodeQuotaDto>();
			hostMap.put("HOST_IP", hostIp);
			for (ClusterNodeQuotaDto nodeDto : hostQuotaList) {
				if (nodeDto.getHostIp().equals(hostIp)) {
					hostList.add(nodeDto);
				}
			}
			hostMap.put("HOST_LIST", hostList);
			retList.add(hostMap);
		}
		
		//获取每个节点在连续的几次资源数据中最小值
		if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getCpuRateSum());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getCpuRateSum());
			}
		} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getMemoryRateSum());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getMemoryRateSum());
			}
		} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getDiskTotalRate());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getDiskTotalRate());
			}
		} else if (BusinessConstant.PARAMS_BUS_4.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getNetworkInRateSum() +"-"+hostNodeList.get(0).getNetworkOutRateSum());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getNetworkInRateSum() + "-" + hostNodeList.get(hostNodeList.size()-1).getNetworkOutRateSum());
			}
		} 
		
		for (int i = 0; i < retList.size(); i++) {
			System.out.println("retList ---> " + retList.get(i));
		}
		
		return null;
	}


	
	public static void main(String[] args) {
		List<ClusterNodeQuotaDto> list = new ArrayList<ClusterNodeQuotaDto>();
		CreateData.addTempData(list, "192.168.161.27");
		
		List<ClusterNodeQuotaDto> list2 = new ArrayList<ClusterNodeQuotaDto>();
		CreateData.addTempData(list2, "192.168.161.28");
		
		List<ClusterNodeQuotaDto> list3 = new ArrayList<ClusterNodeQuotaDto>();
		list3.addAll(list2);
		list3.addAll(list);
		
		CreateData.getMaxQuotaValue(list3, "1");
	}
}
