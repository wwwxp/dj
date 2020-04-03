/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-7-19
 * Time: 下午16:17
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var param = new Object();
var paramData=new Object();
/**
 * 当前需要刷新的主机
 */
var currHostId = "";

//初使化
$(document).ready(function () {
    mini.parse();
    param["inputForm"] = new mini.Form("inputForm");
});

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
    paramData = data;
	//集群类型
	param["CLUSTER_TYPE"]=data["CLUSTER_TYPE"];
	//当前主机
	currHostId = data["HOST_ID"];
	//集群ID
	param["CLUSTER_ID"]=data["CLUSTER_ID"];
	//按钮对象
	param["sumbitButton"] = mini.get("sumbitButton");
	//加载右键
    loadingRightClick();
    //查询M2DB刷新数据主机
    queryM2dbHostDiv(currHostId);
    //加载M2DB实例列表
	loadInstanceList();
}

//初始化主机div
function queryM2dbHostDiv(checkHostId){
	//每次执行本方法之前,都将div中的内容清楚,使代码重用性高  
	$("#hostFitDiv").html("");
	//查询该类型未部署的所有主机
	var params = {
		CLUSTER_ID:param["CLUSTER_ID"],
		CLUSTER_TYPE:param["CLUSTER_TYPE"]
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "M2DB刷新内存-主机查询",
		function(result){
			var str = "";
			$.each(result, function (i, item) {
				var tips = "状&nbsp;&nbsp;态：&nbsp;" + (item.RUN_STATE == "1" ? "正在运行" : "未运行") + "\n上次部署版本：&nbsp;" + item.VERSION;
	        	str+='<ul class="ul_host" id="'+item.HOST_ID+'" style="width:130px;">'
	        		+'	<li style="height:60%;" title="' + tips + '">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_run_' + item.RUN_STATE + '.png" class="ul_host_img"/>'
	        		+'	</li>'
	        		+'	<li style="height:20%;">';
	        	//如果运行状态为运行，则不可选
	        	if(item.RUN_STATE==1){
	        		if (item.HOST_ID == checkHostId) {
	        			str+='	<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" data-SSH_USER="'+item.SSH_USER+'" name="ck_host" '
	        			   + '	checked=checked style="font-size:12px;" value="'+item.HOST_ID+'" />' + item.HOST_IP;
	        		} else {
	        			str+='	<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" data-SSH_USER="'+item.SSH_USER+'" name="ck_host" '
	        			   + '	style="font-size:12px;" value="'+item.HOST_ID+'" />' + item.HOST_IP;
	        		}
	        	} else if (item.RUN_STATE==0){//运行状态为未运行，则默认选中
	        		str+='	<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" '
	        			+'		data-SSH_USER="'+item.SSH_USER+'" name="ck_host" '
	        			+'		style="font-size:12px;" disabled="true" value="'+item.HOST_ID+'" />' + item.HOST_IP;
	        	}
	        	str+='	</li></ul>';
	        });
			$("#hostFitDiv").append(str);
			//主机上面添加右键功能
			$(".ul_host").hover(function() {
				var hostId = $(this).attr("id");
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
				context.attach("#"+hostId, array);
			});
	},"hostStart.queryStartHostList", "", false);
}

/**
 * 刷新
 */
function onSubmit(){
	param["inputForm"].validate();
    if (param["inputForm"].isValid() == false){
        return;
    }
	chooseToStart();
}

/**
 * 获取当前集群实例列表
 */
function loadInstanceList(){
	var instanceList = getInstanceList(param["CLUSTER_ID"], param["CLUSTER_TYPE"]);
	mini.get("instanceName").setData(instanceList);
}

//选择要添加的主机  
function chooseToStart(){
	var obj=document.getElementsByName('ck_host');
	var host_arry=new Array();
	//取到对象数组后，我们来循环检测它是不是被选中
	for(var i=0; i<obj.length; i++){
		if(obj[i].checked){
			var hostData = {
				HOST_ID:obj[i].value,
				CLUSTER_ID:param["CLUSTER_ID"],
				CLUSTER_TYPE:param["CLUSTER_TYPE"]
			};
			host_arry.push(hostData);
		}
		
	}
	//没有选中主机
	if(host_arry.length>0){
		var info = {};
		info["bolt"] = mini.get('bolt').getValue();
		info["hostList"] = host_arry;
		info["CLUSTER_ID"] = param["CLUSTER_ID"];
		info["CLUSTER_TYPE"] = param["CLUSTER_TYPE"];
		info["INSTANCE_NAME"] = mini.get("instanceName").getValue();
		
		showConfirmMessageAlter("确定该操作？",function ok(){
			param["sumbitButton"].setText("正在运行");
			param["sumbitButton"].setEnabled(false);
			
			var startTimes = (new Date()).getTime();
			var textValue = "刷新开始时间: " + ((new Date(startTimes)).format("yyyy-MM-dd hh:mm:ss")) + "<br/>";
			textValue +="正在运行...<br/>";
			$("#deployTextarea").html(textValue);
			
			//将滚动条自动滚动到最下面
			var deployDiv = document.getElementById("mainDiv");
			deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
			
			getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_M2DB_REFRESH_MEM_URL,info,"启停管理-刷新表m2db",
				function(result){
					//刷新输出信息
				    textValue+=result.success;
				    textValue=textValue.replaceAll("\n","<br/>");
				    var endTimes = (new Date()).getTime();
		        	textValue += "刷新结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss"));
				    $("#deployTextarea").html(textValue);
			    
				    queryM2dbHostDiv(currHostId);

				    //将滚动条自动滚动到最下面
		        	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
		        	
		        	param["sumbitButton"].setText("确定");
		        	param["sumbitButton"].setEnabled(true);
		        	
					var lowerValue = textValue.toLocaleLowerCase();
					if(lowerValue.indexOf(systemVar.SUCCESS) != -1) {
                        showMessageTips("操作成功！");
		        	}else{
		        		showErrorMessageAlter("操作失败！");
		        		//当关闭窗口时,用于判断是否重新加载
						param["submit_state"] =1;
		        	}
			},null,null,true,null,false);
		} );
	}else{
        showWarnMessageTips("请选择主机！");
	}
}

/**
 * 根据state判断是否部署过主机,如果部署过,关闭窗口后要刷新
 */
function close(){
	closeWindow(paramData);
}

function selectAll(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!array[i].disabled){
			array[i].checked = true;
		}
	}
}

function selectNone(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!array[i].disabled){
			array[i].checked = false;
		}
	}
}