//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//是否新增Redis信息
var isRedis = false;
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["fileForm"] = new mini.Form("fileForm");
});

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {
    JsVar["data"] = data;
    JsVar["copyFiles"] = mini.get("copyFilesNames");
    JsVar["file_host_ip"] = mini.get("file_host_ip");
    //获取部署主机列表信息
    loadDeployHostId();
    //获取模板文件列表
    loadDefaultFile();
    
    //如果选择节点为Redis，则获取DCA对应的redis端口
    if (JsVar["data"]["CLUSTER_TYPE"] == busVar.DCA 
    		&& JsVar["data"]["fileName"] == busVar.REDIS 
    		&& JsVar["data"]["hostIpDir"] == busVar.REDIS) {
    	JsVar["CHILD_DATA"] = JsVar["data"]["CHILD_DATA"];
    	isRedis = true;
    	mini.get("file_host_ip").setShowNullItem(false);
    	mini.get("fileName").setVisible(false);
    	mini.get("redisFileName").setVisible(true);
    }
}

/**
 * 获取该分类已部署主机IP
 */
function loadDeployHostId(){
	var params=new Object();
	params["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	params["CLUSTER_CODE"]=JsVar["data"]["CLUSTER_CODE"];
	params["CLUSTER_TYPE"]=JsVar["data"]["CLUSTER_TYPE"];
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params,"配置修改--新建实例-查询分类下已部署主机",
	        function(result){
		    	if(result!=null && result.length>0 ){
		    		JsVar["data"]["CLUSTER_ID"] = result[0].CLUSTER_ID;
		    		//当前选择主机
		    		//JsVar["HOST_ID"] = result[0]["HOST_ID"];
		    		//给可选主机IP下拉框注值
		    		JsVar["file_host_ip"].setData(result);
		    	}
	    },"deployHome.queryDeployHostByCodeAndHost");
}

/**
 * 获取default下的文件值
 */
function loadDefaultFile(){
	getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_LOAD_DEFAULT_FILE_URL, JsVar["data"],"配置修改-新建实例--获取可选文件",
        function(result){
	    	if(result!=null && result.length>0 ){
	    		//给可选文件下拉框注值
	    		JsVar["copyFiles"].setData(result);
	    	}
    });
}

/**
 * IP值改变触发
 */
function changeDefualtFileName(e){
	//获取当前主机ID
	JsVar["HOST_ID"] = e.selected.HOST_ID;
	//当前主机IP
	var chosenIp=JsVar["file_host_ip"].getValue();
	//配置文件路径
	var filePath=JsVar["data"]["filePath"];
	var fileName=JsVar["data"]["fileName"];
	if(chosenIp != "" && chosenIp != null && !isRedis){
		//查询子节点文件名称
//		getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_LOAD_FILE_UNDER_GIVEN_PATH_URL, JsVar["data"],"配置修改--新建实例-查询指定文件下子节点",
//				function(result){
//					var nodeFileName;
//					var maxDefualtNum=1;
//					if(result!=null && result.length>0){
//						$.each(result,function(i,item){
//							nodeFileName=item.fileName;
//							var beginIndex=nodeFileName.indexOf("(",0);
//							var endIndex=nodeFileName.indexOf(")",0);
//							var beginText=nodeFileName.substr(0,beginIndex);
//							var endText=nodeFileName.substr(endIndex+1,nodeFileName.length);
//							var nodeFileName_part=beginText+endText;
//							var fileArr=nodeFileName_part.split("_");
//							var currNum=0;
//							//名称中包含下划线
//							if(fileArr.length>1){
//								//currIpUser=fileArr[0];
//								currNum=fileArr[1];
//								var array=nodeFileName.split("_"+currNum);
//								if(array.length>1){
//									currIpUser=array[0];
//								}
//								if(!isNaN(currNum) && currIpUser==chosenText && currNum>=maxDefualtNum){//是数字
//									maxDefualtNum=parseInt(currNum)+1;
//								}
//							}
//						});
//						//初始化为两位数，取值范围[01,99]
//						maxDefualtNum=('' + maxDefualtNum).length < 2 ? ((new Array(2 + 1)).join('0') + ''+maxDefualtNum).slice(-2) : maxDefualtNum;
//						mini.get("fileName").setValue(maxDefualtNum);
//					}
//			    });
		} else {
			//Redis端口信息
			var params = {};
			var hostIp = mini.get("file_host_ip").getValue();
			if (JsVar["CHILD_DATA"] != null) {
				var portList = [];
				for (var i=0; i<JsVar["CHILD_DATA"].length; i++) {
					if (JsVar["CHILD_DATA"][i]["HOST_IP"] == hostIp) {
						portList.push(JsVar["CHILD_DATA"][i]["HOST_PORT"]);
					}
				}
				params["EXCLUDE_PORT"] = portList.join(",");
			}
			params["HOST_IP"] = hostIp;
			//params["HOST_IP"] = "192.168.161.165";
			//JsVar["data"]["CLUSTER_CODE"] = "CCC";
			getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "配置修改--新建实例-获取主机对应Redis端口",
					function(result){
							if (result != null && result.length > 0) {
								mini.get("redisFileName").setData(result);
							}
				    }, "dcaConfig.queryPortListByHost", JsVar["data"]["CLUSTER_CODE"]);
		}
}

/**
 * 支持批量添加文件
 */
function addBatchFile() {
	var addData = JsVar["fileForm"].getData();
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false){
        return;
    }
    
    if(JsVar["data"]["fileName"]==addData["fileName"]){
        showWarnMessageTips("实例名称不合法，请检查！");
    	return;
    }
    //名称不能包含下划线
    if(addData["fileName"].indexOf("_")>0){
        showWarnMessageTips("实例名称不能输入下划线，请检查！");
    	return;
    }
    
    var hostIp = JsVar["file_host_ip"].getValue();
    var fileName = addData["fileName"];
    if (isRedis) {
    	fileName = mini.get("redisFileName").getValue();
    }
    JsVar["data"]["hostIp"] = hostIp;
    JsVar["data"]["newFileNames"] = fileName;
    JsVar["data"]["copyFilesNames"] = addData["copyFilesNames"];
    JsVar["data"]["HOST_ID"] = JsVar["HOST_ID"];
    
    //设置添加成功后选中的文件
    var selFileName = fileName;
    if (fileName.indexOf(",") != -1) {
    	selFileName = fileName.split(",")[0];
    }
    var finalSelFileName = (hostIp == null || hostIp == "") ? selFileName : hostIp + "_" + selFileName;
    JsVar["data"]["SEL_FILE_NAME"] = finalSelFileName;
    
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_BATCH_ADD_COPY_FILE_URL, JsVar["data"],"配置修改-新建文件夹",
            function(result){
	    	if (result != null && result["RST_MSG"] != null) {
	    		if (result["RST_CODE"] == '0') {
                    showWarnMessageTips(result["RST_MSG"],function ok(){
		    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
		    		});
	    		} else if (result["RST_CODE"] == '1') {
	    			showErrorMessageAlter(result["RST_MSG"],function ok(){
		    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
		    		});
	    		} else {
	    			showMessageAlter(result["RST_MSG"],function ok(){
		    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
		    		});
	    		}
	    	} else {
	    		showMessageAlter("创建实例失败",function ok(){
	    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
	    		});
	    	}
        });
}

/**
 * 新增
 * @param e
 */
function addFile(){
    //是新建文件
	var addData = JsVar["fileForm"].getData();
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false){
        return;
    }
    if(JsVar["data"]["fileName"]==addData["fileName"]){
        showWarnMessageTips("实例名称不合法，请检查！");
    	return;
    }
    //名称不能包含下划线
    if(addData["fileName"].indexOf("_")>0){
        showWarnMessageTips("实例名称不能输入下划线，请检查！");
    	return;
    }
    //获取IP号
    var fileHostIp= JsVar["file_host_ip"].getValue();
    var fileName = addData["fileName"];
    if (isRedis) {
    	fileName = mini.get("redisFileName").getValue();
    }
    fileName=(fileHostIp == "" ? fileName : fileHostIp + "_" + fileName);
    
    JsVar["data"]["newFileName"] = fileName;
    JsVar["data"]["copyFilesNames"] = addData["copyFilesNames"];
    JsVar["data"]["HOST_IP"]=fileHostIp;
    
    //主机ID
    JsVar["data"]["HOST_ID"] = JsVar["HOST_ID"];
    
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_ADD_COPY_FILE_URL, JsVar["data"],"配置修改-新建文件夹",
        function(result){
	    	if(result.successNum!=undefined && result.errorNum!=undefined){
				if(result.successNum>0 && result.errorNum>0){//有成功有失败  --警告
					showWarnMessageAlter(result.isSuccess,function ok(){
		    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["newFileName"]});
		    		});
				}else if(result.successNum>0 && result.errorNum<=0){//只有有成功  --成功
					showMessageAlter(result.isSuccess,function ok(){
		    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["newFileName"]});
		    		});
				}else if(result.successNum<=0 && result.errorNum>0){//只有失败  --错误
					showErrorMessageAlter(result.isSuccess,function ok(){
		    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["newFileName"]});
		    		});
				}
			}else{
				showMessageAlter(result.isSuccess,function ok(){
	    			closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["newFileName"]});
	    		});
			}
    });
}
