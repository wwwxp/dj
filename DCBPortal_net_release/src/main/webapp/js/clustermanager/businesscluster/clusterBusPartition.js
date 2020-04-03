/**
 * Created with IntelliJ IDEA.
 * Creater: yuanhao
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
    //加载集群TAB
    //loadingClusterTab();
    //加载右键
    loadingRightClick();
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
	    }, "deployHome.queryConfigNameAndValue", "", false);
}

/**
 * 加载业务主集群
 */
function loadingClusterTab() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
        function(result){
		var tab_str="";
		if(result.length>0){
			var tabs = mini.get("#deploy_tabs");
			$.each(result, function (i, item) {
				var tab = {
					title:item.BUS_CLUSTER_NAME,
					id:item.BUS_CLUSTER_ID,
					code:item.BUS_CLUSTER_CODE,
					dataField:item.BUS_CLUSTER_ID, 
					showCloseButton: false,
					refreshOnClick:false
				};
				tabs.addTab(tab);
            });
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
		}
    },"busMainCluster.queryBusMainClusterListByState");
}

/**
 * 获取当前tab页的id
 * @param e
 */
function loadPage(data){
	//业务主集群
	//var busClusterId = e.tab.id;
	JsVar["BUS_CLUSTER_ID"] = data["BUS_CLUSTER_ID"];
	JsVar["BUS_CLUSTER_CODE"] = data["BUS_CLUSTER_CODE"];
	JsVar["BUS_CLUSTER_NAME"] = data["BUS_CLUSTER_NAME"];
	
	//加载集群类型
    loadingClusterConfig();
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
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL + "?BUS_CLUSTER_ID=" + busClusterId, params, "部署图-查询全部部署分类",
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
						if(item.TYPE == configValue){
							//拼接类型名的html
			            	var  contentJq= $(loadDeployList(item, configValue));
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
	                    if($(this).data("type") == type){
	                        //如果当前状态为隐藏，则显示出来，反之显示出来
	                       if($(this).is(":hidden")){
	                           $(this).show(500);
	                           $(this).parent().css({"border":"none","margin":"0px 0px 0px 0px"});
	                       }else{
	                           $(this).hide(500);
	                           //隐藏时，给分类的div给出一个边框
	                           $(this).parent().css({"border":"1px dashed #D9E4F1", "margin":"0px 5px 20px 0px"});
	                       }
	                    }
	                });
	            });
				
	            //把拼装好的html jq对象放到content里面
	            $("#fitDiv_cont").append(typeJqObj);
	            
				//组件默认收缩
	            if (configValue == busVar.COMPONENT_TYPE) {
	            	j$(typeJqObj.find("span")[0]).click();
	            }
			}
        }, "serviceType.queryAllDeployByBusClusterId","",false);
}

/**
 * 部署分类名字和ID拼接
 */
function loadDeployList(item, type){
    var str="";
    //data-value主要给隐藏分了和显示分类时用
    
    if (busVar.BUSINESS_TYPE == type) {
    	str += '<div class="div_1" data-type="'+type+'">'
	    	+  '	<div class="div_2">'
		    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
		    +  '		<div class="operate_block">'
			+  '			<image src="'+Globals.ctx+'/images/deployHost/module_add.png" '
			+  '				data-hostRepeat="'+item.HOST_REPEAT+'" '
			+  '				data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
			+  '				data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
			+  '				data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" '
			+  '				data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
			+  '				data-JSTORM_RUN="'+item.JSTORM_RUN+'" '
			+  '				data-CLUSTER_ELE_TYPE="'+item.CLUSTER_ELE_TYPE+'" '
			+  '				title="添加" class="operate_but1" id="add_but_'+item.CLUSTER_ID+'" onClick="addType(this)"/>'
			+  '		</div>'
			+  '		<div style="clear:both;"></div>'
			+  '	</div>'
	    	+  '	<div class="all_center"  style="display: none;" id="all_center_'+item.CLUSTER_ID+'"></div>'
	    	+  '</div>';
    	//加载类型下的主机
    	loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME, item.CLUSTER_ELE_TYPE);
    } else {
    	str += '<div class="div_1" data-type="'+type+'">'
    	+  '	<div class="div_2">'
	    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
	    +  '		<div class="operate_block">'
	    
		+  '		</div>'
		+  '		<div style="clear:both;"></div>'
		+  '	</div>'
    	+  '	<div class="all_center"  id="all_center_'+item.CLUSTER_ID+'"></div>'
    	+  '</div>';
    	//加载类型下的主机
    	loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME, item.CLUSTER_ELE_TYPE);
    }
    return str;
}

/**
 * 加载类型下的主机列表
 * @param CLUSTER_ID  集群ID
 * @param CLUSTER_CODE 集群编码
 * @param CLUSTER_TYPE 集群类型
 * @param CLUSTER_NAME 集群名称
 * @param CLUSTER_ELE_TYPE 集群类型(业务集群，组件集群)
 */
function loadHostList(CLUSTER_ID, CLUSTER_CODE, CLUSTER_TYPE, CLUSTER_NAME, CLUSTER_ELE_TYPE){
	
	//业务集群
	var dbKey = "deployHome.queryBusDeployHostAllCodeList";
	//组件集群
	if (CLUSTER_ELE_TYPE == busVar.COMPONENT_TYPE) {
		dbKey = "deployHome.queryDeployHostAllCodeList";
	}
	var params = {
		CLUSTER_ID:CLUSTER_ID,
		CLUSTER_NAME:CLUSTER_NAME,
		CLUSTER_CODE:CLUSTER_CODE,
		CLUSTER_TYPE:CLUSTER_TYPE
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "集群划分-查询分类下的所有主机",
	        function(result){
				var host_str="";
				//每次开始前将中间主机显示区域的内容清空
				$("#all_center_"+CLUSTER_ID).html("");
				if(result.length>0){
					//循环每一个数组
					$.each(result, function (i, item) {
						host_str='<ul class="ul_host" id="'+item.ID+'"'
								+'  data-CLUSTER_ID="'+item.CLUSTER_ID+'"'
								+'  data-CLUSTER_NAME="'+item.CLUSTER_NAME+'"'
								+'  data-CLUSTER_ELE_TYPE="'+CLUSTER_ELE_TYPE+'"'
								+'  data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'"' 
								+'  data-HOST_ID="'+item.HOST_ID+'"' 
								+'  data-RUN_STATE="'+item.RUN_STATE+'">'
								+'	<li style="height:60%;">'
								+'		<image src="'+Globals.ctx+'/images/deployHost/module_'+item.STATE+'.png" class="ul_host_img"/>'
								+'	</li>'
								+'	<li style="height:40%;">'+item.HOST_IP + '<br/>(' + item.SSH_USER + ')'+'</li>'
								+'</ul>';
						//将host_str包含的html封装成jq对象，给每个对象加上hover事件
						var host_jq=$(host_str);
						host_jq.hover(function () {
							//获取当前对象
							var obj = $(this);
							//给类加上菜单
							var array = new Array();
							array.push({header: '右击菜单'});
							//给类加上菜单
							array.push({text: '删除', action: function(e){
								e.preventDefault();
								deleteHostInDeploy(obj);
							}});
							//给类加上菜单
							array.push({text: '批量删除', action: function(e){
								e.preventDefault();
								deleteHostBatchInDeploy(obj);
							}});
							array.push({text: '主机详情', action: function(e){
								e.preventDefault();
								scanHostInfo(obj);
							}});
							array.push({text: '终端操作', action: function(e){
								e.preventDefault();
								operatorTerminal(obj);
							}});
							context.attach('.ul_host', array);
	                    });
						$("#all_center_"+CLUSTER_ID).append(host_jq);
		            });
					$("#all_center_"+CLUSTER_ID).append("<div style='clear:both;'></div>");
				}
				var hostCount = result == null ? 0 : result.length;
				$("#all_center_"+CLUSTER_ID).prev("div").find(".div_3").html(params["CLUSTER_NAME"] + "（" + hostCount +  "台主机）");
	        }, dbKey);
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
 * 按钮：添加类型 
 * @param obj 组件对象
 */
function addType(obj){
	//业务参数
	var params = {
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		JSTORM_RUN:$(obj).data("JSTORM_RUN"),
		CLUSTER_ELE_TYPE:$(obj).data("CLUSTER_ELE_TYPE")
	};
	//点添加按钮如果该 div 隐藏的话，则显示出来
    $(obj).parents('.div_1').find(".all_center").css("display","block");

	showAddDialog("添加主机",780,470,Globals.baseJspUrl.DEPLOY_JSP_ADD_HOST_URL,
	    function destroy(data){
			if(systemVar.SUCCESS==data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], params["CLUSTER_ELE_TYPE"]);
			}
	    },params);
}

/**
 * 批量删除
 */
function deleteHostBatchInDeploy(obj) {
	//业务参数
	var params = {
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		JSTORM_RUN:$(obj).data("JSTORM_RUN"),
		CLUSTER_ELE_TYPE:$(obj).data("CLUSTER_ELE_TYPE")
	};
	showAddDialog("批量删除",780,470,Globals.baseJspUrl.DEPLOY_JSP_BATCH_DEL_HOST_URL,
		function destroy(data){
			if(systemVar.SUCCESS==data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], params["CLUSTER_ELE_TYPE"]);
			}
    }, params);
}

/**
 * 在分类中删除主机
 * @param obj 删除主机对象
 */
function deleteHostInDeploy(obj){
	var params = {
		ID:$(obj).attr("id"),
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		HOST_ID:$(obj).data("HOST_ID"),
		RUN_STATE:$(obj).data("RUN_STATE"),
		CLUSTER_ELE_TYPE:$(obj).data("CLUSTER_ELE_TYPE")
	};
	
	//删除分为两大类,业务类根据this主机是否有下挂task,基础类跟框架类根据this主机是否部署即STATE=1
	if(params["CLUSTER_ELE_TYPE"] == busVar.BUSINESS_TYPE){
		showConfirmMessageAlter("确认在该分类中删除此主机？", function(){
			getJsonDataByPost(Globals.baseActionUrl.CLUSTER_DIV_ACTION_DELETE_URL, params, "",
				function (result){
					//刷新该类下的主机列表
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], params["CLUSTER_ELE_TYPE"]);
                    showMessageTips("主机删除成功！");
			});
		});
	}else {
		if(params["RUN_STATE"] == 1){
            showWarnMessageTips("该主机正在运行,无法删除!");
			return;
		}
		
		//弹出确认框
		showConfirmMessageAlter("确认在该分类中删除此主机？", function(){
			getJsonDataByPost(Globals.baseActionUrl.DEPLOY_TASK_ACTION_DELETE_HOST_URL, params, "集群划分-删除主机以及远程目录",
					function (result){
				if(result.success="true"){
					//刷新该类下的主机列表
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], params["CLUSTER_ELE_TYPE"]);
                    showMessageTips("主机删除成功！") ;
				}else{
					showErrorMessageAlter("主机删除失败！");
				}
			});
		});
	}
	
}

/**
 * 查看分类中的主机详情 
 * @param obj 
 */
function scanHostInfo(obj){
	var queryParams = {
		HOST_ID:$(obj).data("HOST_ID")
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, queryParams,"集群划分-查询详细信息",
			function (result){
				if (result != null) {
					var queryParam=result[0];
					showDialog("详细信息",600,400,Globals.baseJspUrl.HOST_JSP_DETAIL_URL,
							function destroy(data){
				
					},queryParam);
				}
	   },"host.queryHostList");
}

/**
 * 加载右键菜单
 */
function loadingRightClick(){
	context.init({preventDoubleContext: false});
	context.settings({compress: true});
}
