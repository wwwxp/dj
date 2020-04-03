/**
 * Created with IntelliJ IDEA.
 * Creater: yuanhao
 * Description: 集群启停共用函数
 * Date: 17-2-14
 * Time: 下午16:03
 * To change this template use File | Settings | File Templates.
 */

/**
 * 查询未部署的主机列表
 */
function queryHostDivForStart(param){	
	//每次执行本方法之前,都将div中的内容清除,使代码重用性高
	$("#hostFitDiv").html("");
	//查询该类型未部署的所有主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, param, "启停管理-获取部署的主机列表",
	    function(result){
		    var str="";
			$.each(result, function (i, item) {

				var tips = "主机名称：" + item.HOST_NAME
						+ "\n主机信息：" + item.HOST_IP + "(" + item.SSH_USER + ")"
						+ "\n实例状态：" + (item.RUN_STATE == "1" ? "运行中" : "未运行")
						+ "\n部署版本：" + item.VERSION;
	        	
	        	//如果运行状态为运行，则不可选
	        	if(item.RUN_STATE==1){
	        		str+='<ul class="ul_host_common" id="'+(new Date()).getTime()+'" title="'+tips+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:#5cb85c;">'
	        		/*+'	<li style="height:60%;" title="' + tips + '">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_run_' + item.RUN_STATE + '.gif" class="ul_host_img"/>'
	        		+'	</li>'*/
	        		+'	<li style="height:20%;margin-top:6px;">';
	        		str+='		<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" data-SSH_USER="'+item.SSH_USER+'" name="ck_host" checked=checked onClick="singleChange(this)"'
	        			+'			style="font-size:12px;" value="'+item.HOST_ID+'" /> '
	        			+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
	        			+'			for="'+item.HOST_ID+'" >' + item.HOST_IP + "(" + item.SSH_USER + ")</label>";
	        	}else if(item.RUN_STATE==0){//运行状态为未运行，则默认选中
	        		str+='<ul class="ul_host_common" id="'+(new Date()).getTime()+'" title="'+tips+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:gray;">'
	        		/*+'	<li style="height:60%;" title="' + tips + '">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_run_' + item.RUN_STATE + '.gif" class="ul_host_img"/>'
	        		+'	</li>'*/
	        		+'	<li style="height:20%;margin-top:6px;">';
	        		str+='		<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" data-SSH_USER="'+item.SSH_USER+'" name="ck_host" checked=checked onClick="singleChange(this)"'
	        			+'			style="font-size:12px;" value="'+item.HOST_ID+'" />'
	        			+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
	        			+'			for="'+item.HOST_ID+'" >' + item.HOST_IP + "(" + item.SSH_USER + ")</label>";
	        	}
	        	str+='	</li></ul>';
	        });
			$("#hostFitDiv").append(str);
			//主机上面添加右键功能
			$(".ul_host_common").hover(function() {
				var id = $(this).attr("id");
				var hostId = $(this).data("HOST_ID");
				//给类加上菜单
				var array = new Array();
				array.push({header: '右击菜单'});
				array.push({text: '主机详情', action: function(e){
					e.preventDefault();
					scanHostInfo(hostId);
				}});
				array.push({text: '终端操作', action: function(e){
					e.preventDefault();
					operatorTerminal(hostId);
				}});
				context.attach("#"+id, array);
			});
			//表单联动：根据选中的信息，关联展示表单信息,默认选中所有主机
			selectAll();
	 },"hostStart.queryStartHostList","",false);
}

/**
 * 查询未部署的主机列表
 */
function queryHostDivForStop(param){	
	//每次执行本方法之前,都将div中的内容清除,使代码重用性高
	$("#hostFitDiv").html("");
	//查询该类型未部署的所有主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, param, "启停管理-获取部署的主机列表",
	    function(result){
		    var str="";
			$.each(result, function (i, item) {
                var tips = "主机名称：" + item.HOST_NAME
                    + "\n主机信息：" + item.HOST_IP + "(" + item.SSH_USER + ")"
                    + "\n实例状态：" + (item.RUN_STATE == "1" ? "运行中" : "未运行")
                    + "\n部署版本：" + item.VERSION;

	        	//如果运行状态为运行，则不可选
	        	if(item.RUN_STATE==1){
	        		str+='<ul class="ul_host_common" id="'+(new Date()).getTime()+'" title="'+tips+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:#5cb85c;">'
	        		/*+'	<li style="height:60%;" title="' + tips + '">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_run_' + item.RUN_STATE + '.gif" class="ul_host_img"/>'
	        		+'	</li>'*/
	        		+'	<li style="height:20%;margin-top:6px;">';
	        		str+='		<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" data-SSH_USER="'+item.SSH_USER+'" name="ck_host" checked=checked onClick="checkedchanged()"'
	        			+'			style="font-size:12px;" value="'+item.HOST_ID+'" /> '
	        			+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
	        			+'			for="'+item.HOST_ID+'" >' + item.HOST_IP + "(" + item.SSH_USER + ")</label>";
	        	}else if(item.RUN_STATE==0){//运行状态为未运行，则默认选中
	        		str+='<ul class="ul_host_common" id="'+(new Date()).getTime()+'" title="'+tips+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:gray;">'
	        		/*+'	<li style="height:60%;" title="' + tips + '">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_run_' + item.RUN_STATE + '.gif" class="ul_host_img"/>'
	        		+'	</li>'*/
	        		+'	<li style="height:20%;margin-top:6px;">';
	        		str+='		<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" data-SSH_USER="'+item.SSH_USER+'" name="ck_host" onClick="checkedchanged()"'
	        			+'			style="font-size:12px;" value="'+item.HOST_ID+'" />'
	        			+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
	        			+'			for="'+item.HOST_ID+'" >' + item.HOST_IP + "(" + item.SSH_USER + ")</label>";
	        	}
	        	str+='	</li></ul>';
	        });
			$("#hostFitDiv").append(str);
			//主机上面添加右键功能
			$(".ul_host_common").hover(function() {
				var id = $(this).attr("id");
				var hostId = $(this).data("HOST_ID");
				//给类加上菜单
				var array = new Array();
				array.push({header: '右击菜单'});
				array.push({text: '主机详情', action: function(e){
					e.preventDefault();
					scanHostInfo(hostId);
				}});
				array.push({text: '终端操作', action: function(e){
					e.preventDefault();
					operatorTerminal(hostId);
				}});
				context.attach("#"+id, array);
			});
			//默认选中主机关联实例
			checkedchanged();
	 },"hostStart.queryStartHostList","",false);
}

/**
 * 加载右键菜单 
 */
function loadingRightClick(){
	context.init({preventDoubleContext: false});
	context.settings({compress: true});
}

/**
 * 查看分类中的主机详情
 */
function scanHostInfo(hostId){
	var params = {
		HOST_ID:hostId
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "主机管理-查询详细信息",
			   function (result){
			var hostParam = result[0];
			showDialog("详细信息",600,400,Globals.baseJspUrl.HOST_JSP_DETAIL_URL,
					function destroy(data){
				
			 }, hostParam);
	   },"host.queryHostList");
}

/**
 * 终端操作
 */
function operatorTerminal(hostId) {
	//获取主机ID
	var hostStr = "'" + hostId + "'";
	$("#termialHost").val(hostStr);
	$("#termialForm").attr("action", Globals.baseActionUrl.HOST_ACTION_TERMINAL_URL);
	$("#termialForm").submit();
}

/**
 * 查看配置文件
 * @param param
 * @param fullPath
 * @param hostId
 * @param fileName
 */
function showConfig(clusterId, clusterType, hostId, instPath,  fileName) {
	//获取主机部署根目录
	var params = {
		CLUSTER_ID:clusterId,
		CLUSTER_TYPE:clusterType
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "启停管理-获取主机部署根目录",
			function success(result){
			if (result != null && result.length > 0) {
				var deployRootPath = result[0]["CLUSTER_DEPLOY_PATH"];
                if (!isNull(deployRootPath) && !deployRootPath.endWith("/")) {
                    deployRootPath  = deployRootPath + "/";
                }

                var toolsDir = isNull(getPropListByKey(cfgVar.tools_dir)) ? "" : getPropListByKey(cfgVar.tools_dir)[cfgVar.tools_dir];
                if (!isNull(toolsDir) && !toolsDir.endWith("/")) {
                    toolsDir = toolsDir + "/";
                }

                var confDir = isNull(getPropListByKey(cfgVar.conf_dir)) ? "" : getPropListByKey(cfgVar.conf_dir)[cfgVar.conf_dir];
                if (!isNull(confDir) && !confDir.endWith("/")) {
                    confDir = confDir + "/";
                }

				var fullPath = deployRootPath + toolsDir + confDir + clusterType + "/" + instPath;
				var params = {
					HOST_ID:hostId,
					filePath:fullPath,
					fileName:fileName
				};
				showDialog("查看配置文件",780, "80%", Globals.baseJspUrl.HOST_JSP_SHOW_CONFIG_CONTENT_URL,
					function destroy(data){
					
				}, params);
			}
	},"serviceType.queryClusterById");
}

/**
 * 显示多个文件
 * 
 * @param param
 * @param hostId
 * @param instPath
 */
function showMutilConfig(clusterId, clusterType, hostId, instPath) {
	//获取主机部署根目录
	var params = {
		CLUSTER_ID:clusterId,
		CLUSTER_TYPE:clusterType
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "启停管理-获取主机部署根目录",
			function success(result){
			if (result != null && result.length > 0) {
				var deployRootPath = result[0]["CLUSTER_DEPLOY_PATH"];
				if (!isNull(deployRootPath) && !deployRootPath.endWith("/")) {
                    deployRootPath  = deployRootPath + "/";
				}

                var toolsDir = isNull(getPropListByKey(cfgVar.tools_dir)) ? "" : getPropListByKey(cfgVar.tools_dir)[cfgVar.tools_dir];
                if (!isNull(toolsDir) && !toolsDir.endWith("/")) {
                    toolsDir = toolsDir + "/";
                }

                var confDir = isNull(getPropListByKey(cfgVar.conf_dir)) ? "" : getPropListByKey(cfgVar.conf_dir)[cfgVar.conf_dir];
                if (!isNull(confDir) && !confDir.endWith("/")) {
                    confDir = confDir + "/";
                }

				var fullPath = deployRootPath + toolsDir + confDir + clusterType + "/" + instPath;
				var params = {
					HOST_ID:hostId,
					filePath:fullPath
				};
				showDialog("查看配置文件",780, "80%", Globals.baseJspUrl.HOST_JSP_SHOW_MUTIL_CONFIG_CONTENT_URL,
					function destroy(data){
					
				}, params);
			}
	}, "serviceType.queryClusterById");
}


/**
 * 当前主机可启动的版本
 */
function getStartVersionData(clusterId, clusterType, hostId) {
	var versionData = [];
	var params = {
		CLUSTER_ID:clusterId,
		CLUSTER_TYPE:clusterType,
		HOST_ID:hostId
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "启停管理-查询主机可启停的版本列表",
			function success(result){
			if (result != null && result.length > 0) {
				versionData = result;
			}
	},"deployVersion.queryDeployVersionByHostId", null, false);
	return versionData;
}

/**
 * 集群部署版本列表
 * @param clusterId
 * @param clusterType
 * @returns {Array}
 */
function getStartVersionList(clusterId, clusterType) {
    var versionList = [];
    var params = {
        CLUSTER_ID:clusterId,
        CLUSTER_TYPE:clusterType
    };
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "启停管理-查询集群可启停的版本列表",
        function success(result){
            if (result != null && result.length > 0) {
                versionList = result;
            }
        },"deployVersion.queryDeployVersionByCluster", null, false);
    return versionList;
}

/**
 * 获取当前集群可启动的实例
 * @param clusterId
 * @param clusterType
 * @param hostId
 */
function getInstanceList(clusterId, clusterType) {
	var instanceArray = [];
	var params = {
		CLUSTER_ID:clusterId,
		CLUSTER_TYPE:clusterType
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "启停管理-查询M2DB实例列表",
			function success(result){
			if (result != null && result.length > 0) {
				var instanceName = result[0]["M2DB_INSTANCE"];
				if (instanceName.indexOf(",") != -1) {
					var instanceList = instanceName.split(",");
					for (var i=0; i<instanceList.length; i++) {
						instanceArray.push({"INSTANCE_NAME":instanceList[i]});
					}
				} else {
					instanceArray.push({"INSTANCE_NAME":instanceName});
				}
			}
	},"serviceType.queryServiceTypeList", null, false);
	return instanceArray;
}

/**
 * 组件状态检查
 */
function showClusterStatus(queryParam) {
	var params = {
		CLUSTER_CODE:queryParam["CLUSTER_CODE"],
		CLUSTER_TYPE:queryParam["CLUSTER_TYPE"]
	};
	showDialog("组件启停状态检查","98%","98%",Globals.baseJspUrl.INST_CONFIG_JSP_CHECK_CONDITIONS_URL,
			function destroy(data){
		
	 }, params, {allowDrag:false});
}

/**
 * 高亮检索关键字
 */
function heightLightKeyWord(){
	$("#deployTextarea").textSearch("success,successful",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#429C39;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
	$("#deployTextarea").textSearch("failed,error",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#CF5130;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
}

/**
 * 根据state判断是否部署过主机,如果部署过,关闭窗口后要刷新
 */
function close(){
	closeWindow(paramData);
}

/**
 * 选中所有主机
 */
function selectAll(){
	//先删除数据
	selectNone();
	//全选所有主机
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!array[i].disabled){
			array[i].checked = true;
			singleChange($(array[i]));
		}
	}
}

/**
 * 不选中主机
 */
function selectNone(){
	$("#paramsInfo").html("");
	$("#hostFitDiv input[type='checkbox']").attr("checked", false);
}


/**
 * 获取默认部署模式
 */
function changeDefaultDeployType(clusterType) {
	var defaultDeployType = mini.get("defaultDeployType").getValue();
	if (defaultDeployType != null && defaultDeployType != "") {
		$("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
			var time = $(item).attr("time");
			mini.get("deployType_"+time).setValue(defaultDeployType);
			if (clusterType == busVar.DCA || clusterType == busVar.DMDB || clusterType == busVar.ROCKETMQ) {
				changeDeploy("deployType_" + time, time);
			}
		});
	}
}

/**
 * 获取默认部署模式
 */
function changeDefaultDeployVersion(clusterType) {
    var defaultDeployVersion = mini.get("defaultDeployVersion").getValue();
    if (defaultDeployVersion != null && defaultDeployVersion != "" && typeof(defaultDeployVersion) != 'undefined') {
        $("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
            var time = $(item).attr("time");
            mini.get("version_"+time).setValue(defaultDeployVersion);
        });
    }
}

/**
 * 查看程序启动日志信息（组件启动界面）
 * @param clusterId 集群ID
 * @param clusterType 集群类型
 * @param hostId 主机ID
 * @param instPath 配置文件实例路径
 * @param fileName 配置文件名称
 * @param timeSeq 配置文件对应行标识
 */
function showStartOperationLog(clusterId, clusterType, hostId, instPath, fileName, timeSeq) {
    var deployFileType = clusterType;
	try {
        deployFileType = mini.get("deployType_" + timeSeq).getValue();
	} catch (err) {
        console.warn("deployFileType: " + deployFileType);
	}
    var params = {
        CLUSTER_ID:clusterId,
        CLUSTER_TYPE:clusterType,
        HOST_ID:hostId,
        INST_PATH:instPath,
        FILE_NAME:fileName,
        DEPLOY_FILE_TYPE:deployFileType,
        VERSION:mini.get("version_" + timeSeq).getValue()
    };
    getJsonDataByPost(Globals.baseActionUrl.INST_CONFIG_LOG_DETAIL_ACTION_URL, params, clusterType + "组件程序管理-获取组件启停日志文件信息",
        function success(result){
            if (result != null && result["retCode"] == "1") {
                var textValue = result["retMsg"];
                if (textValue != null) {
                    textValue = textValue.replaceAll("\n","<br/>");
                }
                var params = {
                    RST_STR:textValue,
                    RST_EXEC:"查看日志",
                    RST_FLAG:result["flag"]
                };
                showDialog("日志详情","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                    function destroy(data){

                    }, params, {allowDrag:true});
            } else if (result != null && result["retCode"] == "2") {
                showWarnMessageTips(result["retMsg"]);
            } else {
                showErrorMessageTips(result["retMsg"]);
            }
        });
}

/**
 * 查看程序启停日志信息（组件停止界面）
 * @param clusterId 组件集群id
 * @param clusterType 集群类型
 * @param instId 组件程序实例ID
 */
function showStopOperationLog(clusterId, clusterType, instId) {
    var params = {
        CLUSTER_ID:clusterId,
        CLUSTER_TYPE:clusterType,
        INST_ID:instId
    };
    getJsonDataByPost(Globals.baseActionUrl.INST_CONFIG_LOG_DETAIL_ACTION_URL, params, clusterType + "组件程序管理-获取组件启停日志文件信息",
        function success(result){
            if (result != null && result["retCode"] == "1") {
                var textValue = result["retMsg"];
                if (textValue != null) {
                    textValue = textValue.replaceAll("\n","<br/>");
                }
                var params = {
                    RST_STR:textValue,
                    RST_EXEC:"查看日志",
                    RST_FLAG:result["flag"]
                };
                showDialog("日志详情","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                    function destroy(data){

                    }, params, {allowDrag:true});
            } else if (result != null && result["retCode"] == "2") {
                showWarnMessageTips(result["retMsg"]);
            } else {
                showErrorMessageTips(result["retMsg"]);
            }
        });
}