package com.tydic.bean;

import java.io.Serializable;

public class ClusterNodeQuotaDto implements Serializable {

	private static final long serialVersionUID = -4369494467543344559L;
	// 批次号
	private String batchNo;
	// 主机IP
	private String hostIp;
	// CPU使用率(%)
	private double cpuRateSum;
	// 磁盘使用率(%)
	private double diskTotalRate;
	// 内存使用率(%)
	private double memoryRateSum;
	// 网络输出（M）
	private double networkOutRateSum;
	// 网络输入(M)
	private double networkInRateSum;
	// 业务量
	private double bussSum;

	public ClusterNodeQuotaDto() {
		super();
	}

	public ClusterNodeQuotaDto(String batchNo, String hostIp,
			double cpuRateSum, double diskTotalRate, double memoryRateSum,
			double networkOutRateSum, double networkInRateSum) {
		super();
		this.batchNo = batchNo;
		this.hostIp = hostIp;
		this.cpuRateSum = cpuRateSum;
		this.diskTotalRate = diskTotalRate;
		this.memoryRateSum = memoryRateSum;
		this.networkOutRateSum = networkOutRateSum;
		this.networkInRateSum = networkInRateSum;
	}

	public ClusterNodeQuotaDto(String batchNo, String hostIp,
			double cpuRateSum, double diskTotalRate, double memoryRateSum,
			double networkOutRateSum, double networkInRateSum, double bussSum) {
		super();
		this.batchNo = batchNo;
		this.hostIp = hostIp;
		this.cpuRateSum = cpuRateSum;
		this.diskTotalRate = diskTotalRate;
		this.memoryRateSum = memoryRateSum;
		this.networkOutRateSum = networkOutRateSum;
		this.networkInRateSum = networkInRateSum;
		this.bussSum = bussSum;
	}

	public double getBussSum() {
		return bussSum;
	}

	public void setBussSum(double bussSum) {
		this.bussSum = bussSum;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public double getCpuRateSum() {
		return cpuRateSum;
	}

	public void setCpuRateSum(double cpuRateSum) {
		this.cpuRateSum = cpuRateSum;
	}

	public double getDiskTotalRate() {
		return diskTotalRate;
	}

	public void setDiskTotalRate(double diskTotalRate) {
		this.diskTotalRate = diskTotalRate;
	}

	public double getMemoryRateSum() {
		return memoryRateSum;
	}

	public void setMemoryRateSum(double memoryRateSum) {
		this.memoryRateSum = memoryRateSum;
	}

	public double getNetworkOutRateSum() {
		return networkOutRateSum;
	}

	public void setNetworkOutRateSum(double networkOutRateSum) {
		this.networkOutRateSum = networkOutRateSum;
	}

	public double getNetworkInRateSum() {
		return networkInRateSum;
	}

	public void setNetworkInRateSum(double networkInRateSum) {
		this.networkInRateSum = networkInRateSum;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bussSum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cpuRateSum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(diskTotalRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(memoryRateSum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(networkInRateSum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(networkOutRateSum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClusterNodeQuotaDto other = (ClusterNodeQuotaDto) obj;
		if (Double.doubleToLongBits(bussSum) != Double
				.doubleToLongBits(other.bussSum))
			return false;
		if (Double.doubleToLongBits(cpuRateSum) != Double
				.doubleToLongBits(other.cpuRateSum))
			return false;
		if (Double.doubleToLongBits(diskTotalRate) != Double
				.doubleToLongBits(other.diskTotalRate))
			return false;
		if (Double.doubleToLongBits(memoryRateSum) != Double
				.doubleToLongBits(other.memoryRateSum))
			return false;
		if (Double.doubleToLongBits(networkInRateSum) != Double
				.doubleToLongBits(other.networkInRateSum))
			return false;
		if (Double.doubleToLongBits(networkOutRateSum) != Double
				.doubleToLongBits(other.networkOutRateSum))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClusterNodeQuotaDto [batchNo=" + batchNo + ", hostIp=" + hostIp
				+ ", cpuRateSum=" + cpuRateSum + ", diskTotalRate="
				+ diskTotalRate + ", memoryRateSum=" + memoryRateSum
				+ ", networkOutRateSum=" + networkOutRateSum
				+ ", networkInRateSum=" + networkInRateSum + ", bussSum="
				+ bussSum + "]";
	}




}
