/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-8-4
 * Time: 下午19:22
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var param=new Object();
var paramData=new Object();

//checkbox选中的个数
var checkedLength=0;

//部署进度说明需要的全局变量
var index=0;
var sumbitButton;


/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
	mini.parse();
	JsVar["configGrid"] = mini.get("deploy_datagrid");//取得主机表格
	sumbitButton = mini.get("sumbitButton");
    paramData = data;
	//集群编码
	param["CLUSTER_CODE"] = data["CLUSTER_CODE"];
	//集群ID
	param["CLUSTER_ID"] = data["CLUSTER_ID"];
	//集群名称
	param["CLUSTER_NAME"] = data["CLUSTER_NAME"];
	//集群类型
	param["CLUSTER_TYPE"] = data["CLUSTER_TYPE"];
	//鼠标右键单台主机启停参数
	param["HOST_ID"] = data["HOST_ID"];
	//加载右键
    loadingRightClick();
	//拼接html 填充主机列表
	queryHostDivForStop(param);
}

/**
 * 查询所有的正在运行信息
 */
function checkedchanged() {
	var hostArray = [];
	var obj=document.getElementsByName('ck_host');
	for(var i=0; i<obj.length; i++){
		if(obj[i].checked){ 
			//获取到选中主机ID
			hostArray.push("'" + obj[i].value + "'");
		}
	}
	if (hostArray != null && hostArray.length > 0) {
		var params = {
			HOST_ID:hostArray.join(","),
			DEPLOY_TYPE: param["CLUSTER_TYPE"],
			CLUSTER_ID:param["CLUSTER_ID"],
			STATUS: busVar.STATE_ACTIVE
		};
		datagridLoad(mini.get("deploy_datagrid"), params, "instConfig.queryInstConfigByHost");
	} else {
		mini.get("deploy_datagrid").clearRows();
	}
}

/**
 * 合并数据
 * @param e
 */
function loadStopData(e) {
	var gridData = mini.get("deploy_datagrid").getData();
	var mergeCells2="HOST_IP,DEPLOY_FILE_TYPE,INST_NAME";
 	var mergeCellColumnIndex2="1,2,3";
	var mergeData = getMergeCellsOnGroup(gridData, mergeCells2, mergeCellColumnIndex2);
	mini.get("deploy_datagrid").mergeCells(mergeData);
}

/**
 * 渲染
 * @returns {String}
 */
function onActionRender(e) {
	var rowIndex=e.rowIndex;
    var clusterType = e.record.DEPLOY_TYPE;
    var instId = e.record.INST_ID;
    var clusterId = e.record.CLUSTER_ID;
	var actionHtml = '<a class="Delete_Button" href="javascript:showConfigContent(' + rowIndex +')">查看配置文件</a>';
    actionHtml += '<a class="Delete_Button" href="javascript:showStopOperationLog(\'' + clusterId + '\', \'' + clusterType +'\', \'' + instId + '\')">操作日志</a>';
	return actionHtml;

}

/**
 * 查看文件内容
 */
function showConfigContent(index) {
	var rowData=JsVar["configGrid"].getRow(index);
	var hostId = rowData.HOST_ID;
	var filePath = rowData.FILE_PATH;
	var instId = rowData.INST_ID;
	var fileName = rowData.FILE_NAME;
	var deployType=rowData.DEPLOY_TYPE;
	var deployFileType=rowData.DEPLOY_FILE_TYPE;
	
	var params = {
			INST_ID:instId,
			HOST_ID:hostId,
			filePath:filePath
		};
	showDialog("查看配置文件",780,"80%",Globals.baseJspUrl.HOST_JSP_SHOW_MUTIL_CONFIG_CONTENT_URL,
			function destroy(data){
		}, params);
}

/**
 * 渲染停止
 * @param e
 */
function onStatusRender(e) {
    var status = e.record.STATUS;
	if(status == "1" ){
		return "<span class='label label-success'>&nbsp;运行中&nbsp;</span>";
	} else {
		return "<span class='label label-danger'>&nbsp;未运行&nbsp;</span>";
	}
}

/**
 * 停止对应的部署程序
 * @param hostId
 * @param deployType
 */
var textValue="";
var startTimes = 0;
function onSubmit() {
	var stopArray = mini.get("deploy_datagrid").getSelecteds();
	if (stopArray == null || stopArray.length == 0) {
        showWarnMessageTips("请选择需要停止的实例！");
		return;
	}
	var hostArray = [];
	for (var i=0; i<stopArray.length; i++) {
		hostArray.push({
			HOST_ID:stopArray[i].HOST_ID,
			HOST_IP:stopArray[i].HOST_IP,
			INST_ID:stopArray[i].INST_ID,
			DEPLOY_FILE_TYPE:stopArray[i].DEPLOY_FILE_TYPE,
			FILE_PATH:stopArray[i].FILE_PATH,
			FILE_CONTENT:stopArray[i].FILE_CONTENT,
			INST_PATH:stopArray[i].INST_PATH,
			VERSION:stopArray[i].VERSION,
			CLUSTER_ID: param["CLUSTER_ID"],
			CLUSTER_CODE:param["CLUSTER_CODE"],
			CLUSTER_TYPE:param["CLUSTER_TYPE"],
			CLUSTER_NAME:param["CLUSTER_NAME"]
		});
	}
	textValue='';
	sumbitButton.setText("正在停止");
	sumbitButton.setEnabled(false);
	startTimes = (new Date()).getTime();
	textValue += "停止开始时间: " + ((new Date(startTimes)).format("yyyy-MM-dd hh:mm:ss")) + "<br/>";

    showConfirmMessageAlter("确定停止选中进程？",function ok() {
        postAjaxForDeploy(hostArray);
    });
}

function postAjaxForDeploy(host_arry){
	textValue +="正在停止主机运行...<br/>";
	$("#deployTextarea").html(textValue);
	
	//将滚动条自动滚动到最下面
	var deployDiv = document.getElementById("mainDiv");
	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	
	getJsonDataByPost(Globals.baseActionUrl.M2DB_TASK_ACTION_STOP_DEPLOY_INFOMATION_URL, host_arry, "启停管理--停止M2DB",
		function(result){
			if(result && result["RST_STR"] != ""){
				textValue+=result["RST_STR"];
				textValue=textValue.replaceAll("\n","<br/>");
				var endTimes = (new Date()).getTime();
	        	textValue += "停止结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss")) + ", 本次停止"+host_arry.length+"个实例, 总耗时: " + ((endTimes - startTimes)/1000).toFixed(2) + "秒";
	        	$("#deployTextarea").html(textValue);
				
	        	//将滚动条自动滚动到最下面
	        	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	        	
	        	queryHostDivForStop(param);
	        	mini.get("deploy_datagrid").clearSelect( true );
				sumbitButton.setText("停止");
				sumbitButton.setEnabled(true);
				
				var rstCode = result["RST_CODE"];
				if (rstCode == busVar.FAILED) {
	        		showErrorMessageAlter("停止失败");
	        	} else {
                    showMessageTips("停止成功");
	        		//当关闭窗口时,用于判断是否重新加载 
					param["submit_state"] =1;
	        	}
				//高亮检索“失败”“成功”关键字
				heightLightKeyWord();
			}
	});
}
/**
 * 高亮检索关键字
 */
function heightLightKeyWord(){
	$("#deployTextarea").textSearch("success,successful",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#429C39;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
	$("#deployTextarea").textSearch("failed,error",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#CF5130;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
}

/**
 * 组件状态检查
 */
function showStatus() {
	showClusterStatus(param);
}

/**
 * 根据state判断是否部署过主机,如果部署过,关闭窗口后要刷新
 */
function close(){
	closeWindow(paramData);
}

/**
 * 选择所有主机
 */
function selectAll(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!array[i].disabled){
			array[i].checked = true;
		}
	}
	checkedchanged();
}

/**
 * 不选择主机
 */
function selectNone(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!array[i].disabled){
			array[i].checked = false;
		}
	}
	checkedchanged();
}

/**
 * 点击事件:查看部署（运行/停止）详情
 */
function processScanEvent(){
	var html_paragraph=$("#deployTextarea").html();
	if(html_paragraph==""){
        showWarnMessageTips("当前不存在停止进度详情，请停止后再进行此操作！");
		return;
	}
	showDialog("查看部署日志",700,450,Globals.baseJspUrl.HOST_JSP_PROCESS_SCAN_LOG_CONTENT_URL,
			function destroy(data){
			
		}, {"textValue":html_paragraph});
}