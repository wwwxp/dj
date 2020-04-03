package com.tydic.test;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.tydic.bp.common.utils.tools.BlankUtil;

public class ZkTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ZooKeeper zk = new ZooKeeper("192.168.161.25:2281", 30000, null);

		ZkTest.createNode("/brokers/mq/address/192.168.161.25:2281", zk);
	}

	/**
	 * 创建ZK目录
	 * 
	 * @param fullName
	 * @param zk
	 * @throws Exception
	 */
	private static void createNode(String fullName, ZooKeeper zk)
			throws Exception {
		String[] nodeNames = fullName.split("/");
		String node = "";
		for (int i = 0; i < nodeNames.length; i++) {
			String nodeName = nodeNames[i];
			if (BlankUtil.isBlank(nodeName)) {
				continue;
			}
			node += "/" + nodeName;
			Stat stat = zk.exists(node, null);
			if (stat != null) {
				continue;
			}
			zk.create(node, new byte[0], Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
		}
	}

}
