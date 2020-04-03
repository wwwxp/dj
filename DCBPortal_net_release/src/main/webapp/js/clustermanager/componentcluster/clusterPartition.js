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
    //获取组件部署情况
    findAllDeploy();
});

/**
 *  发送ajax请求获取部署分类(zk/nimbus等)
 */
function findAllDeploy(){
	var params = {
		TYPE: busVar.COMPONENT_TYPE
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署图-查询全部部署分类",
        function(result){
			$("#fitDiv_cont").html("");
			var typeJqObj=$('<div></div>');
			if(result.length>0){
				//循环每一个数组
				$.each(result, function (i, item) {
					//拼接类型名的html
					var deployHtml = loadDeployList(item, busVar.COMPONENT_TYPE);
		            var contentJq= $(deployHtml);
                    typeJqObj.append(contentJq);
	            });
				
				//显示和隐藏分组的事件
				j$(typeJqObj.find(".div_3")).off("click").on("click",function(){
	               //如果当前状态为隐藏，则显示出来，反之显示出来
	               if($(this).parent().next("div").is(":hidden")){
	                   $(this).parent().next("div").show(500);
	                   //$(this).parent().next("div").css({"border":"none","margin":"0px 0px 0px 0px"});
	               }else{
	                   $(this).parent().next("div").hide(500);
	                   //隐藏时，给分类的div给出一个边框
	                   //$(this).parent().next("div").css({"border":"1px dashed #D9E4F1", "border-bottom":"0px dashed #D9E4F1", "margin":"0px 0px 20px 0px"});
	               }
			    });
			}
            //把拼装好的html jq对象放到content里面
            $("#fitDiv_cont").append(typeJqObj);
        },"serviceType.queryAllDeploy","",false);
}

/**
 * 部署分类名字和ID拼接
 */
function loadDeployList(item, type){
    var str="";
    //data-value主要给隐藏分了和显示分类时用
    str += '<div class="div_1" data-type="'+type+'">'
    	+  '	<div class="div_2">'
	    +  '		<div class="div_3">'+item.CLUSTER_NAME+'</div>'
	    +  '		<div class="operate_block">'
		+  '			<image src="'+Globals.ctx+'/images/deployHost/module_add.png" '
		+  '				data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
		+  '				data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
		+  '				data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" '
		+  '				data-CLUSTER_CODE="'+item.CLUSTER_CODE+'" '
		+  '				data-JSTORM_RUN="'+item.JSTORM_RUN+'" '
		+  '				title="添加" class="operate_but1" id="add_but_'+item.CLUSTER_ID+'" onClick="addType(this)"/>'
		+  '		</div>'
		+  '		<div style="clear:both;"></div>'
		+  '	</div>'
    	+  '	<div class="all_center" style="display: none;" id="all_center_'+item.CLUSTER_ID+'"></div>'
    	+  '</div>';
    
    //加载类型下的主机
    loadHostList(item.CLUSTER_ID, item.CLUSTER_CODE, item.CLUSTER_TYPE, item.CLUSTER_NAME);
    
    return str;
}

/**
 * 加载类型下的主机列表
 * @param CLUSTER_ID  集群ID
 * @param CLUSTER_CODE 集群编码
 * @param CLUSTER_TYPE 集群类型
 */
function loadHostList(CLUSTER_ID, CLUSTER_CODE, CLUSTER_TYPE, CLUSTER_NAME){
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
						host_str='<ul class="ul_host" id="'+item.ID+'" data-CLUSTER_ID="'+item.CLUSTER_ID+'" '
								+'  data-CLUSTER_NAME="'+item.CLUSTER_NAME+'" '
								+'  data-CLUSTER_TYPE="'+item.CLUSTER_TYPE+'" data-HOST_ID="'+item.HOST_ID+'" '
								+'  data-RUN_STATE="'+item.RUN_STATE+'" data-CLUSTER_ELE_TYPE="'+item.CLUSTER_ELE_TYPE+'">'
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
	        }, "deployHome.queryDeployHostAllCodeList");
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
		CLUSTER_ID:$(obj).data("CLUSTER_ID"),
		CLUSTER_NAME:$(obj).data("CLUSTER_NAME"),
		CLUSTER_CODE:$(obj).data("CLUSTER_CODE"),
		CLUSTER_TYPE:$(obj).data("CLUSTER_TYPE"),
		JSTORM_RUN:$(obj).data("JSTORM_RUN")
	};
    //点添加按钮如果该 div 隐藏的话，则显示出来
    $(obj).parents('.div_1').find(".all_center").css("display","block");

	showAddDialog("添加主机",780,470,Globals.baseJspUrl.DEPLOY_JSP_ADD_HOST_URL,
	    function destroy(data){
			if(systemVar.SUCCESS==data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"]);
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
		JSTORM_RUN:$(obj).data("JSTORM_RUN")
	};
	showAddDialog("批量删除",780,470,Globals.baseJspUrl.DEPLOY_JSP_BATCH_DEL_HOST_URL,
		function destroy(data){
			if(systemVar.SUCCESS==data){
				loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"]);
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
	
	//业务类型
	if(params["CLUSTER_ELE_TYPE"] == busVar.BUSINESS_TYPE){
		showConfirmMessageAlter("确认在该分类中删除此主机？", function(){
			getJsonDataByPost(Globals.baseActionUrl.CLUSTER_DIV_ACTION_DELETE_URL, params, "",
				function (result){
					//刷新该类下的主机列表
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"]);
                    showMessageTips("主机删除成功！");
			});
		});
	}else {
		//组件类型
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
					loadHostList(params["CLUSTER_ID"], params["CLUSTER_CODE"], params["CLUSTER_TYPE"], params["CLUSTER_NAME"]);
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
