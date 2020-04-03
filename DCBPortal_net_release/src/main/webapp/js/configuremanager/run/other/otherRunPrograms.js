
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
	 //程序下拉框对象
	 JsVar["programCb"] = mini.get("programCb");
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
	// 加载程序下拉框
	 loadProgramCombobox();
}

/**
 * 加在版本号下拉框
 * @returns
 */
function loadProgramCombobox(){
	var params = {
		PROGRAM_TYPE:JsVar["CLUSTER_TYPE"],
		PROGRAM_STATE:1
	};
	comboxLoad(JsVar["programCb"], params, "program.queryProgramList","","",false);
	var programData = JsVar["programCb"].getData();
	if (programData != null && programData.length > 0) {
		JsVar["programCb"].select(0);
	}
}

/**
 * 下拉框选择触发函数
 * @returns
 */
function reload(e){
	var PROGRAM_CODE = e.selected.PROGRAM_CODE;
	JsVar["PROGRAM_CODE"] = PROGRAM_CODE;
	JsVar["PROGRAM_ID"] = e.selected.PROGRAM_ID;
	JsVar["PROGRAM_NAME"] = JsVar["programCb"].getText();
	queryOtherHostDiv();
	index=0;
}

//初使化主机列表的div
function queryOtherHostDiv(){
	//每次执行本方法之前,都将div中的内容清除,使代码重用性高
	$("#hostFitDiv").html("");
	//查询符合的所有主机
	var param = {
		CLUSTER_TYPE:JsVar["CLUSTER_TYPE"],
		PROGRAM_CODE:JsVar["PROGRAM_CODE"],
		CLUSTER_ID:JsVar["CLUSTER_ID"],
		TASK_ID:JsVar["TASK_ID"]
	};
	//查询程序主机关联表数据，如果没有则查询当前集群版本部署主机列表
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,param,"",
		    function(result){
				//如果程序主机关联表数据为空，则查询部署所有主机列表
				if(result.length <1){
					getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,param,"",
						    function(res){
						result = res;
					},"taskProgram.queryhostByTaskIDOrNull", "", false);
				}
				var str="";
				$.each(result, function (i, item) {
		        	str+='<ul class="ul_host" style="width:165px;">'
		        		+'	<li style="height:70%;">'
		        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_run_'+item.RUN_STATE+'.gif" class="ul_host_img"/>'
		        		+'	</li>'
		        		+'	<li style="height:30%;">';
		         
		        	if(item.RUN_STATE==0){
		        		str+='	<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" data-PROGRAM_ID="'+item.PROGRAM_ID+'" name="ck_host" checked=checked'
		        		   +'		"style="font-size:12px;" value="'+item.HOST_ID+'" />'+item.HOST_IP ;
		        	}else{
		        		str+='	<input type="checkbox" id="'+item.HOST_IP+'" data-HOST_IP="'+item.HOST_IP+'" data-PROGRAM_ID="'+item.PROGRAM_ID+'" name="ck_host" ';
		        		str+='		sum_run="run" onclick="runLoad(id)"';
		        		str+=' 	"style="font-size:12px;" value="'+item.HOST_ID+'" />' +item.HOST_IP;
		        	}
		        	str+='	</li>'
		        		+'</ul>';
		    });
			$("#hostFitDiv").append(str);
	  },"taskProgram.queryhostByTaskID","",false);
}

/**
 * 启动
 */
function runPrograms(){
	var obj = document.getElementsByName('ck_host');
	var host_arry = new Array();
	//取到对象数组后，我们来循环检测它是不是被选中
	for(var i=0; i<obj.length; i++){
		if(obj[i].checked){
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
		showConfirmMessageAlter("确定启动该程序吗？",function ok(){
			sumbit(host_arry);
		} );
	}else{//没有选中主机
        showWarnMessageTips("请选择主机！");
	}
}



var index=0;
var textValue="";
var flagError= "";
function sumbit(host_arry){
	JsVar["sumbitButton"].setText("正在启动");
	JsVar["sumbitButton"].setEnabled(false);
	
	index = 0;
	textValue = "";
	flagError = "";
	startTimes = (new Date()).getTime();
	textValue += "启动开始时间: " + ((new Date(startTimes)).format("yyyy-MM-dd hh:mm:ss")) + "<br/>";
	
	var is_param = JsVar["IS_PARAM"];
    var info = {};
    if(is_param == 1){
        info["IS_PARAM"] = is_param;
        info["PARAM_DESC"] = JsVar["PARAM_DESC"];
    	showDialog("运行",450,200,Globals.baseJspUrl.OTHER_PROGRAM_JSP_INPUT_PARAM_URL,
    	        function destroy(data){
    				if(data && data["flag"]){
    					postAjax(host_arry,data["params"]);
    				}
		    		
    	    }, info);
    }else{
       postAjax(host_arry,info);
    }
}

/**
 * 程序启动
 * @param host_arry
 * @param obj
 */
function postAjax(host_arry,obj){
	var info = host_arry[index];
	//obj["list"] = [info];
	
	var params = {
		CLUSTER_ID:JsVar["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["CLUSTER_TYPE"],
		IS_PARAM:JsVar["IS_PARAM"],
		HOST_LIST:[info],
		TASK_ID:JsVar["TASK_ID"],
		TASK_CODE:JsVar["TASK_CODE"],
		VERSION:JsVar["VERSION"],
		INPUT_PARAM:obj["INPUT_PARAM"]
	};
	
	textValue +="主机【"+info["HOST_IP"]+"】，正在启动【"+info["PROGRAM_NAME"]+"】 ...<br/>";
	$("#deployTextarea").html(textValue);
	
	//将滚动条自动滚动到最下面
	var deployDiv = document.getElementById("mainDiv");
	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	
	getJsonDataByPost(Globals.baseActionUrl.OTHER_RUN_ACTION_MANAGE_URL, params, "启停管理-运行other周边程序",
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
		        	textValue += "启动结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss")) + ", 本次启动"+host_arry.length+"台主机程序, 总耗时: " + ((endTimes - startTimes)/1000).toFixed(2) + "秒";
		        	$("#deployTextarea").html(textValue);
		 
		        	queryHostDiv();
					
		        	JsVar["sumbitButton"].setText("启动");
					JsVar["sumbitButton"].setEnabled(true);
					if(flagError.length>1) {
		        		showErrorMessageAlter(flagError+"启动失败");
		        	}else{
                        showMessageTips("启动成功");
		        	}
		        }else{
		        	postAjax(host_arry,obj);
		        }
		        //高亮检索“失败”“成功”关键字
				heightLightKeyWord();
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