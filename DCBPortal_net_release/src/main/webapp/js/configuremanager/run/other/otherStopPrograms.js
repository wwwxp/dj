
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
	mini.parse();
	JsVar["PROGRAMBOX"] = mini.get("PROGRAMBOX");
	JsVar["sumbitButton"] = mini.get("sumbitButton");
	//按钮对象
	JsVar["sumbitButton"] = mini.get("sumbitButton");
	//集群ID
	JsVar["CLUSTER_ID"] = data["CLUSTER_ID"];
	//集群类型
	JsVar["CLUSTER_TYPE"] = data["CLUSTER_TYPE"];
	//任务ID
	JsVar["TASK_ID"] = data["TASK_ID"];
	//任务编码
	JsVar["TASK_CODE"] = data["TASK_CODE"];
	//当前版本
	JsVar["VERSION"] = data["VERSION"];
	
	//JsVar["data"] = data ;
	//JsVar["version"] = data["TASK_CODE"].toUpperCase().substring(data["TASK_CODE"].toUpperCase().indexOf("_V") + 1);
    loadProgramCombobox();// 加载程序下拉框
	//queryHostDiv(JsVar["PROGRAMBOX"].getValue());
}

/**
 * 加在版本号下拉框
 * @returns
 */
function loadProgramCombobox(){
	comboxLoad(JsVar["PROGRAMBOX"],{PROGRAM_TYPE:"other"},"program.queryProgramList","","",false);
	JsVar["PROGRAMBOX"].select(0);
}

/**
 * 下拉框选择触发函数
 * @returns
 */
function reload(e){
	var PROGRAM_CODE = e.selected.PROGRAM_CODE;
	JsVar["PROGRAM_CODE"] = PROGRAM_CODE;
	JsVar["PROGRAM_ID"] = e.selected.PROGRAM_ID;
	JsVar["PROGRAM_NAME"] = JsVar["PROGRAMBOX"].getText();
	queryHostDiv();
	index=0;
} 

//初使化主机列表的div
function queryHostDiv(){
	//每次执行本方法之前,都将div中的内容清除,使代码重用性高
	$("#hostFitDiv").html("");
	//查询符合的所有主机
	var param={
		CLUSTER_ID:JsVar["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["CLUSTER_TYPE"],
		PROGRAM_CODE:JsVar["PROGRAM_CODE"],
		TASK_ID:JsVar["TASK_ID"]
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,param,"",
		    function(result){
	    var str="";
		$.each(result, function (i, item) {
        	str+='<ul class="ul_host" style="width:165px;">'
        		+'	<li style="height:70%;">'
        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_run_'+item.RUN_STATE+'.png" class="ul_host_img"/>'
        		+'	</li>'
        		+'	<li style="height:30%;">';
         
        	if(item.RUN_STATE==1){
        		str+='	<input type="checkbox" id="'+item.HOST_IP+'" name="ck_host" checked=checked';
        		
        	}else{
        		str+='	<input type="checkbox" id="'+item.HOST_IP+'" name="ck_host" ';
        		str+='  sum_run="run" ';
        	}
    		str+=' "style="font-size:12px;" data-HOST_IP="'+item.HOST_IP+'" data-RUN_STATE="'+item.RUN_STATE+'" data-PROGRAM_ID="'+item.PROGRAM_ID+'" value="'+item.HOST_ID+'" />' +item.HOST_IP;
        	str+='	</li>'
        		+'</ul>';
        });
		$("#hostFitDiv").append(str);
	  },"taskProgram.queryhostByTaskID","",false);
	
}

//选择要添加的主机
function stopPrograms() {
	var obj = document.getElementsByName('ck_host');
	var host_arry = new Array();
	//取到对象数组后，我们来循环检测它是不是被选中
	for(var i=0; i<obj.length; i++) {
		if(obj[i].checked) {
			var hostData = {
				HOST_ID:obj[i].value,
				HOST_IP:$(obj[i]).data("HOST_IP"),
				PROGRAM_CODE:JsVar["PROGRAM_CODE"],
				TASK_ID:JsVar["TASK_ID"],
				PROGRAM_ID:$(obj[i]).data("PROGRAM_ID"),
				PROGRAM_NAME:JsVar["PROGRAM_NAME"],
				CLUSTER_ID:JsVar["CLUSTER_ID"],
				CLUSTER_TYPE:JsVar["CLUSTER_TYPE"],
				VERSION:JsVar["VERSION"]
			};
			//将对象放入数组
			host_arry.push(hostData);
		}
	}
	
	//选中主机
	if(host_arry.length>0){
		showConfirmMessageAlter("确定停止该程序吗？",function ok(){
			sumbit(host_arry);
		} );
	}else{//没有选中主机
        showWarnMessageTips("请选择主机！");
	}
}

/**
 * 停止主机
 * @param host_arry
 */
function sumbit(host_arry){
	JsVar["sumbitButton"].setText("正在停止");
	JsVar["sumbitButton"].setEnabled(false);
	$("#stopTextarea").val("正在停止。。。");
	postAjax(host_arry);
}

var index=0;
var textValue="";
var flagError= "";
function postAjax(host_arry){
	var info = host_arry[index];
	
	//停止程序业务参数
	var params = {
		CLUSTER_ID:JsVar["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["CLUSTER_TYPE"],
		HOST_LIST:[info],
		TASK_ID:JsVar["TASK_ID"],
		TASK_CODE:JsVar["TASK_CODE"],
		VERSION:JsVar["VERSION"]
	};
	
	$("#stopTextarea").text(textValue);
	
	//将滚动条自动滚动到最下面
	var deployDiv = document.getElementById("mainDiv");
	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	
	getJsonDataByPost(Globals.baseActionUrl.OTHER_STOP_ACTION_MANAGE_URL, params, "启停管理-运行other周边程序",
			function(result){  
		
				if(result["flag"]=='error'){
					flagError += info.HOST_IP+",";
				}
				index++;
				textValue=$("#deployTextarea").html() +"<br/>" + result.info + "<br/>返回结果:"+result.reason+"<br/>";
				textValue=textValue.replaceAll("\n","<br/>");
				$("#deployTextarea").html(textValue);
		        if(host_arry.length==index){
					var endTimes = (new Date()).getTime();
		        	textValue += "停止结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss")) + ", 本次停止"+host_arry.length+"台主机程序, 总耗时: " + ((endTimes - startTimes)/1000).toFixed(2) + "秒";
		        	$("#deployTextarea").html(textValue);
		 
		        	queryHostDiv();
					
		        	JsVar["sumbitButton"].setText("停止");
					JsVar["sumbitButton"].setEnabled(true);
					if(flagError.length>1) {
		        		showErrorMessageAlter(flagError+"停止失败");
		        	}else{
                        showMessageTips("停止成功");
		        	}
		        }else{
		        	postAjax(host_arry);
		        }
		        //高亮检索“失败”“成功”关键字
				heightLightKeyWord();
		    },null,null,true,null,false);
}

/**
 * 根据state判断是否部署过主机,如果部署过,关闭窗口后要刷新
 */
function close(){
	if(JsVar["deploy_state"] ==1){
		closeWindow(systemVar.SUCCESS);
	}else{
		closeWindow();
	}
}

function selectAll(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!($(array[i]).attr("sum_run"))){
			array[i].checked = true;
		}
	}
}

function selectNone(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		array[i].checked = false;
	}
}

/**
 * 已运行复选框点击事件
 * @returns
 */
function runLoad(id){
    showWarnMessageTips("该主机正在运行");
	document.getElementById(id).checked = false;
}