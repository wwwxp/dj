//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["topologyGrid"] = mini.get("topologyGrid");//取得任务表格
    search();
});


/**
 * 查询
 */
function search() {
	datagridLoadPage(JsVar["topologyGrid"],{clusterName:clusterName},"",Globals.baseActionUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_SUMMARY_URL);
}

/**
 * 格式化端口显示数据
 * @param e
 * @returns {String}
 */
function formatPort(e){
	return e.record.numUsedWorkers+"/"+e.record.numWorkers;
}

/**
 * 格式化“配置”列显示
 * @param e
 * @returns {String}
 */
function getConfig(e){
	return  '<a class="Delete_Button" href="javascript:showConfig(\''+e.record.id+'\')">conf</a>';
}


/**
 * 格式化拓扑名称显示
 * @param e
 * @returns {String}
 */
function formatTopologyName(e){
	return  '<a class="Delete_Button" href="javascript:topologySummary(\''+e.record.id+'\',\''+e.record.name+'\')">'+e.record.name+'</a>';
}

/**
 * 格式化ComponentMetrics显示
 */
function formatComponentMetrics(e){
	return  '<a class="Delete_Button" href="javascript:componentMetrics(\''+e.record.id+'\',\''+e.record.name+'\')">Component Metrics</a>';
}

/**
 * 格式化WorkerMetrics显示
 * @param e
 * @returns {String}
 */
function formatWorkerMetrics(e){
	return  '<a class="Delete_Button" href="javascript:workerMetrics(\''+e.record.id+'\',\''+e.record.name+'\')">Worker Metrics</a>';
}

/**
 * 格式化TaskStats显示
 * @param e
 * @returns {String}
 */
function formatTaskStats(e){
	return  '<a class="Delete_Button" href="javascript:taskStats(\''+e.record.id+'\',\''+e.record.name+'\')">TaskStats</a>';
}

/**
 * 显示topology配置
 */
function showConfig(topologyId){
	showDialog("Topology Conf","800","550",Globals.baseJspUrl.MONITOR_ACTION_PRESSURE_CONFIGURATION_JSP,function(){
		
	},{clusterName:clusterName,topologyId:topologyId});
	 
	
}

/**
 * 弹出显示拓扑摘要信息
 * @param topologyId
 * @param topologyName
 */
function topologySummary(topologyId,topologyName){
    showDialog("Topology Summary","100%","100%",Globals.baseJspUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_SUMMARY_JSP,function(){
		
	},{clusterName:clusterName,topologyId:topologyId,topologyName:topologyName});
}

/**
 * 弹出显示componentMetrics信息
 * @param topologyId
 * @param topologyName
 */
function componentMetrics(topologyId,topologyName){
    showDialog("Component Metrics","1200","550",Globals.baseJspUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_COMPONENT_METRICS_JSP,function(){
		
	},{clusterName:clusterName,topologyId:topologyId,topologyName:topologyName});
}

/**
 * 弹出显示workerMetrics信息
 * @param topologyId
 * @param topologyName
 */
function workerMetrics(topologyId,topologyName){
    showDialog("worker Metrics","1200","550",Globals.baseJspUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_WORKER_METRICS_JSP,function(){
		
	},{clusterName:clusterName,topologyId:topologyId,topologyName:topologyName});
}

/**
 * 弹出显示taskStats信息
 * @param topologyId
 * @param topologyName
 */
function taskStats(topologyId,topologyName){
    showDialog("Task Stats","800","550",Globals.baseJspUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_TASK_STATS_JSP,function(){
		
	},{clusterName:clusterName,topologyId:topologyId,topologyName:topologyName});
}

