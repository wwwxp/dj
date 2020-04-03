/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 17-2-14
 * Time: 下午16:03
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var param=new Object();
var paramData=new Object();

//部署进度说明需要的全局变量
var index=0;
var sumbitButton;

//获取Redis已使用的文件列表
var lastFileData = [];

//每台主机配置文件保存数组
var deployTypeFileArray = [];

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
	mini.parse();
	//按钮对象
	sumbitButton = mini.get("sumbitButton");
    paramData = data;
	//取得主机表格
	JsVar["configGrid"] = mini.get("configGrid");
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
    //默认启动版本列表加载
    initStartVersion();
	//加载右键
    loadingRightClick();
	//拼接html 填充主机列表
	queryHostDivForStart(param);
	//查询实例
	queryInstConfigList();
}

/**
 * 默认启动版本加载
 */
function initStartVersion() {
    var versionList = getStartVersionList(param["CLUSTER_ID"], param["CLUSTER_TYPE"]);
    mini.get("defaultDeployVersion").setData(versionList);
    if (versionList != null && versionList.length > 1) {
        mini.get("defaultDeployVersion").setValue(versionList[1]["VERSION"]);
    }
}

/**
 * 查询实例
 */
function queryInstConfigList() {
	var params = {
		CLUSTER_ID:param["CLUSTER_ID"],
		CLUSTER_TYPE:param["CLUSTER_TYPE"]
	};
	datagridLoadPage(mini.get("configGrid"), params, "instConfig.queryInstConfigInfo");
}

/**
 * 渲染操作按钮
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
	var rowIndex=e.rowIndex;
	var html = '<a class="Delete_Button" href="javascript:showConfigContent(' + rowIndex +')">配置文件</a>';
	html += '<a class="Delete_Button" href="javascript:checkHost('+rowIndex+')">状态检查</a>';
	html += '<a class="Delete_Button" href="javascript:deleteInst('+rowIndex+')">删除</a>';
	return html;
}

/**
 * 查看配置
 */
function showConfigContent(index) {
	var rowData=JsVar["configGrid"].getRow(index);
	var hostId = rowData.HOST_ID;
	var filePath = rowData.FILE_PATH;
	var instId = rowData.INST_ID;
	var fileName = rowData.FILE_NAME;
	var clusterType = rowData.CLUSTER_TYPE;
	var deployFileType = rowData.DEPLOY_FILE_TYPE;
	if((clusterType==busVar.DCA && deployFileType!=busVar.REDIS) 
			|| clusterType==busVar.MONITOR
			|| clusterType==busVar.DMDB
			|| clusterType==busVar.DCLOG
			|| clusterType==busVar.M2DB){//多个配置文件
		var params = {
			INST_ID:instId,
			HOST_ID:hostId,
			filePath:filePath
		};
		showDialog("查看配置文件",780,"80%",Globals.baseJspUrl.HOST_JSP_SHOW_MUTIL_CONFIG_CONTENT_URL,
				function destroy(data){
			}, params);
	}else{
		var params = {
			INST_ID:instId,
			HOST_ID:hostId,
			filePath:filePath,
			fileName:fileName
		};
		showDialog("查看配置文件",780,"80%",Globals.baseJspUrl.HOST_JSP_SHOW_CONFIG_CONTENT_URL,
				function destroy(data){
			}, params);
	}
}

/**
 * 检查
 */
function checkHost(index){
	var rowData=JsVar["configGrid"].getRow(index);
	if(rowData==null || rowData==undefined){
        showWarnMessageTips("请选择一条记录");
		return;
	}
	var state=rowData.STATUS;
	
	var updateParams=new Object();
	updateParams["INST_ID"]=rowData.INST_ID;
	updateParams["HOST_ID"]=rowData.HOST_ID;
	updateParams["DEPLOY_TYPE"]=rowData.DEPLOY_TYPE;
	updateParams["CLUSTER_TYPE"]=rowData.CLUSTER_TYPE;
	
	updateParams["DEPLOY_FILE_TYPE"] = rowData.DEPLOY_FILE_TYPE;
	//updateParams["CLUSTER_CODE"]=rowData.DEPLOY_TYPE;
	updateParams["SOFT_LINK_PATH"]=rowData.SOFT_LINK_PATH;
	updateParams["VERSION"]=rowData.VERSION;
	
	//1.远程主机检查,返回真实状态	
	getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_CHECK_PROCESS_STATE_URL,rowData,"instconfig--状态检查",
		function(result){
		var processInfo=(result.processPort==undefined?"":result.processPort);
		//2.真实状态与数据库的做对比
		if(state==1){//数据库:运行
			if(result.processState==1){//远程主机：运行
				if(rowData.DEPLOY_FILE_TYPE == "m2db"){
                    showMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，实例名为【"+processInfo+"】");
				}else{
                    showMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，进程号为【"+processInfo+"】");
				}
				
			}else{//远程主机：未运行
				//3.状态不正确，提示更新数据库
				updateParams["STATUS"]='0';
				showConfirmMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序未运行，"+processInfo+"是否同步数据库?", function ok(){
					 synchronizeDatabase(updateParams);
				});
			}
		}else{//数据库:未运行
			if(result.processState==1){//远程主机：运行
				//3.状态不正确，提示更新数据库
				updateParams["STATUS"]='1';
				showConfirmMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，进程号为【"+processInfo+"】，是否同步数据库?", function ok(){
					 synchronizeDatabase(updateParams);
				});
			}else{//远程主机：未运行
                showWarnMessageTips(rowData.DEPLOY_FILE_TYPE+"程序未运行!"+processInfo+"");
			}
		}
	});
}

/**
 * 检查状态，数据库同步方法
 */
function synchronizeDatabase(params){
	getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_UPDATE_PROCESS_STATE_URL,params,"instconfig--状态检查--同步数据库状态",
			function(result){
		if(result!="error"){
            showMessageTips("同步数据库成功！");
			//刷新
			queryInstConfigList();
		}else{
			showErrorMessageAlter("同步数据库失败:"+result);
		}
	});
}

/**
 * 删除已停止实例
 * @param index
 */
function deleteInst(index){
	var rowData=JsVar["configGrid"].getRow(index);
	if(rowData==null || rowData==undefined){
        showWarnMessageTips("请选择一条记录");
		return;
	}
	var RUN_STATE=rowData.STATUS;
	if(RUN_STATE == 1){
        showWarnMessageTips("该程序正在运行中，不可删除！");
		return;
	}
	
	var msg="确定删除记录？";
	if(rowData.DEPLOY_TYPE==busVar.DMDB){
		msg="确定删除记录及对应远程主机上的目录？";
	}
	showConfirmMessageAlter(msg,function ok(){
		getJsonDataByPost(Globals.baseActionUrl.INST_CONFIG_TASK_ACTION_DELETE_INFOMATION_URL,rowData,"instconfig--删除已停止的实例信息",
				function(result){
			if(result.Success!=undefined){
                showMessageTips("删除成功！");
			}else if(result.error!=undefined){
				showErrorMessageAlter("删除失败！请检查！");
			}
			
			queryInstConfigList();
		});
	});
}

/**
 * 状态渲染
 * @param e
 * @returns {String}
 */
function statusRenderer(e) {
	var RUN_STATE=e.record.STATUS;
    var html= "";
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
    	return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;停止&nbsp;</span>";
    }else if(RUN_STATE == 1){//已启用， 运行中
    	return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;运行&nbsp;</span>";
    }
}

/**
 * 点击后可能出现滚动条需要重新渲染页面
 */
function clickPanelBtn() {
	setTimeout(function(){
		mini.parse();
	}, 500);
}

/**
 * 勾选实例
 * @param e
 */
function selectGridRow() {
	
	var selRows = JsVar["configGrid"].getSelecteds();
	
	//判断当前行是否勾选中，如果没有勾选中则当前行删除
	$("#paramsInfo:eq(0)>tr").each(function(index, item){
		var isExists = false;
		var instId = $(item).data("INST_ID");
		for (var i=0; i<selRows.length; i++) {
			if (selRows[i]["INST_ID"] == instId) {
				isExists = true;
				break;
			}
		}
		if (instId && !isExists) {
			$(item).remove();
		}
	});
	
	//判断勾选中的行是否有创建，如果没有创建则创建勾选行
	if (selRows != null) {
		for (var i=0; i<selRows.length; i++) {
			var selRow = selRows[i];
			//判断选中实例是否已经创建
			var isCreate= false;
			$("#paramsInfo:eq(0)>tr").each(function(index, item){
				var instId = $(item).data("INST_ID");
				var hostId = $(item).data("HOST_ID");
				if (selRow["HOST_ID"] == hostId) {
					isCreate = true;
					return false;
				}
			});
			if (!isCreate && selRow["STATUS"] != busVar.STATE_ACTIVE) {
				addConfigRow(null, selRow["HOST_ID"], selRow["HOST_IP"], selRow["SSH_USER"], true, selRow, selRow["INST_ID"]);
			}
		}
	}
}

/**
 * 添加一行记录
 * @param currentRow 当前行对象
 * @param hostId 主机ID
 * @param hostIp 主机IP
 * @param isFull 是否需要填充值
 * @param fullObj 填充值对象
 * @param instId 添加行来源
 * @returns
 */
function addConfigRow(currentRow, hostId, hostIp, sshUser, isFull, fullObj, instId) {
	var timeSeq = (new Date()).getTime();
	var str='<tr style="padding-top:10px;" time="' + timeSeq + '" id="tr_'+hostIp+'" data-HOST_IP="'+hostIp+'" data-HOST_ID="'+hostId+'" data-INST_ID="'+instId+'">'
			+'<th><span>主机：</span></th>'
			+'<td class="host_ips" id="host_ips">'
			+'	<input rowType="ip" class="mini-textbox" allowInput="false" data-HOST_IP="'+hostIp+'"'
			+'		name="ip_'+timeSeq+'" id="ip_'+timeSeq+'" value="'+hostIp + '(' + sshUser + ')'+'" style="width:100%;">'
			+'</td>'
			
			+'<th><span class="fred">*</span>启动模式：</th>'
			+'<td><input class="mini-combobox" id="deployType_'+timeSeq+'" name="deployType_'+timeSeq+'" popupWidth="100%" '
			+'	data="getSysDictData(\'jstorm_deploy_type\')" textField="text" valueField="code" required="true" '
			+'	showNullItem="false" style="width:100%;"/>'
			+'</td>'
			
			+'<th style="text-align:center;"><input class="mini-checkbox" tooltip="勾选表示要清除对应主机Data目录数据" id="clearData_'+timeSeq+'" name="clearData_'+timeSeq+'" /></th>'
			
			+'<th><span class="fred">*</span>启动版本：</th>'
			+'<td><input class="mini-combobox" id="version_'+timeSeq+'" popupWidth="100%" '
			+'	valueField="VERSION" textField="VERSION" allowInput="false"  data="getStartVersionData(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\')" '
			+'	name="version_'+timeSeq+'" style="width:100%;"/>'
			+'</td>'

			+'<td>'
			+'	<div style="height:100%;line-height:30px;">'
			+'		<div style="text-align:center;width:100%;margin:2px 0px;cursor:pointer;" title="最近一次操作日志" '
			+'			id="log_' + timeSeq + '" name="log_'+timeSeq+'" href="javascript:void(0)" '
			+'			onclick="showStartOperationLog(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\', \'storm.yaml\', \'storm.yaml\', \''+timeSeq+'\')"><img src="../../..//images/comlog24.png" /></div>'
			+'	</div>'
			+'</td>'

			+'<td> '
			+'	<input class="mini-hidden" name="hostId_'+timeSeq+'" id="hostId_'+timeSeq+'" value="'+hostId+'"/>'
			+'</td> '
			+'</tr>';
	$("#paramsInfo").append(str);
	mini.parse();
	if (isFull) {
		mini.get("deployType_" + timeSeq).setValue(fullObj["DEPLOY_FILE_TYPE"]);
		mini.get("version_" + timeSeq).setValue(fullObj["VERSION"]);
		//判断主机是否被选中，如果没选中在选中主机
		$("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
			var trId = $(item).attr("id");
			if (trId.indexOf(hostIp) != -1) {
				$("#hostFitDiv input[id='"+hostId+"']").attr("checked", true);
				return false;
			}
		});
	}
}

/**
 * 保存当前配置
 */
function addOperator() {
	var forms = new mini.Form("#paramsTable");
	if (forms.isValid() == false){
		return;
	}
	
	//获取所有的选中主机配置数据航
	var rowData = [];
	$("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
		var time = $(item).attr("time");
		var hostIp = $(this).data("HOST_IP");
		var deployType = mini.get("deployType_"+time).getValue();
		var version = mini.get("version_"+time).getValue();
		var hostId = mini.get("hostId_"+time).getValue();
		var checkClear = mini.get("clearData_"+time).getChecked();
		var singleParams = {
			HOST_IP:hostIp,
			DEPLOY_FILE_TYPE:deployType,
			VERSION:version,
			HOST_ID:hostId,
			DATA_CLEAR:checkClear ? "1" : "0",
			CLUSTER_TYPE:param["CLUSTER_TYPE"],
			CLUSTER_ID:param["CLUSTER_ID"],
			CLUSTER_CODE:param["CLUSTER_CODE"],
			CLUSTER_NAME:param["CLUSTER_NAME"]
		};
		rowData.push(singleParams);
	});
	
	//被选中主机的个数
	if (rowData.length == 0) {
        showWarnMessageTips("请选择需要启停主机！");
		return;
	}
	
	//参数校验
	for (var i=0; i<rowData.length; i++) {
		if (rowData[i]["DEPLOY_TYPE"] == "" || rowData[i]["VERSION"] == "") {
            showWarnMessageTips("启动模式、版本均不能为空，请选择！");
			return;
		}
	}
		
	//配置数据
	var params = {
		CLUSTER_ID:param["CLUSTER_ID"],
		CLUSTER_TYPE:param["CLUSTER_TYPE"],
		CLUSTER_CODE:param["CLUSTER_CODE"],
		CLUSTER_NAME:param["CLUSTER_NAME"],
		HOST_LIST:rowData
	};
	
	mini.prompt("<span class='fred'>*</span>批次号:", "启动配置项保存", function(action, value) {
		if (action == "ok") {
			if (value == null || value.trim() == "") {
                showWarnMessageTips("批次名称不能为空， 请输入批次名称！");
			} else if (value.length > 15){
                showWarnMessageTips("批次名称过程， 批次名称长度不得超过15个字符！");
			} else {
				params["BATCH_NAME"] = value;
				//保存数据
				getJsonDataByPost(Globals.baseActionUrl.HOST_START_ACTION_INIT_CONFIG_URL, params, "组件启动-保存配置项数据",
						function success(result){
						if (result != null && result.RST_CODE == "1") {
                            showMessageTips(result.RST_MSG);
						} else {
							showErrorMessageAlter(result.RST_MSG);
						}
				},null,null,false);
			}
		} 
	});
}

/**
 * 重载上次数据
 */
function reloadConfig() {
	showDialog("载入批次","80%","90%",Globals.baseJspUrl.INST_CONFIG_JSP_START_ONCE_URL,
		function destroy(data){
			if (data != null && data["BATCH_NAME"] != null) {
				var batchName = data["BATCH_NAME"];
				
				//根据批次名称查询配置文件信息
				var params = {
					CLUSTER_ID:param["CLUSTER_ID"],
					BATCH_NAME:batchName
				};
				getJsonDataByPost(Globals.baseActionUrl.HOST_START_ACTION_LOAD_CONFIG_URL, params, "",
					function success(result){
						if (result != null && result != '') {
							//清空所有节点数据
							selectNone();
							
							//加载重载数据
							for (var i=0; i<result.length; i++) {
								var hostId = result[i]["HOST_ID"];
								var hostIp = result[i]["HOST_IP"];
								var sshUser = result[i]["SSH_USER"];
								var hostCheck = $("#hostFitDiv input[id='"+hostIp+"']").attr("checked");
								if (hostCheck == false || hostCheck == undefined) {
									$("#hostFitDiv input[id='"+hostIp+"']").attr("checked", true);
								}
								addConfigRow(null, hostId, hostIp, sshUser, true, result[i]);
							}
						} else {
                            showWarnMessageTips("没有保存配置数据！");
						}
				},null, null, false);
			}
	 }, param, {allowDrag:false});
}

/**
 * 单台主机启停，不刷新所有表格数据
 */
function singleChange(obj){
	//获取主机ID
	var hostId = $(obj).val();
	//获取主机IP
	var hostIp = $(obj).data("HOST_IP");
	//获取主机用户
	var sshUser = $(obj).data("SSH_USER");
	//获取主机是否选中
	var checked = $(obj).attr("checked");
	var str = "";
	if (checked || checked == "checked") {
		var timeSeq = (new Date()).getTime();
		str+='<tr style="padding-top:10px;" time="' + timeSeq + '" id="tr_'+hostIp+'" data-HOST_IP="'+hostIp+'" data-HOST_ID="'+hostId+'">'
			+'<th><span>主机：</span></th>'
			+'<td class="host_ips" id="host_ips">'
			+'	<input rowType="ip" class="mini-textbox" allowInput="false" data-HOST_IP="'+hostIp+'"'
			+'		name="ip_'+timeSeq+'" id="ip_'+timeSeq+'" value="'+hostIp + '(' + sshUser + ')'+'" style="width:100%;">'
			+'</td>'
			
			+'<th><span class="fred">*</span>启动模式：</th>'
			+'<td><input class="mini-combobox" id="deployType_'+timeSeq+'" name="deployType_'+timeSeq+'" popupWidth="100%" '
			//+'	onvaluechanged="changeDeploy(\'deployType_'+timeSeq+'\', '+timeSeq+')" '
			+'	data="getSysDictData(\'jstorm_deploy_type\')" textField="text" valueField="code" required="true" '
			+'	showNullItem="false" style="width:100%;"/>'
			+'</td>'

			+'<th style="text-align:center;"><input class="mini-checkbox" tooltip="勾选表示要清除对应主机Data目录数据" id="clearData_'+timeSeq+'" name="clearData_'+timeSeq+'" /></th>'

			+'<th><span class="fred">*</span>启动版本：</th>'
			+'<td><input class="mini-combobox" id="version_'+timeSeq+'" popupWidth="100%" '
			+'	valueField="VERSION" textField="VERSION" allowInput="false"  data="getStartVersionData(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\')" '
			+'	name="version_'+timeSeq+'" style="width:100%;"/>'
			+'</td>'

            +'<td>'
            +'	<div style="height:100%;line-height:30px;">'
            +'		<div style="text-align:center;width:100%;margin:2px 0px;cursor:pointer;" title="最近一次操作日志" '
            +'			id="log_' + timeSeq + '" name="log_'+timeSeq+'" href="javascript:void(0)" '
            +'			onclick="showStartOperationLog(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\', \'storm.yaml\', \'storm.yaml\', \''+timeSeq+'\')"><img src="../../..//images/comlog24.png" /></div>'
            +'	</div>'
            +'</td>'

			+'<td> '
			+'	<input class="mini-hidden" name="hostId_'+timeSeq+'" id="hostId_'+timeSeq+'" value="'+hostId+'"/>'
			+'</td> '
			+'</tr>';
		$("#paramsInfo").append(str);
		mini.parse();
		
		//设置版本初始化,默认选中最新版本启动
		var versionData = mini.get("version_" + timeSeq).getData();
		if (versionData != null && versionData.length > 0) {
			var lastVersion = versionData[0]["VERSION"];
			mini.get("version_" + timeSeq).setValue(lastVersion);

            //设置版本为当前共用选择版本
            var deployVersion = mini.get("defaultDeployVersion").getValue();
            if (deployVersion != null && deployVersion != '' && typeof(deployVersion) != 'undefined') {
                mini.get("version_" + timeSeq).setValue(deployVersion);
            }
		}
	} else {
		//主机不选中删除对应的数据行
		$("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
			var trId = $(item).attr("id");
			if (trId == "tr_" + hostIp) {
				$(item).remove();
				return false;
			}
		});
	}
}

/**
 * 点击提交
 */
var textValue="";
var startTimes = 0;
function onSubmit(){
	$("#deployTextarea").html("");
	var params = new Object();
	if (new mini.Form("#paramsTable").isValid() == false){
		return;
	}
	
	//获取所有的选中主机配置数据航
	var params = [];
	$("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
		var time = $(item).attr("time");
		var ip = mini.get("ip_"+time).getValue();
		var deployType = mini.get("deployType_"+time).getValue();
		var version = mini.get("version_"+time).getValue();
		var host = mini.get("hostId_"+time).getValue();
		var checkClear = mini.get("clearData_"+time).getChecked();
		var singleParams = {
			HOST_IP:ip,
			DEPLOY_TYPE:deployType,
			VERSION:version,
			HOST_ID:host,
			DATA_CLEAR:checkClear ? "1" : "0",
			CLUSTER_TYPE:param["CLUSTER_TYPE"],
			CLUSTER_ID:param["CLUSTER_ID"],
			CLUSTER_CODE:param["CLUSTER_CODE"],
			CLUSTER_NAME:param["CLUSTER_NAME"]
		};
		params.push(singleParams);
	});
	
	//被选中主机的个数
	var checkedHostLength = $("input[name='ck_host']:checked").length;
	if (params.length == 0 || checkedHostLength == 0) {
        showWarnMessageTips("请选择需要启停主机！");
		return;
	}
	
	//参数校验
	for (var i=0; i<params.length; i++) {
		if (params[i]["DEPLOY_TYPE"] == ""|| params[i]["VERSION"] == "") {
            showWarnMessageTips("启动模式、版本均不能为空，请选择！");
			return;
		}
	}
	
	textValue='';
	sumbitButton.setText("正在启动");
	sumbitButton.setEnabled(false);
	startTimes = (new Date()).getTime();
	textValue += "启动开始时间: " + ((new Date(startTimes)).format("yyyy-MM-dd hh:mm:ss")) + "<br/>";
	postAjaxForDeploy(params);
}

/**
 * 启动JSTORM
 * @param params
 */
function postAjaxForDeploy(params){
	
	textValue +="主机正在启动...<br/>";
	$("#deployTextarea").html(textValue);
	
	//将滚动条自动滚动到最下面
	var deployDiv = document.getElementById("mainDiv");
	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	
	getJsonDataByPost(Globals.baseActionUrl.JSTORM_TASK_ACTION_DEPLOY_INFOMATION_URL,params,"启停管理--启动JSTORM",
			function(result){
				if(result && result["RST_STR"] != ""){
					textValue+=result["RST_STR"];
					textValue=textValue.replaceAll("\n","<br/>");
					var endTimes = (new Date()).getTime();
		        	textValue += "运行结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss")) + ", 本次运行"+params.length+"个实例, 总耗时: " + ((endTimes - startTimes)/1000).toFixed(2) + "秒";
		        	$("#deployTextarea").html(textValue);
		        	
		        	//将滚动条自动滚动到最下面
		        	var deployDiv = document.getElementById("mainDiv");
		        	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
		        	
					sumbitButton.setText("启动");
					sumbitButton.setEnabled(true);
				 
					var rstCode = result["RST_CODE"];
					if (rstCode == busVar.FAILED) {
						showErrorMessageAlter("启动失败");
		        	} else {
                        showMessageTips("启动成功");
		        		//当关闭窗口时,用于判断是否重新加载 
						param["submit_state"] =1;
		        	}
					queryInstConfigList();
					
					//高亮检索“失败”“成功”关键字
					heightLightKeyWord();
				}
				mini.parse();
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
