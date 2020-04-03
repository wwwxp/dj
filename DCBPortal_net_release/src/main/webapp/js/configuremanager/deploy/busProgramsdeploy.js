
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
	//按钮对象
	JsVar["sumbitButton"] = mini.get("sumbitButton");
	//版本列表
	JsVar["versionList"] = mini.get("versionList");
	//业务主集群ID
	JsVar["BUS_CLUSTER_ID"] = data["BUS_CLUSTER_ID"];
	//业务主集群编码
	JsVar["BUS_CLUSTER_CODE"] = data["BUS_CLUSTER_CODE"];
	
    JsVar["result"]={};
    //加载右键
    loadingRightClick();
    // 加载版本号下拉框
    loadVersionCombobox();
    //查询版本对应的主机列表
	queryHostDiv(JsVar["versionList"].getText());
}

/**
 * 加在版本号下拉框
 * @returns
 */
function loadVersionCombobox(){
	var params = {
		FILE_TYPE:2,
		STATE:1,
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
	};
	comboxLoad(JsVar["versionList"], params, "ftpFileUpload.queryFileInfo","","",false);
	
	var versionData = JsVar["versionList"].getData();
	if (versionData != null && versionData.length > 0) {
		JsVar["versionList"].select(0);
		//所选业务包类型
		JsVar["PACKAGE_TYPE"] = versionData[0]["PACKAGE_TYPE"];
		//选择版本包名称， 如:DIC-BIL-OCS-MOD-AH_V14.0.1.0
		JsVar["NAME"] = versionData[0]["NAME"];
		//所选版本, 如:14.0.1.0
		JsVar["VERSION"] = versionData[0]["VERSION"];
		//查询业务实例
		queryBusInstList();
	}
}

/**
 * 下拉框选择触发函数
 * @returns
 */
function reloadVersionData(e){
	//版本包类型
	JsVar["PACKAGE_TYPE"] = e.selected.PACKAGE_TYPE;
	//版本对应业务包名称
	JsVar["NAME"] = e.selected.NAME;
	//选择版本
	JsVar["VERSION"] = e.selected.VERSION;
	//查询当前包主机列表
	queryHostDiv();
	//获取当前版本已经启动的业务实例
	queryBusInstList();
}

//初使化主机列表的div
function queryHostDiv(version){
	//每次执行本方法之前,都将div中的内容清除,使代码重用性高
	$("#hostFitDiv").html("");
	//查询该类型未部署的所有主机
	var param={
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
		VERSION:JsVar["VERSION"],
		PACKAGE_TYPE:JsVar["PACKAGE_TYPE"]
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,param,"部署图-查询该部署分类下的所有未部署主机",
	    function(result){
			var htmlStr = "";
			if (result != null && result.length > 0) {
				//获取业务类型
				var clusterCodeArray = [];
				for (var i=0; i<result.length; i++) {
					var hasExists = false;
					for (var j=0; j<clusterCodeArray.length; j++) {
						if (result[i]["CLUSTER_CODE"] == clusterCodeArray[j]["CLUSTER_CODE"]) {
							hasExists = true;
							break;
						}
					}
					if (!hasExists) {
						clusterCodeArray.push({
							CLUSTER_CODE:result[i]["CLUSTER_CODE"],
							CLUSTER_TYPE:result[i]["CLUSTER_TYPE"],
							CLUSTER_NAME:result[i]["CLUSTER_NAME"],
							IS_RUN_JSTORM:result[i]["CLUSTER_ELE_RUN_JSTORM"]
						});
					}
				}
				
				//根据业务类型划分组显示
				for (var i=0; i<clusterCodeArray.length; i++) {
					var type = clusterCodeArray[i]["CLUSTER_CODE"];
					var isRunCluster = clusterCodeArray[i]["IS_RUN_JSTORM"];
//					htmlStr += ' <div class="div_1" style="margin:0px 5px 0px 5px;padding-bottom:0px;" data-type="'+type+'" data-cluster="' + isRunCluster +'">'
//				 			+' <div class="div_2" style="width:120px;line-height:55px;">' +clusterCodeArray[i].CLUSTER_NAME +'</div>'
//				 			+' <div style="margin:10px 0px 0px 0px;padding-left:140px;">';

                    htmlStr += '<div class="div_1" data-type="'+type+'">'
                        +  '	<div class="div_2">'
                        +  '		<div class="div_3">'
                        +  '		    <span>'+clusterCodeArray[i].CLUSTER_NAME+'</span>'
                        +  '			<div id="ckCluster" style="margin-left:8px;" name="cluster_name" class="mini-checkbox" readOnly="false" text="" title="全选/全不选" onvaluechanged="checkClusterBox">全选/全不选</div>'
                        +  '		</div>'
                        +  '		<div class="operate_block">'
                        +  '		</div>'
                        +  '		<div style="clear:both;"></div>'
                        +  '	</div>'
                        +  '	<div class="all_center" id="all_center_'+clusterCodeArray[i].CLUSTER_ID+'">';

					// htmlStr += '<div class="div_1" data-type="'+type+'">'
				    // 	+  '	<div class="div_2">'
					//     +  '		<div class="div_3">'+clusterCodeArray[i].CLUSTER_NAME+'</div>'
					//     +  '		<div class="operate_block">'
					// 	+  '		</div>'
					// 	+  '		<div style="clear:both;"></div>'
					// 	+  '	</div>'
				    // 	+  '	<div class="all_center" id="all_center_'+clusterCodeArray[i].CLUSTER_ID+'">';
					for(var j=0;j<result.length;j++) {
						var _type = result[j]["CLUSTER_CODE"];
						if(_type != type ){
							continue;
						}
						var item = result[j];
						JsVar["result"][item["ID"]]=item;
						var tips = "主机名称：" + item.HOST_NAME
								+ "\n主机信息：" + item.HOST_IP + "(" + item.SSH_USER + ")"
                        		+ "\n最近部署版本: " + (item.VERSION == undefined ? "未部署" : item.VERSION);
						if(item.SUM_RUN > 0){
			        		htmlStr+='<ul class="ul_host_common" title="'+tips+'" id="'+(new Date()).getTime()+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:#5cb85c;" >'
			        				+'	<li style="height:20%;">';
			        		htmlStr+='	<input type="checkbox" sum_run="'+ item.SUM_RUN +'"  data-CLUSTER_TYPE="'+ item.CLUSTER_TYPE +'" id="'+item.ID+'" data-HOST_ID="'+item.HOST_ID+'" name="ck_host"'
		        					+'		style="font-size:12px;" value="'+item.ID+'" />'
		    	        			+'	<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
		    	        			+'		for="'+item.ID+'" >' + item.HOST_IP + "(" + item.SSH_USER + ")</label>";
			        	} else {
			        		htmlStr+='<ul class="ul_host_common" title="'+tips+'" id="'+(new Date()).getTime()+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:gray;">'
			        				+'	<li style="height:20%;">';
			        		htmlStr+='	<input type="checkbox" data-CLUSTER_TYPE="'+ item.CLUSTER_TYPE +'" id="'+item.ID+'" data-HOST_ID="'+item.HOST_ID+'" name="ck_host" checked=checked '
			        				+'		style="font-size:12px;" value="'+item.ID+'" />'
		    	        			+'	<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
		    	        			+'		for="'+item.ID+'" >' + item.HOST_IP + "(" + item.SSH_USER + ")</label>";
			        	}
			        	htmlStr+='	</li>'
				        		+'</ul>';
					}
					htmlStr+= '</div><div style="clear:both;"></div>';
					htmlStr+=  '</div>';
					htmlStr+='</div><div style="clear:both;"></div></div>';
				}
			}
			$("#hostFitDiv").append(htmlStr);
			mini.parse();
			//主机上面添加右键功能
			$("#hostFitDiv .ul_host_common").hover(function(e) {
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
			}, function(e) {
				mini.parse();
			});
	},"deployHome.queryBusDeployConfig","",false);
	
	//initClick();
}

/**
 * 小类全选功能
 */
function checkClusterBox() {
    var array = document.getElementsByClassName("div_1");
    var uid = this.uid + "$check";
    var flag = this.value;
    for(var i=0;i<array.length;i++){
        var length = $(array[i]).find("input[id='"+uid+"']").length;
        if(parseInt(length) > 0){
            var obj = $(array[i]).find("input[name='ck_host']");
            if(flag == "true"){
                for(var k=0;k<obj.length;k++){
                    obj[k].checked = true;
                }
            }else{
                for(var k=0;k<obj.length;k++){
                    obj[k].checked = false;
                }
            }
        }
    }
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
		param=result[0];
		showDialog("详细信息",600,400,Globals.baseJspUrl.HOST_JSP_DETAIL_URL,
				function destroy(data){
			
		 },param);
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

//选择部署主机
function chooseDeploy(){
	var codes=[];
	var obj=document.getElementsByName('ck_host');
	var host_arry = new Array();
	var state_type=0;
	//取到对象数组后，我们来循环检测它是不是被选中
	for(var i=0; i<obj.length; i++){
		var host_info={};
		if(obj[i].checked){
		    var value = obj[i].value;
		    host_info["ID"]=JsVar["result"][value]["ID"];
		    host_info["HOST_ID"]=JsVar["result"][value]["HOST_ID"];
		    host_info["HOST_IP"]=JsVar["result"][value]["HOST_IP"];
		    host_info["SSH_USER"]=JsVar["result"][value]["SSH_USER"];
		    //业务主集群
		    host_info["BUS_CLUSTER_ID"]=JsVar["BUS_CLUSTER_ID"];
		    //业务主集群编码
		    host_info["BUS_CLUSTER_CODE"]=JsVar["BUS_CLUSTER_CODE"];
		    //当前业务集群类型
			host_info["CLUSTER_TYPE"]=JsVar["result"][value]["CLUSTER_TYPE"];
			//当前业务集群ID
			host_info["CLUSTER_ID"]=JsVar["result"][value]["CLUSTER_ID"];
			//集群名称
			host_info["CLUSTER_NAME"] = JsVar["result"][value]["CLUSTER_NAME"];
			host_info["STATE"]=JsVar["result"][value]["STATE"];
			codes.push(host_info["CLUSTER_TYPE"]);
			if(host_info.STATE==1){
				state_type=1;
			}
			//将对象放入数组
			host_arry.push(host_info);
		}
	}
	
	//去重
	codes = codes.unique();
	
	if(host_arry.length>0){
		//var last = host_arry.length-1;
		host_arry[0]["codes"]=codes;
	}
	
	//选中主机
	if(host_arry.length>0){
		if(state_type==1){//
			showConfirmMessageAlter("已选中已部署主机，确定覆盖部署环境吗？",function ok(){
				sumbit(host_arry);
			} );
		}else{
			showConfirmMessageAlter("确定部署环境吗？",function ok(){
				sumbit(host_arry);
			});
	  }
	}else{//没有选中主机
        showWarnMessageTips("请选择主机！");
	}
}

var textValue = "";
var index = 0;
function sumbit(host_arry){
	JsVar["sumbitButton"].setText("正在部署");
	JsVar["sumbitButton"].setEnabled(false);
	index = 0;
	textValue = "";
	startTimes = (new Date()).getTime();
	textValue += "部署开始时间: " + ((new Date(startTimes)).format("yyyy-MM-dd hh:mm:ss")) + "<br/>";
	postAjax(host_arry);
}

/**
 * 部署
 * @param host_arry
 */
function postAjax(host_arry){
	//获取单个记录
	var program = host_arry[index];
	program["VERSION"] = JsVar["VERSION"];
	program["NAME"] = JsVar["NAME"];
	program["PACKAGE_TYPE"] = JsVar["PACKAGE_TYPE"];
	if (index != 0) {
		textValue += "<br/>主机【"+program["HOST_IP"]+ "(" + program["SSH_USER"] + ")" + "】，正在部署【" + program["CLUSTER_NAME"] + "】， 部署版本【"+JsVar["versionList"].getText()+"】...<br/>";
	} else {
		textValue += "主机【"+program["HOST_IP"]+ "(" + program["SSH_USER"] + ")" + "】，正在部署【" + program["CLUSTER_NAME"] + "】， 部署版本【"+JsVar["versionList"].getText()+"】...<br/>";
	}
	$("#deployTextarea").html(textValue);
	
	//将滚动条自动滚动到最下面
	var deployDiv = document.getElementById("mainDiv");
	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	
	//高亮检索“失败”“成功”关键字
	heightLightKeyWord();
	
	getJsonDataByPost(Globals.baseActionUrl.CLUSTER_ACTION_BUSINESS_DEPLOY_URL, program, "部署管理-业务类主机部署",
		function(result){
			index++;
			textValue=$("#deployTextarea").html()+result.success;
			textValue=textValue.replaceAll("\n","<br/>");
			$("#deployTextarea").html(textValue);
	        
			if(host_arry.length == index){
	        	var lowerValue = textValue.toLocaleLowerCase();
	        	var endTimes = (new Date()).getTime();
	        	textValue += "部署结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss")) + ", 本次部署"+index+"台主机, 总耗时: " + Math.floor((endTimes - startTimes)/1000) + "秒";
	        	$("#deployTextarea").html(textValue);
	        	
	        	//将滚动条自动滚动到最下面
	        	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	        	
	        	//高亮检索“失败”“成功”关键字
	        	heightLightKeyWord();
	        	
	    		queryHostDiv();
	    		JsVar["deploy_state"]="1";
	        	
	        	//重置按钮
	        	JsVar["sumbitButton"].setText("部署");
	        	JsVar["sumbitButton"].setEnabled(true);
				if(lowerValue.indexOf("error") >-1 || lowerValue.indexOf("failed")>-1 || lowerValue.indexOf("失败") > -1) {
	        		showErrorMessageAlter("出现异常，中断部署！");
	        	}else{
                    showMessageTips("部署成功");
	        	}
	        }else{
	        	postAjax(host_arry);
	        }
	    },null,null,true,null,false);
}

/**
 * 高亮检索关键字
 */
function heightLightKeyWord(){
	$("#deployTextarea").textSearch("success,successful,成功",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#429C39;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
	$("#deployTextarea").textSearch("failed,error,失败",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#CF5130;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
}

/**
 * 根据state判断是否部署过主机,如果部署过,关闭窗口后要刷新
 */
function close(){
	if(JsVar["deploy_state"] == 1){
		closeWindow(systemVar.SUCCESS);
	}else{
		closeWindow();
	}
}

/**
 * 全选
 */
function selectAllVersionHost() {
    var array = document.getElementsByName("ck_host");
    for(var i=0;i<array.length;i++){
        array[i].checked = true;
    }

    var clusters = document.getElementsByName("cluster_name");
    for(var k=0;k<clusters.length;k++){
        clusters[k].checked = true;
    }
}

/**
 * 只选择未部署过的主机列表
 */
function selectAll(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!($(array[i]).attr("sum_run"))){
			array[i].checked = true;
		}
	}
}

/**
 * 反选
 */
function selectNone(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		array[i].checked = false;
	}

    var clusters = document.getElementsByName("cluster_name");
    for(var k=0;k<clusters.length;k++){
        clusters[k].checked = false;
    }
}

/**
 * 点击选中
 */
function initClick() {
	$(".div_1").each(function(index, item) {
		//当前集群类型是否有主机运行
		var isRun = false;
		//判断当前版本是否与进程在运行，如果有进程在运行不能重新部署版本
		var subList = $(item).find("input[type='checkbox']");
		$(item).find("input[type='checkbox']").each(function(subIndex, subItem) {
			var hostId = $(subItem).data("HOST_ID");
			var clusterType = $(subItem).data("CLUSTER_TYPE");
			
			$.each(currVerData, function(lastIndex, lastItem){
				var lastHostId = lastItem["HOST_ID"];
				var programType = lastItem["PROGRAM_TYPE"];
				var runJstorm = lastItem["RUN_JSTORM"];
				if (runJstorm == "1") {
					if (programType == clusterType && lastItem["RUN_STATE"] == "1") {
						isRun = true;
						$(subItem).bind('click',function(e){
                            showWarnMessageTips('当前版本该主机程序正在运行,需先停止,再部署!');
							return false;
						});
					}
				} else {
					if (programType == clusterType && lastHostId == hostId && lastItem["RUN_STATE"] == "1") {
						$(subItem).bind('click',function(e){
                            showWarnMessageTips('当前版本该主机程序正在运行,需先停止,再部署!');
							return false;
						});
					}
				}
			});
		});
		
		//运行在Jstorm中
		if ($(item).data("cluster") == "1" && !isRun) {
			var subList = $(item).find("input[type='checkbox']");
			$(item).find("input[type='checkbox']").each(function(subIndex, subItem) {
					$(subItem).bind('click', function(e){
						if(this.checked){
							$.each(subList, function(lastIndex, lastItem){
								$(lastItem).attr('checked','checked');
							});
						}else{
							$.each(subList, function(lastIndex, lastItem){
								$(lastItem).removeAttr('checked');
							});
						}
				});
			});
		}
	});
}

/**
 * 查询当前版本程序实例
 * @param version
 */
var currVerData = [];
function queryBusInstList() {
	var params = {
		VERSION:JsVar["VERSION"],
		RUN_STATE:busVar.STATE_ACTIVE,
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "程序实例查询-分组查询每台主机实例",
			function (result){
			if (result != null) {
				currVerData = result;
			}
	   }, "taskProgram.queryProgramRunList", "", false);
}
