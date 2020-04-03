//用于action url
Globals.baseActionUrl = {
    /**********************框架的核心方法  start *************************/
    //用于综合处理方法简单逻辑，如需发多条sql语句
    FRAME_MULTI_OPERATION_URL: Globals.ctx + "/core/multiOperation",
    //根据条件查询某个对象
    FRAME_QUERY_FOR_OBJECT_URL: Globals.ctx + "/core/queryForObject",
    //插入对象
    FRAME_INSERT_OBJECT_URL: Globals.ctx + "/core/insertObject",
    //批量插入对象
    FRAME_INSERT_BATCH_OBJECT_URL: Globals.ctx + "/core/insertBatchObject",
    //更新对象
    FRAME_UPDATE_OBJECT_URL: Globals.ctx + "/core/updateObject",
    //批量更新对象
    FRAME_UPDATE_BATCH_OBJECT_URL: Globals.ctx + "/core/updateBatchObject",
    //查询多条记录
    FRAME_QUERY_FOR_LIST_URL: Globals.ctx + "/core/queryForList",
    //分页查询多条记录 ORACLE/mysql
    FRAME_QUERY_PAGE_LIST: Globals.ctx + "/core/queryPageList",
    //插入某个对象并返回key值
    FRAME_INSERT_OBJECT_RETURNKEY_URL: Globals.ctx + "/core/insertObjectReturnKey",
    //删除对象
    FRAME_DELETE_OBJECT_URL: Globals.ctx + "/core/deleteObject",
    //批量删除对象
    FRAME_DELETE_BATCH_OBJECT_URL: Globals.ctx + "/core/deleteBatchObject",
    //得到最大值
    FRAME_GET_MAX_VALUE_URL: Globals.ctx + "/core/getMaxValue",
    //查询序列
    FRAME_GET_SEQUENCE_URL: Globals.ctx + "/core/getSequence",
    /**********************框架的核心方法  end *************************/

    /**********************首页  start************************/
    //首页-注销
    HOME_ACTION_LOGIN_OUT_URL: Globals.ctx + "/login/logonOut",
    /**********************首页  end************************/

    /**********************用户管理  start************************/
    //用户管理-增加用户
    USER_ACTION_ADD_URL: Globals.ctx + "/user/insertEmpee",
    //用户管理-修改用户
    USER_ACTION_UPDATE_URL: Globals.ctx + "/user/updateEmpee",
    ////用户管理-根据ID查询
    USER_ACTION_QUERY_ID_URL: Globals.ctx + "/user/queryEmpeeInfoById",
    //用户管理-增加用户
    HOME_ACTION_EDI_PASSWORD_URL: Globals.ctx + "/user/editPassword",
    //用户管理-分页查询用户
    USER_ACTION_QUERY_URL: Globals.ctx + "/user/queryEmpeeInfo",
    /**********************用户管理  end************************/

    /**********************权限管理  start************************/
    //权限管理-更新权限
    PRIVILEGE_ACTION_UPDATE_URL: Globals.ctx + "/privilege/updatePrivilege",
    //权限管理-新增权限
    PRIVILEGE_ACTION_ADD_URL: Globals.ctx + "/privilege/addPrivilege",
    /**********************权限管理  end************************/

    /**********************配置管理  start************************/
    //配置管理--查询文件列表 
    TOPCONFIG_ACTION_FILE_TREE_URL: Globals.ctx + "/topconfig/filesTree",
    //配置管理－查看文件
    TOPCONFIG_ACTION_OPEN_FILE_URL: Globals.ctx + "/topconfig/openFile",
    //配置管理－保存文件
    TOPCONFIG_ACTION_SAVE_FILE_URL: Globals.ctx + "/topconfig/saveFile",
    //配置管理－校验ＪＳＯＮ格式
    TOPCONFIG_ACTION_VALIDATE_JSON_URL: Globals.ctx + "/topconfig/validateJson",
    //配置管理－发布
    TOPCONFIG_ACTION_RELEASE_FILE_URL: Globals.ctx + "/topconfig/release",
    /**********************配置管理  end************************/

    /*************************自定义拓扑图 start**************************************/
    //获取自定义信息
    TOPOLOGY_ACTION_GET_URL: Globals.ctx + "/customTopology/getCustomTopologyInfo",
    //保存自定义信息
    TOPOLOGY_ACTION_SAVE_URL: Globals.ctx + "/customTopology/saveCustomTopologyInfo",
    //获取图片信息
    TOPOLOGY_ACTION_GET_IMAGES_URL: Globals.ctx + "/customTopology/getImagesInfo",
    /*************************自定义拓扑图 end**************************************/

    /*************************监控管理 start**************************************/
    //supervisor 摘要信息
    MONITOR_ACTION_RESOURCE_SUMMARY_URL: Globals.ctx + "/host/minitor/resource/supervisor/summary",
    //nimbus 配置信息
    MONITOR_ACTION_RESOURCE_CONFIGURATION_URL: Globals.ctx + "/host/minitor/resource/nimbus/configuration",
    //worker 信息
    MONITOR_ACTION_RESOURCE_WORKER_METRICS_URL: Globals.ctx + "/host/minitor/resource/worker/metrics",
    // worker图表数据
    MONITOR_ACTION_RESOURCE_WORKER_CHARTS_URL: Globals.ctx + "/host/minitor/resource/worker/charts",
    //jstorm topology 摘要信息
    MONITOR_ACTION_PRESSURE_TOPOLOGY_SUMMARY_URL: Globals.ctx + "/host/minitor/pressure/topology/summary",
    //jstorm topology 配置信息
    MONITOR_ACTION_PRESSURE_TOPOLOGY_CONFIGURATION_URL: Globals.ctx + "/host/minitor/pressure/topology/configuration",
    //jstorm topology 运行状态
    MONITOR_ACTION_PRESSURE_TOPOLOGY_STATE_URL: Globals.ctx + "/host/minitor/pressure/topology/state",
    //jstorm component 信息
    MONITOR_ACTION_PRESSURE_TOPOLOGY_COMPONENT_METRICS_URL: Globals.ctx + "/host/minitor/pressure/topology/componentMetrics",
    //jstorm worker状态
    MONITOR_ACTION_PRESSURE_TOPOLOGY_WORKER_METRICS_URL: Globals.ctx + "/host/minitor/pressure/topology/workerMetrics",
    //jstorm task状态
    MONITOR_ACTION_PRESSURE_TOPOLOGY_TASK_STATS_URL: Globals.ctx + "/host/minitor/pressure/topology/taskStats",
    //主机资源监控图表数据
    MONITOR_ACTION_BUSINESS_HOST_RESOURCE_URL: Globals.ctx + "/host/minitor/business/resource/charts",
    //流量转接查询
    MONITOR_ACTION_FLOW_TRANSFER_QUERY_URL: Globals.ctx + "/surplusedFlow/flowTransferQuery",
    /*************************监控管理 end**************************************/

    /*************************二次开发 start**************************************/
    //列出文件
    DEVELOP_ACTION_LIST_FILES_TREE_URL: Globals.ctx + "/develop/listFilesTree",
    //创建文件或目录
    DEVELOP_ACTION_CREATE_DIRECTORY_OR_FILE_URL: Globals.ctx + "/develop/createDirectoryOrFile",
    //重命名文件或目录
    DEVELOP_ACTION_RENAME_DIRECTORY_OR_FILE_URL: Globals.ctx + "/develop/renameDirectoryOrFile",
    //删除文件或目录
    DEVELOP_ACTION_DELETE_DIRECTORY_OR_FILE_URL: Globals.ctx + "/develop/deleteDirectoryOrFile",
    //保存文件
    DEVELOP_ACTION_SAVE_DEVELOP_FILE_URL: Globals.ctx + "/develop/saveDevelopFile",
    //发布版本
    DEVELOP_ACTION_RELEASE_URL: Globals.ctx + "/develop/release",
    //上传文件
    DEVELOP_ACTION_UPLOAD_URL: Globals.ctx + "/develop/upload",

    /*************************二次开发 end**************************************/

    /*******************************在线shell start********************************************/
    //建立shell连接
    SHELL_ACTION_SEND_SHELL_URL: Globals.ctx + "/onLineShell/shell",
    //删除shell连接
    SHELL_ACTION_REMOVE_HOST_LINK_URL: Globals.ctx + "/onLineShell/removeHostLink",
    /*******************************在线shell end********************************************/

    //集群划分--添加业务主机划分
    DEPLOY_BUSINESS_HOST_URL: Globals.ctx + "/deploy/insertBusiness",
    DEPLOY_DEL_BATCH_HOST_URL: Globals.ctx + "/deploy/delHostBatchPartition",

    /**********************************运维 start********************************************/
    MAINTENANCE_HOST_QUERY_CDRLIST: Globals.ctx + "/host/queryCdrList",
    MAINTENANCE_HOST_CLEAR_LOGS: Globals.ctx + "/host/clearLogs",
    /**********************************运维shell END********************************************/

    /**********************************（新）部署图 start********************************************/
    DEPLOY_ACTION_ADD_HOST_URL: Globals.ctx + "/deploy/insertChosenHost",
    //集群管理(新)--部署
    CLUSTER_ACTION_DEPLOY_URL: Globals.ctx + "/deploy/deployHost",
    CLUSTER_ACTION_BUSINESS_DEPLOY_URL: Globals.ctx + "/busDeploy/distribute",

    /**********************************（新）部署图 start********************************************/
    DEPLOY_ACTION_ADD_HOST_URL: Globals.ctx + "/deploy/insertChosenHost",
    //文件上传
    FTP_ACTION_FILE_UPLOAD_URL: Globals.ctx + "/uploadFTP/fileUpload",
    //删除框架版本包
    FTP_ACTION_DELETE_PLATFORM_BY_VERSION_URL: Globals.ctx + "/uploadFTP/deletePlatformPackage",
    // 版本回退
    FTP_ACTION_BACK_VERSION_URL: Globals.ctx + "/uploadFTP/backVersion",
    // 删除业务程序包
    FTP_ACTION_DELETE_SERVICE_URL: Globals.ctx + "/uploadFTP/delete",
    // 查看文件
    FTP_ACTION_FILE_CONTENT_URL: Globals.ctx + "/uploadFTP/fileContent",
    /**********************************（新）部署图 end********************************************/



    /**********************************（新）运维管理start********************************************/
    //配置管理--查询文件列表 
    CONFIGURE_ACTION_FILE_TREE_URL: Globals.ctx + "/configure/loadFileTree",
    //配置管理--查询脚本文件列表
    CONFIGURE_ACTION_SCRIPT_FILE_TREE_URL: Globals.ctx + "/configure/loadScriptTree",
    //配置管理--查询文件内容
    CONFIGURE_ACTION_FILE_CONTENT_URL: Globals.ctx + "/configure/getFileContent",
    //配置管理--查询业务文件内容
    CONFIGURE_ACTION_FILE_BUS_CONTENT_URL: Globals.ctx + "/configure/getFileBusContent",
    //配置管理--查询文件内容
    CONFIGURE_ACTION_SHOW_CONFIG_FILE_CONTENT_URL: Globals.ctx + "/configure/showConfigContent",
    //配置管理--保存修改后的文件
    CONFIGURE_ACTION_FILE_SAVE_URL: Globals.ctx + "/configure/saveFileContents",
    //配置管理--保存修改后的文件
    CONFIGURE_ACTION_FILE_SAVE_BUS_URL: Globals.ctx + "/configure/saveBusFileContents",
    //配置管理--分发修改后的文件
    CONFIGURE_ACTION_FILE_DISTRIBUTE_URL: Globals.ctx + "/configure/distributeFileContent",
    //配置管理--分发修改后的脚本文件
    CONFIGURE_ACTION_SCRIPT_DISTRIBUTE_URL: Globals.ctx + "/configure/updateSaveScript",
    //配置修改--新建文件
    CONFIGURE_ACTION_CREATE_FILE_URL: Globals.ctx + "/configure/createFile",
    //配置修改--新建文件
    CONFIGURE_ACTION_BATCH_BUSS_FILE_URL: Globals.ctx + "/configure/batchBussFile",
    //配置修改--重命名文件
    CONFIGURE_ACTION_RENAME_FILE_URL: Globals.ctx + "/configure/renameFile",
    //配置修改--删除文件
    CONFIGURE_ACTION_DELETE_FILE_URL: Globals.ctx + "/configure/deleteFile",
    //配置修改--删除redis批量目录文件
    CONFIGURE_ACTION_DELETE_REDIS_BATCH_FILE_URL: Globals.ctx + "/configure/deleteBatchRedisFile",
    //配置修改--删除选中目录下的所有实例
    CONFIGURE_ACTION_DELETE_ALL_FILE_URL: Globals.ctx + "/configure/deleteSentinelInstance",
    //配置修改--查找对应集群编码下的default下的文件
    CONFIGURE_ACTION_LOAD_DEFAULT_FILE_URL: Globals.ctx + "/configure/loadFileListUnderDefault",
    //配置修改--查找指定路径下子节点
    CONFIGURE_ACTION_LOAD_FILE_UNDER_GIVEN_PATH_URL: Globals.ctx + "/configure/loadFilesUnderGivenPath",
    //配置修改--查看目录下的所有文件
    CONFIGURE_ACTION_LOAD_FILE_DOWN_FOLDER_URL: Globals.ctx + "/configure/loadFileListByFolder",
    //配置修改--新建实例
    CONFIGURE_ACTION_ADD_COPY_FILE_URL: Globals.ctx + "/configure/createAndCopyFolder",
    //配置修改--新建实例（redis）
    CONFIGURE_ACTION_FOUND_COPY_FILE_URL: Globals.ctx + "/configure/foundAndCopyFolder",
    //配置新增--批量新增实例
    CONFIGURE_ACTION_BATCH_ADD_COPY_FILE_URL: Globals.ctx + "/configure/addBatchFileAndFolder",
    //配置新增--批量新增redis实例
    CONFIGURE_ACTION_BATCH_CREATE_COPY_REDIS_FILE_URL: Globals.ctx + "/configure/createBatchRedisFileAndFolder",
    //配置管理--查询文件列表
    CONFIGURE_ACTION_TREE_CREATE_CLUSTER_CODE_URL: Globals.ctx + "/configure/createAndCopyClusterCode",
    //配置修改--新建实例
    CONFIGURE_ACTION_QUERY_ZK_REDIS_NODES_URL: Globals.ctx + "/configure/getRedisNodes",

    //运行管理--运行主机
    HOSTSTART_ACTION_RUN_URL: Globals.ctx + "/hostStart/runHost",
    //运行管理--m2db刷新表
    HOSTSTART_ACTION_M2DB_REFRESH_URL: Globals.ctx + "/hostStart/m2dbRefresh",
    //运行管理--m2db刷新数据m2dbRefreshMem
    HOSTSTART_ACTION_M2DB_REFRESH_MEM_URL: Globals.ctx + "/hostStart/m2dbRefreshMem",
    //运行管理--m2db刷新数据m2dbinputtable
    HOSTSTART_ACTION_M2DB_INPUT_URL: Globals.ctx + "/hostStart/m2dbInputTable",
    //运行管理--停止运行主机
    HOSTSTART_ACTION_STOP_RUN_URL: Globals.ctx + "/hostStart/stopRunHost",
    //运行管理--检查主机运行状态
    HOSTSTART_ACTION_CHECK_HOST_STATE_URL: Globals.ctx + "/hostStart/checkHostState",
    //主机管理--查询配置文件
    HOSTSTART_ACTION_VIEW_CONF_URL: Globals.ctx + "/hostStart/viewConf",
    //检查主机状态（fastdfs/rocketmq/dcas）
    HOSTSTART_ACTION_CHECK_PROCESS_STATE_URL: Globals.ctx + "/hostStart/checkProcessState",
    //组将状态检查（批量检查）
    HOSTSTART_ACTION_BATCH_CHECK_STATE_URL: Globals.ctx + "/hostStart/batchCheckStatus",
    //更新程序状态和主机状态（fastdfs/rocketmq/dcas）
    HOSTSTART_ACTION_UPDATE_PROCESS_STATE_URL: Globals.ctx + "/hostStart/updateProcessState",
    //DEPLOY_TASK_ACTION_UPLOAD_URL:Globals.ctx + "/deploy/task/fileUpload",


    //rocketmq管理--提交部署信息
    ROCKETMQ_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/rocketmqStart/startRocketmq",
    //rocketmq管理--停止部署信息
    ROCKETMQ_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/rocketmqStart/stopRocketmq",

    //fastdfs管理--启动fastdfs
    FASTDFS_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/fastdfsStart/startFastDFS",
    //fastdfs管理--停止fastdfs
    FASTDFS_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/fastdfsStart/stopFastDFS",
    //fastDFS管理--获取配置文件列表
    DEPLOY_TASK_ACTION_GET_CONFIG_LIST_URL: Globals.ctx + "/fastdfsStart/getFileList",

    //dca管理--启动DCA
    DCA_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/dcaStart/startDca",
    //dca管理--停止DCA
    DCA_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/dcaStart/stopDca",

    //zookeeper管理--启动zookeeper
    ZOOKEEPER_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/zookeeperStart/startZookeeper",
    //zookeeper管理--停止zookeeper
    ZOOKEEPER_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/zookeeperStart/stopZookeeper",

    //JSTORM管理--启动JSTORM
    JSTORM_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/jstormStart/startJstorm",
    //JSTORM管理--停止JSTORM
    JSTORM_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/jstormStart/stopJstorm",

    //M2DB管理--启动M2DB实例
    M2DB_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/m2dbStart/startM2db",
    //M2DB管理--停止M2DB实例
    M2DB_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/m2dbStart/stopM2db",

    //dclog管理--启动JSTORM
    DCLOG_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/dclogStart/startDclog",
    //DCLOG管理--停止JSTORM
    DCLOG_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/dclogStart/stopDclog",

    //Monitor管理--启动Monitor
    MONITOR_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/monitorStart/startMonitor",
    //Monitor管理--停止Monitor
    MONITOR_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/monitorStart/stopMonitor",
    //dmdb管理--启动DMDB
    DMDB_TASK_ACTION_RUN_INFOMATION_URL: Globals.ctx + "/dmdbStart/startDmdb",
    //dmdb管理--停止DMDB
    DMDB_TASK_ACTION_STOP_INFOMATION_URL: Globals.ctx + "/dmdbStart/stopDmdb",
    //实例管理--删除实例
    INST_CONFIG_TASK_ACTION_DELETE_INFOMATION_URL: Globals.ctx + "/instConfig/deleteInstConfig",
    //业务程序管理-删除程序实例
    INST_CONFIG_BUS_TASK_ACTION_DELETE_INFO_URL: Globals.ctx + "/instConfig/deleteBusInstConfig",

    /**********************数据源管理  star************************/
    DATASOURCE_ACTION_INSERT_URL:Globals.ctx + "/dataSourceConfig/insertDataSource",
    DATASOURCE_ACTION_EDIT_URL:Globals.ctx + "/dataSourceConfig/editDataSource",
    DATASOURCE_ACTION_QUERY_URL:Globals.ctx + "/dataSourceConfig/queryDataSource",
    DATASOURCE_ACTION_DELETE_URL:Globals.ctx + "/dataSourceConfig/deleteDataSource",
    DATASOURCE_ACTION_TEST_URL:Globals.ctx + "/dataSourceConfig/testDataSource",


    //dsf管理--启动dsf
    DSF_TASK_ACTION_DEPLOY_INFOMATION_URL: Globals.ctx + "/dsfstart/startDsf",
    //dsf管理--停止dsf
    DSF_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL: Globals.ctx + "/dsfstart/stopDsf",

    //查询组件实例启停日志信息
    INST_CONFIG_LOG_DETAIL_ACTION_URL: Globals.ctx + "/instConfig/queryInstConfigLogDetail",

    //route
    ROUTE_RUN_ACTION_MANAGE_URL: Globals.ctx + "/route/program/run",
    ROUTE_STOP_ACTION_MANAGE_URL: Globals.ctx + "/route/program/stop",
    ROUTE_CHECK_ACTION_MANAGE_URL: Globals.ctx + "/route/program/check",
    ROUTE_SWITCH_ACTION_MANAGE_URL: Globals.ctx + "/route/program/switch",

    //启停添加程序
    BUS_PROGRAM_TASK_ADD_ACTION_MANAGE_URL: Globals.ctx + "/busProgramTask/insertBusProgramTask",
    BUS_PROGRAM_TASK_DEL_ACTION_MANAGE_URL: Globals.ctx + "/busProgramTask/delBusProgramTask",
    BUS_PROGRAM_TASK_DEL_CURR_VER_ACTION_MANAGE_URL: Globals.ctx + "/busProgramTask/delCurrVerBusProgramTask",
    BUS_TOPO_PROGRAM_TASK_DEL_ACTION_MANAGE_URL: Globals.ctx + "/busProgramTask/delBusTopologyProgramTask",
    BUS_PROGRAM_FILES_ACTION_MANAGE_URL: Globals.ctx + "/busProgramTask/getBusConfigList",
    BUS_PROGRAM_FILE_CONTENT_ACTION_MANAGE_URL: Globals.ctx + "/busProgramTask/getBusTargetConfigList",
    BUS_PROGRAM_LIST_ACTION_MANAGE_URL: Globals.ctx + "/busProgramTask/getBusProgramListWithHost",
    BUS_PROGRAM_UPDATE_ACTION_CELL_URL: Globals.ctx + "/busProgramTask/updateTaskCell",
    BUS_PROGRAM_LOG_DETAIL_ACTION_URL: Globals.ctx + "/busProgramTask/queryLogDetail",

    //dcm
    DCM_RUN_ACTION_MANAGE_URL: Globals.ctx + "/dcm/program/run",
    DCM_STOP_ACTION_MANAGE_URL: Globals.ctx + "/dcm/program/stop",
    DCM_CHECK_ACTION_MANAGE_URL: Globals.ctx + "/dcm/program/check",

    // billing
    BILLING_RUN_ACTION_MANAGE_URL: Globals.ctx + "/billing/task/run",
    BILLING_STOP_ACTION_MANAGE_URL: Globals.ctx + "/billing/task/stop",
    BILLING_CHECK_ACTION_MANAGE_URL: Globals.ctx + "/billing/task/check",
    BILLING_VIEWCONF_ACTION_MANAGE_URL: Globals.ctx + "/billing/viewConf",

    //other周边--运行程序
    OTHER_RUN_ACTION_MANAGE_URL: Globals.ctx + "/other/program/run",
    //other周边--停止程序
    OTHER_STOP_ACTION_MANAGE_URL: Globals.ctx + "/other/program/stop",
    //other周边--检查程序
    OTHER_CHECK_ACTION_MANAGE_URL: Globals.ctx + "/other/program/check",

    //rent月租--运行任务 
    RENT_RUN_TASK_ACTION_MANAGE_URL: Globals.ctx + "/rent/runTask",
    //rent月租--停止任务 
    RENT_STOP_TASK_ACTION_MANAGE_URL: Globals.ctx + "/rent/stopTask",
    //rent月租--任务状态检查
    RENT_CHECK_PROGRAM_ACTION_MANAGE_URL: Globals.ctx + "/rent/checkProgram",
    //rent月租--查看任务的定义 
    RENT_SCAN_CONFIG_FILE_TASK_ACTION_MANAGE_URL: Globals.ctx + "/rent/scanConfigFile",


    //不区分主机IP启停
    COMMON_IP_RUN_ACTION_MANAGE_URL: Globals.ctx + "/runSame/program/run",
    COMMON_IP_STOP_ACTION_MANAGE_URL: Globals.ctx + "/runSame/program/stop",
    COMMON_IP_CHECK_ACTION_MANAGE_URL: Globals.ctx + "/runSame/program/check",

    //启停Topology
    COMMON_TOPOLOGY_RUN_TASK_ACTION_MANAGE_URL: Globals.ctx + "/runTopology/task/run",
    COMMON_TOPOLOGY_STOP_TASK_ACTION_MANAGE_URL: Globals.ctx + "/runTopology/task/stop",
    COMMON_TOPOLOGY_CHECK_PROGRAM_ACTION_MANAGE_URL: Globals.ctx + "/runTopology/task/check",
    COMMON_TOPOLOGY_SCAN_CONFIG_FILE_TASK_ACTION_MANAGE_URL: Globals.ctx + "/runTopology/viewConf",
    COMMON_TOPOLOGY_SERVICE_CONFIG_FILE_TASK_ACTION_MANAGE_URL: Globals.ctx + "/runTopology/viewServiceConf",

    //区分IP启停
    COMMON_DIFF_IP_RUN_ACTION_MANAGE_URL: Globals.ctx + "/runDiff/program/run",
    COMMON_DIFF_IP_STOP_ACTION_MANAGE_URL: Globals.ctx + "/runDiff/program/stop",
    COMMON_DIFF_IP_CHECK_ACTION_MANAGE_URL: Globals.ctx + "/runDiff/program/check",

    //主备切换--灰度升级--获取网元下拉框值
    SWITCH_MASTER_STANDBY_UPGRADE_NET_VALUE_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandby/value/netElement",
    //主备切换--灰度升级--获取sp_switch.xml号段、网元信息
    SWITCH_MASTER_STANDBY_UPGRADE_NUM_INFO_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandby/info/existNum",
    //主备切换--灰度升级操作
    SWITCH_MASTER_STANDBY_OPT_UPGRADE_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandby/opt/upgrade",
    //主备切换--正式发布
    SWITCH_MASTER_STANDBY_OPT_LAUNCH_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandby/opt/launch",
    //主备切换--回退
    SWITCH_MASTER_STANDBY_OPT_ROLLBACK_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandby/opt/rollback",
    //主备切换--灰度配置文件修改操作
    SWITCH_MASTER_STANDBY_OPT_UPGRADE_CONFIG_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandby/opt/upgradeConfig",

    //ABM主备切换--灰度升级--获取网元下拉框值
    SWITCH_ABM_MASTER_STANDBY_UPGRADE_LATN_VALUE_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandbyAbm/value/latnElement",
    //ABM主备切换--灰度升级--获取sp_switch.xml号段、网元信息
    SWITCH_ABM_MASTER_STANDBY_UPGRADE_NUM_INFO_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandbyAbm/info/existNum",
    //ABM主备切换--灰度升级操作
    SWITCH_ABM_MASTER_STANDBY_OPT_UPGRADE_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandbyAbm/opt/upgrade",
    //ABM主备切换--正式发布
    SWITCH_ABM_MASTER_STANDBY_OPT_LAUNCH_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandbyAbm/opt/launch",
    //ABM主备切换--回退
    SWITCH_ABM_MASTER_STANDBY_OPT_ROLLBACK_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandbyAbm/opt/rollback",
    //ABM主备切换--灰度配置文件修改操作
    SWITCH_ABM_MASTER_STANDBY_OPT_UPGRADE_CONFIG_ACTION_MANAGE_URL: Globals.ctx + "/switchMasterStandbyAbm/opt/upgradeConfig",

    //切离线--获取sp_switch.xml下offline节点下信息
    CUT_OFFLINE_INFO_NUM_NET_ACTION_MANAGE_URL: Globals.ctx + "/cutOffline/info/existNumOrNet",
    //切离线--切离线/不切离线
    CUT_OFFLINE_CUT_OPT_ACTION_MANAGE_URL: Globals.ctx + "/cutOffline/opt/cutOffline",
    /**********************************（新）运维管理 end********************************************/

    //日志级别调整
    LOG_LEVEL_CFG_ACTION_UPDATE_URL: Globals.ctx + "/logLevelCfg/update",
    LOG_LEVEL_CFG_ACTION_SENDMSG_URL: Globals.ctx + "/logLevelCfg/sendMsg",
    LOG_LEVEL_CFG_ACTION_NEW_ADD_URL: Globals.ctx + "/logLevelCfg/addLogLevel",
    LOG_LEVEL_CFG_ACTION_NEW_UPATE_URL: Globals.ctx + "/logLevelCfg/updateLogLevel",
    LOG_LEVEL_CFG_ACTION_NEW_DEL_URL: Globals.ctx + "/logLevelCfg/delLogLevel",
    //获得重新负载配置文件
    TOP_REBALANCE_CONFIG_FILE_LIST_URL: Globals.ctx + "/topManager/getFileList",
    //重新负载
    TOP_REBALANCE_CONFIG_RELOAD_URL: Globals.ctx + "/topManager/topRebalanceReload",

    /**********************************（新）top管理 end********************************************/

    /**********************************（新）监控管理 start********************************************/
    //监控管理--集群摘要--加载集群列表
    MONITOR_ACTION_SUMMARY_BUS_CLUSTER_LIST_URL: Globals.ctx + "/monitorManager/clusterSummary/geBusClusterList",
    //监控管理--集群摘要--加载集群列表
    MONITOR_ACTION_SUMMARY_CLUSTER_LIST_URL: Globals.ctx + "/monitorManager/clusterSummary/getClusterList",
    //监控管理--集群摘要--加载表格
    MONITOR_ACTION_SUMMARY_DATAGRID_URL: Globals.ctx + "/monitorManager/clusterSummary/dataGridInfo",
    //监控管理--集群摘要--加载“topology配置信息”表格
    MONITOR_ACTION_SUMMARY_TOPOLOGY_CONFIGURATION_URL: Globals.ctx + "/monitorManager/clusterSummary/topConfigureInfo",
    //监控管理--集群摘要--加载“topology配置信息”表格
    MONITOR_ACTION_SUMMARY_NIMBUS_CONFIGURATION_URL: Globals.ctx + "/monitorManager/clusterSummary/nimConfigureInfo",
    //监控管理--集群摘要--加载“supervisor配置信息”表格
    MONITOR_ACTION_SUMMARY_SUPERVISOR_CONFIGURATION_URL: Globals.ctx + "/monitorManager/clusterSummary/supConfigureInfo",
    //监控管理--集群摘要--日志信息
    MONITOR_ACTION_SUMMARY_NIMBUS_LOG_URL: Globals.ctx + "/monitorManager/log/nimLogInfo",
    //监控管理--集群摘要--日志文件下载
    MONITOR_ACTION_SUMMARY_DOWNLOAD_LOG_URL: Globals.ctx + "/monitorManager/log/download",
    //监控管理--集群摘要--日志管理
    MONITOR_ACTION_SUMMARY_CLUSTER_LOG_FILE_URL: Globals.ctx + "/monitorManager/log/file/fileLists",
    //监控管理--JStack日志信息
    MONITOR_ACTION_SUMMARY_JSTACK_LOG_URL: Globals.ctx + "/monitorManager/log/jstack",
    //监控管理--集群摘要--日志搜索
    MONITOR_ACTION_SUMMARY_SEARCH_LOG_URL: Globals.ctx + "/monitorManager/log/searchLogInfo",


    //监控管理--zookeeper管理--加载表格信息
    MONITOR_ACTION_ZOOKEEPER_DATAGRID_URL: Globals.ctx + "/monitorManager/zookeeperManager/dataGridInfo",
    //监控管理--zookeeper管理--加载树节点
    MONITOR_ACTION_ZOOKEEPER_TREE_NODE_URL: Globals.ctx + "/monitorManager/zookeeperManager/zookeeper/node",
    //监控管理--zookeeper管理--加载树节点数据
    MONITOR_ACTION_ZOOKEEPER_TREE_NODE_DATA_URL: Globals.ctx + "/monitorManager/zookeeperManager/zookeeper/nodeData",

    MONITOR_ACTION_TOPOLOGY_SUMMARY_URL: Globals.ctx + "/monitorManager/topology/summary",
    MONITOR_ACTION_TOPOLOGY_COMPONENT_METRICS_URL: Globals.ctx + "/monitorManager/topology/componentMetrics",
    MONITOR_ACTION_TOPOLOGY_COMPONENT_METRICS_LIST_SUMMARY_URL: Globals.ctx + "/monitorManager/topology/componentMetricList",
    MONITOR_ACTION_TOPOLOGY_WORKER_METRICS_URL: Globals.ctx + "/monitorManager/topology/workerMetrics",
    MONITOR_ACTION_TOPOLOGY_TASK_STATS_MONITOR_URL: Globals.ctx + "/monitorManager/topology/taskStats",
    MONITOR_ACTION_TOPOLOGY_NIMBUS_CONFIGURATION_MONITOR_URL: Globals.ctx + "/monitorManager/topology/configuration",
    MONITOR_ACTION_TOPOLOGY_SUPERVISOR_SUMMARY_URL: Globals.ctx + "/monitorManager/topology/supervisor",
    MONITOR_ACTION_TOPOLOGY_SUPERVISOR_WORKER_METRIC_URL: Globals.ctx + "/monitorManager/topology/supervisor/workerMetrics",
    MONITOR_ACTION_TOPOLOGY_NETTY_METRICS_URL: Globals.ctx + "/monitorManager/topology/nettyMetrics",
    /**********************************（新）监控管理 end********************************************/


    /**********************************（新）集群管理 start********************************************/
    CLUSTER_DIV_ACTION_DELETE_URL: Globals.ctx + "/deploy/deleteServiceHost",
    DEPLOY_TASK_ACTION_DELETE_HOST_URL: Globals.ctx + "/deploy/deleteHostAndPath",
    /**********************************（新）集群管理 end********************************************/

    /**********************主机管理  start************************/
    HOST_ACTION_DELETE_URL: Globals.ctx + "/host/delete",
    HOST_ACTION_ADD_URL: Globals.ctx + "/host/insertHost",
    HOST_ACTION_BATCH_UPDATE_PASSWD_URL: Globals.ctx + "/host/updatePasswdBatch",
    HOST_ACTION_EDIT_URL: Globals.ctx + "/host/updateHost",
    HOST_ACTION_QUERY_INFO_URL: Globals.ctx + "/host/getHostInfo",
    HOST_ACTION_LOGIN_TEST_URL: Globals.ctx + "/host/loginTest",
    HOST_ACTION_TERMINAL_URL: Globals.ctx + "/host/terminal",
    HOST_ACTION_EXCEL_URL: Globals.ctx + "/host/importFromExcel",
    HOST_ACTION_DOWN_EXCEL_URL: Globals.ctx + "/host/downloadExcel",
    /**********************主机管理  end************************/

    /**********************集群管理  start************************/
    BUS_MAIN_CLUSTER_ACTION_DELETE_URL: Globals.ctx + "/busMainCluster/deleteBusMainCluster",
    BUS_MAIN_CLUSTER_ACTION_ADD_URL: Globals.ctx + "/busMainCluster/insertBusMainCluster",
    BUS_MAIN_CLUSTER_ACTION_EDIT_URL: Globals.ctx + "/busMainCluster/updateBusMainCluster",
    BUS_MAIN_CLUSTER_ACTION_GET_URL: Globals.ctx + "/busMainCluster/getUserBusMainCluster",

    SERVICE_TYPE_ACTION_DELETE_URL: Globals.ctx + "/serviceType/deleteServiceType",
    SERVICE_TYPE_ACTION_ADD_URL: Globals.ctx + "/serviceType/insertServiceType",
    SERVICE_TYPE_ACTION_GET_PROP_LIST_URL: Globals.ctx + "/serviceType/getPropList",
    SERVICE_TYPE_ACTION_EDIT_URL: Globals.ctx + "/serviceType/updateServiceType",

    SERVICE_TYPE_ACTION_GET_PARAMS_URL: Globals.ctx + "/serviceType/queryComponentsParams",
    /**********************集群管理  end************************/

    /**********************Topic配置管理  start************************/
    TOPIC_CONFIG_ACTION_ADD_URL: Globals.ctx + "/topicConfig/addTopicConfig",
    TOPIC_CONFIG_ACTION_DEL_URL: Globals.ctx + "/topicConfig/delTopicConfig",
    /**********************Topic配置管理  end************************/

    //一键启动， 保存配置数据
    HOST_START_ACTION_INIT_CONFIG_URL: Globals.ctx + "/fastdfsStart/addOperator",
    //一键启动， 加载数据
    HOST_START_ACTION_LOAD_CONFIG_URL: Globals.ctx + "/fastdfsStart/queryOperator",
    //一键启动，数据加载
    HOST_START_ACTION_ONCE_LOAD_URL: Globals.ctx + "/fastdfsStart/addOnceStartConfig",

    /**********************Topic插件管理  start************************/
    TOPOLOGY_PLUGIN_ACTION_QUERY_URL: Globals.ctx + "/topologyPlugin/queryPlugin",
    TOPOLOGY_PLUGIN_ACTION_GET_XMLDESC_URL: Globals.ctx + "/topologyPlugin/getXmlDesc",
    /**********************Topic插件管理  end************************/

    HOMEINDEX_QUERY_BOLT_URL: Globals.ctx + "/topoBolt/query/boltServiceList",

    //任务运行积压情况查询
    TASK_OVERSTOCK_SERVICE_LIST_QUERY_URL: Globals.ctx + "/overstock/queryServiceList",
    TASK_OVERSTOCK_SERVICE_DATA_QUERY_URL: Globals.ctx + "/overstock/queryZkServiceDataList",
    //根据服务组查询节点数据
    TASK_OVERSTOCK_SERVICE_DATA_QUERY_WITH_GROUP_URL: Globals.ctx + "/overstock/queryZkServiceDataListWithGroup",
    //查询服务组对应的服务列表信息
    TASK_OVERSTOCK_SERVICE_GROUP_QUERY_URL: Globals.ctx + "/overstock/queryZkServiceGroupList",

    TASK_OVERSTOCK_SERVICE_CHARTS_QUERY_URL: Globals.ctx + "/overstock/queryChartsList",

    //查询zookeeper集群列表（显示关联的jstorm集群）
    TASK_OVERSTOCK_SERVICE_ZK_QUERY_URL: Globals.ctx + "/overstock/queryZookeeperList",

    //实例状态查询(树菜单查询)
    INST_CONFIG_TREE_QUERY_URL: Globals.ctx + "/instConfig/queryInstConfigTreeData",
    INST_CONFIG_BUS_TREE_QUERY_URL: Globals.ctx + "/instConfig/querybusInstConfigTreeData",


    //查询用户程序权限信息
    BUS_USER_TREE_QUERY_PROGRAM_URL: Globals.ctx + "/userBus/queryUserProgramList",
    BUS_CFGFILE_TREE_QUERY_PROGRAM_URL: Globals.ctx + "/userBusCfgFile/queryCfgFileList",


    BUS_USER_ADD_PROGRAM_URL: Globals.ctx + "/userBus/addUserBusProgramList",
    BUS_CFGFILE_ADD_PROGRAM_URL: Globals.ctx + "/userBusCfgFile/addCfgFileList",

    BUS_USER_START_STOP_LIST_PROGRAM_URL: Globals.ctx + "/userBus/queryUserProgramStartStopList",
    BUS_USER_START_PROGRAM_URL: Globals.ctx + "/userBus/addRunProgram",
    BUS_USER_STOP_PROGRAM_URL: Globals.ctx + "/userBus/addStopProgram",
    BUS_USER_CHECK_STATUS_PROGRAM_URL: Globals.ctx + "/userBus/checkRunStopProgram",

    //业务部署图查询数据
    DEPLOY_VIEW_QUERY_DATA_URL: Globals.ctx + "/deployView/getDeployViewData",
    USER_ROLE_CONFIG_ACTION_QUERY: Globals.ctx + "/roleconfig/queryEmpeeList",

    //节点策略配置(树菜单查询)
    NODE_EXPEND_STRATEGY_CONFIG_TREE_QUERY_URL:Globals.ctx + "/nodeexpend/queryClusterTreeList",
    THRESHOLD_CONFIG_ADD_URL:Globals.ctx + "/nodeexpend/addThresholdConfig",
    THRESHOLD_CONFIG_UPDATE_URL:Globals.ctx + "/nodeexpend/updateThresholdConfig",
    THRESHOLD_CONFIG_DEL_URL:Globals.ctx + "/nodeexpend/delThresholdConfig",
    TIMING_CONFIG_ADD_URL:Globals.ctx + "/nodeexpend/addTimingConfig",
    TIMING_CONFIG_UPDATE_URL:Globals.ctx + "/nodeexpend/updateTimingConfig",
    TIMING_CONFIG_DEL_URL:Globals.ctx + "/nodeexpend/delTimingConfig",
    MANUAL_CONFIG_ADD_URL:Globals.ctx + "/nodeexpend/addManualConfig",
    MANUAL_CONFIG_UPDATE_URL:Globals.ctx + "/nodeexpend/updateManualConfig",
    MANUAL_CONFIG_EXEC_URL:Globals.ctx + "/nodeexpend/execManual",
    MANUAL_CONFIG_QUERY_RULE_URL:Globals.ctx + "/nodeexpend/queryRule",
    MANUAL_CONFIG_QUERY_ZKDATA_URL:Globals.ctx + "/nodeexpend/zkData",
    MANUAL_CONFIG_DEL_URL:Globals.ctx + "/nodeexpend/delManualConfig",
    EXEC_NODE_EXPEND_JOB_URL:Globals.ctx + "/nodeexpend/execNodeexpendJob",
    EXEC_NODE_IMM_EXPEND_JOB_URL :Globals.ctx + "/nodeexpend/execImmJob",

    /**********************定时任务  start************************/
    JOBTASKCFG_ACTION_EXEC_URL:Globals.ctx + "/jobTaskCfg/execJob",
    JOBTASKCFG_ACTION_TIMER_URL:Globals.ctx + "/jobTaskCfg/timerJob",
    JOBTASKCFG_ACTION_STOP_URL:Globals.ctx + "/jobTaskCfg/jobStop",
    PARAMETER_COLLECT_SELECT_CRON_FIVE_FIRE_ACTION_URL:Globals.ctx + "/jobTaskCfg/queryCronFiveFireTime",
    /**********************定时任务  end************************/

    /**********************定时任务  start************************/
    JOBTASKAPI_ACTION_EXEC_URL:Globals.ctx + "/jobApi/execJob",
    JOBTASKAPI_ACTION_TIMER_URL:Globals.ctx + "/jobApi/timerJob",
    JOBTASKAPI_ACTION_LOOP_URL:Globals.ctx + "/jobApi/loopJob",
    JOBTASKAPI_ACTION_STOP_URL:Globals.ctx + "/jobApi/jobStop",
    JOBTASKAPI_COLLECT_SELECT_CRON_FIVE_FIRE_ACTION_URL:Globals.ctx + "/jobApi/queryCronFiveFireTime",
    JOBTASKAPI_SELECT_JOBLIST_AND_CHECK_JOB_HEALTH:Globals.ctx + "/jobApi/queryJobsAndCheck",
    /**********************定时任务  end************************/


    /**********************************版本切换 start********************************************/
    //加载正在运行的Topology
    SWITCH_LOAD_RUNNING_TOLOLOGY_URL:Globals.ctx + "/switchMasterStandbyNet/loadRunningTopologyList",
    //加载待升级的Topology列表
    SWITCH_LOAD_UPGRADE_TOLOLOGY_URL:Globals.ctx + "/switchMasterStandbyNet/loadUpgradTopologyList",
    //查询正在运行Topology的节点列表
    SWITCH_LOAD_RUNNING_TOLOLOGY_NODE_LIST_URL:Globals.ctx + "/switchMasterStandbyNet/loadRunningTopologyNodeList",
    //版本灰度升级
    SWITCH_START_NODE_UPGRADE_TOLOLOGY_LIST_URL:Globals.ctx + "/switchMasterStandbyNet/startNodeVersionUpgrade",
    /**********************************版本切换 end********************************************/

    /**********************************参数配置 start********************************************/
    GROUP_CONFIG_ADD_URL:Globals.ctx + "/groupConfig/addGroupConfig",
    GROUP_CONFIG_DEL_URL:Globals.ctx + "/groupConfig/delGroupConfig",
    /**********************************参数配置 end********************************************/


    /**********************************节点集群管理 start********************************************/
    NODE_CLUSTER_DEPLOY_PROGRAM_LIST:Globals.ctx + "/nodeClusterDeploy/queryNodeClusterProgramList",
    NODE_CLUSTER_DEPLOY_VERSION_HOST_LIST:Globals.ctx + "/nodeClusterDeploy/queryNodeClusterVersionHostList",
    NODE_CLUSTER_DEPLOY_START_LIST:Globals.ctx + "/nodeClusterDeploy/startNodeDeploy",
    /**********************************节点集群管理 end********************************************/


};


//用于jsp url
Globals.baseJspUrl = {
    /**********************用户管理  start************************/
    //用户管理--增加和修改页面
    USER_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/setting/sysmanage/user/addEditUser",
    //详情
    USER_JSP_VIEW_EDIT_URL: Globals.ctx + "/jsp/setting/sysmanage/user/viewUser",
    //用户管理--增加和修改页面
    USER_JSP_EDIT_PASSWORD_URL: Globals.ctx + "/jsp/setting/sysmanage/user/editPasswordUser",
    /**********************用户管理  end************************/

    /**********************角色管理  start************************/
    //角色管理--增加和修改页面
    ROLE_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/setting/sysmanage/role/addEditRole",
    //角色管理 -指派权限页面
    ROLE_JSP_DISPATCH_PRIVILEGE_URL: Globals.ctx + "/jsp/setting/sysmanage/role/dispatchPrivilege",
    /**********************角色管理  end************************/

    /**********************业务权限配置  start************************/
    //角色配置--增加和修改页面
    ROLE_CONFIG_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/setting/sysmanage/busiprivilege/addEditBusiRole",
    //角色配置 -指派权限页面
    USER_ROLE_JSP_DISPATCH_ROLE_URL: Globals.ctx + "/jsp/setting/sysmanage/busiprivilege/userBindRole",
    /**********************业务权限配置  end************************/

    /**********************权限管理  start************************/
    //权限管理-新增权限页面
    PRIVILEGE_JSP_ADD_URL: Globals.ctx + "/jsp/setting/sysmanage/privilege/addPrivilege",
    /**********************权限管理  end************************/


    /**********************主机管理  start************************/
    //主机管理界面
    HOST_JSP_MANAGE_URL: Globals.ctx + "/jsp/clustermanager/mainframes/mainframespages/hostManage",
    //主机管理--增加和修改页面
    HOST_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/clustermanager/mainframes/mainframespages/addEditHost",
    //主机管理--批量修改主机密码
    HOST_JSP_BATCH_UPDATE_PASSWD_EDIT_URL: Globals.ctx + "/jsp/clustermanager/mainframes/mainframespages/updatePasswdBatch",
    //主机管理--详细信息页面
    HOST_JSP_DETAIL_URL: Globals.ctx + "/jsp/clustermanager/mainframes/mainframespages/viewHost",
    /**********************主机管理  end************************/

    /**********************业务集群配置管理  start************************/
    //业务集群配置管理--增加和修改页面
    BUSINESS_CLUSTER_CFG_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/configuremanager/businessclusterconfig/addEditBusinessClusterConfig",
    //业务集群配置主机管理--详细信息页面
    BUSINESS_CLUSTER_CFG_JSP_DETAIL_URL: Globals.ctx + "/jsp/configuremanager/businessclusterconfig/viewBusinessClusterConfig",
    /**********************业务集群配置管理  end************************/

    /**********************组件集群配置管理  start************************/
    //业务集群配置管理--增加和修改页面
    COMPONENT_CLUSTER_CFG_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/configuremanager/componentclusterconfig/addEditComponentClusterConfig",
    //业务集群配置主机管理--详细信息页面
    COMPONENT_CLUSTER_CFG_JSP_DETAIL_URL: Globals.ctx + "/jsp/configuremanager/componentclusterconfig/viewComponentClusterConfig",
    /**********************组件集群配置管理  end************************/

    /**********************业务主集群配置管理  start************************/
    //业务主集群管理--增加和修改页面
    BUS_MAIN_CLUSTER_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/configuremanager/busmainclusterconfig/addEditBusMainClusterCfg",
    //业务主集群管理--详细信息页面
    BUS_MAIN_CLUSTER_JSP_DETAIL_URL: Globals.ctx + "/jsp/configuremanager/busmainclusterconfig/viewBusMainClusterCfg",
    /**********************业务主集群配置管理  end************************/

    /*************************监控管理 start**************************************/
    //supervisor
    MONITOR_ACTION_RESOURCE_CONFIGURATION_JSP: Globals.ctx + "/jsp/minitor/resource/nimbusConfig",
    //worker metrice
    MONITOR_ACTION_RESOURCE_WORKER_METRICE_JSP: Globals.ctx + "/jsp/minitor/resource/workersMetrics",

    MONITOR_ACTION_PRESSURE_CONFIGURATION_JSP: Globals.ctx + "/jsp/minitor/pressure/topologyConfig",
    MONITOR_ACTION_PRESSURE_TOPOLOGY_SUMMARY_JSP: Globals.ctx + "/jsp/minitor/pressure/topologySummary",
    MONITOR_ACTION_PRESSURE_TOPOLOGY_COMPONENT_METRICS_JSP: Globals.ctx + "/jsp/minitor/pressure/componentMetrics",
    MONITOR_ACTION_PRESSURE_TOPOLOGY_WORKER_METRICS_JSP: Globals.ctx + "/jsp/minitor/pressure/workerMetrics",
    MONITOR_ACTION_PRESSURE_TOPOLOGY_TASK_STATS_JSP: Globals.ctx + "/jsp/minitor/pressure/taskStats",
    MONITOR_ACTION_MAIN_HOST_MONITOR_JSP: Globals.ctx + "/jsp/minitor/mainHostMonitor",
    MONITOR_ACTION_PRE_PAID_HISTORY_JSP: Globals.ctx + "/jsp/minitor/prepaid/prePaidMonitorHistory",
    MONITOR_ACTION_PRE_PAID_INCLUDE_JSP: Globals.ctx + "/jsp/minitor/prepaid/prePaidMonitorInclude",
    MONITOR_ACTION_TOPOLOGY_NODE_EDIT_JSP: Globals.ctx + "/jsp/minitor/pressure/nodeEdit",
    MONITOR_ACTION_TOPOLOGY_EXTEND_NODE_JSP: Globals.ctx + "/jsp/minitor/pressure/addExtendNode",
    MONITOR_ACTION_POSTPAID_TREND_JSP: Globals.ctx + "/jsp/minitor/postpaid/trendChart",
    MONITOR_ACTION_PREPAID_PRETRENDCHART_JSP: Globals.ctx + "/jsp/minitor/prepaid/preTrendChart",
    MONITOR_ACTION_RESULTCODE_MANAGE_JSP: Globals.ctx + "/jsp/minitor/resultCode/addEditResultCodeManage",
    /*************************监控管理 end**************************************/

    /*************************二次开发 start**************************************/
    DEVELOP_ACTION_PRESSURE_ADD_EDIT_FILE_JSP: Globals.ctx + "/jsp/develop/addEditFile",
    DEVELOP_ACTION_DEVELOP_PAGE_JSP: Globals.ctx + "/develop/openDevelopFile",
    DEVELOP_ACTION_DEVELOP_HELP_INFOMATION_JSP: Globals.ctx + "/jsp/develop/helpInfomation",
    DEVELOP_ACTION_DEVELOP_RELEASE_HANDLE_JSP: Globals.ctx + "/jsp/develop/releaseHandle",
    DEVELOP_ACTION_FILE_UPLOAD_JSP: Globals.ctx + "/jsp/develop/fileUpload",
    /*************************二次开发 end**************************************/

    /**********************************在线shell start********************************************/
    SHELL_ACTION_ADD_HOST_LINK_JSP: Globals.ctx + "/jsp/shell/addHostLink",
    SHELL_ACTION_OPEN_HOST_LINK_JSP: Globals.ctx + "/onLineShell/openHostLink",
    /**********************************在线shell end********************************************/


    /**********************************（新）部署图 start********************************************/
    DEPLOY_JSP_ADD_HOST_URL: Globals.ctx + "/jsp/clustermanager/deploy/deploypages/addHostPartition",
    //批量删除
    DEPLOY_JSP_BATCH_DEL_HOST_URL: Globals.ctx + "/jsp/clustermanager/deploy/deploypages/delHostPartition",
    //部署选中主机
    DEPLOY_ACTION_DEPLOY_URL: Globals.ctx + "/host/deployZKHost",
    //集群新增修改页面
    CLUSTER_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/clustermanager/cluster/clusterpages/addEditconfig",
    //上传文件弹框
    UPLOAD_JSP_ADD_URL: Globals.ctx + "/jsp/clustermanager/uploadpages/addftp",
    UPLOAD_JSP_CHOOSE_REMOTE_FILE_URL: Globals.ctx + "/jsp/clustermanager/uploadpages/chooseRemoteFile",
    UPLOAD_JSP_VIEW_URL: Globals.ctx + "/jsp/clustermanager/uploadpages/fileView",
    UPLOAD_JSP_ADD_SERVICE_URL: Globals.ctx + "/jsp/clustermanager/serviceUploadpages/serviceAddftp",
    UPLOAD_JSP_VIEW_SERVICE_URL: Globals.ctx + "/jsp/clustermanager/serviceUploadpages/serviceFileView",
    UPLOAD_JSP_FILE_CONTENT_SERVICE_URL: Globals.ctx + "/jsp/clustermanager/serviceUploadpages/fileContent",
    UPLOAD_JSP_EXCEL_FILE_UPLOAD: Globals.ctx + "/jsp/clustermanager/mainframes/mainframespages/excelUploadftp",
    /**********************************（新）部署图 end********************************************/

    /**********************************（新）运维管理 start********************************************/
    DEPLOY_BUS_PROGRAMS_JSP_URL: Globals.ctx + "/jsp/configuremanager/deploy/busProgramsdeploy",

    //运行主机 -- rocketmq弹框
    HOST_JSP_ROCKMQ_START_URL: Globals.ctx + "/jsp/configuremanager/run/addRocketmqForStart",
    //运行主机 -- fastdfs弹框
    HOST_JSP_FASTDFS_START_URL: Globals.ctx + "/jsp/configuremanager/run/addFastdfsForStart",
    //运行主机 -- fastdfs弹框
    HOST_JSP_FASTDFS_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addFastdfsForStop",
    //启停管理 -- 查看配置文件
    HOST_JSP_SHOW_CONFIG_CONTENT_URL: Globals.ctx + "/jsp/configuremanager/run/showConfigContent",
    //启停管理 -- 查看配置文件(多个文件)
    HOST_JSP_SHOW_MUTIL_CONFIG_CONTENT_URL: Globals.ctx + "/jsp/configuremanager/run/showMutilConfigContent",
    //启停管理 -- 查看业务状态配置文件（多个文件）
    HOST_JSP_SHOW_MUTIL_BUS_CONFIG_CONTENT_URL: Globals.ctx + "/jsp/configuremanager/deployinstconfig/showMutilBusConfigContent",
    //启停管理 -- fastdfs和rocketmq查看详情
    INST_CONFIG_JSP_CHECK_CONDITIONS_URL: Globals.ctx + "/jsp/configuremanager/deployinstconfig/instConfigMain?dialog=1",

    //组件一键启动配置
    INST_CONFIG_JSP_START_ONCE_URL: Globals.ctx + "/jsp/configuremanager/run/showStartConfig",

    //运行主机 -- DCAS弹框
    HOST_JSP_DCA_START_URL: Globals.ctx + "/jsp/configuremanager/run/addDcaForStart",
    //运行主机 -- DCAS弹框
    HOST_JSP_DCA_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addDcaForStop",

    //运行主机 -- MONITOR弹框
    HOST_JSP_MONITOR_START_URL: Globals.ctx + "/jsp/configuremanager/run/addMonitorForStart",
    //运行主机 -- MONITOR弹框
    HOST_JSP_MONITOR_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addMonitorForStop",

    //运行主机 -- DMDB弹框
    //HOST_JSP_DMDB_START_URL: Globals.ctx + "/jsp/configuremanager/run/addDmdbForStart",
    HOST_JSP_DMDB_START_URL: Globals.ctx + "/jsp/configuremanager/run/addDmdbNewForStart",

    //停止主机 -- DMDB弹框
    //HOST_JSP_DMDB_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addDmdbForStop",
    HOST_JSP_DMDB_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addDmdbNewForStop",

    //运行主机 -- zookeeper弹框
    HOST_JSP_ZOOKEEPER_START_URL: Globals.ctx + "/jsp/configuremanager/run/addZookeeperForStart",
    //停止主机 -- zookeeper弹框
    HOST_JSP_ZOOKEEPER_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addZookeeperForStop",
    //运行主机 -- jstorm弹框
    HOST_JSP_JSTORM_START_URL: Globals.ctx + "/jsp/configuremanager/run/addJstormForStart",
    //停止主机 -- jstorm弹框
    HOST_JSP_JSTORM_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addJstormForStop",
    //运行主机 -- dclog弹框
    HOST_JSP_DCLOG_START_URL: Globals.ctx + "/jsp/configuremanager/run/addDCLogForStart",
    //停止主机 -- dclog弹框
    HOST_JSP_DCLOG_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addDCLogForStop",
    //运行主机 -- m2db弹框
    HOST_JSP_M2DB_START_URL: Globals.ctx + "/jsp/configuremanager/run/addM2dbForStart",
    //停止主机 -- m2db弹框
    HOST_JSP_M2DB_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addM2dbForStop",
    //运行主机 -- dsf弹框
    HOST_JSP_DSF_START_URL: Globals.ctx + "/jsp/configuremanager/run/addDsfForStart",
    //停止主机 -- sdf弹框
    HOST_JSP_DSF_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addDsfForStop",
    //M2DB刷数据
    HOST_M2DB_REFRESH_URL: Globals.ctx + "/jsp/configuremanager/run/m2db_refresh_tables",
    HOST_M2DB_INPUT_URL: Globals.ctx + "/jsp/configuremanager/run/m2db_input_table",
    HOST_M2DB_MEM_URL: Globals.ctx + "/jsp/configuremanager/run/m2db_refresh_mem",

    //停止运行主机 -- rocketmq弹框
    HOST_JSP_ROCKMQ_STOP_URL: Globals.ctx + "/jsp/configuremanager/run/addRocketmqForStop",
    //配置修改--新建文件
    CONFIGURE_JSP_CREATE_FILE_URL: Globals.ctx + "/jsp/configuremanager/editconfig/addFile",
    //批量新增
    CONFIGURE_JSP_CREATE_BATCH_FILE_URL: Globals.ctx + "/jsp/configuremanager/editconfig/addBatchFile",
    //配置修改--新建文件夹
    CONFIGURE_JSP_CREATE_FOLDER_URL: Globals.ctx + "/jsp/configuremanager/editconfig/addFolder",
    //配置修改--新建文件夹（选择复制其下文件）
    CONFIGURE_JSP_CREATE_COPY_FOLDER_URL: Globals.ctx + "/jsp/configuremanager/editconfig/addAndCopyFolder",
    //配置修改--新建文件夹[redis]（选择复制其下文件）
    CONFIGURE_JSP_ADD_COPY_FOLDER_URL: Globals.ctx + "/jsp/configuremanager/editconfig/createAndCopyFolders",

    //配置修改--重命名文件
    CONFIGURE_JSP_RENAME_FILE_URL: Globals.ctx + "/jsp/configuremanager/editconfig/renameFile",

    //查询未部署主机
    DEPLOY_JSP_DEPLOY_HOST_URL: Globals.ctx + "/jsp/configuremanager/deploy/deployhostnotin",
    //组件集群部署
    DEPLOY_JSP_CLUSTER_DEPLOY_URL: Globals.ctx + "/jsp/clustermanager/componentcluster/clusterManage",


    //查看配置
    HOST_JSP_CONF_URL: Globals.ctx + "/jsp/configuremanager/run/viewConf",
    //TOP管理--重新负载
    TOP_REBALANCE_RELOAD_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/top/topRebalanceReload",

    //启停管理--采集程序管理
    DCM_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/dcm/dcmProgramManage",

    //启停管理--路由程序管理
    ROUTE_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/route/routeProgramManage",
    //启停管理--计费查看定义
    BILLING_VIEW_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/billing/viewBillingConfFile",
    //启停管理--计费查看定义
    COMMON_RUN_TOPOLOGY_VIEW_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/runtopology/viewTopologyConfFile",
    //启停管理--Topology服务启动查看
    COMMON_RUN_TOPOLOGY_VIEW_SERVICE_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/runtopology/viewTopologyService",
    //启停管理--计费程序管理
    BILLING_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/billing/billingProgramManage",
    //启停管理--周边程序管理
    OTHER_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/other/otherProgramManage",
    OTHER_PROGRAM_JSP_RUNS_URL: Globals.ctx + "/jsp/configuremanager/run/other/otherRunPrograms",
    OTHER_PROGRAM_JSP_STOPS_URL: Globals.ctx + "/jsp/configuremanager/run/other/otherStopPrograms",
    OTHER_PROGRAM_JSP_INPUT_PARAM_URL: Globals.ctx + "/jsp/configuremanager/run/other/otherInputParam",

    //区分IP
    RUN_DIFF_IP_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/rundiffip/runDiffProgramManage",
    //不区分IP
    RUN_SAME_IP_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/runsameip/runSameProgramManage",
    RUN_SAME_IP_ADD_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/runsameip/addEditProgram",
    RUN_DIFF_IP_ADD_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/rundiffip/addEditProgram",

    //Topology
    RUN_TOPOLOGY_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/runtopology/runTopologyProgramManage",
    //业务启停输出结果界面
    RUN_STOP_RESULT_JSP_URL: Globals.ctx + "/jsp/configuremanager/run/runResult",

    //启停管理--月租查看任务的定义页面
    RENT_VIEW_JSP_FILE_CONF_URL: Globals.ctx + "/jsp/configuremanager/run/rent/viewRentConfFile",
    //启停管理--月租程序管理
    RENT_PROGRAM_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/run/rent/rentProgramManage",
    //业务程序管理--添加修改页面
    PROGRAM_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/configuremanager/program/addEditProgram",
    //业务程序管理--显示返回信息
    SERVICE_PROGRAM_JSP_RESULT_URL: Globals.ctx + "/jsp/configuremanager/run/programView",
    //日志级别调整
    LOG_LEVEL_JSP_CONFIRM_URL: Globals.ctx + "/jsp/configuremanager/logLevelCfg/confirmDetail",
    //日志级别新增&修改
    LOG_LEVEL_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/configuremanager/logLevelCfgNew/addEditLogLevel",
    //主备切换--灰度升级
    SWITCH_PROGRAM_UPGRADE_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandby/greyUpgradeOpt",
    //主备切换--正式发布
    SWITCH_PROGRAM_LAUNCH_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandby/officialLaunchOpt",
    //主备切换--回退
    SWITCH_PROGRAM_ROLLBACK_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandby/rollbackOpt",
    //主备切换--灰度升级配置文件修改
    SWITCH_PROGRAM_UPGRADE_CONFIG_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandby/greyUpgradeConfigOpt",

    //ABM主备切换--灰度升级
    SWITCH_ABM_PROGRAM_UPGRADE_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandbyABM/greyUpgradeOpt",
    //ABM主备切换--正式发布
    SWITCH_ABM_PROGRAM_LAUNCH_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandbyABM/officialLaunchOpt",
    //ABM主备切换--回退
    SWITCH_ABM_PROGRAM_ROLLBACK_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandbyABM/rollbackOpt",
    //ABM主备切换--灰度升级配置文件修改
    SWITCH_ABM_PROGRAM_UPGRADE_CONFIG_JSP_MANAGE_URL: Globals.ctx + "/jsp/configuremanager/masterstandbyABM/greyUpgradeConfigOpt",

    //环境变量新增修改页面
    ENVIRONMENT_JSP_ADD_EDIT_URL: Globals.ctx + "/jsp/configuremanager/environmentconfig/addEditEnv",

    /**********************************（新）运维管理 end********************************************/

    /**********************************（新）监控管理 start********************************************/
    MONITOR_JSP_SUMMARY_TOPOLOGY_SHOW_CONFIGURE_URL: Globals.ctx + "/jsp/monitormanager/configinfo/configInfoMain",
    //日志管理页面
    MONITOR_JSP_SUMMARY_CLUSTER_LOG_FILE_URL: Globals.ctx + "/jsp/monitormanager/logmanager/logManager",

    /**********************************（新）监控管理 end********************************************/

    /**********************************Topic配置管理 start********************************************/
    TOPIC_CONFIG_JSP_ADD_URL: Globals.ctx + "/jsp/configuremanager/topic/addTopicConfig",
    /**********************************Topic配置管理 end********************************************/


    /**********************************（新）数据源and命令配置 start********************************************/
    DATASOURCE_JSP_ADD_EDIT:Globals.ctx + "/jsp/configuremanager/configdfine/addEditDataSource",

    COMMAND_JSP_ADD_EDIT:Globals.ctx + "/jsp/configuremanager/configdfine/addEditCommandConfig",

    COMMAND_EMPEE_RELATION_JSP:Globals.ctx + "/jsp/configuremanager/configdfine/setCmdEmpeeRelation",



    //用户业务权限指派
    BUS_USER_JSP_DISPATCH_PRIVILEGE_URL: Globals.ctx + "/jsp/clustermanager/businessassigned/businessDispatchPrivilege",
    BUS_USER_JSP_DETAILS_URL: Globals.ctx + "/jsp/clustermanager/businessassigned/businessDetailsPrivilege",
    BUS_CFGFILE_JSP_DISPATCH_PRIVILEGE_URL: Globals.ctx + "/jsp/clustermanager/businessassigned/busCfgFileDispatchPrivilege",
    BUS_USER_JSP_DISPATCH_CLUSTER_URL: Globals.ctx + "/jsp/clustermanager/businessassigned/businessDispatchCluster",

    //业务部署图实例，主机信息列表展示
    DEPLOY_VIEW_LIST_URL: Globals.ctx + "/jsp/clustermanager/deploy/deployview/businessDeployList",

    //集群摘要集群信息跳转
    BUS_JSP_DISPATCH_CLUSTER_URL: Globals.ctx + "/jsp/monitormanager/clustersummary/clusterSummaryMain",

    /**********************************新增策略配置 start********************************************/
    NODE_STRATEGY_THRESHOLD_CONFIG_URL:Globals.ctx + "/jsp/nodeexpend/thresholdConfig",
    NODE_STRATEGY_TIMING_CONFIG_URL:Globals.ctx + "/jsp/nodeexpend/timingConfig",
    NODE_STRATEGY_TIMING_DYNAMIC_URL:Globals.ctx + "/jsp/dynamicNodeexpend/timingConfig",
    NODE_STRATEGY_MANUAL_DYNAMIC_URL:Globals.ctx + "/jsp/dynamicNodeexpend/manualConfig",
    NODE_STRATEGY_MANUAL_CONFIG_URL:Globals.ctx + "/jsp/nodeexpend/manualConfig",
    NODE_STRATEGY_TIMING_LOG_URL:Globals.ctx + "/jsp/nodeexpend/queryLog",
    NODE_STRATEGY_DYNMAIC_LOG_URL:Globals.ctx + "/jsp/dynamicNodeexpend/queryLog",
    NODE_STRATEGY_EXPANSION_REPORT_URL:Globals.ctx + "/jsp/dynamicNodeexpend/expansionReport",
    NODE_STRATEGY_UNEXPANSION_REPORT_URL:Globals.ctx + "/jsp/dynamicNodeexpend/unexpansionReport",
    NODE_STRATEGY_FORECAST_REPORT_URL:Globals.ctx + "/jsp/dynamicNodeexpend/forecastReport",
    NODE_STRATEGY_UNFORECAST_REPORT_URL:Globals.ctx + "/jsp/dynamicNodeexpend/unforecastReport",
    NODE_STRATEGY_TIMING_TASK_CONFIG_URL:Globals.ctx + "/jsp/jobtaskcfg/timer/jobTimerTaskManage",
    NODE_STRATEGY_THRESHOLD_DY_CONFIG_URL:Globals.ctx + "/jsp/dynamicNodeexpend/thresholdConfig",
    /**********************************新增策略配置 end********************************************/

    /**********************************（新）定时任务 start********************************************/
    JOBTASKCFG_JSP_ADD_EDIT_URL:Globals.ctx + "/jsp/jobtaskcfg/convention/addEditJobTask",
    JOBTIMERTASKCFG_JSP_ADD_EDIT_URL:Globals.ctx + "/jsp/jobtaskcfg/timer/addEditJobTimerTask",
    JOBTIMERTASKCFG_JSP_ADD_CRON_URL:Globals.ctx + "/jsp/jobtaskcfg/timer/quartz_cron",
    JOBTIMERTASKCFG_JSP_QUERYLOG_CRON_URL:Globals.ctx + "/jsp/jobtaskcfg/timer/queryLog",

    JOBLIST_JSP_ADD_EDIT_URL:Globals.ctx + "/jsp/jobtaskcfg/addEditJobTask",
    JOBLIST_JSP_QUERY_LOG_URL:Globals.ctx + "/jsp/jobtaskcfg/queryLog",



    /**********************************（新）数据源配置 end********************************************/

    //ABM主备切换--灰度升级配置文件修改
    SWITCH_NET_UPGRAD_HISTORY_QUERY_JSP_MANAGE_URL:Globals.ctx + "/jsp/configuremanager/masterstandbyNet/upgradHistory"
};