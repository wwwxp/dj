package com.tydic.dcm.util.tools;

public class ParamsConstant {

	//定时器刷新间隔参数名称
	public static final String PARAMS_COLL_HEART = "coll_heart";
	public static final String PARAMS_DIST_HEART = "dist_heart";
	public static final String PARAMS_WARN_HEART = "warn_heart";
	public static final String PARAMS_LINK_REFRESH_INTERVAL = "link_refresh_interval";

	//分发任务线程数量
	public static final String PARAMS_DIST_THREAD_SIZE = "dist_thread_size";
	
	//序列
	public static final String PARAMS_DST_RULE_SEQUENCE = "sequence";
	
	//链路告警时间间隔参数名称
	public static final String PARAMS_WARN_INTERVAL ="warn_interval";
	
	//文件对比开关
	public static final String PARAMS_FILE_COMPARISON_SWITCH = "file_comparison_switch";
	//文件是否进行剔重
	public static final String PARAMS_FILE_FILTER_SWITCH = "coll_filter_switch";
	//存储到DCA中键进行hash转化
	public static final String PARAMS_FILE_FILTER_HASH = "coll_filter_hash";
	//当前Dcm部署GroupId名称
	public static final String PARAMS_GROUP_ID = "groupId";
	//文件传输协议类型
	public static final String PARAMS_PROTOCOL_TYPE = "protocol";
	//监听客户端端口
	public static final String PARAMS_SERVER_PORT = "server_port";
	//程序启动是否启动采集、分发
	public static final String PARAMS_COLL_INIT_ACTIVE = "coll_init_active";
	public static final String PARAMS_DIST_INIT_ACTIVE = "dist_init_active";
	public static final String PARAMS_DIST_TASK_ABN_CALLBACK_ACTIVE = "dist_task_abn_callback_active";
	public static final String PARAMS_DIST_TASK_ABN_INTERVAL = "dist_task_abn_callback_interval";


	//Job类
	public static final String PARAMS_JOB_COLL_CLZ = "com.tydic.dcm.task.CollTaskJob";
	public static final String PARAMS_JOB_DIST_CLZ = "com.tydic.dcm.task.DistTaskJob";
	
	//默认Cron表达式触发器
	public static final String DEFAULT_TRIGGER_CRON_EXP = "0/60 * * * * ?";
	
	//默认文件传输协议类型
	public static final String DEFAULT_PROTOCOL_FTP = "ftp";
	//Ftp采集被动模式
	public static final String LINK_FTP_TRAN_MODE_PASV = "PASV";
	//Ftp采集主动模式
	public static final String LINK_FTP_TRAN_MODE_PORT = "PORT";
	
	//采集类型
	public static final String TASK_TYPE_HAND_COLL = "hand-coll";
	public static final String TASK_TYPE_REAL_COLL = "real-coll";
	public static final String TASK_TYPE_AUTO_COLL = "auto-coll";
	public static final String TASK_TYPE_OFFLINE_COLL = "offline-coll";
	
	//分发类型
	public static final String TASK_TYPE_HAND_DIST = "hand-dist";
	public static final String TASK_TYPE_AUTO_DIST = "auto-dist";
	public static final String TASK_TYPE_REAL_DIST = "real-dist";
	//多线程分发
	public static final String TASK_TYPE_MULT_DIST = "mult-dist'";
	
	//分隔符
	public static final String DEFAULT_TAB_SEPARATOR = "\t";
	
	//默认单次分发文件个数
	public static final int DEFAULT_DIST_RECORD_ROWS = 500;
	//单词分发文件最大个数
	public static final int MAX_DIST_RECORD_ROWS = 5000;

	//分发异常数据回收间隔时间，单位(秒)
	public static final Long DEFAULT_DIST_TASK_ABN_INTERVAL = 600L;
	
	//下载文件、上传文件重复尝试次
	public static final int FTP_GET_PUT_TRY_COUNT = 3;
	//FTP重试登录此时
	public static final int FTP_LOGIN_TRY_COUNT = 3;
	
	//链路提示级别
	public static final String LINK_TIPS_LEVEL_0 = "0";
	public static final String LINK_TIPS_LEVEL_1 = "1";
	public static final String LINK_TIPS_LEVEL_2 = "2";
	
	//子链路类型
	public static final String ST_NOR_LINK = "NOR_LINK";
	public static final String ST_DBA_LINK = "DBA_LINK";
	
	//是否需要格式化 0:不分发  1:分发  2:格式化并且分发
	public static final String NOFORMATTER = "0";
	public static final String FORMATTER = "1";
	public static final String FORMATTER_AND_DIST = "2";
	
	//SFTP文件类型
	public static final String SFTP_FILE_TYPE = "1";
	public static final String SFTP_DIR_TYPE = "2";
	
	//链路类型
	public static final String TYPE_COLL_LINK = "COLL_LINK";
	public static final String TYPE_DIST_LINK = "DIST_LINK";
	
	//文件存放规则参数
	public static final String DIR_FLAG_MONTH = "month";
	public static final String DIR_FLAG_TENDAYS = "tendays";
	public static final String DIR_FLAG_DAY = "day";
	
	
	//ftp/sftp连接超时时间(60秒)
	public static final String FTP_CONN_TIMEOUT = "60000";
	
	//采集链路运行状态
	public static final String COLL_LINK_RUN_STATE_STOP = "0";
	public static final String COLL_LINK_RUN_STATE_RUN = "1";
	public static final String COLL_LINK_RUN_STATE_ERR = "2";
	
	//分发链路运行状态(0:停止分发 1:正在分发)
	public static final String DIST_LINK_RUN_STATE_STOP = "0";
	public static final String DIST_LINK_RUN_STATE_RUN = "1";
	public static final String DIST_LINK_RUN_STATE_ERR = "2";
	
	//链路执行类型
	public static final String LINK_EXEC_METHOD = "method";
	
	//文件采集或者分发方式
	public static final String LINK_EXEC_AUTO = "auto";
	public static final String LINK_EXEC_HAND = "hand";
	
	//上传方式
	public static final String LINK_FILE_UPLOAD_TRUE = "true";
	public static final String LINK_FILE_UPLOAD_FALSE = "false";
	public static final String LINK_FILE_UPLOAD_ALL = "all";
	
	//参数
	public static final String PARAMS_0 = "0";
	public static final String PARAMS_1 = "1";
	public static final String PARAMS_2 = "2";
	public static final String PARAMS_5 = "5";
	public static final String PARAMS_60 = "60";
	public static final String PARAMS_99 = "99";
	public static final String PARAMS_600 = "600";
	
	//排序字段
	public static final String LINK_SORT_KEY_FILE_TIME = "file_time";
	
	//采集链路排重字段
	public static final String LINK_NOT_COLL_KEY_NAME = "name";
	public static final String LINK_NOT_COLL_KEY_PATH_NAME_TIME = "path+name+time";
	public static final String LINK_NOT_COLL_KEY_PATH_NAME = "path+name";
	
	//任务调度失败默认初始化时间（单位：秒）
	public static final String QUARTZ_FAIL_DEFAULT_TIME = "quartz_fail_default_time";
	//文件对比剃重时间（单位：月）
	public static final String COLL_COMPARISON_TIME = "coll_comparison_time";

	//采集文件列表去重服务
	public static final String COLL_FILTER_TYPE = "coll_filter_type";
	public static final String COLL_FILTER_TYPE_SQL = "sql";
	public static final String COLL_FILTER_TYPE_DCA = "dca";

	//DCA配置信息
	//DCA连接地址
	public static final String DCA_IP = "dca_ip";
	public static final String DCA_PORT = "dca_port";
	public static final String DCA_SLAVE_IP = "dca_slave_ip";
	public static final String DCA_SLAVE_PORT = "dca_slave_port";

	//dca账户
	public static final String DCA_ACCT_ID = "dca_acct_id";
	//dca用户名
	public static final String DCA_USER_NAME = "dca_user_name";
	public static final String DCA_PASSWD = "dca_passwd";
	//是否启动定时检查连接池
	public static final String DCA_CHEAK_POOL = "dca_cheak_pool";
	//第一次检测在启动后多少毫秒执行
	public static final String DCA_LAZY_CHECK = "dca_lazy_check";
	//之后每次隔多久检查一次
	public static final String DCA_PERIOD_CHECK = "dca_period_check";

	//dca初始化连接数
	public static final String DCA_INIT_CONNECTIONS = "dca_init_connections";
	//dca空闲池，最大连接数
	public static final String DCA_MAX_FREE_CONNECTIONS = "dca_max_free_connections";
	//dca最大允许的连接数
	public static final String DCA_MAX_ACTIVE_CONNECTIONS = "dca_max_active_connections";
	//key存活时间（单位：天）
	public static final String DCA_EXPIRE_TIME = "dca_expire_time";
	//key域
	public static final String DCA_KEY_FIELD = "dca_key_field";


	
	//文件存储方式：采集到本地或和写入分布式文件系统local|dfs
	public static final String FILE_STORE_TYPE = "file_store_type";
	
	//消息分发类型(mq/jstorm)
	public static final String DIST_MESSAGE_TYPE = "dist_message_type";

	//分布式文件系统挂载点
	public static final String DFS_MOUNT_POINT = "dfs_mount_point";

	public static final String FTP_AUTO_CONVERT_MODE = "ftp_auto_convert_mode";

	//临时文件重命名前缀使用
	public static final String TMP_LOCATION_PREFIX = "prefix";
	//临时文件重命名后缀使用
	public static final String TMP_LOCATION_SUFFIX = "suffix";

}