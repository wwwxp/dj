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
    //加载集群类型
    loadingClusterConfig();
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
			var tabs=mini.get("#deploy_tabs");
			$.each(result, function (i, item) {
				var tab = {
					title:item.BUS_CLUSTER_NAME,
					id:item.BUS_CLUSTER_ID,
					code:item.BUS_CLUSTER_CODE,
					dataField:item.BUS_CLUSTER_ID, 
					showCloseButton: false
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
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL + "?BUS_CLUSTER_ID=" + busClusterId, params, "部署管理-查询全部部署分类",
        function(result){
			$("#fitDiv_cont").html("");
			var data = JsVar["PROGREAM_DATA"];
			for(var j=0;j<data.length;j++){
				var configValue = data[j].CONFIG_VALUE;//分类
				//分类的jq对象，data-value主要给隐藏分类和显示分类时用
				var headStr="<div style='margin:0px 0px 20px 0px;'>";
				headStr += '<div style="height:40px;">';
				if (configValue == busVar.COMPONENT_TYPE) {
	            	headStr += '<image src="'+Globals.ctx+'/images/component.png" class="bus_img" />';
	            } else {
	            	headStr += '<image src="'+Globals.ctx+'/images/business.png"  class="bus_img" />';
	            }
				headStr += '<span class="classify_name" data-value="'+configValue+'">'+data[j].CONFIG_NAME+'</span>';
				if (configValue == busVar.BUSINESS_TYPE) {
					headStr +=  '<div class="operate_block">';
					headStr	+=  '	<input type="button" value="部署" id="deploy_but_busPrograms_1" ';
					headStr +=  '		class="operate_but" onClick="deploy(this, true)"/>';				
				 	headStr +=  '</div>';
				}
				headStr += '</div>';
				//返回的是每一个集群下的主机列表
				if(result.length>0){
					//循环每一个数组
					$.each(result, function (i, item) {
						if(item.TYPE == configValue){
							//拼接类型名的html
							headStr+= loadDeployList(item, configValue);
						}
		            });
				}
				headStr+="</div>";
				//装成jq对象
				typeJqObj=$(headStr);
				
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
				
				//显示和隐藏分组的事件
				j$(typeJqObj.find(".div_3")).off("click").on("click",function(){
	               //如果当前状态为隐藏，则显示出来，反之显示出来
	               if($(this).parent().next("div").is(":hidden")){
	                   $(this).parent().next("div").show(500);
	               }else{
	                   $(this).parent().next("div").hide(500);
	               }
			    });
				
				$("#fitDiv_cont").append(typeJqObj);
				
				//组件默认折叠
	            if (configValue == busVar.COMPONENT_TYPE) {
	            	j$(typeJqObj.find("span")[0]).click();
	            }
			}
        }, "serviceType.queryAllDeployByBusClusterId");
}

/**
 * 部署分类名字和ID拼接
 */
function loadDeployList(item, type){
	var str = "";
	if (busVar.BUSINESS_TYPE == type) {
		str += '<div class="div_1" data-type="'+type+'">'
			+  '	<div class="div_2">'
		    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
		    +  '		<div class="operate_block">'
	 		
			+  '		</div>'
			+  '		<div style="clear:both;"></div>'
			+  '	</div>'
			+  '	<div class="all_center"  style="display: none;" id="all_center_'+item.CLUSTER_ID+'"></div>'
			+  '</div>';
		loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME, 2);
	} else {
		str += '<div class="div_1" data-type="'+type+'">'
			+  '	<div class="div_2">'
		    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
		    +  '		<div class="operate_block">'
//	 		+  '			<input type="button" value="部署" '
//	 		+  '				data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
//	 		+  '				data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
//			+  '				data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" '
//			+  '				data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
//	 		+  '				class="operate_but" id="'+item.CLUSTER_ID+'" onClick="deploy(this, false)"/>'
			+  '		</div>'
			+  '		<div style="clear:both;"></div>'
			+  '	</div>'
			+  '	<div class="all_center"  id="all_center_'+item.CLUSTER_ID+'"></div>'
			+  '</div>';
		loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME, busVar.COMPONENT_TYPE);
	}
	return str;
}

/**
 * 加载类型下的主机列表
 * @param CODE
 */
function loadHostList(CLUSTER_ID, CLUSTER_CODE, CLUSTER_TYPE, CLUSTER_NAME, flag){
	var sqlCode = "deployHome.queryDeployHostAllCodeList";
	//flag 代表1 是组件，2是业务
	if(flag == 2){
		sqlCode = "deployHome.queryHostByDeploy";
	}
	var params = {
		CLUSTER_ID:CLUSTER_ID,
		CLUSTER_NAME:CLUSTER_NAME,
		CLUSTER_CODE:CLUSTER_CODE,
		CLUSTER_TYPE:CLUSTER_TYPE
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署图-查询部署分类下的所有主机",
	        function(result){
				var host_str="";
				//每次开始前将中间主机显示区域的内容清空
				$("#all_center_" + CLUSTER_ID).html("");
				if(result.length>0){
					//循环每一个数组
					$.each(result, function (i, item) {
						host_str='<ul class="ul_host" id="' + item.ID + '" data-ID="'+item.ID+'" data-CLUSTER_ID="'+item.CLUSTER_ID+'" data-CLUSTER_NAME="'+item.CLUSTER_NAME+'"' 
								+' data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" data-HOST_ID="'+item.HOST_ID+'" >';
						if(item.VERSION != null && item.VERSION.length > 0){
							host_str += '	<li style="height:60%;" title="版本号：' + item.VERSION + '">';
						}else{
							host_str += '	<li style="height:60%;">';
						}
						host_str +='		<image src="'+Globals.ctx+'/images/deployHost/module_'+item.STATE+'.png" class="ul_host_img"/>'
								  +'	</li>'
								  +'	<li style="height:40%;">'+item.HOST_IP + '<br/>(' + item.SSH_USER + ')'+'</li>'
								  +'</ul>';
						
						//将host_str包含的html封装成jq对象，给每个对象加上hover事件
						var host_jq=$(host_str);
						host_jq.hover(function () {
							var obj = $(this);
							//给类加上菜单
							var array = new Array();
							array.push({header: '右击菜单'});
							if(obj.data("CLUSTER_TYPE") != 'billing' && obj.data("CLUSTER_TYPE") != 'other'){
								array.push({text: '部署', action: function(e){
									e.preventDefault();
									deployHost(obj);
								}});
							}
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
	        },sqlCode);
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
 * 按钮：部署
 * @param obj 集群对象
 * @param isBusiness 是否业务集群
 */
function deploy(obj, isBusiness){
	if (isBusiness) {
		var params = {
			BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
			BUS_CLUSTER_CODE:JsVar["BUS_CLUSTER_CODE"]
		};
		showDialog("部署业务程序","98%", "98%", Globals.baseJspUrl.DEPLOY_BUS_PROGRAMS_JSP_URL,
		        function destroy(data){
					//刷新当前Tab
			    	if(data == systemVar.SUCCESS){
			    		var activeTab = window.parent.mini.get("deploy_tabs").getActiveTab();
			    		window.parent.mini.get("deploy_tabs").updateTab(activeTab);
			    	}
		    },params, {allowDrag:false});
	} else {
		var params = {
			CLUSTER_ID:$(obj).data("CLUSTER_ID"),
			CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
			CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
			CLUSTER_NAME:$(obj).data("CLUSTER_NAME")
		};
		var title = "部署:" + params["CLUSTER_NAME"];
		showDialog(title,"98%","98%",Globals.baseJspUrl.DEPLOY_JSP_DEPLOY_HOST_URL,
		    function destroy(data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], 1);
		    },params, {allowDrag:false});
	}
}

/**
 * 在分类中部署主机
 * @param obj 组件对象 
 */
function deployHost(obj){
	var params = {
		ID:$(obj).data("ID"),               //划分ID
		HOST_ID:$(obj).data("HOST_ID"),  	    //主机ID
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),        //集群ID
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),        //集群ID
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),          //集群类型
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME")          //集群名称
	};
	var tips = "部署" + params["CLUSTER_NAME"];
	showDialog(tips, "98%", "98%", Globals.baseJspUrl.DEPLOY_JSP_DEPLOY_HOST_URL,
	    function destroy(data){
		      if(data==systemVar.SUCCESS){
		    	  loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"]);
		      }
	    },params, {allowDrag:false});
}

/**
 * 查看分类中的主机详情
 * @param obj
 */
function scanHostInfo(obj){
	var params = {
		HOST_ID:$(obj).data("HOST_ID")
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,JsVar,"主机管理-查询详细信息",
			function (result){
			var param=result[0];
			showDialog("详细信息",600,400,Globals.baseJspUrl.HOST_JSP_DETAIL_URL,
					function destroy(data){
				
			 },param);
	   },"host.queryHostList");
}

/**
 * 加载右键菜单
 */
function loadingRightClick(){
	context.init({preventDoubleContext: false});
	context.settings({compress: true});
}

/**
 * 拼接新建部署按钮
 */
function loadCreateDeployButton(){
	str+='<div style="text-align:center;height:50px;">'
		+'<input type="button" value="新建部署分类" class="add_button" onClick="adddeploy()" />'
		+'</div>';
}
