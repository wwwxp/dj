package com.tydic.util;


import com.tydic.bp.core.utils.properties.SystemProperty;
import org.apache.commons.lang3.StringUtils;

public class Constant {
	public static final String SUCCESS = "Success";
	public static final String OPERATOR_SUCCESS = " Success.";
	public static final String OPERATOR_FAILED = " Failed.";
	public static final String FAILED = "failed";
	public static final String FAIL = "fail";
	public static final String ERROR = "error";
	public static final String EXCEPTION = "Exception";
	
	//返回编码
	public static final String FLAG_ERROR = "$$";
	public static final String FLAG_RECOVERT_ERROR = "\\$\\$";
	public static final String RST_STR = "RST_STR";
	public static final String RST_CODE = "RST_CODE";
	public static final String RST_CODE_SUCCESS = "1";
	public static final String RST_CODE_FAILED = "0";
	public static final String LINE_FLAG = "\n";
	
	/**
	 * 线程池初始化大小
	 */
	public static int THREAD_POOL_SIZE = 5;
	
	/**
	 * 一批次处理条数
	 */
	public static int THREAD_HANDLE_NUM = 10;
	
	/**
	 * 时间
	 */
	public static int THREAD_HANDLE_TIMES = 5000;
	
	/**
	 * ftp文件地址子目录名
	 */
	public static final String PLAT_CONF="platform_config/";
	
	/**
	 * ftp文件地址子目录名
	 */
	public static final String BUSS_CONF="business_config/";
	
	/**
	 * ftp文件地址子目录名
	 */
	public static final String RELEASE="release";
	
	/**
	 *  ftp文件地址子目录名
	 */
	public static final String RELEASE_DIR="release/";
	
	/**
	 * env 目录 名
	 */
	public static final String ENV = SystemProperty.getContextProperty("env_dir");

	/**
	 * 部署云平台主机IPV6网卡
	 */
	public static final String LOCAL_NET_CARD = StringUtils.defaultString(SystemProperty.getContextProperty("local.netcard"), "");
	
	/**
	 * default 目录 名
	 */
	public static final String DEFAULT = "default/";
	
	/**
	 * default 目录 名
	 */
	public static final String T_DEFAULT = "default";
	
	/**
	 * cluster_default 名称
	 */
	public static final String CLUSTER_DEFAULT = "cfg_templet";
	
	/**
	 * cluster_default 目录
	 */
	public static final String CLUSTER_DEFAULT_DIR = "cfg_templet/";
	
	/**
	 * release目录标志
	 */
	public static final String ROOT_NODE_FLAG="rootNodeFlag";
	
	/**
	 * m2db
	 */
	public static final String M2DB = "m2db";
	/**
	 * m2db 目录 名
	 */
	public static final String M2DB_DIV = "m2db/";
	
	/**
	 * shell脚本文件 目录 名
	 */
	public static final String LOG_DIR = "logs/";
	
	/**
	 * 远程主机子程序配置文件目录名称
	 */
	public static final String CFG_DIR = SystemProperty.getContextProperty("conf.name.dirs");
	
	/**
	 * cfg目录名
	 */
	public static final String CFG = SystemProperty.getContextProperty("conf.name.dirs.no.suffix");
	
	/**
	 * jdk目录名
	 */
	public static final String JDK_ZIP = "jdk.zip";
	
	/**
	 * monitor.sh
	 */
	public static final String MONITOR_SH = "monitor.sh";
	
	/**
	 * 推送主机分类
	 */
	public static final String Tools = SystemProperty.getContextProperty("tools_dir");
	
	/**
	 * 配置zookeeper 的必备参数Dir
	 */
	public static String ZOOKEEPER_DIR = "zookeeper/";
	
	/**
	 * 配置zookeeper 的必备参数
	 */
	public static String ZOOKEEPER = "zookeeper";
	
	/**
	 * 配置jstorm 的必备参数Dir
	 */
	public static String JSTORM_DIR = "jstorm/";
	
	/**
	 * 配置jstorm 的必备参数
	 */
	public static String JSTORM = "jstorm";
	/**
	 * 配置dsf 的必备参数
	 */
	public static String DSF = "dsf";
	/**
	 * 配置dsf 的必备参数Dir
	 */
	public static String DSF_DIR = "dsf/";
	
	/**
	 * 配置DCLOG 的必备参数Dir
	 */
	public static String DCLOG_DIR = "dclog/";
	
	/**
	 * 配置DCLOG 的必备参数
	 */
	public static String DCLOG = "dclog";
	
	/**
	 * 临时 目录 名
	 */
	public static String TMP = "tmp/";
	
	/**
	 * common 目录 名
	 */
	public static String COMMON = "common/";
	
	/**
	 * 监控目录目录
	 */
	public static String MONITOR="monitor/";
	
	/**
	 * 监控名
	 */
	public static String MONITOR_SERVICE="monitor";

	/**
	 * DCA部署模式
	 */
	public static final String DCA_MONITOR = "monitorDCA";
	public static final String DCA_SWITCH = "switchDCA";
	
	/**
	 *  bin 目录 名
	 */
	public static String BIN= "bin/";
	
	/**
	 * sp_switch.xml文件
	 */
	public static String SP_SWITCH = SystemProperty.getContextProperty("sp.switch");
	
	/**
	 * 部署脚本名称
	 */
	public static String DEPLOY_SH_FILE_NAME = SystemProperty.getContextProperty("deploy.sh.filename");
	
	/**
	 * 调用部署脚本命令
	 */
	public static String DEPLOY_SH = SystemProperty.getContextProperty("deploy.sh");
	
	/**
	 * 业务启停脚本命令
	 */
	public static String SERVICE_SH = SystemProperty.getContextProperty("service.sh");
	
	/**
	 * 组件启动共用命令
	 */
	public static String RUN_AUTH_FILE_COMMON = SystemProperty.getContextProperty("RUN_AUTH_FILE_COMMON");
	
	/**
	 * 组件停止共用命令
	 */
	public static String STOP_AUTH_FILE_COMMON = SystemProperty.getContextProperty("STOP_AUTH_FILE_COMMON");
	
	/**
	 * M2DB启停命令
	 */
	public static String M2DB_AUTH_FILE_COMMON = SystemProperty.getContextProperty("M2DB_AUTH_FILE_COMMON");
	
	/**
	 * 删除实例命令
	 */
	public static String DELETE_AUTH_FILE_COMMON = SystemProperty.getContextProperty("DELETE_AUTH_FILE_COMMON");
	
	/**
	 * 启动进程PM2
	 */
	public static String RUN_AUTH_FILE_EXT = SystemProperty.getContextProperty("RUN_AUTH_FILE_EXT");
	
	/**
	 * 停止进程PM2
	 */
	public static String STOP_AUTH_FILE_EXT = SystemProperty.getContextProperty("STOP_AUTH_FILE_EXT");
	
	/**
	 * PM2检查进程
	 */
	public static String CHECK_AUTH_FILE_EXT = SystemProperty.getContextProperty("CHECK_AUTH_FILE_EXT");
	
	/**
	 * 进程检查共用命令
	 */
	public static String CHECK_AUTH_FILE_COMMON = SystemProperty.getContextProperty("CHECK_AUTH_FILE_COMMON");
	
	/**
	 * Topology检查读取配置文件开关
	 */
	public static String FILE_SWITCH = SystemProperty.getContextProperty("file.switch");
	
	/**
	 * Topology检查读取配置自定义目录
	 */
	public static String STORAM_YAML_PATH = SystemProperty.getContextProperty("storm.yaml.path");
	
	/**
	 * Topology检查读取配置ocs目录
	 */
	public static String OCS_JTOPO_PATH = SystemProperty.getContextProperty("ocs.jtopo.path");
	
	/**
	 * 初使化主机配置目录 
	 */
	public static String CONF = SystemProperty.getContextProperty("conf_dir");
	
	/**
	 * 初使化主机配置
	 */
	public static String CONF_SERVICE = "conf";

	/**
	 * 初使化主机配置目录 
	 */
	public static String CONFIG = "config/";
	
	/**
	 * 初使化ROCKETMQ配置目录 
	 */
	public static String ROCKETMQ_DIR = "rocketmq/";
	/**
	 * ROCKETMQ名称
	 */
	public static String ROCKETMQ = "rocketmq";
	
	/**
	 * ROCKETMQ固定路径片段
	 */
	public static String ROCKETMQ_2M_2S_ASYNC = "2m-2s-async/";
	
	/**
	 * 初始化FastDFS配置目录
	 */
	public static String FASTDFS_DIR = "fastdfs/";
	
	/**
	 * Fastdfs名称
	 */
	public static String FASTDFS = "fastdfs";
	
	/**
	 * 初始化Cloudb配置目录
	 */
	public static String CLOUDB_DIR = "cloudb/";
	
	/**
	 * Fastdfs名称
	 */
	public static String CLOUDB = "cloudb";

	/**
	 * Fastdfs固定路径片段
	 */
	public static String FASTDFS_ETC_FDFS = "etc/fdfs/";

	/**
	 * 初始化DCAM配置目录
	 */
	public static String DCAM_DIR = "dcam/";
	
	/**
	 * DCAM名称
	 */
	public static String DCAM = "dcam";
	
	/**
	 * 初始化DCAS配置目录
	 */
	public static String DCAS_DIR = "dcas/";
	
	/**
	 * DCAS名称
	 */
	public static String DCAS = "dcas";
	
	/**
	 * 初始化DCA配置目录
	 */
	public static String DCA_DIR = "dca/";
	
	/**
	 * DCAS名称
	 */
	public static String DCA = "dca";
	
	/**
	 * 初始化DAEMOn配置目录
	 */
	public static String DAEMON_DIR = "daemon/";
	

	/**
	 * DAEMON名称
	 */
	public static String DAEMON = "daemon";

	/**
	 * SENTINEL哨兵名称
	 */
	public static String SENTINEL = "sentinel";

	/**
	 * 初始化SENTINEL哨兵配置目录
	 */
	public static String SENTINEL_DIR = "sentinel/";
	
	/**
	 * 增量刷新
	 */
	public static String REDIS_INC_REFRESH = "redisIncRefresh";
	
	/**
	 * 全量刷新
	 */
	public static String REDIS_WHOLE_REFRESH = "redisWholeRefresh";
	
	/**
	 * 全量校验
	 */
	public static String REDIS_WHOLE_CHECK = "redisWholeCheck";
	
	/**
	 * 增量稽核
	 */
	public static String REDIS_INC_CHECK = "redisIncCheck";
	
	/**
	 * 修复程序
	 */
	public static String REDIS_REVISE = "redisRevise";
	
	/**
	 * 初始化REDIS配置目录
	 */
	public static String REDIS_DIR = "redis/";
	
	/**
	 * REDIS名称
	 */
	public static String REDIS = "redis";
	
	/**
	 * 初始化DMDB配置目录
	 */
	public static String DMDB_DIR = "dmdb/";
	
	/**
	 * DCAS名称
	 */
	public static String DMDB = "dmdb";

	/**
	 * route名称
	 */
	public static String BUS_ROUTE = "route";

	public static String OTHER = "other";

	/**
	 * 版本回退备份目录
	 */
	public static String BACK = "back/";
	
	/**
	 * business/
	 */
	public static String BUSS = SystemProperty.getContextProperty("buss_dir");
	
	/**
	 * 主备切换--灰度升级操作码
	 */
	public static String SWITCH_UPGRADE = "0";
	/**
	 * 主备切换--正式发布操作码
	 */
	public static String SWITCH_LAUNCH = "1";
	/**
	 * 主备切换--回退操作码
	 */
	public static String SWITCH_BACK = "2";
	
	/**
	 * 主备切换upgradestate值：1
	 */
	public static String UPGRADE_STATE_FLAG_1 = "1";
	/**
	 * 主备切换upgradestate值：0
	 */
	public static String UPGRADE_STATE_FLAG_0 = "0";
	
	/**
	 * topology web端服务目录 
	 */
	public static String TOPOLOGY_DIR = SystemProperty.getContextProperty("TOPOLOGY_DIR");
	
	/**
	 * 配置zookeeper 的必备参数 dataDir
	 */
	public static String VERSION_ZOOKEEPER_DATA_DIR = SystemProperty.getContextProperty("version.zookeeper.dataDir");

	/**
	 * top.rebalance.sh
	 */
	public static String TOP_REBALANCE_SH = SystemProperty.getContextProperty("top.rebalance.sh");
	
	/**
	 * 执行启动、停止、检查命令时，对返回的结果进行  特殊值匹配(必须小写，以逗号隔开)
	 */
	public static String FILTER_KEYWORD ="errorinfo,topology failed";
	
	
	//状态 1:有效  0:无效
	public static final String STATE_NOT_ACTIVE = "0";
	public static final String STATE_ACTIVE = "1";

	public static final String ROCKETMQ_NAMESRV = "namesrv";
	
	//public static final String[] MONITOR_PROCESS_ARRAY ={"monitor/bin/alarm","monitor/bin/storage","monitor/bin/server","monitor/bin/agent","component/collector","component/dca","component/dcq","component/dcfile", "component/stateServer"};
	public static final String[] MONITOR_PROCESS_ARRAY ={"monitor/bin/alarm","monitor/bin/storage","monitor/bin/server","monitor/bin/agent","component/collector","component/dcq"};
	public static final String[] MONITOR_PROCESS_ARRAY_SX ={"monitor/bin/alarm","monitor/bin/storage","monitor/bin/server","monitor/bin/agent","component/collector","component/zookeeper"};

	//public static final String[] DCLOG_PROCESS_ARRAY ={"flume","agent.bak","agent","analysis"};
	public static final String[] DCLOG_PROCESS_ARRAY ={"agent","analysis"};
	
	//route集群类型
	public static final String ROUTE = "route";
	
	//DMDB部署模式
	public static final String MAIN_PATTERN = "main_pattern";
	public static final String INSTANCE_PATTERN = "instance_pattern";
	public static final String ROUTE_PATTERN = "route_pattern";
	public static final String SYNC_PATTERN = "sync_pattern";
	public static final String MGR_PATTERN = "mgr_pattern";
	public static final String WATCHER_PATTERN = "watcher_pattern";
	public static final String MOVESYNC_PATTERN = "movesync_pattern";
	
	//ftp连接超时时间
	public static final int FTP_CONN_TIMEOUT = 8000;
	
	public static final String JSTORM_NIMBUS="nimbus";
	
	public static final String NGINX = "nginx";

	//Jstorm-supervisor
	public static final String JSTORM_SUPERVISOR = "supervisor";
	
	//Jstorm配置文件名称
	public static final String STORM_YAML = "storm.yaml";
	
	//创建数据源时是否需要加密
    public static final boolean NEED_DES_PASSWD = true;

    //本地网标识
    public static final String WEB_LATN = "latnId";
    //陕西本地网
    public static final String WEB_LATN_SX = "sx";

	//定时任务类型：数据库归档
	public static final String BUS_TYPE_THREE_FILE_ARCH ="3";
	//定时任务类型：数据库归档
	public static final String BUS_TYPE_TWO_DB_ARCH ="2";
	//定时任务类型：伸缩策略
	public static final String BUS_TYPE_ONE_EXPAND_STRATEGY ="1";
	//定时任务：动态伸缩策略配置-判断是否能触发伸缩报告
	public static final String BUS_TYPE_FOUR_DYNIMIC_CONF_REPORT = "4";
	//定时任务：动态伸缩策略配置-根据伸缩报告进行节点伸缩操作
	public static final String BUS_TYPE_FIVE_DYNIMIC_CONF_EXPEND = "5";


	//定时任务执行类：数据库归档
	public static final String JOB_CLASS_FILE_ARCH = "com.tydic.quartz.FilePigholeQuartz";
	//定时任务执行类：数据库归档
	public static final String JOB_CLASS_DB_ARCH = "com.tydic.quartz.DatabaseArchiveQuartz";
	//定时任务执行类：伸缩策略
	public static final String JOB_CLASS_EXPAND_STRATEGY = "com.tydic.quartz.ClusterNodeManageQuartz";
	//定时任务执行类：动态伸缩扩展
	public static final String JOB_CLASS_DYNAMIC_EXPEND_STRATEGY = "com.tydic.quartz.DynamicThresholdQuartz";

	//数据库类型
	public static final String DATA_SOURCE_TYPE_ORACLE = "Oracle";
	public static final String DATA_SOURCE_TYPE_MYSQL = "MySql";

	public static final int INT_ONE = 1;
	public static final int INT_TWO = 2;

	//用户登录信息
	public static final String LOGIN_EMPEE_ACCT = "LOGIN_EMPEE_ACCT";
	public static final String LOGIN_EMPEE_ID = "LOGIN_EMPEE_ID";
	public static final String LOGIN_SUPER_ADMIN = "LOGIN_SUPER_ADMIN";
	public static final String LOGIN_CITY_ID = "LOGIN_CITY_ID";

	//节点部署的 程序类型，是否区分配置 1区分 0不区分
	public static final String NODE_TYPE_CONFIG_DIFF_CFG_TRUE = "1";
	public static final String NODE_TYPE_CONFIG_DIFF_CFG_FALSE = "0";

	//业务程序启停日志文件输出目录
	public static final String BUSS_TASK_LOG_PATH = "buss.task.log.path";
	//组件程序启停日志文件输出目录
	public static final String COMPONENT_TASK_LOG_PATH = "component.task.log.path";


}
