
/**
 * 获取所有系统的数据字典
 * @param type
 */
function getSysDictData(type,flag){
    var dataArray = [];
	if (flag) {
		dataArray.push({id:"", text:"=请选择=", type:type});
	}
    for (i = 0; i < sysDictData.length; i++) {
        if (sysDictData[i].type == type) {
            dataArray.push(sysDictData[i]);
        }
    }
    //把数组按照order属性排序
    dataArray.sort(function(a,b){
        return a.order-b.order;
    });
    return dataArray;
}

/**
 * 所有模块数据字典
 * @param type  类型
 * @returns {Array}
 */
var sysDictData = [
    {id:1,code:1,text:'业务角色',order:1,type:"role_type"},
    {id:2,code:2,text:'系统角色',order:2,type:"role_type"},
    {id:1,code:3,text:'菜单权限',order:1,type:"privilege_type"},
    {id:1,code:0,text:'失效',order:1,type:"host_state"},
    {id:2,code:1,text:'启用',order:2,type:"host_state"},
    {id:1,code:0,text:'待机',order:1,type:"host_run_state"},
    {id:2,code:1,text:'运行中',order:2,type:"host_run_state"},
    {id:1,code:0,text:'未运行',order:1,type:"task_run_state"},
    {id:2,code:1,text:'运行中',order:2,type:"task_run_state"},
    {id:1,code:0,text:'失效',order:1,type:"queue_state"},
    {id:2,code:1,text:'启用',order:2,type:"queue_state"},
    {id:1,code:0,text:'全部',order:1,type:"top_rebalance_reload"},
    {id:2,code:1,text:'部分',order:2,type:"top_rebalance_reload"},
    {id:1,code:0,text:'已终止',order:1,type:"queue_run_state"},
    {id:2,code:1,text:'运行中',order:2,type:"queue_run_state"},
    {id:1,code:0,text:'备',order:1,type:"nimbus_type"},
    {id:1,code:1,text:'主',order:2,type:"nimbus_type"},
    {id:1,code:1,text:'bolt类型',order:2,type:"topolopy_node_type"},
    {id:2,code:2,text:'spout类型',order:1,type:"topolopy_node_type"},
    {id:3,code:3,text:'自定义类型',order:3,type:"topolopy_node_type"},
    {id:3,code:'A',text:'格式化',order:1,type:"procer_id"},
    {id:3,code:'C',text:'批价',order:2,type:"procer_id"},
    {id:1,code:'1',text:'语音',order:1,type:"cdr_type"},
    {id:2,code:'2',text:'数据',order:2,type:"cdr_type"},
    {id:3,code:'3',text:'短信',order:3,type:"cdr_type"},
    {id:1,code:'1',text:'扩容(业务不中断)',order:1,type:"rebalance_oper_type"},
    {id:2,code:'2',text:'重新分配(业务部分中断)',order:2,type:"rebalance_oper_type"},
    
    {id:1,code:'M',text:'主',order:1,type:"topology_attr"},
    {id:2,code:'B',text:'备',order:2,type:"topology_attr"},
    
    //RocketMq组件
    {id:1,code:"namesrv",text:'namesrv',order:1,type:"rocketmq_deploy_type"},
    {id:2,code:"broker",text:'broker',order:2,type:"rocketmq_deploy_type"},
    
    //fastDFS组件
    {id:1,code:"tracker",text:'tracker',order:1,type:"fastdfs_deploy_type"},
    {id:2,code:"storage",text:'storage',order:2,type:"fastdfs_deploy_type"},
    {id:3,code:"nginx",text:'nginx',order:3,type:"fastdfs_deploy_type"},
    
    //DCA组件
    {id:1,code:"dcam",text:'dcam',order:1,type:"dca_deploy_type"},
    {id:2,code:"dcas",text:'dcas',order:2,type:"dca_deploy_type"},
    {id:3,code:"redis",text:'redis',order:3,type:"dca_deploy_type"},
    {id:4,code:"daemon",text:'daemon',order:4,type:"dca_deploy_type"},
    {id:11,code:"sentinel",text:'哨兵(sentinel)',order:12,type:"dca_deploy_type"},
    {id:5,code:"redisIncRefresh",text:'增量刷新',order:5,type:"dca_deploy_type"},
    {id:6,code:"redisWholeRefresh",text:'全量刷新',order:6,type:"dca_deploy_type"},
    {id:7,code:"redisIncCheck",text:'增量稽核',order:7,type:"dca_deploy_type"},
    {id:8,code:"redisWholeCheck",text:'全量稽核',order:8,type:"dca_deploy_type"},
    {id:9,code:"redisRevise",text:'修复程序',order:9,type:"dca_deploy_type"},
    {id:10,code:"switchDCA",text:'主备切换(switchDCA)',order:10,type:"dca_deploy_type"},
    {id:11,code:"monitorDCA",text:'监控程序(monitorDCA)',order:11,type:"dca_deploy_type"},
    
    //DMDB组件
    //2、其中后两种模式，在删除时需要调用删除数据的接口，且需要在关闭当前实例后调用
    //3、主机模式已启动情况下，仍支持修改相关配置文件
    //{id:1,code:"main_pattern",text:'main_pattern',order:1,type:"dmdb_deploy_type"},
    {id:2,code:"instance_pattern",text:'instance_pattern(store)',order:2,type:"dmdb_deploy_type"},
    {id:3,code:"route_pattern",text:'route_pattern(tdal)',order:3,type:"dmdb_deploy_type"},
    {id:4,code:"sync_pattern",text:'sync_pattern',order:3,type:"dmdb_deploy_type"},
    {id:5,code:"mgr_pattern",text:'mgr_pattern',order:5,type:"dmdb_deploy_type"},
    {id:6,code:"watcher_pattern",text:'watcher_pattern',order:6,type:"dmdb_deploy_type"},
    {id:7,code:"movesync_pattern",text:'movesync_pattern',order:7,type:"dmdb_deploy_type"},
    
    //Jstorm组件
    {id:1,code:"nimbus",text:'nimbus',order:1,type:"jstorm_deploy_type"},
    {id:2,code:"supervisor",text:'supervisor',order:2,type:"jstorm_deploy_type"},

    //Dsf组件
    {id:1,code:"dsf",text:'dsf',order:1,type:"dsf_deploy_type"},
    /*{id:2,code:"agent",text:'agent',order:1,type:"dsf_deploy_type"},*/

    //监控组件
    {id:1,code:"monitor",text:'monitor',order:1,type:"monitor_deploy_type"},
    /*{id:2,code:"elasticsearch",text:'elasticsearch',order:2,type:"monitor_deploy_type"}*/
    
    //集群类型
    {id:1,code:"zookeeper",text:'zookeeper',order:1,type:"cluster_type_list", partition:"component"},
    {id:2,code:"dca",text:'dca',order:2,type:"cluster_type_list", partition:"component"},
    {id:3,code:"rocketmq",text:'rocketmq',order:3,type:"cluster_type_list", partition:"component"},
    {id:4,code:"m2db",text:'m2db',order:4,type:"cluster_type_list", partition:"component"},
    {id:5,code:"fastdfs",text:'fastdfs',order:5,type:"cluster_type_list", partition:"component"},
    {id:6,code:"monitor",text:'monitor',order:6,type:"cluster_type_list", partition:"component"},
    {id:7,code:"dclog",text:'dclog',order:7,type:"cluster_type_list", partition:"component"},
    {id:8,code:"dmdb",text:'dmdb',order:8,type:"cluster_type_list", partition:"component"},
    
    {id:9,code:"rent",text:'rent',order:9,type:"cluster_type_list", partition:"business"},
    {id:10,code:"other",text:'other',order:10,type:"cluster_type_list", partition:"business"},
    {id:11,code:"billing",text:'billing',order:11,type:"cluster_type_list", partition:"business"},
    {id:12,code:"route",text:'route',order:12,type:"cluster_type_list", partition:"business"},
    {id:13,code:"dsf",text:'dsf',order:13,type:"cluster_type_list", partition:"component"},

    //是否根据主机IP拆分配置文件
    {id:0,code:"0",text:'否',order:0,type:"personal_conf_list"},
    {id:1,code:"1",text:'是',order:1,type:"personal_conf_list"},
    
    //是否需要参数
    {id:0,code:"0",text:'否',order:0,type:"is_param_list"},
    {id:1,code:"1",text:'是',order:1,type:"is_param_list"},
    
    //组件大类型
    {id:1,code:"1",text:'基本类',order:1,type:"type_list"},
    {id:2,code:"3",text:'业务类',order:2,type:"type_list"},
    
    //主业务集群类型
    {id:1,code:"1",text:'OCS在线计费',order:1,type:"BUS_CLUSTER_TYPE_LIST"},
    {id:2,code:"2",text:'后付费',order:2,type:"BUS_CLUSTER_TYPE_LIST"},
    
    //程序状态
    {id:1,code:"1",text:'运行',order:1,type:"PROGRAM_STATE_LIST"},
    {id:2,code:"0",text:'未运行',order:2,type:"PROGRAM_STATE_LIST"},

    //程序状态
    {id:1,code:"1",text:'运行',order:1,type:"INST_STATUS"},
    {id:2,code:"0",text:'停止',order:2,type:"INST_STATUS"},
    
    //环境变量状态配置
    {id:1,code:0,text:'失效',order:1,type:"env_state"},
    {id:2,code:1,text:'有效',order:2,type:"env_state"},
    
    //任务积压查询界面排序规则
    {id:1,code:"TASK_ID_ASC",text:'任务ID升序',order:1,type:"TASK_SORT_LIST"},
    {id:2,code:"TASK_ID_DESC",text:'任务ID降序',order:2,type:"TASK_SORT_LIST"},
    {id:3,code:"EXEC_QUENE_SIZE_ASC",text:'执行队列升序',order:3,type:"TASK_SORT_LIST"},
    {id:4,code:"EXEC_QUENE_SIZE_DESC",text:'执行队列降序',order:4,type:"TASK_SORT_LIST"},
    {id:5,code:"FILE_QUEUE_SIZE_ASC",text:'fileQueue升序',order:5,type:"TASK_SORT_LIST"},
    {id:6,code:"FILE_QUEUE_SIZE_DESC",text:'fileQueue降序',order:6,type:"TASK_SORT_LIST"},
    {id:7,code:"MSG_COUNT_ASC",text:'发送消息总量(向C进程)升序',order:7,type:"TASK_SORT_LIST"},
    {id:8,code:"MSG_COUNT_DESC",text:'发送消息总量(向C进程)降序',order:8,type:"TASK_SORT_LIST"},

    //自动刷新配置
    {id:0,code:"0",text:'不自动刷新',order:1,type:"REFRESH_CONFIG"},
    {id:1,code:"30",text:'频率：30S/次',order:2,type:"REFRESH_CONFIG"},
    {id:2,code:"60",text:'频率：60S/次',order:3,type:"REFRESH_CONFIG"},
    
    //业务角色状态 
    {id:0,code:0,text:'失效',order:1,type:"role_state"},
    {id:1,code:1,text:'有效',order:2,type:"role_state"},

    //定时任务类型
    {id:0,code:2,text:'循环任务',order:1,type:"job_task_type"},
    {id:1,code:1,text:'自定义任务',order:2,type:"job_task_type"},

    {id:0,code:0,text:'一次性任务',order:1,type:"job_type"},
    {id:0,code:2,text:'循环任务',order:2,type:"job_type"},
    {id:1,code:1,text:'自定义任务',order:3,type:"job_type"},

    //定时任务状态0：无效 1：有效
    {id:0,code:0,text:'无效',order:2,type:"job_task_cron_status"},
    {id:1,code:1,text:'有效',order:1,type:"job_task_cron_status"},

    //定时任务业务关联-业务类型
    {id:0,code:1,text:'弹性伸缩业务',order:1,type:"job_task_bus_type"},
    {id:1,code:2,text:'表数据定时处理',order:2,type:"job_task_bus_type"},

    //数据源状态
    {id:0,code:0,text:'无效',order:2,type:"valid_flag"},
    {id:1,code:1,text:'有效',order:1,type:"valid_flag"},

    //数据源类型
    {id:0,code:'Oracle',text:'Oracle',order:2,type:"datasource_type"},
    {id:1,code:'MySql',text:'MySql',order:1,type:"datasource_type"},

    //启动方式
    {id:1,code:1,text:'手动执行',order:1,type:"excu_type"},
    {id:2,code:2,text:'定时执行',order:2,type:"excu_type"},

    //文件归档处理方式
    {id:1,code:1,text:'归档后源文件清理',order:1,type:"excu_file_type"},
    {id:2,code:2,text:'归档后源文件不清理',order:2,type:"excu_file_type"},

    //处理方式
    {id:1,code:'1',text:'归档前目标数据清空',order:1,type:"excu_rule"},
    {id:2,code:'2',text:'归档后源表数据清空(含过滤条件)',order:2,type:"excu_rule"},
    {id:3,code:'3',text:'归档后进行表分析',order:3,type:"excu_rule"},
    {id:4,code:'4',text:'归档后进行索引分析',order:4,type:"excu_rule"},

    //动态表名后缀
    {id:1,code:'[TB,TB1,TB2,...]',text:'[TB,TB1,TB2,...]',order:1,type:"source_table_suffix_type"},
    {id:2,code:'TB_[LATN_ID,LATN_ID1,...]',text:'TB_[LATN_ID,LATN_ID1,...]',order:1,type:"source_table_suffix_type"},
    {id:3,code:'TB_[YYYYMM]',text:'TB_[YYYYMM]',order:2,type:"source_table_suffix_type"},
    {id:4,code:'TB_[YYYYMMDD]',text:'TB_[YYYYMMDD]',order:3,type:"source_table_suffix_type"},
    {id:5,code:'TB_[YYYYMM-1]',text:'TB_[YYYYMM-1]',order:4,type:"source_table_suffix_type"},
    {id:6,code:'TB_[YYYYMMDD-1]',text:'TB_[YYYYMMDD-1]',order:5,type:"source_table_suffix_type"},

    //重单配置策略
    {id:1,code:'1',text:'完全拣重',order:1,type:"par_repeat_bill_rule"},
    {id:2,code:'2',text:'交叉拣重',order:2,type:"par_repeat_bill_rule"},
    {id:3,code:'3',text:'包含拣重',order:3,type:"par_repeat_bill_rule"},

    //重单表名


    //数据源驱动
    {id:0,code:'oracle.jdbc.driver.OracleDriver',text:'Oracle',order:2,type:"driverClass"},
    {id:1,code:'com.mysql.jdbc.Driver',text:'MySql',order:1,type:"driverClass"},

    //指标类型
    {id:1, code:'1', text:'CPU', order:1, type:"quota_type"},
    {id:2, code:'2', text:'内存', order:2, type:"quota_type"},
    {id:3, code:'3', text:'磁盘', order:3, type:"quota_type"},
    {id:4, code:'4', text:'网络', order:4, type:"quota_type"},
    //指标类型
    {id:1, code:'1', text:'CPU', order:1, type:"quota_dy_type"},
    {id:2, code:'2', text:'内存', order:2, type:"quota_dy_type"},
    {id:3, code:'3', text:'磁盘', order:3, type:"quota_dy_type"},
    {id:5, code:'5', text:'业务量', order:4, type:"quota_dy_type"},
    {id:1, code:'1', text:'集群主机资源最大>=', order:1, type:"condition_param_expend_dy"},
    {id:1, code:'1', text:'集群主机资源最大<=', order:1, type:"condition_param_unexpend_dy"},
    //条件类型
    {id:1, code:'1', text:'集群主机资源最大>=', order:1, type:"condition_param_expend"},
    {id:2, code:'2', text:'集群主机平均资源占比>=', order:2, type:"condition_param_expend"},
    //{id:3, code:'3', text:'集群主机资源都>=', order:3, type:"condition_param_expend"},
    {id:1, code:'1', text:'集群主机资源最大<=', order:1, type:"condition_param_unexpend"},
    {id:2, code:'2', text:'集群主机平均资源占比<=', order:2, type:"condition_param_unexpend"},
    //{id:3, code:'3', text:'集群主机资源都<=', order:3, type:"condition_param_unexpend"}

    //节点配置   -版本上传 是否全量
    {id:1,code:'1',text:'全量',order:1,type:"node_version_pkg_full"},
    {id:2,code:'0',text:'增量',order:2,type:"node_version_pkg_full"},

    //节点配置  -程序启停 已部署程序的运行/停止
    {id:1,code:'1',text:'运行中',order:1,type:"deployed_node_state"},
    {id:2,code:'0',text:'停止',order:2,type:"deployed_node_state"}
];

