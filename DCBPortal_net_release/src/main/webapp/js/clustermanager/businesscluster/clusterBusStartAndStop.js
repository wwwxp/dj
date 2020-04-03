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
    //加载集群类型
    loadingClusterConfig();
    //加载所有的集群类型
    loadClusterType(busVar.BUSINESS_TYPE);
    //业务启停界面返回操作
    getForwardParams();
    //加载集群TAB
    //loadingClusterTab();
    //加载右键
    loadingRightClick();
});

/**
 * 加载所有的集群类型
 * @param type
 */
function loadClusterType(type) {
	var params = {
		TYPE:type
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "业务启停-查询所有的业务类型",
	    function(result){
			JsVar["CLUSTER_TYPE_LIST"] = [];
			if (result != null) {
				for (var i=0; i<result.length; i++) {
					JsVar["CLUSTER_TYPE_LIST"].push({
						CLUSTER_NAME:result[i]["CLUSTER_TYPE"],
						CLUSTER_TYPE:result[i]["CLUSTER_ELE_TYPE"],
						RUN_JSTORM:result[i]["RUN_JSTORM"]
					});
				}
			}
	    }, "clusterEleDefine.queryClusterEleList", "", false);
}

/**
 * 获取请求参数
 */
function getForwardParams(){
    var busClusterId = getQueryString("busClusterId");
    JsVar["forwardClusterId"] = getQueryString("CLUSTER_ID");
    if (busClusterId) {
        //查询业务主集群Tab
        var tabObj = window.parent.mini.get("#deploy_tabs");
        var tabArray = window.parent.mini.get("#deploy_tabs").getTabs();
        if (tabArray != null && tabArray.length > 0) {
        	for (var i=0; i<tabArray.length; i++) {
        		
        		//获取跳转的Tab页签
        		if (tabArray[i]["id"] == busClusterId) {
        			var data = {
        				BUS_CLUSTER_ID:busClusterId,
        				BUS_CLUSTER_CODE:tabArray[i]["code"],
        				BUS_CLUSTER_NAME:tabArray[i]["title"]
        			};
        			loadPage(data);
        			break;
        		}
        	}
        }
    }
}

//改变页面大小时，业务类主机显示块重新规划大小
$(window).resize(function() {
	var client_width=document.body.clientWidth;
	var width=(client_width-170)+"px";
	$('#servicePackage').css("width",width);
	
	//拿到业务类左侧块的高度，再填充右侧按钮块的高度，使其高度一致
	var common_height=($("#servicePackage").height()-22)+"px";
	var top_height=($("#servicePackage").height()-2-40)/2+"px";
	$("#serviceButton").css("height",common_height);
	$("#serviceButton>input").css("margin-top",top_height);
});

/**
* 加载集群类型
*/
function loadingClusterConfig() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "部署图-查询全部部署分类",
	    function(result){
			if(result != null && result.length>0){
				JsVar["PROGREAM_DATA"] = result;
			} else {
				JsVar["PROGREAM_DATA"] = [{"CONFIG_NAME":"组件", "CONFIG_VALUE":"1"}, {"CONFIG_NAME":"业务", "CONFIG_VALUE":"3"}];
			}
	    }, "deployHome.queryConfigNameAndValue");
}

/**
* 加载业务主集群
*/
function loadingClusterTab() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
      function(result){
		var tab_str="";
		if(result.length>0){
			var index = 0;
			var tabs=mini.get("#deploy_tabs");
			$.each(result, function (i, item) {
				var tab = {
					title:item.BUS_CLUSTER_NAME,
					id:item.BUS_CLUSTER_ID,
					name:item.BUS_CLUSTER_ID,
					code:item.BUS_CLUSTER_CODE,
					dataField:item.BUS_CLUSTER_ID, 
					showCloseButton: false
				};
				tabs.addTab(tab);
				
				if (JsVar["BUS_CLUSTER_ID"] == item.BUS_CLUSTER_ID) {
					index = i;
				}
			});
			//给第一个tab加上active动作
			tabs.setActiveIndex(index);
		}
  },"busMainCluster.queryBusMainClusterListByState", "", false);
}

/**
 * 获取当前tab页的id
 * @param e
 */
function loadPage(data){
	//业务主集群
	//var busClusterId = e.tab.id;
	JsVar["BUS_CLUSTER_ID"] = data["BUS_CLUSTER_ID"];
	JsVar["BUS_CLUSTER_NAME"] = data["BUS_CLUSTER_NAME"];
	JsVar["BUS_CLUSTER_CODE"] = data["BUS_CLUSTER_CODE"];
	//查询业务主集群Tab
	findAllDeploy(JsVar["BUS_CLUSTER_ID"]);
}

/**
 *  发送ajax请求获取部署分类(zk/nimbus等)
 */
function findAllDeploy(busClusterId){
	var params = {
		BUS_CLUSTER_ID:busClusterId
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL + "?BUS_CLUSTER_ID=" + busClusterId, params, "",
        function(result){
			$("#fitDiv_cont").html("");
			var data = JsVar["PROGREAM_DATA"];
			for(var j=0;j<data.length;j++){
				var configValue = data[j].CONFIG_VALUE;//分类
				//分类的jq对象，data-value主要给隐藏分类和显示分类时用
	            var headStr = '<div style="margin:0px 0px 20px 0px;">';
	                headStr += '<div style="height:40px;">';
	            if (configValue == busVar.COMPONENT_TYPE) {
	            	headStr += '<image src="'+Globals.ctx+'/images/component.png" class="bus_img" />';
	            } else {
	            	headStr += '<image src="'+Globals.ctx+'/images/business.png"  class="bus_img" />';
	            }
	            headStr += '<span class="classify_name" style="color:#1296db;" data-value="'+configValue+'">'+data[j].CONFIG_NAME+'</span>';
				headStr += '</div>';
				headStr += '</div>';
				typeJqObj = $(headStr);
				
				if(result.length>0){
					//循环每一个数组
					$.each(result, function (i, item) {
						if(item.TYPE==configValue){
							//拼接类型名的html
			            	var  contentJq= $(loadDeployList(item,configValue));
	                        typeJqObj.append(contentJq);
						}
		            });
				}
				
				//显示和隐藏分组的事件
				j$(typeJqObj.find(".div_3")).off("click").on("click",function(){
	               //如果当前状态为隐藏，则显示出来，反之显示出来
	               if($(this).parent().next("div").is(":hidden")){
	                   $(this).parent().next("div").show(500);
	               }else{
	                   $(this).parent().next("div").hide(500);
	               }
			    });
				
	            //显示和隐藏分组的事件
	            j$(typeJqObj.find("span")[0]).off("click").on("click",function(){
	                var type = $(this).data("value");//父分类标识
	                $("#fitDiv_cont").find(".div_1").each(function (){
	                    if($(this).data("type")==type){
	                        //如果当前状态为隐藏，则显示出来，反之显示出来
	                       if($(this).is(":hidden")){
	                           $(this).show(500);
	                           $(this).parent().css({"border":"none","margin":"0px 0px 0px 0px"});
	                       }else{
	                           $(this).hide(500);
	                           //隐藏时，给分类的div给出一个边框
	                           $(this).parent().css({"border":"1px dashed #D9E4F1" ,"margin":"0px 5px 20px 5px"});
	                       }
	                    }
	                });
	            });
				$("#fitDiv_cont").append(typeJqObj);
				
				//组件
	            if (configValue == busVar.COMPONENT_TYPE) {
	            	j$(typeJqObj.find("span")[0]).click();
	            }
			}
            //如果是返回操作的话，则定位到之前点击的操作按钮
			if(!isEmptyStr(JsVar["forwardClusterId"])){
				//先把主机列表的div显示出来
				var $div = "#all_center_"+ JsVar["forwardClusterId"];
                $($div).css("display","block");
                //定位到操作按钮
                document.querySelector($div).scrollIntoView();
            }
        }, "serviceType.queryAllDeployByBusClusterId");
}

/**
 * 部署分类名字和ID拼接
 * @param item 集群对象
 * @param type 集群类型
 */
function loadDeployList(item,type){	
	
	var str="";
	if (busVar.BUSINESS_TYPE == type) {
	    //data-value主要给隐藏分了和显示分类时用
		var startId = 'add_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
	    str += '<div class="div_1" data-type="'+type+'">'
	    	+  '	<div class="div_2">'
		    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
		    +  '		<div class="operate_run_block">'
	 		+  '			<input type="button" value="操作" data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
	 		+  ' 				data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
	 		+  '				data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" '
	 		+  ' 				data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
	 		+  ' 				data-PERSONAL_CONF="'+item.PERSONAL_CONF+'" '
	 		+  ' 				data-JSTORM_RUN="'+item.JSTORM_RUN+'" '
	 		+  '				class="operate_but" style="width: 65px;" id="'+startId+'" onClick="startHost(this);"/>'
			+  '		</div>'
			+  '		<div style="clear:both;"></div>'
			+  '	</div>'
	    	+  '	<div class="all_center" style="display: none;" id="all_center_'+item.CLUSTER_ID+'"></div>'
	    	+  '</div>';
	} else {
		var startId = 'add_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
		var stopId = 'deploy_but_'+item.CLUSTER_ID+'_'+item.CLUSTER_TYPE;
		str += '<div class="div_1" data-type="'+type+'">'
	    	+  '	<div class="div_2">'
		    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
		    +  '		<div class="operate_run_block">'

			+  '		</div>'
			+  '		<div style="clear:both;"></div>'
			+  '	</div>'
	    	+  '	<div class="all_center" id="all_center_'+item.CLUSTER_ID+'"></div>'
	    	+  '</div>';
	}
	
    //加载类型下的主机
	loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME, type);

	return str;
}

/**
 * 加载类型下的主机列表
 * @param CODE
 */
function loadHostList(CLUSTER_ID, CLUSTER_CODE, CLUSTER_TYPE, CLUSTER_NAME, type){
	var dbParam = "hostStart.queryHostByStart";
	
    for (var i=0; i<JsVar["CLUSTER_TYPE_LIST"].length; i++) {
    	if (JsVar["CLUSTER_TYPE_LIST"][i]["CLUSTER_NAME"] == CLUSTER_TYPE) {
    		dbParam = "hostStart.queryHostByDeployBybillingRent";
    		break;
    	}
    }
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
				if(result != null && result.length>0){
					
					// 业务类运行状态处理
					for (var i=0; i<JsVar["CLUSTER_TYPE_LIST"].length; i++) {
						if (JsVar["CLUSTER_TYPE_LIST"][i]["CLUSTER_NAME"] == CLUSTER_TYPE) {
							//对于运行在Jstorm中的程序，如果其中一个实例处于运行状态，那么该集群所有主机都是运行状态
							if (JsVar["CLUSTER_TYPE_LIST"][i]["RUN_JSTORM"] == "1") {
								// var instStatus = "0";
								// for (var i=0; i<result.length; i++) {
								// 	if (result[i]["JS_SUM"] > 0) {
								// 		instStatus = "1";
								// 		break;
								// 	}
								// }
								// for (var i=0; i<result.length; i++) {
								// 	result[i]["RUN_STATE"] = instStatus;
								// }

								//Jstorm当有supervisor运行，并且业务在运行才是运行状态
                                for (var i=0; i<result.length; i++) {
                                	if (result[i]["VERSION"] != null && result[i]["VERSION"] != "" ) {
                                        result[i]["RUN_STATE"] = "1";
									} else {
                                        result[i]["RUN_STATE"] = "0";
									}
                                }

							} else {
								for (var i=0; i<result.length; i++) {
									if (result[i]["SUM"] > 0) {
										result[i]["RUN_STATE"] = "1";
									} else {
										result[i]["RUN_STATE"] = "0";
									}
								}
							}
							break;
						}
					}
					
					//循环每一个数组
					$.each(result, function (i, item) {
						host_str='<ul class="ul_host" id="'+item.ID+'" data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
								+'  data-ID="'+item.ID+'" data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
								+'  data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
								+'  data-HOST_ID="'+item.HOST_ID+'" data-STATE="'+item.STATE+'">'
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
	        },dbParam);
}

/**
 * 按钮：运行主机
 * @param obj 组件对象
 */
function startHost(obj){
	//业务参数
	var params = {
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		PERSONAL_CONF:$(obj).data("PERSONAL_CONF"),
		JSTORM_RUN:$(obj).data("JSTORM_RUN")
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
	if(params["CLUSTER_TYPE"]==busVar.ROCKETMQ){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ROCKMQ_STOP_URL,
		    function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
		},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.FASTDFS){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DCA){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.MONITOR){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DMDB){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//ZOOKEEPER
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_STOP_URL,
			function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//JSTORM
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//dclog
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.M2DB){  //M2DB
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
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
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
		},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.FASTDFS){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DCA){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.MONITOR){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"]==busVar.DMDB){
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_STOP_URL,
			    function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//ZOOKEEPER
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//jstorm
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//DCLOG
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.M2DB){ //M2DB
		showDialog(title,"98%", "98%",Globals.baseJspUrl.HOST_JSP_M2DB_STOP_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
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
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
		},params, {allowDrag:false});
	} else if (params["CLUSTER_TYPE"] == busVar.FASTDFS) {   //FastDFS
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_FASTDFS_START_URL,
				function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if (params["CLUSTER_TYPE"] == busVar.DCA) {   //DCAS
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCA_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if (params["CLUSTER_TYPE"] == busVar.MONITOR) {   //Monitor
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_MONITOR_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.DMDB){//Dmdb
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DMDB_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.ZOOKEEPER){//zookeeper
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_ZOOKEEPER_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} else if(params["CLUSTER_TYPE"] == busVar.JSTORM){//jstorm
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_JSTORM_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	}else if(params["CLUSTER_TYPE"] == busVar.DCLOG){//DCLOG
		showDialog(theme,"98%", "98%",Globals.baseJspUrl.HOST_JSP_DCLOG_START_URL,
				function destroy(data){
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
			},params, {allowDrag:false});
	} 
	
//	else if(params["CLUSTER_TYPE"] == "route"){//路由
//		window.location.href = Globals.ctx+"/jsp/configuremanager/run/route/routeTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"]+ "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
//    } else if(params["CLUSTER_TYPE"] == "other"){//周边
//		window.location.href = Globals.ctx+"/jsp/configuremanager/run/other/otherTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
//    } else if(params["CLUSTER_TYPE"] == "billing"){//计费
//		window.location.href = Globals.ctx+"/jsp/configuremanager/run/billing/billingTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
//    } else if(params["CLUSTER_TYPE"] == "rent"){//月租
//		window.location.href = Globals.ctx+"/jsp/configuremanager/run/rent/rentTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
//    } else if(params["CLUSTER_TYPE"] == "dcm"){  //DCM采集
//		window.location.href = Globals.ctx+"/jsp/configuremanager/run/dcm/dcmTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"]+ "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
//    } 
    
    else if(params["PERSONAL_CONF"] == busVar.STATE_ACTIVE){  //根据主机IP启停程序
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/rundiffip/runDiffTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"]+ "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
    } else if(params["JSTORM_RUN"] == busVar.STATE_ACTIVE){   //运行在Jstorm中的程序
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/runtopology/runTopologyTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
    } else if(params["PERSONAL_CONF"] != busVar.STATE_ACTIVE && typeof(params["PERSONAL_CONF"]) != 'undefined'
    		&& params["JSTORM_RUN"] != busVar.STATE_ACTIVE && typeof(params["JSTORM_RUN"]) != 'undefined'){//计费
		window.location.href = Globals.ctx+"/jsp/configuremanager/run/runsameip/runSameTaskManage?CLUSTER_ID=" + params["CLUSTER_ID"] + "&CLUSTER_CODE=" + params["CLUSTER_CODE"] + "&CLUSTER_TYPE=" + params["CLUSTER_TYPE"] + "&BUS_CLUSTER_ID=" + JsVar["BUS_CLUSTER_ID"];
    }
    
    else if(params["CLUSTER_TYPE"] == busVar.M2DB && params["REFRESH_TABLE"]){  //刷新M2DB表
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
    			loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
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
	var title = params["CLUSTER_TYPE"] + "刷数据";
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
		loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], busVar.COMPONENT_TYPE);
	 }, params, {allowDrag:false});
}
