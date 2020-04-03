package com.tydic.dcm.util.jdbc;

import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.dca.DCAClient;
import com.tydic.dca.config.DCAMConfig;
import com.tydic.dca.connection.DCAConnector;
import com.tydic.dcm.util.tools.ParamsConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: tianjc
 * Date: 2018-10-22
 * Time: 14:09
 */
@Slf4j
public class DcaUtil {
    //以链路id保存dca连接
    private static ConcurrentHashMap<String, DCAClient> map = new ConcurrentHashMap<>();

    /**
     * 初始化dca连接池
     */
    public static DCAClient init(String devId) {
        //获取连接池
        DCAClient client = map.get(devId);
        if (client == null) {
            log.info("开始创建dca连接池");

            String dcaMasterIp = SystemProperty.getContextProperty(ParamsConstant.DCA_IP);
            Integer dcaMasterPort = NumberUtils.toInt(SystemProperty.getContextProperty(ParamsConstant.DCA_PORT));
            String dcaSlaveIp = SystemProperty.getContextProperty(ParamsConstant.DCA_SLAVE_IP);
            Integer dcaSlavePort = NumberUtils.toInt(SystemProperty.getContextProperty(ParamsConstant.DCA_SLAVE_PORT));

            String dcaAcctId = SystemProperty.getContextProperty(ParamsConstant.DCA_ACCT_ID);
            String dcaUserName = SystemProperty.getContextProperty(ParamsConstant.DCA_USER_NAME);
            String dcaUserPwd = SystemProperty.getContextProperty(ParamsConstant.DCA_PASSWD);
            dcaUserPwd = DesTool.dec(dcaUserPwd);

            //初始化dcas连接
            DCAMConfig dcamConfig = new DCAMConfig(dcaMasterIp, dcaMasterPort, dcaSlaveIp, dcaSlavePort, dcaAcctId, dcaUserName, dcaUserPwd);
            log.info("DCA info--->masterIp:{}, masterPort:{}, slaveIp:{}, slavePort:{}, AcctId:{}, userName:{}, userPwd:{}",
                    dcaMasterIp, dcaMasterPort, dcaSlaveIp, dcaSlavePort, dcaAcctId, dcaUserName, dcaUserPwd);

            DCAConnector dcaConnector = new DCAConnector(dcamConfig);
            client =  dcaConnector.connect();
            log.info("DCA Client --->{}", client);
            map.put(devId, client);
        }
        return client;
    }

    /**
     * 从连接池获取dca连接
     *
     * @return
     */
    public static DCAClient getConnection(String devId) {
        DCAClient client = map.get(devId);

        //判断连接是否有效
        if (client == null) {
            log.error("获取dca连接失败,devId:{}", devId);

            //重连
            client = reconnect(devId);
        }

        //判断现有连接是否有效
        boolean isConnected = false;
        try {
            isConnected = client.isConnected();
        } catch (Exception e) {
            log.warn("dca连接已关闭,devId:{}", devId, e);
        }
        if (!isConnected) {
            //重连
            client = reconnect(devId);
        }

        return client;
    }

    /**
     * 关闭dca连接
     *
     * @param devId
     * @param client
     */
    public static void close(String devId, DCAClient client) {
        //删除无效链接
        map.remove(devId);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("关闭dca连接失败,devId:{}", devId, e);
            }
        }
    }

    /**
     * 关闭dca连接
     *
     * @param devId
     */
    public static void close(String devId) {
        DCAClient client = map.get(devId);
        //删除无效链接
        map.remove(devId);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("关闭dca连接失败,devId:{}", devId, e);
            }
        }
    }

    /**
     * 重连
     *
     * @param devId
     * @return
     */
    public static DCAClient reconnect(String devId) {
        //先尝试关闭之前的连接
        DCAClient client = map.get(devId);
        close(devId, client);

        //重新初始化
        return init(devId);
    }

    /**
     * 添加
     *
     * @param devId
     * @param key
     * @return
     */
    public static boolean exists(String devId, String key) {
        log.debug("begin check redis key, devId:{}, key:{}, cache connection:{}", devId, key, map);

        //默认Key已经存在，否则当DCA异常，就会出现文件重复采集问题
        boolean ret = true;
        int errorCnt = 0;
        DCAClient client = null;
        while (errorCnt < 3) {
            try {
                client = DcaUtil.getConnection(devId);
                ret = client.exists(key);
                if (errorCnt > 0) {
                    log.info("reconnection check redis key success， devId:{}, key:{}, ret:{}", devId, key, ret);
                }
                break;
            } catch (Exception e) {
                ++errorCnt;
                log.error("check redis key exception, devId:{}, key:{}, reconnection count:{}, client:{}", devId, key, errorCnt, client, e);
                DcaUtil.close(devId, client);
            } catch (Error e) {
                ++errorCnt;
                log.error("check redis key error, devId:{}, key:{}, reconnection count:{}, client:{}", devId, key, errorCnt, client, e);
                DcaUtil.close(devId, client);
            }
        }
        //添加是否成功
        return ret;
    }


    /**
     * 添加
     *
     * @param devId
     * @param key
     * @param expire
     * @param value
     * @return
     */
    public static boolean setex(String devId, String key, int expire, String value) {
        log.debug("begin set redis key, devId:{}, key:{}, expire:{}, value:{}", devId, key, expire, value);
        DCAClient client = null;
        boolean setOk = false;
        int errorCnt = 0;
        while (errorCnt < 3) {
            try {
                client = DcaUtil.getConnection(devId);
                setOk = client.setex(key, expire, value);
                if (errorCnt > 0) {
                    log.info("reconnection set redis key success， devId:{}, key:{}", devId, key);
                }
                break;
            } catch (Exception e) {
                ++errorCnt;
                log.error("set redis key exception, devId:{}, key:{}, expire:{}, reconnection count:{}, client: {}", devId, key, expire, errorCnt, client, e);
                DcaUtil.close(devId, client);
            } catch (Error e) {
                ++errorCnt;
                log.error("set redis key error, devId:{}, key:{}, expire:{}, reconnection count:{}, client: {}", devId, key, expire, errorCnt, client, e);
                DcaUtil.close(devId, client);
            }
        }
        return setOk;
    }

    /**
     * 从dca中删除key
     *
     * @param devId
     * @param keys
     */
    public static void delKey(String devId, String... keys) {
        log.debug("begin delete redis key, devId:{}, key:{}", devId, keys);
        try {
            if (ArrayUtils.isNotEmpty(keys)) {
                for (String key : keys) {
                    try {
                        Long result = DcaUtil.getConnection(devId).del(key);

                        //是否删除成功
                        if (result != 1 && result != 0) {
                            log.error("从dca删除key失败,devId:{},key:{},result:{}", devId, key, result);
                        }
                    } catch (Exception e) {
                        log.error("从dca删除key失败,devId:{},key:{}", devId, key, e);
                    } catch (Error e) {
                        log.error("从dca删除key错误,devId:{},key:{}", devId, key, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("从dca删除无效数据失败,devId:{},keys:{}", devId, keys);
        }
    }

    /**
     * 获取当前进程号
     *
     * @return
     */
    public static final int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println(runtimeMXBean.getName());
        return NumberUtils.toInt(runtimeMXBean.getName().split("@")[0]);
    }


    public static void main(String[] args) {
        int PID = getProcessID();
        String PNAME = "DCM_SERVER_" + PID;


        String dcaMasterIp = "192.168.161.13";
        Integer dcaMasterPort = 17021;
        String dcaSlaveIp = "192.168.161.13";
        Integer dcaSlavePort = 17021;

        String dcaAcctId = "billing";
        String dcaUserName = "tydichyp";
        String dcaUserPwd = "5DDC45A2FFF468B9";
        dcaUserPwd = DesTool.dec(dcaUserPwd);

        //初始化dcas连接
        DCAMConfig dcamConfig = new DCAMConfig(dcaMasterIp, dcaMasterPort, dcaSlaveIp, dcaSlavePort, dcaAcctId, dcaUserName, dcaUserPwd);
        log.info("DCA info--->{}", dcamConfig.toString());

        DCAConnector dcaConnector = new DCAConnector(dcamConfig);
        DCAClient client =  dcaConnector.connect();
        log.info("DCA Client --->{}", client);

        setex("131", "DCM.131.ZTESOFT.r", 300000, "1");
        System.out.println("DATA add Success!");

        boolean isExists = exists("131", "BILLING.131#TYDIC_DATA_888_654.log#/public/bp/DCM_SERVER/SOURCE_COLL/");
        String keyValue = client.get("BILLING.131#TYDIC_DATA_888_654.log#/public/bp/DCM_SERVER/SOURCE_COLL/");
        System.out.println("Exists --->" + isExists + ", keyValue--->" + keyValue);
    }

}
