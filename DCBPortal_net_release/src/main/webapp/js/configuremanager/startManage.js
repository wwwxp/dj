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
//记录配置集群分类的数据
var config_data=new Object;
//存储每个hover事件的jq对象的id

//初始化
$(document).ready(function () {
    mini.parse();
    //加载右键
    loadingRightClick();
    //业务启停界面返回操作
    getForwardParams();
});

/**
 * 获取请求参数
 */
function getForwardParams(){
    var forwardParamString = window.location.search;
    if (forwardParamString != null && forwardParamString.indexOf("=") != -1) {
    	var queryArray = forwardParamString.split("=");
        var tabName = queryArray[1];
        var tabs = mini.get("#deploy_tabs");
        tabs.activeTab(tabs.getTab(tabName));
    }
}

/**
 * 获取当前tab页的id
 * @param e
 */
function loadPage(e){
	JsVar["CLUSTER_ID"] = e.tab.name;
	//加载Tab数据
	findAllDeploy();
}

/**
 *  发送ajax请求获取部署分类(zk/nimbus等)
 */
function findAllDeploy(){
	//获取Tab展示业务类型
	var tabName = mini.get("deploy_tabs").getActiveTab().name;
	var type = "1";
	if (tabName == "billingTab") {
		type = "3";
	}
	var params = {
		TYPE:type
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "",
        function(result){
		$("#fitDiv_cont").html("");
//		var data=config_data["config_data"];
//		
//		for(var j=0;j<data.length;j++){
//			var configValue = data[j].CONFIG_VALUE;//分类
//			//分类的jq对象，data-value主要给隐藏分类和显示分类时用
//			typeJqObj=$('<div><p class="classify_name" data-value="'+configValue+'">'+data[j].CONFIG_NAME+'</p></div>');
		var typeJqObj = $('<div></div>');
			if(result.length>0){
				//循环每一个数组
				$.each(result, function (i, item) {
					//if(item.TYPE==configValue){
						//拼接类型名的html
		            	var  contentJq= $(loadDeployList(item, type));
                        typeJqObj.append(contentJq);
					//}
	            });
			}
            //显示和隐藏分组的事件
//            j$(typeJqObj.find("p")[0]).off("click").on("click",function(){
//                var type = $(this).data("value");//父分类标识
//                $("#fitDiv_cont").find(".div_1").each(function (){
//                    if($(this).data("type")==type){
//                        //如果当前状态为隐藏，则显示出来，反之显示出来
//                       if($(this).is(":hidden")){
//                           $(this).show(500);
//                           $(this).parent().css({"border":"none","margin":"0px 0px 0px 0px"});
//                       }else{
//                           $(this).hide(500);
//                           //隐藏时，给分类的div给出一个边框
//                           $(this).parent().css({"border":"1px dashed #D9E4F1","margin":"0px 5px 20px 5px"});
//                       }
//                    }
//                });
//            });
			$("#fitDiv_cont").append(typeJqObj);
			mini.parse();
//		}
        },"serviceType.queryAllDeploy");
}

/**
 * 部署分类名字和ID拼接
 */
function loadDeployList(item,type){	
	var str="";
	str+='<div class="div_1" data-type="'+type+'">'
	 	+'<div class="div_2">'+item.CLUSTER_NAME+'</div>'
		+'<div class="all_center" id="all_center_'+item.CLUSTER_ID+'">';
	//加载类型下的主机
	loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, type);
	if(type==3){
		var startId = 'add_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
		str+='</div>'
	 		+'<div class="operate_run_block">'
	 		+'	<input type="button" value="操作" data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
	 		+' 		data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'"'
	 		+' 		data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
	 		+'		class="operate_but" style="width: 65px;" id="'+startId+'" onClick="startHost(this);"/>'
            +'</div>'
	 		+'<br style="clear:left"/>'
	 		+'</div>';
	}else{
		var startId = 'add_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
		var stopId = 'deploy_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
		str+='</div>'
			+'<div class="operate_run_block">'
			+'	<input type="button" value="启动" style="width: 65px;" '
			+'		data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
			+' 		data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'"'
			+' 		data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
			+' 		class="operate_but" id="'+startId+'" onClick="startHost(this);"/>'
			+'	<input type="button" value="停止" class="operate_but"'
			+'		data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
			+' 		data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'"'
			+' 		data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
			+'		id="'+stopId+'" onClick="stopHost(this)"/>'
			+'</div>'
			+'<br style="clear:left"/>'
			+'</div>';
	}
	return str;
}

/**
 * 加载类型下的主机列表
 * @param CODE
 */
function loadHostList(CLUSTER_ID, CLUSTER_CODE, CLUSTER_TYPE, type){
	var dbParam = "hostStart.queryHostByStart";
	if(CLUSTER_TYPE == 'billing' || CLUSTER_TYPE == 'other' || CLUSTER_TYPE == 'route' || CLUSTER_TYPE == 'rent'){
		dbParam = "hostStart.queryHostByDeployBybillingRent";
	}
	//查询业务参数
	var params = {
		CLUSTER_ID:CLUSTER_ID,
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
						// 业务类运行状态处理
						if(item.CLUSTER_TYPE === "route" || item.CLUSTER_TYPE === "other" || 
								item.CLUSTER_TYPE === "billing" || item.CLUSTER_TYPE === "rent"){
							if(item.SUM > 0){
								item.RUN_STATE = 1;
							}else{
								item.RUN_STATE = 0;
							}
						}
						host_str='<ul class="ul_host" id="'+item.ID+'" data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
								+'  data-ID="'+item.ID+'" data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
								+'  data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
								+'  data-HOST_ID="'+item.HOST_ID+'" data-STATE="'+item.STATE+'">'
								+'	<li style="height:60%;">'
								+'		<image src="../../../images/deployHost/module_run_'+item.RUN_STATE+'.gif" class="ul_host_img"/>'
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
				}
	        },dbParam);
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
	var theme = "启动" + params["CLUSTER_NAME"];
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
	var title="停止" + params["CLUSTER_NAME"];
	if(params["CLUSTER_TYPE"]==busVar.ROCKETMQ){ //RocketMQ
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ROCKMQ_STOP_URL,
		    function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
		},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.FASTDFS){ //Fastdfs
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DCA){  //DCA
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.MONITOR){ //Monitor
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DMDB){  //DMDB
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//ZOOKEEPER
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_STOP_URL,
			function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//JSTORM
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//dclog
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.M2DB){  //M2DB
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
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
	var title="停止" + params["CLUSTER_NAME"];
	if(params["CLUSTER_TYPE"]==busVar.ROCKETMQ){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ROCKMQ_STOP_URL,
		    function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
		},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.FASTDFS){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DCA){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.MONITOR){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DMDB){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//ZOOKEEPER
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//jstorm
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//DCLOG
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.M2DB){ //M2DB
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
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
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ROCKMQ_START_URL,
			function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
		},params, {allowDrag:false});
	} else if (params["CLUSTER_TYPE"] == busVar.FASTDFS) {   //FastDFS
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_START_URL,
				function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if (params["CLUSTER_TYPE"] == busVar.DCA) {   //DCAS
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if (params["CLUSTER_TYPE"] == busVar.MONITOR) {   //Monitor
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.DMDB){//Dmdb
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//zookeeper
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//jstorm
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	}else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//DCLOG
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
	}else if(params["CLUSTER_TYPE"] == busVar.ROUTE){//路由
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/route/routeTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"]+ "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == busVar.OTHER){//周边
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/other/otherTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == busVar.BILLING){//计费
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/billing/billingTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == busVar.RENT){//月租
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/rent/rentTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&TAB_NAME=" + mini.get("deploy_tabs").getActiveTab().name;
    }else if(params["CLUSTER_TYPE"] == busVar.M2DB && params["REFRESH_TABLE"]){  //刷新M2DB表
    	showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_M2DB_REFRESH_URL,
    			function destroy(data){
    			 
    		},params, {allowDrag:false});
    }else if(params["CLUSTER_TYPE"] == busVar.M2DB && params["REFRESH_MEM"]){	//刷新M2DB数据
    	showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_M2DB_MEM_URL,
    			function destroy(data){
    			 
    		},params, {allowDrag:false});
    }else if(params["CLUSTER_TYPE"] == busVar.M2DB && params["INPUT_TABLE"]){	//导入M2DB数据
    	showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_M2DB_INPUT_URL,
    			function destroy(data){
    			 
    		},params, {allowDrag:false});
    } else if (params["CLUSTER_TYPE"] == busVar.M2DB) {
    	showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_START_URL,
				function destroy(data){
    			loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
			},params, {allowDrag:false});
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
	var title=params["CLUSTER_TYPE"]+"刷新表";
	chooseToForward(params, title);
}

/**
 * M2DB刷新数据
 * @param obj 选中主机CheckBox对象
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
	var title = params["CLUSTER_TYPE"] + "刷数据";
	chooseToForward(params, title);
}

/**
 * M2DB导入
 * @param obj 选中主机CheckBox对象
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
	var title = params["CLUSTER_TYPE"] + "导入表";
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
	showDialog(params["CLUSTER_NAME"]+"状态检查","98%","98%",Globals.baseJspUrl.INST_CONFIG_JSP_CHECK_CONDITIONS_URL,
			function destroy(data){
		loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
	 }, params, {allowDrag:false});
}
