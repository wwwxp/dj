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
    //加载右键
    loadingRightClick();
    //组件集群部署
    findAllDeploy();
});

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
 *  发送ajax请求获取部署分类(zk/nimbus等)
 */
function findAllDeploy(){
	//获取Tab展示业务类型
	var params = {
		TYPE: busVar.COMPONENT_TYPE
	};
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署管理-查询全部部署分类",
        function(result){
			$("#fitDiv_cont").html("");
			var headStr = "";	
			//遍历集群，获取集群对应的主机列表
			if(result.length>0){
				//循环每一个数组
				$.each(result, function (i, item) {
					headStr+= loadDeployList(item, busVar.COMPONENT_TYPE);
	            });
			}
			headStr+="</div></div>";
			//装成jq对象
			typeJqObj=$(headStr);
			
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
        },"serviceType.queryAllDeploy");
}

/**
 * 部署分类名字和ID拼接
 */
function loadDeployList(item, type){
	var str = "";
    str += '<div class="div_1" data-type="'+type+'">'
		+  '	<div class="div_2">'
	    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
	    +  '		<div class="operate_block">'
		+  '			<input type="button" value="部署" id="'+item.CLUSTER_ID+'" '
		+  '				data-CLUSTER_ID="'+item.CLUSTER_ID+'" ' 
		+  '				data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
		+  '				data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" '
		+  '				data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
 		+  '				class="operate_but"  onClick="deploy(this, false)"/>'
		+  '		</div>'
		+  '		<div style="clear:both;"></div>'
		+  '	</div>'
		+  '	<div class="all_center"  style="display: none;" id="all_center_'+item.CLUSTER_ID+'"></div>'
		+  '</div>';
	
	loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME, 1);
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
						host_str='<ul class="ul_host" id="' + item.ID + '" '
								+' data-ID="'+item.ID+'" '
								+' data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
								+' data-CLUSTER_NAME="'+item.CLUSTER_NAME+'"' 
								+' data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'"'
								+' data-CLUSTER_CODE="'+item.CLUSTER_CODE+'"'
								+' data-HOST_ID="'+item.HOST_ID+'" >';
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
							
							array.push({text: '部署', action: function(e){
								e.preventDefault();
								deployHost(obj);
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
    //点添加按钮如果该 div 隐藏的话，则显示出来
    $(obj).parents('.div_1').find(".all_center").css("display","block");
	if (isBusiness) {

		var params = {
			CLUSTER_TYPE_LIST:["billing", "other"]
		};
		showDialog("部署业务程序","98%", "98%", Globals.baseJspUrl.DEPLOY_BUS_PROGRAMS_JSP_URL,
		        function destroy(data){
					//刷新当前Tab
			    	if(data == systemVar.SUCCESS){
			    		var activeTab = mini.get("deploy_tabs").getActiveTab();
			    		mini.get("deploy_tabs").updateTab(activeTab);
			    	}
		    },params, {allowDrag:false});
	} else {
		var params = {
			CLUSTER_ID:$(obj).data("CLUSTER_ID"),
			CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
			CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
			CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
            IFRAME:"elePages"
		};
		var title = "部署:" + params["CLUSTER_NAME"];


        showDialogJumpPage(title,"98%","98%",Globals.baseJspUrl.DEPLOY_JSP_DEPLOY_HOST_URL,
            function destroy(data){
                loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
            },params, {allowDrag:false},2+params["CLUSTER_ID"],true);
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
		    	  loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"], 1);
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


function reloadComplete(data) {
    if(!isEmptyStr(data["CLUSTER_ID"])){
        var $div = "#all_center_"+ data["CLUSTER_ID"];
        $($div).css("display","block");
        //定位到操作按钮
        $($div).prev()[0].scrollIntoView();
    }
    loadHostList(data["CLUSTER_ID"], data["CLUSTER_CODE"], data["CLUSTER_TYPE"], data["CLUSTER_NAME"], 1);
}