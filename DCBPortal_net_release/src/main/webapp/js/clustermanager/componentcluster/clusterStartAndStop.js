/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-7-19
 * Time: 上午10:00
 * To change this template use File | Settings | File Templates.
 */
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //加载右键
    loadingRightClick();
    //组件启停
    findAllDeploy();
});

/**
 *  发送ajax请求获取部署分类(zk/nimbus等)
 */
function findAllDeploy(){
	//获取Tab展示业务类型
	var params = {
		TYPE: busVar.COMPONENT_TYPE
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "",
        function(result){
			$("#fitDiv_cont").html("");
			var typeJqObj = $('<div></div>');
			if(result.length>0){
				//循环每一个数组
				$.each(result, function (i, item) {
					//拼接类型名的html
	            	var contentJq= $(loadDeployList(item, busVar.COMPONENT_TYPE));
                    typeJqObj.append(contentJq);
	            });
				
				//显示和隐藏分组的事件
				j$(typeJqObj.find(".div_3")).off("click").on("click",function(){
	               //如果当前状态为隐藏，则显示出来，反之显示出来
	               if($(this).parent().next("div").is(":hidden")){
	                   $(this).parent().next("div").show(500);
	               }else{
	                   $(this).parent().next("div").hide(500);
	               }
			    });
			}
			$("#fitDiv_cont").append(typeJqObj);
			mini.parse();
        },"serviceType.queryAllDeploy");
}

/**
 * 部署分类名字和ID拼接
 */
function loadDeployList(item,type){	
	var startId = 'add_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
	var stopId = 'deploy_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
	
	var str="";
    //data-value主要给隐藏分了和显示分类时用
    str += '<div class="div_1" data-type="'+type+'">'
    	+  '	<div class="div_2">'
	    +  '		<div class="div_3" style="width: 85%">'+item.CLUSTER_NAME+'</div>'
	    +  '		<div class="operate_run_block">'
		+  '			<input type="button" value="启动" style="width: 65px;" '
		+  '				data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
		+  ' 				data-CLUSTER_CODE="'+item.CLUSTER_CODE+'"'
		+  '				data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'"'
		+  ' 				data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
		+  ' 				class="operate_but" id="'+startId+'" onClick="startHost(this);"/>'
		+  '			<input type="button" value="停止" class="operate_but"'
		+  '				data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
		+  ' 				data-CLUSTER_CODE="'+item.CLUSTER_CODE+'"'
		+  '				data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'"'
		+  ' 				data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
		+  '				id="'+stopId+'" onClick="stopHost(this)"/>'
		+  '		</div>'
		+  '		<div style="clear:both;"></div>'
		+  '	</div>'
    	+  '	<div class="all_center" style="display: none;" id="all_center_'+item.CLUSTER_ID+'"></div>'
    	+  '</div>';
	
 	//加载类型下的主机
	loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME, type);
	
	return str;
}

/**
 * 加载类型下的主机列表
 * @param CODE
 */
function loadHostList(CLUSTER_ID, CLUSTER_CODE, CLUSTER_TYPE, CLUSTER_NAME, type){
	//查询业务参数
	var params = {
		CLUSTER_ID:CLUSTER_ID,
		CLUSTER_NAME:CLUSTER_NAME,
		CLUSTER_CODE:CLUSTER_CODE,
		CLUSTER_TYPE:CLUSTER_TYPE
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "",
	        function(result){
				var host_str="";
				//每次开始前将中间主机显示区域的内容清空
				$("#all_center_"+CLUSTER_ID).html("");
				if(result.length>0){
					//循环每一个数组
					$.each(result, function (i, item) {
						host_str='<ul class="ul_host" id="'+item.ID+'" '
								+'  data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
								+'  data-ID="'+item.ID+'" '
								+'  data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
								+'  data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" '
								+'  data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
								+'  data-HOST_ID="'+item.HOST_ID+'" '
								+'  data-STATE="'+item.STATE+'">'
								+'	<li style="height:60%;">'
								+'		<image src="../../../images/deployHost/module_run_'+item.RUN_STATE+'.png" class="ul_host_img"/>'
								+'	</li>'
								+'	<li style="height:40%;">'+item.HOST_IP + '<br/>(' + item.SSH_USER + ')'+'</li>'
								
								+'</ul>';
						//将host_str包含的html封装成jq对象，给每个对象加上hover事件
						var host_jq=$(host_str);
						host_jq.hover(function () {
							var obj = $(this);
							//组件类型
							var clusterType = obj.data("CLUSTER_TYPE");
							//给类加上菜单
							var array = new Array();
							array.push({header: '右击菜单'});
							array.push({text: '主机详情', action: function(e){
								e.preventDefault();
								scanHostInfo(obj);
							}});
							if(type != "3"){
								array.push({text: '启动', action: function(e){
									e.preventDefault();
									startOneHost(obj);
								}});
								array.push({text: '停止', action: function(e){
									e.preventDefault();
									stopOneHost(obj);
								}});
								array.push({text: '状态检查', action: function(e){
									e.preventDefault();
									checkRunStatus(obj);
								}});
							}
							
							if (clusterType == busVar.M2DB) {
								array.push({text: '刷新表', action: function(e){
									e.preventDefault();
									refresh_tables(obj);
								}});
								array.push({text: '刷数据', action: function(e){
									e.preventDefault();
									refresh_mem(obj);
								}});
								array.push({text: '导入', action: function(e){
									e.preventDefault();
									input_table(obj);
								}});
							}
							
							array.push({text: '终端操作', action: function(e){
								e.preventDefault();
								operatorTerminal(obj);
							}});
							context.attach(".ul_host", array);
	                    });
						$("#all_center_"+CLUSTER_ID).append(host_jq);
		            });
					$("#all_center_"+CLUSTER_ID).append("<div style='clear:both;'></div>");
				}
				var hostCount = result == null ? 0 : result.length;
				$("#all_center_"+CLUSTER_ID).prev("div").find(".div_3").html(params["CLUSTER_NAME"] + "（" + hostCount +  "台主机）");
	        }, "hostStart.queryHostByStart");
}

/**
 * 按钮：运行主机
 * @param obj 组件对象
 */
function startHost(obj){
	//业务参数
	var params = {
		CLUSTER_ID:obj.id.split("_")[2],
		CLUSTER_TYPE:obj.id.split("_")[3],
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME")
	};

    //点添加按钮如果该 div 隐藏的话，则显示出来
    $(obj).parents('.div_1').find(".all_center").css("display","block");
    
	var flag;// 判断是否有可启动主机

	//判断主机是否已部署
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,params,"",
		function(result){
			if(result["SUM"] == 0){
				flag = true;
				return;
			}
	},"hostStart.queryHostCountDeployed","",false);
	if(flag){
        showWarnMessageTips("未找到已部署主机,请检查!");
		return;
	}
	var theme = "启动-" + params["CLUSTER_NAME"];
	chooseToForward(params, theme);
}

/**
 * 停止主机
 * @param obj 组件对象
 */
function stopHost(obj){
	//业务参数
	var params = {
		ID:$(obj).data("ID"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		STATE:$(obj).data("STATE"),
		HOST_ID:$(obj).data("HOST_ID")
	};

    //点添加按钮如果该 div 隐藏的话，则显示出来
    $(obj).parents('.div_1').find(".all_center").css("display","block");
    
	var title="停止-" + params["CLUSTER_NAME"];
	if(params["CLUSTER_TYPE"]==busVar.ROCKETMQ){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ROCKMQ_STOP_URL,
		    function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
		},params, {allowDrag:false},"ROCKETMQ"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.FASTDFS){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"FASTDFS"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.DCA){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"DCA"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.MONITOR){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"MONITOR"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.DMDB){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"DMDB"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//ZOOKEEPER
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_STOP_URL,
			function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"ZOOKEEPER"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//JSTORM
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"JSTORM"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//dclog
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"DCLOG"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.M2DB){  //M2DB
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"M2DB"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.DSF){  //M2DB
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DSF_STOP_URL,
            function destroy(data){
                loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
            },params, {allowDrag:false},"DSF"+params["CLUSTER_ID"],true);
    }
}

/**
 * 查看分类中的主机详情
 * @param obj 组件对象
 */
function scanHostInfo(obj){
	var params = {
		HOST_ID:$(obj).data("HOST_ID")
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params,"主机管理-查询详细信息",
			function (result){
				var param=result[0];
				showDialog("详细信息",600,400,Globals.baseJspUrl.HOST_JSP_DETAIL_URL,
					function destroy(data){
				
				},param);
	   },"host.queryHostList");
}

/**
 * 终端操作
 * @param obj 组件对象
 */
function operatorTerminal(obj) {
	//获取主机ID
	var hostId = $(obj).data("HOST_ID");
	var hostStr = "'" + hostId + "'";
	$("#termialHost").val(hostStr);
	$("#termialForm").attr("action", Globals.baseActionUrl.HOST_ACTION_TERMINAL_URL);
	$("#termialForm").submit();
}

/**
 * 右键启动
 * @param obj 组件对象
 */
function startOneHost(obj){
	//业务参数
	var params = {
		ID:$(obj).data("ID"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		STATE:$(obj).data("STATE"),
		HOST_ID:$(obj).data("HOST_ID")
	};
	var title="运行" + params["CLUSTER_NAME"];
	chooseToForward(params,title);
} 

/**
 * 右键停止组件
 * @param obj 组件对象
 */
function stopOneHost(obj){
	//业务参数
	var params = {
		ID:$(obj).data("ID"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		HOST_ID:$(obj).data("HOST_ID")
	};
	var title="停止-" + params["CLUSTER_NAME"];
	if(params["CLUSTER_TYPE"]==busVar.ROCKETMQ){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ROCKMQ_STOP_URL,
		    function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
		},params, {allowDrag:false},"ROCKETMQ"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.FASTDFS){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"FASTDFS"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.DCA){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"DCA"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.MONITOR){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"MONITOR"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"]==busVar.DMDB){
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"DMDB"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//ZOOKEEPER
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"ZOOKEEPER"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//jstorm
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"JSTORM"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//DCLOG
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"DCLOG"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.M2DB){ //M2DB
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"M2DB"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.DSF){ //M2DB
        showDialogJumpPage(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DSF_STOP_URL,
            function destroy(data){
                loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
            },params, {allowDrag:false},"DSF"+params["CLUSTER_ID"],true);
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
 * @params params 业务参数
 */
function chooseToForward(params, theme){
	if(params["CLUSTER_TYPE"] == busVar.ROCKETMQ){//rucketmq业务
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ROCKMQ_START_URL,
			function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
		},params, {allowDrag:false},"rucketmq"+params["CLUSTER_ID"],true);
	} else if (params["CLUSTER_TYPE"] == busVar.FASTDFS) {   //FastDFS
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_START_URL,
				function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"fastdfs"+params["CLUSTER_ID"],true);
	} else if (params["CLUSTER_TYPE"] == busVar.DCA) {   //DCAS
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"dcas"+params["CLUSTER_ID"],true);
	} else if (params["CLUSTER_TYPE"] == busVar.MONITOR) {   //Monitor
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"monitor"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.DMDB){//Dmdb
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"Dmdb"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//zookeeper
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"zookeeper"+params["CLUSTER_ID"],true);
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//jstorm
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"jstorm"+params["CLUSTER_ID"],true);
	}else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//DCLOG
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"dclog"+params["CLUSTER_ID"],true);
	}else if(params["CLUSTER_TYPE"] == "route"){//路由
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/route/routeTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"]+ "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == "other"){//周边
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/other/otherTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == "billing"){//计费
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/billing/billingTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == "rent"){//月租
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/rent/rentTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == busVar.M2DB && params["REFRESH_TABLE"]){  //刷新M2DB表
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_M2DB_REFRESH_URL,
    			function destroy(data){
    		},params, {allowDrag:false},"REFRESH_TABLE"+params["CLUSTER_ID"],true);
    }else if(params["CLUSTER_TYPE"] == busVar.M2DB && params["REFRESH_MEM"]){	//刷新M2DB数据
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_M2DB_MEM_URL,
    			function destroy(data){
    			 
    		},params, {allowDrag:false},"REFRESH_MEM"+params["CLUSTER_ID"],true);
    }else if(params["CLUSTER_TYPE"] == busVar.M2DB && params["INPUT_TABLE"]){	//导入M2DB数据
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_M2DB_INPUT_URL,
    			function destroy(data){
    			 
    		},params, {allowDrag:false},"INPUT_TABLE"+params["CLUSTER_ID"],true);
    } else if (params["CLUSTER_TYPE"] == busVar.M2DB) {//M2DB
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_START_URL,
				function destroy(data){
    			loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false},"m2db"+params["CLUSTER_ID"],true);
    } else if(params["CLUSTER_TYPE"] == busVar.DSF){//Dsf
        showDialogJumpPage(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DSF_START_URL,
            function destroy(data){
                loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
            },params, {allowDrag:false},"dsf"+params["CLUSTER_ID"],true);
	}
}

/**
 * M2DB刷新表
 * @param obj
 */
function refresh_tables(obj){
	//业务参数
	var params = {
		ID:$(obj).data("ID"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		HOST_ID:$(obj).data("HOST_ID"),
		REFRESH_TABLE:"1"
	};
	
	var flag;// 判断是否有可启动主机
	//判断主机是否已部署
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,params,"",
		function(result){
			if(result["SUM"] == 0){
				flag = true;
				return;
			}
	},"hostStart.queryHostCountDeployed","",false);

	if(flag){
        showWarnMessageTips("未找到已部署主机,请检查!");
		return;
	}

	// 判断是否有可运行主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,params,"",
		function(result){
			if(result["SUM"] == 0){
				flag = true;
				return;
			}
		},"hostStart.queryHostCountForStart","",false);

	if(flag){
        showWarnMessageTips("没有一个创建了m2db实例!");
		return;
	}
	var title=params["CLUSTER_TYPE"]+"-刷新表";
	chooseToForward(params, title);
}

/**
 * M2DB刷新数据
 * @param host_code_id
 * @returns
 */
function refresh_mem(obj){
	//业务参数
	var params = {
		ID:$(obj).data("ID"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		HOST_ID:$(obj).data("HOST_ID"),
		REFRESH_MEM:"1"
	};

	var flag;// 判断是否有可启动主机

	//判断主机是否已部署
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,params,"",
		function(result){
			if(result["SUM"] == 0){
				flag = true;
				return;
			}
	},"hostStart.queryHostCountDeployed","",false);

	if(flag){
        showWarnMessageTips("未找到已部署主机,请检查!");
		return;
	}

	// 判断是否有可运行主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,params,"",
		function(result){
			if(result["SUM"] == 0){
				flag = true;
				return;
			}
		},"hostStart.queryHostCountForStart","",false);

	if(flag){
        showWarnMessageTips("没有一个创建了m2db实例!");
		return;
	}
	var title = params["CLUSTER_TYPE"] + "-刷数据";
	chooseToForward(params, title);
}

/**
 * M2DB导入
 * @param host_code_id
 */
function input_table(obj){
	//业务参数
	var params = {
		ID:$(obj).data("ID"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		HOST_ID:$(obj).data("HOST_ID"),
		INPUT_TABLE:"1"
	};

	var flag;// 判断是否有可启动主机

	//判断主机是否已部署
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,params,"",
		function(result){
			if(result["SUM"] == 0){
				flag = true;
				return;
			}
	},"hostStart.queryHostCountDeployed","",false);

	if(flag){
        showWarnMessageTips("未找到已部署主机,请检查!");
		return;
	}

	// 判断是否有可运行主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,params,"",
		function(result){
			if(result["SUM"] == 0){
				flag = true;
				return;
			}
		},"hostStart.queryHostCountForStart","",false);

	if(flag){
        showWarnMessageTips("没有一个创建了m2db实例!");
		return;
	}
	var title = params["CLUSTER_TYPE"] + "-导入表";
	chooseToForward(params, title);
}

/**
 * 配置情况
 * @param obj 组件对象
 */
function checkRunStatus(obj){
	//业务参数
	var params = {
		ID:$(obj).data("ID"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		HOST_ID:$(obj).data("HOST_ID")
	};
    showDialogJumpPage(params["CLUSTER_NAME"]+"-状态检查","98%","98%",Globals.baseJspUrl.INST_CONFIG_JSP_CHECK_CONDITIONS_URL,
			function destroy(data){
		loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
	 }, params, {allowDrag:false},"check"+params["CLUSTER_TYPE"]+params["CLUSTER_ID"],true);
}

/**
 * 弹出页面销毁
 * @param params
 */
function reloadComplete(params) {
    if(!isEmptyStr(params["CLUSTER_ID"])){
        var $div = "#all_center_"+ params["CLUSTER_ID"];
        $($div).css("display","block");
        //定位到操作按钮
        $($div).prev()[0].scrollIntoView();
    }
    loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
}