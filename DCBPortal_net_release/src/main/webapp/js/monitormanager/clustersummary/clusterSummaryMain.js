/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-8-17
 * Time: 下午15:10
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var MyChart=new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //取得表格
    JsVar["clusterGrid"] = mini.get("clusterGrid");
    JsVar["topologyGrid"] = mini.get("topologyGrid");
    JsVar["nimbusGrid"] = mini.get("nimbusGrid");
    JsVar["supervisorGrid"] = mini.get("supervisorGrid");
    JsVar["zookeeperGrid"] = mini.get("zookeeperGrid");
    
    //获取跳转参数
    getForwardParams();
});

function hostsRenderer(e){
	var CLUSTER_CODE = e.record.clusterName;
	 var resultHost ="0";
	 getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,{CLUSTER_CODE:CLUSTER_CODE},"角色管理-删除角色",
                function(result){
			 		if(!isEmptyObject(result)){
			 			resultHost = result["HOST_NUM"];
			 		} 
			       
                },"expendStrategyLog.queryhosts",null,false);
	return resultHost;
	 
}

function zkDataRenderer(e){
	var CLUSTER_CODE = JsVar["CLUSTER_CODE"];
	var name = e.record.name;
	var zkData=0;
	 getJsonDataByPost(Globals.baseActionUrl.MANUAL_CONFIG_QUERY_ZKDATA_URL,{CLUSTER_CODE:CLUSTER_CODE,TASK_NAME:name},"",
                function(result){
			 		if(!isEmptyObject(result)){
			 			zkData = result[name];
			 		} 
			       
                },null,null,false);
	return zkData;
	 
}

function queryNodePV(e){
	var PROGRAM_NAME = e.record.name;
	var NodeName = "PV_DATA"+PROGRAM_NAME;
	if(JsVar[NodeName]==undefined){
		 getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,{PROGRAM_NAME:PROGRAM_NAME},"角色管理-删除角色",
	                function(result){
				 		if(isEmptyObject(result)){
				 			 JsVar[NodeName]={};
				 		}else{
				 			 JsVar[NodeName] = result;
				 		}
				       
	                },"expendStrategyLog.queryStrategyConfigLogBySId",null,false);
	}
	var field = e.field;
	 
	if(JsVar[NodeName][field]){
		return JsVar[NodeName][field];
	}else{
		return "0";
	}
		
	 
}

/**
 * 获取请求参数
 */
function getForwardParams(){

    // var forwardParamString = window.location.search;
    // var queryArray = forwardParamString.split("=");

    //业务集群编码
    // var clusterCode = queryArray[1];
    //业务主集群
    JsVar['CLUSTER_CODE'] = getQueryString('CLUSTER_CODE');
    //加载表格信息
    loadGridInfo();
    //加载图信息
    loadLineCharts();
}




/**
 * 加载Tab数据
 * @param e
 */
function loadPage(e){
	//业务主集群
	var busClusterId = e.tab.id;
	JsVar["BUS_CLUSTER_ID"] = busClusterId;
	JsVar["BUS_CLUSTER_NAME"] = e.tab.title;
	//当前Jstorm集群编码
	JsVar["CLUSTER_CODE"] = e.tab.clusterCode;
	
	//加载表格信息
    loadGridInfo();
    //加载图信息
    loadLineCharts();
}


/**
 * 加载表格信息
 */
function loadGridInfo(){
	var params = {
		clusterName:JsVar["CLUSTER_CODE"]
	};
	//加载集群、拓扑、nimbus、supervisor信息表
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_DATAGRID_URL, params, "",
		function(result){
		if(result.error!=null){
			showErrorMessageAlter("获取集群信息失败！");
		}else{
			if(!isEmptyObject(result)){
				JsVar["clusterGrid"].setData(result.clusterData);
				JsVar["topologyGrid"].setData(result.topologyData);
			    JsVar["nimbusGrid"].setData(result.nimbusData);
			    JsVar["supervisorGrid"].setData(result.supervisorData);
			    JsVar["zookeeperGrid"].setData(result.zookeeperData);
			    //合并单元格
			    zkOnLoad(result.zookeeperData.length);
			}else{
				JsVar["clusterGrid"].clearRows();
				JsVar["topologyGrid"].clearRows();
			    JsVar["nimbusGrid"].clearRows();
			    JsVar["supervisorGrid"].clearRows();
			    JsVar["zookeeperGrid"].clearRows();
			}
		}
	},null,null,false);
}

/**
 * 加载折线图
 */
function loadLineCharts(){
	var lineChartsOptions = new Object();
	
	//基于准备好的dom，初始化echarts实例
    MyChart["Failed"] = echarts.init(document.getElementById('Failed'));
    MyChart["Emitted"] = echarts.init(document.getElementById('Emitted'));
    MyChart["Acked"] = echarts.init(document.getElementById('Acked'));
    MyChart["SendTps"] = echarts.init(document.getElementById('SendTps'));
    MyChart["RecvTps"] = echarts.init(document.getElementById('RecvTps'));
    MyChart["ProcessLatency"] = echarts.init(document.getElementById('Process'));
    MyChart["CpuUsedRatio"] = echarts.init(document.getElementById('CPUUsed'));
    MyChart["MemoryUsed"] = echarts.init(document.getElementById('MemUsed'));
    MyChart["HeapMemory"] = echarts.init(document.getElementById('HeapMemory'));
	
	 //获取图表数据
    var data=getChartsDataByPost();
    var len = 0;
    var array = ["Failed","Emitted","Acked","SendTps","RecvTps","ProcessLatency","CpuUsedRatio","MemoryUsed","HeapMemory"];
    var transfer = ["失败发送","成功发送","处理反馈","发送速率","接收速率","流转耗时","CPU使用","内存使用","堆内存"];
    for(var j = 0; j < array.length; j ++){
    	if(data[array[j]] != undefined){
    		len = data[array[j]]["series"][0]["data"].length;
    		break;
    	}
    }
    
    for(var i = 0; i < array.length; i ++){
    	if(data[array[i]] != undefined){
    		//提示tooltip
    		if(array[i] == "Failed" ||array[i] == "Emitted" ||array[i] == "Acked"){
    			data[array[i]]["tooltip"]["formatter"] = array[i] + " ：{c}次<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} 次';
    		}
    		if(array[i] == "SendTps" ||array[i] == "RecvTps"){
    			data[array[i]]["tooltip"]["formatter"] = array[i] + " ：{c}tps<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} tps';
    			for(var k = 0; k < len; k ++){
    				data[array[i]]["series"][0]["data"][k] = data[array[i]]["series"][0]["data"][k].toFixed(2);
    			}
    		}
    		if(array[i] == "ProcessLatency"){
    			data[array[i]]["tooltip"]["formatter"] = "Process ：{c}ms<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} ms';
    			data[array[i]]["title"]["text"]='Process';
    			for(var k = 0; k < len; k ++){
    				var tmpData = data[array[i]]["series"][0]["data"][i];
    				data[array[i]]["series"][0]["data"][i] = (tmpData/1000);
    			}
    		}
    		if(array[i] == "CpuUsedRatio"){
    			data[array[i]]["tooltip"]["formatter"] =  array[i] + " ：{c}%<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} %';
    		}
    		if(array[i] == "MemoryUsed" || array[i] == "HeapMemory"){
    			data[array[i]]["tooltip"]["formatter"] =  array[i] + " ：{c}MB<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} MB';
    			for(var k = 0; k < len; k ++){
    				var tmpData = data[array[i]]["series"][0]["data"][k];
    				data[array[i]]["series"][0]["data"][k]=(tmpData/(1024*1024)).toFixed(2);
    			}
    		}
    		data[array[i]]["title"]["text"]=transfer[i];	
    		MyChart[array[i]].setOption(data[array[i]]);
    	}else{
    		MyChart[array[i]].setOption({
				title: {
					text: transfer[i],
					x:'center',
					y:'bottom',
					textStyle:{fontSize:14}
				},
				tooltip: {
					trigger: 'axis',
					axisPointer : {
			            type : 'line',
			            lineStyle : {
			                color: '#48b',
			                width: 2,
			                type: 'solid'
			            }
					}
				},
				legend: {
					data:['data'],
					show:false
				},
				grid:{//表格左上角间距
					y:40,
					x:100
				},
				xAxis: {
					data: [0],
					splitLine:[{
						interval:1
					}]
				},
				yAxis: {
					axisLabel:{
						
					}
				},
				series: [{
					type: 'line',
					data: [0]
				}]
			});
    	}
    }
}

/**
 * 获取图表数据
 * @returns {Object}
 */
function getChartsDataByPost(){
	var lineChartsOptions=new Object();
	getJsonDataByPost(Globals.ctx + "/api/v2/cluster/"+JsVar["CLUSTER_CODE"]+"/metrics",'',"",
			function(result){
				var time_array=new Array();
				//做0值给默认的option用
				var y_array=new Array();
				var time=result["metrics"][0].category;
				//X轴时间处理
				for(var i=0;i<time.length;i++){
					var _time=time[i].split(" ")[1];
					time_array.push(_time);
					y_array.push(0);
				}
				$.each( result["metrics"] , function( i , item ){
					// 指定图表的配置项和数据
					var option={
						title: {
							text: item.name,
							x:'center',
							y:'bottom',
							textStyle:{fontSize:14}
						},
						tooltip: {
							trigger: 'axis',
							axisPointer : {
					            type : 'line',
					            lineStyle : {
					                color: '#48b',
					                width: 2,
					                type: 'solid'
					            }
					        }
						},
						legend: {
							data:['data'],
							show:false
						},
						dataZoom:{
							show:true,
							start:0,
							end:100,
							 realtime:true
						},
						grid:{//表格左上角间距
							y:40,
							x:100
						},
						xAxis: {
							data: time_array,
							splitLine:[{
								interval:1
							}]
							
						},
						yAxis: {
							axisLabel:{
								
							}
						},
						series: [{
							type: 'line',
							data: item.data,
						}]
					};
					
					lineChartsOptions[item.name]=option;
				});
				//做默认的option
				var default_option={
					title: {
						text: 'Failed',
						x:'center',
						y:'bottom',
						textStyle:{fontSize:14}
					},
					tooltip: {
						trigger: 'axis',
						axisPointer : {
				            type : 'line',
				            lineStyle : {
				                color: '#48b',
				                width: 2,
				                type: 'solid'
				            }
				        }
					},
					legend: {
						data:['data'],
						show:false
					},
					dataZoom:{
						show:true,
						start:0,
						end:100,
						 realtime:true
					},
					grid:{//表格左上角间距
						y:40,
						x:100
					},
					xAxis: {
						data: time_array,
						splitLine:[{
							interval:1
						}]
						
					},
					yAxis: {
						axisLabel:{
							
						}
					},
					series: [{
						type: 'line',
						data: y_array,
					}]
				};
				lineChartsOptions["Default"]=default_option;
		},null,null,false);
	return lineChartsOptions;
	
}

/**
 * clusterGrid:端口使用（比例）
 */
function clusterSlotsScale(e){
	return e.row.slotsUsed+'/'+e.row.slotsTotal;
}

/**
 * topologyGrid：拓扑名称（加载a标签）
 */
function topNameRenderer(e){
	var index = e.rowIndex;
	return  '<a class="Delete_Button" href="javascript:topologySummary('+index+')">'+e.record.name+'</a>';
}

/**
 * topologyGrid:状态（高亮显示）
 */
function topStateRenderer(e){
	if(e.value=="ACTIVE"){
		return "<span class='label label-success'>"+"&nbsp;"+e.value+"&nbsp;&nbsp;</span>";
	}else{
		return "<span class='label label-danger'>"+"&nbsp;"+e.value+"&nbsp;&nbsp;</span>";
	}
}

/**
 * topologyGrid:配置(加载a标签)
 */
function topConfRenderer(e){
	return  '<a class="Delete_Button" href="javascript:topShowConfig(\''
		+e.record.id+'\')">查看</a>';
}

/**
 * topologyGrid跳转：拓扑信息
 */
function topologySummary(index){
	var rowInfo = JsVar["topologyGrid"].getRow(index);
	window.location.href = "../topology/topologyInfoMonitor?clusterName=" + JsVar["CLUSTER_CODE"]
		+ "&topologyId=" + rowInfo.id + "&topologyName=" + rowInfo.name;
}
/**
 *topologyGrid跳转：配置
 */
function topShowConfig(topologyId){
	window.location.href = Globals.ctx + "/jsp/monitormanager/clustersummary/topConfigInfo?clusterName=" + JsVar["CLUSTER_CODE"] + "&topologyId=" + topologyId;
}

/**
 * nimbus：配置（加载a标签）
 */
function nimbusConf(e){
	return  '<a class="Delete_Button" href="javascript:nimbusScanConf()">查看</a>';
}

/**
 * nimbus：日志（加载a标签）
 */
function nimbusLog(e){
	var html='<a class="Delete_Button" href="' +Globals.baseJspUrl.MONITOR_JSP_SUMMARY_CLUSTER_LOG_FILE_URL
		+'?clusterName='+JsVar["CLUSTER_CODE"]+'&host='+e.record.ip +'&name=nimbus">管理</a>&nbsp;&nbsp;'
		+'<a class="Delete_Button" href="' +Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_NIMBUS_LOG_URL
		+'?clusterName='+JsVar["CLUSTER_CODE"]+'&host='+e.record.ip +'&file=nimbus.log">查看</a>';
	return  html;
}

/**
 * nimbus：查看配置
 */
function nimbusScanConf(){
	window.location.href = Globals.ctx + "/jsp/monitormanager/clustersummary/nimConfigInfo?clusterName=" + JsVar["CLUSTER_CODE"];
}

/**
 * supervisor:主机（a标签加载）
 */
function superHostRenderer(e){
	var index = e.rowIndex;
	return  '<a class="Delete_Button" href="javascript:supervisorMonitorForward('+index+')">'+e.record.ip+'</a>';
}

//supervisor监控跳转 
function supervisorMonitorForward(index){
	var rowInfo = JsVar["supervisorGrid"].getRow(index);
	window.location.href = Globals.ctx + "/jsp/monitormanager/topology/supervisorMonitor?clusterName=" 
		+ JsVar["CLUSTER_CODE"] + "&host=" + rowInfo.host;
}

/**
 * supervisor:端口使用(比例)
 */
function superSlotsUsedRenderer(e){
	return e.row.slotsUsed+'/'+e.row.slotsTotal;
}

/**
 * supervisor:配置（a标签加载）
 */
function superConfRenderer(e){
	return  '<a class="Delete_Button" href="javascript:superScanConf(\''
	+e.record.host+'\')">查看</a>';
}

/**
 * supervisor:日志(a标签)
 */
function superLogRenderer(e){
	var html='<a class="Delete_Button" href="' +Globals.baseJspUrl.MONITOR_JSP_SUMMARY_CLUSTER_LOG_FILE_URL
		+'?clusterName='+JsVar["CLUSTER_CODE"]+'&host='+e.record.ip +'&name=supervisor">管理</a>&nbsp;&nbsp;'
		+'<a class="Delete_Button" href="' +Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_NIMBUS_LOG_URL
		+'?clusterName='+JsVar["CLUSTER_CODE"]+'&host='+e.record.ip +'&file=supervisor.log">查看</a>';
	return  html;
}

/**
 * supervisor:查看配置
 */
function superScanConf(host){
	window.location.href = Globals.ctx + "/jsp/monitormanager/clustersummary/supConfigInfo?clusterName=" + JsVar["CLUSTER_CODE"]+ "&host=" + host;
}

/**
 * zookeeper：信息列（a标签加载）
 */
function zkInfoRenderer(e){
	return  '<a class="Delete_Button" href="javascript:zkInfoScan()">查看</a>';
}

/**
 * zookeeper:合并单元格
 */
function zkOnLoad(length){
	var cells = [
	    { rowIndex: 0, columnIndex: 2, rowSpan: length, colSpan: 1 },
        { rowIndex: 0, columnIndex: 1, rowSpan: length, colSpan: 1 }
    ];
	JsVar["zookeeperGrid"].mergeCells ( cells );
}

/**
 * zookeeper:跳转
 */
function zkInfoScan(){
	window.location.href = Globals.ctx + "/jsp/monitormanager/zkmanager/zkManagerMain?clusterName=" + JsVar["CLUSTER_CODE"];
}

/**
 * 刷新当前页
 */
function resultButton() {
	return '<a class="mini-button" href="javascript:window.location.reload()" style="background-color: #59bd5d;color: #ffffff;height: 26px;line-height: 26px;"><span class="mini-button-text ">刷新</span></a>';
}
